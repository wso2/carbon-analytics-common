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
package org.wso2.carbon.analytics.common.data.provider.rdbms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.common.data.provider.rdbms.config.RDBMSDataProviderConf;
import org.wso2.carbon.analytics.common.data.provider.utils.AbstractStreamingDataProvider;
import org.wso2.carbon.analytics.common.data.provider.spi.ProviderConfig;

/**
 * RDBMS data provider instance.
 */

public class RDBMSStreamingDataProvider extends AbstractStreamingDataProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(RDBMSStreamingDataProvider.class);
    private RDBMSDataProviderConf rdbmsProviderConf;

    @Override
    public void publish(String sessionID) {
    }

    @Override
    public void purging() {
    }

    @Override
    public void setProviderConfig(ProviderConfig providerConfig) {
        this.rdbmsProviderConf = (RDBMSDataProviderConf) providerConfig;
    }

    @Override
    public boolean configValidator(ProviderConfig providerConfig){
        return true;
    }
}
