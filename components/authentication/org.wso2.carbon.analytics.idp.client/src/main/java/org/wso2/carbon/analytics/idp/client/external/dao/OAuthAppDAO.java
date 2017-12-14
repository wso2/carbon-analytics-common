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
package org.wso2.carbon.analytics.idp.client.external.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.analytics.idp.client.external.ExternalIdPClientConstants;
import org.wso2.carbon.analytics.idp.client.external.models.OAuthApplicationInfo;
import org.wso2.carbon.analytics.idp.client.external.util.ExternalIdPClientUtil;
import org.wso2.carbon.database.query.manager.config.Queries;
import org.wso2.carbon.database.query.manager.exception.QueryMappingNotAvailableException;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.secvault.SecretRepository;
import org.wso2.carbon.secvault.exception.SecureVaultException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 * DAO class for Oauth App data.
 */
public class OAuthAppDAO {
    private static final Logger LOG = LoggerFactory.getLogger(OAuthAppDAO.class);

    private SecretRepository secretRepository;
    private DataSourceService dataSourceService;
    private DataSource dataSource;
    private String databaseName;
    private List<Queries> deploymentQueries;
    private Map<String, String> queries;

    public OAuthAppDAO(DataSourceService dataSourceService, String databaseName,
                       List<Queries> deploymentQueries, SecretRepository secretRepository) {
        this.dataSourceService = dataSourceService;
        this.databaseName = databaseName;
        this.deploymentQueries = deploymentQueries;
        this.secretRepository = secretRepository;
    }

    private static void closeConnection(Connection connection, PreparedStatement preparedStatement,
                                        ResultSet resultSet) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOG.warn("Error closing database connection", e);
            }
        }

        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                LOG.warn("Error closing prepared statement.", e);
            }
        }

        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                LOG.warn("Error closing result set.", e);
            }
        }
    }

    public void init() throws IdPClientException {
        Connection conn = null;
        try {
            conn = getConnection();
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            queries = ExternalIdPClientUtil.getQueries(databaseMetaData.getDatabaseProductName(),
                    databaseMetaData.getDatabaseProductVersion(), this.deploymentQueries);
        } catch (SQLException | IOException | QueryMappingNotAvailableException e) {
            throw new IdPClientException("Error initializing connection.", e);
        } finally {
            closeConnection(conn, null, null);
        }
    }

    public void createTable() throws IdPClientException {
        String createTableQuery = getQuery(ExternalIdPClientConstants.CREATE_TABLE_TEMPLATE);

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(createTableQuery);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing query: " + createTableQuery);
            }
            ps.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            throw new IdPClientException("Unable to create table . [Query=" + createTableQuery + "]", e);
        } finally {
            closeConnection(conn, ps, null);
        }
    }

    public void addOAuthApp(OAuthApplicationInfo oAuthApplicationInfo) throws IdPClientException {
        Connection conn = null;
        PreparedStatement ps = null;
        String query = getQuery(ExternalIdPClientConstants.ADD_OAUTH_APP_TEMPLATE);

        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(query);
            ps.setString(1, oAuthApplicationInfo.getClientName());
            ps.setString(2, oAuthApplicationInfo.getClientId());
            byte[] encrypt = this.secretRepository.encrypt(oAuthApplicationInfo.getClientSecret().getBytes("UTF-8"));
            ps.setBytes(3, encrypt);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing query: " + query);
            }
            ps.executeUpdate();
            conn.commit();
        } catch (UnsupportedEncodingException | SecureVaultException e) {
            throw new IdPClientException("Unable to add OauthApp. [Query=" + query + "]. Encryption failed.", e);
        } catch (SQLException e) {
            throw new IdPClientException("Unable to add OauthApp. [Query=" + query + "]", e);
        } finally {
            closeConnection(conn, ps, null);
        }
    }

    public OAuthApplicationInfo getOAuthApp(String name) throws IdPClientException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        String query = getQuery(ExternalIdPClientConstants.RETRIEVE_OAUTH_APP_TEMPLATE);

        try {
            conn = getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, name);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing query: " + query);
            }
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                byte[] clientSecret = resultSet.getBytes("CLIENTSECRET");
                String decrypted = new String(this.secretRepository.decrypt(clientSecret), "UTF-8");
                return new OAuthApplicationInfo(name, resultSet.getString("CLIENTID"), decrypted);
            }
        } catch (UnsupportedEncodingException | SecureVaultException e) {
            throw new IdPClientException("Unable to retrieve OAuthApp. [Query=" + query + "]. Decryption failed.", e);
        } catch (SQLException e) {
            throw new IdPClientException("Unable to retrieve OAuthApp. [Query=" + query + "]", e);
        } finally {
            closeConnection(conn, ps, resultSet);
        }
        return null;
    }

    public void updateOAuthApp(OAuthApplicationInfo oAuthApplicationInfo) throws IdPClientException {
        Connection conn = null;
        PreparedStatement ps = null;
        String query = getQuery(ExternalIdPClientConstants.UPDATE_OAUTH_APP_TEMPLATE);

        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(query);
            ps.setString(1, oAuthApplicationInfo.getClientId());
            ps.setString(2, oAuthApplicationInfo.getClientSecret());
            byte[] encrypt = this.secretRepository.encrypt(oAuthApplicationInfo.getClientSecret().getBytes("UTF-8"));
            ps.setBytes(3, encrypt);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing query: " + query);
            }
            ps.executeUpdate();
            conn.commit();
        } catch (UnsupportedEncodingException | SecureVaultException e) {
            throw new IdPClientException("Unable to update OauthApp. [Query=" + query + "]. Encryption failed", e);
        } catch (SQLException e) {
            throw new IdPClientException("Unable to update OauthApp. [Query=" + query + "]", e);
        } finally {
            closeConnection(conn, ps, null);
        }
    }

    private DataSource getDataSource() throws IdPClientException {
        if (dataSource != null) {
            return dataSource;
        }

        if (dataSourceService == null) {
            throw new IdPClientException("Datasource service is null. Cannot retrieve datasource '"
                    + this.databaseName + "'.");
        }

        try {
            dataSource = (DataSource) dataSourceService.getDataSource(this.databaseName);
        } catch (DataSourceException e) {
            throw new IdPClientException("Unable to retrieve the datasource: '" + this.databaseName + "'.", e);
        }
        return dataSource;
    }

    private Connection getConnection() throws SQLException, IdPClientException {
        return getDataSource().getConnection();
    }

    private String getQuery(String key) {
        if (!this.queries.containsKey(key)) {
            throw new RuntimeException("Unable to find the configuration entry for the key: " + key);
        }
        return this.queries.get(key);
    }
}

