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
package org.wso2.carbon.analytics.idp.client.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.idp.client.core.api.IdPClient;
import org.wso2.carbon.analytics.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.analytics.idp.client.core.spi.IdPClientFactory;
import org.wso2.carbon.analytics.idp.client.core.utils.config.IdPClientConfiguration;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;

import java.util.Map;

/**
 * IdP Client Utils.
 */
public class IdPServiceUtils {
    private static final Logger LOG = LoggerFactory.getLogger(IdPServiceUtils.class);

    public static IdPClient getIdPClient(ConfigProvider configProvider, Map<String,
            IdPClientFactory> idPClientFactoryMap) throws IdPClientException {
        IdPClientFactory idPClientFactory;
        IdPClient idPClient;
        try {
            IdPClientConfiguration idPClientConfiguration;
            if (configProvider.getConfigurationObject(IdPClientConstants.SP_AUTH_NAMESPACE) == null) {
                idPClientConfiguration = new IdPClientConfiguration();
                LOG.info("Enabling default IdPClient Local User Store as configuration is not overridden.");
            } else {
                idPClientConfiguration = configProvider.
                        getConfigurationObject(IdPClientConfiguration.class);
            }
            idPClientFactory = idPClientFactoryMap.get(idPClientConfiguration.getType());
            if (idPClientFactory == null) {
                throw new IdPClientException("No idPClientFactory found for type: " + idPClientConfiguration.getType());
            }
            idPClient = idPClientFactory.getIdPClient(idPClientConfiguration);
            LOG.info("IdP client of type '" + idPClientConfiguration.getType() + "' is started.");
        } catch (ConfigurationException e) {
            throw new IdPClientException("Error in reading '" + IdPClientConstants.SP_AUTH_NAMESPACE + "' from file.",
                    e);
        }
        return idPClient;
    }
}
