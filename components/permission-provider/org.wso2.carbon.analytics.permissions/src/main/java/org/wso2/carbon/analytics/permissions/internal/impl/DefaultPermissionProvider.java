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

package org.wso2.carbon.analytics.permissions.internal.impl;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.idp.client.core.api.IdPClient;
import org.wso2.carbon.analytics.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.analytics.permissions.PermissionProvider;
import org.wso2.carbon.analytics.permissions.bean.Permission;
import org.wso2.carbon.analytics.permissions.bean.PermissionConfig;
import org.wso2.carbon.analytics.permissions.bean.Role;
import org.wso2.carbon.analytics.permissions.exceptions.PermissionException;
import org.wso2.carbon.analytics.permissions.internal.dao.PermissionsDAO;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.datasource.core.api.DataSourceService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of permission provider.
 */
@Component(
        name = "DefaultPermissionProvider",
        service = PermissionProvider.class,
        immediate = true
)
public class DefaultPermissionProvider implements PermissionProvider {
    private static final Logger log = LoggerFactory.getLogger(DefaultPermissionProvider.class);

    private DataSourceService dataSourceService;
    private IdPClient idPClient;
    private PermissionConfig permissionConfig;

    private PermissionsDAO permissionsDAO;

    private PermissionsDAO getPermissionsDAO() {
        if (permissionsDAO == null) {
            log.debug("Permission DAO is not initialized. Initializing the DAO.");
            permissionsDAO = new PermissionsDAO(dataSourceService, permissionConfig);
        }
        return permissionsDAO;
    }

    /**
     * Add permission.
     *
     * @param permission
     * @throws PermissionException
     */
    @Override
    public void addPermission(Permission permission) throws PermissionException {
        if (log.isDebugEnabled()) {
            log.debug("Add permission " + permission);
        }
        this.getPermissionsDAO().addPermission(permission);
    }

    /**
     * Check permission.
     *
     * @param permission this is the permission object with app name and permission string parameters.
     * @throws PermissionException throws when exception occurred while executing query.
     */
    @Override
    public boolean isPermissionExists(Permission permission) throws PermissionException {
        return this.getPermissionsDAO().isPermissionExists(permission);
    }

    /**
     * Delete permission.
     *
     * @param permission
     * @throws PermissionException
     */
    @Override
    public void deletePermission(Permission permission) throws PermissionException {
        if (log.isDebugEnabled()) {
            log.debug("Delete permission " + permission);
        }
        this.getPermissionsDAO().revokePermission(permission);
        this.getPermissionsDAO().deletePermission(permission);
    }

    /**
     * Grant permission to specific role.
     *
     * @param permission
     * @param role
     * @throws PermissionException
     */
    @Override
    public void grantPermission(Permission permission, Role role) throws PermissionException {
        if (log.isDebugEnabled()) {
            log.debug("Grant permission " + permission + " to " + role);
        }
        this.getPermissionsDAO().grantPermission(permission, role);
    }

    /**
     * Revoke permission from all roles.
     *
     * @param permission
     * @throws PermissionException
     */
    @Override
    public void revokePermission(Permission permission) throws PermissionException {
        if (log.isDebugEnabled()) {
            log.debug("Revoke permission " + permission);
        }
        this.getPermissionsDAO().revokePermission(permission);
    }

    /**
     * Revoke permission from specific role.
     *
     * @param permission
     * @param role
     * @throws PermissionException
     */
    @Override
    public void revokePermission(Permission permission, Role role) throws PermissionException {
        if (log.isDebugEnabled()) {
            log.debug("Revoke permission " + permission + " from " + role);
        }
        this.getPermissionsDAO().revokePermission(permission, role);
    }

