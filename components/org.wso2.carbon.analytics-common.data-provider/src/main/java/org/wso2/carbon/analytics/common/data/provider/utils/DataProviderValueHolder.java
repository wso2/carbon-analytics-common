package org.wso2.carbon.analytics.common.data.provider.utils;

import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.datasource.core.api.DataSourceService;

/**
 * Created by sajithd on 11/21/17.
 */
public class DataProviderValueHolder {
    private static DataProviderValueHolder dataProviderHelper = null;
    private static DataSourceService dataSourceService = null;
    private static ConfigProvider configProvider = null;

    public static DataProviderValueHolder getDataProviderHelper() {
        if(dataProviderHelper == null) {
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
