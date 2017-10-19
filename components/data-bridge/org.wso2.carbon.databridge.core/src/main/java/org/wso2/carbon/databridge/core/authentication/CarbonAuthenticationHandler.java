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

package org.wso2.carbon.databridge.core.authentication;

import org.wso2.carbon.databridge.core.utils.AgentSession;


/**
 * CarbonAuthenticationHandler implementation that authenticate Agents.
 * via Carbon AuthenticationService
 */

public class CarbonAuthenticationHandler implements AuthenticationHandler {

    public boolean authenticate(String userName, String password) {
        // TODO: 9/14/17: Re-enable authentication
        /*
         * Caas based authentication was removed due to kernel/dependency issues.
         * Proper authentication will be added once the OSGi based authenticator is released.
         */
        return true;
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

