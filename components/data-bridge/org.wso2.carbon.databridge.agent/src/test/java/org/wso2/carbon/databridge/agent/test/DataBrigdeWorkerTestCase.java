/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.databridge.agent.test;

import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataEndpointAgent;
import org.wso2.carbon.databridge.agent.conf.DataEndpointConfiguration;
import org.wso2.carbon.databridge.agent.endpoint.DataEndpoint;
import org.wso2.carbon.databridge.agent.endpoint.DataEndpointConnectionWorker;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

public class DataBrigdeWorkerTestCase {
    private String agentConfigFileName = "data-agent-config.xml";

    @BeforeClass
    public static void init() {
        DataPublisherTestUtil.setKeyStoreParams();
        DataPublisherTestUtil.setTrustStoreParams();
    }

    @Test
    public void testDataEndpointConnectionWorkerNotInitializedTest() {
        Thread thread = new Thread(new DataEndpointConnectionWorker());
        thread.start();
    }

    @Test
    public void testDataEndpointConnectionWorkerReInitializingTest() throws DataEndpointAuthenticationException,
            DataEndpointAgentConfigurationException, TransportException, DataEndpointException,
            DataEndpointConfigurationException, ClassNotFoundException, IllegalAccessException, InstantiationException {

        String hostName = DataPublisherTestUtil.LOCAL_HOST;
        Boolean expected = false;
        AgentHolder.setConfigPath(DataPublisherTestUtil.getDataAgentConfigPath(agentConfigFileName));
        DataEndpointAgent dataEndpointAgent = AgentHolder.getInstance().getDataEndpointAgent("Binary");
        DataEndpointConnectionWorker dataEndpointConnectionWorker = new DataEndpointConnectionWorker();
        DataEndpointConfiguration endpointConfiguration =
                new DataEndpointConfiguration("tcp://" + hostName + ":9681",
                        "ssl://" + hostName + ":9781", "admin", "admin",
                        dataEndpointAgent.getTransportPool(),
                        dataEndpointAgent.getSecuredTransportPool(), dataEndpointAgent.
                        getAgentConfiguration().getBatchSize(),
                        dataEndpointAgent.getAgentConfiguration().getCorePoolSize(),
                        dataEndpointAgent.getAgentConfiguration().getMaxPoolSize(),
                        dataEndpointAgent.getAgentConfiguration().getKeepAliveTimeInPool());
        DataEndpoint dataEndpoint = dataEndpointAgent.getNewDataEndpoint();
        dataEndpoint.initialize(endpointConfiguration);
        dataEndpointConnectionWorker.initialize(dataEndpoint, endpointConfiguration);
        try {
            dataEndpointConnectionWorker.initialize(dataEndpoint, endpointConfiguration);
        } catch (DataEndpointException e) {
            expected = true;
        }
        Assert.assertTrue("Already data endpoint is configured for the connection worker, hence expected to fail", expected);
    }
}
