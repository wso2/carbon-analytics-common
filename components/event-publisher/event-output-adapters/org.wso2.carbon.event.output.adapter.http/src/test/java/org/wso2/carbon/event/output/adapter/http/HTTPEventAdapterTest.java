/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.output.adapter.http;

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.event.output.adapter.core.EventAdapterSecretProcessor;
import org.wso2.carbon.event.output.adapter.core.EventAdapterUtil;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterRuntimeException;
import org.wso2.carbon.event.output.adapter.core.internal.ds.OutputEventAdapterServiceValueHolder;
import org.wso2.carbon.identity.external.api.client.api.constant.ErrorMessageConstant;
import org.wso2.carbon.identity.external.api.client.api.exception.APIClientException;
import org.wso2.carbon.identity.external.api.client.api.model.APIRequestContext;
import org.wso2.carbon.identity.external.api.client.api.model.APIResponse;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.INTERNAL_REFRESH_TOKEN;

/**
 * Unit tests for the HTTPEventAdapter.
 */
public class HTTPEventAdapterTest {

    @BeforeClass
    public void globalSetup() {

        SecretManager secretManager = new SecretManagerImpl();
        OutputEventAdapterServiceValueHolder.setSecretManager(secretManager);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private HTTPEventAdapter buildAdapter(String clientMethod, String authType,
                                          Map<String, String> extraStaticProps) {

        OutputEventAdapterConfiguration config = new OutputEventAdapterConfiguration();
        config.setName("TestHttpAdaptor");
        config.setType("http");
        config.setMessageFormat("json");

        Map<String, String> staticProps = new HashMap<>();
        staticProps.put("http.client.method", clientMethod);
        if (authType != null) {
            staticProps.put("http.authType", authType);
        }
        if (extraStaticProps != null) {
            staticProps.putAll(extraStaticProps);
        }
        config.setStaticProperties(staticProps);

        Map<String, String> globalProperties = new HashMap<>();
        globalProperties.put("jobQueueSize", "10000");
        globalProperties.put("keepAliveTimeInMillis", "20000");
        globalProperties.put("maxThread", "100");
        globalProperties.put("minThread", "8");
        globalProperties.put("defaultMaxConnectionsPerHost", "50");
        globalProperties.put("maxTotalConnections", "1000");

        return new HTTPEventAdapter(config, globalProperties);
    }

    private HTTPEventAdapter buildNoneAuthPostAdapter() {

        return buildAdapter("HttpPost", "NONE", null);
    }

    private SyncHttpClientManager injectMockManager(HTTPEventAdapter adapter) throws Exception {

        SyncHttpClientManager mockManager = mock(SyncHttpClientManager.class);
        Field field = HTTPEventAdapter.class.getDeclaredField("syncHttpClientManager");
        field.setAccessible(true);
        field.set(adapter, mockManager);
        return mockManager;
    }

    private void setContentType(HTTPEventAdapter adapter, String contentType) throws Exception {

        Field f = HTTPEventAdapter.class.getDeclaredField("contentType");
        f.setAccessible(true);
        f.set(adapter, contentType);
    }

    private void setInternalAccessToken(HTTPEventAdapter adapter, String token) throws Exception {

        Field f = HTTPEventAdapter.class.getDeclaredField("internalAccessToken");
        f.setAccessible(true);
        f.set(adapter, token);
    }

    private APIResponse mockResponse(int statusCode, String body) {

        APIResponse response = mock(APIResponse.class);
        when(response.getStatusCode()).thenReturn(statusCode);
        when(response.getResponseBody()).thenReturn(body);
        return response;
    }

    private Map<String, String> defaultDynamicProps() {

        Map<String, String> dp = new HashMap<>();
        dp.put("http.url", "http://localhost:8080/api/test");
        return dp;
    }

    private Map<String, String> clientCredentialStaticProps() {

        Map<String, String> props = new HashMap<>();
        props.put("http.clientId", "test-client-id");
        props.put("http.clientSecret", "test-client-secret");
        props.put("http.tokenEndpoint", "https://localhost:9443/oauth2/token");
        props.put("http.scopes", "openid");
        return props;
    }

    private Map<String, String> passwordCredentialStaticProps() {

        Map<String, String> props = new HashMap<>();
        props.put("http.clientId", "test-client-id");
        props.put("http.clientSecret", "test-client-secret");
        props.put("http.username", "test-username");
        props.put("http.password", "test-password");
        props.put("http.tokenEndpoint", "https://localhost:9443/oauth2/token");
        props.put("http.scopes", "openid");
        return props;
    }

    // -----------------------------------------------------------------------
    // isSync
    // -----------------------------------------------------------------------

    @Test
    public void testIsSync_trueWhenFlagIsTrue() {

        HTTPEventAdapter adapter = buildNoneAuthPostAdapter();
        Map<String, String> dp = new HashMap<>();
        dp.put("http.sync", "true");
        Assert.assertTrue(adapter.isSync(dp));
    }

    @Test
    public void testIsSync_falseWhenFlagIsFalse() {

        HTTPEventAdapter adapter = buildNoneAuthPostAdapter();
        Map<String, String> dp = new HashMap<>();
        dp.put("http.sync", "false");
        Assert.assertFalse(adapter.isSync(dp));
    }

    @Test
    public void testIsSync_falseWhenKeyAbsent() {

        HTTPEventAdapter adapter = buildNoneAuthPostAdapter();
        Assert.assertFalse(adapter.isSync(new HashMap<>()));
    }

    // -----------------------------------------------------------------------
    // connectSync
    // -----------------------------------------------------------------------

    @Test
    public void testConnectSync_isIdempotentWhenManagerAlreadyInitialized() throws Exception {

        HTTPEventAdapter adapter = buildNoneAuthPostAdapter();
        SyncHttpClientManager existingManager = injectMockManager(adapter);

        // Second call with manager already present must be a no-op.
        adapter.connectSync();

        Field f = HTTPEventAdapter.class.getDeclaredField("syncHttpClientManager");
        f.setAccessible(true);
        Assert.assertSame(existingManager, f.get(adapter),
                "connectSync() must not replace an already-initialised SyncHttpClientManager");
    }

    // -----------------------------------------------------------------------
    // disconnectSync
    // -----------------------------------------------------------------------

    @Test
    public void testDisconnectSync_whenNotInitialized_isNoOp() throws Exception {

        HTTPEventAdapter adapter = buildNoneAuthPostAdapter();
        // Must not throw even though syncHttpClientManager is null.
        adapter.disconnectSync();

        Field f = HTTPEventAdapter.class.getDeclaredField("syncHttpClientManager");
        f.setAccessible(true);
        Assert.assertNull(f.get(adapter));
    }

    @Test
    public void testDisconnectSync_closesManagerAndNullsField() throws Exception {

        HTTPEventAdapter adapter = buildNoneAuthPostAdapter();
        SyncHttpClientManager mockManager = injectMockManager(adapter);

        adapter.disconnectSync();

        verify(mockManager).close();
        Field f = HTTPEventAdapter.class.getDeclaredField("syncHttpClientManager");
        f.setAccessible(true);
        Assert.assertNull(f.get(adapter), "syncHttpClientManager must be null after disconnectSync");
    }

    @Test
    public void testDisconnectSync_ioException_fieldStillNulled() throws Exception {

        HTTPEventAdapter adapter = buildNoneAuthPostAdapter();
        SyncHttpClientManager mockManager = injectMockManager(adapter);
        Mockito.doThrow(new IOException("close failed")).when(mockManager).close();

        // IOException must be swallowed (logged as warn) – must not propagate.
        adapter.disconnectSync();

        Field f = HTTPEventAdapter.class.getDeclaredField("syncHttpClientManager");
        f.setAccessible(true);
        Assert.assertNull(f.get(adapter),
                "syncHttpClientManager must still be nulled even when close() throws IOException");
    }

    // -----------------------------------------------------------------------
    // publishSync — not initialised guard
    // -----------------------------------------------------------------------

    @Test(expectedExceptions = OutputEventAdapterException.class)
    public void testPublishSync_throwsWhenNotConnected() throws Exception {

        HTTPEventAdapter adapter = buildNoneAuthPostAdapter();
        adapter.publishSync("payload", defaultDynamicProps());
    }

    // -----------------------------------------------------------------------
    // publishSync — HTTP method selection
    // -----------------------------------------------------------------------

    @Test
    public void testPublishSync_postMethod_usesPostHttpMethod() throws Exception {

        HTTPEventAdapter adapter = buildNoneAuthPostAdapter();
        SyncHttpClientManager mockManager = injectMockManager(adapter);
        setContentType(adapter, "application/json");

        APIResponse response = mockResponse(200, "OK");
        when(mockManager.send(anyString(), any(), anyMap(), anyString())).thenReturn(response);

        adapter.publishSync("payload", defaultDynamicProps());

        ArgumentCaptor<APIRequestContext.HttpMethod> methodCaptor =
                ArgumentCaptor.forClass(APIRequestContext.HttpMethod.class);
        verify(mockManager).send(anyString(), methodCaptor.capture(), anyMap(), anyString());
        Assert.assertEquals(APIRequestContext.HttpMethod.POST, methodCaptor.getValue());
    }

    @Test
    public void testPublishSync_putMethod_usesPutHttpMethod() throws Exception {

        HTTPEventAdapter adapter = buildAdapter("HttpPut", "NONE", null);
        SyncHttpClientManager mockManager = injectMockManager(adapter);
        setContentType(adapter, "application/json");

        APIResponse response = mockResponse(200, "OK");
        when(mockManager.send(anyString(), any(), anyMap(), anyString())).thenReturn(response);

        adapter.publishSync("payload", defaultDynamicProps());

        ArgumentCaptor<APIRequestContext.HttpMethod> methodCaptor =
                ArgumentCaptor.forClass(APIRequestContext.HttpMethod.class);
        verify(mockManager).send(anyString(), methodCaptor.capture(), anyMap(), anyString());
        Assert.assertEquals(APIRequestContext.HttpMethod.PUT, methodCaptor.getValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPublishSync_getMethod_doesNotAddContentTypeHeader() throws Exception {

        HTTPEventAdapter adapter = buildAdapter("HttpGet", "NONE", null);
        SyncHttpClientManager mockManager = injectMockManager(adapter);
        setContentType(adapter, "application/json");

        APIResponse response = mockResponse(200, "OK");
        when(mockManager.send(anyString(), any(), anyMap(), anyString())).thenReturn(response);

        adapter.publishSync("payload", defaultDynamicProps());

        ArgumentCaptor<Map<String, String>> headersCaptor =
                ArgumentCaptor.forClass((Class<Map<String, String>>) (Class<?>) Map.class);
        verify(mockManager).send(anyString(), any(), headersCaptor.capture(), anyString());
        Assert.assertFalse(headersCaptor.getValue().containsKey("Content-Type"),
                "GET requests must not include a Content-Type header");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPublishSync_postMethod_addsContentTypeHeader() throws Exception {

        HTTPEventAdapter adapter = buildNoneAuthPostAdapter();
        SyncHttpClientManager mockManager = injectMockManager(adapter);
        setContentType(adapter, "application/json");

        APIResponse response = mockResponse(200, "OK");
        when(mockManager.send(anyString(), any(), anyMap(), anyString())).thenReturn(response);

        adapter.publishSync("payload", defaultDynamicProps());

        ArgumentCaptor<Map<String, String>> headersCaptor =
                ArgumentCaptor.forClass((Class<Map<String, String>>) (Class<?>) Map.class);
        verify(mockManager).send(anyString(), any(), headersCaptor.capture(), anyString());
        Assert.assertEquals("application/json", headersCaptor.getValue().get("Content-Type"));
    }

    // -----------------------------------------------------------------------
    // publishSync — error response codes
    // -----------------------------------------------------------------------

    @Test(expectedExceptions = OutputEventAdapterException.class)
    public void testPublishSync_400_throwsOutputException() throws Exception {

        HTTPEventAdapter adapter = buildNoneAuthPostAdapter();
        SyncHttpClientManager mockManager = injectMockManager(adapter);
        setContentType(adapter, "application/json");

        APIResponse response = mockResponse(400, "Bad Request");
        when(mockManager.send(anyString(), any(), anyMap(), anyString())).thenReturn(response);

        adapter.publishSync("payload", defaultDynamicProps());
    }

    @Test(expectedExceptions = OutputEventAdapterException.class)
    public void testPublishSync_401_nonClientCredential_throwsOutputException() throws Exception {

        HTTPEventAdapter adapter = buildNoneAuthPostAdapter();
        SyncHttpClientManager mockManager = injectMockManager(adapter);
        setContentType(adapter, "application/json");

        APIResponse response = mockResponse(401, "Unauthorized");
        when(mockManager.send(anyString(), any(), anyMap(), anyString())).thenReturn(response);

        adapter.publishSync("payload", defaultDynamicProps());
    }

    @Test(expectedExceptions = OutputEventAdapterException.class)
    public void testPublishSync_403_nonClientCredential_throwsOutputException() throws Exception {

        HTTPEventAdapter adapter = buildNoneAuthPostAdapter();
        SyncHttpClientManager mockManager = injectMockManager(adapter);
        setContentType(adapter, "application/json");

        APIResponse response = mockResponse(403, "Forbidden");
        when(mockManager.send(anyString(), any(), anyMap(), anyString())).thenReturn(response);

        adapter.publishSync("payload", defaultDynamicProps());
    }

    @Test(expectedExceptions = OutputEventAdapterException.class)
    public void testPublishSync_429_throwsOutputException() throws Exception {

        HTTPEventAdapter adapter = buildNoneAuthPostAdapter();
        SyncHttpClientManager mockManager = injectMockManager(adapter);
        setContentType(adapter, "application/json");

        APIResponse response = mockResponse(429, "Too Many Requests");
        when(mockManager.send(anyString(), any(), anyMap(), anyString())).thenReturn(response);

        adapter.publishSync("payload", defaultDynamicProps());
    }

    @Test(expectedExceptions = OutputEventAdapterException.class)
    public void testPublishSync_500_throwsOutputException() throws Exception {

        HTTPEventAdapter adapter = buildNoneAuthPostAdapter();
        SyncHttpClientManager mockManager = injectMockManager(adapter);
        setContentType(adapter, "application/json");

        APIResponse response = mockResponse(500, "Internal Server Error");
        when(mockManager.send(anyString(), any(), anyMap(), anyString())).thenReturn(response);

        adapter.publishSync("payload", defaultDynamicProps());
    }

    @Test(expectedExceptions = OutputEventAdapterException.class)
    public void testPublishSync_503_throwsOutputException() throws Exception {

        HTTPEventAdapter adapter = buildNoneAuthPostAdapter();
        SyncHttpClientManager mockManager = injectMockManager(adapter);
        setContentType(adapter, "application/json");

        APIResponse response = mockResponse(503, "Service Unavailable");
        when(mockManager.send(anyString(), any(), anyMap(), anyString())).thenReturn(response);

        adapter.publishSync("payload", defaultDynamicProps());
    }

    @Test(expectedExceptions = OutputEventAdapterException.class)
    public void testPublishSync_unknownErrorCode_throwsOutputException() throws Exception {

        HTTPEventAdapter adapter = buildNoneAuthPostAdapter();
        SyncHttpClientManager mockManager = injectMockManager(adapter);
        setContentType(adapter, "application/json");

        APIResponse response = mockResponse(422, "Unprocessable Entity");
        when(mockManager.send(anyString(), any(), anyMap(), anyString())).thenReturn(response);

        adapter.publishSync("payload", defaultDynamicProps());
    }

    // -----------------------------------------------------------------------
    // publishSync — APIClientException wrapped as OutputEventAdapterException
    // -----------------------------------------------------------------------

    @Test(expectedExceptions = OutputEventAdapterException.class)
    public void testPublishSync_apiClientException_wrappedAsOutputAdapterException() throws Exception {

        HTTPEventAdapter adapter = buildNoneAuthPostAdapter();
        SyncHttpClientManager mockManager = injectMockManager(adapter);
        setContentType(adapter, "application/json");
        when(mockManager.send(anyString(), any(), anyMap(), anyString()))
                .thenThrow(new APIClientException(
                        ErrorMessageConstant.ErrorMessage.ERROR_CODE_WHILE_INVOKING_API,
                        "connection timed out"));

        adapter.publishSync("payload", defaultDynamicProps());
    }

    // -----------------------------------------------------------------------
    // publishSync — CLIENT_CREDENTIAL 401/403 triggers token refresh and retry
    // -----------------------------------------------------------------------

    @Test
    public void testPublishSync_clientCredential_401_refreshesTokenAndRetries_success() throws Exception {

        HTTPEventAdapter adapter = buildAdapter("HttpPost", "CLIENT_CREDENTIAL", clientCredentialStaticProps());
        SyncHttpClientManager mockManager = injectMockManager(adapter);
        setContentType(adapter, "application/json");
        // Pre-set a token so resolveAuthProperties() skips the initial token-fetch call.
        setInternalAccessToken(adapter, "initial-token");

        APIResponse unauthorized = mockResponse(401, "Unauthorized");
        APIResponse success = mockResponse(200, "OK");

        try (MockedStatic<EventAdapterSecretProcessor> mockedSecretProcessor =
                     Mockito.mockStatic(EventAdapterSecretProcessor.class);
             MockedStatic<EventAdapterUtil> mockedUtil =
                     Mockito.mockStatic(EventAdapterUtil.class)) {

            // fetchNewAccessToken() falls back to static-property credentials when decryption fails.
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor
                    .decryptCredential(anyString(), anyString(), anyString()))
                    .thenThrow(new SecretManagementException("not stored"));
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor
                    .encryptAndStoreCredential(anyString(), anyString(), anyString(), anyString()))
                    .thenAnswer(inv -> null);
            mockedUtil.when(() -> EventAdapterUtil
                    .getTokenResponse(anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(new EventAdapterUtil.TokenResponse("refreshed-token", null));

            when(mockManager.send(anyString(), any(), anyMap(), anyString()))
                    .thenReturn(unauthorized)
                    .thenReturn(success);

            adapter.publishSync("payload", defaultDynamicProps());

            verify(mockManager, times(2)).send(anyString(), any(), anyMap(), anyString());
            mockedUtil.verify(() -> EventAdapterUtil.getTokenResponse(
                    anyString(), anyString(), anyString(), anyString()), times(1));
        }
    }

    @Test(expectedExceptions = OutputEventAdapterException.class)
    public void testPublishSync_clientCredential_401_retryAlsoFails_throwsException() throws Exception {

        HTTPEventAdapter adapter = buildAdapter("HttpPost", "CLIENT_CREDENTIAL", clientCredentialStaticProps());
        SyncHttpClientManager mockManager = injectMockManager(adapter);
        setContentType(adapter, "application/json");
        setInternalAccessToken(adapter, "initial-token");

        APIResponse unauthorized = mockResponse(401, "Unauthorized");
        APIResponse forbidden = mockResponse(403, "Forbidden after refresh");

        try (MockedStatic<EventAdapterSecretProcessor> mockedSecretProcessor =
                     Mockito.mockStatic(EventAdapterSecretProcessor.class);
             MockedStatic<EventAdapterUtil> mockedUtil =
                     Mockito.mockStatic(EventAdapterUtil.class)) {

            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor
                    .decryptCredential(anyString(), anyString(), anyString()))
                    .thenThrow(new SecretManagementException("not stored"));
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor
                    .encryptAndStoreCredential(anyString(), anyString(), anyString(), anyString()))
                    .thenAnswer(inv -> null);
            mockedUtil.when(() -> EventAdapterUtil
                    .getTokenResponse(anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(new EventAdapterUtil.TokenResponse("refreshed-token", null));

            when(mockManager.send(anyString(), any(), anyMap(), anyString()))
                    .thenReturn(unauthorized)
                    .thenReturn(forbidden);

            adapter.publishSync("payload", defaultDynamicProps());
        }
    }

    @Test
    public void testPublishSync_clientCredential_403_refreshesTokenAndRetries_success() throws Exception {

        HTTPEventAdapter adapter = buildAdapter("HttpPost", "CLIENT_CREDENTIAL", clientCredentialStaticProps());
        SyncHttpClientManager mockManager = injectMockManager(adapter);
        setContentType(adapter, "application/json");
        setInternalAccessToken(adapter, "initial-token");

        APIResponse forbidden = mockResponse(403, "Forbidden");
        APIResponse success = mockResponse(200, "OK");

        try (MockedStatic<EventAdapterSecretProcessor> mockedSecretProcessor =
                     Mockito.mockStatic(EventAdapterSecretProcessor.class);
             MockedStatic<EventAdapterUtil> mockedUtil =
                     Mockito.mockStatic(EventAdapterUtil.class)) {

            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor
                    .decryptCredential(anyString(), anyString(), anyString()))
                    .thenThrow(new SecretManagementException("not stored"));
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor
                    .encryptAndStoreCredential(anyString(), anyString(), anyString(), anyString()))
                    .thenAnswer(inv -> null);
            mockedUtil.when(() -> EventAdapterUtil
                    .getTokenResponse(anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(new EventAdapterUtil.TokenResponse("refreshed-token", null));

            when(mockManager.send(anyString(), any(), anyMap(), anyString()))
                    .thenReturn(forbidden)
                    .thenReturn(success);

            adapter.publishSync("payload", defaultDynamicProps());

            verify(mockManager, times(2)).send(anyString(), any(), anyMap(), anyString());
        }
    }

    // -----------------------------------------------------------------------
    // publishSync — PASSWORD_CREDENTIAL 401/403 triggers token refresh and retry
    // -----------------------------------------------------------------------

    @Test
    public void testPublishSync_passwordCredential_401_refreshesTokenAndRetries_success() throws Exception {

        HTTPEventAdapter adapter = buildAdapter("HttpPost", "PASSWORD_CREDENTIAL", passwordCredentialStaticProps());
        SyncHttpClientManager mockManager = injectMockManager(adapter);
        setContentType(adapter, "application/json");
        // Pre-set a token so resolveAuthProperties() skips the initial token-fetch call.
        setInternalAccessToken(adapter, "initial-token");

        APIResponse unauthorized = mockResponse(401, "Unauthorized");
        APIResponse success = mockResponse(200, "OK");

        try (MockedStatic<EventAdapterSecretProcessor> mockedSecretProcessor =
                     Mockito.mockStatic(EventAdapterSecretProcessor.class);
             MockedStatic<EventAdapterUtil> mockedUtil =
                     Mockito.mockStatic(EventAdapterUtil.class)) {

            // fetchNewAccessToken() falls back to static-property credentials when decryption fails.
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor
                    .decryptCredential(anyString(), anyString(), anyString()))
                    .thenThrow(new SecretManagementException("not stored"));
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor
                    .encryptAndStoreCredential(anyString(), anyString(), anyString(), anyString()))
                    .thenAnswer(inv -> null);
            mockedUtil.when(() -> EventAdapterUtil
                    .getTokenResponsePasswordGrant(anyString(), anyString(), anyString(), anyString(), anyString(),
                            anyString()))
                    .thenReturn(new EventAdapterUtil.TokenResponse("refreshed-token", null));

            when(mockManager.send(anyString(), any(), anyMap(), anyString()))
                    .thenReturn(unauthorized)
                    .thenReturn(success);

            adapter.publishSync("payload", defaultDynamicProps());

            verify(mockManager, times(2)).send(anyString(), any(), anyMap(), anyString());
            mockedUtil.verify(() -> EventAdapterUtil.getTokenResponsePasswordGrant(
                    anyString(), anyString(), anyString(), anyString(), anyString(), anyString()), times(1));
            mockedUtil.verify(() -> EventAdapterUtil.getTokenResponse(
                    anyString(), anyString(), anyString(), anyString()), times(0));
        }
    }

    @Test(expectedExceptions = OutputEventAdapterException.class)
    public void testPublishSync_passwordCredential_401_retryAlsoFails_throwsException() throws Exception {

        HTTPEventAdapter adapter = buildAdapter("HttpPost", "PASSWORD_CREDENTIAL", passwordCredentialStaticProps());
        SyncHttpClientManager mockManager = injectMockManager(adapter);
        setContentType(adapter, "application/json");
        setInternalAccessToken(adapter, "initial-token");

        APIResponse unauthorized = mockResponse(401, "Unauthorized");
        APIResponse forbidden = mockResponse(403, "Forbidden after refresh");

        try (MockedStatic<EventAdapterSecretProcessor> mockedSecretProcessor =
                     Mockito.mockStatic(EventAdapterSecretProcessor.class);
             MockedStatic<EventAdapterUtil> mockedUtil =
                     Mockito.mockStatic(EventAdapterUtil.class)) {

            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor
                    .decryptCredential(anyString(), anyString(), anyString()))
                    .thenThrow(new SecretManagementException("not stored"));
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor
                    .encryptAndStoreCredential(anyString(), anyString(), anyString(), anyString()))
                    .thenAnswer(inv -> null);
            mockedUtil.when(() -> EventAdapterUtil
                    .getTokenResponsePasswordGrant(anyString(), anyString(), anyString(), anyString(), anyString(),
                            anyString()))
                    .thenReturn(new EventAdapterUtil.TokenResponse("refreshed-token", null));

            when(mockManager.send(anyString(), any(), anyMap(), anyString()))
                    .thenReturn(unauthorized)
                    .thenReturn(forbidden);

            adapter.publishSync("payload", defaultDynamicProps());
        }
    }

