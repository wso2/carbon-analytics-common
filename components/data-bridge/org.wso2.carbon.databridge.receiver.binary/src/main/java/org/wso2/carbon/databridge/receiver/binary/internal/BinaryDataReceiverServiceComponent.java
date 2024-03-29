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
package org.wso2.carbon.databridge.receiver.binary.internal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.databridge.commons.ServerEventListener;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.databridge.receiver.binary.conf.BinaryDataReceiverConfiguration;
import org.wso2.carbon.kernel.CarbonRuntime;

/**
 * Binary Data Receiver ServiceComponent.
 */

@Component(
        name = "binary-receiver-component-service",
        immediate = true
)
public class BinaryDataReceiverServiceComponent {
    private static final Logger log = LogManager.getLogger(BinaryDataReceiverServiceComponent.class);
    private DataBridgeReceiverService dataBridgeReceiverService;
    private static CarbonRuntime carbonRuntime;
    private BinaryDataReceiver binaryDataReceiver;

    /**
     * initialize the agent server here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {
        log.info("org.wso2.carbon.databridge.receiver.binary.internal.Service Component is activated");
        binaryDataReceiver = new BinaryDataReceiver(new BinaryDataReceiverConfiguration(dataBridgeReceiverService.
                getInitialConfig()), dataBridgeReceiverService);
        context.getBundleContext().registerService(ServerEventListener.class.getName(),
                binaryDataReceiver, null);
    }

    protected void deactivate(ComponentContext context) {
        log.info("Binary Data Receiver server shutting down...");
        binaryDataReceiver.stop();
    }

    @Reference(
            name = "databridge.receiver.service",
            service = DataBridgeReceiverService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDatabridgeReceiverService"
    )
    protected void setDataBridgeReceiverService(
            DataBridgeReceiverService dataBridgeReceiverService) {
        this.dataBridgeReceiverService = dataBridgeReceiverService;
    }

    protected void unsetDatabridgeReceiverService(DataBridgeReceiverService dataBridgeReceiverService) {
        this.dataBridgeReceiverService = null;
    }

    @Reference(
            name = "carbon.runtime.service",
            service = CarbonRuntime.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCarbonRuntime"
    )
    protected void setCarbonRuntime(CarbonRuntime carbonRuntime) {
        BinaryDataReceiverServiceComponent.carbonRuntime = carbonRuntime;
    }

    protected void unsetCarbonRuntime(CarbonRuntime carbonRuntime) {
        BinaryDataReceiverServiceComponent.carbonRuntime = null;
    }

    public static CarbonRuntime getCarbonRuntime() {
        return carbonRuntime;
    }

}

