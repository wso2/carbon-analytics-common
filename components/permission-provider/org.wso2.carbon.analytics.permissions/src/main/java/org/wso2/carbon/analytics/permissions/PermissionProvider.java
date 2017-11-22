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

package org.wso2.carbon.analytics.permissions;

import org.wso2.carbon.analytics.permissions.bean.Permission;
import org.wso2.carbon.analytics.permissions.bean.Role;
import org.wso2.carbon.analytics.permissions.exceptions.PermissionException;

import java.util.List;

/**
 * Permission provider interface.
 */
public interface PermissionProvider {
    /**
     * Add permission.
     *
     * @param permission
     * @throws PermissionException
     */
    void addPermission(Permission permission) throws PermissionException;

    /**
     * Delete permission.
     *
     * @param permission
     * @throws PermissionException
     */
    void deletePermission(Permission permission) throws PermissionException;

    /**
     * Grant permission to specific role.
     *
     * @param permission
     * @param role
     * @throws PermissionException
     */
    void grantPermission(Permission permission, Role role) throws PermissionException;

    /**
     * Revoke permission for all roles.
     *
     * @param permission
     * @throws PermissionException
     */
    void revokePermission(Permission permission) throws PermissionException;

    /**
     * Revoke permission from specific role.
     *
     * @param permission
     * @param role
     * @throws PermissionException
     */
    void revokePermission(Permission permission, Role role) throws PermissionException;

    /**
     * Check whether a particular user has specific permission.
     *
     * @param username
     * @param permission
     * @return
     * @throws PermissionException
     */
    boolean hasPermission(String username, Permission permission) throws PermissionException;

    /**
     * Get list of roles which has a permission granted.
     *
     * @param permission
     * @return
     * @throws PermissionException
     */
    List<Role> getGrantedRoles(Permission permission) throws PermissionException;
}
