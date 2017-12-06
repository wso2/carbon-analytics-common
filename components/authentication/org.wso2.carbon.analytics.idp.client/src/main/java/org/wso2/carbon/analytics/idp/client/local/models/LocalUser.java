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
package org.wso2.carbon.analytics.idp.client.local.models;

import org.wso2.carbon.analytics.idp.client.core.models.Role;

import java.util.List;
import java.util.Map;

/**
 * Local user class to hold the user values from user store in configs.
 */
public class LocalUser {
    private final char[] password;
    private String username;
    private Map<String, String> properties;
    private List<Role> roles;

    public LocalUser(String username, char[] password, Map<String, String> properties, List<Role> roles) {
        this.username = username;
        this.password = password.clone();
        this.properties = properties;
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public char[] getPassword() {
        return password.clone();
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public List<Role> getRoles() {
        return roles;
    }

}
