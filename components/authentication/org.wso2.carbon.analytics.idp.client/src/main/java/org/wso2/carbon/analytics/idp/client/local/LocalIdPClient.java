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
import org.wso2.carbon.analytics.idp.client.core.models.Role;
import org.wso2.carbon.analytics.idp.client.core.models.User;
import org.wso2.carbon.analytics.idp.client.core.utils.IdPClientConstants;
import org.wso2.carbon.analytics.idp.client.local.models.LocalSession;
import org.wso2.carbon.analytics.idp.client.local.models.LocalUser;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Local IdP Client.
 */
public class LocalIdPClient implements IdPClient {

    private static final Logger LOG = LoggerFactory.getLogger(LocalIdPClient.class);

    private Map<Integer, LocalSession> usersToSessionMap;
    private Map<String, LocalSession> sessionIdSessionMap;
    private Map<String, LocalSession> refreshIdSessionMap;
    private int sessionTimeout;
    private int refreshTimeout;
    private List<LocalUser> usersList;
    private Role adminRole;
    private List<Role> rolesList;

    public LocalIdPClient(int sessionTimeOut, int refreshTimeout, List<LocalUser> users, List<Role> roles,
                          Role adminRole) {
        this.sessionTimeout = sessionTimeOut;
        this.refreshTimeout = refreshTimeout;
        this.adminRole = adminRole;
        this.rolesList = roles;
        this.usersList = users;
        this.usersToSessionMap = new HashMap<>();
        this.sessionIdSessionMap = new HashMap<>();
        this.refreshIdSessionMap = new HashMap<>();
    }

    @Override
    public List<Role> getAllRoles() {
        return rolesList;
    }

    @Override
    public Role getAdminRole() {
        return adminRole;
    }

    @Override
    public User getUser(String name) {
        LocalUser user = getUserFromUsersList(name);
        if (user != null) {
            return new User(user.getUsername(), user.getProperties(), user.getRoles());
        }
        return null;
    }

    @Override
    public List<Role> getUserRoles(String name) {
        LocalUser user = getUserFromUsersList(name);
        if (user != null) {
            return user.getRoles();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("User with username '" + name + "' is not present when retrieving user roles.");
        }
        return new ArrayList<>();
    }

