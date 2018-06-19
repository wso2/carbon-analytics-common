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

package org.wso2.carbon.analytics.idp.client.core.api;

import feign.Client;

/**
 * This interface is the Feign Client Builder Service, which create a fiegn client for each Http Service, where it is
 * necessary. The reason for having a seperate Class is to encapsulate the hostname verification logic from their
 * HTTP service logic.
 */
public interface AnalyticsHttpClientBuilderService {

    /**
     * Function to create new Feign client instance.
     *
     * @return Feign Client instance
     */
    Client newDefaultClientInstance();

    /**
     * Function to build Feign client factory.
     *
     * @param username User name to be used in auth header
     * @param password Password to be used in auth header
     * @param connectTimeoutMillis Connection timeout of the request
     * @param readTimeoutMillis Read timeout of the request
     * @param target target service stubs to be used by the factory
     * @param url Base url of the API to be created
     * @return Feign client factory
     */
    <T> T build(String username, String password, int connectTimeoutMillis,
                int readTimeoutMillis, Class<T> target, String url);

    /**
     * Function to build Feign client factory.
     *
     * @param connectTimeoutMillis Connection timeout of the request
     * @param readTimeoutMillis Read timeout of the request
     * @param target target service stubs to be used by the factory
     * @param url Base url of the API to be created
     * @return Feign client factory
     */
    <T> T buildWithoutInterceptor(int connectTimeoutMillis, int readTimeoutMillis, Class<T> target, String url);

}
