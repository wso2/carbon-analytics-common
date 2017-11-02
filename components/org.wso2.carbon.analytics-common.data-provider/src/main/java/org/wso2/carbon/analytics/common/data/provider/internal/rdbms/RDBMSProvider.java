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
package org.wso2.carbon.analytics.common.data.provider.internal.rdbms;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.common.data.provider.api.DataModel;
import org.wso2.carbon.analytics.common.data.provider.api.DataSetMetadata;
import org.wso2.carbon.analytics.common.data.provider.internal.DataProviderEndPoint;
import org.wso2.carbon.analytics.common.data.provider.spi.DataProvider;
import org.wso2.carbon.datasource.core.exception.DataSourceException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * RDBMS data provider instance.
 */

public class RDBMSProvider implements DataProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(RDBMSProvider.class);

    private final RDBMSProviderConf providerConf;
    private Connection connection;
    private String sessionID;
    private int lastRow;
    private final ScheduledExecutorService executorService;
    private DataModel dataModel;
    private int columnCount;

    /**
     * initialize the instance with the providerConfiguration.
     *
     * @param providerConf RDBMS provider configuration object
     */
    public RDBMSProvider(RDBMSProviderConf providerConf) {
        this.providerConf = providerConf;
        executorService = Executors.newSingleThreadScheduledExecutor();
    }


    @Override
    public DataProvider init(String sessionId) {
        this.sessionID = sessionId;
        return this;
    }

    @Override
    public DataProvider start() {
        try {

            connection = RDBMSHelper.getConnection(providerConf.getDatabaseName());
        } catch (SQLException e) {
            LOGGER.error("Failed to create a connection to the database", e);
        } catch (DataSourceException e) {
            LOGGER.error("Failed to create a connection to the database", e);
        }
        executorService.scheduleAtFixedRate(() -> {

            Statement statement = null;
            ResultSet resultSet = null;
            try {
                statement = connection.createStatement();
                resultSet = statement.executeQuery(providerConf.getQuery());
                DataSetMetadata metadata = null;
                if (dataModel == null) {
                    ResultSetMetaData rsmd = resultSet.getMetaData();
                    metadata = new DataSetMetadata(rsmd.getColumnCount());

                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                        metadata.put(i - 1, rsmd.getColumnName(i),
                                RDBMSHelper.getMetadataTypes(providerConf.getUrl().split(":")[1], rsmd
                                        .getColumnTypeName
                                                (i)));
                        columnCount++;
                    }


                }

                resultSet.last();
                int currentLast = resultSet.getRow();
                resultSet.beforeFirst();
                Object[][] data = new Object[currentLast - lastRow][columnCount];
                if (lastRow < currentLast) {

                    while (resultSet.next()) {
                        if (resultSet.getRow() <= lastRow) {
                            continue;
                        }

                        for (int i = 1; i <= columnCount; i++) {
                            if (metadata.getTypes()[i - 1].equalsIgnoreCase("linear")) {
                                data[resultSet.getRow() - lastRow - 1][i - 1] = resultSet.getDouble(i);
                            } else if (metadata.getTypes()[i - 1].equalsIgnoreCase("ordinal")) {
                                data[resultSet.getRow() - lastRow - 1][i - 1] = resultSet.getString(i);
                            } else {
                                data[resultSet.getRow() - lastRow - 1][i - 1] = resultSet.getDate(i);
                            }
                        }


                    }


                    lastRow = currentLast;
                    dataModel = new DataModel(metadata, data, lastRow);
                    try {
                        DataProviderEndPoint.sendText(new Gson().toJson(dataModel), sessionID);
                    } catch (IOException e) {
                        LOGGER.error("Failed to deliver message to client", e);
                    }


                }


            } catch (SQLException e) {
                LOGGER.error("SQL exception occurred", e);

            } finally {
                if (resultSet != null) {
                    try {
                        resultSet.close();
                    } catch (SQLException e) {
                        LOGGER.error("Error on closing resultset", e);
                    }
                }


                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        LOGGER.error("Error on closing statement", e);
                    }
                }
            }
        }, 0, providerConf.getPollingInterval(), TimeUnit.SECONDS);


        return this;
    }

    @Override
    public void stop() {
        executorService.shutdown();
        try {
            connection.close();
        } catch (SQLException e) {
            LOGGER.error("Failed to close the Database Connection ", e);
        }
    }
}
