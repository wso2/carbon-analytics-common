/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.databridge.commons.binary.test;

import junit.framework.Assert;
import org.junit.Test;
import org.wso2.carbon.databridge.commons.binary.BinaryMessageConverterUtil;

public class EventBufferSizeTest {

    @Test
    public void testStringByteSize() {
        int size =  BinaryMessageConverterUtil.getSize("i♥apim)");
        Assert.assertEquals("Expected byte length is 13",13,size);
    }
}
