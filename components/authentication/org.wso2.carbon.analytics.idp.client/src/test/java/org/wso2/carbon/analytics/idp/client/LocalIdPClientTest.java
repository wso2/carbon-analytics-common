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
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.idp.client.core.exception.AuthenticationException;
import org.wso2.carbon.analytics.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.analytics.idp.client.core.models.Role;
import org.wso2.carbon.analytics.idp.client.core.models.User;
import org.wso2.carbon.analytics.idp.client.core.utils.IdPClientConstants;
import org.wso2.carbon.analytics.idp.client.local.LocalIdPClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test cases for Local IdP Client.
 */
public class LocalIdPClientTest {

    @Test
    public void testLoginSuccess() throws IdPClientException {
        Map<String, String> userProperties = new HashMap<>();
        Role defaultRole = new Role("1", "admin");
        User defaultUser = new User("admin", "YWRtaW4=", userProperties,
                Collections.singletonList(defaultRole));
        LocalIdPClient localIdPClient = new LocalIdPClient(1, Collections.singletonList(defaultUser),
                Collections.singletonList(defaultRole));

        Map<String, String> loginProperties = new HashMap<>();
        loginProperties.put(IdPClientConstants.GRANT_TYPE, IdPClientConstants.PASSWORD_GRANT_TYPE);
        loginProperties.put(IdPClientConstants.USERNAME, "admin");
        loginProperties.put(IdPClientConstants.PASSWORD, "admin");

        Map<String, String> login = localIdPClient.login(loginProperties);
        Assert.assertEquals(IdPClientConstants.LoginStatus.LOGIN_SUCCESS,
                login.get(IdPClientConstants.LOGIN_STATUS));
    }

    @Test
    public void testLoginFailure() throws IdPClientException, AuthenticationException {
        Map<String, String> userProperties = new HashMap<>();
        Role defaultRole = new Role("1", "admin");
        User defaultUser = new User("admin", "YWRtaW4=", userProperties,
                Collections.singletonList(defaultRole));
        LocalIdPClient localIdPClient = new LocalIdPClient(1, Collections.singletonList(defaultUser),
                Collections.singletonList(defaultRole));

        Map<String, String> loginProperties = new HashMap<>();
        loginProperties.put(IdPClientConstants.GRANT_TYPE, IdPClientConstants.PASSWORD_GRANT_TYPE);
        loginProperties.put(IdPClientConstants.USERNAME, "admin2");
        loginProperties.put(IdPClientConstants.PASSWORD, "admin");

        Map<String, String> login = localIdPClient.login(loginProperties);
        Assert.assertEquals(IdPClientConstants.LoginStatus.LOGIN_FAILURE,
                login.get(IdPClientConstants.LOGIN_STATUS));
    }

    @Test
    public void testLoginFailureNullValue() throws IdPClientException, AuthenticationException {
        Map<String, String> userProperties = new HashMap<>();
        Role defaultRole = new Role("1", "admin");
        User defaultUser = new User("admin", "YWRtaW4=", userProperties,
                Collections.singletonList(defaultRole));
        LocalIdPClient localIdPClient = new LocalIdPClient(1, Collections.singletonList(defaultUser),
                Collections.singletonList(defaultRole));

        Map<String, String> loginProperties = new HashMap<>();
        loginProperties.put(IdPClientConstants.GRANT_TYPE, IdPClientConstants.PASSWORD_GRANT_TYPE);
        loginProperties.put(IdPClientConstants.PASSWORD, "admin");

        Map<String, String> login = localIdPClient.login(loginProperties);
        Assert.assertEquals(IdPClientConstants.LoginStatus.LOGIN_FAILURE,
                login.get(IdPClientConstants.LOGIN_STATUS));
    }

