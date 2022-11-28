/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.event.processor.manager.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.event.processor.manager.core.EventManagementService;
import org.wso2.carbon.event.processor.manager.core.internal.CarbonEventManagementService;
import org.wso2.carbon.utils.ConfigurationContextService;

@Component(
        name = "eventProcessorManagementService.component",
        immediate = true)
public class EventManagementServiceDS {

    private static final Log log = LogFactory.getLog(EventManagementServiceDS.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            CarbonEventManagementService carbonEventManagementService = new CarbonEventManagementService();
            EventManagementServiceValueHolder.setCarbonEventManagementService(carbonEventManagementService);
            context.getBundleContext().registerService(EventManagementService.class.getName(),
                    carbonEventManagementService, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed EventProcessorManagementService");
            }
        } catch (Throwable e) {
            log.error("Could not create EventProcessorManagementService: " + e.getMessage(), e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        EventManagementServiceValueHolder.getCarbonEventManagementService().shutdown();
    }

    @Reference(
            name = "configuration.contextService.service",
            service = org.wso2.carbon.utils.ConfigurationContextService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {

        EventManagementServiceValueHolder.getCarbonEventManagementService().init(configurationContextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {

    }
}
