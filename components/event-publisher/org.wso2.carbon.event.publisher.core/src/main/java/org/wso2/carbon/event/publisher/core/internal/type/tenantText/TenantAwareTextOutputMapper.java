package org.wso2.carbon.event.publisher.core.internal.type.tenantText;

import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfiguration;
import org.wso2.carbon.event.publisher.core.config.mapping.TextOutputMapping;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.internal.type.text.TextOutputMapper;
import org.wso2.siddhi.core.event.Event;

import java.util.Map;

public class TenantAwareTextOutputMapper extends TextOutputMapper {
    private EventPublisherConfiguration eventPublisherConfiguration = null;
    public TenantAwareTextOutputMapper(EventPublisherConfiguration eventPublisherConfiguration,
            Map<String, Integer> propertyPositionMap, int tenantId, StreamDefinition streamDefinition)
            throws EventPublisherConfigurationException {
        super(eventPublisherConfiguration, propertyPositionMap, tenantId, streamDefinition);
        this.eventPublisherConfiguration = eventPublisherConfiguration;
    }

    public Object convertToMappedInputEvent(Event event)
            throws EventPublisherConfigurationException {


        TextOutputMapping outputMapping = (TextOutputMapping) eventPublisherConfiguration.getOutputMapping();
        outputMapping.setMappingText("{{footer}}");
        eventPublisherConfiguration.setOutputMapping(outputMapping);
        super.setMappingText("{{footer}}");
        super.setMappingTextList("{{footer}}");
        return super.convertToMappedInputEvent(event);

    }
}
