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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.databridge.commons.ServerEventListener;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.kernel.CarbonRuntime;

/**
 * ThriftDataReceiver Service component.
 *
 * @since 1.0.0
 */
@Component(
        name = "org.wso2.carbon.databridge.receiver.thrift.internal.ThriftDataReceiverDS",
        immediate = true
)

public class ThriftDataReceiverDS {
    private static final Logger log = LogManager.getLogger(ThriftDataReceiverDS.class);
    private ThriftServerStartupImpl thriftDataReceiver;

    /**
     * This is the activation method of ThriftDataReceiverDS. This will be called when its references are
     * satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {
        log.info("Service Component is activated");
        thriftDataReceiver = new ThriftServerStartupImpl();
        bundleContext.registerService(ServerEventListener.class.getName(),
                thriftDataReceiver, null);
    }

    /**
     * This is the deactivation method of ThriftDataReceiverDS. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        thriftDataReceiver.stop();
    }

    /**
     *
     * @param dataBridgeReceiverService The DataBridgeReceiverService instance registered as an OSGi service
     */
    @Reference(
            name = "dataBridge.receiver.service",
            service = DataBridgeReceiverService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDatabridgeReceiverService"
    )
    protected void setDataBridgeReceiverService(
            DataBridgeReceiverService dataBridgeReceiverService) {
        ServiceHolder.setDataBridgeReceiverService(dataBridgeReceiverService);
    }

    protected void unsetDatabridgeReceiverService(
            DataBridgeReceiverService dataBridgeReceiverService) {
        ServiceHolder.setDataBridgeReceiverService(dataBridgeReceiverService);
    }


    /**
     * This bind method will be called when CarbonRuntime OSGi service is registered.
     *
     * @param carbonRuntime The CarbonRuntime instance registered by Carbon Kernel as an OSGi service
     */
    @Reference(
            name = "carbon.runtime.service",
            service = CarbonRuntime.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCarbonRuntime"
    )
    protected void setCarbonRuntime(CarbonRuntime carbonRuntime) {
        ServiceHolder.setCarbonRuntime(carbonRuntime);
    }

    protected void unsetCarbonRuntime(CarbonRuntime carbonRuntime) {
        ServiceHolder.setCarbonRuntime(null);
    }

}

