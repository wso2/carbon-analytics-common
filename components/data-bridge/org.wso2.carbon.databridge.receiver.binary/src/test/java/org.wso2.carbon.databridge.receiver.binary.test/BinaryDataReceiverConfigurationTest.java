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
import org.junit.Test;
import org.wso2.carbon.databridge.core.conf.DataBridgeConfiguration;
import org.wso2.carbon.databridge.receiver.binary.conf.BinaryDataReceiverConfiguration;
import org.wso2.carbon.databridge.receiver.binary.test.util.BinaryServerUtil;

import java.io.IOException;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

public class BinaryDataReceiverConfigurationTest {

    @Test
    public void testConfigurationConstructorOne() throws IOException, XMLStreamException, JAXBException {
        BinaryServerUtil.setupCarbonConfig("test");
        DataBridgeConfiguration dataBridgeConfiguration = BinaryServerUtil.
                getDataBridgeConfiguration("data-bridge-config.xml");

        BinaryDataReceiverConfiguration config = new BinaryDataReceiverConfiguration(dataBridgeConfiguration);
        Assert.assertEquals(9711, config.getSSLPort());
        Assert.assertEquals(9611, config.getTCPPort());
        Assert.assertEquals(100, config.getSizeOfSSLThreadPool());
        Assert.assertEquals(120, config.getSizeOfTCPThreadPool());
        Assert.assertEquals("TLSv1.2", config.getSslProtocols());
        Assert.assertEquals("SSL_RSA_WITH_RC4_128_MD5,SSL_RSA_WITH_RC4_128_SHA,SSL_RSA_WITH_DES_CBC_SHA," +
                "SSL_RSA_WITH_3DES_EDE_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA", config.getCiphers());
    }

    @Test
    public void testConfigurationConstructorTwo() {
        BinaryDataReceiverConfiguration config = new BinaryDataReceiverConfiguration(9711, 9611);
        Assert.assertEquals(9711, config.getSSLPort());
        Assert.assertEquals(9611, config.getTCPPort());
    }
}
