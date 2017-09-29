/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.databridge.core;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.IndexDefinitionConstants;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.*;
import org.wso2.carbon.databridge.core.Utils.AgentSession;
import org.wso2.carbon.databridge.core.definitionstore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.databridge.core.definitionstore.StreamAddRemoveListener;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.core.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;


@RunWith(PowerMockRunner.class)
@PrepareForTest(DataBridgeTest.class)
public class DataBridgeTest {
    private static final int TENANT_ID = -1234;

    private AuthenticationHandler dummyAuthenticator = new AuthenticationHandler() {
        @Override
        public boolean authenticate(String userName, String password) {
            return (userName.equals("user") && password.equals("password"));
        }

        @Override
        public String getTenantDomain(String userName) {
            return "org.test.com";
        }

        @Override
        public int getTenantId(String tenantDomain) throws UserStoreException {
            return TENANT_ID;
        }

        @Override
        public void initContext(AgentSession agentSession) {

        }

        @Override
        public void destroyContext(AgentSession agentSession) {

        }
    };

    InMemoryStreamDefinitionStore streamDefinitionStore = new InMemoryStreamDefinitionStore();

    public static String getDataBridgeConfigPath() {
        File filePath = new File("src" + File.separator + "test" + File.separator + "resources");
        if (!filePath.exists()) {
            filePath = new File("components" + File.separator + "data-bridge" + File.separator + "org.wso2.carbon.databridge.agent" + File.separator + "src" + File.separator + "test" + File.separator + "resources");
        }
        if (!filePath.exists()) {
            filePath = new File("resources");
        }
        if (!filePath.exists()) {
            filePath = new File("test" + File.separator + "resources");
        }
        return filePath.getAbsolutePath() + File.separator + "data-bridge-config.xml";
    }

    private static String definition = "{" +
            "  'name':'org.wso2.esb.MediatorStatistics'," +
            "  'version':'2.3.0'," +
            "  'nickName': 'Stock Quote Information'," +
            "  'description': 'Some Desc'," +
            "  'tags':['foo', 'bar']," +
            "  'metaData':[" +
            "          {'name':'ipAdd','type':'STRING'}" +
            "  ]," +
            "  'payloadData':[" +
            "          {'name':'symbol','type':'string'}," +
            "          {'name':'price','type':'double'}," +
            "          {'name':'volume','type':'int'}," +
            "          {'name':'maxTemp','type':'double'}," +
            "          {'name':'minTemp','type':'double'}" +
            "  ]," +
            "  'correlationData':[" +
            "          {'name':'location','type':'STRING'}" +
            "  ]" +
            "}";

    private static String differentDefinition = "{" +
            "  'name':'org.wso2.esb.MediatorStatistics'," +
            "  'version':'2.3.0'," +
            "  'nickName': 'Stock Quote Information'," +
            "  'description': 'Some Desc'," +
            "  'tags':['foo', 'bar']," +
            "  'metaData':[" +
            "          {'name':'ipAdd','type':'STRING'}" +
            "  ]," +
            "  'payloadData':[" +
            "          {'name':'symbol','type':'string'}," +
            "          {'name':'price','type':'double'}," +
            "          {'name':'volume','type':'int'}," +
            "          {'name':'minTemp','type':'double'}" +
            "  ]," +
            "  'correlationData':[" +
            "          {'name':'location','type':'STRING'}" +
            "  ]" +
            "}";

    private static String indexDefinition = "[" + IndexDefinitionConstants.FIXED_INDEX + ":=" + "symbol]";

    private static boolean isStreamAdded = false;
    private static boolean isStreamRemoved = false;

    StreamAddRemoveListener listener = new StreamAddRemoveListener() {
        @Override
        public void streamAdded(int tenantId, String streamId) {
            isStreamAdded = true;
        }

        @Override
        public void streamRemoved(int tenantId, String streamId) {
            isStreamRemoved = true;
        }
    };


    @Test
    public void testDataBridgeDefineStream() throws AuthenticationException, MalformedStreamDefinitionException,
            SessionTimeoutException, DifferentStreamDefinitionAlreadyDefinedException,
            StreamDefinitionNotFoundException, StreamDefinitionStoreException {

        isStreamAdded = false;
        DataBridge dataBridge = new DataBridge(dummyAuthenticator, streamDefinitionStore, getDataBridgeConfigPath());
        String sessionId = dataBridge.login("user", "password");
        dataBridge.subscribe(listener);
        dataBridge.defineStream(sessionId, definition);

        Assert.assertTrue(isStreamAdded);
        Assert.assertTrue(dataBridge.getAllStreamDefinitions(TENANT_ID).size() == 1);
        Assert.assertTrue(dataBridge.getAllStreamDefinitions(sessionId).size() == 1);
        Assert.assertTrue(dataBridge.getStreamDefinition(sessionId, "org.wso2.esb.MediatorStatistics", "2.3.0") != null);

        // Stream definition with index definition
        dataBridge.defineStream(sessionId, definition, indexDefinition);
        Assert.assertTrue(dataBridge.getAllStreamDefinitions(TENANT_ID).size() == 1);
        Assert.assertTrue(dataBridge.getAllStreamDefinitions(sessionId).size() == 1);
        Assert.assertTrue(dataBridge.getStreamDefinition(sessionId, "org.wso2.esb.MediatorStatistics", "2.3.0") != null);
    }

