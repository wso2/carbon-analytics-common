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

package org.wso2.carbon.databridge.commons;

import junit.framework.TestCase;
import org.junit.Test;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.IndexDefinitionConverterUtils;

public class IndexDefinitionTest extends TestCase {

    private static final String secondaryIndex = "payload_secIndex1:STRING,correlation_secIndex2:BOOL,meta_secIndex3:STRING";

    private static final String incrementalIndex = "correlation_incIndex1:FLOAT,payload_incIndex2:DOUBLE," +
            "meta_incIndex3:BOOL";

    private static final String customIndex = "meta_custIndex1:INT,payload_custIndex2:LONG," +
            "correlation_cusIndex3:BOOL,Timestamp:LONG";

    private static final String fixedIndex = "meta_fieldIndex1:INT,payload_fieldIndex2:LONG," +
            "correlation_fieldIndex3:BOOL,fieldIndex4:STRING";

    private static final String arbitraryIndex = "payload_arbIndex1:INT,meta_arbIndex2:LONG," +
            "correlation_arbIndex3:BOOL";

    private static String getIndexDefinition(){
        return ("[" + IndexDefinitionConstants.SECONDARY_INDEX + ":=" + secondaryIndex + "]" +
                "[" + IndexDefinitionConstants.INCREMENTAL_INDEX+ ":=" + incrementalIndex + "]" +
                "[" + IndexDefinitionConstants.CUSTOM_INDEX + ":=" + customIndex + "]" +
                "[" + IndexDefinitionConstants.FIXED_INDEX + ":=" + fixedIndex + "]" +
                "[" + IndexDefinitionConstants.ARBITRARY_INDEX + ":=" + arbitraryIndex + "]");
    }

    private static String getIndexDefinitionForStreamDefiniton() {
        String indexDef = getIndexDefinition().replaceAll("meta_", "").replaceAll("payload_", "").replaceAll
                ("correlation_", "");

        indexDef = indexDef.replaceAll(":STRING", "").replaceAll(":INT", "").replaceAll(":FLOAT", "").replaceAll
                (":DOUBLE", "")
                .replaceAll(":BOOL", "");

        return indexDef;
    }

    @Test
    public void testCreateIndexDefinition(){
        IndexDefinition indexDefinition1 = IndexDefinitionConverterUtils.getIndexDefinition(getIndexDefinition());
        assertEquals(indexDefinition1.getSecondaryIndexData().size(), 3);
        assertEquals(indexDefinition1.getArbitraryIndexData().size(), 3);
        assertEquals(indexDefinition1.getFixedSearchData().size(), 4);
        assertEquals(indexDefinition1.getCustomIndexData().size(), 4);

        assertTrue(indexDefinition1.getSecondaryIndexDefn().equals(secondaryIndex));
        assertTrue(indexDefinition1.getFixedSearchDefn().equals(fixedIndex));
        assertTrue(indexDefinition1.getCustomIndexDefn().equals(customIndex));

        assertTrue(indexDefinition1.getAttributeNameforProperty("custIndex2").equals("payload_custIndex2"));
        assertTrue(indexDefinition1.getAttributeNameforProperty("cusIndex3").equals("correlation_cusIndex3"));
        assertTrue(indexDefinition1.getAttributeNameforProperty("custIndex1").equals("meta_custIndex1"));
        assertTrue(indexDefinition1.getAttributeNameforProperty("Timestamp").equals("Timestamp"));
        assertTrue(indexDefinition1.getAttributeNameforProperty("payload_arbIndex1").equals("payload_arbIndex1"));

        assertEquals(indexDefinition1.getAttributeTypeforProperty("custIndex2"), AttributeType.LONG);
        assertEquals(indexDefinition1.getAttributeTypeforProperty("cusIndex3"), AttributeType.BOOL);
        assertEquals(indexDefinition1.getAttributeTypeforProperty("custIndex1"), AttributeType.INT);
        assertEquals(indexDefinition1.getAttributeTypeforProperty("Timestamp"), AttributeType.LONG);
        assertEquals(indexDefinition1.getAttributeTypeforProperty("payload_arbIndex1"), AttributeType.INT);
    }

    @Test
    public void testCreateIndexDefinitionWithStream() throws MalformedStreamDefinitionException {
        StreamDefinition streamDefinition = new StreamDefinition("my.stream.id", "1.0.0");
        streamDefinition.addPayloadData("secIndex1", AttributeType.STRING);
        streamDefinition.addPayloadData("incIndex2", AttributeType.DOUBLE);
        streamDefinition.addPayloadData("custIndex2", AttributeType.LONG);
        streamDefinition.addPayloadData("fieldIndex2", AttributeType.LONG);
        streamDefinition.addPayloadData("arbIndex1", AttributeType.LONG);
        streamDefinition.addCorrelationData("secIndex2", AttributeType.BOOL);
        streamDefinition.addCorrelationData("incIndex1", AttributeType.FLOAT);
        streamDefinition.addCorrelationData("cusIndex3", AttributeType.BOOL);
        streamDefinition.addCorrelationData("fieldIndex3", AttributeType.BOOL);
        streamDefinition.addCorrelationData("arbIndex3", AttributeType.BOOL);
        streamDefinition.addMetaData("secIndex3", AttributeType.STRING);
        streamDefinition.addMetaData("incIndex3", AttributeType.BOOL);
        streamDefinition.addMetaData("custIndex1", AttributeType.INT);
        streamDefinition.addMetaData("fieldIndex1", AttributeType.INT);
        streamDefinition.addMetaData("arbIndex2", AttributeType.LONG);

        streamDefinition.createIndexDefinition(getIndexDefinitionForStreamDefiniton());
        IndexDefinition indexDefinition = streamDefinition.getIndexDefinition();

        assertTrue(indexDefinition.getFixedSearchData().size() == 2);
        assertTrue(indexDefinition.getCustomIndexData().size() == 4);
        assertTrue(indexDefinition.getSecondaryIndexData().size() == 3);
        assertTrue(indexDefinition.getArbitraryIndexData().size() == 3);
    }

    @Test
    public void testIndexDefinitionString(){
        IndexDefinition indexDefinition1 = IndexDefinitionConverterUtils.getIndexDefinition(getIndexDefinition());
        String convertedDefinition = IndexDefinitionConverterUtils.getIndexDefinitionString(indexDefinition1);
        assertTrue(convertedDefinition.contains(customIndex));
        assertTrue(convertedDefinition.contains(secondaryIndex));
        assertTrue(convertedDefinition.contains(fixedIndex));
    }

    @Test
    public void testClearIndexData(){
        IndexDefinition indexDefinition = IndexDefinitionConverterUtils.getIndexDefinition(getIndexDefinition());
        indexDefinition.clearIndexInformation();

        assertTrue(indexDefinition.getSecondaryIndexData() == null);
        assertTrue(indexDefinition.getCustomIndexData() == null);
    }
}
