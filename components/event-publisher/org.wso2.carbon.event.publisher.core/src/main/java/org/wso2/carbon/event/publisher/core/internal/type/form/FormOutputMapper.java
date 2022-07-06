/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.publisher.core.internal.type.form;

import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfiguration;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.config.mapping.FormOutputMapping;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.internal.OutputMapper;
import org.wso2.carbon.event.publisher.core.internal.util.EventPublisherUtil;
import org.wso2.carbon.event.publisher.core.internal.util.RuntimeResourceLoader;
import org.wso2.siddhi.core.event.Event;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This is the Output mapper class for form url encoded output type.
 */
public class FormOutputMapper implements OutputMapper {

    private List<String> mappingTextList;
    private EventPublisherConfiguration eventPublisherConfiguration = null;
    private Map<String, Integer> propertyPositionMap = null;
    private final StreamDefinition streamDefinition;
    private final RuntimeResourceLoader runtimeResourceLoader;
    private final boolean isCustomMappingEnabled;
    private final String mappingText;

    public FormOutputMapper(EventPublisherConfiguration eventPublisherConfiguration,
                            Map<String, Integer> propertyPositionMap, int tenantId, StreamDefinition streamDefinition)
            throws EventPublisherConfigurationException {

        this.eventPublisherConfiguration = eventPublisherConfiguration;
        this.propertyPositionMap = propertyPositionMap;
        this.streamDefinition = streamDefinition;

        FormOutputMapping outputMapping = (FormOutputMapping) eventPublisherConfiguration.getOutputMapping();
        this.runtimeResourceLoader = new RuntimeResourceLoader(outputMapping.getCacheTimeoutDuration(),
                propertyPositionMap);
        this.isCustomMappingEnabled = outputMapping.isCustomMappingEnabled();

        if (this.isCustomMappingEnabled) {
            this.mappingText = getCustomMappingText();
        } else {
            this.mappingText = generateFormEventTemplate(streamDefinition);
        }

        if (!outputMapping.isRegistryResource()) {
            this.mappingTextList = generateMappingTextList(this.mappingText);
        }
    }

    private List<String> generateMappingTextList(String mappingText) {

        List<String> mappingTextList = new ArrayList<String>();
        List<String> textList = Arrays.asList(mappingText.trim().split(","));
        textList.replaceAll(String::trim);
        String text = String.join("&", textList);

        mappingTextList.clear();
        while (text.contains(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX) && text.indexOf(
                EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX) > 0) {
            mappingTextList.add(text.substring(0, text.indexOf(
                    EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX)).trim());
            mappingTextList.add(text.substring(text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX)
                    + 2, text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX)).trim());
            text = text.substring(text.indexOf(EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX) + 2);
        }
        if (!text.isEmpty()) {
            mappingTextList.add(text.trim());
        }
        return mappingTextList;
    }

    @Override
    public Object convertToMappedInputEvent(Event event)
            throws EventPublisherConfigurationException {

        if (this.isCustomMappingEnabled) {
            EventPublisherUtil.validateStreamDefinitionWithOutputProperties(mappingText, propertyPositionMap,
                    event.getArbitraryDataMap());
        }

        StringBuilder eventText = new StringBuilder(mappingTextList.get(0));
        for (int i = 1; i < mappingTextList.size(); i++) {
            if (i % 2 == 0) {
                eventText.append(mappingTextList.get(i));
            } else {
                eventText.append(getPropertyValue(event, mappingTextList.get(i)));
            }
        }

        if (!this.isCustomMappingEnabled) {
            Map<String, Object> arbitraryDataMap = event.getArbitraryDataMap();
            if (arbitraryDataMap != null && !arbitraryDataMap.isEmpty()) {
                // Add arbitrary data map to the default template
                eventText.append(EventPublisherConstants.EVENT_ATTRIBUTE_SEPARATOR);
                for (Map.Entry<String, Object> entry : arbitraryDataMap.entrySet()) {
                    eventText.append(entry.getKey() + EventPublisherConstants.EVENT_FORM_ATTRIBUTE_VALUE_SEPARATOR +
                            entry.getValue() + EventPublisherConstants.EVENT_ATTRIBUTE_SEPARATOR);
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

    private String getCustomMappingText() throws EventPublisherConfigurationException {

        FormOutputMapping formOutputMapping = ((FormOutputMapping) eventPublisherConfiguration.getOutputMapping());
        String actualMappingText = formOutputMapping.getMappingText();
        if (actualMappingText == null) {
            throw new EventPublisherConfigurationException("Form mapping text is empty!");
        }
        return actualMappingText;
    }

    private Object getPropertyValue(Event event, String mappingProperty) throws EventPublisherConfigurationException {

        Object[] eventData = event.getData();
        Map<String, Object> arbitraryMap = event.getArbitraryDataMap();
        Integer position = propertyPositionMap.get(mappingProperty);
        Object data = null;

        if (position != null && eventData.length != 0) {
            data = eventData[position];
        } else if (mappingProperty != null && arbitraryMap != null && arbitraryMap.containsKey(mappingProperty)) {
            data = arbitraryMap.get(mappingProperty);
        }
        if (data != null) {
            try {
                return URLEncoder.encode(data.toString(), StandardCharsets.UTF_8.toString());
            } catch (UnsupportedEncodingException e) {
                throw new EventPublisherConfigurationException("Error while encoding.");
            }
        }
        return "";
    }

    private String generateFormEventTemplate(StreamDefinition streamDefinition) {

        String templateTextEvent = "";

        List<Attribute> metaDatAttributes = streamDefinition.getMetaData();
        if (metaDatAttributes != null && metaDatAttributes.size() > 0) {
            for (Attribute attribute : metaDatAttributes) {
                templateTextEvent += EventPublisherConstants.PROPERTY_META_PREFIX +
                        attribute.getName() + EventPublisherConstants.EVENT_FORM_ATTRIBUTE_VALUE_SEPARATOR +
                        EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX +
                        EventPublisherConstants.PROPERTY_META_PREFIX + attribute.getName() +
                        EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX +
                        EventPublisherConstants.EVENT_ATTRIBUTE_SEPARATOR;
            }
        }

        List<Attribute> correlationAttributes = streamDefinition.getCorrelationData();
        if (correlationAttributes != null && correlationAttributes.size() > 0) {
            for (Attribute attribute : correlationAttributes) {
                templateTextEvent += EventPublisherConstants.PROPERTY_CORRELATION_PREFIX + attribute.getName() +
                        EventPublisherConstants.EVENT_FORM_ATTRIBUTE_VALUE_SEPARATOR +
                        EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX +
                        EventPublisherConstants.PROPERTY_CORRELATION_PREFIX + attribute.getName() +
                        EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX +
                        EventPublisherConstants.EVENT_ATTRIBUTE_SEPARATOR;
            }
        }

        List<Attribute> payloadAttributes = streamDefinition.getPayloadData();
        if (payloadAttributes != null && payloadAttributes.size() > 0) {
            for (Attribute attribute : payloadAttributes) {
                templateTextEvent += attribute.getName() +
                        EventPublisherConstants.EVENT_FORM_ATTRIBUTE_VALUE_SEPARATOR +
                        EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX + attribute.getName() +
                        EventPublisherConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX +
                        EventPublisherConstants.EVENT_ATTRIBUTE_SEPARATOR;
            }
        }
        if (templateTextEvent.trim().endsWith(EventPublisherConstants.EVENT_ATTRIBUTE_SEPARATOR)) {
            templateTextEvent = templateTextEvent.substring(0, templateTextEvent.length() - 1).trim();
        }

        return templateTextEvent;
    }
}
