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


import org.apache.log4j.Logger;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.databridge.agent.test.thrift.ThriftTestServer;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.thrift.exception.ThriftNoStreamDefinitionExistException;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

/**
 * Legacy Datapublisher Test Case to verify the deprecated methods.
 */
public class LegacyDataPublisherTestCase {
    Logger log = Logger.getLogger(LegacyDataPublisherTestCase.class);
    private static final String STREAM_NAME = "org.wso2.esb.MediatorStatistics";
    private static final String VERSION = "1.0.0";
    private ThriftTestServer testServer;

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


    private synchronized void startServer(int port) throws DataBridgeException,
            StreamDefinitionStoreException, MalformedStreamDefinitionException, IOException {
        testServer = new ThriftTestServer();
        testServer.start(port);
        testServer.addStreamDefinition(STREAM_DEFN);
    }

    @Test
    public void testSendingSameStreamDefinitions() throws DataBridgeException,
            MalformedStreamDefinitionException, StreamDefinitionStoreException, IOException,
            TransportException, AuthenticationException, AgentException,
            DifferentStreamDefinitionAlreadyDefinedException, StreamDefinitionException, InterruptedException {

        startServer(7614);
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7614", "admin", "admin");

        String id1 = dataPublisher.defineStream("{" +
                "  'name':'org.wso2.esb.MediatorStatistics'," +
                "  'version':'2.3.0'," +
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
                "}");

        String id2 = dataPublisher.defineStream("{" +
                "  'name':'org.wso2.esb.MediatorStatistics'," +
                "  'version':'2.3.0'," +
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
                "}");

        AssertJUnit.assertEquals(id1, id2);
        //In this case correlation data is null
        dataPublisher.publish(id1, new Object[]{"127.0.0.1"}, null, new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        dataPublisher.stop();

        testServer.resetReceivedEvents();
        testServer.stop();
    }

    @Test
    public void testSendingSameStreamDefinitionWithAndWithoutVersion() throws DataBridgeException,
            MalformedStreamDefinitionException, StreamDefinitionStoreException, IOException,
            TransportException, AuthenticationException, AgentException,
            DifferentStreamDefinitionAlreadyDefinedException, StreamDefinitionException, InterruptedException {

        startServer(7615);

        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7615", "admin", "admin");
        String streamDef = dataPublisher.defineStream("{" +
                "  'name':'org.wso2.esb.MediatorStatistics1'," +
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
                "}");
        dataPublisher.defineStream("{" +
                "  'name':'org.wso2.esb.MediatorStatistics1'," +
                "  'version':'1.0.0'," +
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
                "}");

        //In this case correlation data is null
        dataPublisher.publish(streamDef, new Object[]{"127.0.0.1"}, null,
                new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        dataPublisher.stop();
        testServer.stop();

    }

    @Test
    public void testSendingSameStreamDefinitionWithAndWithoutVersion2() throws DataBridgeException,
            MalformedStreamDefinitionException, StreamDefinitionStoreException, IOException,
            TransportException, AuthenticationException, AgentException,
            DifferentStreamDefinitionAlreadyDefinedException, StreamDefinitionException, InterruptedException {

        startServer(7616);

        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7616", "admin", "admin");
        String streamDef = dataPublisher.defineStream("{" +
                "  'name':'org.wso2.esb.MediatorStatistics2'," +
                "  'version':'1.0.0'," +
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
                "}");
        dataPublisher.defineStream("{" +
                "  'name':'org.wso2.esb.MediatorStatistics2'," +
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
                "}");

        //In this case correlation data is null
        dataPublisher.publish(streamDef, new Object[]{"127.0.0.1"}, null,
                new Object[]{"IBM", 96.8, 300, 120.6, 70.4});
        dataPublisher.stop();
        testServer.stop();
    }

    @Test
    public void testSendingTwoDifferentStreamDefinitionsWithSameStreamId() throws DataBridgeException,
            MalformedStreamDefinitionException, StreamDefinitionStoreException, IOException,
            TransportException, AuthenticationException, AgentException,
            DifferentStreamDefinitionAlreadyDefinedException, StreamDefinitionException, InterruptedException {

        startServer(7617);

        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7617", "admin", "admin");
        dataPublisher.defineStream("{" +
                "  'name':'org.wso2.esb.MediatorStatistics3'," +
                "  'version':'2.3.0'," +
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
                "}");
        Boolean exceptionOccurred = false;
        try {
            dataPublisher.defineStream("{" +
                    "  'name':'org.wso2.esb.MediatorStatistics3'," +
                    "  'version':'2.3.0'," +
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
                    "          {'name':'min','type':'Double'}" +
                    "  ]" +
                    "}");

        } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
            exceptionOccurred = true;
        }
        AssertJUnit.assertTrue(exceptionOccurred);

        dataPublisher.stop();
        testServer.stop();
    }

    @Test
    public void testDeletingStream() throws DataBridgeException,
            MalformedStreamDefinitionException, StreamDefinitionStoreException, IOException,
            TransportException, AuthenticationException, AgentException,
            DifferentStreamDefinitionAlreadyDefinedException, StreamDefinitionException, InterruptedException {

        startServer(7632);

        //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7632", "admin", "admin");
        String id1 = dataPublisher.defineStream("{" +
                "  'name':'org.wso2.esb.MediatorStatistics'," +
                "  'version':'2.3.0'," +
                "  'nickName': 'Stock Quote Information'," +
                "  'description': 'Some Desc'," +
                "  'tags':['foo', 'bar']," +
                "  'metaData':[" +
                "          {'name':'ipAdd','type':'STRING'}" +
                "  ]," +
                "  'correlationData':[" +
                "          {'name':'correlationId','type':'STRING'}" +
                "  ]," +
                "  'payloadData':[" +
                "          {'name':'symbol','type':'STRING'}," +
                "          {'name':'price','type':'DOUBLE'}," +
                "          {'name':'volume','type':'INT'}," +
                "          {'name':'max','type':'DOUBLE'}," +
                "          {'name':'min','type':'Double'}" +
                "  ]" +
                "}");

        //In this case correlation data is null
        dataPublisher.publish(id1, new Object[]{"127.0.0.1"}, new Object[]{"HD34"},
                new Object[]{"IBM", 96.8, 300, 120.6, 70.4});

        await().atLeast(2000, TimeUnit.MILLISECONDS);

        if (!dataPublisher.deleteStream(id1)) {
            AssertJUnit.fail("Stream not deleted");
        }
        boolean pass = false;
        try {
            dataPublisher.findStreamId("org.wso2.esb.MediatorStatistics", "2.3.0");
        } catch (AgentException e) {
            if (e.getCause().getCause() instanceof ThriftNoStreamDefinitionExistException) {
                pass = true;
            }
        }
        AssertJUnit.assertEquals(true, pass);

        id1 = dataPublisher.defineStream("{" +
                "  'name':'org.wso2.esb.MediatorStatistics'," +
                "  'version':'2.3.0'," +
                "  'nickName': 'Stock Quote Information'," +
                "  'description': 'Some Desc'," +
                "  'tags':['foo', 'bar']," +
                "  'metaData':[" +
                "          {'name':'ipAdd','type':'STRING'}" +
                "  ]," +
                "  'correlationData':[" +
                "          {'name':'correlationId','type':'STRING'}" +
                "  ]," +
                "  'payloadData':[" +
                "          {'name':'symbol','type':'STRING'}," +
                "          {'name':'price','type':'DOUBLE'}," +
                "          {'name':'volume','type':'INT'}," +
                "          {'name':'max','type':'DOUBLE'}," +
                "          {'name':'min','type':'Double'}" +
                "  ]" +
                "}");

        //In this case correlation data is null
        dataPublisher.publish(id1, new Object[]{"127.0.0.1"}, new Object[]{"HD34"},
                new Object[]{"WSO2", 96.8, 300, 120.6, 70.4});
        await().atLeast(2000, TimeUnit.MILLISECONDS);

        dataPublisher.stop();
        testServer.stop();
    }


    @Test(expectedExceptions = MalformedStreamDefinitionException.class)
    public void testMalformedStreamDefinition() throws DataBridgeException,
            MalformedStreamDefinitionException, StreamDefinitionStoreException, IOException,
            TransportException, AuthenticationException, AgentException,
            DifferentStreamDefinitionAlreadyDefinedException, StreamDefinitionException, InterruptedException {

        startServer(7614);
        DataPublisher dataPublisher = new DataPublisher("tcp://localhost:7614", "admin", "admin");

        dataPublisher.defineStream("{" +
                "  'name':'org.wso2.esb.MediatorStatistics'," +
                "  'version':'2.3.0'," +
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
                "          {'name':'min','type':'Double'" +
                "  ]" +
                "}");

        dataPublisher.stop();

        testServer.resetReceivedEvents();
        testServer.stop();
    }

}
