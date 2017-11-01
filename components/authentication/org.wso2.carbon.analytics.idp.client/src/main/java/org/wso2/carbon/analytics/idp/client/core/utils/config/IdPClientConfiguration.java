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

import org.wso2.carbon.analytics.idp.client.core.models.Role;
import org.wso2.carbon.analytics.idp.client.core.models.User;
import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IdP configurations.
 */
@Configuration(namespace = "auth.configs", description = "SP Authorization Configuration Parameters")
public class IdPClientConfiguration {

    private Map<String, String> userProperties = new HashMap<>();
    private Role defaultRole = new Role("1", "admin");
    private User defaultUser = new User("admin", "YWRtaW4=", userProperties,
            Collections.singletonList(defaultRole));

    @Element(description = "Client Type")
    private String type = "local";

    @Element(description = "Client properties")
    private Map<String, String> properties = new HashMap<>();

    @Element(description = "Users")
    private List<User> users = Collections.singletonList(defaultUser);

    @Element(description = "Groups")
    private List<Role> roles = Collections.singletonList(defaultRole);

    public String getType() {
        return type;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Role> getRoles() {
        return roles;
    }
}
