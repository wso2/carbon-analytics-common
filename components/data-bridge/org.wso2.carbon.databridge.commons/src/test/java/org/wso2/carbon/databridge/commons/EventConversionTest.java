package org.wso2.carbon.databridge.commons;

import junit.framework.TestCase;
import org.junit.Test;
import org.wso2.carbon.databridge.commons.exception.MalformedEventException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventConverterUtils;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;

import java.util.*;

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

public class EventConversionTest extends TestCase {

    private String properJSON2 = "[\n" +
            "     {\n" +
            "      \"payloadData\" : [\"val1\", \"val2\"] ,\n" +
            "      \"metaData\" : [\"val1\", \"val2\", \"val3\"] ,\n" +
            "      \"correlationData\" : [\"val1\"],\n" +
            "      \"timeStamp\" : 1339496299900\n" +
            "     }\n" +
            "    ,\n" +
            "     {\n" +
            "      \"payloadData\" : [\"val1\", \"val2\"] ,\n" +
            "      \"metaData\" : [\"val1\", \"val2\", \"val3\"] ,\n" +
            "      \"correlationData\" : [\"val1\", \"val2\"]\n" +
            "     }\n" +
            "\n" +
            "   ]";


    private String properJSON = "[\n" +
            "     {\n" +
            "      \"streamId\" : \"foo::1.0.0\",\n" +
            "      \"payloadData\" : [\"val1\", \"val2\"] ,\n" +
            "      \"metaData\" : [\"val1\", \"val2\", \"val3\"] ,\n" +
            "      \"correlationData\" : [\"val1\"],\n" +
            "      \"timeStamp\" : 1312345432\n" +
            "     }\n" +
            "    ,\n" +
            "     {\n" +
            "      \"streamId\" : \"bar::2.1.0\", \n" +
            "      \"payloadData\" : [\"val1\", \"val2\"] ,\n" +
            "      \"metaData\" : [\"val1\", \"val2\", \"val3\"] ,\n" +
            "      \"correlationData\" : [\"val1\", \"val2\"]\n" +
            "     }\n" +
            "\n" +
            "   ]";

    private String properJSONForSameStream = "[\n" +
            "     {\n" +
            "      \"streamId\" : \"test.stream.def::1.0.0\",\n" +
            "      \"payloadData\" : [\"true\", \"1\"] ,\n" +
            "      \"metaData\" : [\"1.0\", \"2.0\"] ,\n" +
            "      \"correlationData\" : [\"1\", \"val1\"],\n" +
            "      \"timeStamp\" : 1312345432\n" +
            "     }\n" +
            "    ,\n" +
            "     {\n" +
            "      \"streamId\" : \"test.stream.def::1.0.0\",\n" +
            "      \"payloadData\" : [\"false\", \"2\"] ,\n" +
            "      \"metaData\" : [\"2.0\", \"3.0\"] ,\n" +
            "      \"correlationData\" : [\"2\", \"val2\"],\n" +
            "      \"timeStamp\" : 1312345433\n" +
            "     }\n" +
            "\n" +
            "   ]";

    private String noStreamIdJSON = "[\n" +
            "     {\n" +
            "      \"payloadData\" : [\"val1\", \"val2\"] ,\n" +
            "      \"metaData\" : [\"val1\", \"val2\", \"val3\"] ,\n" +
            "      \"correlationData\" : [\"val1\", \"val2\"],\n" +
            "      \"timeStamp\" : 1312345432\n" +
            "     }\n" +
            "    ,\n" +
            "     {\n" +
            "      \"streamId\" : \"bar::2.1.0\", \n" +
            "      \"payloadData\" : [\"val1\", \"val2\"] ,\n" +
            "      \"metaData\" : [\"val1\", \"val2\", \"val3\"] ,\n" +
            "      \"correlationData\" : [\"val1\", \"val2\"]\n" +
            "     }\n" +
            "\n" +
            "   ]";

