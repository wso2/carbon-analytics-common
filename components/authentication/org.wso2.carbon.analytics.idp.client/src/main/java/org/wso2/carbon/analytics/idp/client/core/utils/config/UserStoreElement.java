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
package org.wso2.carbon.analytics.idp.client.core.utils.config;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.Collections;
import java.util.List;

/**
 * User store Element.
 */
@Configuration(description = "User Store Element")
public class UserStoreElement {

    @Element(description = "Groups", required = true)
    private List<RoleElement> roles = Collections.singletonList(new RoleElement());

    @Element(description = "Users", required = true)
    private List<UserElement> users = Collections.singletonList(new UserElement());

    public List<UserElement> getUsers() {
        return users;
    }

    public List<RoleElement> getRoles() {
        return roles;
    }
}
