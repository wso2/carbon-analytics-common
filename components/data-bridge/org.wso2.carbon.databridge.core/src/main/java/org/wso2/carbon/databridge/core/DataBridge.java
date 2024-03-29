/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.databridge.core;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.SessionTimeoutException;
import org.wso2.carbon.databridge.commons.exception.UndefinedEventTypeException;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.databridge.core.conf.DataBridgeConfiguration;
import org.wso2.carbon.databridge.core.conf.DatabridgeConfigurationFileResolver;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.databridge.core.definitionstore.StreamAddRemoveListener;
import org.wso2.carbon.databridge.core.definitionstore.StreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.DataBridgeConfigurationException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.core.internal.EventDispatcher;
import org.wso2.carbon.databridge.core.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.databridge.core.internal.authentication.Authenticator;
import org.wso2.carbon.databridge.core.internal.utils.DataBridgeConstants;
import org.wso2.carbon.databridge.core.utils.AgentSession;
import org.wso2.carbon.utils.Utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * this class represents as the interface between the agent server and agent
 * server implementations.
 */
public class DataBridge implements DataBridgeSubscriberService, DataBridgeReceiverService {

    private static final Logger log = LogManager.getLogger(DataBridge.class);
    private StreamDefinitionStore streamDefinitionStore;
    private EventDispatcher eventDispatcher;
    private Authenticator authenticator;
    private AuthenticationHandler authenticatorHandler;
    private List<StreamAddRemoveListener> streamAddRemoveListenerList = new ArrayList<>();
    private DataBridgeConfiguration dataBridgeConfiguration;
    private AtomicInteger eventsReceived;
    private AtomicInteger totalEventCounter;
    private long startTime;
    private boolean isProfileReceiver;
    private int cutoff = 100000;

    public DataBridge(AuthenticationHandler authenticationHandler,
                      AbstractStreamDefinitionStore streamDefinitionStore,
                      DataBridgeConfiguration dataBridgeConfiguration) {

        this.setInitialConfig(dataBridgeConfiguration);
        this.eventDispatcher = new EventDispatcher(streamDefinitionStore, dataBridgeConfiguration);
        this.streamDefinitionStore = streamDefinitionStore;
        authenticatorHandler = authenticationHandler;
        authenticator = new Authenticator(authenticationHandler, dataBridgeConfiguration);
        String profileReceiver = System.getProperty("profileReceiver");
        String receiverStatsCutoff = System.getProperty("receiverStatsCutoff");

        if (profileReceiver != null && profileReceiver.equalsIgnoreCase("true")) {
            isProfileReceiver = true;
            eventsReceived = new AtomicInteger();
            totalEventCounter = new AtomicInteger();
            startTime = 0;

            if (receiverStatsCutoff != null && StringUtils.isNumeric(receiverStatsCutoff)) {
                this.cutoff = Integer.parseInt(receiverStatsCutoff);
            }
        }
    }

    public DataBridge(AuthenticationHandler authenticationHandler,
                      AbstractStreamDefinitionStore streamDefinitionStore,
                      String dataBridgeConfigPath) {
        DataBridgeConfiguration dataBridgeConfiguration = null;
        try {
            dataBridgeConfiguration = createDataBridgeConfiguration(dataBridgeConfigPath);
        } catch (DataBridgeConfigurationException e) {
            log.error("Error while loading the data bridge configuration file : " + dataBridgeConfigPath, e);
        }
        this.setInitialConfig(dataBridgeConfiguration);
        this.eventDispatcher = new EventDispatcher(streamDefinitionStore, dataBridgeConfiguration);
        this.streamDefinitionStore = streamDefinitionStore;
        authenticatorHandler = authenticationHandler;
        authenticator = new Authenticator(authenticationHandler, dataBridgeConfiguration);
        String profileReceiver = System.getProperty("profileReceiver");
        String receiverStatsCutoff = System.getProperty("receiverStatsCutoff");

        if (profileReceiver != null && profileReceiver.equalsIgnoreCase("true")) {
            isProfileReceiver = true;
            eventsReceived = new AtomicInteger();
            totalEventCounter = new AtomicInteger();
            startTime = 0;

            if (receiverStatsCutoff != null && StringUtils.isNumeric(receiverStatsCutoff)) {
                this.cutoff = Integer.parseInt(receiverStatsCutoff);
            }
        }
    }

    public String defineStream(String sessionId, String streamDefinition)
            throws

