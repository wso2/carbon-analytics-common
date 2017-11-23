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
package org.wso2.carbon.data.provider.rdbms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.data.provider.api.DataSetMetadata;
import org.wso2.carbon.data.provider.exception.DataProviderException;
import org.wso2.carbon.datasource.core.exception.DataSourceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * RDBMS batch data provider instance.
 */
public class RDBMSBatchDataProvider extends AbstractRDBMSDataProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(RDBMSBatchDataProvider.class);

    public RDBMSBatchDataProvider() throws DataProviderException {
        super();
    }

    @Override
    public void publish(String sessionID) {
        String customQuery = getCustomQuery();
        DataSetMetadata metadata = getMetadata();
        int columnCount = getColumnCount();
        if (customQuery != null) {
            Connection connection;
            try {
                connection = getConnection(getRdbmsProviderConfig().getDatasourceName());
                PreparedStatement statement = null;
                ResultSet resultSet = null;
                try {
                    statement = connection.prepareStatement(customQuery);
                    resultSet = statement.executeQuery();
                    connection.commit();
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
                    publishToEndPoint(data, sessionID);
                } catch (SQLException e) {
                    LOGGER.error("SQL exception occurred " + e.getMessage(), e);
                } finally {
                    cleanupConnection(resultSet, statement, connection);
                }
            } catch (SQLException | DataSourceException e) {
                LOGGER.error("Failed to create a connection to the database " + e.getMessage(), e);
            }
        }
    }
}
