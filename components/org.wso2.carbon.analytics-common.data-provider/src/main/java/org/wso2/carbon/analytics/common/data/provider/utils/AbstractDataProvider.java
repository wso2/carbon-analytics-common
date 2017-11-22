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

package org.wso2.carbon.analytics.common.data.provider.utils;

import org.wso2.carbon.analytics.common.data.provider.exception.DataProviderException;
import org.wso2.carbon.analytics.common.data.provider.spi.DataProvider;
import org.wso2.carbon.analytics.common.data.provider.spi.ProviderConfig;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Abstract data provider class.
 */
public abstract class AbstractDataProvider implements DataProvider {
    private String sessionID;
    private ScheduledExecutorService dataPublishingExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService dataPurgingExecutorService = Executors.newSingleThreadScheduledExecutor();
    private long publishingInterval;
    private long purgingInterval;
    private boolean isPurgingEnable;

    @Override
    public DataProvider init(String sessionID, ProviderConfig providerConfig)
            throws DataProviderException {
        if (configValidator(providerConfig)) {
            this.sessionID = sessionID;
            this.publishingInterval = providerConfig.getPublishingInterval();
            this.purgingInterval = providerConfig.getPurgingInterval();
            this.isPurgingEnable = providerConfig.isPurgingEnable();
            setProviderConfig(providerConfig);
            return this;
        } else {
            throw new DataProviderException("Invalid configuration provided. Unable to complete initialization " +
                    "of batch data provider.");
        }
    }

    @Override
    public void stop() {
        dataPublishingExecutorService.shutdown();
        dataPurgingExecutorService.shutdown();
    }

    @Override
    public void start() {
        dataPublishingExecutorService.scheduleAtFixedRate(() -> {
            publish(this.sessionID);
        }, 0, publishingInterval, TimeUnit.SECONDS);
        if (isPurgingEnable) {
            dataPublishingExecutorService.scheduleAtFixedRate(() -> {
                purging();
            }, 0, purgingInterval, TimeUnit.SECONDS);
        }
    }

    @Override
    public abstract boolean configValidator(ProviderConfig providerConfig) throws DataProviderException;

    public abstract void publish(String sessionID);

    public abstract void purging();

    public abstract void setProviderConfig(ProviderConfig providerConfig);
}
