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

package org.wso2.carbon.data.provider;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.data.provider.api.DataModel;
import org.wso2.carbon.data.provider.api.DataSetMetadata;
import org.wso2.carbon.data.provider.endpoint.DataProviderEndPoint;
import org.wso2.carbon.data.provider.exception.DataProviderException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Abstract data provider class.
 */
public abstract class AbstractDataProvider implements DataProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDataProvider.class);
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
                    "of data provider.");
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

    public void publishToEndPoint(ArrayList<Object[]> data, String sessionID) {
        if (data.size() > 0) {
            DataModel dataModel = new DataModel(getMetadata(), data.toArray(new Object[0][0]), -1);
            try {
                DataProviderEndPoint.sendText(new Gson().toJson(dataModel), sessionID);
            } catch (IOException e) {
                LOGGER.error("Failed to deliver message to client " + e.getMessage(), e);
            }
        }
    }

    @Override
    public abstract boolean configValidator(ProviderConfig providerConfig) throws DataProviderException;

    public abstract void publish(String sessionID);

    public abstract void purging();

    /**
     * Set the provider configuration, child class will be maintained
     * its own provider configuration bean object.
     * @param providerConfig client provided configuration.
     */
    public abstract void setProviderConfig(ProviderConfig providerConfig);

    /**
     * Get the meta data of the RDBMS provider.
     * @return rdbms meta data object.
     */
    public abstract DataSetMetadata getMetadata();
}