            DifferentStreamDefinitionAlreadyDefinedException,
            MalformedStreamDefinitionException, SessionTimeoutException {
        AgentSession agentSession = authenticator.getSession(sessionId);
        if (agentSession.getCredentials() == null) {
            if (log.isDebugEnabled()) {
                log.debug("session " + sessionId + " expired ");
            }
            throw new SessionTimeoutException(sessionId + " expired");
        }
        try {
            authenticatorHandler.initContext(agentSession);
            String streamId = eventDispatcher.defineStream(streamDefinition, agentSession);
            if (streamId != null) {
                for (StreamAddRemoveListener streamAddRemoveListener : streamAddRemoveListenerList) {
                    streamAddRemoveListener.streamAdded(streamId);
                }
            }
            return streamId;
        } catch (MalformedStreamDefinitionException e) {
            throw new MalformedStreamDefinitionException(e.getErrorMessage(), e);
        } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
            throw new DifferentStreamDefinitionAlreadyDefinedException(e.getErrorMessage(), e);
        } catch (StreamDefinitionStoreException e) {
            throw new MalformedStreamDefinitionException(e.getErrorMessage(), e);
        } finally {
            authenticatorHandler.destroyContext(agentSession);
        }
    }

    public String defineStream(String sessionId, String streamDefinition, String indexDefinition)
            throws

            DifferentStreamDefinitionAlreadyDefinedException,
            MalformedStreamDefinitionException, SessionTimeoutException {
        AgentSession agentSession = authenticator.getSession(sessionId);
        if (agentSession.getCredentials() == null) {
            if (log.isDebugEnabled()) {
                log.debug("session " + sessionId + " expired ");
            }
            throw new SessionTimeoutException(sessionId + " expired");
        }
        try {
            authenticatorHandler.initContext(agentSession);
            String streamId = eventDispatcher.defineStream(streamDefinition, agentSession, indexDefinition);
            if (streamId != null) {
                for (StreamAddRemoveListener streamAddRemoveListener : streamAddRemoveListenerList) {
                    streamAddRemoveListener.streamAdded(streamId);
                }
            }
            return streamId;
        } catch (MalformedStreamDefinitionException e) {
            throw new MalformedStreamDefinitionException(e.getErrorMessage(), e);
        } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
            throw new DifferentStreamDefinitionAlreadyDefinedException(e.getErrorMessage(), e);
        } catch (StreamDefinitionStoreException e) {
            throw new MalformedStreamDefinitionException(e.getErrorMessage(), e);
        } finally {
            authenticatorHandler.destroyContext(agentSession);
        }
    }

    public String findStreamId(String sessionId, String streamName, String streamVersion)
            throws SessionTimeoutException {
        AgentSession agentSession = authenticator.getSession(sessionId);
        if (agentSession.getCredentials() == null) {
            if (log.isDebugEnabled()) {
                log.debug("session " + sessionId + " expired ");
            }
            throw new SessionTimeoutException(sessionId + " expired");
        }
        try {
            authenticatorHandler.initContext(agentSession);
            return eventDispatcher.findStreamId(streamName,
                    streamVersion, agentSession);
        } catch (StreamDefinitionStoreException e) {
            log.warn("Cannot find streamId for " + streamName + " " + streamVersion, e);
            return null;
        } finally {
            authenticatorHandler.destroyContext(agentSession);
        }

    }

    public boolean deleteStream(String sessionId, String streamId)
            throws SessionTimeoutException {
        AgentSession agentSession = authenticator.getSession(sessionId);
        if (agentSession.getCredentials() == null) {
            if (log.isDebugEnabled()) {
                log.debug("session " + sessionId + " expired ");
            }
            throw new SessionTimeoutException(sessionId + " expired");
        }
        boolean status;

        try {
            authenticatorHandler.initContext(agentSession);
            status = eventDispatcher.deleteStream(DataBridgeCommonsUtils.getStreamNameFromStreamId(streamId),
                    DataBridgeCommonsUtils.getStreamVersionFromStreamId(streamId), agentSession);
            if (status) {
                for (StreamAddRemoveListener streamAddRemoveListener : streamAddRemoveListenerList) {
                    streamAddRemoveListener.streamRemoved(streamId);
                }
            }
        } finally {
            authenticatorHandler.destroyContext(agentSession);

        }
        return status;
    }

    public boolean deleteStream(String sessionId, String streamName, String streamVersion)
            throws SessionTimeoutException {
        return deleteStream(sessionId, DataBridgeCommonsUtils.generateStreamId(streamName, streamVersion));
    }

    public void publish(Object eventBundle, String sessionId, EventConverter eventConverter)
            throws UndefinedEventTypeException, SessionTimeoutException {
        startTimeMeasurement();
        AgentSession agentSession = authenticator.getSession(sessionId);
        if (agentSession.getCredentials() == null) {
            if (log.isDebugEnabled()) {
                log.debug("session " + sessionId + " expired ");
            }
            throw new SessionTimeoutException(sessionId + " expired");
        }
        try {
            authenticatorHandler.initContext(agentSession);
            eventDispatcher.publish(eventBundle, agentSession, eventConverter);
            endTimeMeasurement(eventConverter.getNumberOfEvents(eventBundle));
        } finally {
            authenticatorHandler.destroyContext(agentSession);
        }
    }

    public Boolean isQueueEmpty() {
        return eventDispatcher.isQueueEmpty();
    }

    private void endTimeMeasurement(int eventsNum) {
        if (isProfileReceiver) {
            eventsReceived.addAndGet(eventsNum);
            if (eventsReceived.get() > cutoff) {
                synchronized (this) {
                    if (eventsReceived.get() > cutoff) {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date date = new Date();

                        long endTime = System.currentTimeMillis();
                        int currentBatchSize = eventsReceived.getAndSet(0);
                        totalEventCounter.addAndGet(currentBatchSize);

                        String line = "[" + dateFormat.format(date) + "] # of events : " + currentBatchSize +
                                " start timestamp : " + startTime +
                                " end time stamp : " + endTime + " Throughput is (events / sec) : " +
                                (currentBatchSize * 1000) / (endTime - startTime) + " Total Event Count : " +
                                totalEventCounter + " \n";
                        File file = new File(Utils.getCarbonHome() + File.separator + "receiver-perf.txt");
                        if (!file.exists()) {
                            log.info("Creating the performance measurement file at : " + file.getAbsolutePath());
                        }
                        try {
                            appendToFile(IOUtils.toInputStream(line), file);
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                        }
                        startTime = 0;
                    }
                }
            }
        }

    }

    public void appendToFile(final InputStream in, final File f) throws IOException {
        OutputStream stream = null;
        try {
            stream = new BufferedOutputStream(new FileOutputStream(f, true));
            IOUtils.copy(in, stream);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    private void startTimeMeasurement() {
        if (isProfileReceiver) {
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            }
        }
    }

    public String login(String username, String password) throws AuthenticationException {
        log.info("user " + username + " connected");
        return authenticator.authenticate(username, password);
    }

    public void logout(String sessionId) throws Exception {
        AgentSession agentSession = authenticator.getSession(sessionId);
        authenticator.logout(sessionId);
        if (agentSession != null) {
            log.info("user " + agentSession.getUsername() + " disconnected");
        } else {
            log.info("session " + sessionId + " disconnected");
        }
    }

    public DataBridgeConfiguration getInitialConfig() {
        return this.dataBridgeConfiguration;
    }

    public void setInitialConfig(DataBridgeConfiguration initialConfig) {
        this.dataBridgeConfiguration = initialConfig;
    }

    /**
     * CEP/BAM can subscribe for Event Streams.
     *
     * @param agentCallback callbacks of the subscribers
     */
    public void subscribe(AgentCallback agentCallback) {
        eventDispatcher.addCallback(agentCallback);
    }

    /**
     * CEP/BAM can subscribe for Raw Event Streams.
     *
     * @param agentCallback callbacks of the subscribers
     */
    public void subscribe(RawDataAgentCallback agentCallback) {
        eventDispatcher.addCallback(agentCallback);
    }

    @Override
    public StreamDefinition getStreamDefinition(String sessionId, String streamName,
                                                String streamVersion)
            throws SessionTimeoutException, StreamDefinitionNotFoundException,
            StreamDefinitionStoreException {
        AgentSession agentSession = authenticator.getSession(sessionId);
        if (agentSession.getUsername() == null) {
            if (log.isDebugEnabled()) {
                log.debug("session " + sessionId + " expired ");
            }
            throw new SessionTimeoutException(sessionId + " expired");
        }

        try {
            authenticatorHandler.initContext(agentSession);
            return getStreamDefinition(streamName, streamVersion);
        } finally {
            authenticatorHandler.destroyContext(agentSession);

        }
    }

    @Override
    public List<StreamDefinition> getAllStreamDefinitions(String sessionId)
            throws SessionTimeoutException {
        AgentSession agentSession = authenticator.getSession(sessionId);
        if (agentSession.getUsername() == null) {
            if (log.isDebugEnabled()) {
                log.debug("session " + sessionId + " expired ");
            }
            throw new SessionTimeoutException(sessionId + " expired");
        }
        try {
            authenticatorHandler.initContext(agentSession);
            return getAllStreamDefinitions();
        } finally {
            authenticatorHandler.destroyContext(agentSession);
        }
    }

    @Override
    public void saveStreamDefinition(String sessionId, StreamDefinition streamDefinition)
            throws SessionTimeoutException, StreamDefinitionStoreException,
            DifferentStreamDefinitionAlreadyDefinedException {
        AgentSession agentSession = authenticator.getSession(sessionId);
        if (agentSession.getUsername() == null) {
            if (log.isDebugEnabled()) {
                log.debug("session " + sessionId + " expired ");
            }
            throw new SessionTimeoutException(sessionId + " expired");
        }
        try {
            authenticatorHandler.initContext(agentSession);
            saveStreamDefinition(streamDefinition);
            eventDispatcher.updateStreamDefinitionHolder(agentSession);
        } finally {
            authenticatorHandler.destroyContext(agentSession);
        }
    }

    // Stream store operations
    @Override
    public StreamDefinition getStreamDefinition(String streamName,
                                                String streamVersion)
            throws StreamDefinitionNotFoundException, StreamDefinitionStoreException {
        return streamDefinitionStore.getStreamDefinition(streamName, streamVersion);

    }

    @Override
    public StreamDefinition getStreamDefinition(String streamId)
            throws StreamDefinitionNotFoundException, StreamDefinitionStoreException {
        return streamDefinitionStore.getStreamDefinition(streamId);

    }

    @Override
    public List<StreamDefinition> getAllStreamDefinitions() {
        return new ArrayList<>(streamDefinitionStore.getAllStreamDefinitions());
    }

    @Override
    public void saveStreamDefinition(StreamDefinition streamDefinition)
            throws DifferentStreamDefinitionAlreadyDefinedException,
            StreamDefinitionStoreException {
        streamDefinitionStore.saveStreamDefinition(streamDefinition);
    }

    @Override
    public boolean deleteStreamDefinition(String streamName,
                                          String streamVersion) {
        return streamDefinitionStore.deleteStreamDefinition(streamName, streamVersion);
    }

    public List<AgentCallback> getSubscribers() {
        return eventDispatcher.getSubscribers();
    }

    public List<RawDataAgentCallback> getRawDataSubscribers() {
        return eventDispatcher.getRawDataSubscribers();
    }

    @Override
    public void subscribe(StreamAddRemoveListener streamAddRemoveListener) {
        if (streamAddRemoveListener != null) {
            streamAddRemoveListenerList.add(streamAddRemoveListener);
        }
    }

    @Override
    public void unsubscribe(StreamAddRemoveListener streamAddRemoveListener) {
        if (streamAddRemoveListener != null) {
            streamAddRemoveListenerList.remove(streamAddRemoveListener);
        }
    }

    /**
     * This creates the DataBridgeConfiguration from the data-bridge-config.yaml file.
     *
     * @param configPath
     * @return DataBridgeConfiguration
     */
    private DataBridgeConfiguration createDataBridgeConfiguration(String configPath) throws
            DataBridgeConfigurationException {
        Path databridgeConfigPath = Paths.get(configPath);
        DataBridgeConfiguration dataBridgeConfiguration;
        try {
            if (Files.exists(databridgeConfigPath)) {
                ConfigProvider configProvider = ConfigProviderFactory.getConfigProvider(databridgeConfigPath);
                LinkedHashMap transportsConf = (LinkedHashMap) configProvider.
                        getConfigurationObject(DataBridgeConstants.TRANSPORTS_NAMESPACE);
                if (transportsConf != null) {
                    dataBridgeConfiguration = DatabridgeConfigurationFileResolver
                            .resolveTransportsNamespaceConfiguration(transportsConf);
                } else {
                    dataBridgeConfiguration = DatabridgeConfigurationFileResolver.
                            resolveAndSetDatabridgeConfiguration((LinkedHashMap) configProvider.
                                    getConfigurationObject(DataBridgeConstants.DATABRIDGE_CONFIG_NAMESPACE));
                }
                return dataBridgeConfiguration;
            } else {
                log.error("Cannot find data bridge configuration file : " + configPath);
                return null;
            }
        } catch (ConfigurationException e) {
            throw new DataBridgeConfigurationException("Error in when loading databridge configuration", e);
        }
    }

}