    @Test
    public void testPublishSync_passwordCredential_403_refreshesTokenAndRetries_success() throws Exception {

        HTTPEventAdapter adapter = buildAdapter("HttpPost", "PASSWORD_CREDENTIAL", passwordCredentialStaticProps());
        SyncHttpClientManager mockManager = injectMockManager(adapter);
        setContentType(adapter, "application/json");
        setInternalAccessToken(adapter, "initial-token");

        APIResponse forbidden = mockResponse(403, "Forbidden");
        APIResponse success = mockResponse(200, "OK");

        try (MockedStatic<EventAdapterSecretProcessor> mockedSecretProcessor =
                     Mockito.mockStatic(EventAdapterSecretProcessor.class);
             MockedStatic<EventAdapterUtil> mockedUtil =
                     Mockito.mockStatic(EventAdapterUtil.class)) {

            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor
                    .decryptCredential(anyString(), anyString(), anyString()))
                    .thenThrow(new SecretManagementException("not stored"));
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor
                    .encryptAndStoreCredential(anyString(), anyString(), anyString(), anyString()))
                    .thenAnswer(inv -> null);
            mockedUtil.when(() -> EventAdapterUtil
                    .getTokenResponsePasswordGrant(anyString(), anyString(), anyString(), anyString(), anyString(),
                            anyString()))
                    .thenReturn(new EventAdapterUtil.TokenResponse("refreshed-token", null));