    private String emptyStreamIdJSON = "[\n" +
            "     {\n" +
            "      \"streamId\" : \"\", \n" +
            "      \"payloadData\" : [\"val1\", \"val2\"] ,\n" +
            "      \"metaData\" : [\"val1\", \"val2\", \"val3\"] ,\n" +
            "      \"correlationData\" : [\"val1\", \"val2\"],\n" +
            "      \"timeStamp\" : 1312345432\n" +
            "     }\n" +
            "    ,\n" +
            "     {\n" +
            "      \"streamId\" : \"bar::2.1.0\", \n" +
            "      \"payloadData\" : [\"val1\", \"val2\"] ,\n" +
            "      \"metaData\" : [\"val1\", \"val2\", \"val3\"] ,\n" +
            "      \"correlationData\" : [\"val1\", \"val2\"]\n" +
            "     }\n" +
            "\n" +
            "   ]";


    private String emptyArrayJSON = "[\n" +
            "     {\n" +
            "      \"streamId\" : \"foo::1.0.0\", \n" +
            "      \"payloadData\" : [] ,\n" +
            "      \"metaData\" : [\"val1\", \"val2\", \"val3\"] ,\n" +
            "      \"correlationData\" : [\"val1\", \"val2\"],\n" +
            "      \"timeStamp\" : 1312345432\n" +
            "     }\n" +
            "    ,\n" +
            "     {\n" +
            "      \"streamId\" : \"bar::2.1.0\", \n" +
            "      \"payloadData\" : [\"val1\", \"val2\"] ,\n" +
            "      \"metaData\" : [\"val1\", \"val2\", \"val3\"] ,\n" +
            "      \"correlationData\" : [\"val1\", \"val2\"]\n" +
            "     }\n" +
            "\n" +
            "   ]";

    private String invalidJSON = "[\n" +
            "     {\n" +
            "      \"streamId\" : \"foo::1.0.0\", \n" +
            "      \"payloadData\" : [] ,\n" +
            "      \"metaData\" : [\"val1\", \"val2\", \"val3\"] ,\n" +
            "      \"correlationData\" : [\"val1\", \"val2\"],\n" +
            "      \"timeStamp\" : 1312345432\n" +
            "     }\n" +
            "    ,\n" +
            "     {\n" +
            "      \"streamId\" : \"bar::2.1.0\", \n" +
            "      \"payloadData\" : [\"val1\", \"val2\"] ,\n" +
            "      \"metaData\" : [\"val1\", \"val2\", \"val3\"] ,\n" +
            "      \"correlationData\" : [\"val1\", \"val2\"]\n" +
            "\n" +
            "   ]";

    private static StreamDefinition createStreamDef() throws MalformedStreamDefinitionException {
        StreamDefinition definition = new StreamDefinition("test.stream.def", "1.0.0");
        definition.addPayloadData("attr1", AttributeType.BOOL);
        definition.addPayloadData("attr2", AttributeType.INT);
        definition.addMetaData("attr3", AttributeType.DOUBLE);
        definition.addMetaData("attr4", AttributeType.FLOAT);
        definition.addCorrelationData("attr5", AttributeType.LONG);
        definition.addCorrelationData("attr6", AttributeType.STRING);
        return definition;
    }


    @Test
    public void testConversion() {
        List<Event> eventList = EventConverterUtils.convertFromJson(properJSON);
        assertEquals(2, eventList.size());
        Event event = eventList.get(0);
        assertEquals(event.getCorrelationData().length, 1);
        assertEquals(event.getPayloadData().length, 2);
        assertEquals(event.getMetaData().length, 3);

    }

    @Test(expected = MalformedEventException.class)
    public void testNoStreamId() {
        try {
            EventConverterUtils.convertFromJson(noStreamIdJSON);
        } catch (MalformedEventException e) {
        }
    }

    @Test(expected = MalformedEventException.class)
    public void testEmptyStreamId() {
        try {
            EventConverterUtils.convertFromJson(emptyStreamIdJSON);
        } catch (MalformedEventException e) {
        }
    }


    @Test(expected = MalformedEventException.class)
    public void testInvalidJson() {
        try {
            EventConverterUtils.convertFromJson(invalidJSON);
        } catch (MalformedEventException e) {
        }
    }

    @Test
    public void testEmptyEventArray() {
        List<Event> eventList = EventConverterUtils.convertFromJson(emptyArrayJSON);
        Event event = eventList.get(0);
        assertEquals(event.getPayloadData().length, 0);
    }

