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

package org.wso2.carbon.databridge.agent.test.thrift;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.endpoint.DataEndpointConnectionWorker;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.agent.test.DataPublisherTestUtil;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class implemented to test LoggingControlIntervalInSeconds Configuration.
 */
public class LoggingControlIntervalConfigTest {
    Log log = LogFactory.getLog(OneEndPointDPThriftTest.class);
    private static final String STREAM_NAME = "org.wso2.esb.MediatorStatistics";
    private static final String VERSION = "1.0.0";
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

    @BeforeClass public static void init() {
        DataPublisherTestUtil.setKeyStoreParams();
        DataPublisherTestUtil.setTrustStoreParams();
    }

    @Test public void testLoggingControlInterval()
            throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException,
            DataEndpointException, DataEndpointConfigurationException, MalformedStreamDefinitionException,
            DataBridgeException, StreamDefinitionStoreException, IOException {

        log.info("Test scenario : LoggingControlIntervalInSeconds set to 500 seconds ");

        Logger testLogger = (Logger) LogManager.getLogger(DataEndpointConnectionWorker.class);
        UnitTestAppender testApender = new UnitTestAppender("UnitTestAppender",null,null);
        testLogger.addAppender(testApender);

        AgentHolder.setConfigPath(DataPublisherTestUtil.getDataAgentConfigPath(agentConfigFileName));
        String hostName = DataPublisherTestUtil.LOCAL_HOST;
        DataPublisher dataPublisher = new DataPublisher("Thrift", "ssl://" + hostName + ":7711",
                "ssl://" + hostName + ":7711", "admin", "admin");
        Event event = new Event();
        event.setStreamId(DataBridgeCommonsUtils.generateStreamId(STREAM_NAME, VERSION));
        event.setMetaData(new Object[] { "127.0.0.1" });
        event.setCorrelationData(null);
        event.setPayloadData(new Object[] { "WSO2", 123.4, 2, 12.4, 1.3 });

        int numberOfEventsSent = 3;
        for (int i = 0; i < numberOfEventsSent; i++) {
            dataPublisher.publish(event);
        }

        int numberOfErrorMessage = 0;
        List<String> logMessageList = new ArrayList<>();
        try {
            Thread.sleep(60000);
            dataPublisher.shutdownWithAgent();
            logMessageList.addAll(testApender.getLogMessageList());
        } catch (InterruptedException e) {
            if (log.isDebugEnabled()) {
                log.debug("Thread is interrupted while waiting.");
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while shutdown the data publisher ");
            }
        }

        for (String logMessage : testApender.getLogMessageList()) {
            if (logMessage.contains("Error while trying to connect to the endpoint.")) {
                numberOfErrorMessage++;
            }
        }

        //since it wait 60 seconds, according to data bridge default behaviour it should log the error message 3 times.
        //But, we configure loggingControlInterval to 300 times it will print only one time.
        Assert.assertTrue(numberOfErrorMessage < 2);
    }

    //Inner class implement AppenderSkeleton
    public class UnitTestAppender extends AbstractAppender {
        private List<String> logMessageList = new ArrayList<>();

        public UnitTestAppender(String name, Filter filter, Layout<? extends Serializable> layout) {

            super(name, filter, layout);
        }

        @Override public void append(LogEvent loggingEvent) {
            logMessageList.add(loggingEvent.getMessage().getFormattedMessage());
        }


        public List<String> getLogMessageList() {
            return this.logMessageList;
        }

    }

}
