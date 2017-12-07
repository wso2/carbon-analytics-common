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
package org.wso2.carbon.analytics.permissions.internal.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.permissions.bean.Permission;
import org.wso2.carbon.analytics.permissions.bean.PermissionConfig;
import org.wso2.carbon.analytics.permissions.bean.PermissionString;
import org.wso2.carbon.analytics.permissions.bean.Role;
import org.wso2.carbon.analytics.permissions.exceptions.PermissionException;
import org.wso2.carbon.analytics.permissions.internal.util.PermissionUtil;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

/**
 * Permission DAO.
 */
public class PermissionsDAO {
    private static final Logger log = LoggerFactory.getLogger(PermissionsDAO.class);

    private DataSourceService dataSourceService;
    private DataSource dataSource;
    private PermissionConfig permissionConfig;

    public PermissionsDAO(DataSourceService dataSourceService, PermissionConfig permissionConfig) {
        this.dataSourceService = dataSourceService;
        this.permissionConfig = permissionConfig;
    }

    /**
     * @param appName
     * @return
     */
    public List<PermissionString> getPermissionStrings(String appName) {
        Connection conn = null;
        PreparedStatement ps = null;
        List<PermissionString> permissionStrings = new ArrayList<>();
        String query = "SELECT PERMISSION_ID, PERMISSION_STRING FROM PERMISSIONS WHERE APP_NAME = ?";
        ResultSet resultSet = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, appName);
            if (log.isDebugEnabled()) {
                log.debug("Executing query: " + query);
            }
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                permissionStrings.add(new PermissionString(resultSet.getString("PERMISSION_ID"),
                        resultSet.getString("PERMISSION_STRING")));
            }
        } catch (SQLException e) {
            throw new PermissionException("Unable to retrieve the PERMISSION_STRINGS: " +
                    permissionConfig.getDatasourceName(), e);
        } finally {
            closeConnection(conn, ps, resultSet);
        }
        return permissionStrings;
    }

    public String getAppName(String permissionID) {
        Connection conn = null;
        PreparedStatement ps = null;
        String query = "SELECT APP_NAME FROM PERMISSIONS WHERE PERMISSION_ID = ?";
        ResultSet resultSet = null;
        String appName = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, permissionID);
            if (log.isDebugEnabled()) {
                log.debug("Executing query: " + query);
            }
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                appName = resultSet.getString("APP_NAME");
            }
        } catch (SQLException e) {
            throw new PermissionException("Unable to retrieve the APP_NAME: " +
                    permissionConfig.getDatasourceName(), e);
        } finally {
            closeConnection(conn, ps, resultSet);
        }
        return appName;
    }

    /**
     * Add permission.
     *
     * @param permission
     */
    public void addPermission(Permission permission) {
        Connection conn = null;
        PreparedStatement ps = null;
        // TODO: Get the query from the QueryManager
        String query = "INSERT INTO PERMISSIONS(PERMISSION_ID, APP_NAME, PERMISSION_STRING) VALUES(?, ?, ?)";
        String uuid = UUID.nameUUIDFromBytes(permission.toString().getBytes(Charset.forName("UTF-8"))).toString();
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(query);
            ps.setString(1, uuid);
            ps.setString(2, permission.getAppName());
            ps.setString(3, permission.getPermissionString());
            if (log.isDebugEnabled()) {
                log.debug("Executing query: " + query);
            }
            ps.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            throw new PermissionException("Unable to add permission. [Query=" + query + "]", e);
        } finally {
            closeConnection(conn, ps, null);
        }
    }

    /**
     * Check Permission already there.
     *
     * @param permission this is the permission object with app name and permission string parameters.
     */
    public boolean isPermissionExists(Permission permission) {
        boolean hasPermission = false;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        String query = "SELECT * FROM ROLE_PERMISSIONS WHERE APP_NAME = ? AND PERMISSION_STRING = ?";

        try {
            conn = getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, permission.getAppName());
            ps.setString(2, permission.getPermissionString());
            if (log.isDebugEnabled()) {
                log.debug("Executing query: " + query);
            }
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                hasPermission = true;
            }
        } catch (SQLException e) {
            throw new PermissionException("Unable to execute check permissions query.", e);
        } finally {
            closeConnection(conn, ps, resultSet);
        }
        return hasPermission;
    }

    /**
     * Delete permission.
     *
     * @param permission
     */
    public void deletePermission(Permission permission) {
        Connection conn = null;
        PreparedStatement ps = null;
        // TODO: Get the query from the QueryManager
        String query = "DELETE FROM PERMISSIONS WHERE APP_NAME = ? AND PERMISSION_STRING = ?";
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(query);
            ps.setString(1, permission.getAppName());
            ps.setString(2, permission.getPermissionString());
            if (log.isDebugEnabled()) {
                log.debug("Executing query: " + query);
            }
            ps.execute();
            conn.commit();
        } catch (SQLException e) {
            throw new PermissionException("Unable to delete permission. [Query=" + query + "]", e);
        } finally {
            closeConnection(conn, ps, null);
        }
    }

    /**
     * Delete permission through API.
     *
     * @param permissionID
     */
    public void deletePermission(String permissionID) {
        Connection conn = null;
        PreparedStatement ps = null;
        String query = "DELETE FROM PERMISSIONS WHERE PERMISSION_ID = ?";

        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(query);
            ps.setString(1, permissionID);
            if (log.isDebugEnabled()) {
                log.debug("Executing query: " + query);
            }
            ps.execute();
            conn.commit();
        } catch (SQLException e) {
            throw new PermissionException("Unable to delete permission. [Query=" + query + "]", e);
        } finally {
            closeConnection(conn, ps, null);
        }
    }

    /**
     * Grant permission to specific role.
     *
     * @param permission
     * @param role
     */
    public void grantPermission(Permission permission, Role role) {
        Connection conn = null;
        PreparedStatement ps = null;
        String uuid = PermissionUtil.createPermissionID(permission);
        // TODO: Get the query from the QueryManager
        String query = "INSERT INTO ROLE_PERMISSIONS(PERMISSION_ID, APP_NAME, PERMISSION_STRING, ROLE_ID) "
                + "VALUES(?, ?, ?, ?)";

        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(query);
            ps.setString(1, uuid);
            ps.setString(2, permission.getAppName());
            ps.setString(3, permission.getPermissionString());
            ps.setString(4, role.getId());
            if (log.isDebugEnabled()) {
                log.debug("Executing query: " + query);
            }
            ps.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            throw new PermissionException("Unable to grant permission. [Query=" + query + "]", e);
        } finally {
            closeConnection(conn, ps, null);
        }
    }

    /**
     * Revoke permission from all the roles.
     *
     * @param permission
     */
    public void revokePermission(Permission permission) {
        Connection conn = null;
        PreparedStatement ps = null;
        // TODO: Get the query from the QueryManager
        String query = "DELETE FROM ROLE_PERMISSIONS WHERE APP_NAME = ? AND PERMISSION_STRING = ?";

        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(query);
            ps.setString(1, permission.getAppName());
            ps.setString(2, permission.getPermissionString());
            if (log.isDebugEnabled()) {
                log.debug("Executing query: " + query);
            }
            ps.execute();
            conn.commit();
        } catch (SQLException e) {
            throw new PermissionException("Unable to revoke permission. [Query=" + query + "]", e);
        } finally {
            closeConnection(conn, ps, null);
        }
    }

    /**
     * Revoke permission for PermissionID.
     *
     * @param permissionID
     */
    public void revokePermission(String permissionID) {
        Connection conn = null;
        PreparedStatement ps = null;
        // TODO: Get the query from the QueryManager
        String query = "DELETE FROM ROLE_PERMISSIONS WHERE PERMISSION_ID = ?";

        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(query);
            ps.setString(1, permissionID);
            if (log.isDebugEnabled()) {
                log.debug("Executing query: " + query);
            }
            ps.execute();
            conn.commit();
        } catch (SQLException e) {
            throw new PermissionException("Unable to revoke permission. [Query=" + query + "]", e);
        } finally {
            closeConnection(conn, ps, null);
        }
    }

    /**
     * Revoke permission from a specific role.
     *
     * @param permission
     * @param role
     */
    public void revokePermission(Permission permission, Role role) {
        Connection conn = null;
        PreparedStatement ps = null;
        // TODO: Get the query from the QueryManager
        String query = "DELETE FROM ROLE_PERMISSIONS WHERE APP_NAME = ? AND PERMISSION_STRING = ? AND ROLE_ID = ?";

        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(query);
            ps.setString(1, permission.getAppName());
            ps.setString(2, permission.getPermissionString());
            ps.setString(3, role.getId());
            if (log.isDebugEnabled()) {
                log.debug("Executing query: " + query);
            }
            ps.execute();
            conn.commit();
        } catch (SQLException e) {
            throw new PermissionException("Unable to revoke permission. [Query=" + query + "]", e);
        } finally {
            closeConnection(conn, ps, null);
        }
    }

    /**
     * Revoke Permission.
     *
     * @param permission
     * @param roleID
     */
    public void revokePermission(Permission permission, String roleID) {
        Connection conn = null;
        PreparedStatement ps = null;
        String query = "DELETE FROM ROLE_PERMISSIONS WHERE APP_NAME = ? PERMISSION_STRING = ? AND ROLE_ID = ?";

        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(query);
            ps.setString(1, permission.getAppName());
            ps.setString(2, permission.getPermissionString());
            ps.setString(3, roleID);
            if (log.isDebugEnabled()) {
                log.debug("Executing query: " + query);
            }
            ps.execute();
            conn.commit();
        } catch (SQLException e) {
            throw new PermissionException("Unable to revoke permission. [Query=" + query + "]", e);
        } finally {
            closeConnection(conn, ps, null);
        }
    }

    /**
     * Check set of roles for specific permission.
     *
     * @param roles
     * @param permission
     * @return
     */
    public boolean hasPermission(List<Role> roles, Permission permission) {
        boolean hasPermission = false;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        String query = "SELECT * FROM ROLE_PERMISSIONS WHERE APP_NAME = ? AND PERMISSION_STRING = ? AND ROLE_ID IN (";

        StringBuilder sb = new StringBuilder(query);
        for (int i = 0; i < roles.size(); i++) {
            sb.append("?,");
        }
        String rolesId = sb.deleteCharAt(sb.length() - 1).append(")").toString();

        try {
            conn = getConnection();
            ps = conn.prepareStatement(rolesId);
            ps.setString(1, permission.getAppName());
            ps.setString(2, permission.getPermissionString());
            for (int i = 0; i < roles.size(); i++) {
                ps.setString(i + 3, roles.get(i).getId());
            }
            if (log.isDebugEnabled()) {
                log.debug("Executing query: " + query);
            }
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                hasPermission = true;
            }
        } catch (SQLException e) {
            throw new PermissionException("Unable to check permissions. [Query=" + query + "]", e);
        } finally {
            closeConnection(conn, ps, resultSet);
        }
        return hasPermission;
    }

    /**
     * @param roles
     * @param permissionID
     * @return
     */
    public boolean hasPermission(List<Role> roles, String permissionID) {
        boolean hasPermission = false;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        String query = "SELECT * FROM ROLE_PERMISSIONS WHERE PERMISSIO_ID = ? AND ROLE_ID IN (";

        StringBuilder sb = new StringBuilder(query);
        for (int i = 0; i < roles.size(); i++) {
            sb.append("?,");
        }
        String rolesId = sb.deleteCharAt(sb.length() - 1).append(")").toString();

        try {
            conn = getConnection();
            ps = conn.prepareStatement(rolesId);
            ps.setString(1, permissionID);
            for (int i = 0; i < roles.size(); i++) {
                ps.setString(i + 3, roles.get(i).getId());
            }
            if (log.isDebugEnabled()) {
                log.debug("Executing query: " + query);
            }
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                hasPermission = true;
            }
        } catch (SQLException e) {
            throw new PermissionException("Unable to check permissions. [Query=" + query + "]", e);
        } finally {
            closeConnection(conn, ps, resultSet);
        }
        return hasPermission;
    }

    /**
     * Get granted roles of a specific permission.
     *
     * @param permission
     * @return
     */
    public List<Role> getGrantedRoles(Permission permission) {
        List<Role> roles = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        String query = "SELECT ROLE_ID FROM ROLE_PERMISSIONS WHERE APP_NAME = ? AND PERMISSION_STRING = ?";

        try {
            conn = getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, permission.getAppName());
            ps.setString(2, permission.getPermissionString());
            if (log.isDebugEnabled()) {
                log.debug("Executing query: " + query);
            }
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                roles.add(new Role(resultSet.getString("ROLE_ID"), ""));
            }
        } catch (SQLException e) {
            throw new PermissionException("Unable to get roles assigned for the permission " + permission +
                    ". [Query=" + query + "]", e);
        } finally {
            closeConnection(conn, ps, resultSet);
        }
        return roles;
    }

    /**
     * @param permissionID
     * @return
     */
    public List<Role> getGrantedRoles(String permissionID) {
        List<Role> roles = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        String query = "SELECT ROLE_ID FROM ROLE_PERMISSIONS WHERE PERMISSION_ID = ?";

        try {
            conn = getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, permissionID);
            if (log.isDebugEnabled()) {
                log.debug("Executing query: " + query);
            }
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                roles.add(new Role(resultSet.getString("ROLE_ID"), ""));
            }
        } catch (SQLException e) {
            throw new PermissionException("Unable to get roles assigned for the permission " + permissionID +
                    ". [Query=" + query + "]", e);
        } finally {
            closeConnection(conn, ps, resultSet);
        }
        return roles;
    }

    /**
     * Get datasource object from carbon data-sources.
     *
     * @return
     * @throws PermissionException
     */
    private DataSource getDataSource() throws PermissionException {
        if (dataSource != null) {
            return dataSource;
        }

        if (dataSourceService == null) {
            throw new PermissionException("Datasource service is null. Cannot retrieve datasource " +
                    permissionConfig.getDatasourceName());
        }

        try {
            dataSource = (DataSource) dataSourceService.getDataSource(permissionConfig.getDatasourceName());
        } catch (DataSourceException e) {
            throw new PermissionException("Unable to retrieve the datasource: " +
                    permissionConfig.getDatasourceName(), e);
        }
        return dataSource;
    }

    /**
     * Get database connection.
     *
     * @return
     * @throws SQLException
     */
    private Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    /**
     * Close connection including prepared statement and result set objects.
     *
     * @param connection
     * @param preparedStatement
     * @param resultSet
     */
    public static void closeConnection(Connection connection, PreparedStatement preparedStatement,
                                       ResultSet resultSet) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.warn("Error closing database connection", e);
            }
        }

        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                log.warn("Error closing prepared statement.", e);
            }
        }

        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                log.warn("Error closing result set.", e);
            }
        }
    }
}
