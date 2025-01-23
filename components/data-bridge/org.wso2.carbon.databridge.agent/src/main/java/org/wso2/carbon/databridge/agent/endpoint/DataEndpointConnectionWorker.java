/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.carbon.databridge.agent.endpoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.conf.DataEndpointConfiguration;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointLoginException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * DataEndpoint Connection worker class implementation.
 */

public class DataEndpointConnectionWorker {

    private static Log log = LogFactory.getLog(DataEndpointConnectionWorker.class);

    private DataEndpointConfiguration dataEndpointConfiguration;

    private DataEndpoint dataEndpoint;

    private ScheduledExecutorService loggingControlScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> loggingSchedule;

    private LoggingTask loggingTask = new LoggingTask();

    private AtomicBoolean loggingControlFlag = new AtomicBoolean(true);

    private boolean isLoggingControl = false;


    /**
     * Creates the connection to the data endpoint.
     * Increases the delay between each re connect attempt exponentially in case of connection exceptions.
     *
     * @param reconnect Informs whether the method is invoked from re connection task to increase the
     *                  reconnection delay.
     */
    public void runConnection(boolean reconnect) {
        if (isInitialized()) {
            String receiverURL = dataEndpoint.getDataEndpointConfiguration().getReceiverURL();
            try {
                connect();
                if (dataEndpointConfiguration.isFailOverEndpoint()) {
                    loggingControlFlag.set(true);
                }
                dataEndpoint.activate();
                if (dataEndpoint.delayMap.get(receiverURL) != null) {
                    dataEndpoint.delayMap.replace(receiverURL,
                            dataEndpointConfiguration.getReconnectionInterval() * 1000l);
                }
            } catch (DataEndpointAuthenticationException e) {
                if (reconnect) {
                    if (dataEndpoint.delayMap.get(receiverURL) == null) {
                        dataEndpoint.delayMap.put(receiverURL,
                                dataEndpointConfiguration.getReconnectionInterval() * 1000l);
                    }
                    if (dataEndpointConfiguration.getExpFactor() < 1) {
                        dataEndpointConfiguration.setExpFactor(1);
                    }
                    dataEndpoint.delayMap.replace(receiverURL, dataEndpoint.delayMap.get(receiverURL)
                            * dataEndpointConfiguration.getExpFactor());
                    long maxDelayInMillis = dataEndpointConfiguration.getMaxDelayInSeconds() * 1000l;
                    if (maxDelayInMillis > 0 && dataEndpoint.delayMap.get(receiverURL) > maxDelayInMillis) {
                        dataEndpoint.delayMap.replace(receiverURL, maxDelayInMillis);
                    }
                    dataEndpoint.setReConnectTimestamp(System.currentTimeMillis() +
                            dataEndpoint.delayMap.get(receiverURL));
                }
                if (isLoggingControl) {
                    if (loggingControlFlag.get()) {
                        if (dataEndpointConfiguration.isFailOverEndpoint()) {
                            log.info("Attempt to connect to the endpoint " +
                                dataEndpoint.getDataEndpointConfiguration().getAuthURL() + " failed." + " " +
                                "Next Reconnection attempt to Data endpoint : " +
                                dataEndpoint.getDataEndpointConfiguration().getReceiverURL() +
                                " will be after " + dataEndpoint.delayMap.get(receiverURL) / 1000 + " seconds");
                            log.debug("Error while trying to connect to the endpoint. " + e.getErrorMessage(), e);
                        } else {
                            log.error("Error while trying to connect to the endpoint. " + e.getErrorMessage(), e);
                            if(reconnect) {
                                log.warn("Next Reconnection attempt to Data endpoint : " +
                                    dataEndpoint.getDataEndpointConfiguration().getReceiverURL() +
                                    " will be after " + dataEndpoint.delayMap.get(receiverURL) / 1000 + " seconds");
                            }
                        }
                        loggingControlFlag.set(false);
                    }
                } else {
                    log.error("Error while trying to connect to the endpoint. " + e.getErrorMessage(), e);
                    if(reconnect) {
                        log.warn("Next Reconnection attempt to Data endpoint : " +
                            dataEndpoint.getDataEndpointConfiguration().getReceiverURL() +
                            " will be after " + dataEndpoint.delayMap.get(receiverURL) / 1000 + " seconds");
                    }
                }
                dataEndpoint.deactivate();
            } catch (DataEndpointLoginException e) {
                if (reconnect) {
                    if (dataEndpoint.delayMap.get(receiverURL) == 0) {
                        dataEndpoint.delayMap.replace(receiverURL,
                                dataEndpointConfiguration.getReconnectionInterval() * 1000l);
                    }
                    if (dataEndpointConfiguration.getExpFactor() < 1) {
                        dataEndpointConfiguration.setExpFactor(1);
                    }
                    dataEndpoint.delayMap.replace(receiverURL, dataEndpoint.delayMap.get(receiverURL)
                            * dataEndpointConfiguration.getExpFactor());
                    long maxDelayInMillis = dataEndpointConfiguration.getMaxDelayInSeconds() * 1000l;
                    if (maxDelayInMillis > 0 && dataEndpoint.delayMap.get(receiverURL) > maxDelayInMillis) {
                        dataEndpoint.delayMap.replace(receiverURL, maxDelayInMillis);
                    }
                    dataEndpoint.setReConnectTimestamp(System.currentTimeMillis() +
                            dataEndpoint.delayMap.get(receiverURL));
                    log.warn("Next Reconnection attempt to Data endpoint : " +
                            dataEndpoint.getDataEndpointConfiguration().getReceiverURL() +
                            " will be after " + dataEndpoint.delayMap.get(receiverURL) / 1000 + " seconds");
                }
                log.error("Error while trying to connect to the endpoint. " + e.getErrorMessage(), e);
                dataEndpoint.deactivate();
            }
        } else {
            String errorMsg = "Data endpoint connection worker is not properly initialized ";
            if (dataEndpoint == null)
                errorMsg += ", data Endpoint is not provided ";
            if (dataEndpointConfiguration == null)
                errorMsg += ", data Endpoint configuration is not provided";
            errorMsg += ".";
            log.error(errorMsg);
        }
    }

