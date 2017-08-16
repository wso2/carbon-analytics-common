/**
 *
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.databridge.receiver.thrift.conf;

import org.wso2.carbon.databridge.commons.thrift.utils.CommonThriftConstants;
import org.wso2.carbon.databridge.core.conf.DataBridgeConfiguration;
import org.wso2.carbon.databridge.core.conf.DataReceiverConfiguration;
import org.wso2.carbon.databridge.receiver.thrift.internal.utils.ThriftDataReceiverConstants;

/**
 * configuration details related to DataReceiver.
 */
public class ThriftDataReceiverConfiguration {
    private int secureDataReceiverPort;
    private int dataReceiverPort;
    private String sslProtocols;
    private String ciphers;
    private String receiverHostName;
    private int tcpMaxWorkerThreads = ThriftDataReceiverConstants.THRIFT_TCP_DEFAULT_MAX_WORKER_THREADS;
    private int tcpMinWorkerThreads = ThriftDataReceiverConstants.UNDEFINED;
    private int tcpRequestTimeout = ThriftDataReceiverConstants.UNDEFINED;
    private int tcpStopTimeoutVal = ThriftDataReceiverConstants.UNDEFINED;
    private int sslMaxWorkerThreads = ThriftDataReceiverConstants.THRIFT_SSL_DEFAULT_MAX_WORKER_THREADS;
    private int sslMinWorkerThreads = ThriftDataReceiverConstants.UNDEFINED;
    private int sslRequestTimeout = ThriftDataReceiverConstants.UNDEFINED;
    private int sslStopTimeoutVal = ThriftDataReceiverConstants.UNDEFINED;

    public ThriftDataReceiverConfiguration(int defaultSslPort, int defaultPort) {
        secureDataReceiverPort = defaultSslPort;
        dataReceiverPort = defaultPort;
    }

    public ThriftDataReceiverConfiguration(DataBridgeConfiguration dataBridgeConfiguration, int portOffset) {
        DataReceiverConfiguration dataReceiverConfiguration = dataBridgeConfiguration.getDataReceiver
                (ThriftDataReceiverConstants.DATA_BRIDGE_RECEIVER_NAME);

        String sslPortConfiguration = dataReceiverConfiguration.getProperties().get(
                ThriftDataReceiverConstants.SECURE_PORT_ELEMENT);
        String tcpPortConfiguration = dataReceiverConfiguration.getProperties().get(
                ThriftDataReceiverConstants.PORT_ELEMENT);
        String receiverHostName = dataReceiverConfiguration.getProperties().get(
                ThriftDataReceiverConstants.RECEIVER_HOST_NAME);
        String sslProtocols = dataReceiverConfiguration.getProperties().get(
                ThriftDataReceiverConstants.PROTOCOLS_ELEMENT);
        String ciphers = dataReceiverConfiguration.getProperties().get(ThriftDataReceiverConstants.CIPHERS_ELEMENT);

        if (sslPortConfiguration != null && !sslPortConfiguration.trim().isEmpty()) {
            this.secureDataReceiverPort = Integer.parseInt(sslPortConfiguration.trim()) + portOffset;
        } else {
            this.secureDataReceiverPort = CommonThriftConstants.DEFAULT_RECEIVER_PORT +
                                          CommonThriftConstants.SECURE_EVENT_RECEIVER_PORT_OFFSET + portOffset;
        }

        if (tcpPortConfiguration != null && !tcpPortConfiguration.trim().isEmpty()) {
            this.dataReceiverPort = Integer.parseInt(tcpPortConfiguration) + portOffset;
        } else {
            this.dataReceiverPort = CommonThriftConstants.DEFAULT_RECEIVER_PORT + portOffset;
        }

        if (receiverHostName != null && !receiverHostName.trim().isEmpty()) {
            this.receiverHostName = receiverHostName;
        } else {
            this.receiverHostName = ThriftDataReceiverConstants.DEFAULT_HOSTNAME;
        }

        if (sslProtocols != null && !sslProtocols.trim().isEmpty()) {
            this.sslProtocols = sslProtocols;
        }

        if (ciphers != null && !ciphers.trim().isEmpty()) {
            this.ciphers = ciphers;
        }

        String tcpMaxWorkerThreads = dataReceiverConfiguration.getProperties().get(
                ThriftDataReceiverConstants.THRIFT_TCP_MAX_WORKER_THREADS);
        if (tcpMaxWorkerThreads != null && !tcpMaxWorkerThreads.trim().isEmpty()) {
            this.tcpMaxWorkerThreads = Integer.parseInt(tcpMaxWorkerThreads.trim());
        }

        String tcpMinWorkerThreads = dataReceiverConfiguration.getProperties().get(
                ThriftDataReceiverConstants.THRIFT_TCP_MIN_WORKER_THREADS);
        if (tcpMinWorkerThreads != null && !tcpMinWorkerThreads.trim().isEmpty()) {
            this.tcpMinWorkerThreads = Integer.parseInt(tcpMinWorkerThreads.trim());
        }

        String tcpRequestTimeout = dataReceiverConfiguration.getProperties().get(
                ThriftDataReceiverConstants.THRIFT_TCP_REQUEST_TIMEOUT);
        if (tcpRequestTimeout != null && !tcpRequestTimeout.trim().isEmpty()) {
            this.tcpRequestTimeout = Integer.parseInt(tcpRequestTimeout.trim());
        }

        String tcpStopTimeoutVal = dataReceiverConfiguration.getProperties().get(
                ThriftDataReceiverConstants.THRIFT_TCP_STOP_TIMEOUT_VAL);
        if (tcpStopTimeoutVal != null && !tcpStopTimeoutVal.trim().isEmpty()) {
            this.tcpStopTimeoutVal = Integer.parseInt(tcpStopTimeoutVal.trim());
        }

        String sslMaxWorkerThreads = dataReceiverConfiguration.getProperties().get(
                ThriftDataReceiverConstants.THRIFT_SSL_MAX_WORKER_THREADS);
        if (sslMaxWorkerThreads != null && !sslMaxWorkerThreads.trim().isEmpty()) {
            this.sslMaxWorkerThreads = Integer.parseInt(sslMaxWorkerThreads.trim());
        }

        String sslMinWorkerThreads = dataReceiverConfiguration.getProperties().get(
                ThriftDataReceiverConstants.THRIFT_SSL_MIN_WORKER_THREADS);
        if (sslMinWorkerThreads != null && !sslMinWorkerThreads.trim().isEmpty()) {
            this.sslMinWorkerThreads = Integer.parseInt(sslMinWorkerThreads.trim());
        }

        String sslRequestTimeout = dataReceiverConfiguration.getProperties().get(
                ThriftDataReceiverConstants.THRIFT_SSL_REQUEST_TIMEOUT);
        if (sslRequestTimeout != null && !sslRequestTimeout.trim().isEmpty()) {
            this.sslRequestTimeout = Integer.parseInt(sslRequestTimeout.trim());
        }

        String sslStopTimeoutVal = dataReceiverConfiguration.getProperties().get(
                ThriftDataReceiverConstants.THRIFT_SSL_STOP_TIMEOUT_VAL);
        if (sslStopTimeoutVal != null && !sslStopTimeoutVal.trim().isEmpty()) {
            this.sslStopTimeoutVal = Integer.parseInt(sslStopTimeoutVal.trim());
        }
    }