    @Test
    public void testRESTEventConversion() {
        List<Event> eventList = EventConverterUtils.convertFromJson(properJSON2, "foo::1.0.0-" + UUID.randomUUID());
        assertEquals(2, eventList.size());
        Event event = eventList.get(0);
        assertEquals(event.getCorrelationData().length, 1);
        assertEquals(event.getPayloadData().length, 2);
        assertEquals(event.getMetaData().length, 3);
    }

    @Test
    public void testConversionWithStreamDefinition() throws MalformedStreamDefinitionException {
        StreamDefinition definition = createStreamDef();
        List<Event> events = EventConverterUtils.convertFromJson(properJSONForSameStream, "test.stream.def::1.0.0", definition);
        assertEquals(2, events.size());
        Event event = events.get(0);
        assertEquals(event.getCorrelationData().length, 2);
        assertEquals(event.getPayloadData().length, 2);
        assertEquals(event.getMetaData().length, 2);
    }

    @Test
    public void testConversionWithStreamDefinitionInvalidJson() throws MalformedStreamDefinitionException {
        try {
            StreamDefinition definition = createStreamDef();
            EventConverterUtils.convertFromJson(properJSONForSameStream.substring(0, 50),"test.stream.def::1.0.0", definition);
        } catch (MalformedEventException e) {
        }
    }

    @Test
    public void testConversionWithStreamDefinitionEmptyStramId() throws MalformedStreamDefinitionException {
        try {
            StreamDefinition definition = createStreamDef();
            EventConverterUtils.convertFromJson(properJSONForSameStream.substring(0, 50),"", definition);
        } catch (MalformedEventException e) {
        }
    }

    @Test(expected = MalformedEventException.class)
    public void testRESTEventConversionWithInvalidJSON() {
        try {
            EventConverterUtils.convertFromJson(invalidJSON, "foo::1.0.0-" + UUID.randomUUID());
        } catch (MalformedEventException e) {
        }
    }

    @Test(expected = MalformedEventException.class)
    public void testNullRESTEvents() {
        try {
            EventConverterUtils.convertFromJson(properJSON2, null);
        } catch (MalformedEventException e) {

        }

    }

    @Test(expected = MalformedEventException.class)
    public void testEmptyRESTEvents() {
        try {
            EventConverterUtils.convertFromJson(properJSON2, "");
        } catch (MalformedEventException e) {
            return;
        }
    }


    @Test
    public void testEventEquality() {
        long timestamp = System.currentTimeMillis();

        Event event1 = new Event();
        event1.setStreamId("foo:1.0.0");
        event1.setTimeStamp(timestamp);
        event1.setPayloadData(new Object[]{"abc", 78.5, 45f});

        Event event2 = new Event();
        event2.setStreamId("foo:1.0.0");
        event2.setTimeStamp(timestamp);
        event2.setPayloadData(new Object[]{"abc", 78.5, 45f});
        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
        assertEquals(event1.toString(), event2.toString());

        // Equality with different setter methods
        Event event3 = new Event();
        event3.setStreamId("foo:1.0.0");
        event3.setTimeStamp(timestamp);
        event3.setData("metaData", new Object[]{"abc"});
        event3.setData("correlationData", new Object[]{"def"});
        event3.setData("payloadData", new Object[]{2});
        event3.setArbitraryDataMap((Map<String, String>) new HashMap<>().put("arbitrary1", 123));

        Event event4 = new Event();
        event4.setStreamId("foo:1.0.0");
        event4.setTimeStamp(timestamp);
        event4.setData("metaData", new Object[]{"abc"});
        event4.setData("correlationData", new Object[]{"def"});
        event4.setData("payloadData", new Object[]{2});
        event4.setArbitraryDataMap((Map<String, String>) new HashMap<>().put("arbitrary1", 123));
        assertEquals(event3, event4);
        assertEquals(event3.hashCode(), event4.hashCode());
        assertEquals(event3.toString(), event4.toString());

        // Testing equality with it self
        assertTrue(event1.equals(event1));
    }

