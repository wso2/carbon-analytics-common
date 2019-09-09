package org.wso2.carbon.event.publisher.core.internal.type.tenantText;

import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfiguration;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.internal.OutputMapper;
import org.wso2.carbon.event.publisher.core.internal.type.text.TextOutputMapper;
import org.wso2.carbon.event.publisher.core.internal.type.text.TextOutputMapperFactory;

import java.util.Map;

public class TenantAwareTextOutputMapperFactory extends TextOutputMapperFactory {

    public OutputMapper constructOutputMapper(
            EventPublisherConfiguration eventPublisherConfiguration,
            Map<String, Integer> propositionMap, int tenantId, StreamDefinition streamDefinition)
            throws EventPublisherConfigurationException {
        return new TenantAwareTextOutputMapper(eventPublisherConfiguration, propositionMap, tenantId, streamDefinition);
    }
}
