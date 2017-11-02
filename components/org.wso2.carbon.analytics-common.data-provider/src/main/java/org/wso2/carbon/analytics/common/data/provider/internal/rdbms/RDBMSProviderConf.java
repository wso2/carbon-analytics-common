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
package org.wso2.carbon.analytics.common.data.provider.internal.rdbms;


/**
 * Model class for the RDBMS provider configurations.
 */
public class RDBMSProviderConf {
    private String databaseName;
    private String url;
    private String table;
    private String username;
    private String password;
    private String query;
    private int pollingInterval;

    public RDBMSProviderConf(String databaseName, String url, String table,
                             String username, String password, String query) {
        this.databaseName = databaseName;
        this.url = url;
        this.table = table;
        this.username = username;
        this.password = password;
        this.query = query;
    }


    public String getUrl() {
        return url;
    }

    public String getTable() {
        return table;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getQuery() {
        return query;
    }

    public int getPollingInterval() {
        return pollingInterval;
    }
}
