/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.databridge.receiver.thrift.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.HttpService;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.utils.ConfigurationContextService;

@Component(
        name = "thriftdatareceiver.component",
        immediate = true)
public class ThriftDataReceiverDS {

    private static final Log log = LogFactory.getLog(ThriftDataReceiverDS.class);

    private static final String DISABLE_RECEIVER = "disable.receiver";

    /**
     * initialize the agent server here.
     *
     * @param context
     */
    @Activate
    protected void activate(ComponentContext context) {

        String disableReceiver = System.getProperty(DISABLE_RECEIVER);
        if (disableReceiver != null && Boolean.parseBoolean(disableReceiver)) {
            log.info("Receiver disabled.");
            return;
        }
        context.getBundleContext().registerService(ServerStartupObserver.class.getName(), new
                ThriftServerStartupObserver(), null);
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        log.info("Thrift server shutting down...");
        ServiceHolder.getDataReceiver().stop();
        if (log.isDebugEnabled()) {
            log.debug("Successfully stopped agent server");
        }
    }

    @Reference(
            name = "server.configuration",
            service = org.wso2.carbon.base.api.ServerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetServerConfiguration")
    protected void setServerConfiguration(ServerConfigurationService serverConfiguration) {

        ServiceHolder.setServerConfiguration(serverConfiguration);
    }

    protected void unsetServerConfiguration(ServerConfigurationService serverConfiguration) {

        ServiceHolder.setServerConfiguration(null);
    }

    @Reference(
            name = "configuration.context",
            service = org.wso2.carbon.utils.ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContext")
    protected void setConfigurationContext(ConfigurationContextService configurationContext) {

        ServiceHolder.setConfigurationContext(configurationContext);
    }

    protected void unsetConfigurationContext(ConfigurationContextService configurationContext) {

        ServiceHolder.setConfigurationContext(null);
    }

    @Reference(
            name = "databridge.core",
            service = org.wso2.carbon.databridge.core.DataBridgeReceiverService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDatabridgeReceiverService")
    protected void setDataBridgeReceiverService(DataBridgeReceiverService dataBridgeReceiverService) {

        ServiceHolder.setDataBridgeReceiverService(dataBridgeReceiverService);
    }

    protected void unsetDatabridgeReceiverService(DataBridgeReceiverService dataBridgeReceiverService) {

        ServiceHolder.setDataBridgeReceiverService(dataBridgeReceiverService);
    }

    @Reference(
            name = "http.service",
            service = org.osgi.service.http.HttpService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetHttpService")
    protected void setHttpService(HttpService httpService) {

        ServiceHolder.setHttpServiceInstance(httpService);
    }

    protected void unsetHttpService(HttpService httpService) {

        ServiceHolder.setHttpServiceInstance(httpService);
    }
}

