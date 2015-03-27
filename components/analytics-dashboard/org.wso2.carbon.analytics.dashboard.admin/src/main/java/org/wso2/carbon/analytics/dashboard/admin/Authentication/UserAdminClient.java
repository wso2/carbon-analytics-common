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


import java.rmi.RemoteException;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.authenticator.stub.AuthenticationAdmin;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminCallbackHandler;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LoginWithRememberMeOptionAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.RememberMeData;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;

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
    public boolean authenticate(String userName, String password) throws Exception {

        String serviceEndPoint;
        AuthenticationAdminStub authStub;
        boolean authenticate;
        
        serviceEndPoint = serverUrl + "AuthenticationAdmin";
        authStub = new AuthenticationAdminStub(configContext, serviceEndPoint);
        authStub._getServiceClient().getOptions().setManageSession(true);
        authenticate = authStub.login(userName, password, null);
        authCookie = (String) authStub._getServiceClient().getServiceContext().getProperty(
                HTTPConstants.COOKIE_STRING);
        
        
        return authenticate;
    }

   
    /**
     * TODO
     * Method that need to implement get all users defined in Carbon Server
     * @return user names as String array
     * @return user names as String array
     * @throws Exception, if fails
     */
    public String[] getAllUserNames()  throws Exception{

        String serviceEndPoint;
        UserAdminStub adminStub;

        serviceEndPoint = serverUrl + "UserAdmin";
        adminStub = new UserAdminStub(configContext, serviceEndPoint);
        ServiceClient client = adminStub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(HTTPConstants.COOKIE_STRING, authCookie);
        String s=client.toString();
        System.out.println(s);
        return adminStub.listUsers("*");
    }

}