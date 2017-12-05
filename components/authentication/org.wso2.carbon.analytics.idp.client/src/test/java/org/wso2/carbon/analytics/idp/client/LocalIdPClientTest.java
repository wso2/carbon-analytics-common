/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.analytics.idp.client;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.idp.client.core.exception.AuthenticationException;
import org.wso2.carbon.analytics.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.analytics.idp.client.core.models.Role;
import org.wso2.carbon.analytics.idp.client.core.models.User;
import org.wso2.carbon.analytics.idp.client.core.utils.IdPClientConstants;
import org.wso2.carbon.analytics.idp.client.local.LocalIdPClient;
import org.wso2.carbon.analytics.idp.client.local.models.LocalUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test cases for Local IdP Client.
 */
public class LocalIdPClientTest {

    private Role defaultRole;
    private LocalUser defaultUser;
    private LocalIdPClient localIdPClient;

    @BeforeMethod
    public void initializeIdPClient() {
        Map<String, String> userProperties = new HashMap<>();
        this.defaultRole = new Role("1", "admin");
        char[] password = "YWRtaW4=".toCharArray();
        this.defaultUser = new LocalUser("admin", password, userProperties,
                Collections.singletonList(defaultRole));
        this.localIdPClient = new LocalIdPClient(1, Collections.singletonList(defaultUser),
                Collections.singletonList(defaultRole), defaultRole);

    }

    @Test
    public void testLoginSuccessPasswordGrantType() throws IdPClientException {

        Map<String, String> loginProperties = new HashMap<>();
        loginProperties.put(IdPClientConstants.GRANT_TYPE, IdPClientConstants.PASSWORD_GRANT_TYPE);
        loginProperties.put(IdPClientConstants.USERNAME, "admin");
        loginProperties.put(IdPClientConstants.PASSWORD, "admin");

        Map<String, String> login = localIdPClient.login(loginProperties);
        Assert.assertEquals(IdPClientConstants.LoginStatus.LOGIN_SUCCESS,
                login.get(IdPClientConstants.LOGIN_STATUS));
    }

    @Test
    public void testLoginSuccessClientCredentialsGrantType() throws IdPClientException {
        Map<String, String> loginProperties = new HashMap<>();
        loginProperties.put(IdPClientConstants.GRANT_TYPE, IdPClientConstants.CLIENT_CREDENTIALS_GRANT_TYPE);
        loginProperties.put(IdPClientConstants.USERNAME, "admin");
        loginProperties.put(IdPClientConstants.PASSWORD, "admin");

        Map<String, String> login = localIdPClient.login(loginProperties);
        Assert.assertEquals(IdPClientConstants.LoginStatus.LOGIN_SUCCESS,
                login.get(IdPClientConstants.LOGIN_STATUS));
    }

    @Test
    public void testLoginFailureUserNotFound() throws IdPClientException, AuthenticationException {
        Map<String, String> loginProperties = new HashMap<>();
        loginProperties.put(IdPClientConstants.GRANT_TYPE, IdPClientConstants.PASSWORD_GRANT_TYPE);
        loginProperties.put(IdPClientConstants.USERNAME, "admin2");
        loginProperties.put(IdPClientConstants.PASSWORD, "admin");

        Map<String, String> login = localIdPClient.login(loginProperties);
        Assert.assertEquals(IdPClientConstants.LoginStatus.LOGIN_FAILURE,
                login.get(IdPClientConstants.LOGIN_STATUS));
    }

    @Test
    public void testLoginFailureWrongPassword() throws IdPClientException, AuthenticationException {
        Map<String, String> loginProperties = new HashMap<>();
        loginProperties.put(IdPClientConstants.GRANT_TYPE, IdPClientConstants.PASSWORD_GRANT_TYPE);
        loginProperties.put(IdPClientConstants.USERNAME, "admin");
        loginProperties.put(IdPClientConstants.PASSWORD, "admin2");

        Map<String, String> login = localIdPClient.login(loginProperties);
        Assert.assertEquals(IdPClientConstants.LoginStatus.LOGIN_FAILURE,
                login.get(IdPClientConstants.LOGIN_STATUS));
    }

    @Test
    public void testLoginFailureUnsupportedGrantType() throws IdPClientException, AuthenticationException {
        Map<String, String> loginProperties = new HashMap<>();
        loginProperties.put(IdPClientConstants.GRANT_TYPE, "invalid_grant_type");
        loginProperties.put(IdPClientConstants.USERNAME, "admin2");
        loginProperties.put(IdPClientConstants.PASSWORD, "admin");

        Map<String, String> login = localIdPClient.login(loginProperties);
        Assert.assertEquals(IdPClientConstants.LoginStatus.LOGIN_FAILURE,
                login.get(IdPClientConstants.LOGIN_STATUS));
    }

