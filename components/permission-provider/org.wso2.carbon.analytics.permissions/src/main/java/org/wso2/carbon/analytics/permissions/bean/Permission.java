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
 * Permission bean class.
 */
public class Permission {
    private String appName;
    private String permissionString;

    /**
     * Default constructor.
     */
    public Permission() {
    }

    /**
     * Constructor with app name and permission string parameters.
     *
     * @param appName
     * @param permissionString
     */
    public Permission(String appName, String permissionString) {
        this.appName = appName;
        this.permissionString = permissionString;
    }

    /**
     * Get app name.
     *
     * @return App name
     */
    public String getAppName() {
        return appName;
    }

    /**
     * Set app name.
     *
     * @param appName App name
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }

    /**
     * Get permission string.
     *
     * @return Permission string
     */
    public String getPermissionString() {
        return permissionString;
    }

    /**
     * Set permission string.
     *
     * @param permissionString Permission string
     */
    public void setPermissionString(String permissionString) {
        this.permissionString = permissionString;
    }

    /**
     * Overrides to strings.
     *
     * @return
     */
    @Override
    public String toString() {
        return "Permission[appName=" + appName + ", permissionString=" + permissionString + "]";
    }
}