    @Test(expectedExceptions = AuthenticationException.class)
    public void testSessionTimeout() throws InterruptedException, IdPClientException, AuthenticationException {
        Map<String, String> userProperties = new HashMap<>();
        Role defaultRole = new Role("1", "admin");
        User defaultUser = new User("admin", "YWRtaW4=", userProperties,
                Collections.singletonList(defaultRole));
        LocalIdPClient localIdPClient = new LocalIdPClient(1, Collections.singletonList(defaultUser),
                Collections.singletonList(defaultRole));

        Map<String, String> loginProperties = new HashMap<>();
        loginProperties.put(IdPClientConstants.GRANT_TYPE, IdPClientConstants.PASSWORD_GRANT_TYPE);
        loginProperties.put(IdPClientConstants.USERNAME, "admin");
        loginProperties.put(IdPClientConstants.PASSWORD, "admin");

        Map<String, String> login = localIdPClient.login(loginProperties);
        Assert.assertEquals(IdPClientConstants.LoginStatus.LOGIN_SUCCESS,
                login.get(IdPClientConstants.LOGIN_STATUS));
        String sessionId = login.get(IdPClientConstants.ACCESS_TOKEN);
       Thread.sleep(5000);
        boolean authenticate = localIdPClient.authenticate(sessionId);
        Assert.assertEquals(authenticate, true);
    }

    @Test
    public void testLoginSuccessWithinSessionTime() throws IdPClientException {
        Map<String, String> userProperties = new HashMap<>();
        Role defaultRole = new Role("1", "admin");
        User defaultUser = new User("admin", "YWRtaW4=", userProperties,
                Collections.singletonList(defaultRole));
        LocalIdPClient localIdPClient = new LocalIdPClient(1, Collections.singletonList(defaultUser),
                Collections.singletonList(defaultRole));

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
        Map<String, String> userProperties = new HashMap<>();
        Role defaultRole = new Role("1", "admin");
        User defaultUser = new User("admin", "YWRtaW4=", userProperties,
                Collections.singletonList(defaultRole));
        LocalIdPClient localIdPClient = new LocalIdPClient(1, Collections.singletonList(defaultUser),
                Collections.singletonList(defaultRole));
        List<Role> allRoles = localIdPClient.getAllRoles();
        Assert.assertEquals(allRoles, Collections.singletonList(defaultRole));
    }

    @Test
    public void testGetUserRoles() throws IdPClientException {
        Map<String, String> userProperties = new HashMap<>();
        Role defaultRole = new Role("1", "admin");
        User defaultUser = new User("admin", "YWRtaW4=", userProperties,
                Collections.singletonList(defaultRole));
        LocalIdPClient localIdPClient = new LocalIdPClient(1, Collections.singletonList(defaultUser),
                Collections.singletonList(defaultRole));
        List<Role> allRoles = localIdPClient.getUserRoles("admin");
        Assert.assertEquals(allRoles, Collections.singletonList(defaultRole));
    }

    @Test
    public void testGetUserRolesFailure() throws IdPClientException {
        Map<String, String> userProperties = new HashMap<>();
        Role defaultRole = new Role("1", "admin");
        User defaultUser = new User("admin", "YWRtaW4=", userProperties,
                Collections.singletonList(defaultRole));
        LocalIdPClient localIdPClient = new LocalIdPClient(1, Collections.singletonList(defaultUser),
                Collections.singletonList(defaultRole));
        List<Role> allRoles = localIdPClient.getUserRoles("admin2");
        Assert.assertEquals(allRoles, new ArrayList<>());
    }

    @Test
    public void testGetUser() throws IdPClientException {
        Map<String, String> userProperties = new HashMap<>();
        Role defaultRole = new Role("1", "admin");
        User defaultUser = new User("admin", "YWRtaW4=", userProperties,
                Collections.singletonList(defaultRole));
        LocalIdPClient localIdPClient = new LocalIdPClient(1, Collections.singletonList(defaultUser),
                Collections.singletonList(defaultRole));
        User admin = localIdPClient.getUser("admin");
        Assert.assertEquals(admin, defaultUser);
    }

    @Test
    public void testGetUserFailure() throws IdPClientException {
        Map<String, String> userProperties = new HashMap<>();
        Role defaultRole = new Role("1", "admin");
        User defaultUser = new User("admin", "YWRtaW4=", userProperties,
                Collections.singletonList(defaultRole));
        LocalIdPClient localIdPClient = new LocalIdPClient(1, Collections.singletonList(defaultUser),
                Collections.singletonList(defaultRole));
        Assert.assertNull(localIdPClient.getUser("admin2"));
    }
}
