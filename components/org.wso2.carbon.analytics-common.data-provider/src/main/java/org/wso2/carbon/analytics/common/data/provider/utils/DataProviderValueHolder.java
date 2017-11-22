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

import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.datasource.core.api.DataSourceService;

/**
 * Value holder for data provider.
 */
public class DataProviderValueHolder {
    private static DataProviderValueHolder dataProviderHelper = null;
    private static DataSourceService dataSourceService = null;
    private static ConfigProvider configProvider = null;

    public static DataProviderValueHolder getDataProviderHelper() {
        if (dataProviderHelper == null) {
            dataProviderHelper = new DataProviderValueHolder();
        }
        return dataProviderHelper;
    }

    public static DataSourceService getDataSourceService() {
        return dataSourceService;
    }

    public static void setDataSourceService(DataSourceService dataSourceService) {
        DataProviderValueHolder.dataSourceService = dataSourceService;
    }

    public static ConfigProvider getConfigProvider() {
        return configProvider;
    }

    public static void setConfigProvider(ConfigProvider configProvider) {
        DataProviderValueHolder.configProvider = configProvider;
    }
}
