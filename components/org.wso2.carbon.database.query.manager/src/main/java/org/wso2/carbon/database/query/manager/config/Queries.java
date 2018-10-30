/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.database.query.manager.config;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.io.Serializable;
import java.util.Map;

/**
 * Bean class for query configuration.
 */
@Configuration(
        description = "Queries configuration parent."
)
public class Queries implements Serializable {
    @Element(
            description = "Queries type",
            required = true
    )
    private String type = "";
    @Element(
            description = "Queries version",
            required = true
    )
    private String version = "";
    @Element(
            description = "Queries mappings",
            required = true
    )
    private Map<String, String> mappings;

    public Queries() {
    }

    public String getType() {
        return this.type;
    }

    public String getVersion() {
        return this.version;
    }

    public Map<String, String> getMappings() {
        return this.mappings;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setMappings(Map<String, String> mappings) {
        this.mappings = mappings;
    }
}
