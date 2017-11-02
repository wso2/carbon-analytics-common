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
package org.wso2.carbon.analytics.common.data.provider.api;


import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.common.data.provider.internal.rdbms.RDBMSProvider;
import org.wso2.carbon.analytics.common.data.provider.internal.rdbms.RDBMSProviderConf;
import org.wso2.carbon.analytics.common.data.provider.spi.DataProvider;

/**
 * Data provider factory - Use to create different data provider sources.
 */
public class ProviderFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderFactory.class);


    public DataProvider createNewDataProvider(String providerType, String providerConf) {

        DataProvider dataProvider = null;

        switch (providerType) {
            case "rdbms":
                RDBMSProviderConf conf = new Gson().fromJson(providerConf, RDBMSProviderConf.class);
                dataProvider = new RDBMSProvider(conf);
                break;
            default:
                throw new NullPointerException("Provider type not registered");
        }

        return dataProvider;
    }


}
