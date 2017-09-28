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

package org.wso2.carbon.databridge.receiver.thrift.test;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.core.DataBridge;
import org.wso2.carbon.databridge.core.Utils.AgentSession;
import org.wso2.carbon.databridge.core.conf.DataBridgeConfiguration;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.databridge.core.definitionstore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.core.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.databridge.receiver.thrift.ThriftDataReceiver;
import org.wso2.carbon.databridge.receiver.thrift.ThriftDataReceiverFactory;
import org.wso2.carbon.databridge.receiver.thrift.conf.ThriftDataReceiverConfiguration;
import org.wso2.carbon.databridge.receiver.thrift.test.util.ThriftServerUtil;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

public class ThriftDataReceiverTest {

    private static Logger log = Logger.getLogger(ThriftDataReceiverTest.class);

    private static final int TENANT_ID = -1234;
    private static final String STREAM_NAME = "org.wso2.esb.MediatorStatistics";
    private static final String VERSION = "1.0.0";

    private static DataBridge dataBridge;

    @BeforeClass
    public static void initDataBridge() throws DifferentStreamDefinitionAlreadyDefinedException,
            StreamDefinitionStoreException {
        StreamDefinition streamDefinition = ThriftServerUtil.getSampleStreamDefinition(STREAM_NAME, VERSION);
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
        }, streamDefinitionStore, ThriftServerUtil.testDir + File.separator + "data-bridge-config.xml");
    }

    @Test(expected = DataBridgeException.class)
    public void testReceiverWithNoKeyStoreLocation() throws DataBridgeException, InterruptedException {

        System.clearProperty("Security.KeyStore.Location");
        ThriftDataReceiver thriftDataReceiver = new ThriftDataReceiver(7611, dataBridge);
        thriftDataReceiver.start("localhost");
        Thread.sleep(1000);
        thriftDataReceiver.stop();
    }

    @Test(expected = DataBridgeException.class)
    public void testReceiverWithNoKeyPassword() throws DataBridgeException, InterruptedException {

        System.clearProperty("Security.KeyStore.Password");
        System.setProperty("Security.KeyStore.Location", ThriftServerUtil.testDir + File.separator + "wso2carbon.jks");
        ThriftDataReceiver thriftDataReceiver = new ThriftDataReceiver(7611, dataBridge);
        thriftDataReceiver.start("localhost");
        Thread.sleep(1000);
        thriftDataReceiver.stop();
    }

    @Test
    public void testReceiver() {

        System.setProperty("Security.KeyStore.Location", ThriftServerUtil.testDir + File.separator + "wso2carbon.jks");
        System.setProperty("Security.KeyStore.Password", "wso2carbon");
        ThriftDataReceiver thriftDataReceiver = new ThriftDataReceiver(7611, dataBridge);

        try {
            thriftDataReceiver.start("localhost");
            Thread.sleep(1000);
            thriftDataReceiver.stop();
        } catch (DataBridgeException e) {
            log.error("Error starting data bridge thrift receiver");
        } catch (InterruptedException e) {
            log.error("Error in waiting for thrift data receiver to stop");
        }
    }

    @Test
    public void testReceiverFactory() throws JAXBException, XMLStreamException, IOException, DataBridgeException,
            InterruptedException {

        ThriftServerUtil.setupCarbonConfig("testTenant");
        System.setProperty("Security.KeyStore.Location", ThriftServerUtil.testDir + File.separator + "wso2carbon.jks");
        System.setProperty("Security.KeyStore.Password", "wso2carbon");

        ThriftDataReceiverFactory receiverFactory = new ThriftDataReceiverFactory();
        ThriftDataReceiver receiverOne = receiverFactory.createAgentServer(7611, dataBridge);
        ThriftDataReceiver receiverTwo = receiverFactory.createAgentServer(7711, 7611, dataBridge);
        DataBridgeConfiguration dataBridgeConfiguration = ThriftServerUtil.
                getDataBridgeConfiguration("data-bridge-config.xml");

        ThriftDataReceiverConfiguration config = new ThriftDataReceiverConfiguration(dataBridgeConfiguration);
        ThriftDataReceiver receiverThree = receiverFactory.createAgentServer(config, dataBridge);

        receiverOne.start("localhost");
        Thread.sleep(1000);
        receiverOne.stop();
        receiverTwo.start("localhost");
        Thread.sleep(1000);
        receiverTwo.stop();
        receiverThree.start("localhost",10);
        Thread.sleep(1000);
        receiverThree.stop();
    }
}
