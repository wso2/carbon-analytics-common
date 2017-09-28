/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.databridge.commons;

import junit.framework.Assert;
import org.junit.Test;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;

import java.util.ArrayList;
import java.util.List;

public class MultipleDefinitionConversionTest {
    private String definition1 = "{" +
                                 "  \"name\":\"org.wso2.esb.MediatorStatistics\"," +
                                 "  \"version\":\"2.3.0\"," +
                                 "  \"nickName\": \"Stock Quote Information\"," +
                                 "  \"description\": \"Some Desc\"," +
                                 "  \"tags\":[\"foo\", \"bar\"]," +
                                 "  \"metaData\":[" +
                                 "          {\"name\":\"ipAdd\",\"type\":\"STRING\"}" +
                                 "  ]," +
                                 "  \"payloadData\":[" +
                                 "          {\"name\":\"symbol\",\"type\":\"string\"}," +
                                 "          {\"name\":\"price\",\"type\":\"double\"}," +
                                 "          {\"name\":\"volume\",\"type\":\"int\"}," +
                                 "          {\"name\":\"max\",\"type\":\"double\"}," +
                                 "          {\"name\":\"min\",\"type\":\"double\"}" +
                                 "  ]" +
                                 "}";
    private String definition2 = "{" +
                                 "  \"name\":\"org.wso2.esb.MediatorStatistics\"," +
                                 "  \"version\":\"3.0.0\"," +
                                 "  \"nickName\": \"Stock Quote Information\"," +
                                 "  \"description\": \"Some Desc\"," +
                                 "  \"tags\":[\"foo\", \"bar\"]," +
                                 "  \"metaData\":[" +
                                 "          {\"name\":\"ipAdd\",\"type\":\"STRING\"}" +
                                 "  ]," +
                                 "  \"payloadData\":[" +
                                 "          {\"name\":\"symbol\",\"type\":\"float\"}," +
                                 "          {\"name\":\"price\",\"type\":\"float\"}," +
                                 "          {\"name\":\"volume\",\"type\":\"string\"}," +
                                 "          {\"name\":\"max\",\"type\":\"double\"}," +
                                 "          {\"name\":\"min\",\"type\":\"double\"}" +
                                 "  ]," +
                                 "  \"correlationData\":[" +
                                 "          {\"name\":\"symbol\",\"type\":\"string\"}," +
                                 "          {\"name\":\"price\",\"type\":\"float\"}" +
                                 "  ]" +
                                 "}";
    private String definition3 = "{" +
                                 "  \"name\":\"org.wso2.esb.MediatorStatistics\"," +
                                 "  \"version\":\"2.3.1\"," +
                                 "  \"nickName\": \"Stock Quote Information\"," +
                                 "  \"description\": \"Some Desc\"," +
                                 "  \"tags\":[\"foo\", \"bar\"]," +
                                 "  \"metaData\":[" +
                                 "          {\"name\":\"ipAdd\",\"type\":\"STRING\"}" +
                                 "  ]," +
                                 "  \"payloadData\":[" +
                                 "          {\"name\":\"symbol\",\"type\":\"sTring\"}," +
                                 "          {\"name\":\"price\",\"type\":\"DOUBLE\"}," +
                                 "          {\"name\":\"volume\",\"type\":\"int\"}," +
                                 "          {\"name\":\"max\",\"type\":\"douBLE\"}," +
                                 "          {\"name\":\"min\",\"type\":\"double\"}" +
                                 "  ]," +
                                 "  \"correlationData\":[]" +
                                 "}";

