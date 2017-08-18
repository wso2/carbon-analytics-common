/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.databridge.receiver.thrift.internal.utils;


/**
 * Agent Server Constants
 */
public final class ThriftDataReceiverConstants {

    private ThriftDataReceiverConstants() {
    }
    public static final String SECURE_PORT_ELEMENT = "sslPort";
    public static final String PORT_ELEMENT = "tcpPort";
    public static final String PROTOCOLS_ELEMENT = "sslEnabledProtocols";
    public static final String CIPHERS_ELEMENT = "ciphers";
    public static final String CARBON_CONFIG_PORT_OFFSET_NODE = "Ports.Offset";
    public static final String RECEIVER_HOST_NAME = "hostName";
    public static final String WAITING_TIME_IN_MILISEONDS = "waitingTimeInMilliSeconds";
    public static final String DEFAULT_HOSTNAME = "0.0.0.0";
    public static final String DATA_BRIDGE_RECEIVER_NAME = "Thrift";
    public static final String DATA_BRIDGE_KEY_STORE_LOCATION = "keyStoreLocation";
    public static final String DATA_BRIDGE_KEY_STORE_PASSWORD = "keyStorePassword";

    public static final String THRIFT_TCP_MAX_WORKER_THREADS = "tcpMaxWorkerThreads";
    public static final String THRIFT_TCP_MIN_WORKER_THREADS = "tcpMinWorkerThreads";
    public static final String THRIFT_TCP_REQUEST_TIMEOUT = "tcpRequestTimeout";
    public static final String THRIFT_TCP_STOP_TIMEOUT_VAL = "tcpStopTimeoutVal";

    public static final String THRIFT_SSL_MAX_WORKER_THREADS = "sslMaxWorkerThreads";
    public static final String THRIFT_SSL_MIN_WORKER_THREADS = "sslMinWorkerThreads";
    public static final String THRIFT_SSL_REQUEST_TIMEOUT = "sslRequestTimeout";
    public static final String THRIFT_SSL_STOP_TIMEOUT_VAL = "sslStopTimeoutVal";

    public static final int THRIFT_TCP_DEFAULT_MAX_WORKER_THREADS = 20;
    public static final int THRIFT_SSL_DEFAULT_MAX_WORKER_THREADS = 20;

    public static final int UNDEFINED = -1;
}
