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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.streamdefn.filesystem.FileSystemStreamDefinitionStore;
import org.wso2.carbon.databridge.streamdefn.filesystem.internal.EventStreamListenerImpl;
import org.wso2.carbon.databridge.streamdefn.filesystem.internal.ServiceHolder;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.event.stream.core.internal.CarbonEventStreamService;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

public class FileSystemStreamDefinitionStoreTestCase {
    private FileSystemStreamDefinitionStore fileSystemStreamDefinitionStore;
    private EventStreamService carbonEventStreamService;
    private StreamDefinition streamDefinition;
    private MockedStatic<ServiceHolder> serviceHolderMockedStatic;
    
    @Mock
    private EventStreamService mockEventStreamService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();


    @Before
    public void init() throws Exception {
        FilesystemTestUtil.setupCarbonConfig();
        serviceHolderMockedStatic = mockStatic(ServiceHolder.class);
        fileSystemStreamDefinitionStore = new FileSystemStreamDefinitionStore();
        streamDefinition = EventDefinitionConverterUtils.convertFromJson(FilesystemTestUtil.STREAM_DEFN);
        carbonEventStreamService = new CarbonEventStreamService();
        carbonEventStreamService.addEventStreamDefinition(streamDefinition);
        ServiceHolder serviceHolder = new ServiceHolder();
        ServiceHolder.setEventStreamService(carbonEventStreamService);
        ServiceHolder.setStreamDefinitionStore(fileSystemStreamDefinitionStore);

        serviceHolderMockedStatic.when(ServiceHolder::getEventStreamService).thenReturn(carbonEventStreamService);
        serviceHolderMockedStatic.when(ServiceHolder::getStreamDefinitionStore).thenReturn(fileSystemStreamDefinitionStore);
    }

    @After
    public void tearDown() {
        if (serviceHolderMockedStatic != null) {
            serviceHolderMockedStatic.close();
        }
    }

    @Test
    public void testFileSystemStreamDefintionStoreSave() throws MalformedStreamDefinitionException, StreamDefinitionStoreException {
        fileSystemStreamDefinitionStore.saveStreamDefinitionToStore(streamDefinition, FilesystemTestUtil.TENANT_ID);
    }

    @Test(expected = StreamDefinitionStoreException.class)
    public void testFileSystemStreamDefintionStoreGetStreamDefintion() throws MalformedStreamDefinitionException, StreamDefinitionStoreException, EventStreamConfigurationException {
        serviceHolderMockedStatic.when(ServiceHolder::getEventStreamService).thenReturn(mockEventStreamService);
        doThrow(EventStreamConfigurationException.class).when(mockEventStreamService).getAllStreamDefinitions();
        fileSystemStreamDefinitionStore.getAllStreamDefinitionsFromStore(FilesystemTestUtil.TENANT_ID);
    }

    @Test(expected = StreamDefinitionStoreException.class)
    public void test2() throws StreamDefinitionStoreException, EventStreamConfigurationException {
        serviceHolderMockedStatic.when(ServiceHolder::getEventStreamService).thenReturn(mockEventStreamService);
        doThrow(EventStreamConfigurationException.class).when(mockEventStreamService).getStreamDefinition(FilesystemTestUtil.STREAM_NAME, FilesystemTestUtil.VERSION);
        fileSystemStreamDefinitionStore.getStreamDefinitionFromStore(FilesystemTestUtil.STREAM_NAME, FilesystemTestUtil.VERSION, FilesystemTestUtil.TENANT_ID);
    }

    @Test(expected = StreamDefinitionStoreException.class)
    public void test3() throws StreamDefinitionStoreException, EventStreamConfigurationException {
        serviceHolderMockedStatic.when(ServiceHolder::getEventStreamService).thenReturn(mockEventStreamService);
        doThrow(EventStreamConfigurationException.class).when(mockEventStreamService).getStreamDefinition(FilesystemTestUtil.STREAM_ID);
        fileSystemStreamDefinitionStore.getStreamDefinitionFromStore(FilesystemTestUtil.STREAM_ID, FilesystemTestUtil.TENANT_ID);
    }

    @Test
    public void testFileSystemStreamDefintionStoreRemoveStreamDefintion() throws MalformedStreamDefinitionException, StreamDefinitionStoreException, EventStreamConfigurationException {
        serviceHolderMockedStatic.when(ServiceHolder::getEventStreamService).thenReturn(mockEventStreamService);
        doThrow(EventStreamConfigurationException.class).when(mockEventStreamService).getAllStreamDefinitions();
        fileSystemStreamDefinitionStore.removeStreamDefinition(FilesystemTestUtil.STREAM_NAME, FilesystemTestUtil.VERSION, FilesystemTestUtil.TENANT_ID);
    }

    @Test
    public void testEventStreamListenerImpl() {
        EventStreamListenerImpl eventStreamListener = new EventStreamListenerImpl();
        eventStreamListener.addedEventStream(FilesystemTestUtil.TENANT_ID, FilesystemTestUtil.STREAM_NAME, FilesystemTestUtil.VERSION);
        eventStreamListener.removedEventStream(FilesystemTestUtil.TENANT_ID, FilesystemTestUtil.STREAM_NAME, FilesystemTestUtil.VERSION);
    }
}
