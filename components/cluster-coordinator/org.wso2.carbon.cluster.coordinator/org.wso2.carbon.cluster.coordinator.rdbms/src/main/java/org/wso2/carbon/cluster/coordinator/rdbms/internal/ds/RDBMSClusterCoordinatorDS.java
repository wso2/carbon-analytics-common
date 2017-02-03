/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.cluster.coordinator.rdbms.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.datasource.core.api.DataSourceService;

/**
 * @scr.reference name="datasource.service" interface="org.wso2.carbon.datasource.core.api.DataSourceService"
 * cardinality="1..1" policy="dynamic"  bind="setDataSourceService" unbind="unsetDataSourceService"
 */
public class RDBMSClusterCoordinatorDS {
    /**
     * The logger class.
     */
    private static final Log log = LogFactory.getLog(RDBMSClusterCoordinatorDS.class);

    /**
     * The activate method of the OSGI service.
     *
     * @param context Component context
     */
    protected void activate(ComponentContext context) {
    }

    /**
     * The setter for the datasource service.
     *
     * @param dataSourceService The datasource service to be set.
     */
    protected void setDataSourceService(DataSourceService dataSourceService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Datasource Service");
        }
        RDBMSClusterCoordinatorServiceHolder.setDataSourceService(dataSourceService);
    }

    /**
     * The unsetter for the datasource service.
     *
     * @param dataSourceService The datasource service to unset
     */
    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the DataSource Service");
        }
    }
}
