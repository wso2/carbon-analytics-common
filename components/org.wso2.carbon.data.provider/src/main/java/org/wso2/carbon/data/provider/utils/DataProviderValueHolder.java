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

package org.wso2.carbon.data.provider.utils;

import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.data.provider.DataProvider;
import org.wso2.carbon.datasource.core.api.DataSourceService;

import java.util.HashMap;
import java.util.Map;
import javax.websocket.Session;

/**
 * Value holder for data provider.
 */
public class DataProviderValueHolder {
    private static DataProviderValueHolder dataProviderHelper = null;
    private DataSourceService dataSourceService = null;
    private ConfigProvider configProvider = null;
    private Map<String, DataProvider> dataProviderMap = new HashMap<>();
    private Session session;
    private Map<String, DataProvider> topicProviderMap = new HashMap<>();

    public static DataProviderValueHolder getDataProviderHelper() {
        if (dataProviderHelper == null) {
            dataProviderHelper = new DataProviderValueHolder();
        }
        return dataProviderHelper;
    }

    public DataSourceService getDataSourceService() {
        return dataSourceService;
    }

    public void setDataSourceService(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }

    public ConfigProvider getConfigProvider() {
        return configProvider;
    }

    public void setConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    public void setDataProvider(String providerName, DataProvider dataProvider) {
        this.dataProviderMap.put(providerName, dataProvider);
    }

    public DataProvider getDataProvider(String providerName) {
        return this.dataProviderMap.get(providerName);
    }

    public Map<String, DataProvider> getDataProviderMap() {
        return dataProviderMap;
    }

    public void setTopicProvider(String dataProviderTopic, DataProvider dataProvider) {
        this.topicProviderMap.put(dataProviderTopic, dataProvider);
    }

    public Map<String, DataProvider> getTopicProviderMap() {
        return topicProviderMap;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }
}
