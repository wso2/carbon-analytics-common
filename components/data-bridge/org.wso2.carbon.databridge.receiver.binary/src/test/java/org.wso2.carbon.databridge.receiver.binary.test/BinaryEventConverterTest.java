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

package org.wso2.carbon.databridge.receiver.binary.test;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.core.StreamTypeHolder;
import org.wso2.carbon.databridge.receiver.binary.BinaryEventConverter;
import org.wso2.carbon.databridge.receiver.binary.test.util.BinaryServerUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class BinaryEventConverterTest {

    private static final int TENANT_ID = -1234;
    private static final String STREAM_NAME = "org.wso2.esb.MediatorStatistics";
    private static final String VERSION = "1.0.0";

    private static BinaryEventConverter binaryEventConverter;

    @BeforeClass
    public static void init() {
        BinaryServerUtil.setupCarbonConfig("testTenant");
        binaryEventConverter = BinaryEventConverter.getConverter();
    }

    @Test
    public void testThriftEventConverter() throws IOException {

        Event event = BinaryServerUtil.buildEvent(STREAM_NAME, VERSION);

        byte[] eventByteArray = BinaryServerUtil.convertEventToByteArray(
                event, "760c8f1e-bbb1-4ac7-bcda-dbd929bc9264");

        Object eventBundle = Arrays.copyOfRange(eventByteArray, 5, eventByteArray.length); //Remove message type
        StreamTypeHolder streamTypeHolder = new StreamTypeHolder(TENANT_ID);
        streamTypeHolder.putStreamDefinition(BinaryServerUtil.getSampleStreamDefinition(STREAM_NAME, VERSION));
        List<Event> outputList = binaryEventConverter.toEventList(eventBundle, streamTypeHolder);

        Assert.assertEquals(1, outputList.size());
        Assert.assertEquals(146, binaryEventConverter.getSize(eventBundle));
        Assert.assertEquals(1, binaryEventConverter.getNumberOfEvents(eventBundle));
    }
}
