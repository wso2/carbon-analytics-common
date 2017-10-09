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

package org.wso2.carbon.databridge.receiver.thrift.test;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.thrift.data.ThriftEventBundle;
import org.wso2.carbon.databridge.core.StreamTypeHolder;
import org.wso2.carbon.databridge.core.conf.DataBridgeConfiguration;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.databridge.core.definitionstore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.DataBridgeConfigurationException;
import org.wso2.carbon.databridge.core.exception.EventConversionException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.core.internal.EventDispatcher;
import org.wso2.carbon.databridge.receiver.thrift.converter.IndexCounter;
import org.wso2.carbon.databridge.receiver.thrift.converter.ThriftEventConverter;
import org.wso2.carbon.databridge.receiver.thrift.test.util.ThriftServerUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test cases for testing event converting.
 */
public class ThriftEventConverterTest {

    private static ThriftEventConverter thriftEventConverter;

    private static final String STREAM_NAME = "org.wso2.esb.MediatorStatistics";
    private static final String VERSION = "1.0.0";

    @BeforeClass
    public static void init() {
        ThriftServerUtil.setupCarbonConfig("test");
        thriftEventConverter = new ThriftEventConverter();
    }

    @Test
    public void testThriftEventConverter() throws DifferentStreamDefinitionAlreadyDefinedException,
            StreamDefinitionStoreException, DataBridgeConfigurationException {

        ThriftEventBundle thriftEventBundle = new ThriftEventBundle();
        thriftEventBundle.setSessionId("123");
        StreamDefinition streamDefinition = ThriftServerUtil.getSampleStreamDefinition(STREAM_NAME, VERSION);

        List<Integer> intList = new ArrayList<>();
        List<Long> longList = new ArrayList<>();
        List<Double> doubleList = new ArrayList<>();
        List<Boolean> boolList = new ArrayList<>();
        List<String> stringList = new ArrayList<>();

        thriftEventBundle.setEventNum(1);
        intList.add(504);
        longList.add(1506338103567L);
        longList.add(19900813115534L);
        doubleList.add(90.34344);
        doubleList.add(20.44345);
        doubleList.add(2.299999952316284);
        doubleList.add(20.44345);
        boolList.add(false);
        stringList.add("org.wso2.esb.MediatorStatistics:1.0.0");
        stringList.add("_null");
        stringList.add("1");

        thriftEventBundle.setIntAttributeList(intList);
        thriftEventBundle.setLongAttributeList(longList);
        thriftEventBundle.setDoubleAttributeList(doubleList);
        thriftEventBundle.setBoolAttributeList(boolList);
        thriftEventBundle.setStringAttributeList(stringList);

        StreamTypeHolder streamTypeHolder = new StreamTypeHolder();
        streamTypeHolder.putStreamDefinition(streamDefinition);

        AbstractStreamDefinitionStore streamDefinitionStore = new InMemoryStreamDefinitionStore();
        streamDefinitionStore.saveStreamDefinition(streamDefinition);

        DataBridgeConfiguration dataBridgeConfiguration = null;

        dataBridgeConfiguration = ThriftServerUtil.getDataBridgeConfiguration("databridge.config.yaml");

        EventDispatcher eventDispatcher = new EventDispatcher(streamDefinitionStore, dataBridgeConfiguration);
        streamTypeHolder.setEventDispatcherCallback(eventDispatcher);
        streamTypeHolder.putStreamDefinition(streamDefinition);

        Map<Integer, Map<String, String>> arbitraryMap = new HashMap<>();
        arbitraryMap.put(1, new HashMap<String, String>() {
        });
        thriftEventBundle.setArbitraryDataMapMap(arbitraryMap);

        AttributeType[] attributeTypeOrder = new AttributeType[7];
        attributeTypeOrder[0] = AttributeType.LONG;
        attributeTypeOrder[1] = AttributeType.BOOL;
        attributeTypeOrder[2] = AttributeType.INT;
        attributeTypeOrder[3] = AttributeType.DOUBLE;
        attributeTypeOrder[4] = AttributeType.FLOAT;
        attributeTypeOrder[5] = AttributeType.STRING;
        attributeTypeOrder[6] = AttributeType.STRING;

        int size = thriftEventConverter.getSize(thriftEventBundle);
        int numberOfEvents = thriftEventConverter.getNumberOfEvents(thriftEventBundle);
        Object[] object = thriftEventConverter.toObjectArray(thriftEventBundle, attributeTypeOrder, new IndexCounter());

        Assert.assertEquals(267, size);
        Assert.assertEquals(1, numberOfEvents);
        Assert.assertEquals(1506338103567L, object[0]);
        Assert.assertEquals(false, object[1]);
        Assert.assertEquals(504, object[2]);
        Assert.assertEquals(90.34344, object[3]);

        List<Event> eventList = thriftEventConverter.toEventList(thriftEventBundle, streamTypeHolder);

        Assert.assertEquals(STREAM_NAME + ":" + VERSION, eventList.get(0).getStreamId());
    }

    @Test(expectedExceptions = EventConversionException.class)
    public void testFaultyThriftEventConverterGetSize() {
        thriftEventConverter.getSize(new Object());
        thriftEventConverter.getNumberOfEvents(new Object());
    }

    @Test(expectedExceptions = EventConversionException.class)
    public void testFaultyThriftEventConverterGetNumberOfEvents() {
        thriftEventConverter.getNumberOfEvents(new Object());
    }

    @Test
    public void testFaultyThriftEventConverterToObjectArray() {
        Object[] objectArray = thriftEventConverter.toObjectArray(new ThriftEventBundle(), null,
                null);
        Assert.assertNull(objectArray);
    }

    @Test(expectedExceptions = EventConversionException.class)
    public void testFaultyThriftEventConverterToEventList() {
        thriftEventConverter.toEventList(new Object(), new StreamTypeHolder());
    }
}
