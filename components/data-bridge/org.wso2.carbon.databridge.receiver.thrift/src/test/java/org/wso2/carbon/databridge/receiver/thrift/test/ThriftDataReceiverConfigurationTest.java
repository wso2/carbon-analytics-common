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

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.databridge.core.conf.DataBridgeConfiguration;
import org.wso2.carbon.databridge.receiver.thrift.conf.ThriftDataReceiverConfiguration;
import org.wso2.carbon.databridge.receiver.thrift.test.util.ThriftServerUtil;

import java.io.IOException;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

public class ThriftDataReceiverConfigurationTest {

    @Test
    public void testConfigurationConstructorOne() throws IOException , XMLStreamException , JAXBException {
        ThriftServerUtil.setupCarbonConfig("test");
        DataBridgeConfiguration dataBridgeConfiguration = ThriftServerUtil.
                getDataBridgeConfiguration("data-bridge-config.xml");

        ThriftDataReceiverConfiguration config = new ThriftDataReceiverConfiguration(dataBridgeConfiguration);
        Assert.assertEquals(7711, config.getSecureDataReceiverPort());
        Assert.assertEquals(7611, config.getDataReceiverPort());
        Assert.assertEquals("10.101.10.101", config.getReceiverHostName());
        Assert.assertEquals(10, config.getWaitingTimeInMilliSeconds());
        Assert.assertEquals("TLSv1", config.getSslProtocols());
        Assert.assertEquals("SSL_RSA_WITH_RC4_128_MD5,SSL_RSA_WITH_RC4_128_SHA,SSL_RSA_WITH_DES_CBC_SHA," +
                "SSL_RSA_WITH_3DES_EDE_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA", config.getCiphers());
        Assert.assertEquals(20, config.getTcpMaxWorkerThreads());
        Assert.assertEquals(5, config.getTcpMinWorkerThreads());
        Assert.assertEquals(10, config.getTcpRequestTimeout());
        Assert.assertEquals(50, config.getTcpStopTimeoutVal());
        Assert.assertEquals(20, config.getSslMaxWorkerThreads());
        Assert.assertEquals(5, config.getSslMinWorkerThreads());
        Assert.assertEquals(10, config.getSslRequestTimeout());
        Assert.assertEquals(50, config.getSslStopTimeoutVal());
    }

    @Test
    public void testConfigurationSetterMethods() {
        ThriftDataReceiverConfiguration config = new ThriftDataReceiverConfiguration(7711, 7611);
        Assert.assertEquals(7711, config.getSecureDataReceiverPort());
        Assert.assertEquals(7611, config.getDataReceiverPort());

        config.setSecureDataReceiverPort(7712);
        config.setDataReceiverPort(7612);
        config.setReceiverHostName("10.101.10.102");
        config.setSslProtocols("ssl2");
        config.setCiphers("cipher2");
        config.setTcpMaxWorkerThreads(21);
        config.setTcpMinWorkerThreads(6);
        config.setTcpRequestTimeout(11);
        config.setTcpStopTimeoutVal(51);
        config.setSslMaxWorkerThreads(21);
        config.setSslMinWorkerThreads(6);
        config.setSslRequestTimeout(11);
        config.setSslStopTimeoutVal(51);

        Assert.assertEquals(7712, config.getSecureDataReceiverPort());
        Assert.assertEquals(7612, config.getDataReceiverPort());
        Assert.assertEquals("10.101.10.102", config.getReceiverHostName());
        Assert.assertEquals("ssl2", config.getSslProtocols());
        Assert.assertEquals("cipher2", config.getCiphers());
        Assert.assertEquals(21, config.getTcpMaxWorkerThreads());
        Assert.assertEquals(6, config.getTcpMinWorkerThreads());
        Assert.assertEquals(11, config.getTcpRequestTimeout());
        Assert.assertEquals(51, config.getTcpStopTimeoutVal());
        Assert.assertEquals(21, config.getSslMaxWorkerThreads());
        Assert.assertEquals(6, config.getSslMinWorkerThreads());
        Assert.assertEquals(11, config.getSslRequestTimeout());
        Assert.assertEquals(51, config.getSslStopTimeoutVal());
    }

    @Test
    public void testConfigurationConstructorTwo() {
        ThriftDataReceiverConfiguration config = new ThriftDataReceiverConfiguration(7711, 7611);
        Assert.assertEquals(7711, config.getSecureDataReceiverPort());
        Assert.assertEquals(7611, config.getDataReceiverPort());
    }

    @Test
    public void testConfigurationConstructorThree() {
        ThriftDataReceiverConfiguration config = new ThriftDataReceiverConfiguration(7711, 7611, "localhost");
        Assert.assertEquals(7711, config.getSecureDataReceiverPort());
        Assert.assertEquals(7611, config.getDataReceiverPort());
        Assert.assertEquals("localhost", config.getReceiverHostName());
    }

}
