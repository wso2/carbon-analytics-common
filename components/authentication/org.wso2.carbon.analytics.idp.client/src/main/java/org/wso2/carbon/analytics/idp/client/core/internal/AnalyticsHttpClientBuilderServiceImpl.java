/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.analytics.idp.client.core.internal;

import feign.Client;
import feign.Feign;
import feign.Request;
import feign.auth.BasicAuthRequestInterceptor;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.gson.GsonDecoder;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.idp.client.core.api.AnalyticsHttpClientBuilderService;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.kernel.config.model.CarbonConfiguration;


/**
 * This class is the Feign Client Builder Service, which create a fiegn client for each Http Service, where it is
 * necessary. The reason for having a seperate Class is to encapsulate the hostname verification logic from their
 * HTTP service logic.
 */
@Component(
        service = AnalyticsHttpClientBuilderService.class,
        immediate = true
)
public class AnalyticsHttpClientBuilderServiceImpl implements AnalyticsHttpClientBuilderService {

    private static final Logger LOG = LoggerFactory.getLogger(AnalyticsHttpClientBuilderServiceImpl.class);
    private boolean isHostnameVerifierEnabled;

    @Activate
    protected void start(BundleContext bundleContext) throws Exception {
        LOG.debug("AnalyticsHttpClientBuilderServiceImpl Service Component Activated");
    }

    @Deactivate
    protected void stop() {
        LOG.debug("AnalyticsHttpClientBuilderServiceImpl Service Component Deactivated");
    }

    @Reference(
            name = "carbon.config.provider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider) {
        CarbonConfiguration carbonConfiguration = null;
        try {
            carbonConfiguration = configProvider.getConfigurationObject(CarbonConfiguration.class);
            this.isHostnameVerifierEnabled = carbonConfiguration.isHostnameVerificationEnabled();
        } catch (ConfigurationException e) {
            LOG.error("Error occurred while initializing AnalyticsHttpClientBuilderService: " + e.getMessage(), e);
        }

    }

    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        // Nothing to do
    }

    public Client newDefaultClientInstance() {
        if (!isHostnameVerifierEnabled) {
            return new Client.Default(null, (hostName, sslSession) -> true);
        } else {
            return new Client.Default(null, null);
        }
    }

    public <T> T build(String username, String password, int connectTimeoutMillis,
                       int readTimeoutMillis, Class<T> target, String url) {
        return Feign.builder().requestInterceptor(new BasicAuthRequestInterceptor(username, password))
                .encoder(new Encoder.Default()).decoder(new Decoder.Default())
                .options(new Request.Options(connectTimeoutMillis, readTimeoutMillis))
                .client(newDefaultClientInstance())
                .target(target, url);
    }

    public <T> T buildWithoutInterceptor(int connectTimeoutMillis, int readTimeoutMillis, Class<T> target, String url) {
        return Feign.builder()
                .encoder(new Encoder.Default()).decoder(new GsonDecoder())
                .options(new Request.Options(connectTimeoutMillis, readTimeoutMillis))
                .client(newDefaultClientInstance())
                .target(target, url);
    }

}
