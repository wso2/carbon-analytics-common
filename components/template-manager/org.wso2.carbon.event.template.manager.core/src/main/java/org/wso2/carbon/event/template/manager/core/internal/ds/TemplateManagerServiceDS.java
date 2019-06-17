/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.template.manager.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.event.template.manager.core.TemplateManagerService;
import org.wso2.carbon.event.template.manager.core.exception.TemplateManagerException;
import org.wso2.carbon.event.template.manager.core.internal.CarbonTemplateManagerService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;

/**
 * This class is used to get the EventProcessor service.
 */
@Component(
        name = "org.wso2.carbon.event.template.manager.core.TemplateManagerService",
        immediate = true)
public class TemplateManagerServiceDS {

    private static final Log log = LogFactory.getLog(TemplateManagerServiceDS.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            TemplateManagerService templateManagerService = new CarbonTemplateManagerService();
            context.getBundleContext().registerService(TemplateManagerService.class.getName(),
                    templateManagerService, null);
            TemplateManagerValueHolder.setTemplateManagerService(templateManagerService);
            if (log.isDebugEnabled()) {
                log.debug("Execution manager core service deployed successfully");
            }
        } catch (RuntimeException e) {
            log.error("Execution manager core service cannot be deployed ", e);
        } catch (TemplateManagerException e) {
            log.error("Execution manager core service cannot be deployed", e);
        }
    }

    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) throws RegistryException {

        TemplateManagerValueHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {

        TemplateManagerValueHolder.setRegistryService(null);
    }

    @Reference(
            name = "eventStreamService.service",
            service = org.wso2.carbon.event.stream.core.EventStreamService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventStreamService")
    protected void setEventStreamService(EventStreamService eventStreamService) {

        TemplateManagerValueHolder.setEventStreamService(eventStreamService);
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {

        TemplateManagerValueHolder.setEventStreamService(null);
    }
}
