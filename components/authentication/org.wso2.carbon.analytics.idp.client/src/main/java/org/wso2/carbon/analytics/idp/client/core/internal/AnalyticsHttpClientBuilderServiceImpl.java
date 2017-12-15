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
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;

import org.wso2.carbon.analytics.idp.client.core.api.AnalyticsHttpClientBuilderService;


/**
 * This class is the Feign Client Builder Service, which create a fiegn client for each Http Service, where it is
 * necessary. The reason for having a seperate Class is to encapsulate the hostname verification logic from their
 * HTTP service logic.
 */
public class AnalyticsHttpClientBuilderServiceImpl implements AnalyticsHttpClientBuilderService {

    private boolean isHostnameVerifierEnabled;

    public AnalyticsHttpClientBuilderServiceImpl(boolean isEnabled) {
        this.isHostnameVerifierEnabled = isEnabled;
    }

    public Client newDefaultClientInstance() {
        if (isHostnameVerifierEnabled) {
            return new Client.Default(null, (hostName, sslSession) -> true);
        } else {
            return new Client.Default(null, null);
        }
    }

    public <T> T build(String username, String password, int connectTimeoutMillis,
                       int readTimeoutMillis, Class<T> target, String url) {
        return Feign.builder().requestInterceptor(new BasicAuthRequestInterceptor(username, password))
                .encoder(new GsonEncoder()).decoder(new GsonDecoder())
                .options(new Request.Options(connectTimeoutMillis, readTimeoutMillis))
                .client(newDefaultClientInstance())
                .target(target, url);
    }
}
