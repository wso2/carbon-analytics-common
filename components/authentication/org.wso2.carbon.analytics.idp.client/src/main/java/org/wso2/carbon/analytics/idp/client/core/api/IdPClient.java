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
 * IdPClient interface.
 */
public interface IdPClient {
    List<Role> getAllRoles() throws IdPClientException;

    User getUser(String name) throws IdPClientException;

    List<Role> getUserRoles(String name) throws IdPClientException;

    Map<String, String> login(Map<String, String> properties) throws IdPClientException;

    void logout(Map<String, String> properties) throws IdPClientException;

    boolean authenticate(String token) throws AuthenticationException, IdPClientException;
}
