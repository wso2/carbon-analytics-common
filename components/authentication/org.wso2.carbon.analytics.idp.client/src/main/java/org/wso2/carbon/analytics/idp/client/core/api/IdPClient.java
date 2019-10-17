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
package org.wso2.carbon.analytics.idp.client.core.api;

import org.wso2.carbon.analytics.idp.client.core.exception.AuthenticationException;
import org.wso2.carbon.analytics.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.analytics.idp.client.core.models.Role;
import org.wso2.carbon.analytics.idp.client.core.models.User;

import java.util.List;
import java.util.Map;

/**
 * This interface can be used to access common functionality of the IdpClient. This can be used by the dependent
 * component without the need to know about the underlying IdPClient implementation.
 */
public interface IdPClient {

    /**
     * This returns all roles available in the user store.
     *
     * @return List of {Role} objects
     * @throws IdPClientException thrown when an error occurred when retrieving roles
     */
    List<Role> getAllRoles() throws IdPClientException;

    /**
     * This returns all the roles available in the user store for the given tenant.
     *
     * @param username name of the user
     * @return List of {Role} objects
     * @throws IdPClientException thrown when an error occurred when retrieving roles
     */
    List<Role> getAllRolesOfTenant(String username) throws IdPClientException;

    /**
     * This returns the admin role of the user store.
     *
     * @return {Role}
     * @throws IdPClientException thrown when an error occurred when retrieving admin role
     */
    Role getAdminRole() throws IdPClientException;

    /**
     * This returns the {User} object with the name passed in the parameter.
     *
     * @param name Username
     * @return {User}
     * @throws IdPClientException thrown when an error occurred when retrieving user
     */
    User getUser(String name) throws IdPClientException;

    /**
     * This returns a list of the roles assigned to the user.
     *
     * @param name username
     * @return List of {Role}
     * @throws IdPClientException thrown when an error occurred when retrieving user role
     */
    List<Role> getUserRoles(String name) throws IdPClientException;

    /**
     * This returns the login parameters of a login request.
     *
     * @param properties Map of properties that is needed for login
     * @return Map of login properties such as Status, Access Token, Refresh Token, Expiry time
     * @throws IdPClientException thrown when an error occurred when login
     */
    Map<String, String> login(Map<String, String> properties) throws IdPClientException;

    /**
     * This invalidates the user session.
     *
     * @param properties Map of logout properties.
     * @return properties Map of logout properties such as Redirect_URI
     * @throws IdPClientException thrown when an error occurred when logout
     */
    Map<String, String> logout(Map<String, String> properties) throws IdPClientException;

    /**
     * This will authenticate user based on the token.
     *
     * @param token token used to identify user session.
     * @return username if authentication is success
     * @throws AuthenticationException thrown when authentication fails
     * @throws IdPClientException thrown when an error occurred when authenticating
     */
    String authenticate(String token) throws AuthenticationException, IdPClientException;
}
