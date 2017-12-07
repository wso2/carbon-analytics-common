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

import java.util.ArrayList;
import java.util.List;

/**
 * REST API Authentication config Element.
 */
@Configuration(description = "REST API Auth configurations")
public class RESTAPIConfigurationElement {

    @Element(description = "Enable authentication for REST API", required = true)
    private String authEnable = "true";

    @Element(description = "APIs to be excluded when auth is enabled", required = true)
    private List<String> exclude = new ArrayList<>();

    public String getAuthEnable() {
        return authEnable;
    }

    public List<String> getExclude() {
        return exclude;
    }
}
