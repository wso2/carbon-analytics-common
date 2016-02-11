/*
 * Copyright (c) 20016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.throttle.event.core.internal;

public class GlobalCEPClient {
//todo We need this class when wiring up global cep
//    private AuthenticationAdminStub authenticationAdminStub = null;
//    private String host = "localhost";           //The host on which CEP Server runs
//    private String port = "9453";                //CEP Server Management-Port
//    private static String username = "admin";    //If the user is NOT from super tenant, username would be in the format 'user@tenantdomain.com'
//    private static String password = "admin";
//    private static String cepHome = "/home/dilini/CEP/4.1.0/throttling/wso2cep-4.0.0_global";               //absolute location of CEP Server Home E.g. /home/dilini/CEP/4.1.0/throttling/wso2cep-4.0.0_global
//
//    /**
//     * Initialize
//     */
//    private void init() {
//        System.setProperty("javax.net.ssl.trustStore", cepHome + "/repository/resources/security/wso2carbon.jks");
//        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
//        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
//    }
//
//    /**
//     * Login to the system
//     *
//     * @param username valid username
//     * @param password valid password
//     * @return
//     * @throws java.rmi.RemoteException
//     * @throws org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException
//     *
//     */
//    private String login(String username, String password) throws RemoteException,
//                                                                 LoginAuthenticationExceptionException {
//        authenticationAdminStub = new AuthenticationAdminStub("https://" + host + ":" + port + "/services/AuthenticationAdmin");
//        String sessionCookie = null;
//
//        if (authenticationAdminStub.login(username, password, host)) {
//            System.out.println("Login Successful");
//
//            ServiceContext serviceContext = authenticationAdminStub.
//                    _getServiceClient().getLastOperationContext().getServiceContext();
//            sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
//        }
//        return sessionCookie;
//    }
//
//    private void deploy(String sessionCookie, String executionPlan)
//            throws RemoteException {
//        ServiceClient serviceClient;
//        Options options;
//
//        EventProcessorAdminServiceStub eventProcessorAdminServiceStub = new EventProcessorAdminServiceStub("https://" + host + ":" + port + "/services/EventProcessorAdminService");
//        serviceClient = eventProcessorAdminServiceStub._getServiceClient();
//        options = serviceClient.getOptions();
//        options.setManageSession(true);
//        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
//
//        eventProcessorAdminServiceStub.deployExecutionPlan(executionPlan);
//    }
//
//
//    private void logout() throws RemoteException, LogoutAuthenticationExceptionException {
//        authenticationAdminStub.logout();
//        System.out.println("Logout successful");
//    }
//
//
//    public void deployExecutionPlan(String executionPlan) {
//        try {
//            init();
//            String sessionID = login(username, password);
//            deploy(sessionID, executionPlan);
//            logout();
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        } catch (LogoutAuthenticationExceptionException e) {
//            e.printStackTrace();
//        } catch (LoginAuthenticationExceptionException e) {
//            e.printStackTrace();
//        }
//    }
}
