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

import java.util.HashMap;
import java.util.Map;

/**
 * User Details Child Element.
 */
@Configuration(description = "User Child Element configuration")
public class UserChildElement {

    @Element(description = "Username", required = true)
    private String username = "admin";

    @Element(description = "Encrypted Password", required = true)
    private String password = "YWRtaW4=";

    @Element(description = "Properties associated with the user")
    private Map<String, String> properties = new HashMap<>();

    @Element(description = "List of comma separated role ids", required = true)
    private String roles = "1";

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String getRoles() {
        return roles;
    }
}
