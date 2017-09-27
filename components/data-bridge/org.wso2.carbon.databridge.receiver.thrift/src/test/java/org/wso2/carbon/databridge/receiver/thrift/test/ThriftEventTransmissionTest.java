/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.databridge.receiver.thrift.test;

import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.SessionTimeoutException;
import org.wso2.carbon.databridge.commons.thrift.data.ThriftEventBundle;
import org.wso2.carbon.databridge.commons.thrift.exception.ThriftAuthenticationException;
import org.wso2.carbon.databridge.commons.thrift.exception.ThriftDifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.thrift.exception.ThriftMalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.thrift.exception.ThriftNoStreamDefinitionExistException;
import org.wso2.carbon.databridge.commons.thrift.exception.ThriftSessionExpiredException;
import org.wso2.carbon.databridge.core.DataBridge;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.receiver.thrift.service.ThriftEventTransmissionServiceImpl;
import org.wso2.carbon.databridge.receiver.thrift.service.ThriftSecureEventTransmissionServiceImpl;


import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;


@RunWith(PowerMockRunner.class)
@PrepareForTest(DataBridgeReceiverService.class)
public class ThriftEventTransmissionTest {

    private static final String MOCK_STREAM_NAME = "org.wso2.esb.MediatorStatistics";
    private static final String MOCK_VERSION = "1.0.0";
    private static final String STREAM_DEFINITION = "";
    private static final String MOCK_RETURN_STREAM_ID = "returnStreamId";
    private static final String MOCK_STREAM_ID = "streamId";
    private static final String MOCK_NORMAL_SESSION = "normalSessionId";
    private static final String ALREADY_DEFINED_EXEP_SESSION = "alreadyDefinedMockExceptionSessionId";
    private static final String MALFORMED_EXEP_SESSION = "malformedMockExceptionSessionId";
    private static final String NO_STREAM_DEF_EXEP_SESSION = "noStreamDefinitionMockExceptionSessionId";
    private static final String SESSION_TIMEOUT_SESSION = "sessionTimeoutMockExceptionSessionId";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String WRONG_PASSWORD = "wrongPassword";
    private static final String RETURN_SESSION_ID = "sessionId";
    private ThriftEventTransmissionServiceImpl service = null;
    private ThriftSecureEventTransmissionServiceImpl secureService = null;
    private ThriftEventBundle thriftEventBundle = new ThriftEventBundle();

    @Before
    @SuppressWarnings("unchecked")
    public void initialize() throws Exception {

        DataBridgeReceiverService dataBridge = mock(DataBridge.class);
        when(dataBridge.defineStream(MOCK_NORMAL_SESSION, STREAM_DEFINITION)).thenReturn(MOCK_RETURN_STREAM_ID);
        when(dataBridge.defineStream(ALREADY_DEFINED_EXEP_SESSION, STREAM_DEFINITION)).
                thenThrow(DifferentStreamDefinitionAlreadyDefinedException.class);
        when(dataBridge.defineStream(MALFORMED_EXEP_SESSION, STREAM_DEFINITION)).
                thenThrow(MalformedStreamDefinitionException.class);
        when(dataBridge.defineStream(SESSION_TIMEOUT_SESSION, STREAM_DEFINITION)).
                thenThrow(SessionTimeoutException.class);

        when(dataBridge.findStreamId(MOCK_NORMAL_SESSION, MOCK_STREAM_NAME, MOCK_VERSION)).
                thenReturn(MOCK_RETURN_STREAM_ID);
        when(dataBridge.findStreamId(SESSION_TIMEOUT_SESSION, MOCK_STREAM_NAME, MOCK_VERSION)).
                thenThrow(SessionTimeoutException.class);
        when(dataBridge.findStreamId(NO_STREAM_DEF_EXEP_SESSION, MOCK_STREAM_NAME, MOCK_VERSION)).thenReturn(null);

        when(dataBridge.deleteStream(MOCK_NORMAL_SESSION, MOCK_STREAM_ID)).thenReturn(true);
        when(dataBridge.deleteStream(SESSION_TIMEOUT_SESSION, MOCK_STREAM_ID)).thenThrow(SessionTimeoutException.class);

        when(dataBridge.deleteStream(MOCK_NORMAL_SESSION, MOCK_STREAM_NAME, MOCK_VERSION)).thenReturn(true);
        when(dataBridge.deleteStream(SESSION_TIMEOUT_SESSION, MOCK_STREAM_NAME, MOCK_VERSION)).
                thenThrow(SessionTimeoutException.class);

        when(dataBridge.login(USERNAME, PASSWORD)).thenReturn(RETURN_SESSION_ID);
        when(dataBridge.login(USERNAME, WRONG_PASSWORD)).thenThrow(AuthenticationException.class);

        service = new ThriftEventTransmissionServiceImpl(dataBridge);
        secureService = new ThriftSecureEventTransmissionServiceImpl(dataBridge);

    }

