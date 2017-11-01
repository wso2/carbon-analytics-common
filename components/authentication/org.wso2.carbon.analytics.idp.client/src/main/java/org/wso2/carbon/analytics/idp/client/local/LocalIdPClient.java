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
package org.wso2.carbon.analytics.idp.client.local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.idp.client.core.api.IdPClient;
import org.wso2.carbon.analytics.idp.client.core.exception.AuthenticationException;
import org.wso2.carbon.analytics.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.analytics.idp.client.core.models.Role;
import org.wso2.carbon.analytics.idp.client.core.models.User;
import org.wso2.carbon.analytics.idp.client.core.utils.IdPClientConstants;
import org.wso2.carbon.analytics.idp.client.local.models.Session;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Local IdP Client.
 */
public class LocalIdPClient implements IdPClient {

    private static final Logger LOG = LoggerFactory.getLogger(LocalIdPClient.class);
    private Map<Integer, Session> usersToSessionMap = new HashMap<>();
    private Map<String, Session> sessionIdSessionMap = new HashMap<>();

    private int sessionTimeout;
    private List<User> usersList;
    private List<Role> rolesList;
    private int systemLoginCount;

    public LocalIdPClient(int sessionTimeOut, List<User> users, List<Role> roles) {
        this.sessionTimeout = sessionTimeOut * 1000;
        this.rolesList = roles;
        this.systemLoginCount = 0;
        this.usersList = users.stream().map((user) -> {
            List<String> roleIdList = Arrays.asList(user.getRoles().split(","));
            List<Role> userRolesFromId = this.rolesList.stream()
                    .filter((role) -> roleIdList.contains(role.getId()))
                    .collect(Collectors.toList());
            user.addRolesList(userRolesFromId);
            return user;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Role> getAllRoles() {
        return rolesList;
    }

    @Override
    public User getUser(String name) {
        User user = getUserFromUsersList(name);
        user.setPassword(null);
        return user;
    }

    @Override
    public List<Role> getUserRoles(String name) {
        User user = getUserFromUsersList(name);
        if (user != null) {
            return user.getRolesList();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("User with username '" + name + "' is not present when retrieving user roles.");
        }
        return null;
    }

    @Override
    public Map<String, String> login(Map<String, String> properties) throws IdPClientException {
        Map<String, String> returnProperties = new HashMap<>();
        String grantType = properties.getOrDefault(IdPClientConstants.GRANT_TYPE,
                IdPClientConstants.PASSWORD_GRANT_TYPE);
        String userName, password, errorMessage;
        switch (grantType) {
            case IdPClientConstants.PASSWORD_GRANT_TYPE:
                userName = properties.get(IdPClientConstants.USERNAME);
                password = properties.get(IdPClientConstants.PASSWORD);
                Session session;
                int userValue = (userName + ":" + password).hashCode();
                if (userName != null & password != null) {
                    //Checking if session is already present and update expiry time.
                    Session oldSession = this.usersToSessionMap.get(userValue);
                    if (oldSession != null) {
                        Long createdAt = Calendar.getInstance().getTimeInMillis();
                        oldSession.setExpiryTime(createdAt + this.sessionTimeout);
                        usersToSessionMap.replace(userValue, oldSession);
                        sessionIdSessionMap.replace(oldSession.getSessionId().toString(), oldSession);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("User '" + userName + "' session is extended.");
                        }
                        returnProperties.put(IdPClientConstants.LOGIN_STATUS,
                                IdPClientConstants.LoginStatus.LOGIN_SUCCESS);
                        returnProperties.put(IdPClientConstants.ACCESS_TOKEN, oldSession.getSessionId().toString());
                        returnProperties.put(IdPClientConstants.CREATED_AT, createdAt.toString());
                        returnProperties.put(IdPClientConstants.VALIDITY_PERIOD, String.valueOf(this.sessionTimeout));
                        return returnProperties;
                    }
                } else {
                    errorMessage = "The login credential used for login are invalid, username : '" + userName + "'.";
                    LOG.error(errorMessage);
                    returnProperties.put(IdPClientConstants.LOGIN_STATUS, IdPClientConstants.LoginStatus.LOGIN_FAILURE);
                    returnProperties.put(IdPClientConstants.ERROR, IdPClientConstants.Error.INVALID_CREDENTIALS);
                    returnProperties.put(IdPClientConstants.ERROR_DESCRIPTION, errorMessage);
                    return returnProperties;
                }
                User user = getUserFromUsersList(userName);
                if (user != null) {
                    byte[] plainUserPassword = Base64.getDecoder().decode(user.getPassword());
                    if (!Arrays.equals(plainUserPassword, password.getBytes(Charset.forName("UTF-8")))) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Password did not match with the configured user, userName: '" +
                                    userName + "', Failing the authentication.");
                        }
                        returnProperties.put(IdPClientConstants.LOGIN_STATUS,
                                IdPClientConstants.LoginStatus.LOGIN_FAILURE);
                        returnProperties.put(IdPClientConstants.ERROR, IdPClientConstants.Error.INVALID_CREDENTIALS);
                        returnProperties.put(IdPClientConstants.ERROR_DESCRIPTION, "The login credential used " +
                                "for login are invalid, username : '" + userName + "'.");
                        return returnProperties;
                    } else {
                        Long createdAt = Calendar.getInstance().getTimeInMillis();
                        session = new Session(userValue, false, createdAt + this.sessionTimeout);
                        usersToSessionMap.put(userValue, session);
                        sessionIdSessionMap.put(session.getSessionId().toString(), session);
                        returnProperties.put(IdPClientConstants.LOGIN_STATUS,
                                IdPClientConstants.LoginStatus.LOGIN_SUCCESS);
                        returnProperties.put(IdPClientConstants.ACCESS_TOKEN, session.getSessionId().toString());
                        returnProperties.put(IdPClientConstants.CREATED_AT, createdAt.toString());
                        returnProperties.put(IdPClientConstants.VALIDITY_PERIOD, String.valueOf(this.sessionTimeout));
                        return returnProperties;
                    }
                } else {
                    LOG.debug("User not found for userName: '" + userName + "'. Failing the authentication.");
                    returnProperties.put(IdPClientConstants.LOGIN_STATUS, IdPClientConstants.LoginStatus.LOGIN_FAILURE);
                    returnProperties.put(IdPClientConstants.ERROR, IdPClientConstants.Error.INVALID_CREDENTIALS);
                    returnProperties.put(IdPClientConstants.ERROR_DESCRIPTION, "The login credential used for " +
                            "login are invalid, username : '" + userName + "'.");
                    return returnProperties;
                }
            case IdPClientConstants.CLIENT_CREDENTIALS_GRANT_TYPE:
                //Login for any system service to access API's
                userName = IdPClientConstants.SYSTEM_LOGIN + (++this.systemLoginCount);
                password = IdPClientConstants.SYSTEM_LOGIN + (++this.systemLoginCount);
                int userHash = (userName + ":" + password).hashCode();
                Long createdAt = Calendar.getInstance().getTimeInMillis();
                Session newSession = new Session(userHash, true, createdAt + this.sessionTimeout);
                usersToSessionMap.put(userHash, newSession);
                sessionIdSessionMap.put(newSession.getSessionId().toString(), newSession);
                returnProperties.put(IdPClientConstants.LOGIN_STATUS, IdPClientConstants.LoginStatus.LOGIN_SUCCESS);
                returnProperties.put(IdPClientConstants.ACCESS_TOKEN, newSession.getSessionId().toString());
                returnProperties.put(IdPClientConstants.CREATED_AT, createdAt.toString());
                returnProperties.put(IdPClientConstants.VALIDITY_PERIOD, String.valueOf(this.sessionTimeout));
                return returnProperties;

            default:
                errorMessage = "The Grant Type '" + grantType + "' is not" +
                        "supported by the IdPClient '" + LocalIdPClient.class.getName();
                LOG.error(errorMessage);
                returnProperties.put(IdPClientConstants.LOGIN_STATUS, IdPClientConstants.LoginStatus.LOGIN_FAILURE);
                returnProperties.put(IdPClientConstants.ERROR, IdPClientConstants.Error.GRANT_TYPE_NOT_SUPPORTED);
                returnProperties.put(IdPClientConstants.ERROR_DESCRIPTION, errorMessage);
                return returnProperties;
        }
    }

    @Override
    public void logout(Map<String, String> properties) {
        String token = properties.get(IdPClientConstants.ACCESS_TOKEN);
        Session session = sessionIdSessionMap.get(token);
        if (session != null) {
            usersToSessionMap.remove(session.getUserHash());
            sessionIdSessionMap.remove(token);
        }
    }

    @Override
    public boolean authenticate(String token) throws AuthenticationException, IdPClientException {
        Session session = sessionIdSessionMap.get(token);
        if (session == null) {
            throw new AuthenticationException("The session with id '" + token + "' is not valid.");
        }
        if (session.isInternalUser()) {
            return true;
        }
        if (session.getExpiryTime() > Calendar.getInstance().getTimeInMillis()) {
            return true;
        } else {
            usersToSessionMap.remove(session.getUserHash());
            sessionIdSessionMap.remove(session.getSessionId().toString());
            throw new AuthenticationException("The session with id '" + token + "' has expired.");
        }
    }

    private User getUserFromUsersList(String name) {
        return this.usersList.stream()
                .filter(user -> user.getUsername().equals(name))
                .findFirst()
                .orElse(null);
    }
}
