/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.throttle.event.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.throttle.event.core.ThrottlerService;
import org.wso2.carbon.throttle.event.core.internal.CarbonThrottlerService;

/**
 * @scr.component name="eventThrottlerService.component" immediate="true"
 * @scr.reference name="org.wso2.carbon.ndatasource" interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1" policy="dynamic" bind="setDataSourceService" unbind="unsetDataSourceService"
 */

public class ThrottlerDS {
    private static final Log log = LogFactory.getLog(ThrottlerDS.class);

    protected void activate(ComponentContext context) {
        try {
            CarbonThrottlerService throttlerService = new CarbonThrottlerService();
            ThrottleServiceValueHolder.registerThrottlerService(throttlerService);
            context.getBundleContext().registerService(ThrottlerService.class.getName(), throttlerService, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed the WSO2 Throttling Service");
            }
        } catch (Throwable e) {
            log.error("Could not create WSO2 Throttling Service: " + e.getMessage(), e);
        }
    }

    protected void deactivate(ComponentContext context){
        ThrottleServiceValueHolder.getThrottlerService().stop();
        if (log.isDebugEnabled()) {
            log.debug("Successfully deactivated the WSO2 throttling service");
        }
    }

    protected void setDataSourceService(DataSourceService dataSourceService) {
        ThrottleServiceValueHolder.setDataSourceService(dataSourceService);
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        ThrottleServiceValueHolder.setDataSourceService(null);
    }


}
