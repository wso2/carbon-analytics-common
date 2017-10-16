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

package org.wso2.carbon.databridge.receiver.binary.test.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.binary.BinaryMessageConstants;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.core.conf.DataBridgeConfiguration;
import org.wso2.carbon.databridge.core.internal.utils.DataBridgeConstants;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.databridge.commons.binary.BinaryMessageConverterUtil.getSize;

public class BinaryServerUtil {

    public static final Path testDir = Paths.get("src", "test", "resources");

    private static Logger log = Logger.getLogger(BinaryServerUtil.class);

    public static byte[] convertEventToByteArray(Event event, String sessionId) throws IOException {

        int messageSize = 8 + sessionId.length();
        int eventSize = getEventSize(event);
        messageSize += eventSize + 4;
        ByteBuffer eventDataBuffer = ByteBuffer.allocate(sessionId.length() + eventSize + 17);
        eventDataBuffer.put((byte) 2);
        eventDataBuffer.putInt(messageSize);
        eventDataBuffer.putInt(sessionId.length());
        eventDataBuffer.put(sessionId.getBytes(BinaryMessageConstants.DEFAULT_CHARSET));
        eventDataBuffer.putInt(1);
        eventDataBuffer.putInt(eventSize);
        eventDataBuffer.putLong(event.getTimeStamp());
        eventDataBuffer.putInt(event.getStreamId().length());
        eventDataBuffer.put(event.getStreamId().getBytes(BinaryMessageConstants.DEFAULT_CHARSET));

        if (event.getMetaData() != null && event.getMetaData().length != 0) {
            for (Object aMetaData : event.getMetaData()) {
                assignData(aMetaData, eventDataBuffer);
            }
        }
        if (event.getCorrelationData() != null && event.getCorrelationData().length != 0) {
            for (Object aCorrelationData : event.getCorrelationData()) {
                assignData(aCorrelationData, eventDataBuffer);
            }
        }
        if (event.getPayloadData() != null && event.getPayloadData().length != 0) {
            for (Object aPayloadData : event.getPayloadData()) {
                assignData(aPayloadData, eventDataBuffer);
            }
        }
        if (event.getArbitraryDataMap() != null && event.getArbitraryDataMap().size() != 0) {
            for (Map.Entry<String, String> aArbitraryData : event.getArbitraryDataMap().entrySet()) {
                assignData(aArbitraryData.getKey(), eventDataBuffer);
                assignData(aArbitraryData.getValue(), eventDataBuffer);
            }
        }

        return eventDataBuffer.array();
    }

    public static Event buildEvent(String streamName, String version) {
        Event event = new Event();
        event.setStreamId(streamName + ":" + version);
        event.setMetaData(new Object[]{"127.0.0.1"});
        event.setCorrelationData(null);
        event.setPayloadData(new Object[]{"WSO2", 123.4, 2, 12.4, 1.3});
        return event;
    }

    private static void assignData(Object data, ByteBuffer eventDataBuffer) throws IOException {
        if (data instanceof String) {
            eventDataBuffer.putInt(((String) data).getBytes().length);
            eventDataBuffer.put(((String) data).getBytes(BinaryMessageConstants.DEFAULT_CHARSET));
        } else if (data instanceof Integer) {
            eventDataBuffer.putInt((Integer) data);
        } else if (data instanceof Long) {
            eventDataBuffer.putLong((Long) data);
        } else if (data instanceof Float) {
            eventDataBuffer.putFloat((Float) data);
        } else if (data instanceof Double) {
            eventDataBuffer.putDouble((Double) data);
        } else if (data instanceof Boolean) {
            eventDataBuffer.put((byte) (((Boolean) data) ? 1 : 0));
        } else {
            eventDataBuffer.putInt(0);
        }
    }

    private static int getEventSize(Event event) {
        int eventSize = 4 + event.getStreamId().length() + 8;
        Object[] data = event.getMetaData();
        if (data != null) {
            for (Object aData : data) {
                eventSize += getSize(aData);
            }
        }
        data = event.getCorrelationData();
        if (data != null) {
            for (Object aData : data) {
                eventSize += getSize(aData);
            }
        }
        data = event.getPayloadData();
        if (data != null) {
            for (Object aData : data) {
                eventSize += getSize(aData);
            }
        }
        if (event.getArbitraryDataMap() != null && event.getArbitraryDataMap().size() != 0) {
            for (Map.Entry<String, String> aArbitraryData : event.getArbitraryDataMap().entrySet()) {
                eventSize += 8 + aArbitraryData.getKey().length() + aArbitraryData.getValue().length();
            }
        }
        return eventSize;
    }

    public static DataBridgeConfiguration getDataBridgeConfiguration(String file) throws IOException,
            XMLStreamException, JAXBException {

        String configPath = testDir + File.separator + file;

        File configFile = new File(configPath);
        DataBridgeConfiguration dataBridgeConfiguration;

        if (configFile.exists()) {
            try (FileInputStream fileInputStream = new FileInputStream(configFile)) {
                JAXBContext jaxbContext = JAXBContext.newInstance(DataBridgeConfiguration.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                dataBridgeConfiguration = (DataBridgeConfiguration) jaxbUnmarshaller.unmarshal(configFile);
                StAXOMBuilder builder = new StAXOMBuilder(fileInputStream);
                OMElement configElement = builder.getDocumentElement();
                SecretResolver secretResolver = SecretResolverFactory.create(configElement, true);
                if (secretResolver != null && secretResolver.isInitialized()) {
                    String resolvedPassword = getResolvedPassword(secretResolver,
                            DataBridgeConstants.DATA_BRIDGE_CONF_PASSWORD_ALIAS);
                    if (resolvedPassword != null) {
                        dataBridgeConfiguration.setKeyStorePassword(resolvedPassword);
                    }
                }
                return dataBridgeConfiguration;
            }
        } else {
            return null;
        }
    }

    private static String getResolvedPassword(SecretResolver secretResolver, String alias) {
        if (secretResolver.isTokenProtected(alias)) {
            String resolvedPassword = secretResolver.resolve(alias);
            if (resolvedPassword != null && !resolvedPassword.isEmpty()) {
                return resolvedPassword;
            }
        }
        return null;
    }

    public static void setupCarbonConfig(String tenantName) {
        System.setProperty("carbon.home", Paths.get(testDir.toString(), "carbon-context").toString());
        System.setProperty("tenant.name", tenantName);
        System.setProperty("portOffset", "0");
    }

    public static StreamDefinition getSampleStreamDefinition(String streamName, String version) {
        StreamDefinition streamDefinition = null;
        try {
            streamDefinition = new StreamDefinition(streamName, version);
        } catch (MalformedStreamDefinitionException e) {
            log.error("Error Creating Stream Definition");
        }
        streamDefinition.addMetaData("ipAdd", AttributeType.STRING);
        streamDefinition.addPayloadData("symbol", AttributeType.STRING);
        streamDefinition.addPayloadData("price", AttributeType.DOUBLE);
        streamDefinition.addPayloadData("volume", AttributeType.INT);
        streamDefinition.addPayloadData("max", AttributeType.DOUBLE);
        streamDefinition.addPayloadData("min", AttributeType.DOUBLE);

        return streamDefinition;
    }
}
