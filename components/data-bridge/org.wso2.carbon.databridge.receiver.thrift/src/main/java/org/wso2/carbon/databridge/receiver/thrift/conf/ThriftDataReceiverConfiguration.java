/**
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.databridge.receiver.thrift.conf;

import org.wso2.carbon.databridge.commons.thrift.utils.CommonThriftConstants;
import org.wso2.carbon.databridge.core.conf.DataBridgeConfiguration;
import org.wso2.carbon.databridge.core.conf.DataReceiver;
import org.wso2.carbon.databridge.receiver.thrift.internal.utils.ThriftDataReceiverConstants;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * configuration details related to DataReceiver
 */
public class ThriftDataReceiverConfiguration {

    private boolean enable = true;
    private int secureDataReceiverPort;
    private int dataReceiverPort;
    private String sslProtocols;
    private String ciphers;
    private String receiverHostName;
    private int waitingTimeInMilliSeconds;
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

    public ThriftDataReceiverConfiguration(DataBridgeConfiguration dataBridgeConfiguration) {
        DataReceiver dataReceiver = dataBridgeConfiguration.getDataReceiver(ThriftDataReceiverConstants.
                DATA_BRIDGE_RECEIVER_NAME);
        int portOffset = getPortOffset();
        secureDataReceiverPort = Integer.parseInt(dataReceiver.getConfiguration(ThriftDataReceiverConstants.SECURE_PORT_ELEMENT,
                CommonThriftConstants.DEFAULT_RECEIVER_PORT+CommonThriftConstants.SECURE_EVENT_RECEIVER_PORT_OFFSET).toString()) + portOffset;
        dataReceiverPort = Integer.parseInt(dataReceiver.getConfiguration(ThriftDataReceiverConstants.PORT_ELEMENT,
                CommonThriftConstants.DEFAULT_RECEIVER_PORT).toString()) + portOffset;
        receiverHostName = dataReceiver.getConfiguration(ThriftDataReceiverConstants.RECEIVER_HOST_NAME,
                ThriftDataReceiverConstants.DEFAULT_HOSTNAME).toString();
        waitingTimeInMilliSeconds = Integer.parseInt(dataReceiver.getConfiguration(ThriftDataReceiverConstants
                                .WAITING_TIME_IN_MILISEONDS, 0).toString());

        Object sslProtocolObj = dataReceiver.getConfiguration(ThriftDataReceiverConstants.PROTOCOLS_ELEMENT, null);
        sslProtocols =  sslProtocolObj != null ? sslProtocolObj.toString() : null;
        Object ciphersObj = dataReceiver.getConfiguration(ThriftDataReceiverConstants.CIPHERS_ELEMENT, null);
        ciphers =  sslProtocolObj != null ? ciphersObj.toString() : null;

        tcpMaxWorkerThreads = Integer.parseInt(dataReceiver.getConfiguration(
                ThriftDataReceiverConstants.THRIFT_TCP_MAX_WORKER_THREADS, tcpMaxWorkerThreads).toString());
        tcpMinWorkerThreads = Integer.parseInt(dataReceiver.getConfiguration(
                ThriftDataReceiverConstants.THRIFT_TCP_MIN_WORKER_THREADS, tcpMinWorkerThreads).toString());
        tcpRequestTimeout = Integer.parseInt(dataReceiver.getConfiguration(
                ThriftDataReceiverConstants.THRIFT_TCP_REQUEST_TIMEOUT, tcpRequestTimeout).toString());
        tcpStopTimeoutVal = Integer.parseInt(dataReceiver.getConfiguration(
                ThriftDataReceiverConstants.THRIFT_TCP_STOP_TIMEOUT_VAL, tcpStopTimeoutVal).toString());

        sslMaxWorkerThreads = Integer.parseInt(dataReceiver.getConfiguration(
                ThriftDataReceiverConstants.THRIFT_SSL_MAX_WORKER_THREADS, sslMaxWorkerThreads).toString());
        sslMinWorkerThreads = Integer.parseInt(dataReceiver.getConfiguration(
                ThriftDataReceiverConstants.THRIFT_SSL_MIN_WORKER_THREADS, sslMinWorkerThreads).toString());
        sslRequestTimeout = Integer.parseInt(dataReceiver.getConfiguration(
                ThriftDataReceiverConstants.THRIFT_SSL_REQUEST_TIMEOUT, sslRequestTimeout).toString());
        sslStopTimeoutVal = Integer.parseInt(dataReceiver.getConfiguration(
                ThriftDataReceiverConstants.THRIFT_SSL_STOP_TIMEOUT_VAL, sslStopTimeoutVal).toString());
        enable = Boolean.valueOf(dataReceiver.getConfiguration(ThriftDataReceiverConstants.ENABLE_THRIFT_RECEIVER, enable).toString());
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

    public int getPortOffset() {
        return CarbonUtils.
                getPortFromServerConfig(ThriftDataReceiverConstants.CARBON_CONFIG_PORT_OFFSET_NODE) + 1;
    }

    public String getSslProtocols() {
        return sslProtocols;
    }

    public int getWaitingTimeInMilliSeconds(){
        return waitingTimeInMilliSeconds;
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

    public boolean isEnable() {

        return enable;
    }
}