    @Test
    public void testNormalMock() throws TException, DifferentStreamDefinitionAlreadyDefinedException,
            StreamDefinitionStoreException {

        service.publish(new ThriftEventBundle());
        secureService.publish(new ThriftEventBundle());

        Assert.assertEquals(MOCK_RETURN_STREAM_ID, service.defineStream(MOCK_NORMAL_SESSION, STREAM_DEFINITION));
        Assert.assertEquals(MOCK_RETURN_STREAM_ID, service.findStreamId(MOCK_NORMAL_SESSION, MOCK_STREAM_NAME, MOCK_VERSION));
        Assert.assertEquals(true, service.deleteStreamById(MOCK_NORMAL_SESSION, MOCK_STREAM_ID));
        Assert.assertEquals(true, service.deleteStreamByNameVersion(MOCK_NORMAL_SESSION, MOCK_STREAM_NAME,
                MOCK_VERSION));

        Assert.assertEquals(MOCK_RETURN_STREAM_ID, secureService.defineStream(MOCK_NORMAL_SESSION, STREAM_DEFINITION));
        Assert.assertEquals(MOCK_RETURN_STREAM_ID, secureService.findStreamId(MOCK_NORMAL_SESSION, MOCK_STREAM_NAME, MOCK_VERSION));
        Assert.assertEquals(true, secureService.deleteStreamById(MOCK_NORMAL_SESSION, MOCK_STREAM_ID));
        Assert.assertEquals(true, secureService.deleteStreamByNameVersion(MOCK_NORMAL_SESSION, MOCK_STREAM_NAME,
                MOCK_VERSION));

        Assert.assertEquals(RETURN_SESSION_ID, secureService.connect(USERNAME, PASSWORD));
        secureService.disconnect(MOCK_NORMAL_SESSION);

    }

    @Test(expected = ThriftDifferentStreamDefinitionAlreadyDefinedException.class)
    public void testThriftDifferentStreamDefinitionAlreadyDefinedException() throws TException {
        service.defineStream(ALREADY_DEFINED_EXEP_SESSION, STREAM_DEFINITION);
    }

    @Test(expected = ThriftMalformedStreamDefinitionException.class)
    public void testThriftMalformedStreamDefinitionException() throws TException {
        service.defineStream(MALFORMED_EXEP_SESSION, STREAM_DEFINITION);
    }

    @Test(expected = ThriftSessionExpiredException.class)
    public void testDefineStreamThriftSessionExpiredException() throws TException {
        service.defineStream(SESSION_TIMEOUT_SESSION, STREAM_DEFINITION);
    }

    @Test(expected = ThriftSessionExpiredException.class)
    public void testFindStreamIdThriftSessionExpiredException() throws TException {
        service.findStreamId(SESSION_TIMEOUT_SESSION, MOCK_STREAM_NAME, MOCK_VERSION);
    }

    @Test(expected = ThriftSessionExpiredException.class)
    public void testDeleteStreamByIdThriftSessionExpiredException() throws TException {
        service.deleteStreamById(SESSION_TIMEOUT_SESSION, MOCK_STREAM_ID);
    }

    @Test(expected = ThriftSessionExpiredException.class)
    public void testDeleteStreamByNameVersionThriftSessionExpiredException() throws TException {
        service.deleteStreamByNameVersion(SESSION_TIMEOUT_SESSION, MOCK_STREAM_NAME, MOCK_VERSION);
    }

    @Test(expected = ThriftNoStreamDefinitionExistException.class)
    public void testThriftNoStreamDefinitionExistException() throws TException {
        service.findStreamId(NO_STREAM_DEF_EXEP_SESSION, MOCK_STREAM_NAME, MOCK_VERSION);
    }

    @Test(expected = ThriftDifferentStreamDefinitionAlreadyDefinedException.class)
    public void testSecureThriftDifferentStreamDefinitionAlreadyDefinedException() throws TException {
        secureService.defineStream(ALREADY_DEFINED_EXEP_SESSION, STREAM_DEFINITION);
    }

    @Test(expected = ThriftMalformedStreamDefinitionException.class)
    public void testSecureThriftMalformedStreamDefinitionException() throws TException {
        secureService.defineStream(MALFORMED_EXEP_SESSION, STREAM_DEFINITION);
    }

    @Test(expected = ThriftSessionExpiredException.class)
    public void testSecureDefineStreamThriftSessionExpiredException() throws TException {
        secureService.defineStream(SESSION_TIMEOUT_SESSION, STREAM_DEFINITION);
    }

    @Test(expected = ThriftSessionExpiredException.class)
    public void testSecureFindStreamIdThriftSessionExpiredException() throws TException {
        secureService.findStreamId(SESSION_TIMEOUT_SESSION, MOCK_STREAM_NAME, MOCK_VERSION);
    }

    @Test(expected = ThriftSessionExpiredException.class)
    public void testSecureDeleteStreamByIdThriftSessionExpiredException() throws TException {
        secureService.deleteStreamById(SESSION_TIMEOUT_SESSION, MOCK_STREAM_ID);
    }

    @Test(expected = ThriftSessionExpiredException.class)
    public void testSecureDeleteStreamByNameVersionThriftSessionExpiredException() throws TException {
        secureService.deleteStreamByNameVersion(SESSION_TIMEOUT_SESSION, MOCK_STREAM_NAME, MOCK_VERSION);
    }

    @Test(expected = ThriftNoStreamDefinitionExistException.class)
    public void testSecureThriftNoStreamDefinitionExistException() throws TException {
        secureService.findStreamId(NO_STREAM_DEF_EXEP_SESSION, MOCK_STREAM_NAME, MOCK_VERSION);
    }

    @Test(expected = ThriftAuthenticationException.class)
    public void testSecureThriftAuthenticationException() throws TException {
        secureService.connect(USERNAME, WRONG_PASSWORD);
    }
}
