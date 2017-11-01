/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.analytics.permissions.internal.impl;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.permissions.PermissionManager;
import org.wso2.carbon.analytics.permissions.PermissionProvider;
import org.wso2.carbon.analytics.permissions.bean.PermissionConfig;
import org.wso2.carbon.analytics.permissions.exceptions.PermissionException;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides implementation for Permission Manager interface.
 */
@Component(
        name = "PermissionManagerImpl",
        service = PermissionManager.class,
        immediate = true
)
public class PermissionManagerImpl implements PermissionManager {
    private static final Logger log = LoggerFactory.getLogger(PermissionManagerImpl.class);

    private final Map<String, PermissionProvider> permissionProviders = new HashMap<>();

    private PermissionConfig permissionConfig;

    /**
     * Register permission provider.
     *
     * @param permissionProvider
     */
    @Reference(
            name = "permission.provider",
            service = PermissionProvider.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterPermissionProvider"
    )
    protected void registerConfigProvider(PermissionProvider permissionProvider) {
        this.permissionProviders.put(permissionProvider.getClass().getName(), permissionProvider);
    }

    /**
     * Unregister permission provider.
     *
     * @param permissionProvider
     */
    protected void unregisterPermissionProvider(PermissionProvider permissionProvider) {
        this.permissionProviders.remove(permissionProvider.getClass().getName());
    }

    /**
     * Register configuration provider.
     *
     * @param configProvider
     */
    @Reference(
            name = "carbon.config.provider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider) {
        try {
            permissionConfig = configProvider.getConfigurationObject(PermissionConfig.class);
        } catch (ConfigurationException e) {
            log.error("Error occurred while fetching permission configuration.", e);
            throw new PermissionException("Error occurred while fetching permission configuration.", e);
        }
    }

    /**
     * Unregister configuration provider.
     *
     * @param configProvider
     */
    protected void unregisterConfigProvider(ConfigProvider configProvider) {
    }

    /**
     * Get permission provider.
     *
     * @return
     */
    @Override
    public PermissionProvider getProvider() {
        String providerName = permissionConfig.getPermissionProvider();
        if (permissionProviders.containsKey(providerName)) {
            log.debug("Permission provider " + providerName + " found.");
            return permissionProviders.get(providerName);
        }

        log.error("Unable to find the permission provider \"" + providerName + "\"");
        throw new PermissionException("Unable to find permission provider \"" + providerName + "\"");
    }
}
