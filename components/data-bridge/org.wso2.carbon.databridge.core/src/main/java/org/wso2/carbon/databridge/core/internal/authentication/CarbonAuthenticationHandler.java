/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.databridge.core.internal.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.analytics.idp.client.core.utils.IdPClientConstants;
import org.wso2.carbon.databridge.core.DataBridgeServiceValueHolder;
import org.wso2.carbon.databridge.core.utils.AgentSession;

import java.util.HashMap;
import java.util.Map;


/**
 * CarbonAuthenticationHandler implementation that authenticate Agents.
 * via Carbon AuthenticationService
 */

public class CarbonAuthenticationHandler implements AuthenticationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(CarbonAuthenticationHandler.class);

    public boolean authenticate(String userName, String password) {
        try {
            Map<String, String> loginProperties = new HashMap<>();
            loginProperties.put(IdPClientConstants.USERNAME, userName);
            loginProperties.put(IdPClientConstants.PASSWORD, password);
            loginProperties.put(IdPClientConstants.GRANT_TYPE, IdPClientConstants.PASSWORD_GRANT_TYPE);
            Map<String, String> login = DataBridgeServiceValueHolder.getIdPClient().login(loginProperties);
            String loginStatus = login.get(IdPClientConstants.LOGIN_STATUS);
            if (loginStatus.equals(IdPClientConstants.LoginStatus.LOGIN_SUCCESS)) {
                return true;
            } else {
                LOG.error("Authentication failed for username '" + userName + "'. Error : '"
                        + login.get(IdPClientConstants.ERROR) + "'. Error Description : '"
                        + login.get(IdPClientConstants.ERROR_DESCRIPTION) + "'");
                return false;
            }
        } catch (IdPClientException e) {
            return false;
        }
    }

    @Override
    public void initContext(AgentSession agentSession) {
        //Not required
    }

    @Override
    public void destroyContext(AgentSession agentSession) {
        //Not required
    }

}

