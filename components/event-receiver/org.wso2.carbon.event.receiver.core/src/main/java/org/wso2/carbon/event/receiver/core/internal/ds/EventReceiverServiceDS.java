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
package org.wso2.carbon.event.receiver.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterFactory;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterService;
import org.wso2.carbon.event.processor.manager.core.EventManagementService;
import org.wso2.carbon.event.receiver.core.EventReceiverService;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.core.internal.CarbonEventReceiverManagementService;
import org.wso2.carbon.event.receiver.core.internal.CarbonEventReceiverService;
import org.wso2.carbon.event.receiver.core.internal.EventStreamListenerImpl;
import org.wso2.carbon.event.stream.core.EventStreamListener;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.ArrayList;
import java.util.Set;

@Component(
        name = "eventReceiverService.component",
        immediate = true)
public class EventReceiverServiceDS {

    private static final Log log = LogFactory.getLog(EventReceiverServiceDS.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            checkIsStatsEnabled();
            CarbonEventReceiverService carbonEventReceiverService = new CarbonEventReceiverService();
            EventReceiverServiceValueHolder.registerEventReceiverService(carbonEventReceiverService);
            CarbonEventReceiverManagementService carbonEventReceiverManagementService = new
                    CarbonEventReceiverManagementService();
            EventReceiverServiceValueHolder.getEventManagementService().subscribe(carbonEventReceiverManagementService);
            EventReceiverServiceValueHolder.registerReceiverManagementService(carbonEventReceiverManagementService);
            context.getBundleContext().registerService(EventReceiverService.class.getName(),
                    carbonEventReceiverService, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed EventReceiverService.");
            }
            activateInactiveEventReceiverConfigurations(carbonEventReceiverService);
            context.getBundleContext().registerService(EventStreamListener.class.getName(), new
                    EventStreamListenerImpl(), null);
        } catch (Throwable e) {
            log.error("Could not create EventReceiverService or EventReceiver : " + e.getMessage(), e);
        }
    }

    private void checkIsStatsEnabled() {

        ServerConfiguration config = ServerConfiguration.getInstance();
        String confStatisticsReporterDisabled = config.getFirstProperty("StatisticsReporterDisabled");
        if (!"".equals(confStatisticsReporterDisabled)) {
            boolean disabled = Boolean.valueOf(confStatisticsReporterDisabled);
            if (disabled) {
                return;
            }
        }
        EventReceiverServiceValueHolder.setGlobalStatisticsEnabled(true);
    }

    private void activateInactiveEventReceiverConfigurations(CarbonEventReceiverService carbonEventReceiverService) {

        Set<String> inputEventAdapterTypes = EventReceiverServiceValueHolder.getInputEventAdapterTypes();
        inputEventAdapterTypes.addAll(EventReceiverServiceValueHolder.getInputEventAdapterService()
                .getInputEventAdapterTypes());
        for (String type : inputEventAdapterTypes) {
            try {
                carbonEventReceiverService.activateInactiveEventReceiverConfigurationsForAdapter(type);
            } catch (EventReceiverConfigurationException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Reference(
            name = "inputEventAdapter.service",
            service = org.wso2.carbon.event.input.adapter.core.InputEventAdapterService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetInputEventAdapterService")
    protected void setInputEventAdapterService(InputEventAdapterService inputEventAdapterService) {

        EventReceiverServiceValueHolder.registerInputEventAdapterService(inputEventAdapterService);
    }

    protected void unsetInputEventAdapterService(InputEventAdapterService inputEventAdapterService) {

        EventReceiverServiceValueHolder.getInputEventAdapterTypes().clear();
        EventReceiverServiceValueHolder.registerInputEventAdapterService(null);
    }

    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) throws RegistryException {

        EventReceiverServiceValueHolder.registerRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {

        EventReceiverServiceValueHolder.registerRegistryService(null);
    }

    @Reference(
            name = "eventStreamManager.service",
            service = org.wso2.carbon.event.stream.core.EventStreamService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventStreamService")
    protected void setEventStreamService(EventStreamService eventStreamService) {

        EventReceiverServiceValueHolder.registerEventStreamService(eventStreamService);
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {

        EventReceiverServiceValueHolder.registerEventStreamService(null);
    }

    @Reference(
            name = "config.context.service",
            service = org.wso2.carbon.utils.ConfigurationContextService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {

        EventReceiverServiceValueHolder.setConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {

        EventReceiverServiceValueHolder.setConfigurationContextService(null);
    }

    @Reference(
            name = "input.event.adapter.tracker.service",
            service = org.wso2.carbon.event.input.adapter.core.InputEventAdapterFactory.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetEventAdapterType")
    protected void setEventAdapterType(InputEventAdapterFactory inputEventAdapterFactory) {

        EventReceiverServiceValueHolder.addInputEventAdapterType(inputEventAdapterFactory.getType());
        if (EventReceiverServiceValueHolder.getCarbonEventReceiverService() != null) {
            try {
                EventReceiverServiceValueHolder.getCarbonEventReceiverService()
                        .activateInactiveEventReceiverConfigurationsForAdapter(inputEventAdapterFactory.getType());
            } catch (EventReceiverConfigurationException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    protected void unSetEventAdapterType(InputEventAdapterFactory inputEventAdapterFactory) {

        EventReceiverServiceValueHolder.removeInputEventAdapterType(inputEventAdapterFactory.getType());
        if (EventReceiverServiceValueHolder.getCarbonEventReceiverService() != null) {
            try {
                EventReceiverServiceValueHolder.getCarbonEventReceiverService()
                        .deactivateActiveEventReceiverConfigurationsForAdapter(inputEventAdapterFactory.getType());
            } catch (EventReceiverConfigurationException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Reference(
            name = "eventManagement.service",
            service = org.wso2.carbon.event.processor.manager.core.EventManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventManagementService")
    protected void setEventManagementService(EventManagementService eventManagementService) {

        EventReceiverServiceValueHolder.registerEventManagementService(eventManagementService);
    }

    protected void unsetEventManagementService(EventManagementService eventManagementService) {

        EventReceiverServiceValueHolder.registerEventManagementService(null);
        eventManagementService.unsubscribe(EventReceiverServiceValueHolder.getCarbonEventReceiverManagementService());
    }

    @Reference(
            name = "user.realmservice.default",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {

        EventReceiverServiceValueHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

    }
}
