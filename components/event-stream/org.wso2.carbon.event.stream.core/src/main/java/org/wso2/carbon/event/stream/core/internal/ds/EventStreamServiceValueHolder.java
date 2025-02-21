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

import org.wso2.carbon.event.stream.core.EventStreamListener;
import org.wso2.carbon.event.stream.core.internal.CarbonEventStreamService;
import org.wso2.carbon.event.stream.core.internal.EventStreamRuntime;
import org.wso2.carbon.event.stream.core.internal.config.EventPublisherConfigs;
import org.wso2.carbon.securevault.SecretCallbackHandlerService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventStreamServiceValueHolder {

    private static CarbonEventStreamService carbonEventStreamService;
    private static ConfigurationContextService configurationContextService;
    private static List<EventStreamListener> eventStreamListenerList =  new CopyOnWriteArrayList<EventStreamListener>();
    private static EventStreamRuntime eventStreamRuntime;
    private static EventPublisherConfigs eventPublisherConfigs;
    private static SecretCallbackHandlerService secretCallbackHandlerService;

    private EventStreamServiceValueHolder() {

    }

    public static CarbonEventStreamService getCarbonEventStreamService() {
        return EventStreamServiceValueHolder.carbonEventStreamService;
    }

    public static void setCarbonEventStreamService(
            CarbonEventStreamService carbonEventStreamService) {
        EventStreamServiceValueHolder.carbonEventStreamService = carbonEventStreamService;
    }

    public static void registerConfigurationContextService(ConfigurationContextService configurationContextService) {
        EventStreamServiceValueHolder.configurationContextService = configurationContextService;
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public static List<EventStreamListener> getEventStreamListenerList() {
        return eventStreamListenerList;
    }

    public static void registerEventStreamListener(EventStreamListener eventStreamListener) {
        eventStreamListenerList.add(eventStreamListener);
    }

    public static void unregisterEventStreamListener(EventStreamListener eventStreamListener) {
        eventStreamListenerList.remove(eventStreamListener);
    }

    public static EventStreamRuntime getEventStreamRuntime() {
        return eventStreamRuntime;
    }

    public static void registerEventStreamRuntime(EventStreamRuntime eventStreamRuntime) {
        EventStreamServiceValueHolder.eventStreamRuntime = eventStreamRuntime;
    }

    public static void setEventPublisherConfigs(EventPublisherConfigs globalAdapterConfigs) {
        EventStreamServiceValueHolder.eventPublisherConfigs = globalAdapterConfigs;
    }

    public static EventPublisherConfigs getEventPublisherConfigs() {
        return eventPublisherConfigs;
    }

    public static void setSecretCallbackHandlerService(SecretCallbackHandlerService secretCallbackHandlerService) {
        EventStreamServiceValueHolder.secretCallbackHandlerService = secretCallbackHandlerService;
    }

    public static SecretCallbackHandlerService getSecretCallbackHandlerService() {
        return secretCallbackHandlerService;
    }
}