    @Test
    public void multipleDefnConversionFromJSON()
            throws MalformedStreamDefinitionException {

        String multipleDefns = combineJSONEventDefinitons();


        List<StreamDefinition> actualStreamDefinitions =
                EventDefinitionConverterUtils.convertMultipleEventDefns(multipleDefns);
        StreamDefinition actualStreamDefinition1 = actualStreamDefinitions.get(0);
        StreamDefinition actualStreamDefinition2 = actualStreamDefinitions.get(1);
        StreamDefinition actualStreamDefinition3 = actualStreamDefinitions.get(2);


        // add stream defines as otherwise they will be generated into a unique value
        StreamDefinition expectedEventDefinitions1 =
                new StreamDefinition("org.wso2.esb.MediatorStatistics", "2.3.0",
                                     actualStreamDefinition1.getStreamId());
        List<Attribute> meta = new ArrayList<Attribute>(1);
        meta.add(new Attribute("ipAdd", AttributeType.STRING));
        expectedEventDefinitions1.setMetaData(meta);
        List<Attribute> payload = new ArrayList<Attribute>(5);
        payload.add(new Attribute("symbol", AttributeType.STRING));
        payload.add(new Attribute("price", AttributeType.DOUBLE));
        payload.add(new Attribute("volume", AttributeType.INT));
        payload.add(new Attribute("max", AttributeType.DOUBLE));
        payload.add(new Attribute("min", AttributeType.DOUBLE));
        expectedEventDefinitions1.setPayloadData(payload);
        List<String> tags = new ArrayList<String>();
        tags.add("foo");
        tags.add("bar");
        expectedEventDefinitions1.setTags(tags);

        // add stream defns as otherwise they will be generated into a unique value
        StreamDefinition expectedEventDefinitions2 =
                new StreamDefinition("org.wso2.esb.MediatorStatistics", "3.0.0",
                                     actualStreamDefinition2.getStreamId());
        List<Attribute> meta2 = new ArrayList<Attribute>(1);
        meta2.add(new Attribute("ipAdd", AttributeType.STRING));
        expectedEventDefinitions2.setMetaData(meta2);
        List<Attribute> payload2 = new ArrayList<Attribute>(5);
        payload2.add(new Attribute("symbol", AttributeType.FLOAT));
        payload2.add(new Attribute("price", AttributeType.FLOAT));
        payload2.add(new Attribute("volume", AttributeType.STRING));
        payload2.add(new Attribute("max", AttributeType.DOUBLE));
        payload2.add(new Attribute("min", AttributeType.DOUBLE));
        expectedEventDefinitions2.setPayloadData(payload2);
        List<Attribute> correlation2 = new ArrayList<Attribute>(5);
        correlation2.add(new Attribute("symbol", AttributeType.STRING));
        correlation2.add(new Attribute("price", AttributeType.FLOAT));
        expectedEventDefinitions2.setCorrelationData(correlation2);
        expectedEventDefinitions2.setTags(tags);

        StreamDefinition expectedEventDefinitions3 =
                new StreamDefinition("org.wso2.esb.MediatorStatistics", "2.3.1",
                                     actualStreamDefinition3.getStreamId());
        List<Attribute> meta3 = new ArrayList<Attribute>(1);
        meta3.add(new Attribute("ipAdd", AttributeType.STRING));
        expectedEventDefinitions3.setMetaData(meta3);
        List<Attribute> payload3 = new ArrayList<Attribute>(5);
        payload3.add(new Attribute("symbol", AttributeType.STRING));
        payload3.add(new Attribute("price", AttributeType.DOUBLE));
        payload3.add(new Attribute("volume", AttributeType.INT));
        payload3.add(new Attribute("max", AttributeType.DOUBLE));
        payload3.add(new Attribute("min", AttributeType.DOUBLE));
        expectedEventDefinitions3.setPayloadData(payload3);
        List<String> tags3 = new ArrayList<String>();
        tags3.add("foo");
        tags3.add("bar");
        expectedEventDefinitions3.setTags(tags3);

        List<StreamDefinition> expectedEventDefns = new ArrayList<StreamDefinition>();
        expectedEventDefns.add(expectedEventDefinitions1);
        expectedEventDefns.add(expectedEventDefinitions2);
        expectedEventDefns.add(expectedEventDefinitions3);

        Assert.assertEquals(actualStreamDefinitions, expectedEventDefns);

    }

    @Test
    public void testConvertDefintionArray() throws MalformedStreamDefinitionException {
        String multipleDefns = combineJSONEventDefinitons();
        List<StreamDefinition> actualStreamDefinitions =
                EventDefinitionConverterUtils.convertMultipleEventDefns(multipleDefns);

        EventDefinitionConverterUtils.convertToJson(actualStreamDefinitions);

        Assert.assertTrue(combineJSONEventDefinitons().equals(multipleDefns));
    }

    @Test(expected = MalformedStreamDefinitionException.class)
    public void multipleDefnConversionFromInvalidJSON() throws MalformedStreamDefinitionException {
        String multipleDefns = combineToInvalidJSONEventDefinitons();
        EventDefinitionConverterUtils.convertMultipleEventDefns(multipleDefns);
    }

    private String combineJSONEventDefinitons() {
        return "[" + definition1 + ", " + definition2 + ", " + definition3 + "]";
    }

    private String combineToInvalidJSONEventDefinitons() {
        return "[" + definition1 + ", " + definition2 + definition3 ;
    }

}