    @Test
    public void testLoginFailureNullValue() throws IdPClientException, AuthenticationException {
        Map<String, String> loginProperties = new HashMap<>();
        loginProperties.put(IdPClientConstants.GRANT_TYPE, IdPClientConstants.PASSWORD_GRANT_TYPE);
        loginProperties.put(IdPClientConstants.PASSWORD, "admin");

        Map<String, String> login = localIdPClient.login(loginProperties);
        Assert.assertEquals(IdPClientConstants.LoginStatus.LOGIN_FAILURE,
                login.get(IdPClientConstants.LOGIN_STATUS));
    }

    @Test(expectedExceptions = AuthenticationException.class)
    public void testSessionTimeout() throws InterruptedException, IdPClientException, AuthenticationException {
        Map<String, String> loginProperties = new HashMap<>();
        loginProperties.put(IdPClientConstants.GRANT_TYPE, IdPClientConstants.PASSWORD_GRANT_TYPE);
        loginProperties.put(IdPClientConstants.USERNAME, "admin");
        loginProperties.put(IdPClientConstants.PASSWORD, "admin");
        loginProperties.put(IdPClientConstants.REMEMBER_ME, "false");

        Map<String, String> login = localIdPClient.login(loginProperties);
        Assert.assertEquals(IdPClientConstants.LoginStatus.LOGIN_SUCCESS,
                login.get(IdPClientConstants.LOGIN_STATUS));
        String sessionId = login.get(IdPClientConstants.ACCESS_TOKEN);
        Thread.sleep(5000);

        Assert.assertNotNull(localIdPClient.authenticate(sessionId));
    }

    @Test
    public void testLoginSuccessWithinSessionTime() throws IdPClientException {
        Map<String, String> loginProperties = new HashMap<>();
        loginProperties.put(IdPClientConstants.GRANT_TYPE, IdPClientConstants.PASSWORD_GRANT_TYPE);
        loginProperties.put(IdPClientConstants.USERNAME, "admin");
        loginProperties.put(IdPClientConstants.PASSWORD, "admin");

        Map<String, String> login = localIdPClient.login(loginProperties);
        Assert.assertEquals(IdPClientConstants.LoginStatus.LOGIN_SUCCESS,
                login.get(IdPClientConstants.LOGIN_STATUS));
        Map<String, String> login2 = localIdPClient.login(loginProperties);
        Assert.assertEquals(IdPClientConstants.LoginStatus.LOGIN_SUCCESS,
                login2.get(IdPClientConstants.LOGIN_STATUS));
    }

    @Test
    public void testGetAllRoles() throws IdPClientException {
        List<Role> allRoles = localIdPClient.getAllRoles();
        Assert.assertEquals(allRoles, Collections.singletonList(defaultRole));
    }

    @Test
    public void testGetUserRoles() throws IdPClientException {
        List<Role> allRoles = localIdPClient.getUserRoles("admin");
        Assert.assertEquals(allRoles, Collections.singletonList(defaultRole));
    }

    @Test
    public void testGetUserRolesFailure() throws IdPClientException {
        List<Role> allRoles = localIdPClient.getUserRoles("admin2");
        Assert.assertEquals(allRoles, new ArrayList<>());
    }

    @Test
    public void testGetUser() throws IdPClientException {
        User admin = localIdPClient.getUser("admin");
        Assert.assertEquals(admin.getUsername(), "admin");
    }

    @Test
    public void testGetUserFailure() throws IdPClientException {
        Assert.assertNull(localIdPClient.getUser("admin2"));
    }

    @Test(expectedExceptions = AuthenticationException.class)
    public void testLogout() throws IdPClientException, AuthenticationException {
        //logging in
        Map<String, String> loginProperties = new HashMap<>();
        loginProperties.put(IdPClientConstants.GRANT_TYPE, IdPClientConstants.PASSWORD_GRANT_TYPE);
        loginProperties.put(IdPClientConstants.USERNAME, "admin");
        loginProperties.put(IdPClientConstants.PASSWORD, "admin");

        Map<String, String> login = localIdPClient.login(loginProperties);

        Assert.assertEquals(IdPClientConstants.LoginStatus.LOGIN_SUCCESS,
                login.get(IdPClientConstants.LOGIN_STATUS));

        //logging out
        Map<String, String> properties = new HashMap<>();
        properties.put(IdPClientConstants.USERNAME, "admin");
        properties.put(IdPClientConstants.PASSWORD, "admin");
        properties.put(IdPClientConstants.ACCESS_TOKEN, login.get(IdPClientConstants.ACCESS_TOKEN));
        localIdPClient.logout(properties);

        String sessionId = login.get(IdPClientConstants.ACCESS_TOKEN);
        Assert.assertNotNull(localIdPClient.authenticate(sessionId));
    }
}