    @Test
    public void testEventInequality() {
        long timestamp = System.currentTimeMillis();
        Map<String, String> arbitraryData = new HashMap<String, String>();
        arbitraryData.put("arbitrary1", "123");

        Event event1 = new Event("foo:1.0.0", timestamp, new Object[]{"abc"}, new Object[]{"def"}, new Object[]{1},
                arbitraryData);
        Event event2 = new Event("foo:2.0.0", timestamp, new Object[]{"abc"}, new Object[]{"def"}, new Object[]{1},
                arbitraryData);
        // Inequality by stream definition
        assertFalse(event1.equals(event2));

        event1 = new Event("foo:1.0.0", timestamp, new Object[]{"abc"}, new Object[]{"def"}, new Object[]{1},
                arbitraryData);
        event2 = new Event("foo:1.0.0", timestamp + 1, new Object[]{"abc"}, new Object[]{"def"}, new Object[]{1},
                arbitraryData);
        // Inequality by timestamp
        assertFalse(event1.equals(event2));

        event1 = new Event("foo:1.0.0", timestamp, new Object[]{"abc"}, new Object[]{"def"}, new Object[]{1},
                arbitraryData);
        event2 = new Event("foo:1.0.0", timestamp, new Object[]{"abd"}, new Object[]{"def"}, new Object[]{1},
                arbitraryData);
        // Inequality by meta data
        assertFalse(event1.equals(event2));

        event1 = new Event("foo:1.0.0", timestamp, new Object[]{"abc"}, new Object[]{"def"}, new Object[]{1},
                arbitraryData);
        event2 = new Event("foo:1.0.0", timestamp, new Object[]{"abc"}, new Object[]{"deg"}, new Object[]{1},
                arbitraryData);
        // Inequality by correlation data
        assertFalse(event1.equals(event2));

        event1 = new Event("foo:1.0.0", timestamp, new Object[]{"abc"}, new Object[]{"def"}, new Object[]{1},
                arbitraryData);
        event2 = new Event("foo:1.0.0", timestamp, new Object[]{"abc"}, new Object[]{"def"}, new Object[]{2},
                arbitraryData);
        // Inequality by payload data
        assertFalse(event1.equals(event2));


        event1 = new Event("foo:1.0.0", timestamp, new Object[]{"abc"}, new Object[]{"def"}, new Object[]{1},
                arbitraryData);
        Map<String, String> arbitraryData2 = new HashMap<String, String>();
        arbitraryData2.put("arbitrary1", "12223");
        event2 = new Event("foo:1.0.0", timestamp, new Object[]{"abc"}, new Object[]{"def"}, new Object[]{1},
                arbitraryData2);
        // Inequality by arbitrary data
        assertFalse(event1.equals(event2));

        // Inequality for different object types
        assertFalse(new Event().equals(new Integer(1)));
    }


    @Test
    public void testEventListEquality() {
        Event event1 = new Event();
        event1.setStreamId("foo:1.0.0");
        event1.setCorrelationData(new Object[]{"abc", 78.5, 45f, 34, 2.3});
        event1.setMetaData(new Object[]{"abc", 78.5, true});
        event1.setPayloadData(new Object[]{"abc", 78.5, 45f});

        List<Event> event1s = new ArrayList<Event>();
        event1s.add(event1);
        event1s.add(event1);

        Event event2 = new Event();
        event2.setStreamId("foo:1.0.0");
        event2.setCorrelationData(new Object[]{"abc", 78.5, 45f, 34, 2.3});
        event2.setMetaData(new Object[]{"abc", 78.5, true});
        event2.setPayloadData(new Object[]{"abc", 78.5, 45f});
        List<Event> event2s = new ArrayList<Event>();
        event2s.add(event2);
        event2s.add(event2);

        assertEquals(event1, event2);
        assertEquals(event1s, event2s);
    }

    @Test
    public void testGetAttributeList() {

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("symbol", AttributeType.STRING));
        attributes.add(new Attribute("price", AttributeType.DOUBLE));
        attributes.add(new Attribute("volume", AttributeType.INT));
        attributes.add(new Attribute("maxTemp", AttributeType.DOUBLE));
        attributes.add(new Attribute("minTemp", AttributeType.DOUBLE));

        AttributeType[] attributeTypes = EventDefinitionConverterUtils.generateAttributeTypeArray(attributes);
        assertEquals(attributeTypes[0], AttributeType.STRING);
        assertEquals(attributeTypes[1], AttributeType.DOUBLE);
        assertEquals(attributeTypes[2], AttributeType.INT);
        assertEquals(attributeTypes[3], AttributeType.DOUBLE);
        assertEquals(attributeTypes[4], AttributeType.DOUBLE);
    }

}
