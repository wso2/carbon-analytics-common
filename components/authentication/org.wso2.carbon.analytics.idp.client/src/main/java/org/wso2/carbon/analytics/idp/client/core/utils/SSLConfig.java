/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.analytics.idp.client.core.utils;

import org.wso2.carbon.analytics.idp.client.core.utils.config.SSLConfiguration;
import org.wso2.carbon.utils.StringUtils;

/**
 * SSL Configs.
 */
public class SSLConfig {

    private boolean sslConfigsExistInConfigProvider = false;
    private final String keyStorePassword;
    private final String trustStorePassword;
    private final String keyStoreLocation;
    private final String trustStoreLocation;

    public SSLConfig(SSLConfiguration sslConfiguration) {

        this.keyStorePassword = sslConfiguration.getKeyStorePassword();
        this.trustStorePassword = sslConfiguration.getTrustStorePassword();
        this.keyStoreLocation = sslConfiguration.getKeyStoreLocation();
        this.trustStoreLocation = sslConfiguration.getTrustStoreLocation();
        if (!StringUtils.isNullOrEmptyAfterTrim(keyStorePassword)
                && !StringUtils.isNullOrEmptyAfterTrim(keyStoreLocation)
                && !StringUtils.isNullOrEmptyAfterTrim(trustStorePassword)
                && !StringUtils.isNullOrEmptyAfterTrim(trustStoreLocation)) {
            sslConfigsExistInConfigProvider = true;
        }
    }

    public boolean isSSLConfigsExistInConfigProvider() {

        return sslConfigsExistInConfigProvider;
    }

    public void exportSSLConfigsExistInConfigProvider() {

        System.setProperty(IdPClientConstants.SSL.KEY_STORE_PASSWORD, this.keyStorePassword);
        System.setProperty(IdPClientConstants.SSL.TRUST_STORE_PASSWORD, this.trustStorePassword);
        System.setProperty(IdPClientConstants.SSL.KEY_STORE, this.keyStoreLocation);
        System.setProperty(IdPClientConstants.SSL.TRUST_STORE, this.trustStoreLocation);
    }
}
