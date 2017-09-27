/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.databridge.commons;

import com.google.gson.Gson;
import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wso2.carbon.databridge.commons.exception.MalformedEventException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;

import java.util.ArrayList;
import java.util.List;

public class DefinitionConversionTest {

    private static Gson gson;

    @BeforeClass
    public static void init() {
        gson = new Gson();
    }

    @Test
    public void testDefinitionConversion()
            throws MalformedStreamDefinitionException {
        String definition = "{" +
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


        StreamDefinition streamDefinition1 = EventDefinitionConverterUtils.convertFromJson(definition);
        Assert.assertTrue(null != streamDefinition1.getStreamId());
        Assert.assertEquals(streamDefinition1.getAttributeListForKey("metaData").size(), 1);
        Assert.assertEquals(streamDefinition1.getAttributeListForKey("payloadData").size(), 5);
        Assert.assertEquals(streamDefinition1.getAttributeListForKey("correlationData").size(), 1);
    }

    @Test(expected = MalformedStreamDefinitionException.class)
    public void testInvalidJsonConversion()
            throws MalformedStreamDefinitionException {
        String definition = "{" +
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
                "  ]"; // missing closing curly brace

        EventDefinitionConverterUtils.convertFromJson(definition);
    }



    @Test(expected = MalformedStreamDefinitionException.class)
    public void testInvalidDefinitionName()
            throws MalformedStreamDefinitionException {
        String definition1 = "{" +
                "  'name':'org.wso2.esb.Medi:atorStatistics'," +
                "  'version':'2.3.0'," +
                "  'nickName': 'Stock Quote Information'," +
                "  'description': 'Some Desc'," +
                "  'tags':['foo', 'bar']," +
                "  'metaData':[" +
                "          {'name':'ipAdd','type':'STRING'}" +
                "  ]," +
                "  'payloadData':[" +
                "          {'name':'symbol','type':'string'}" +
                "  ]" +
                "}";

        try {
            StreamDefinition streamDefinition1 = EventDefinitionConverterUtils.convertFromJson(definition1);
        } catch (MalformedEventException e) {
        }
    }

    @Test(expected = MalformedStreamDefinitionException.class)
    public void testInvalidDefinitionVersion()
            throws MalformedStreamDefinitionException {
        String definition = "{" +
                "  'name':'org.wso2.esb.MediatorStatistics'," +
                "  'version':'2.3:1.0'," +
                "  'nickName': 'Stock Quote Information'," +
                "  'description': 'Some Desc'," +
                "  'tags':['foo', 'bar']," +
                "  'metaData':[" +
                "          {'name':'ipAdd','type':'STRING'}" +
                "  ]," +
                "  'payloadData':[" +
                "          {'name':'symbol','type':'string'}" +
                "  ]" +
                "}";

        try {
            StreamDefinition streamDefinition1 = EventDefinitionConverterUtils.convertFromJson(definition);
        } catch (MalformedEventException e) {
        }
    }


    @Test
    public void testDefinitionConversionWithoutVersion()
            throws MalformedStreamDefinitionException {
        String definition = "{" +
                "  'name':'org.wso2.esb.MediatorStatistics'," +
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
                "          {'name':'max','type':'double'}," +
                "          {'name':'min','type':'double'}" +
                "  ]" +
                "}";


        StreamDefinition streamDefinition1 = EventDefinitionConverterUtils.convertFromJson(definition);
        Assert.assertTrue(null != streamDefinition1.getStreamId());
    }

