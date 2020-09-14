/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.databridge.receiver.binary.test;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.binary.BinaryMessageConstants;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.SessionTimeoutException;
import org.wso2.carbon.databridge.core.DataBridge;
import org.wso2.carbon.databridge.core.Utils.AgentSession;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.databridge.core.definitionstore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.core.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.databridge.receiver.binary.conf.BinaryDataReceiverConfiguration;
import org.wso2.carbon.databridge.receiver.binary.internal.BinaryDataReceiver;
import org.wso2.carbon.databridge.receiver.binary.test.util.BinaryServerUtil;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.databridge.commons.binary.BinaryMessageConverterUtil.loadData;

public class BinaryDataReceiverTest {

    private static final int TENANT_ID = -1234;
    private static final String STREAM_NAME = "org.wso2.esb.MediatorStatistics";
    private static final String VERSION = "1.0.0";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";
    private static final String INVALID_SESSION_STRING = "invalidSessionString";

    private static DataBridge dataBridge;
    private static BinaryDataReceiverConfiguration dataReceiverConfiguration;

    @BeforeClass
    public static void initDataBridge() throws DifferentStreamDefinitionAlreadyDefinedException,
            StreamDefinitionStoreException, JAXBException, XMLStreamException, IOException {
        BinaryServerUtil.setupCarbonConfig("testTenant");
        StreamDefinition streamDefinition = BinaryServerUtil.getSampleStreamDefinition(STREAM_NAME, VERSION);
        AbstractStreamDefinitionStore streamDefinitionStore = new InMemoryStreamDefinitionStore();
        streamDefinitionStore.saveStreamDefinition(streamDefinition, TENANT_ID);
        dataBridge = new DataBridge(new AuthenticationHandler() {
            @Override
            public boolean authenticate(String userName,
                                        String password) {
                return true;// always authenticate to true
            }

            @Override
            public String getTenantDomain(String userName) {
                return "admin";
            }

            @Override
            public int getTenantId(String tenantDomain) throws UserStoreException {
                return -1234;
            }

            @Override
            public void initContext(AgentSession agentSession) {
            }

            @Override
            public void destroyContext(AgentSession agentSession) {

            }
        }, streamDefinitionStore, BinaryServerUtil.testDir + File.separator + "data-bridge-config.xml");

        dataReceiverConfiguration = new BinaryDataReceiverConfiguration(BinaryServerUtil.
                getDataBridgeConfiguration("data-bridge-config.xml"));
    }

    @Test(expected = DataBridgeException.class)
    public void testReceiverWithNoKeyStoreLocation() throws IOException, DataBridgeException, InterruptedException {

        System.clearProperty("Security.KeyStore.Location");
        BinaryDataReceiver binaryDataReceiver = new BinaryDataReceiver(dataReceiverConfiguration, dataBridge);
        binaryDataReceiver.start();
        Thread.sleep(1000);
        binaryDataReceiver.stop();
    }

    @Test(expected = DataBridgeException.class)
    public void testReceiverWithNoKeyPassword() throws IOException, DataBridgeException, InterruptedException {

        System.clearProperty("Security.KeyStore.Password");
        System.setProperty("Security.KeyStore.Location", BinaryServerUtil.testDir + File.separator + "wso2carbon.jks");
        BinaryDataReceiver binaryDataReceiver = new BinaryDataReceiver(dataReceiverConfiguration, dataBridge);
        binaryDataReceiver.start();
        Thread.sleep(1000);
        binaryDataReceiver.stop();
    }