    @Test
    public void testDataBridgeDefineStreamExceptions() throws AuthenticationException,
            DifferentStreamDefinitionAlreadyDefinedException, SessionTimeoutException, MalformedStreamDefinitionException {

        DataBridge dataBridge = new DataBridge(dummyAuthenticator, streamDefinitionStore, getDataBridgeConfigPath());
        String sessionId = dataBridge.login("user", "password");

        //Define stream with malformed definition
        boolean isException = false;
        try {
            dataBridge.defineStream(sessionId, definition.replace("{", ""));
        } catch (MalformedStreamDefinitionException e){
            isException = true;
        }
        Assert.assertTrue(isException);

        //Define stream with malformed definition with index definition
        isException = false;
        try {
            dataBridge.defineStream(sessionId, definition.replace("{", ""), indexDefinition);
        } catch (MalformedStreamDefinitionException e){
            isException = true;
        }
        Assert.assertTrue(isException);

        // Defining the same stream definition twice
        isException = false;
        try {
            dataBridge.defineStream(sessionId, definition);
            dataBridge.defineStream(sessionId, differentDefinition);
        } catch (DifferentStreamDefinitionAlreadyDefinedException e){
            isException = true;
        }
        Assert.assertTrue(isException);

        isException = false;
        try {
            dataBridge.defineStream(sessionId, definition, indexDefinition);
            dataBridge.defineStream(sessionId, differentDefinition, indexDefinition);
        } catch (DifferentStreamDefinitionAlreadyDefinedException e){
            isException = true;
        }
        Assert.assertTrue(isException);

        // Define stream with a invalid sessionId
        isException = false;
        try {
            dataBridge.defineStream("invalidSession", definition);
        } catch (SessionTimeoutException e){
            isException = true;
        }
        Assert.assertTrue(isException);

        // Define stream with a invalid sessionId
        isException = false;
        try {
            dataBridge.defineStream("invalidSession", definition, indexDefinition);
        } catch (SessionTimeoutException e){
            isException = true;
        }
        Assert.assertTrue(isException);
    }

    @Test
    public void testDataBridgeFindStream() throws AuthenticationException,
            MalformedStreamDefinitionException, SessionTimeoutException, DifferentStreamDefinitionAlreadyDefinedException {
        DataBridge dataBridge = new DataBridge(dummyAuthenticator, streamDefinitionStore, getDataBridgeConfigPath());
        String sessionId = dataBridge.login("user", "password");
        dataBridge.defineStream(sessionId, definition);

        String streamId = dataBridge.findStreamId(sessionId, "org.wso2.esb.MediatorStatistics", "2.3.0");
        Assert.assertTrue(streamId != null);

        streamId = dataBridge.findStreamId(sessionId, "invalid.stream.id", "2.3.0");
        Assert.assertTrue(streamId == null);

        boolean isException = false;
        try {
            dataBridge.findStreamId("invalidSessionId", "org.wso2.esb.MediatorStatistics", "2.3.0");
        } catch (SessionTimeoutException e){
            isException = true;
        }
        Assert.assertTrue(isException);
    }

    @Test
    public void testDataBridgeDeleteStream() throws AuthenticationException,
            MalformedStreamDefinitionException, SessionTimeoutException, DifferentStreamDefinitionAlreadyDefinedException {
        isStreamRemoved = false;
        DataBridge dataBridge = new DataBridge(dummyAuthenticator, streamDefinitionStore, getDataBridgeConfigPath());
        String sessionId = dataBridge.login("user", "password");
        dataBridge.defineStream(sessionId, definition);
        dataBridge.subscribe(listener);
        boolean status = dataBridge.deleteStream(sessionId, "org.wso2.esb.MediatorStatistics", "2.3.0");
        Assert.assertTrue(status);
        Assert.assertTrue(isStreamRemoved);

        // Delete invalid stream
        status = dataBridge.deleteStream(sessionId, "invalid.stream.id", "2.3.0");
        Assert.assertTrue(status == false);


        boolean isException = false;
        try {
            dataBridge.deleteStream("invalidSessionId", "org.wso2.esb.MediatorStatistics", "2.3.0");
        } catch (SessionTimeoutException e){
            isException = true;
        }
        Assert.assertTrue(isException);
    }

