/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.analytics.permissions.internal.dao;

import org.wso2.carbon.analytics.permissions.bean.PermissionConfig;
import org.wso2.carbon.database.query.manager.QueryProvider;
import org.wso2.carbon.database.query.manager.config.Queries;
import org.wso2.carbon.database.query.manager.exception.QueryMappingNotAvailableException;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Provides SQl queries for the common permission model.
 */
public class QueryManager {
    public static final String CREATE_PERMISSION_TABLE_QUERY = "create_permission_table";
    public static final String CREATE_ROLE_PERMISSIONS_TABLE_QUERY = "create_role_permissions_table";
    public static final String TABLE_CHECK_QUERY = "table_check";
    public static final String GET_PERMISSION_QUERY = "get_permission";
    public static final String GET_APPNAME_QUERY = "get_appname";
    public static final String ADD_PERMISSION_QUERY = "add_permission";
    public static final String CHECK_PERMISSION_EXISTS_QUERY = "is_permission_exists";
    public static final String DELETE_PERMISSION_QUERY = "delete_permission";
    public static final String DELETE_PERMISSION_BY_ID_QUERY = "delete_permission_by_id";
    public static final String GRANT_PERMISSION_QUERY = "grant_permission";
    public static final String REVOKE_PERMISSION_QUERY = "revoke_permission";
    public static final String REVOKE_PERMISSION_BY_PERMISSION_ID_QUERY = "revoke_permission_by_permission_id";
    public static final String REVOKE_PERMISSION_BY_ROLE_QUERY = "revoke_permission_by_role";
    public static final String REVOKE_PERMISSION_BY_ROLE_ID_QUERY = "revoke_permission_by_role_id";
    public static final String HAS_PERMISSION_QUERY = "has_permission";
    public static final String HAS_PERMISSION_BY_PERMISSION_ID_QUERY = "has_permission_by_permission_id";
    public static final String GET_GRANTED_ROLES_QUERY = "get_granted_roles";
    public static final String GET_GRANTED_ROLES_BY_PERMISSION_ID_QUERY = "get_granted_roles_by_permission_id";
    public static final String PERMISSIONS_TABLE = "PERMISSIONS";
    public static final String ROLE_PERMISSIONS_TABLE = "ROLE_PERMISSIONS";
    public static final String TABLE_NAME_PLACEHOLDER = "{{TABLE_NAME}}";

    private static final String DB2_DB_TYPE = "DB2";
    private static final String FILE_SQL_QUERIES = "queries.yaml";

    private final List<Queries> componentQueries;
    private final List<Queries> deploymentQueries;

    private final Map<String, Map<String, String>> queryMap = new HashMap<>();

    public QueryManager(PermissionConfig permissionConfig) {
        this.componentQueries = readConfigs();
        this.deploymentQueries = permissionConfig.getQueries();
    }

    private List<Queries> readConfigs() {
        List queries;
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(FILE_SQL_QUERIES)) {
            if (inputStream == null) {
                throw new RuntimeException("Cannot find file '" + FILE_SQL_QUERIES + "' in class path.");
            }
            Yaml yaml = new Yaml(new OsgiClassLoaderConstructor(new LoaderOptions()));
            yaml.setBeanAccess(BeanAccess.FIELD);
            queries = yaml.loadAs(inputStream, PermissionConfig.class).getQueries();
        } catch (IOException e) {
            throw new RuntimeException("Cannot read YAML file '" + FILE_SQL_QUERIES + "' from classpath.", e);
        } catch (Exception e) {
            throw new RuntimeException("YAML file '" + FILE_SQL_QUERIES + "' is invalid.", e);
        }
        return queries;
    }

    /**
     * Get SQL query.
     *
     * @param connection SQLConnection object.
     * @param key        Query key
     * @return SQL query
     * @throws SQLException
     */
    public String getQuery(Connection connection, String key) throws SQLException {
        // DB2 product name changes with the specific versions(For an example DB2/LINUXX8664, DB2/NT). Hence, checks
        // whether the product name contains "DB2".
        if (connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ENGLISH)
                .contains(DB2_DB_TYPE.toLowerCase(Locale.ENGLISH))) {
            return getQuery(DB2_DB_TYPE, connection.getMetaData().getDatabaseProductVersion(), key);
        }
        return getQuery(connection.getMetaData().getDatabaseProductName(),
                connection.getMetaData().getDatabaseProductVersion(), key);
    }

    /**
     * Get SQL query for specific database type and version.
     *
     * @param dbType    Database type
     * @param dbVersion Database version
     * @param key       Query key
     * @return SQL query
     * @throws SQLException
     */
    public String getQuery(String dbType, String dbVersion, String key) throws SQLException {
        String dbKey = dbType + "_" + dbVersion;

        Map<String, String> queries = queryMap.get(dbKey);
        if (queries == null) {
            try {
                queries = QueryProvider.mergeMapping(dbType, dbVersion, componentQueries, deploymentQueries);
            } catch (QueryMappingNotAvailableException e) {
                throw new SQLException("Cannot find database queries for " + dbType + " " + dbVersion + ".", e);
            }
            queryMap.put(dbKey, queries);
        }

        String query = queries.get(key);
        if (query == null) {
            throw new SQLException("Cannot find query for " + key + " in " + dbType + " " + dbVersion + ".");
        }
        return queryMap.get(dbKey).get(key);
    }

    /**
     * YAML constructor that loads classes in an OSGi environment.
     *
     * @since 4.0.0
     */
    private static class OsgiClassLoaderConstructor extends Constructor {
        public OsgiClassLoaderConstructor(LoaderOptions loadingConfig) {
            super(loadingConfig);
        }

        @Override
        protected Class<?> getClassForName(String name) throws ClassNotFoundException {
            return Class.forName(name, true, this.getClass().getClassLoader());
        }
    }
}