    DataEndpointConfiguration getDataEndpointConfiguration() {
        return dataEndpointConfiguration;
    }

    /**
     * Initialize the data endpoint connection worker.
     * A connection worker can be instantiated only ONE time.
     *
     * @param dataEndpoint              DataEndpoint instance to handle the connection.
     * @param dataEndpointConfiguration DataEndpointConfiguration to handle the connection.
     * @throws DataEndpointException
     */

    public void initialize(DataEndpoint dataEndpoint, DataEndpointConfiguration dataEndpointConfiguration)
            throws DataEndpointException {
        if (this.dataEndpointConfiguration == null) {
            this.dataEndpointConfiguration = dataEndpointConfiguration;
        } else {
            throw new DataEndpointException("Already data endpoint configuration is set: " +
                    this.dataEndpointConfiguration.toString() + " for the endpoint " +
                    dataEndpointConfiguration.toString());
        }

        if (this.dataEndpoint == null) {
            this.dataEndpoint = dataEndpoint;
        } else {
            throw new DataEndpointException("Already data endpoint is configured for the connection worker");
        }

        if (dataEndpointConfiguration.getLoggingControlIntervalInSeconds() != 0) {
            isLoggingControl = true;
            scheduledLoggingTask();
        } else if (dataEndpointConfiguration.isFailOverEndpoint()) {
            isLoggingControl = true;
        }
    }


    private void connect() throws DataEndpointAuthenticationException, DataEndpointLoginException {
        Object client = null;
        try {
            client = this.dataEndpointConfiguration.getSecuredTransportPool().
                    borrowObject(dataEndpointConfiguration.getAuthKey());
            String sessionId = this.dataEndpoint.
                    login(client, dataEndpointConfiguration.getUsername(),
                            dataEndpointConfiguration.getPassword());
            dataEndpointConfiguration.setSessionId(sessionId);
        } catch (Throwable e) {
            if (e instanceof DataEndpointLoginException) {
                throw new DataEndpointLoginException("Cannot borrow client for "
                        + dataEndpointConfiguration.getAuthURL() + "." , e);
            } else {
                throw new DataEndpointAuthenticationException("Cannot borrow client for " + dataEndpointConfiguration.getAuthURL(), e);
            }
        } finally {
            try {
                this.dataEndpointConfiguration.getSecuredTransportPool().returnObject(dataEndpointConfiguration.getAuthKey(), client);
            } catch (Exception e) {
                this.dataEndpointConfiguration.getSecuredTransportPool().clear(dataEndpointConfiguration.getAuthKey());
            }
        }

    }

    public void disconnect(DataEndpointConfiguration dataPublisherConfiguration) {
        Object client = null;
        try {
            client = this.dataEndpointConfiguration.getSecuredTransportPool().borrowObject(dataPublisherConfiguration.getAuthKey());
            this.dataEndpoint.logout(client, dataPublisherConfiguration.getSessionId());
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot connect to the server at " + dataPublisherConfiguration.getAuthURL() + ", for user: " + dataPublisherConfiguration.getUsername(), e);
            }
            log.warn("Cannot connect to the server at " + dataPublisherConfiguration.getAuthURL() + ", for user: " + dataPublisherConfiguration.getUsername());
        } finally {
            try {
                if (null != loggingControlScheduledExecutorService) {
                    loggingControlScheduledExecutorService.shutdown();
                }
                this.dataEndpointConfiguration.getSecuredTransportPool().returnObject(dataPublisherConfiguration.getAuthKey(), client);
            } catch (Exception e) {
                this.dataEndpointConfiguration.getSecuredTransportPool().clear(dataPublisherConfiguration.getAuthKey());
            }
        }
    }

    private boolean isInitialized() {
        return dataEndpoint != null && dataEndpointConfiguration != null;
    }

    private void scheduledLoggingTask() {
        loggingSchedule = loggingControlScheduledExecutorService.scheduleAtFixedRate(loggingTask, 0,
                dataEndpointConfiguration.getLoggingControlIntervalInSeconds(), TimeUnit.SECONDS );
    }

    private class LoggingTask implements Runnable {
        @Override public void run() {
            loggingControlFlag.set(true);
        }
    }

}
