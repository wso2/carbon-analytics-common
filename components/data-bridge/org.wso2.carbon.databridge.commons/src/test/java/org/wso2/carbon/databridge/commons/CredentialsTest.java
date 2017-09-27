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


public class CredentialsTest extends TestCase {

    @Test
    public void testCredentialsEquality(){
        Credentials credentials1 = new Credentials("username", "password", "domainName", 1);
        Credentials credentials2 = new Credentials("username", "password", "domainName", 1);
        assertTrue(credentials1.equals(credentials2));
        assertEquals(credentials1.hashCode(), credentials2.hashCode());

        //Equlaity to it self
        credentials1.equals(credentials1);
    }

    @Test
    public void testCredentialsInequality(){
        Credentials credentials1 = new Credentials("username1", "password", "domainName", 1);
        Credentials credentials2 = new Credentials("username", "password", "domainName", 1);
        // Inequality by username
        assertFalse(credentials1.equals(credentials2));

        credentials1 = new Credentials("username", "password1", "domainName", 1);
        credentials2 = new Credentials("username", "password", "domainName", 1);
        // Inequality by password
        assertFalse(credentials1.equals(credentials2));

        // Inequality by null
        assertFalse(credentials1.equals(null));

        // Inequality by Integer
        assertFalse(credentials1.equals(new Integer(1)));
    }
}
