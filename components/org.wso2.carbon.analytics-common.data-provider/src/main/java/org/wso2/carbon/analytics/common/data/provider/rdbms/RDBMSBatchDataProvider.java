/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package org.wso2.carbon.analytics.common.data.provider.rdbms;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.common.data.provider.api.DataModel;
import org.wso2.carbon.analytics.common.data.provider.api.DataSetMetadata;
import org.wso2.carbon.analytics.common.data.provider.rdbms.bean.RDBMSDataProviderConfBean;
import org.wso2.carbon.analytics.common.data.provider.utils.AbstractBatchDataProvider;
import org.wso2.carbon.analytics.common.data.provider.spi.ProviderConfig;
import org.wso2.carbon.analytics.common.data.provider.rdbms.config.RDBMSDataProviderConf;
import org.wso2.carbon.analytics.common.data.provider.endpoint.DataProviderEndPoint;
import org.wso2.carbon.analytics.common.data.provider.utils.DataProviderValueHolder;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.datasource.core.exception.DataSourceException;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * RDBMS data provider instance.
 */

public class RDBMSBatchDataProvider extends AbstractBatchDataProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(RDBMSBatchDataProvider.class);
    private RDBMSDataProviderConf rdbmsProviderConf;
    private RDBMSDataProviderConfBean rdbmsDataProviderConfBean;

    public RDBMSBatchDataProvider() throws ConfigurationException {
        this.rdbmsDataProviderConfBean = DataProviderValueHolder.getConfigProvider().
                getConfigurationObject(RDBMSDataProviderConfBean.class);
    }

    @Override
    public void publish(String sessionID) {
        try {
            Connection connection = getConnection(rdbmsProviderConf.getDatasourceName());
            Statement statement = null;
            ResultSet resultSet = null;
            try {
                statement = connection.createStatement();
                resultSet = statement.executeQuery(rdbmsProviderConf.getQuery());
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                DataSetMetadata metadata = new DataSetMetadata(resultSetMetaData.getColumnCount());
                int columnCount = metadata.getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    metadata.put(i, resultSetMetaData.getColumnName(i + 1),
                            getMetadataTypes(resultSetMetaData.getColumnTypeName(i + 1)));
                }
                ArrayList<Object[]> data = new ArrayList<>();
                while (resultSet.next()) {
                    Object[] rowData = new Object[columnCount];
                    for (int i = 0; i < columnCount; i++) {
                        if (metadata.getTypes()[i].equals(DataSetMetadata.Types.LINEAR)) {
                            rowData[i] = resultSet.getDouble(i + 1);
                        } else if (metadata.getTypes()[i].equals(DataSetMetadata.Types.ORDINAL)) {
                            rowData[i] = resultSet.getString(i + 1);
                        } else if (metadata.getTypes()[i].equals(DataSetMetadata.Types.TIME)) {
                            rowData[i] = resultSet.getDate(i + 1);
                        } else {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Meta Data type not defined, added value of the given column as a " +
                                        "java object.");
                            }
                            rowData[i] = resultSet.getObject(i + 1);
                        }
                    }
                    data.add(rowData);
                }
                if (data.size() > 0) {
                    DataModel dataModel = new DataModel(metadata, data.toArray(new Object[0][0]), -1);
                    try {
                        DataProviderEndPoint.sendText(new Gson().toJson(dataModel), sessionID);
                    } catch (IOException e) {
                        LOGGER.error("Failed to deliver message to client " + e.getMessage(), e);
                    }
                }
            } catch (SQLException e) {
                LOGGER.error("SQL exception occurred " + e.getMessage(), e);
            } finally {
                cleanupConnection(resultSet, statement, connection);
            }
        } catch (SQLException | DataSourceException e) {
            LOGGER.error("Failed to create a connection to the database " + e.getMessage(), e);
        }
    }

    @Override
    public void purging() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection(rdbmsProviderConf.getDatasourceName());
            String databaseName = connection.getMetaData().getDatabaseProductName();
            String databaseVersion = connection.getMetaData().getDatabaseProductName();
            String purgingQuery = null;
            if (rdbmsDataProviderConfBean.getPurgingSQLQueryMap().containsKey(databaseName + "_" + databaseVersion)) {
                purgingQuery = rdbmsDataProviderConfBean.getPurgingSQLQueryMap().get(databaseName + "_" +
                        databaseVersion);
            } else if (rdbmsDataProviderConfBean.getPurgingSQLQueryMap().containsKey(databaseName + "_default")) {
                purgingQuery = rdbmsDataProviderConfBean.getPurgingSQLQueryMap().get(databaseName + "_default");
            } else {
                LOGGER.error("Failed to log purging template query for database: " + databaseName + " version: "
                        + databaseVersion + ".");
            }
            if (purgingQuery != null) {
                statement = connection.prepareStatement(purgingQuery);
                resultSet = statement.executeQuery();
                connection.commit();
            }
        } catch (SQLException | DataSourceException e) {
            LOGGER.error("Failed to create a connection to the database " + e.getMessage(), e);
        } finally {
            cleanupConnection(resultSet, statement, connection);
        }
    }

    @Override
    public void setProviderConfig(ProviderConfig providerConfig) {
        this.rdbmsProviderConf = (RDBMSDataProviderConf) providerConfig;
    }

    @Override
    public boolean configValidator(ProviderConfig providerConfig) {
        return true;
    }

    private static void cleanupConnection(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                LOGGER.error("Error on closing resultSet " + e.getMessage(), e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                LOGGER.error("Error on closing statement " + e.getMessage(), e);
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                LOGGER.error("Error on closing connection " + e.getMessage(), e);
            }
        }
    }

    /**
     * Get metadata type(linear,ordinal,time) for the given data type of the data base.
     *
     * @param dataType String data type name provided by the result set metadata
     * @return String metadata type
     */
    public DataSetMetadata.Types getMetadataTypes(String dataType) {
        if (Arrays.asList(rdbmsDataProviderConfBean.getLinearTypes()).contains(dataType)) {
            return DataSetMetadata.Types.LINEAR;
        } else if (Arrays.asList(rdbmsDataProviderConfBean.getOrdinalTypes()).contains(dataType)) {
            return DataSetMetadata.Types.ORDINAL;
        } else if (Arrays.asList(rdbmsDataProviderConfBean.getTimeTypes()).contains(dataType)) {
            return DataSetMetadata.Types.TIME;
        } else {
            return DataSetMetadata.Types.OBJECT;
        }
    }

    /**
     * get connection object for the instance.
     *
     * @return java.sql.Connection object for the dataProvider Configuration
     */
    public static Connection getConnection(String dataSourceName)
            throws SQLException, DataSourceException {
        return ((DataSource) DataProviderValueHolder.getDataSourceService().
                getDataSource(dataSourceName)).getConnection();
    }
}