    @Test
    public void testReceiverConnection() throws IOException, DataBridgeException, InterruptedException {

        System.setProperty("Security.KeyStore.Location", BinaryServerUtil.testDir + File.separator + "wso2carbon.jks");
        System.setProperty("Security.KeyStore.Password", "wso2carbon");

        System.setProperty("javax.net.ssl.trustStore", BinaryServerUtil.testDir + File.separator + "client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "jks");

        Event event = BinaryServerUtil.buildEvent(STREAM_NAME, VERSION);

        BinaryDataReceiver binaryDataReceiver = new BinaryDataReceiver(dataReceiverConfiguration, dataBridge);
        binaryDataReceiver.start();
        Thread.sleep(1000);
        SocketAddress tcpSockAddress = new InetSocketAddress("127.0.0.1", 9611);
        SocketAddress sslSockAddress = new InetSocketAddress("127.0.0.1", 9711);
        Socket socketTCP = new Socket();
        Socket socketSSL = new Socket();
        socketTCP.connect(tcpSockAddress);
        socketSSL.connect(sslSockAddress);

        OutputStream socketOutputStream = new BufferedOutputStream(socketTCP.getOutputStream());
        InputStream socketInputStream = socketTCP.getInputStream();
        BufferedInputStream bufferedInputStream = new BufferedInputStream(socketInputStream);

        //Sending event without valid session Id
        byte[] eventByteArray = BinaryServerUtil.convertEventToByteArray(event, INVALID_SESSION_STRING);
        socketOutputStream.write(eventByteArray);
        socketOutputStream.flush();

        int messageType = bufferedInputStream.read();
        Assert.assertEquals(1, messageType); //Message consumed failed return type 1

        ByteBuffer failedReturnMessage = ByteBuffer.wrap(loadData(bufferedInputStream, new byte[8]));
        int errorClassNameLength = failedReturnMessage.getInt();
        int errorMsgLength = failedReturnMessage.getInt();
        String className = new String(ByteBuffer.wrap(loadData(bufferedInputStream,
                new byte[errorClassNameLength])).array());
        String errorMsg = new String(ByteBuffer.wrap(loadData(bufferedInputStream, new byte[errorMsgLength])).array());

        Assert.assertEquals(SessionTimeoutException.class.getCanonicalName(), className);

        //Sending login message to retrieve session Id to send event
        ByteBuffer loginMessage = ByteBuffer.allocate(13 + USERNAME.length() + PASSWORD.length());
        loginMessage.put((byte) 0);
        loginMessage.putInt(8 + USERNAME.length() + PASSWORD.length());
        loginMessage.putInt(USERNAME.length());
        loginMessage.putInt(PASSWORD.length());
        loginMessage.put(USERNAME.getBytes(BinaryMessageConstants.DEFAULT_CHARSET));
        loginMessage.put(PASSWORD.getBytes(BinaryMessageConstants.DEFAULT_CHARSET));
        socketOutputStream.write(loginMessage.array());
        socketOutputStream.flush();

        messageType = bufferedInputStream.read();
        Assert.assertEquals(2, messageType); //Login success return type 2

        ByteBuffer loginReturnMessage = ByteBuffer.wrap(loadData(bufferedInputStream, new byte[4]));
        int sessionIdLength = loginReturnMessage.getInt();
        String sessionId = new String(ByteBuffer.wrap(loadData(bufferedInputStream,
                new byte[sessionIdLength])).array());

        eventByteArray = BinaryServerUtil.convertEventToByteArray(event, sessionId);
        socketOutputStream.write(eventByteArray);
        socketOutputStream.flush();

        messageType = bufferedInputStream.read();
        Assert.assertEquals(0, messageType); //Message consumed success return type 0

        //Sending logout message
        ByteBuffer buf = ByteBuffer.allocate(9 + sessionId.length());
        buf.put((byte) 1);
        buf.putInt(4 + sessionId.length());
        buf.putInt(sessionId.length());
        buf.put(sessionId.getBytes(BinaryMessageConstants.DEFAULT_CHARSET));

        socketOutputStream.write(buf.array());
        socketOutputStream.flush();

        messageType = bufferedInputStream.read();
        Assert.assertEquals(0, messageType); //Message consumed success return type 0

        socketTCP.close();
        socketSSL.close();
        binaryDataReceiver.stop();
    }
}
