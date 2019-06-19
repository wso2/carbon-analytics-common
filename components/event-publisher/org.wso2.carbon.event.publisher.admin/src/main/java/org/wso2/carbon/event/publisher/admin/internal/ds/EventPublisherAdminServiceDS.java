/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.publisher.admin.internal.ds;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.event.publisher.core.EventPublisherService;

/**
 * This class is used to get the EventPublisher service.
 */
@Component(
        name = "eventPublisherAdmin.component",
        immediate = true)
public class EventPublisherAdminServiceDS {

    @Activate
    protected void activate(ComponentContext context) {

    }

    @Reference(
            name = "event.adapter.service",
            service = org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetEventAdapterService")
    protected void setEventAdapterService(OutputEventAdapterService eventAdapterService) {

        EventPublisherAdminServiceValueHolder.registerEventAdapterService(eventAdapterService);
    }

    protected void unSetEventAdapterService(OutputEventAdapterService eventAdapterService) {

        EventPublisherAdminServiceValueHolder.registerEventAdapterService(null);
    }

    @Reference(
            name = "eventPublisherService.service",
            service = org.wso2.carbon.event.publisher.core.EventPublisherService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetEventPublisherService")
    protected void setEventPublisherService(EventPublisherService eventPublisherService) {

        EventPublisherAdminServiceValueHolder.registerEventPublisherService(eventPublisherService);
    }

    protected void unSetEventPublisherService(EventPublisherService eventPublisherService) {

        EventPublisherAdminServiceValueHolder.registerEventPublisherService(null);
    }
}
