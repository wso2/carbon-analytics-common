package org.wso2.carbon.event.publisher.core.config.mapping;

import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.config.OutputMapping;

public class TenantTextOutputMapping extends TextOutputMapping {

    @Override
    public String getMappingType() {
        return "texttenant";
    }
}
