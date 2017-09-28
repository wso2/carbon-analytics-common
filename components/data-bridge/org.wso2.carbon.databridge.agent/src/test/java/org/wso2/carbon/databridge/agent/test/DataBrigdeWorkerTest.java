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

public class DataBrigdeWorkerTest {
    private String agentConfigFileName = "data-agent-config.xml";

    @BeforeClass
    public static void init() {
        DataPublisherTestUtil.setKeyStoreParams();
        DataPublisherTestUtil.setTrustStoreParams();
    }

    @Test
    public void testDataEndpointConnectionWorkerNotInitializedTest() {
        try {
            new Thread(new DataEndpointConnectionWorker()).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        Assert.assertTrue("Invalid urls passed for receiver and auth, and hence expected to fail", expected);

    }
}