    /**
     * Check whether a user has specific permission.
     *
     * @param username
     * @param permission
     * @return
     */
    @Override
    public boolean hasPermission(String username, Permission permission) throws PermissionException {
        if (log.isDebugEnabled()) {
            log.debug("Check permission " + permission);
        }
        List<Role> roles = getRoles(username);
        org.wso2.carbon.analytics.idp.client.core.models.Role adminRole;
        try {
            adminRole = idPClient.getAdminRole();
            if (roles.stream().anyMatch(role -> role.getId().equals(adminRole.getId()))) {
                return true;
            }
        } catch (IdPClientException e) {
            throw new PermissionException("Failed getting admin role.");
        }
        if (roles.size() == 0) {
            log.debug("No roles retrieved for the user.");
            return false;
        }
        log.debug("Retrieved roles for the user.");
        return this.getPermissionsDAO().hasPermission(roles, permission);
    }

    /**
     * Get granted roles for a specific permission.
     *
     * @param permission
     * @return
     * @throws PermissionException
     */
    @Override
    public List<Role> getGrantedRoles(Permission permission) throws PermissionException {
        if (log.isDebugEnabled()) {
            log.debug("Get roles assigned for " + permission);
        }

        // Create a map out of all the roles with role Id as the key. This map will be used to get the role name.
        Map<String, org.wso2.carbon.analytics.idp.client.core.models.Role> roleMap = new HashMap<>();
        try {
            List<org.wso2.carbon.analytics.idp.client.core.models.Role> allRoles = idPClient.getAllRoles();
            for (org.wso2.carbon.analytics.idp.client.core.models.Role role: allRoles) {
                roleMap.put(role.getId(), role);
            }
        } catch (IdPClientException e) {
            throw new PermissionException("Failed getting roles for the permission " + permission + ".");
        }

        List<Role> roles = this.getPermissionsDAO().getGrantedRoles(permission);
        roles.forEach(role -> role.setName(roleMap.get(role.getId()).getDisplayName()));
        return roles;
    }

    /**
     * Get roles by username.
     *
     * @param username
     * @return
     */
    private List<Role> getRoles(String username) {
        if (idPClient == null) {
            throw new RuntimeException("IdP client is not initialized properly. Unable to get user roles.");
        }
        List<Role> roles = new ArrayList<>();
        try {
            List<org.wso2.carbon.analytics.idp.client.core.models.Role> userRoles = idPClient.getUserRoles(username);
            userRoles.forEach(role -> roles.add(new Role(role.getId(), role.getDisplayName())));
        } catch (IdPClientException e) {
            throw new PermissionException("Failed getting roles of the user. Unable to check permissions.");
        }
        return roles;
    }

    /**
     * Register datasource service.
     *
     * @param dataSourceService
     */
    @Reference(
            name = "org.wso2.carbon.datasource.DataSourceService",
            service = DataSourceService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterDataSourceService"
    )
    protected void registerDataSourceService(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }

    /**
     * Unregister datasource service.
     *
     * @param dataSourceService
     */
    protected void unregisterDataSourceService(DataSourceService dataSourceService) {
        this.dataSourceService = null;
    }

    /**
     * Register configuration provider.
     *
     * @param configProvider
     */
    @Reference(
            name = "carbon.config.provider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider) {
        try {
            permissionConfig = configProvider.getConfigurationObject(PermissionConfig.class);
        } catch (ConfigurationException e) {
            throw new PermissionException("Error occurred while fetching permission configuration.", e);
        }
    }

    /**
     * Unregister configuration provider.
     *
     * @param configProvider
     */
    protected void unregisterConfigProvider(ConfigProvider configProvider) {
    }

    /**
     * Register analytics IdP client.
     *
     * @param idPClient
     */
    @Reference(
            name = "org.wso2.carbon.analytics.idp.client.core.api.IdPClient",
            service = IdPClient.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterIdPClient"
    )
    protected void registerIdPClient(IdPClient idPClient) {
        this.idPClient = idPClient;
    }

    /**
     * Unregister analytics IdP client.
     *
     * @param idPClient
     */
    protected void unregisterIdPClient(IdPClient idPClient) {
        this.idPClient = null;
    }
}