    @Override
    public Map<String, String> login(Map<String, String> properties) {
        Map<String, String> returnProperties = new HashMap<>();
        String grantType = properties.getOrDefault(IdPClientConstants.GRANT_TYPE,
                IdPClientConstants.PASSWORD_GRANT_TYPE);
        String userName, password, errorMessage;
        switch (grantType) {
            case IdPClientConstants.PASSWORD_GRANT_TYPE:
                userName = properties.get(IdPClientConstants.USERNAME);
                password = properties.get(IdPClientConstants.PASSWORD);
                LocalSession session;
                int userValue = (userName + ":" + password).hashCode();
                if (userName != null & password != null) {
                    //Checking if session is already present and update expiry time.
                    LocalSession oldSession = this.usersToSessionMap.get(userValue);
                    if (oldSession != null) {
                        ZonedDateTime createdAt = ZonedDateTime.now();
                        oldSession.setExpiryTime(createdAt.plusSeconds(this.sessionTimeout));
                        oldSession.setRefreshExpiryTime(createdAt.plusSeconds(this.refreshTimeout));

                        usersToSessionMap.replace(userValue, oldSession);
                        sessionIdSessionMap.replace(oldSession.getSessionId().toString(), oldSession);
                        refreshIdSessionMap.replace(oldSession.getRefreshId().toString(), oldSession);

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("User '" + userName + "' session is extended.");
                        }

                        returnProperties.put(IdPClientConstants.LOGIN_STATUS,
                                IdPClientConstants.LoginStatus.LOGIN_SUCCESS);
                        returnProperties.put(IdPClientConstants.USERNAME, userName);
                        returnProperties.put(IdPClientConstants.ACCESS_TOKEN, oldSession.getSessionId().toString());
                        returnProperties.put(IdPClientConstants.REFRESH_TOKEN, oldSession.getRefreshId().toString());
                        returnProperties.put(
                                IdPClientConstants.VALIDITY_PERIOD, String.valueOf(this.sessionTimeout));
                        returnProperties.put(
                                IdPClientConstants.REFRESH_VALIDITY_PERIOD, String.valueOf(this.refreshTimeout));

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
                LocalUser user = getUserFromUsersList(userName);
                if (user != null) {

                    CharBuffer charBuffer = CharBuffer.wrap(user.getPassword());
                    ByteBuffer byteBuffer = Base64.getDecoder().decode(Charset.forName("UTF-8").encode(charBuffer));
                    byte[] plainUserPassword = Arrays.copyOfRange(byteBuffer.array(),
                            byteBuffer.position(), byteBuffer.limit());
                    Arrays.fill(charBuffer.array(), '\u0000'); // clear sensitive data
                    Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data

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
                        ZonedDateTime createdAt = ZonedDateTime.now();
                        session = new LocalSession(userValue, userName, createdAt.plusSeconds(this.sessionTimeout),
                                createdAt.plusSeconds(this.refreshTimeout));
                        returnProperties.put(
                                IdPClientConstants.VALIDITY_PERIOD, String.valueOf(this.sessionTimeout));
                        returnProperties.put(
                                IdPClientConstants.REFRESH_VALIDITY_PERIOD, String.valueOf(this.refreshTimeout));

                        usersToSessionMap.put(userValue, session);
                        sessionIdSessionMap.put(session.getSessionId().toString(), session);
                        refreshIdSessionMap.put(session.getRefreshId().toString(), session);

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("User '" + userName + "' is logged in.");
                        }

                        returnProperties.put(IdPClientConstants.LOGIN_STATUS,
                                IdPClientConstants.LoginStatus.LOGIN_SUCCESS);
                        returnProperties.put(IdPClientConstants.USERNAME, userName);
                        returnProperties.put(IdPClientConstants.ACCESS_TOKEN, session.getSessionId().toString());
                        returnProperties.put(IdPClientConstants.REFRESH_TOKEN, session.getRefreshId().toString());
                        returnProperties.put(
                                IdPClientConstants.VALIDITY_PERIOD, String.valueOf(this.sessionTimeout));

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
            case IdPClientConstants.REFRESH_GRANT_TYPE:
                String refreshId = properties.get(IdPClientConstants.REFRESH_TOKEN);
                LocalSession refreshSession = refreshIdSessionMap.remove(refreshId);
                if (refreshSession != null) {
                    usersToSessionMap.remove(refreshSession.getUserHash());
                    sessionIdSessionMap.remove(refreshSession.getSessionId().toString());
                    if (refreshSession.getRefreshExpiryTime().isAfter(ZonedDateTime.now())) {
                        ZonedDateTime refreshedAt = ZonedDateTime.now();
                        refreshSession.setSessionId(UUID.randomUUID());
                        refreshSession.setExpiryTime(refreshedAt.plusSeconds(this.sessionTimeout));
                        refreshSession.setRefreshId(UUID.randomUUID());
                        refreshSession.setRefreshExpiryTime(refreshedAt.plusSeconds(this.refreshTimeout));

                        usersToSessionMap.put(refreshSession.getUserHash(), refreshSession);
                        sessionIdSessionMap.put(refreshSession.getSessionId().toString(), refreshSession);
                        refreshIdSessionMap.put(refreshSession.getRefreshId().toString(), refreshSession);

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("User '" + refreshSession.getUsername() + "' session is refreshed.");
                        }

                        returnProperties.put(IdPClientConstants.LOGIN_STATUS,
                                IdPClientConstants.LoginStatus.LOGIN_SUCCESS);
                        returnProperties.put(IdPClientConstants.ACCESS_TOKEN, refreshSession.getSessionId().toString());
                        returnProperties.put(
                                IdPClientConstants.REFRESH_TOKEN, refreshSession.getRefreshId().toString());
                        returnProperties.put(
                                IdPClientConstants.VALIDITY_PERIOD, String.valueOf(this.sessionTimeout));
                        return returnProperties;
                    } else {
                        errorMessage = "The refresh token used for login is expired, Refresh token : '"
                                + refreshId + "'.";
                        LOG.error(errorMessage);
                        returnProperties.put(
                                IdPClientConstants.LOGIN_STATUS, IdPClientConstants.LoginStatus.LOGIN_FAILURE);
                        returnProperties.put(IdPClientConstants.ERROR, IdPClientConstants.Error.INVALID_CREDENTIALS);
                        returnProperties.put(IdPClientConstants.ERROR_DESCRIPTION, errorMessage);
                        return returnProperties;
                    }
                } else {
                    errorMessage = "The refresh token used for login are invalid, Refresh token : '" + refreshId + "'.";
                    LOG.error(errorMessage);
                    returnProperties.put(IdPClientConstants.LOGIN_STATUS, IdPClientConstants.LoginStatus.LOGIN_FAILURE);
                    returnProperties.put(IdPClientConstants.ERROR, IdPClientConstants.Error.INVALID_CREDENTIALS);
                    returnProperties.put(IdPClientConstants.ERROR_DESCRIPTION, errorMessage);
                    return returnProperties;
                }
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
        LocalSession session = sessionIdSessionMap.get(token);
        if (session != null) {
            usersToSessionMap.remove(session.getUserHash());
            sessionIdSessionMap.remove(token);
        }
    }

    @Override
    public String authenticate(String token) throws AuthenticationException {
        LocalSession session = sessionIdSessionMap.get(token);
        if (session == null) {
            throw new AuthenticationException("The session with id '" + token + "' is not valid.");
        }
        ZonedDateTime now = ZonedDateTime.now();
        if (session.getExpiryTime().isAfter(now)) {
            return session.getUsername();
        } else {
            usersToSessionMap.remove(session.getUserHash());
            sessionIdSessionMap.remove(session.getSessionId().toString());
            if (session.getRefreshExpiryTime().isAfter(now)) {
                refreshIdSessionMap.remove(session.getRefreshId().toString());
            }
            throw new AuthenticationException("The session with id '" + token + "' has expired.");
        }
    }

    private LocalUser getUserFromUsersList(String name) {
        return this.usersList.stream()
                .filter(user -> user.getUsername().equals(name))
                .findFirst()
                .orElse(null);
    }
}
