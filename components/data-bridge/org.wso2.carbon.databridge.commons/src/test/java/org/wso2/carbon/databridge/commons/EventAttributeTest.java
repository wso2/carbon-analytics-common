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

public class EventAttributeTest extends TestCase {

    @Test
    public void testAttributeEqualityTest() {
        Attribute attribute1 = new Attribute("myAttribute", AttributeType.STRING);
        Attribute attribute2 = new Attribute("myAttribute", AttributeType.STRING);
        assertTrue(attribute1.equals(attribute2));
        assertEquals(attribute1.hashCode(), attribute2.hashCode());
        assertEquals(attribute1.toString(), attribute2.toString());

        //Checking equality with own instance
        attribute1.equals(attribute1);
    }

    @Test
    public void testAttributeInequalityTest() {
        Attribute attribute1 = new Attribute("myAttribute", AttributeType.STRING);
        Attribute attribute2 = new Attribute("myAttribute", AttributeType.BOOL);
        // Inequality by Attribute type
        assertFalse(attribute1.equals(attribute2));

        attribute2.setType(AttributeType.STRING);
        attribute2.setName("myAttribute1");
        // Inequality by attribute name
        assertFalse(attribute1.equals(attribute2));

        //Inequality by different object types
        attribute1.equals(new Integer(1));

        //Inequality by passing null
        attribute1.equals(null);
    }
}
