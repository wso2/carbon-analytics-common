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

package org.wso2.carbon.databridge.receiver.thrift.internal;

import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.commons.ServerEventListener;
import org.wso2.carbon.databridge.commons.thrift.utils.HostAddressFinder;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.receiver.thrift.ThriftDataReceiverFactory;
import org.wso2.carbon.databridge.receiver.thrift.conf.ThriftDataReceiverConfiguration;

/**
 * Thrift Server Startup Implementation.
 */
public class ThriftServerStartupImpl implements ServerEventListener {

    private static final Logger log = Logger.getLogger(ThriftServerStartupImpl.class);
    private static final String DISABLE_RECEIVER = "disable.receiver";
    private boolean isStarted = false;

    @Override
    public void start() {
        String disableReceiver = System.getProperty(DISABLE_RECEIVER);
        if (Boolean.parseBoolean(disableReceiver)) {
            log.info("Receiver disabled.");
            return;
        }
        try {
            ThriftDataReceiverConfiguration thriftDataReceiverConfiguration = new ThriftDataReceiverConfiguration(
                    ServiceHolder.getDataBridgeReceiverService().getInitialConfig(),
                    ServiceHolder.getCarbonRuntime().getConfiguration().getPortsConfig().getOffset());

            if (ServiceHolder.getDataReceiver() == null) {
                ServiceHolder.setDataReceiver(new ThriftDataReceiverFactory().createAgentServer(
                        thriftDataReceiverConfiguration, ServiceHolder.getDataBridgeReceiverService()));
                // TODO: 1/27/17 Hack to get host name. Change later
                /*String serverUrl = CarbonUtils.getServerURL(ServiceHolder.getServerConfiguration(),
                        ServiceHolder.getConfigurationContext().getServerConfigContext());*/
                String hostName = thriftDataReceiverConfiguration.getReceiverHostName();
                if (null == hostName) {
                    hostName = HostAddressFinder.findAddress("localhost");
                    /*try {
                        hostName = new URL(serverUrl).getHost();
                    } catch (MalformedURLException e) {
                        hostName = HostAddressFinder.findAddress("localhost");
                        if (!serverUrl.matches("local:/.services/")) {
                            log.info("The server url :" + serverUrl + " is using local, hence hostname is assigned as '"
                                    + hostName + "'");
                        }
                    }*/
                }
                ServiceHolder.getDataReceiver().start(hostName);
                isStarted = true;
            }
        } catch (DataBridgeException e) {
            log.error("Can not create and start Agent Server ", e);
        } catch (RuntimeException e) {
            log.error("Error in starting Agent Server ", e);
        } catch (Throwable e) {
            log.error("Unexpected Error in starting Agent Server ", e);
        }
    }

    @Override
    public void stop() {
        if (isStarted) {
            log.info("Thrift server shutting down...");

            ServiceHolder.getDataReceiver().stop();
            isStarted = false;
            if (log.isDebugEnabled()) {
                log.debug("Successfully stopped thrift agent server");
            }
        } else {
            log.info("Thrift server not started in order to stop");
        }
    }
}