            when(mockManager.send(anyString(), any(), anyMap(), anyString()))
                    .thenReturn(forbidden)
                    .thenReturn(success);

            adapter.publishSync("payload", defaultDynamicProps());

            verify(mockManager, times(2)).send(anyString(), any(), anyMap(), anyString());
        }
    }

    @Test
    public void testResolveAuthProperties_passwordCredential_fetchesTokenUsingPasswordGrant() throws Exception {

        HTTPEventAdapter adapter = buildAdapter("HttpPost", "PASSWORD_CREDENTIAL", passwordCredentialStaticProps());

        try (MockedStatic<EventAdapterSecretProcessor> mockedSecretProcessor =
                     Mockito.mockStatic(EventAdapterSecretProcessor.class);
             MockedStatic<EventAdapterUtil> mockedUtil =
                     Mockito.mockStatic(EventAdapterUtil.class)) {

            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor
                    .decryptCredential(anyString(), anyString(), anyString()))
                    .thenThrow(new SecretManagementException("not stored"));
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor
                    .encryptAndStoreCredential(anyString(), anyString(), anyString(), anyString()))
                    .thenAnswer(inv -> null);

            ArgumentCaptor<String> clientIdCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> clientSecretCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
            mockedUtil.when(() -> EventAdapterUtil.getTokenResponsePasswordGrant(
                    clientIdCaptor.capture(), clientSecretCaptor.capture(), usernameCaptor.capture(),
                    passwordCaptor.capture(), anyString(), anyString()))
                    .thenReturn(new EventAdapterUtil.TokenResponse("password-grant-token", null));

            SyncHttpClientManager mockManager = injectMockManager(adapter);
            setContentType(adapter, "application/json");
            APIResponse success = mockResponse(200, "OK");
            when(mockManager.send(anyString(), any(), anyMap(), anyString()))
                    .thenReturn(success);

            adapter.publishSync("payload", defaultDynamicProps());

            Assert.assertEquals(clientIdCaptor.getValue(), "test-client-id");
            Assert.assertEquals(clientSecretCaptor.getValue(), "test-client-secret");
            Assert.assertEquals(usernameCaptor.getValue(), "test-username");
            Assert.assertEquals(passwordCaptor.getValue(), "test-password");
        }
    }

    // -----------------------------------------------------------------------
    // Token refresh — a stored refresh token is preferred over a full grant request, and a
    // rejected/expired refresh token falls back to a full grant.
    // -----------------------------------------------------------------------

    @Test
    public void testPublishSync_passwordCredential_401_refreshTokenAvailable_prefersRefreshGrant() throws Exception {

        HTTPEventAdapter adapter = buildAdapter("HttpPost", "PASSWORD_CREDENTIAL", passwordCredentialStaticProps());
        SyncHttpClientManager mockManager = injectMockManager(adapter);
        setContentType(adapter, "application/json");
        setInternalAccessToken(adapter, "initial-token");

        APIResponse unauthorized = mockResponse(401, "Unauthorized");
        APIResponse success = mockResponse(200, "OK");

        try (MockedStatic<EventAdapterSecretProcessor> mockedSecretProcessor =
                     Mockito.mockStatic(EventAdapterSecretProcessor.class);
             MockedStatic<EventAdapterUtil> mockedUtil =
                     Mockito.mockStatic(EventAdapterUtil.class)) {

            // clientId/clientSecret/username/password decryption falls back to static properties.
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor
                    .decryptCredential(anyString(), anyString(), anyString()))
                    .thenThrow(new SecretManagementException("not stored"));
            // But a refresh token IS available in the secret store.
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor
                    .decryptCredential(anyString(), anyString(), eq(INTERNAL_REFRESH_TOKEN)))
                    .thenReturn("stored-refresh-token".toCharArray());
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor
                    .encryptAndStoreCredential(anyString(), anyString(), anyString(), anyString()))
                    .thenAnswer(inv -> null);
            mockedUtil.when(() -> EventAdapterUtil.getTokenResponseUsingRefreshToken(
                            anyString(), anyString(), eq("stored-refresh-token"), anyString(), anyString()))
                    .thenReturn(new EventAdapterUtil.TokenResponse("refreshed-via-refresh-token",
                            "rotated-refresh-token"));

            when(mockManager.send(anyString(), any(), anyMap(), anyString()))
                    .thenReturn(unauthorized)
                    .thenReturn(success);

            adapter.publishSync("payload", defaultDynamicProps());

            verify(mockManager, times(2)).send(anyString(), any(), anyMap(), anyString());
            mockedUtil.verify(() -> EventAdapterUtil.getTokenResponseUsingRefreshToken(
                    anyString(), anyString(), anyString(), anyString(), anyString()), times(1));
            // The full grant (which would resend the resource owner's username/password) must not be attempted.
            mockedUtil.verify(() -> EventAdapterUtil.getTokenResponsePasswordGrant(
                    anyString(), anyString(), anyString(), anyString(), anyString(), anyString()), times(0));
            // The rotated refresh token returned by the server must be persisted for the next renewal.
            mockedSecretProcessor.verify(() -> EventAdapterSecretProcessor.encryptAndStoreCredential(
                    anyString(), anyString(), eq(INTERNAL_REFRESH_TOKEN), eq("rotated-refresh-token")), times(1));
        }
    }

    @Test
    public void testPublishSync_passwordCredential_401_refreshTokenRejected_fallsBackToFullGrant() throws Exception {

        HTTPEventAdapter adapter = buildAdapter("HttpPost", "PASSWORD_CREDENTIAL", passwordCredentialStaticProps());
        SyncHttpClientManager mockManager = injectMockManager(adapter);
        setContentType(adapter, "application/json");
        setInternalAccessToken(adapter, "initial-token");

        APIResponse unauthorized = mockResponse(401, "Unauthorized");
        APIResponse success = mockResponse(200, "OK");

        try (MockedStatic<EventAdapterSecretProcessor> mockedSecretProcessor =
                     Mockito.mockStatic(EventAdapterSecretProcessor.class);
             MockedStatic<EventAdapterUtil> mockedUtil =
                     Mockito.mockStatic(EventAdapterUtil.class)) {

            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor
                    .decryptCredential(anyString(), anyString(), anyString()))
                    .thenThrow(new SecretManagementException("not stored"));
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor
                    .decryptCredential(anyString(), anyString(), eq(INTERNAL_REFRESH_TOKEN)))
                    .thenReturn("expired-refresh-token".toCharArray());
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor
                    .encryptAndStoreCredential(anyString(), anyString(), anyString(), anyString()))
                    .thenAnswer(inv -> null);
            // The stored refresh token has expired or been revoked by the authorization server.
            mockedUtil.when(() -> EventAdapterUtil.getTokenResponseUsingRefreshToken(
                            anyString(), anyString(), anyString(), anyString(), anyString()))
                    .thenThrow(new OutputEventAdapterRuntimeException("invalid_grant"));
            mockedUtil.when(() -> EventAdapterUtil.getTokenResponsePasswordGrant(
                            anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(new EventAdapterUtil.TokenResponse("refreshed-via-full-grant", null));

            when(mockManager.send(anyString(), any(), anyMap(), anyString()))
                    .thenReturn(unauthorized)
                    .thenReturn(success);

            adapter.publishSync("payload", defaultDynamicProps());

            verify(mockManager, times(2)).send(anyString(), any(), anyMap(), anyString());
            mockedUtil.verify(() -> EventAdapterUtil.getTokenResponseUsingRefreshToken(
                    anyString(), anyString(), anyString(), anyString(), anyString()), times(1));
            mockedUtil.verify(() -> EventAdapterUtil.getTokenResponsePasswordGrant(
                    anyString(), anyString(), anyString(), anyString(), anyString(), anyString()), times(1));
        }
    }

    @Test
    public void testPublishSync_clientCredential_401_neverAttemptsRefreshTokenLookup() throws Exception {

        // Per RFC 6749 §4.4.3, an authorization server SHOULD NOT issue a refresh token for the client
        // credentials grant. CLIENT_CREDENTIAL must never even attempt a refresh-token lookup/renewal.
        HTTPEventAdapter adapter = buildAdapter("HttpPost", "CLIENT_CREDENTIAL", clientCredentialStaticProps());
        SyncHttpClientManager mockManager = injectMockManager(adapter);
        setContentType(adapter, "application/json");
        setInternalAccessToken(adapter, "initial-token");

        APIResponse unauthorized = mockResponse(401, "Unauthorized");
        APIResponse success = mockResponse(200, "OK");

        try (MockedStatic<EventAdapterSecretProcessor> mockedSecretProcessor =
                     Mockito.mockStatic(EventAdapterSecretProcessor.class);
             MockedStatic<EventAdapterUtil> mockedUtil =
                     Mockito.mockStatic(EventAdapterUtil.class)) {

            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor
                    .decryptCredential(anyString(), anyString(), anyString()))
                    .thenThrow(new SecretManagementException("not stored"));
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor
                    .encryptAndStoreCredential(anyString(), anyString(), anyString(), anyString()))
                    .thenAnswer(inv -> null);
            mockedUtil.when(() -> EventAdapterUtil.getTokenResponse(
                            anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(new EventAdapterUtil.TokenResponse("refreshed-token", null));

            when(mockManager.send(anyString(), any(), anyMap(), anyString()))
                    .thenReturn(unauthorized)
                    .thenReturn(success);

            adapter.publishSync("payload", defaultDynamicProps());

            mockedSecretProcessor.verify(() -> EventAdapterSecretProcessor.decryptCredential(
                    anyString(), anyString(), eq(INTERNAL_REFRESH_TOKEN)), times(0));
            mockedUtil.verify(() -> EventAdapterUtil.getTokenResponseUsingRefreshToken(
                    anyString(), anyString(), anyString(), anyString(), anyString()), times(0));
        }
    }

    // -----------------------------------------------------------------------
    // publishSync — custom headers from dynamic properties are forwarded
    // -----------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    public void testPublishSync_customHeadersForwarded() throws Exception {

        HTTPEventAdapter adapter = buildNoneAuthPostAdapter();
        SyncHttpClientManager mockManager = injectMockManager(adapter);
        setContentType(adapter, "application/json");

        APIResponse response = mockResponse(200, "OK");
        Map<String, String> dp = defaultDynamicProps();
        dp.put("http.headers", "X-Custom-Header:custom-value");

        when(mockManager.send(anyString(), any(), anyMap(), anyString())).thenReturn(response);

        adapter.publishSync("payload", dp);

        ArgumentCaptor<Map<String, String>> headersCaptor =
                ArgumentCaptor.forClass((Class<Map<String, String>>) (Class<?>) Map.class);
        verify(mockManager).send(anyString(), any(), headersCaptor.capture(), anyString());
        Assert.assertEquals("custom-value", headersCaptor.getValue().get("X-Custom-Header"));
    }

    // -----------------------------------------------------------------------
    // publishSync — BEARER auth header is attached
    // -----------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    public void testPublishSync_bearerAuth_addsAuthorizationHeader() throws Exception {

        Map<String, String> extraProps = new HashMap<>();
        extraProps.put("http.accessToken", "my-bearer-token");
        HTTPEventAdapter adapter = buildAdapter("HttpPost", "BEARER", extraProps);
        SyncHttpClientManager mockManager = injectMockManager(adapter);
        setContentType(adapter, "application/json");

        APIResponse response = mockResponse(200, "OK");

        try (MockedStatic<EventAdapterSecretProcessor> mockedSecretProcessor =
                     Mockito.mockStatic(EventAdapterSecretProcessor.class)) {
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor
                    .decryptCredential(anyString(), anyString(), anyString()))
                    .thenThrow(new SecretManagementException("not stored"));

            when(mockManager.send(anyString(), any(), anyMap(), anyString())).thenReturn(response);

            adapter.publishSync("payload", defaultDynamicProps());

            ArgumentCaptor<Map<String, String>> headersCaptor =
                    ArgumentCaptor.forClass((Class<Map<String, String>>) (Class<?>) Map.class);
            verify(mockManager).send(anyString(), any(), headersCaptor.capture(), anyString());
            Assert.assertTrue(headersCaptor.getValue().containsKey("Authorization"),
                    "Authorization header must be present for BEARER auth");
            Assert.assertTrue(headersCaptor.getValue().get("Authorization").startsWith("Bearer "),
                    "Authorization value must start with 'Bearer '");
        }
    }

    // -----------------------------------------------------------------------
    // publishSync — payload forwarded verbatim (buildBody contract)
    // -----------------------------------------------------------------------

    @Test
    public void testPublishSync_payloadForwardedToManager() throws Exception {

        HTTPEventAdapter adapter = buildNoneAuthPostAdapter();
        SyncHttpClientManager mockManager = injectMockManager(adapter);
        setContentType(adapter, "application/json");

        APIResponse response = mockResponse(200, "OK");
        when(mockManager.send(anyString(), any(), anyMap(), anyString())).thenReturn(response);

        adapter.publishSync("expected-payload", defaultDynamicProps());

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockManager).send(anyString(), any(), anyMap(), payloadCaptor.capture());
        Assert.assertEquals("expected-payload", payloadCaptor.getValue());
    }
}