    public ThriftDataReceiverConfiguration(int defaultSslPort, int defaultPort,
                                           String confHostName) {
        secureDataReceiverPort = defaultSslPort;
        dataReceiverPort = defaultPort;
        receiverHostName = confHostName;
    }

    public int getDataReceiverPort() {
        return dataReceiverPort;
    }

    public void setDataReceiverPort(int dataReceiverPort) {
        this.dataReceiverPort = dataReceiverPort;
    }

    public int getSecureDataReceiverPort() {
        return secureDataReceiverPort;
    }

    public void setSecureDataReceiverPort(int secureDataReceiverPort) {
        this.secureDataReceiverPort = secureDataReceiverPort;
    }

    public String getReceiverHostName() {
        return receiverHostName;
    }

    public void setReceiverHostName(String receiverHostName) {
        this.receiverHostName = receiverHostName;
    }

    public String getSslProtocols() {
        return sslProtocols;
    }

    public void setSslProtocols(String sslProtocols) {
        this.sslProtocols = sslProtocols;
    }

    public String getCiphers() {
        return ciphers;
    }

    public void setCiphers(String ciphers) {
        this.ciphers = ciphers;
    }

    public int getTcpMaxWorkerThreads() {
        return tcpMaxWorkerThreads;
    }

    public void setTcpMaxWorkerThreads(int tcpMaxWorkerThreads) {
        this.tcpMaxWorkerThreads = tcpMaxWorkerThreads;
    }

    public int getTcpMinWorkerThreads() {
        return tcpMinWorkerThreads;
    }

    public void setTcpMinWorkerThreads(int tcpMinWorkerThreads) {
        this.tcpMinWorkerThreads = tcpMinWorkerThreads;
    }

    public int getTcpRequestTimeout() {
        return tcpRequestTimeout;
    }

    public void setTcpRequestTimeout(int tcpRequestTimeout) {
        this.tcpRequestTimeout = tcpRequestTimeout;
    }

    public int getTcpStopTimeoutVal() {
        return tcpStopTimeoutVal;
    }

    public void setTcpStopTimeoutVal(int tcpStopTimeoutVal) {
        this.tcpStopTimeoutVal = tcpStopTimeoutVal;
    }

    public int getSslMaxWorkerThreads() {
        return sslMaxWorkerThreads;
    }

    public void setSslMaxWorkerThreads(int sslMaxWorkerThreads) {
        this.sslMaxWorkerThreads = sslMaxWorkerThreads;
    }

    public int getSslMinWorkerThreads() {
        return sslMinWorkerThreads;
    }

    public void setSslMinWorkerThreads(int sslMinWorkerThreads) {
        this.sslMinWorkerThreads = sslMinWorkerThreads;
    }

    public int getSslRequestTimeout() {
        return sslRequestTimeout;
    }

    public void setSslRequestTimeout(int sslRequestTimeout) {
        this.sslRequestTimeout = sslRequestTimeout;
    }

    public int getSslStopTimeoutVal() {
        return sslStopTimeoutVal;
    }

    public void setSslStopTimeoutVal(int sslStopTimeoutVal) {
        this.sslStopTimeoutVal = sslStopTimeoutVal;
    }
}
