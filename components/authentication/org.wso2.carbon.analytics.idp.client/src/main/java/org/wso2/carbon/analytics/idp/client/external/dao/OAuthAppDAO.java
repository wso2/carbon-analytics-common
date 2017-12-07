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
import org.wso2.carbon.analytics.idp.client.external.models.OAuthApplicationInfo;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * DAO class for Oauth App data.
 */
public class OAuthAppDAO {
    private static final Logger LOG = LoggerFactory.getLogger(OAuthAppDAO.class);

    private static final String TABLE_NAME = "OAUTHAPP";

    private DataSourceService dataSourceService;
    private DataSource dataSource;
    private String databaseName;

    public OAuthAppDAO(DataSourceService dataSourceService, String databaseName) {
        this.dataSourceService = dataSourceService;
        this.databaseName = databaseName;
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

    public void addOAuthApp(OAuthApplicationInfo oAuthApplicationInfo) throws IdPClientException {
        Connection conn = null;
        PreparedStatement ps = null;
        String query = "INSERT INTO " + TABLE_NAME + "(CLIENTNAME, CLIENTID, CLIENTSECRET) VALUES(?, ?, ?)";

        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(query);
            ps.setString(1, oAuthApplicationInfo.getClientName());
            ps.setString(2, oAuthApplicationInfo.getClientId());
            ps.setString(3, oAuthApplicationInfo.getClientSecret());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing query: " + query);
            }
            ps.executeUpdate();
            conn.commit();
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
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE CLIENTNAME = ?";

        try {
            conn = getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, name);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing query: " + query);
            }
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                return new OAuthApplicationInfo(name, resultSet.getString("CLIENTID"),
                        resultSet.getString("CLIENTSECRET"));
            }
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
        String query = "UPDATE " + TABLE_NAME + " SET CLIENTID= ? , CLIENTSECRET = ? WHERE CLIENTNAME = ?;";

        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(query);
            ps.setString(1, oAuthApplicationInfo.getClientId());
            ps.setString(2, oAuthApplicationInfo.getClientSecret());
            ps.setString(3, oAuthApplicationInfo.getClientName());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing query: " + query);
            }
            ps.executeUpdate();
            conn.commit();
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
}

