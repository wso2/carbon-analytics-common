/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.publisher.template.deployer.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.event.publisher.core.EventPublisherService;
import org.wso2.carbon.event.publisher.template.deployer.EventPublisherTemplateDeployer;
import org.wso2.carbon.event.publisher.template.deployer.internal.EventPublisherTemplateDeployerValueHolder;
import org.wso2.carbon.event.template.manager.core.TemplateDeployer;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;

@Component(
        name = "TemplateDeployer.eventPublisher.component",
        immediate = true)
public class EventPublisherTemplateDeployerDS {

    private static final Log log = LogFactory.getLog(EventPublisherTemplateDeployerDS.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            EventPublisherTemplateDeployer templateDeployer = new EventPublisherTemplateDeployer();
            context.getBundleContext().registerService(TemplateDeployer.class.getName(), templateDeployer, null);
        } catch (RuntimeException e) {
            log.error("Couldn't register EventPublisherTemplateDeployer service", e);
        }
    }

    @Reference(
            name = "eventPublisherService.service",
            service = org.wso2.carbon.event.publisher.core.EventPublisherService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventPublisherService")
    protected void setEventPublisherService(EventPublisherService eventPublisherService) {

        EventPublisherTemplateDeployerValueHolder.setEventPublisherService(eventPublisherService);
    }

    protected void unsetEventPublisherService(EventPublisherService eventPublisherService) {

        EventPublisherTemplateDeployerValueHolder.setEventPublisherService(null);
    }

    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) throws RegistryException {

        EventPublisherTemplateDeployerValueHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {

        EventPublisherTemplateDeployerValueHolder.setRegistryService(null);
    }
}
