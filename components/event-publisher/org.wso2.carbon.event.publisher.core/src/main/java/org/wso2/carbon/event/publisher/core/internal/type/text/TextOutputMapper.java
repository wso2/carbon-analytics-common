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
package org.wso2.carbon.event.publisher.core.internal.type.text;

import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.publisher.core.config.CustomMapperFunction;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfiguration;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.config.mapping.TextOutputMapping;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.internal.OutputMapper;
import org.wso2.carbon.event.publisher.core.internal.ds.EventPublisherServiceValueHolder;
import org.wso2.carbon.event.publisher.core.internal.util.EventPublisherUtil;
import org.wso2.carbon.event.publisher.core.internal.util.RuntimeResourceLoader;
import org.wso2.carbon.event.publisher.core.mapper.AttributeOutputMapperType;
import org.wso2.carbon.event.publisher.core.mapper.FunctionOutputMapperType;
import org.wso2.carbon.event.publisher.core.mapper.OutputMapperType;
import org.wso2.carbon.event.publisher.core.mapper.TextOutputMapperType;
import org.wso2.siddhi.core.event.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TextOutputMapper implements OutputMapper {

    private List<OutputMapperType> mappingTextList;
    private EventPublisherConfiguration eventPublisherConfiguration = null;
    private Map<String, Integer> propertyPositionMap = null;
    private final StreamDefinition streamDefinition;
    private boolean isCustomRegistryPath;
    private final RuntimeResourceLoader runtimeResourceLoader;
    private final boolean isCustomMappingEnabled;
    private String mappingText;
    private final ConcurrentHashMap<String, CustomMapperFunction> customMapperFunctions;

    public TextOutputMapper(EventPublisherConfiguration eventPublisherConfiguration,
                            Map<String, Integer> propertyPositionMap, int tenantId,
                            StreamDefinition streamDefinition) throws
            EventPublisherConfigurationException {
        this.eventPublisherConfiguration = eventPublisherConfiguration;
        this.propertyPositionMap = propertyPositionMap;
        this.streamDefinition = streamDefinition;

        TextOutputMapping outputMapping = ((TextOutputMapping) eventPublisherConfiguration.getOutputMapping());
        this.runtimeResourceLoader = new RuntimeResourceLoader(outputMapping.getCacheTimeoutDuration(), propertyPositionMap);
        this.isCustomMappingEnabled = outputMapping.isCustomMappingEnabled();

        if (this.isCustomMappingEnabled) {
            mappingText = outputMapping.getMappingText();
            if (outputMapping.isRegistryResource()) {
                this.isCustomRegistryPath = mappingText.contains(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX) && mappingText.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX) > 0;
                if (!this.isCustomRegistryPath) {
                    mappingText = this.runtimeResourceLoader.getResourceContent(outputMapping.getMappingText());
                }
            }
        } else {
            mappingText = generateTemplateTextEvent(streamDefinition);
        }

        if (!outputMapping.isRegistryResource()) {     // Store only if it is not from registry
            this.mappingTextList = generateMappingTextList(mappingText);
        }
        this.customMapperFunctions = EventPublisherServiceValueHolder.getCustomMapperFunctions();
    }

    private List<String> getOutputMappingPropertyList(String mappingText) {

        List<String> mappingTextList = new ArrayList<String>();
        String text = mappingText;

        mappingTextList.clear();
        while (text.contains(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX) && text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX) > 0) {
            mappingTextList.add(text.substring(text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX) + 2, text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX)));
            text = text.substring(text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX) + 2);
        }
        return mappingTextList;
    }

    private List<OutputMapperType> generateMappingTextList(String mappingText) {
        List<OutputMapperType> mappingTextList = new ArrayList<>();
        String text = mappingText;
        mappingTextList.clear();
        // adding the relevant mapping type objects
        while (text.contains(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX) && text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX) > 0) {
            mappingTextList.add(new TextOutputMapperType(text.substring(0, text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX))));
            String mapText = text.substring(text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX) + 2, text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX));
            if (mapText.endsWith(EventPublisherConstants.CUSTOM_FUNCTION_CLOSING_BRACKET)) {
                mappingTextList.add(new FunctionOutputMapperType(mapText));
            } else {
                mappingTextList.add(new AttributeOutputMapperType(mapText));
            }
            text = text.substring(text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX) + 2);
        }
        mappingTextList.add(new TextOutputMapperType(text));
        return mappingTextList;
    }

    @Override
    public Object convertToMappedInputEvent(Event event)
            throws EventPublisherConfigurationException {

        if (this.isCustomMappingEnabled) {
            EventPublisherUtil.validateStreamDefinitionWithOutputProperties(mappingText, propertyPositionMap, event.getArbitraryDataMap(), customMapperFunctions);
        }
        // Retrieve resource at runtime if it is from registry
        TextOutputMapping outputMapping = (TextOutputMapping) eventPublisherConfiguration.getOutputMapping();
        if (outputMapping.isRegistryResource()) {
            String path = outputMapping.getMappingText();
            if (isCustomRegistryPath) {
                // Retrieve the actual path
                List<OutputMapperType> pathMappingTextList = generateMappingTextList(path);
                StringBuilder pathBuilder = new StringBuilder(pathMappingTextList.get(0).getValue(event, propertyPositionMap, customMapperFunctions).toString());
                for (int i = 1; i < pathMappingTextList.size(); i++) {
                    if (i % 2 == 0) {
                        pathBuilder.append(pathMappingTextList.get(i).getValue(event, propertyPositionMap, customMapperFunctions).toString());
                    } else {
                        pathBuilder.append(pathMappingTextList.get(i).getValue(event, propertyPositionMap, customMapperFunctions).toString());
                    }
                }
                path = pathBuilder.toString();
            }
            // Retrieve actual content
            String actualMappingText = this.runtimeResourceLoader.getResourceContent(path);
            this.mappingTextList = generateMappingTextList(actualMappingText);
        }

        StringBuilder eventText = new StringBuilder(mappingTextList.get(0).getValue(event, propertyPositionMap, customMapperFunctions).toString());
        for (int i = 1; i < mappingTextList.size(); i++) {
            if (i % 2 == 0) {
                eventText.append(mappingTextList.get(i).getValue(event, propertyPositionMap, customMapperFunctions));
            } else {
                eventText.append(mappingTextList.get(i).getValue(event, propertyPositionMap, customMapperFunctions));
            }
        }

        if (!this.isCustomMappingEnabled) {
            Map<String, Object> arbitraryDataMap = event.getArbitraryDataMap();
            if (arbitraryDataMap != null && !arbitraryDataMap.isEmpty()) {
                // Add arbitrary data map to the default template
                eventText.append(EventPublisherConstants.EVENT_ATTRIBUTE_SEPARATOR);
                for (Map.Entry<String, Object> entry : arbitraryDataMap.entrySet()) {
                    eventText.append("\n" + entry.getKey() + EventPublisherConstants.EVENT_ATTRIBUTE_VALUE_SEPARATOR + entry.getValue() + EventPublisherConstants.EVENT_ATTRIBUTE_SEPARATOR);
                }
                eventText.deleteCharAt(eventText.lastIndexOf(EventPublisherConstants.EVENT_ATTRIBUTE_SEPARATOR));
            }
        }
        return eventText.toString();
    }

    @Override
    public Object convertToTypedInputEvent(Event event)
            throws EventPublisherConfigurationException {
        return convertToMappedInputEvent(event);
    }

    private String generateTemplateTextEvent(StreamDefinition streamDefinition) {

        String templateTextEvent = "";

        List<Attribute> metaDatAttributes = streamDefinition.getMetaData();
        if (metaDatAttributes != null && metaDatAttributes.size() > 0) {
            for (Attribute attribute : metaDatAttributes) {
                templateTextEvent += "\n" + EventPublisherConstants.PROPERTY_META_PREFIX + attribute.getName() + EventPublisherConstants.EVENT_ATTRIBUTE_VALUE_SEPARATOR + EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX + EventPublisherConstants.PROPERTY_META_PREFIX + attribute.getName() + EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX + EventPublisherConstants.EVENT_ATTRIBUTE_SEPARATOR;
            }
        }

        List<Attribute> correlationAttributes = streamDefinition.getCorrelationData();
        if (correlationAttributes != null && correlationAttributes.size() > 0) {
            for (Attribute attribute : correlationAttributes) {
                templateTextEvent += "\n" + EventPublisherConstants.PROPERTY_CORRELATION_PREFIX + attribute.getName() + EventPublisherConstants.EVENT_ATTRIBUTE_VALUE_SEPARATOR + EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX + EventPublisherConstants.PROPERTY_CORRELATION_PREFIX + attribute.getName() + EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX + EventPublisherConstants.EVENT_ATTRIBUTE_SEPARATOR;
            }
        }


        List<Attribute> payloadAttributes = streamDefinition.getPayloadData();
        if (payloadAttributes != null && payloadAttributes.size() > 0) {
            for (Attribute attribute : payloadAttributes) {
                templateTextEvent += "\n" + attribute.getName() + EventPublisherConstants.EVENT_ATTRIBUTE_VALUE_SEPARATOR + EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX + attribute.getName() + EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX + EventPublisherConstants.EVENT_ATTRIBUTE_SEPARATOR;
            }
        }
        if (templateTextEvent.trim().endsWith(EventPublisherConstants.EVENT_ATTRIBUTE_SEPARATOR)) {
            templateTextEvent = templateTextEvent.substring(0, templateTextEvent.length() - 1).trim();
        }

        return templateTextEvent;
    }

}
