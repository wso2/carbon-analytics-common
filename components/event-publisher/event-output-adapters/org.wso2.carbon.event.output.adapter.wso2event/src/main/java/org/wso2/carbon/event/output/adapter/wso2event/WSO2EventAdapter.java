/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.output.adapter.wso2event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.event.output.adapter.core.EventAdapterUtil;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.ConnectionUnavailableException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterRuntimeException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.output.adapter.wso2event.internal.util.WSO2EventAdapterConstants;

import javax.jmdns.JmmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

import static org.wso2.carbon.event.output.adapter.wso2event.internal.util.WSO2EventAdapterConstants.*;

public final class WSO2EventAdapter implements OutputEventAdapter {

    private static final Log log = LogFactory.getLog(WSO2EventAdapter.class);
    private final OutputEventAdapterConfiguration eventAdapterConfiguration;
    private final Map<String, String> globalProperties;
    private DataPublisher dataPublisher = null;
    private boolean isBlockingMode = false;
    private long timeout = 0;
    private int tenantId;
    private boolean autoDiscoveryEnabled;

    public WSO2EventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration,
                            Map<String, String> globalProperties) {

        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;

        String isDiscoveryEnabled = System.getProperty("receiverDiscoveryEnabled");
        if (isDiscoveryEnabled != null && isDiscoveryEnabled.equalsIgnoreCase("true")) {
            this.autoDiscoveryEnabled = true;
        }
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    public void init() throws OutputEventAdapterException {
        validateOutputEventAdapterConfigurations();
        tenantId= PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        String configPath = globalProperties.get(ADAPTER_CONF_PATH);
        if (configPath != null) {
            AgentHolder.setConfigPath(configPath);
        }
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        connect();
    }

    @Override
    public synchronized void connect() {

        String userName = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_CONF_WSO2EVENT_PROP_USER_NAME);
        String password = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_CONF_WSO2EVENT_PROP_PASSWORD);
        String authUrl = eventAdapterConfiguration.getStaticProperties()
                .get(ADAPTER_CONF_WSO2EVENT_PROP_AUTHENTICATOR_URL);
        String receiverUrl = eventAdapterConfiguration.getStaticProperties()
                .get(ADAPTER_CONF_WSO2EVENT_PROP_RECEIVER_URL);
        String protocol = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_CONF_WSO2EVENT_PROP_PROTOCOL);
        String publishingMode = eventAdapterConfiguration.getStaticProperties()
                .get(ADAPTER_CONF_WSO2EVENT_PROP_PUBLISHING_MODE);
        String timeoutString = eventAdapterConfiguration.getStaticProperties()
                .get(ADAPTER_CONF_WSO2EVENT_PROP_PUBLISH_TIMEOUT_MS);

        if (publishingMode.equalsIgnoreCase(ADAPTER_PUBLISHING_MODE_BLOCKING)) {
            isBlockingMode = true;
        } else {
            timeout = Long.parseLong(timeoutString);
        }

        if (this.autoDiscoveryEnabled) {
            String discoveredUrl = this.autoDiscoverReceiver();
            if (discoveredUrl != null) {
                log.info("Replacing specified Thrift endpoint URL '" + receiverUrl +
                        "' with auto-discovered Thrift endpoint '" + discoveredUrl + "'");
                receiverUrl = discoveredUrl;
            }
        }

        try {
            if (authUrl != null && authUrl.length() > 0) {
                dataPublisher = new DataPublisher(protocol, receiverUrl, authUrl, userName, password);
            } else {
                dataPublisher = new DataPublisher(protocol, receiverUrl, null, userName, password);
            }
        } catch (DataEndpointAgentConfigurationException e) {
            throwRuntimeException(receiverUrl, authUrl, protocol, userName, e);
        } catch (DataEndpointException e) {
            throwConnectionException(receiverUrl, authUrl, protocol, userName, e);
        } catch (DataEndpointConfigurationException e) {
            throwRuntimeException(receiverUrl, authUrl, protocol, userName, e);
        } catch (DataEndpointAuthenticationException e) {
            throwConnectionException(receiverUrl, authUrl, protocol, userName, e);
        } catch (TransportException e) {
            throwConnectionException(receiverUrl, authUrl, protocol, userName, e);
        }
    }

    @Override
    public void publish(Object message, Map<String, String> dynamicProperties) {
        Event event = (Event) (message);

        if (isBlockingMode) {
            dataPublisher.publish(event);
        } else {
            if (!dataPublisher.tryPublish(event, timeout)) {
                EventAdapterUtil.logAndDrop(eventAdapterConfiguration.getName(), message, "Cannot send event", log, tenantId);
            }
        }
    }

    @Override
    public void disconnect() {
        if (dataPublisher != null) {
            try {
                dataPublisher.shutdown();
            } catch (DataEndpointException e) {
                String userName = eventAdapterConfiguration.getStaticProperties()
                        .get(ADAPTER_CONF_WSO2EVENT_PROP_USER_NAME);
                String authUrl = eventAdapterConfiguration.getStaticProperties()
                        .get(ADAPTER_CONF_WSO2EVENT_PROP_AUTHENTICATOR_URL);
                String receiverUrl = eventAdapterConfiguration.getStaticProperties()
                        .get(ADAPTER_CONF_WSO2EVENT_PROP_RECEIVER_URL);
                String protocol = eventAdapterConfiguration.getStaticProperties()
                        .get(ADAPTER_CONF_WSO2EVENT_PROP_PROTOCOL);
                logException("Error in shutting down the data publisher", receiverUrl, authUrl, protocol, userName, e);
            }
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean isPolled() {
        return false;
    }

    private void validateOutputEventAdapterConfigurations() throws OutputEventAdapterException {
        String timeoutProperty = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_CONF_WSO2EVENT_PROP_PUBLISH_TIMEOUT_MS);
        if(timeoutProperty != null){
            try{
                Long.parseLong(timeoutProperty);
            } catch (NumberFormatException e){
                throw new OutputEventAdapterException("Invalid value set for property 'Publishing Timeout': " + timeoutProperty, e);
            }
        }
    }

    private String autoDiscoverReceiver() {
        JmmDNS mDnsClient = JmmDNS.Factory.getInstance();
        try {
            ServiceInfo[] services = mDnsClient.list(WSO2EventAdapterConstants.MDNS_SERVICE_TYPE);
            for (ServiceInfo service : services) {
                int port = service.getPort();
                InetAddress[] hosts = service.getInetAddresses();
                for (InetAddress host : hosts) {
                    if (isEndpointReachable(host, port, WSO2EventAdapterConstants.MDNS_CONNECTION_TIMEOUT)) {
                        return "tcp://" + host.getCanonicalHostName() + ":" + port;
                    }
                }
            }
            return null;
        } finally {
            try {
                mDnsClient.close();
            } catch (IOException ignore) {

            }
        }
    }

    private boolean isEndpointReachable(InetAddress host, int port, int timeout){
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void throwRuntimeException(String receiverUrl, String authUrl, String protocol, String userName,
                                       Exception e) {
        throw new OutputEventAdapterRuntimeException(
                "Error in data-bridge config for adapter " + eventAdapterConfiguration.getName()
                        + " with the receiverUrl:" + receiverUrl + " authUrl:" + authUrl + " protocol:" + protocol
                        + " and userName:" + userName + "," + e.getMessage(), e);
    }

    private void logException(String message, String receiverUrl, String authUrl, String protocol, String userName,
                              Exception e) {
        log.error(message + " for adapter " + eventAdapterConfiguration.getName()
                + " with the receiverUrl:" + receiverUrl + " authUrl:" + authUrl + " protocol:" + protocol
                + " and userName:" + userName + "," + e.getMessage(), e);
    }

    private void throwConnectionException(String receiverUrl, String authUrl, String protocol, String userName,
                                          Exception e) {
        throw new ConnectionUnavailableException(
                "Connection not available for adapter " + eventAdapterConfiguration.getName()
                        + " with the receiverUrl:" + receiverUrl + " authUrl:" + authUrl + " protocol:" + protocol
                        + " and userName:" + userName + "," + e.getMessage(), e);
    }

}
