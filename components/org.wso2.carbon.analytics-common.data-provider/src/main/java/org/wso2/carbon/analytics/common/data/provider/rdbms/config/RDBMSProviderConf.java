/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package org.wso2.carbon.analytics.common.data.provider.rdbms.config;


import org.wso2.carbon.analytics.common.data.provider.api.DataSetMetadata;
import org.wso2.carbon.analytics.common.data.provider.spi.ProviderConfig;

/**
 * Model class for the RDBMS provider configurations.
 */
public class RDBMSProviderConf implements ProviderConfig {
    private String datasourceName;
    private String query;
    private String tableName;
    private long publishingPollingInterval;
    private long purgingPollingInterval;
    private boolean isPurgingEnable;
    private DataSetMetadata dataSetMetadata;

    public RDBMSProviderConf(){
        this.publishingPollingInterval = 6000;
        this.purgingPollingInterval = 6000;
        this.isPurgingEnable = false;
    }

    public RDBMSProviderConf(String datasourceName, String tableName, String query,
                             DataSetMetadata dataSetMetadata) {
        this.datasourceName = datasourceName;
        this.query = query;
        this.tableName = tableName;
        this.dataSetMetadata = dataSetMetadata;
    }

    public RDBMSProviderConf(String datasourceName, String tableName, String query, long pollingInterval,
                             long initialDelay, DataSetMetadata dataSetMetadata, boolean isPurgingEnable) {
        this(datasourceName, tableName, query, dataSetMetadata);
        this.publishingPollingInterval = pollingInterval;
        this.purgingPollingInterval = initialDelay;
        this.isPurgingEnable = isPurgingEnable;
    }

    public String getDatasourceName() {
        return datasourceName;
    }

    public String getQuery() {
        return query;
    }

    public String getTableName() {
        return tableName;
    }

    public DataSetMetadata getDataSetMetadata(){
        return dataSetMetadata;
    }

    @Override
    public long getPublishingPollingInterval() {
        return publishingPollingInterval;
    }

    @Override
    public long getPurgingPollingInterval() {
        return purgingPollingInterval;
    }

    @Override
    public boolean isPurgingEnable() {
        return isPurgingEnable;
    }
}
