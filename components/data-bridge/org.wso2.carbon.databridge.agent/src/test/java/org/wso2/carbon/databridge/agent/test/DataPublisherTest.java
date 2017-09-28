/*
package org.wso2.carbon.databridge.agent.test;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.agent.test.binary.BinaryTestServer;
import org.wso2.carbon.databridge.agent.test.binary.OneEndPointDPBinaryTest;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DataPublisherTest {
    Logger log = Logger.getLogger(OneEndPointDPBinaryTest.class);
    private static final String STREAM_NAME = "org.wso2.esb.MediatorStatistics";
    private static final String VERSION = "1.0.0";
    private BinaryTestServer testServer;
    private String agentConfigFileName = "data-agent-config.xml";

    private static final String STREAM_DEFN = "{" +
            "  'name':'" + STREAM_NAME + "'," +
            "  'version':'" + VERSION + "'," +
            "  'nickName': 'Stock Quote Information'," +
            "  'description': 'Some Desc'," +
            "  'tags':['foo', 'bar']," +
            "  'metaData':[" +
            "          {'name':'ipAdd','type':'STRING'}" +
            "  ]," +
            "  'payloadData':[" +
            "          {'name':'symbol','type':'STRING'}," +
            "          {'name':'price','type':'DOUBLE'}," +
            "          {'name':'volume','type':'INT'}," +
            "          {'name':'max','type':'DOUBLE'}," +
            "          {'name':'min','type':'Double'}" +
            "  ]" +
            "}";

    @BeforeClass
    public static void init() {
        DataPublisherTestUtil.setKeyStoreParams();
        DataPublisherTestUtil.setTrustStoreParams();
    }

    @AfterClass
    public static void shop() throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException, DataEndpointException, DataEndpointConfigurationException {
        DataPublisher dataPublisher = new DataPublisher("Binary", "tcp://localhost:9687",
                "ssl://localhost:9787", "admin", "admin");
        dataPublisher.shutdownWithAgent();
    }

    private synchronized void startServer(int port, int securePort) throws DataBridgeException,
            StreamDefinitionStoreException, MalformedStreamDefinitionException, IOException {
        testServer = new BinaryTestServer();
        testServer.start(port, securePort);
        testServer.addStreamDefinition(STREAM_DEFN, -1234);
    }

    @Test
    public void overLoadPublish() throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException, DataEndpointException, DataEndpointConfigurationException, MalformedStreamDefinitionException, DataBridgeException, StreamDefinitionStoreException, IOException {
        startServer(9689, 9789);
        AgentHolder.setConfigPath(DataPublisherTestUtil.getDataAgentConfigPath(agentConfigFileName));
        String hostName = DataPublisherTestUtil.LOCAL_HOST;

        DataPublisher dataPublisher = new DataPublisher("Binary", "tcp://" + hostName + ":9689",
                "ssl://" + hostName + ":9789", "admin", "admin");

        String streamID = DataBridgeCommonsUtils.generateStreamId(STREAM_NAME, VERSION);
        Object[] metaData = new Object[]{"127.0.0.1"};
        Object[] correlationData = null;
        Object[] payLoad = new Object[]{"WSO2", 123.4, 2, 12.4, 1.3};

        int numberOfEventsSent = 1000;
        for (int i = 0; i < numberOfEventsSent; i++) {
            dataPublisher.publish(streamID, metaData, correlationData, payLoad);
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        dataPublisher.shutdown();
        Assert.assertEquals(numberOfEventsSent, testServer.getNumberOfEventsReceived());
        testServer.resetReceivedEvents();
        testServer.stop();
    }

    @Test
    public void overLoadPublishWithArbitraryElementsofEvent() throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException, DataEndpointException, DataEndpointConfigurationException, MalformedStreamDefinitionException, DataBridgeException, StreamDefinitionStoreException, IOException {
        startServer(9611, 9711);
        AgentHolder.setConfigPath(DataPublisherTestUtil.getDataAgentConfigPath(agentConfigFileName));
        String hostName = DataPublisherTestUtil.LOCAL_HOST;

        DataPublisher dataPublisher = new DataPublisher("Binary", "tcp://" + hostName + ":9611",
                "ssl://" + hostName + ":9711", "admin", "admin");

        String streamID = DataBridgeCommonsUtils.generateStreamId(STREAM_NAME, VERSION);
        Object[] metaData = new Object[]{"127.0.0.1"};
        Object[] correlationData = null;
        Object[] payLoad = new Object[]{"WSO2", 123.4, 2, 12.4, 1.3};
        Map<String, String> arbitrary = new HashMap<String, String>();
        arbitrary.put("test", "testValue");
        arbitrary.put("test1", "test123");

        int numberOfEventsSent = 1000;
        for (int i = 0; i < numberOfEventsSent; i++) {
            dataPublisher.publish(streamID, metaData, correlationData, payLoad, arbitrary);
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        dataPublisher.shutdown();
        Assert.assertEquals(numberOfEventsSent, testServer.getNumberOfEventsReceived());
        testServer.resetReceivedEvents();
        testServer.stop();
    }

    @Test
    public void overLoadPublishWithTimeStampOfEvent() throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException, DataEndpointException, DataEndpointConfigurationException, MalformedStreamDefinitionException, DataBridgeException, StreamDefinitionStoreException, IOException {
        startServer(9221, 9321);
        AgentHolder.setConfigPath(DataPublisherTestUtil.getDataAgentConfigPath(agentConfigFileName));
        String hostName = DataPublisherTestUtil.LOCAL_HOST;

        DataPublisher dataPublisher = new DataPublisher("Binary", "tcp://" + hostName + ":9221",
                "ssl://" + hostName + ":9321", "admin", "admin");

        String streamID = DataBridgeCommonsUtils.generateStreamId(STREAM_NAME, VERSION);
        Object[] metaData = new Object[]{"127.0.0.1"};
        Object[] correlationData = null;
        Object[] payLoad = new Object[]{"WSO2", 123.4, 2, 12.4, 1.3};
        Long timeStamp = System.currentTimeMillis();

        int numberOfEventsSent = 1000;
        for (int i = 0; i < numberOfEventsSent; i++) {
            dataPublisher.publish(streamID, timeStamp, metaData, correlationData, payLoad);
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        dataPublisher.shutdown();
        Assert.assertEquals(numberOfEventsSent, testServer.getNumberOfEventsReceived());
        testServer.resetReceivedEvents();
        testServer.stop();
    }

    @Test
    public void overLoadPublishWithTimeStampAndArbitaryElementsOfEvent() throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException, DataEndpointException, DataEndpointConfigurationException, MalformedStreamDefinitionException, DataBridgeException, StreamDefinitionStoreException, IOException {
        startServer(9631, 9731);
        AgentHolder.setConfigPath(DataPublisherTestUtil.getDataAgentConfigPath(agentConfigFileName));
        String hostName = DataPublisherTestUtil.LOCAL_HOST;

        DataPublisher dataPublisher = new DataPublisher("Binary", "tcp://" + hostName + ":9631",
                "ssl://" + hostName + ":9731", "admin", "admin");

        String streamID = DataBridgeCommonsUtils.generateStreamId(STREAM_NAME, VERSION);
        Object[] metaData = new Object[]{"127.0.0.1"};
        Object[] correlationData = null;
        Object[] payLoad = new Object[]{"WSO2", 123.4, 2, 12.4, 1.3};
        Map<String, String> arbitrary = new HashMap<String, String>();
        arbitrary.put("test", "testValue");
        arbitrary.put("test1", "test123");
        Long timeStamp = System.currentTimeMillis();

        int numberOfEventsSent = 1000;
        for (int i = 0; i < numberOfEventsSent; i++) {
            dataPublisher.publish(streamID, timeStamp, metaData, correlationData, payLoad, arbitrary);
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        dataPublisher.shutdown();
        Assert.assertEquals(numberOfEventsSent, testServer.getNumberOfEventsReceived());
        testServer.resetReceivedEvents();
        testServer.stop();
    }

    @Test
    public void nonBlockingPublishTest() throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException, DataEndpointException, DataEndpointConfigurationException, MalformedStreamDefinitionException, DataBridgeException, StreamDefinitionStoreException, IOException {
        startServer(9100, 9201);
        AgentHolder.setConfigPath(DataPublisherTestUtil.getDataAgentConfigPath(agentConfigFileName));
        String hostName = DataPublisherTestUtil.LOCAL_HOST;

        DataPublisher dataPublisher = new DataPublisher("Binary", "tcp://" + hostName + ":9100",
                "ssl://" + hostName + ":9201", "admin", "admin");

        Event event = new Event();
        event.setStreamId(DataBridgeCommonsUtils.generateStreamId(STREAM_NAME, VERSION));
        event.setStreamId(DataBridgeCommonsUtils.generateStreamId(STREAM_NAME, VERSION));
        event.setMetaData(new Object[]{"127.0.0.1"});
        event.setCorrelationData(null);
        event.setPayloadData(new Object[]{"WSO2", 123.4, 2, 12.4, 1.3});

        int numberOfEventsSent = 1000;
        for (int i = 0; i < numberOfEventsSent; i++) {
            dataPublisher.tryPublish(event, 1000);
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        dataPublisher.shutdown();
        Assert.assertEquals(numberOfEventsSent, testServer.getNumberOfEventsReceived());
        testServer.resetReceivedEvents();
        testServer.stop();
    }

    @Test
    public void nonBlockingPublishTestOverLoad() throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException, DataEndpointException, DataEndpointConfigurationException, MalformedStreamDefinitionException, DataBridgeException, StreamDefinitionStoreException, IOException {
        startServer(9219, 9319);
        AgentHolder.setConfigPath(DataPublisherTestUtil.getDataAgentConfigPath(agentConfigFileName));
        String hostName = DataPublisherTestUtil.LOCAL_HOST;

        DataPublisher dataPublisher = new DataPublisher("Binary", "tcp://" + hostName + ":9219",
                "ssl://" + hostName + ":9319", "admin", "admin");

        String streamID = DataBridgeCommonsUtils.generateStreamId(STREAM_NAME, VERSION);
        Object[] metaData = new Object[]{"127.0.0.1"};
        Object[] correlationData = null;
        Object[] payLoad = new Object[]{"WSO2", 123.4, 2, 12.4, 1.3};

        int numberOfEventsSent = 1000;
        for (int i = 0; i < numberOfEventsSent; i++) {
            dataPublisher.tryPublish(streamID, metaData, correlationData, payLoad);
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        dataPublisher.shutdown();
        Assert.assertEquals(numberOfEventsSent, testServer.getNumberOfEventsReceived());
        testServer.resetReceivedEvents();
        testServer.stop();
    }

    @Test
    public void nonBlockingPublishTestOverLoadWithArbitaryElementsOfEvent() throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException, DataEndpointException, DataEndpointConfigurationException, MalformedStreamDefinitionException, DataBridgeException, StreamDefinitionStoreException, IOException {
        startServer(9629, 9729);
        AgentHolder.setConfigPath(DataPublisherTestUtil.getDataAgentConfigPath(agentConfigFileName));
        String hostName = DataPublisherTestUtil.LOCAL_HOST;

        DataPublisher dataPublisher = new DataPublisher("Binary", "tcp://" + hostName + ":9651",
                "ssl://" + hostName + ":9751", "admin", "admin");

        String streamID = DataBridgeCommonsUtils.generateStreamId(STREAM_NAME, VERSION);
        Object[] metaData = new Object[]{"127.0.0.1"};
        Object[] correlationData = null;
        Object[] payLoad = new Object[]{"WSO2", 123.4, 2, 12.4, 1.3};
        Map<String, String> arbitrary = new HashMap<String, String>();
        arbitrary.put("test", "testValue");
        arbitrary.put("test1", "test123");


        int numberOfEventsSent = 1000;
        for (int i = 0; i < numberOfEventsSent; i++) {
            dataPublisher.tryPublish(streamID, metaData, correlationData, payLoad, arbitrary);
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        dataPublisher.shutdown();
        testServer.resetReceivedEvents();
        testServer.stop();
    }

    @Test
    public void nonBlockingPublishTestOverLoadWithTimeStampAndArbitaryElementsOfEvent() throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException, DataEndpointException, DataEndpointConfigurationException, MalformedStreamDefinitionException, DataBridgeException, StreamDefinitionStoreException, IOException {
        startServer(9699, 9799);
        AgentHolder.setConfigPath(DataPublisherTestUtil.getDataAgentConfigPath(agentConfigFileName));
        String hostName = DataPublisherTestUtil.LOCAL_HOST;

        DataPublisher dataPublisher = new DataPublisher("Binary", "tcp://" + hostName + ":9699",
                "ssl://" + hostName + ":9799", "admin", "admin");

        String streamID = DataBridgeCommonsUtils.generateStreamId(STREAM_NAME, VERSION);
        Long timeStamp = System.currentTimeMillis();
        Object[] metaData = new Object[]{"127.0.0.1"};
        Object[] correlationData = null;
        Object[] payLoad = new Object[]{"WSO2", 123.4, 2, 12.4, 1.3};
        Map<String, String> arbitrary = new HashMap<String, String>();
        arbitrary.put("test", "testValue");
        arbitrary.put("test1", "test123");


        int numberOfEventsSent = 1000;
        for (int i = 0; i < numberOfEventsSent; i++) {
            dataPublisher.tryPublish(streamID, timeStamp, metaData, correlationData, payLoad, arbitrary);
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        dataPublisher.shutdown();
        testServer.resetReceivedEvents();
        testServer.stop();
    }

}
*/
