/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.analytics.idp.client.core.internal;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.idp.client.core.api.IdPClient;
import org.wso2.carbon.analytics.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.analytics.idp.client.core.spi.IdPClientFactory;
import org.wso2.carbon.analytics.idp.client.core.utils.IdPServiceUtils;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.kernel.startupresolver.StartupServiceUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * IdP Client Service Component.
 */
@Component(
        name = "IdPClientServiceComponent",
        immediate = true,
        property = {
                "componentName=" + IdPClientServiceComponent.COMPONENT_NAME
        }
)
public class IdPClientServiceComponent implements RequiredCapabilityListener {
    public static final String COMPONENT_NAME = "sp-idp-service";
    private static final Logger LOG = LoggerFactory.getLogger(IdPClientServiceComponent.class);

    private BundleContext bundleContext;
    private ServiceRegistration serviceRegistration;
    private ConfigProvider configProvider;
    private Map<String, IdPClientFactory> idPClientFactoryHashMap = new HashMap<>();

    @Activate
    protected void start(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Deactivate
    protected void stop() {
        LOG.info("IdPClient Component is deactivated...");
        if (this.serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }

    @Reference(
            name = "carbon.config.provider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        this.configProvider = null;
    }

    @Reference(
            name = "sp-idp-factory",
            service = IdPClientFactory.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterReader"
    )
    protected void registerReader(IdPClientFactory idPClientFactory) {
        idPClientFactoryHashMap.put(idPClientFactory.getType(), idPClientFactory);
        StartupServiceUtils.updateServiceCache(COMPONENT_NAME, IdPClientFactory.class);
    }

    protected void unregisterReader(IdPClientFactory idPClientFactory) {
        idPClientFactoryHashMap.remove(idPClientFactory.getType());
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        LOG.info("IdPClientServiceComponent is activated...");
        try {
            IdPClient idPClient = IdPServiceUtils.getIdPClient(configProvider, idPClientFactoryHashMap);
            this.serviceRegistration = bundleContext.registerService(IdPClient.class.getName(), idPClient, null);
        } catch (IdPClientException e) {
            LOG.error("Error occurred while initializing IdP Client", e);
        }
    }
}
