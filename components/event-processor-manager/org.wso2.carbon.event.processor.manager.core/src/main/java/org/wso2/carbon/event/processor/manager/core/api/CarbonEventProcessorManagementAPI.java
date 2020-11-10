/*
 *
 *  * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied. See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */
package org.wso2.carbon.event.processor.manager.core.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.processor.manager.core.exception.EventManagementException;
import org.wso2.carbon.event.processor.manager.core.internal.CarbonEventManagementService;

/**
 * This is the actual implementation of the event processor DataAPI osgi service exposed.
 */
public class CarbonEventProcessorManagementAPI implements EventProcessorManagementAPI {
    CarbonEventManagementService carbonEventManagementService = new CarbonEventManagementService();
    private static final Log logger = LogFactory.getLog(CarbonEventProcessorManagementAPI.class);
    public CarbonEventProcessorManagementAPI(CarbonEventManagementService carbonEventManagementService) {
        this.carbonEventManagementService = carbonEventManagementService;
        logger.info("CarbonEventProcessorManagementAPI initiated with carbonEventManagementService.");
    }

    @Override
    public void persistStates() throws EventManagementException {
        carbonEventManagementService.persist();
    }

    @Override
    public boolean getIsActiveNode() throws EventManagementException {
        return carbonEventManagementService.isActiveNode();
    }

}
