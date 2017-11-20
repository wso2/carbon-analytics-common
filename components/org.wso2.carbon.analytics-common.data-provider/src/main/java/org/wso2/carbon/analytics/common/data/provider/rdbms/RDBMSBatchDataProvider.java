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
import org.wso2.carbon.analytics.common.data.provider.spi.AbstractBatchDataProvider;
import org.wso2.carbon.analytics.common.data.provider.spi.ProviderConfig;
import org.wso2.carbon.analytics.common.data.provider.rdbms.config.RDBMSProviderConf;
import org.wso2.carbon.analytics.common.data.provider.endpoint.DataProviderEndPoint;
import org.wso2.carbon.analytics.common.data.provider.rdbms.utils.RDBMSHelper;
import org.wso2.carbon.datasource.core.exception.DataSourceException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * RDBMS data provider instance.
 */

public class RDBMSBatchDataProvider extends AbstractBatchDataProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(RDBMSBatchDataProvider.class);

    @Override
    public void publish(String sessionID, ProviderConfig providerConfig) {
        RDBMSProviderConf rdbmsProviderConf = (RDBMSProviderConf) providerConfig;
        try {
            Connection connection = RDBMSHelper.getConnection(rdbmsProviderConf.getDatasourceName());
            Statement statement = null;
            ResultSet resultSet = null;
            try {
                statement = connection.createStatement();
                resultSet = statement.executeQuery(rdbmsProviderConf.getQuery());
                ArrayList<Object[]> data = new ArrayList<>();
                DataSetMetadata metadata = rdbmsProviderConf.getDataSetMetadata();
                while (resultSet.next()) {
                    int columnCount = metadata.getColumnCount();
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
                connection.commit();
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
    public void purging(ProviderConfig providerConfig) {
    }

    @Override
    public boolean configValidator(ProviderConfig providerConfig) {
        RDBMSProviderConf rdbmsProviderConf = (RDBMSProviderConf) providerConfig;
        return rdbmsProviderConf.getDataSetMetadata().getTypes().length == rdbmsProviderConf.getDataSetMetadata().
                getColumnCount();
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
}
