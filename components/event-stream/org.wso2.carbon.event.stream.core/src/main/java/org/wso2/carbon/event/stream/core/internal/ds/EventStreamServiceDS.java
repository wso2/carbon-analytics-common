/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.stream.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.event.stream.core.EventStreamListener;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.event.stream.core.internal.CarbonEventStreamService;
import org.wso2.carbon.event.stream.core.internal.EventStreamRuntime;
import org.wso2.carbon.utils.ConfigurationContextService;

@Component(
        name = "eventStreamService.component",
        immediate = true)
public class EventStreamServiceDS {

    private static final Log log = LogFactory.getLog(EventStreamServiceDS.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            EventStreamServiceValueHolder.registerEventStreamRuntime(new EventStreamRuntime());
            CarbonEventStreamService carbonEventStreamService = new CarbonEventStreamService();
            EventStreamServiceValueHolder.setCarbonEventStreamService(carbonEventStreamService);
            context.getBundleContext().registerService(EventStreamService.class.getName(), carbonEventStreamService,
                    null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed EventStreamService");
            }
        } catch (Throwable e) {
            log.error("Could not create EventStreamService : " + e.getMessage(), e);
        }
    }

    @Reference(
            name = "config.context.service",
            service = org.wso2.carbon.utils.ConfigurationContextService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {

        EventStreamServiceValueHolder.registerConfigurationContextService(configurationContextService);
        if (EventStreamServiceValueHolder.getCarbonEventStreamService() != null) {
            EventStreamServiceValueHolder.getCarbonEventStreamService().addPendingStreams();
        }
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {

        EventStreamServiceValueHolder.registerConfigurationContextService(null);
    }

    @Reference(
            name = "eventStreamListener.service",
            service = org.wso2.carbon.event.stream.core.EventStreamListener.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventStreamListener")
    protected void setEventStreamListener(EventStreamListener eventStreamListener) {

        EventStreamServiceValueHolder.registerEventStreamListener(eventStreamListener);
    }

    protected void unsetEventStreamListener(EventStreamListener eventStreamListener) {

        EventStreamServiceValueHolder.unregisterEventStreamListener(eventStreamListener);
    }
}
