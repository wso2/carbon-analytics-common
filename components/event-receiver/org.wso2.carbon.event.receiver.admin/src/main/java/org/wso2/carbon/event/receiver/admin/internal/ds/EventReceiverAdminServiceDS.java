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
package org.wso2.carbon.event.receiver.admin.internal.ds;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterService;
import org.wso2.carbon.event.receiver.core.EventReceiverService;

/**
 * This class is used to get the EventReceiver service.
 */
@Component(
        name = "eventReceiverAdmin.component",
        immediate = true)
public class EventReceiverAdminServiceDS {

    @Activate
    protected void activate(ComponentContext context) {

    }

    @Reference(
            name = "event.adapter.service",
            service = org.wso2.carbon.event.input.adapter.core.InputEventAdapterService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetEventAdapterService")
    protected void setEventAdapterService(InputEventAdapterService eventAdapterService) {

        EventReceiverAdminServiceValueHolder.registerEventAdapterService(eventAdapterService);
    }

    protected void unSetEventAdapterService(InputEventAdapterService eventAdapterService) {

        EventReceiverAdminServiceValueHolder.registerEventAdapterService(null);
    }

    @Reference(
            name = "eventReceiverService.service",
            service = org.wso2.carbon.event.receiver.core.EventReceiverService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetEventReceiverService")
    protected void setEventReceiverService(EventReceiverService eventReceiverService) {

        EventReceiverAdminServiceValueHolder.registerEventReceiverService(eventReceiverService);
    }

    protected void unSetEventReceiverService(EventReceiverService eventReceiverService) {

        EventReceiverAdminServiceValueHolder.registerEventReceiverService(null);
    }
}
