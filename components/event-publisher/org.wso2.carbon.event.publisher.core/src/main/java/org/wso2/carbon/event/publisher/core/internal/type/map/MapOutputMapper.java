/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.event.publisher.core.internal.type.map;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.publisher.core.config.EventOutputProperty;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfiguration;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.config.mapping.MapOutputMapping;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherStreamValidationException;
import org.wso2.carbon.event.publisher.core.internal.OutputMapper;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MapOutputMapper implements OutputMapper {

    EventPublisherConfiguration eventPublisherConfiguration = null;
    Map<String, Integer> propertyPositionMap = null;
    private int noOfMetaData = 0;
    private int noOfCorrelationData = 0;
    private int noOfPayloadData = 0;
    private StreamDefinition streamDefinition;

    public MapOutputMapper(EventPublisherConfiguration eventPublisherConfiguration,
                           Map<String, Integer> propertyPositionMap,
                           int tenantId, StreamDefinition streamDefinition) throws
            EventPublisherConfigurationException {
        this.eventPublisherConfiguration = eventPublisherConfiguration;
        this.propertyPositionMap = propertyPositionMap;
        if (eventPublisherConfiguration.getOutputMapping().isCustomMappingEnabled()) {
            validateStreamDefinitionWithOutputProperties();
        } else {
            this.streamDefinition = streamDefinition;
            noOfMetaData = streamDefinition.getMetaData() != null ? streamDefinition.getMetaData().size() : 0;
            noOfCorrelationData = streamDefinition.getCorrelationData() != null ? streamDefinition.getCorrelationData().size() : 0;
            noOfPayloadData = streamDefinition.getPayloadData() != null ? streamDefinition.getPayloadData().size() : 0;
        }

    }

    private void validateStreamDefinitionWithOutputProperties()
            throws EventPublisherConfigurationException {

        MapOutputMapping mapOutputMapping = (MapOutputMapping) eventPublisherConfiguration.getOutputMapping();
        List<EventOutputProperty> outputPropertyConfiguration = mapOutputMapping.getOutputPropertyConfiguration();

        Iterator<EventOutputProperty> outputPropertyConfigurationIterator = outputPropertyConfiguration.iterator();
        for (; outputPropertyConfigurationIterator.hasNext(); ) {
            EventOutputProperty outputProperty = outputPropertyConfigurationIterator.next();
            String valueOf = outputProperty.getValueOf();
            if (!propertyPositionMap.containsKey(valueOf) && (valueOf == null || !valueOf.startsWith(EventPublisherConstants.PROPERTY_ARBITRARY_DATA_MAP_PREFIX))) {
                throw new EventPublisherStreamValidationException("Property " + valueOf + " is not in the input stream definition. ", streamDefinition.getStreamId());
            }
        }
    }

    @Override
    public Object convertToMappedOutputEvent(Event event)
            throws EventPublisherConfigurationException {
        Map<Object, Object> eventMapObject = new TreeMap<Object, Object>();
        Object[] eventData = ArrayUtils.addAll(ArrayUtils.addAll(event.getMetaData(), event.getCorrelationData()), event.getPayloadData());
        Map<String, String> arbitraryDataMap = event.getArbitraryDataMap();

        MapOutputMapping mapOutputMapping = (MapOutputMapping) eventPublisherConfiguration.getOutputMapping();
        List<EventOutputProperty> outputPropertyConfiguration = mapOutputMapping.getOutputPropertyConfiguration();

        if (outputPropertyConfiguration.size() != 0 && eventData.length > 0) {
            for (EventOutputProperty eventOutputProperty : outputPropertyConfiguration) {
                String valueOf = eventOutputProperty.getValueOf();
                Integer position = propertyPositionMap.get(valueOf);
                if (position != null) {
                    eventMapObject.put(eventOutputProperty.getName(), eventData[position]);
                } else if (valueOf != null && arbitraryDataMap != null && valueOf.startsWith(EventPublisherConstants.PROPERTY_ARBITRARY_DATA_MAP_PREFIX)) {
                    eventMapObject.put(eventOutputProperty.getName(), arbitraryDataMap.get(valueOf.replaceFirst(EventPublisherConstants.PROPERTY_ARBITRARY_DATA_MAP_PREFIX, "")));
                }
            }
        }
        return eventMapObject;
    }

    @Override
    public Object convertToTypedOutputEvent(Event event) throws EventPublisherConfigurationException {

        Map<Object, Object> eventMapObject = new TreeMap<Object, Object>();
        int counter = 0;
        Object[] eventData = ArrayUtils.addAll(ArrayUtils.addAll(event.getMetaData(), event.getCorrelationData()), event.getPayloadData());
        if (noOfMetaData > 0) {
            for (Attribute metaData : streamDefinition.getMetaData()) {
                eventMapObject.put(EventPublisherConstants.PROPERTY_META_PREFIX + metaData.getName(), eventData[counter]);
                counter++;
            }
        }

        if (noOfCorrelationData > 0) {
            for (Attribute correlationData : streamDefinition.getCorrelationData()) {
                eventMapObject.put(EventPublisherConstants.PROPERTY_CORRELATION_PREFIX + correlationData.getName(), eventData[counter]);
                counter++;
            }
        }

        if (noOfPayloadData > 0) {
            for (Attribute payloadData : streamDefinition.getPayloadData()) {
                eventMapObject.put(payloadData.getName(), eventData[counter]);
                counter++;
            }
        }

        Map<String, String> arbitraryDataMap = event.getArbitraryDataMap();
        if (arbitraryDataMap != null) {
            for (Map.Entry<String, String> entry : arbitraryDataMap.entrySet()) {
                eventMapObject.put(EventPublisherConstants.PROPERTY_ARBITRARY_DATA_MAP_PREFIX + entry.getKey(), entry.getValue());
            }
        }

        return eventMapObject;
    }
}
