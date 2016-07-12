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
package org.wso2.carbon.event.publisher.core.internal.type.wso2event;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.publisher.core.config.EventOutputProperty;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfiguration;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.config.mapping.WSO2EventOutputMapping;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherStreamValidationException;
import org.wso2.carbon.event.publisher.core.internal.OutputMapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WSO2EventOutputMapper implements OutputMapper {

    private WSO2EventOutputMapping wso2EventOutputMapping;
    private EventPublisherConfiguration eventPublisherConfiguration = null;
    private Map<String, Integer> propertyPositionMap = null;
    private final int tenantId;
    private final StreamDefinition inputStreamDefinition;

    public WSO2EventOutputMapper(EventPublisherConfiguration eventPublisherConfiguration,
                                 Map<String, Integer> propertyPositionMap,
                                 int tenantId, StreamDefinition inputStreamDefinition) throws
            EventPublisherConfigurationException {
        this.eventPublisherConfiguration = eventPublisherConfiguration;
        this.propertyPositionMap = propertyPositionMap;
        this.tenantId = tenantId;
        this.inputStreamDefinition = inputStreamDefinition;

        wso2EventOutputMapping = (WSO2EventOutputMapping) eventPublisherConfiguration.getOutputMapping();
        if (wso2EventOutputMapping.getToEventName() == null || wso2EventOutputMapping.getToEventName().isEmpty() || wso2EventOutputMapping.getToEventVersion() == null || wso2EventOutputMapping.getToEventVersion().isEmpty()) {
            wso2EventOutputMapping.setToEventName(inputStreamDefinition.getName());
            wso2EventOutputMapping.setToEventVersion(inputStreamDefinition.getVersion());
        }
        if (wso2EventOutputMapping.isCustomMappingEnabled()) {
            validateStreamDefinitionWithOutputProperties();
        }
    }

    private void validateStreamDefinitionWithOutputProperties()
            throws EventPublisherConfigurationException {
        WSO2EventOutputMapping wso2EventOutputMapping = (WSO2EventOutputMapping) eventPublisherConfiguration.getOutputMapping();
        List<EventOutputProperty> metaWSO2EventOutputPropertyConfiguration = wso2EventOutputMapping.getMetaWSO2EventOutputPropertyConfiguration();
        List<EventOutputProperty> correlationWSO2EventOutputPropertyConfiguration = wso2EventOutputMapping.getCorrelationWSO2EventOutputPropertyConfiguration();
        List<EventOutputProperty> payloadWSO2EventOutputPropertyConfiguration = wso2EventOutputMapping.getPayloadWSO2EventOutputPropertyConfiguration();

        Iterator<EventOutputProperty> metaWSO2EventOutputPropertyConfigurationIterator = metaWSO2EventOutputPropertyConfiguration.iterator();
        for (; metaWSO2EventOutputPropertyConfigurationIterator.hasNext(); ) {
            EventOutputProperty wso2EventOutputProperty = metaWSO2EventOutputPropertyConfigurationIterator.next();
            if (!propertyPositionMap.containsKey(wso2EventOutputProperty.getValueOf())) {
                throw new EventPublisherStreamValidationException("Property " + wso2EventOutputProperty.getValueOf() + " is not in the input stream definition. ", inputStreamDefinition.getStreamId());
            }
        }


        Iterator<EventOutputProperty> correlationWSO2EventOutputPropertyConfigurationIterator = correlationWSO2EventOutputPropertyConfiguration.iterator();
        for (; correlationWSO2EventOutputPropertyConfigurationIterator.hasNext(); ) {
            EventOutputProperty wso2EventOutputProperty = correlationWSO2EventOutputPropertyConfigurationIterator.next();
            if (!propertyPositionMap.containsKey(wso2EventOutputProperty.getValueOf())) {
                throw new EventPublisherStreamValidationException("Property " + wso2EventOutputProperty.getValueOf() + " is not in the input stream definition. ", inputStreamDefinition.getStreamId());
            }
        }

        Iterator<EventOutputProperty> payloadWSO2EventOutputPropertyConfigurationIterator = payloadWSO2EventOutputPropertyConfiguration.iterator();
        for (; payloadWSO2EventOutputPropertyConfigurationIterator.hasNext(); ) {
            EventOutputProperty wso2EventOutputProperty = payloadWSO2EventOutputPropertyConfigurationIterator.next();
            if (!propertyPositionMap.containsKey(wso2EventOutputProperty.getValueOf())) {
                throw new EventPublisherStreamValidationException("Property " + wso2EventOutputProperty.getValueOf() + " is not in the input stream definition. ", inputStreamDefinition.getStreamId());
            }
        }


    }

    @Override
    public Object convertToMappedOutputEvent(Event event)
            throws EventPublisherConfigurationException {
        Event eventObject = new Event();
        eventObject.setTimeStamp(event.getTimeStamp());
        Object[] eventData = ArrayUtils.addAll(ArrayUtils.addAll(event.getMetaData(), event.getCorrelationData()), event.getPayloadData());
        if (eventData.length > 0) {

            eventObject.setStreamId(wso2EventOutputMapping.getToEventName() + EventPublisherConstants.STREAM_ID_SEPERATOR + wso2EventOutputMapping.getToEventVersion());
            List<EventOutputProperty> metaWSO2EventOutputPropertyConfiguration = wso2EventOutputMapping.getMetaWSO2EventOutputPropertyConfiguration();
            List<EventOutputProperty> correlationWSO2EventOutputPropertyConfiguration = wso2EventOutputMapping.getCorrelationWSO2EventOutputPropertyConfiguration();
            List<EventOutputProperty> payloadWSO2EventOutputPropertyConfiguration = wso2EventOutputMapping.getPayloadWSO2EventOutputPropertyConfiguration();

            if (metaWSO2EventOutputPropertyConfiguration.size() > 0) {
                List<Object> metaData = new ArrayList<Object>();
                for (EventOutputProperty eventOutputProperty : metaWSO2EventOutputPropertyConfiguration) {
                    int position = propertyPositionMap.get(eventOutputProperty.getValueOf());
                    metaData.add(eventData[position]);
                }
                eventObject.setMetaData(metaData.toArray());
            }

            if (correlationWSO2EventOutputPropertyConfiguration.size() != 0) {
                List<Object> correlationData = new ArrayList<Object>();
                for (EventOutputProperty eventOutputProperty : correlationWSO2EventOutputPropertyConfiguration) {
                    int position = propertyPositionMap.get(eventOutputProperty.getValueOf());
                    correlationData.add(eventData[position]);
                }
                eventObject.setCorrelationData(correlationData.toArray());
            }

            if (payloadWSO2EventOutputPropertyConfiguration.size() != 0) {
                List<Object> payloadData = new ArrayList<Object>();
                for (EventOutputProperty eventOutputProperty : payloadWSO2EventOutputPropertyConfiguration) {
                    int position = propertyPositionMap.get(eventOutputProperty.getValueOf());
                    payloadData.add(eventData[position]);
                }
                eventObject.setPayloadData(payloadData.toArray());
            }
        }

        eventObject.setArbitraryDataMap(event.getArbitraryDataMap());
        return eventObject;
    }

    @Override
    public Object convertToTypedOutputEvent(Event event) throws EventPublisherConfigurationException {
        return event;
    }

}