    @Test
    public void testGetStreamDefinitionForInvalidSession() throws AuthenticationException,
            MalformedStreamDefinitionException, SessionTimeoutException, DifferentStreamDefinitionAlreadyDefinedException, StreamDefinitionNotFoundException, StreamDefinitionStoreException {
        DataBridge dataBridge = new DataBridge(dummyAuthenticator, streamDefinitionStore, getDataBridgeConfigPath());
        String sessionId = dataBridge.login("user", "password");
        dataBridge.defineStream(sessionId, definition);

        // With invalid session
        boolean isException = false;
        try {
            dataBridge.getAllStreamDefinitions("InvalidSession");
        } catch (SessionTimeoutException e){
            isException = true;
        }
        Assert.assertTrue(isException);

        // With invalid session with stream id and stream version
        isException = false;
        try {
            dataBridge.getStreamDefinition("InvalidSession", "org.wso2.esb.MediatorStatistics", "2.3.0");
        } catch (SessionTimeoutException e){
            isException = true;
        }
        Assert.assertTrue(isException);
    }

    @Test
    public void testLoginLogout() throws Exception {
        DataBridge dataBridge = new DataBridge(dummyAuthenticator, streamDefinitionStore, getDataBridgeConfigPath());

        //Valid login
        String sessionId = dataBridge.login("user", "password");
        Assert.assertTrue(sessionId != null);

        //Invalid login
        boolean isException = false;
        try {
        sessionId = dataBridge.login("unknownUser", "wrongPassword");
        } catch (Exception e) {
            isException = true;
        }
        Assert.assertTrue(isException);

        //Logout with invalid session ID
        dataBridge.defineStream(sessionId, definition);
        dataBridge.logout("InvalidSessionId");
        Assert.assertTrue(dataBridge.getAllStreamDefinitions(sessionId).size() == 1);

        //Logout with valid session
        dataBridge.logout(sessionId);
        try {
            dataBridge.getAllStreamDefinitions(sessionId);
        } catch (SessionTimeoutException e) {
            isException = true;
        }
        Assert.assertTrue(isException);
    }

    @Test
    public void testSaveStreamDefinition() throws MalformedStreamDefinitionException, AuthenticationException, SessionTimeoutException, StreamDefinitionStoreException, DifferentStreamDefinitionAlreadyDefinedException {
        StreamDefinition streamDefinition = new StreamDefinition("org.wso2.esb.MediatorStatistics", "2.3.0");
        streamDefinition.addCorrelationData("correlationField", AttributeType.STRING);
        streamDefinition.addCorrelationData("payloadField", AttributeType.STRING);
        streamDefinition.addCorrelationData("metaField", AttributeType.STRING);

        DataBridge dataBridge = new DataBridge(dummyAuthenticator, streamDefinitionStore, getDataBridgeConfigPath());
        String sessionId = dataBridge.login("user", "password");
        dataBridge.saveStreamDefinition(sessionId, streamDefinition);
        Assert.assertTrue(dataBridge.getAllStreamDefinitions(sessionId).size() == 1);

        // Saving with invalid session
        boolean isException = false;
        try {
        dataBridge.saveStreamDefinition("invalidSession", streamDefinition);
        } catch (SessionTimeoutException e) {
            isException = true;
        }
        Assert.assertTrue(isException);
    }

    @Test
    public void testDataBridgePublishWithProfileReceiver() throws AuthenticationException,
            MalformedStreamDefinitionException, SessionTimeoutException, DifferentStreamDefinitionAlreadyDefinedException, UndefinedEventTypeException {
        System.setProperty("profileReceiver", "true");
        System.setProperty("carbon.home", ".");
        DataBridge dataBridge = new DataBridge(dummyAuthenticator, streamDefinitionStore, getDataBridgeConfigPath());
        String sessionId = dataBridge.login("user", "password");
        dataBridge.defineStream(sessionId, definition);

        EventConverter converter = PowerMockito.mock(EventConverter.class);
        when(converter.getNumberOfEvents(anyObject())).thenReturn(200000);

        dataBridge.publish(new Object(), sessionId, converter);
        File file = new File(CarbonUtils.getCarbonHome() + File.separator + "receiver-perf.txt");
        Assert.assertTrue(file.exists());
        Assert.assertTrue(file.delete());
    }

    @Test(expected = SessionTimeoutException.class)
    public void testDataBridgeWithInvalidSessionId() throws AuthenticationException, MalformedStreamDefinitionException, SessionTimeoutException, DifferentStreamDefinitionAlreadyDefinedException, UndefinedEventTypeException {
        DataBridge dataBridge = new DataBridge(dummyAuthenticator, streamDefinitionStore, getDataBridgeConfigPath());
        String sessionId = dataBridge.login("user", "password");
        dataBridge.defineStream(sessionId, definition);
        dataBridge.publish(new Object(), "invalidSession", null);
    }
}
