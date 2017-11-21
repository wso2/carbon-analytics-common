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

import java.util.Arrays;

/**
 * Model class for the RDBMS provider configurations.
 */
public class RDBMSDataProviderConf implements ProviderConfig {
    private String datasourceName;
    private String query;
    private String tableName;
    private String incrementalColumn;
    private long publishingInterval;
    private long purgingInterval;
    private long publishingLimit;
    private long purgingLimit;
    private boolean isPurgingEnable;

    public RDBMSDataProviderConf(){
        this.publishingInterval = 6000;
        this.purgingInterval = 6000;
        this.isPurgingEnable = false;
    }

    public RDBMSDataProviderConf(String datasourceName, String tableName, String query) {
        this.datasourceName = datasourceName;
        this.query = query;
        this.tableName = tableName;
    }

    public RDBMSDataProviderConf(String datasourceName, String tableName, String query, long pollingInterval,
                                 long initialDelay, boolean isPurgingEnable) {
        this(datasourceName, tableName, query);
        this.publishingInterval = pollingInterval;
        this.purgingInterval = initialDelay;
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

    public String getIncrementalColumn() {
        return incrementalColumn;
    }

    public long getPublishingLimit() {
        return publishingLimit;
    }

    public long getPurgingLimit() {
        return purgingLimit;
    }

    @Override
    public long getPublishingInterval() {
        return publishingInterval;
    }

    @Override
    public long getPurgingInterval() {
        return purgingInterval;
    }

    @Override
    public boolean isPurgingEnable() {
        return isPurgingEnable;
    }
}
