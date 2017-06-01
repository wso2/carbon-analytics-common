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
package org.wso2.carbon.event.publisher.core.internal.ds;

import org.wso2.carbon.event.output.adapter.core.MessageType;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.event.processor.manager.core.EventManagementService;
import org.wso2.carbon.event.publisher.core.EventPublisherService;
import org.wso2.carbon.event.publisher.core.config.OutputMapperFactory;
import org.wso2.carbon.event.publisher.core.internal.CarbonEventPublisherManagementService;
import org.wso2.carbon.event.publisher.core.internal.CarbonEventPublisherService;
import org.wso2.carbon.event.publisher.core.internal.type.json.JSONOutputMapperFactory;
import org.wso2.carbon.event.publisher.core.internal.type.map.MapOutputMapperFactory;
import org.wso2.carbon.event.publisher.core.internal.type.text.TextOutputMapperFactory;
import org.wso2.carbon.event.publisher.core.internal.type.wso2event.WSO2EventOutputMapperFactory;
import org.wso2.carbon.event.publisher.core.internal.type.xml.XMLOutputMapperFactory;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EventPublisherServiceValueHolder {

    private static OutputEventAdapterService outputEventAdapterService;
    private static CarbonEventPublisherService carbonEventPublisherService;
    private static EventStreamService eventStreamService;
    private static RegistryService registryService;
    private static CarbonEventPublisherManagementService carbonEventPublisherManagementService;
    private static EventManagementService eventManagementService;
    private static ConcurrentHashMap<String, OutputMapperFactory> mappingFactoryMap = new ConcurrentHashMap<String, OutputMapperFactory>();
    public static Set<String> outputEventAdapterTypes = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private static ConfigurationContextService configurationContextService;
    private static boolean globalStatisticsEnabled = false;
    private static boolean isLazyLoading;

    static {
        mappingFactoryMap.put(MessageType.MAP, new MapOutputMapperFactory());
        mappingFactoryMap.put(MessageType.TEXT, new TextOutputMapperFactory());
        mappingFactoryMap.put(MessageType.WSO2EVENT, new WSO2EventOutputMapperFactory());
        mappingFactoryMap.put(MessageType.XML, new XMLOutputMapperFactory());
        mappingFactoryMap.put(MessageType.JSON, new JSONOutputMapperFactory());
    }

    private EventPublisherServiceValueHolder() {

    }

    public static CarbonEventPublisherService getCarbonEventPublisherService() {
        return carbonEventPublisherService;
    }

    public static void registerPublisherService(EventPublisherService eventPublisherService) {
        EventPublisherServiceValueHolder.carbonEventPublisherService = (CarbonEventPublisherService) eventPublisherService;

    }

    public static void registerEventAdapterService(
            OutputEventAdapterService eventAdapterService) {
        EventPublisherServiceValueHolder.outputEventAdapterService = eventAdapterService;
    }

    public static OutputEventAdapterService getOutputEventAdapterService() {
        return EventPublisherServiceValueHolder.outputEventAdapterService;
    }

    public static void setRegistryService(RegistryService registryService) {
        EventPublisherServiceValueHolder.registryService = registryService;
    }

    public static CarbonEventPublisherManagementService getCarbonEventPublisherManagementService() {
        return carbonEventPublisherManagementService;
    }

    public static void registerPublisherManagementService(CarbonEventPublisherManagementService eventPublisherManagementService) {
        EventPublisherServiceValueHolder.carbonEventPublisherManagementService = eventPublisherManagementService;
    }

    public static void registerEventManagementService(EventManagementService eventManagementService) {
        EventPublisherServiceValueHolder.eventManagementService = eventManagementService;

    }

    public static void unSetRegistryService() {
        EventPublisherServiceValueHolder.registryService = null;
    }

    public static RegistryService getRegistryService() {
        return EventPublisherServiceValueHolder.registryService;
    }

    public static Registry getRegistry(int tenantId) throws RegistryException {
        return registryService.getConfigSystemRegistry(tenantId);
    }

    public static ConcurrentHashMap<String, OutputMapperFactory> getMappingFactoryMap() {
        return mappingFactoryMap;
    }

    public static EventManagementService getEventManagementService() {
        return eventManagementService;
    }

    public static void registerEventStreamService(EventStreamService eventStreamService) {
        EventPublisherServiceValueHolder.eventStreamService = eventStreamService;
    }

    public static EventStreamService getEventStreamService() {
        return EventPublisherServiceValueHolder.eventStreamService;
    }

    public static void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        EventPublisherServiceValueHolder.configurationContextService = configurationContextService;
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public static Set<String> getOutputEventAdapterTypes() {
        return outputEventAdapterTypes;
    }

    public static void addOutputEventAdapterType(String outputEventAdapterType) {
        EventPublisherServiceValueHolder.outputEventAdapterTypes.add(outputEventAdapterType);
    }

    public static void removeOutputEventAdapterType(String outputEventAdapterType) {
        EventPublisherServiceValueHolder.outputEventAdapterTypes.remove(outputEventAdapterType);
    }

    public static void setGlobalStatisticsEnabled(boolean globalStatisticsEnabled) {
        EventPublisherServiceValueHolder.globalStatisticsEnabled = globalStatisticsEnabled;
    }

    public static boolean isGlobalStatisticsEnabled() {
        return globalStatisticsEnabled;
    }

    public static boolean isLazyLoading() {
        return isLazyLoading;
    }

    public static void setLazyLoading(boolean lazyLoading) {
        isLazyLoading = lazyLoading;
    }
}