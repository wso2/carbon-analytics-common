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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.config.OutputMapping;
import org.wso2.carbon.event.publisher.core.config.mapping.FormOutputMapping;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * This is the configuration builder class for the Form output mapper.
 */
public class FormOutputMapperConfigurationBuilder {

    private FormOutputMapperConfigurationBuilder() {

    }

    public static OutputMapping fromOM(OMElement mappingElement) throws EventPublisherConfigurationException {

        FormOutputMapping formOutputMapping = new FormOutputMapping();
        String customMappingEnabled = mappingElement.getAttributeValue(new QName(
                EventPublisherConstants.EF_ATTR_CUSTOM_MAPPING));
        if (customMappingEnabled == null || (customMappingEnabled.equals(EventPublisherConstants.ENABLE_CONST))) {
            formOutputMapping.setCustomMappingEnabled(true);
            if (!validateFormEventMapping(mappingElement)) {
                throw new EventPublisherConfigurationException("FORM Mapping is not valid, check the output mapping");
            }

            OMElement innerMappingElement = mappingElement.getFirstChildWithName(
                    new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELE_MAPPING_INLINE));
            if (innerMappingElement != null) {
                formOutputMapping.setRegistryResource(false);
            } else {
                throw new EventPublisherConfigurationException("Form Mapping is not valid, Mapping should be inline");
            }

            if (innerMappingElement.getText() == null || innerMappingElement.getText().trim().isEmpty()) {
                throw new EventPublisherConfigurationException("No mapping content available");

            } else {
                formOutputMapping.setMappingText(innerMappingElement.getText());
            }
        } else {
            formOutputMapping.setCustomMappingEnabled(false);
        }
        return formOutputMapping;
    }

    public static boolean validateFormEventMapping(OMElement omElement) {

        int count = 0;
        Iterator<OMElement> mappingIterator = omElement.getChildElements();
        while (mappingIterator.hasNext()) {
            count++;
            mappingIterator.next();
        }

        return count != 0;

    }

    public static OMElement outputMappingToOM(OutputMapping outputMapping, OMFactory factory) {

        FormOutputMapping formOutputMapping = (FormOutputMapping) outputMapping;

        OMElement mappingOMElement = factory.createOMElement(new QName(EventPublisherConstants.EF_ELEMENT_MAPPING));
        mappingOMElement.declareDefaultNamespace(EventPublisherConstants.EF_CONF_NS);

        mappingOMElement.addAttribute(EventPublisherConstants.EF_ATTR_TYPE,
                EventPublisherConstants.EF_FORM_MAPPING_TYPE, null);

        if (formOutputMapping.isCustomMappingEnabled()) {
            mappingOMElement.addAttribute(EventPublisherConstants.EF_ATTR_CUSTOM_MAPPING,
                    EventPublisherConstants.ENABLE_CONST, null);

            OMElement innerMappingElement = factory.createOMElement(new QName(
                        EventPublisherConstants.EF_ELE_MAPPING_INLINE));
            innerMappingElement.declareDefaultNamespace(EventPublisherConstants.EF_CONF_NS);

            mappingOMElement.addChild(innerMappingElement);
            innerMappingElement.setText(formOutputMapping.getMappingText());
        } else {
            mappingOMElement.addAttribute(EventPublisherConstants.EF_ATTR_CUSTOM_MAPPING,
                    EventPublisherConstants.TM_VALUE_DISABLE, null);
        }

        return mappingOMElement;
    }
}
