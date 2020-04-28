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
package org.wso2.carbon.databridge.streamdefn.filesystem.test;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.streamdefn.filesystem.FileSystemStreamDefinitionStore;
import org.wso2.carbon.databridge.streamdefn.filesystem.internal.ServiceHolder;
import org.wso2.carbon.event.stream.core.internal.CarbonEventStreamService;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ServiceStoreTestCase.class)
public class ServiceStoreTestCase {
    private FileSystemStreamDefinitionStore fileSystemStreamDefinitionStore = new FileSystemStreamDefinitionStore();

    @Mock
    CarbonEventStreamService carbonEventStreamService;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @BeforeClass
    public static void init() {
        FilesystemTestUtil.setupCarbonConfig();
    }

    @Test
    public void testServiceStore() throws MalformedStreamDefinitionException, StreamDefinitionStoreException {
        FilesystemTestUtil.setupCarbonConfig();
        StreamDefinition streamDefinition = EventDefinitionConverterUtils.convertFromJson(FilesystemTestUtil.STREAM_DEFN);
        ServiceHolder.setEventStreamService(carbonEventStreamService);
        ServiceHolder.setStreamDefinitionStore(fileSystemStreamDefinitionStore);
        ServiceHolder.getEventStreamService();
        ServiceHolder.getStreamDefinitionStore();
        ServiceHolder.getEventStreamService();
        fileSystemStreamDefinitionStore.saveStreamDefinitionToStore(streamDefinition, FilesystemTestUtil.TENANT_ID);
    }
}
