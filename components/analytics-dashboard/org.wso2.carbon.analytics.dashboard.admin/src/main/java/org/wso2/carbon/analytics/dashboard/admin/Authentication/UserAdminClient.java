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

package org.wso2.carbon.analytics.dashboard.admin.Authentication;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;

import java.rmi.RemoteException;

/**
 * This class authenticates user with WSO2 Carbon using AuthenticationAdmin service 
 */
public class UserAdminClient {

    /**
     * Cookie String
     */
    private String authCookie;

    /**
     * Axis2 configuration context
     */
    private ConfigurationContext configContext;

    /**
     * WSO2 Carbon server url
     */
    private String serverUrl;

	/**
	 * Logger
	 */
	private static Log logger = LogFactory.getLog(UserAdminClient.class);


	public UserAdminClient(ConfigurationContext configContext, String serverUrl) {
        this.configContext = configContext;
        this.serverUrl = serverUrl;
    }

    /**
     * authenticates user with the WSO2 Carbon products
     * @param userName  user name
     * @param password   password
     * @return  if success, return true, fails, return false.
     * @throws Exception throws
     */
    public boolean authenticate(String userName, String password) throws  Exception{
	    String serviceEndPoint;
	    AuthenticationAdminStub authStub;
	    boolean authenticate;
	    try {
		    serviceEndPoint = serverUrl + "AuthenticationAdmin";
		    authStub = new AuthenticationAdminStub(configContext, serviceEndPoint);
		    authStub._getServiceClient().getOptions().setManageSession(true);
		    authenticate = authStub.login(userName, password, null);
		    authCookie = (String) authStub._getServiceClient().getServiceContext()
		                                  .getProperty(HTTPConstants.COOKIE_STRING);
		    return authenticate;

	    }catch(LoginAuthenticationExceptionException le){
		    logger.error(le);
		    throw le;
	    } catch (AxisFault axisFault) {
		    logger.error(axisFault);
		    throw axisFault;
	    } catch (RemoteException re) {
		    logger.error(re);
		    throw re;
	    }
    }
}