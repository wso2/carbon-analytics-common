/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.analytics.permissions.bean;

/**
 * Role bean class.
 */
public class Role {
    private String id;
    private String name;

    /**
     * Default constructor.
     */
    public Role() {
    }

    /**
     * Constructor with role Id, name parameters.
     *
     * @param id
     * @param name
     */
    public Role(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Get role Id.
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Set role Id.
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get role name.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Set role name.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Overrides to string.
     *
     * @return
     */
    @Override
    public String toString() {
        return "Role[id=" + id + ", name=" + name + "]";
    }
}
