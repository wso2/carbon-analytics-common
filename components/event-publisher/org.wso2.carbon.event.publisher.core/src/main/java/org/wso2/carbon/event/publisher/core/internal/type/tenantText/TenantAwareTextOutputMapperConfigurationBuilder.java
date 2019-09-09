package org.wso2.carbon.event.publisher.core.internal.type.tenantText;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.config.OutputMapping;
import org.wso2.carbon.event.publisher.core.config.mapping.TenantTextOutputMapping;
import org.wso2.carbon.event.publisher.core.config.mapping.TextOutputMapping;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherValidationException;
import org.wso2.carbon.event.publisher.core.internal.type.text.TextOutputMapperConfigurationBuilder;

import java.util.Iterator;
import javax.xml.namespace.QName;

public class TenantAwareTextOutputMapperConfigurationBuilder {

    private TenantAwareTextOutputMapperConfigurationBuilder() {

    }

    public static OutputMapping fromOM(
            OMElement mappingElement)
            throws EventPublisherValidationException, EventPublisherConfigurationException {


        TenantTextOutputMapping tenantTextOutputMapping = new TenantTextOutputMapping();

        String customMappingEnabled = mappingElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_CUSTOM_MAPPING));
        if (customMappingEnabled == null || (customMappingEnabled.equals(EventPublisherConstants.ENABLE_CONST))) {
            tenantTextOutputMapping.setCustomMappingEnabled(true);
            if (!validateTextMapping(mappingElement)) {
                throw new EventPublisherConfigurationException("Text Mapping is not valid, check the output mapping");
            }

            OMElement innerMappingElement = mappingElement.getFirstChildWithName(
                    new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELE_MAPPING_INLINE));
            if (innerMappingElement != null) {
                tenantTextOutputMapping.setRegistryResource(false);
            } else {
                innerMappingElement = mappingElement.getFirstChildWithName(
                        new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELE_MAPPING_REGISTRY));
                if (innerMappingElement != null) {
                    tenantTextOutputMapping.setRegistryResource(true);
                    String cacheTimeoutDurationValue = innerMappingElement.getAttributeValue(QName.valueOf(EventPublisherConstants.EF_ELE_CACHE_TIMEOUT_DURATION));
                    if (cacheTimeoutDurationValue == null || cacheTimeoutDurationValue.isEmpty()) {
                        tenantTextOutputMapping.setCacheTimeoutDuration(0);
                    } else {
                        tenantTextOutputMapping.setCacheTimeoutDuration(Long.parseLong(cacheTimeoutDurationValue));
                    }
                } else {
                    throw new EventPublisherConfigurationException("Text Mapping is not valid, Mapping should be inline or from registry");
                }
            }

            if (innerMappingElement.getText() == null || innerMappingElement.getText().trim().isEmpty()) {
                throw new EventPublisherConfigurationException("No mapping content available");

            } else {
                tenantTextOutputMapping.setMappingText(innerMappingElement.getText());
            }

        } else {
            tenantTextOutputMapping.setCustomMappingEnabled(false);
        }

        return tenantTextOutputMapping;
    }


    public static boolean validateTextMapping(OMElement omElement) {


        int count = 0;
        Iterator<OMElement> mappingIterator = omElement.getChildElements();
        while (mappingIterator.hasNext()) {
            count++;
            mappingIterator.next();
        }

        return count != 0;

    }


    public static OMElement outputMappingToOM(
            OutputMapping outputMapping, OMFactory factory) {

        TenantTextOutputMapping tenantTextOutputMapping = (TenantTextOutputMapping) outputMapping;

        OMElement mappingOMElement = factory.createOMElement(new QName(
                EventPublisherConstants.EF_ELEMENT_MAPPING));
        mappingOMElement.declareDefaultNamespace(EventPublisherConstants.EF_CONF_NS);

        mappingOMElement.addAttribute(EventPublisherConstants.EF_ATTR_TYPE, EventPublisherConstants.EF_TEXT_MAPPING_TYPE, null);

        if (tenantTextOutputMapping.isCustomMappingEnabled()) {
            mappingOMElement.addAttribute(EventPublisherConstants.EF_ATTR_CUSTOM_MAPPING, EventPublisherConstants.ENABLE_CONST, null);


            OMElement innerMappingElement;
            if (tenantTextOutputMapping.isRegistryResource()) {
                innerMappingElement = factory.createOMElement(new QName(
                        EventPublisherConstants.EF_ELE_MAPPING_REGISTRY));
                innerMappingElement.declareDefaultNamespace(EventPublisherConstants.EF_CONF_NS);

                // Cache timeout of registry resource
                innerMappingElement.addAttribute(EventPublisherConstants.EF_ELE_CACHE_TIMEOUT_DURATION, Long.toString(tenantTextOutputMapping.getCacheTimeoutDuration()), null);
            } else {
                innerMappingElement = factory.createOMElement(new QName(
                        EventPublisherConstants.EF_ELE_MAPPING_INLINE));
                innerMappingElement.declareDefaultNamespace(EventPublisherConstants.EF_CONF_NS);
            }
            mappingOMElement.addChild(innerMappingElement);
            innerMappingElement.setText(tenantTextOutputMapping.getMappingText());
        } else {
            mappingOMElement.addAttribute(EventPublisherConstants.EF_ATTR_CUSTOM_MAPPING, EventPublisherConstants.TM_VALUE_DISABLE, null);
        }
        return mappingOMElement;
    }


}