    @Test
    public void testEquality(){
        try {
            StreamDefinition streamDefinition1 = new StreamDefinition("org.wso2.esb.MediatorStatistics", "2.3.0");
            streamDefinition1.setCorrelationData(getCorrelationData());
            streamDefinition1.setMetaData(getMetaData());
            streamDefinition1.setPayloadData(getPayloadData());

            StreamDefinition streamDefinition2 = new StreamDefinition("org.wso2.esb.MediatorStatistics", "2.3.0");
            streamDefinition2.setCorrelationData(getCorrelationData());
            streamDefinition2.setMetaData(getMetaData());
            streamDefinition2.setPayloadData(getPayloadData());

            Assert.assertTrue(streamDefinition1.equals(streamDefinition2));
            Assert.assertEquals(streamDefinition1.hashCode(), streamDefinition2.hashCode());
            Assert.assertEquals(streamDefinition1.toString(), streamDefinition2.toString());

            // Equality with same object
            streamDefinition1.equals(streamDefinition1);
        } catch (MalformedStreamDefinitionException e){

        }
    }

    @Test
    public void testInequality(){
        try {
            StreamDefinition streamDefinition1 = new StreamDefinition("org.wso2.esb.MediatorStatistics1", "2.3.0");
            StreamDefinition streamDefinition2 = new StreamDefinition("org.wso2.esb.MediatorStatistics", "2.3.0");
            // Inequality with stream name
            Assert.assertFalse(streamDefinition1.equals(streamDefinition2));

            streamDefinition1 = new StreamDefinition("org.wso2.esb.MediatorStatistics", "2.3.0");
            streamDefinition2 = new StreamDefinition("org.wso2.esb.MediatorStatistics", "1.3.0");
            // Inequality with stream version
            Assert.assertFalse(streamDefinition1.equals(streamDefinition2));

            streamDefinition1 = new StreamDefinition("org.wso2.esb.MediatorStatistics", "2.3.0");
            streamDefinition1.addMetaData("meta", AttributeType.STRING);
            streamDefinition2 = new StreamDefinition("org.wso2.esb.MediatorStatistics", "1.3.0");
            // Inequality with meta data
            Assert.assertFalse(streamDefinition1.equals(streamDefinition2));

            streamDefinition1 = new StreamDefinition("org.wso2.esb.MediatorStatistics", "2.3.0");
            streamDefinition1.addPayloadData("payload", AttributeType.STRING);
            streamDefinition2 = new StreamDefinition("org.wso2.esb.MediatorStatistics", "1.3.0");
            // Inequality with payload data
            Assert.assertFalse(streamDefinition1.equals(streamDefinition2));

            streamDefinition1 = new StreamDefinition("org.wso2.esb.MediatorStatistics", "2.3.0");
            streamDefinition1.addCorrelationData("correlation", AttributeType.STRING);
            streamDefinition2 = new StreamDefinition("org.wso2.esb.MediatorStatistics", "1.3.0");
            // Inequality with payload data
            Assert.assertFalse(streamDefinition1.equals(streamDefinition2));

            //Inequality from different object types
            Assert.assertFalse(streamDefinition1.equals(new Integer(1)));

            //Inequality from null
            Assert.assertFalse(streamDefinition1.equals(null));


        } catch (MalformedStreamDefinitionException e){

        }
    }

    private StreamDefinition populateStreamDefiniton(StreamDefinition definition){
        definition.setMetaData(getMetaData());
        definition.setPayloadData(getPayloadData());
        definition.setCorrelationData(getCorrelationData());
        return definition;
    }

    private List<Attribute> getPayloadData(){
        List<Attribute> payload = new ArrayList<Attribute>(5);
        payload.add(new Attribute("symbol", AttributeType.STRING));
        payload.add(new Attribute("price", AttributeType.DOUBLE));
        payload.add(new Attribute("volume", AttributeType.INT));
        payload.add(new Attribute("maxTemp", AttributeType.DOUBLE));
        payload.add(new Attribute("minTemp", AttributeType.DOUBLE));
        return  payload;
    }

    private List<Attribute> getMetaData(){
        List<Attribute> meta = new ArrayList<Attribute>(1);
        meta.add(new Attribute("ipAdd", AttributeType.STRING));
        return meta;
    }

    private List<Attribute> getCorrelationData(){
        List<Attribute> meta = new ArrayList<Attribute>(1);
        meta.add(new Attribute("location", AttributeType.STRING));
        return meta;
    }
}
