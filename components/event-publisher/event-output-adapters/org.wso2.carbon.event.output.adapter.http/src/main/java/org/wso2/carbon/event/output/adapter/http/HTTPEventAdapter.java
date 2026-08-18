/*
 * Copyright (c) 2015-2026, WSO2 LLC. (http://www.wso2.com).
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

import org.apache.axiom.om.util.Base64;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.conn.params.ConnRoutePNames;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.core.EventAdapterUtil;
import org.wso2.carbon.event.output.adapter.core.MessageType;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.ConnectionUnavailableException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterRuntimeException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.identity.external.api.client.api.exception.APIClientException;
import org.wso2.carbon.identity.external.api.client.api.model.APIClientConfig;
import org.wso2.carbon.identity.external.api.client.api.model.APIRequestContext;
import org.wso2.carbon.identity.external.api.client.api.model.APIResponse;
import org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.DiagnosticLog;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

import static org.wso2.carbon.event.output.adapter.core.EventAdapterSecretProcessor.decryptCredential;
import static org.wso2.carbon.event.output.adapter.core.EventAdapterSecretProcessor.encryptAndStoreCredential;
import static org.wso2.carbon.event.output.adapter.core.EventAdapterUtil.getTokenResponse;
import static org.wso2.carbon.event.output.adapter.core.EventAdapterUtil.getTokenResponsePasswordGrant;
import static org.wso2.carbon.event.output.adapter.core.EventAdapterUtil.getTokenResponseUsingRefreshToken;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.ACCESS_TOKEN;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.ADAPTER_ACCESS_TOKEN;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.ADAPTER_API_KEY_HEADER;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.ADAPTER_API_KEY_VALUE;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.ADAPTER_CLIENT_ID;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.ADAPTER_CLIENT_SECRET;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.ADAPTER_PASSWORD;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.ADAPTER_SCOPES;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.ADAPTER_TOKEN_ENDPOINT;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.ADAPTER_USERNAME;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.API_KEY;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.API_KEY_HEADER;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.API_KEY_VALUE;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.BASIC;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.BEARER;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.ADAPTER_SECRET_PROVIDER;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.CLIENT_CREDENTIAL;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.CLIENT_ID;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.CLIENT_SECRET;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.DEFAULT_SECRET_PROVIDER;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.INTERNAL_ACCESS_TOKEN;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.INTERNAL_REFRESH_TOKEN;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.LogConstants.ActionIDs.SEND_EMAIL;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.LogConstants.EMAIL_PUBLISHER_EVENT_ADAPTER_NAME;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.LogConstants.HTTP_EVENT_ADAPTER_SERVICE;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.LogConstants.InputKeys.ERROR_RESPONSE;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.LogConstants.InputKeys.RESPONSE_CODE;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.MAX_RETRY_ATTEMPTS;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.NONE;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.PASSWORD;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.PASSWORD_CREDENTIAL;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.USERNAME;

public class HTTPEventAdapter implements OutputEventAdapter {
    private static final Log log = LogFactory.getLog(OutputEventAdapter.class);
    private OutputEventAdapterConfiguration eventAdapterConfiguration;
    private Map<String, String> globalProperties;
    private static ExecutorService executorService;
    private String clientMethod;
    private String proxyHost = null;
    private String proxyPort = null;
    private int tenantId;

    private String contentType;
    private static HttpConnectionManager connectionManager;
    private HttpClient httpClient = null;
    private HostConfiguration hostConfiguration = null;
    private String internalAccessToken = null;
    private final String provider;
    private SyncHttpClientManager syncHttpClientManager = null;

    public HTTPEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration,
            Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
        this.clientMethod = eventAdapterConfiguration.getStaticProperties()
                .get(HTTPEventAdapterConstants.ADAPTER_HTTP_CLIENT_METHOD);
        // Read provider from static properties; default to EMAIL_PROVIDER for backward compatibility.
        String configuredProvider = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_SECRET_PROVIDER);
        this.provider = StringUtils.isNotBlank(configuredProvider) ? configuredProvider : DEFAULT_SECRET_PROVIDER;
        // Setting the static proxy configurations for the HTTP adapter.
        if (eventAdapterConfiguration.getStaticProperties().get(HTTPEventAdapterConstants.ADAPTER_PROXY_HOST) != null &&
                eventAdapterConfiguration.getStaticProperties().
                        get(HTTPEventAdapterConstants.ADAPTER_PROXY_PORT) != null) {
            this.proxyPort =
                    eventAdapterConfiguration.getStaticProperties().get(HTTPEventAdapterConstants.ADAPTER_PROXY_PORT);
            this.proxyHost =
                    eventAdapterConfiguration.getStaticProperties().get(HTTPEventAdapterConstants.ADAPTER_PROXY_HOST);
        }

    }

    @Override
    public void init() throws OutputEventAdapterException {

        tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        //ExecutorService will be assigned  if it is null
        if (executorService == null) {
            int minThread;
            int maxThread;
            long defaultKeepAliveTime;
            int jobQueSize;

            //If global properties are available those will be assigned else constant values will be assigned
            if (globalProperties.get(HTTPEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE_NAME) != null) {
                minThread = Integer
                        .parseInt(globalProperties.get(HTTPEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE_NAME));
            } else {
                minThread = HTTPEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE;
            }

            if (globalProperties.get(HTTPEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE_NAME) != null) {
                maxThread = Integer
                        .parseInt(globalProperties.get(HTTPEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE_NAME));
            } else {
                maxThread = HTTPEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE;
            }

            if (globalProperties.get(HTTPEventAdapterConstants.ADAPTER_KEEP_ALIVE_TIME_NAME) != null) {
                defaultKeepAliveTime = Integer
                        .parseInt(globalProperties.get(HTTPEventAdapterConstants.ADAPTER_KEEP_ALIVE_TIME_NAME));
            } else {
                defaultKeepAliveTime = HTTPEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_IN_MILLIS;
            }

            if (globalProperties.get(HTTPEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME) != null) {
                jobQueSize = Integer
                        .parseInt(globalProperties.get(HTTPEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME));
            } else {
                jobQueSize = HTTPEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE;
            }
            executorService = new ThreadPoolExecutor(minThread, maxThread, defaultKeepAliveTime, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(jobQueSize));

            //configurations for the httpConnectionManager which will be shared by every http adapter
            int defaultMaxConnectionsPerHost;
            int maxTotalConnections;

            if (globalProperties.get(HTTPEventAdapterConstants.DEFAULT_MAX_CONNECTIONS_PER_HOST) != null) {
                defaultMaxConnectionsPerHost = Integer
                        .parseInt(globalProperties.get(HTTPEventAdapterConstants.DEFAULT_MAX_CONNECTIONS_PER_HOST));
            } else {
                defaultMaxConnectionsPerHost = HTTPEventAdapterConstants.DEFAULT_DEFAULT_MAX_CONNECTIONS_PER_HOST;
            }

            if (globalProperties.get(HTTPEventAdapterConstants.MAX_TOTAL_CONNECTIONS) != null) {
                maxTotalConnections = Integer
                        .parseInt(globalProperties.get(HTTPEventAdapterConstants.MAX_TOTAL_CONNECTIONS));
            } else {
                maxTotalConnections = HTTPEventAdapterConstants.DEFAULT_MAX_TOTAL_CONNECTIONS;
            }

            connectionManager = new MultiThreadedHttpConnectionManager();
            connectionManager.getParams().setDefaultMaxConnectionsPerHost(defaultMaxConnectionsPerHost);
            connectionManager.getParams().setMaxTotalConnections(maxTotalConnections);

        }
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("Test connection is not available");
    }

    @Override
    public void connect() {
        this.checkHTTPClientInit(eventAdapterConfiguration.getStaticProperties());
    }

    /**
     * Build the request body from the event message. Subclasses may override this method to
     * customise how the message is serialised before it is sent to the endpoint.
     *
     * @param message           Event message received from the event publisher framework.
     * @return String body to be sent in the HTTP request.
     */
    protected String buildBody(Object message) {

        return message.toString();
    }

    /**
     * Build the initial set of HTTP request headers derived from the event message and dynamic
     * properties. Subclasses may override this method to add, remove or modify headers before
     * the request is sent. Auth-related headers are added separately by the publish logic and
     * must not be included here.
     *
     * @param message           Event message received from the event publisher framework.
     * @param dynamicProperties Dynamic properties from the event publisher configuration.
     * @return Mutable map of header name to header value, or {@code null} if no extra headers.
     */
    protected Map<String, String> buildHeaders(Object message, Map<String, String> dynamicProperties) {

        return extractHeaders(dynamicProperties.get(HTTPEventAdapterConstants.ADAPTER_HEADERS));
    }

    @Override
    public void publish(Object message, Map<String, String> dynamicProperties) {

        //Load dynamic properties
        String url = dynamicProperties.get(HTTPEventAdapterConstants.ADAPTER_MESSAGE_URL);
        String authType = eventAdapterConfiguration.getStaticProperties().get(HTTPEventAdapterConstants.ADAPTER_AUTH_TYPE);
        Map<String, String> headers = buildHeaders(message, dynamicProperties);
        String payload = buildBody(message);
        Map<String, String> authProperties = resolveAuthProperties(authType, dynamicProperties);
        try {
            executorService.submit(new HTTPSender(url, payload, headers, httpClient, authType, authProperties));
        } catch (RejectedExecutionException e) {
            EventAdapterUtil.logAndDrop(eventAdapterConfiguration.getName(), message,
                    "Job queue is full", e, log, tenantId);
        }
    }

    @Override
    public void disconnect() {

        //not required
    }

    @Override
    public void connectSync() throws ConnectionUnavailableException, OutputEventAdapterException {

        if (syncHttpClientManager != null) {
            return;
        }
        
        try {
            APIClientConfig.Builder configBuilder = new APIClientConfig.Builder()
                    .httpConnectionTimeoutInMillis(getGlobalIntProperty(
                            HTTPEventAdapterConstants.SYNC_CONNECTION_TIMEOUT,
                            HTTPEventAdapterConstants.DEFAULT_SYNC_CONNECTION_TIMEOUT_MS))
                    .httpReadTimeoutInMillis(getGlobalIntProperty(
                            HTTPEventAdapterConstants.SYNC_READ_TIMEOUT,
                            HTTPEventAdapterConstants.DEFAULT_SYNC_READ_TIMEOUT_MS))
                    .httpConnectionRequestTimeoutInMillis(getGlobalIntProperty(
                            HTTPEventAdapterConstants.SYNC_CONNECTION_REQUEST_TIMEOUT,
                            HTTPEventAdapterConstants.DEFAULT_SYNC_CONNECTION_REQUEST_TIMEOUT_MS))
                    .poolSizeToBeSet(getGlobalIntProperty(
                            HTTPEventAdapterConstants.SYNC_POOL_SIZE,
                            HTTPEventAdapterConstants.DEFAULT_SYNC_POOL_SIZE))
                    .defaultMaxPerRoute(getGlobalIntProperty(
                            HTTPEventAdapterConstants.SYNC_MAX_CONNECTIONS_PER_ROUTE,
                            HTTPEventAdapterConstants.DEFAULT_SYNC_MAX_CONNECTIONS_PER_ROUTE))
                    .responseLimitInBytes(getGlobalLongProperty(
                            HTTPEventAdapterConstants.SYNC_RESPONSE_LIMIT_BYTES,
                            HTTPEventAdapterConstants.DEFAULT_SYNC_RESPONSE_LIMIT_BYTES));
            if (StringUtils.isNotBlank(proxyHost) && StringUtils.isNotBlank(proxyPort)) {
                try {
                    configBuilder.proxyHost(proxyHost).proxyPort(Integer.parseInt(proxyPort));
                } catch (NumberFormatException e) {
                    log.error(HTTPEventAdapterConstants.ErrorMessage.SYNC_INVALID_PROXY_PORT
                            .formatMessage(proxyPort), e);
                }
            }
            int syncRetryCount = getGlobalIntProperty(
                    HTTPEventAdapterConstants.SYNC_RETRY_COUNT,
                    HTTPEventAdapterConstants.DEFAULT_SYNC_RETRY_COUNT);
            syncHttpClientManager = new SyncHttpClientManager(configBuilder.build(), syncRetryCount);
            initContentType();
        } catch (APIClientException e) {
            throw new OutputEventAdapterException(
                    HTTPEventAdapterConstants.ErrorMessage.SYNC_CLIENT_INIT_FAILED.getCode(),
                    HTTPEventAdapterConstants.ErrorMessage.SYNC_CLIENT_INIT_FAILED
                            .formatMessage(eventAdapterConfiguration.getName()), e);
        }
    }

    @Override
    public void disconnectSync() {

        if (syncHttpClientManager == null) {
            return;
        }
        try {
            syncHttpClientManager.close();
        } catch (IOException exception) {
            log.warn("Error occurred while closing the HTTP sync client for adapter: "
                    + eventAdapterConfiguration.getName(), exception);
        } finally {
            syncHttpClientManager = null;
        }
    }

    @Override
    public void publishSync(Object message, Map<String, String> dynamicProperties)
            throws OutputEventAdapterException {

        if (syncHttpClientManager == null) {
            throw new OutputEventAdapterException(
                    HTTPEventAdapterConstants.ErrorMessage.SYNC_CLIENT_NOT_INITIALIZED.getCode(),
                    HTTPEventAdapterConstants.ErrorMessage.SYNC_CLIENT_NOT_INITIALIZED
                            .formatMessage(eventAdapterConfiguration.getName()));
        }
        String url = dynamicProperties.get(HTTPEventAdapterConstants.ADAPTER_MESSAGE_URL);
        String authType = eventAdapterConfiguration.getStaticProperties()
                .get(HTTPEventAdapterConstants.ADAPTER_AUTH_TYPE);
        String payload = buildBody(message);

        Map<String, String> rawHeaders = buildHeaders(message, dynamicProperties);
        Map<String, String> headers = rawHeaders != null ? rawHeaders : new HashMap<>();
        Map<String, String> authProperties = resolveAuthProperties(authType, dynamicProperties);
        Optional<AuthHeader> authHeader = buildAuthHeader(authType, authProperties);
        if (authHeader.isPresent()) {
            headers.put(authHeader.get().name(), authHeader.get().value());
        }
        if (!clientMethod.equalsIgnoreCase(HTTPEventAdapterConstants.CONSTANT_HTTP_GET)) {
            headers.put("Content-Type", contentType);
        }

        APIRequestContext.HttpMethod httpMethod;
        if (clientMethod.equalsIgnoreCase(HTTPEventAdapterConstants.CONSTANT_HTTP_GET)) {
            httpMethod = APIRequestContext.HttpMethod.GET;
        } else if (clientMethod.equalsIgnoreCase(HTTPEventAdapterConstants.CONSTANT_HTTP_PUT)) {
            httpMethod = APIRequestContext.HttpMethod.PUT;
        } else {
            httpMethod = APIRequestContext.HttpMethod.POST;
        }

        try {
            UUID uuid = UUID.randomUUID();
            APIResponse response = syncHttpClientManager.send(url, httpMethod, headers, payload);
            int responseCode = response.getStatusCode();
            if (responseCode / 100 == 2) {
                if (log.isDebugEnabled()) {
                    log.debug("[Id: " + uuid + "] Successfully published event to the endpoint: " + url +
                            ". Received HTTP response code is: " + responseCode +
                            ". Response body: " + response.getResponseBody());
                }
                logEventPublishing(
                        "Received success response from external endpoint: " + url +
                                " for HTTP-based sync publishing.",
                        DiagnosticLog.ResultStatus.SUCCESS);
            } else if ((responseCode == 401 || responseCode == 403) &&
                    (StringUtils.equalsIgnoreCase(CLIENT_CREDENTIAL, authType) ||
                            StringUtils.equalsIgnoreCase(PASSWORD_CREDENTIAL, authType))) {
                if (log.isDebugEnabled()) {
                    log.debug("[Id: " + uuid + "] Received an unauthorized response from the endpoint: " + url +
                            ". Response code: " + responseCode +
                            ". Response body: " + response.getResponseBody() +
                            ". Hence refreshing the access token and retrying.");
                }
                logEventPublishing(
                        "Received unauthorized response (HTTP " + responseCode + ") from external endpoint: " + url +
                                ". Refreshing access token and retrying.",
                        DiagnosticLog.ResultStatus.FAILED);
                EventAdapterUtil.TokenResponse tokenResponse = fetchNewAccessToken(authType);
                String newToken = tokenResponse.getAccessToken();
                Map<String, String> retryHeaders = new HashMap<>(headers);
                retryHeaders.put("Authorization", "Bearer " + newToken);
                APIResponse retryResponse = syncHttpClientManager.send(url, httpMethod, retryHeaders, payload);
                int retryCode = retryResponse.getStatusCode();
                if (retryCode / 100 != 2) {
                    Map<String, Object> retryParams = new HashMap<>();
                    retryParams.put(RESPONSE_CODE, retryCode);
                    retryParams.put(ERROR_RESPONSE, retryResponse.getResponseBody());
                    log.error("[Id: " + uuid + "] Error while publishing event to the endpoint: " + url +
                            " after token refresh. Received HTTP response code is: " + retryCode +
                            ". Response body: " + retryResponse.getResponseBody());
                    logEventPublishing(
                            "Received error response from external endpoint: " + url +
                                    " after token refresh for HTTP-based sync publishing.",
                            DiagnosticLog.ResultStatus.FAILED, retryParams);
                    throw resolveHttpError(retryCode, url, retryResponse.getResponseBody());
                }
                this.internalAccessToken = newToken;
                storeTokenResponse(authType, tokenResponse);
                if (log.isDebugEnabled()) {
                    log.debug("[Id: " + uuid + "] Successfully published event to the endpoint: " + url +
                            " after token refresh. Received HTTP response code is: " + retryCode +
                            ". Response body: " + retryResponse.getResponseBody());
                }
                logEventPublishing(
                        "Received success response from external endpoint: " + url +
                                " after token refresh for HTTP-based sync publishing.",
                        DiagnosticLog.ResultStatus.SUCCESS);
            } else {
                Map<String, Object> params = new HashMap<>();
                params.put(RESPONSE_CODE, responseCode);
                params.put(ERROR_RESPONSE, response.getResponseBody());
                log.error("[Id: " + uuid + "] Error while publishing event to the endpoint: " + url +
                        ". Received HTTP response code is: " + responseCode +
                        ". Response body: " + response.getResponseBody());
                logEventPublishing(
                        "Received error response from external endpoint: " + url +
                                " for HTTP-based sync publishing.",
                        DiagnosticLog.ResultStatus.FAILED, params);
                throw resolveHttpError(responseCode, url, response.getResponseBody());
            }
        } catch (APIClientException e) {
            throw new OutputEventAdapterException(
                    HTTPEventAdapterConstants.ErrorMessage.SYNC_PUBLISH_FAILED_IO.getCode(),
                    HTTPEventAdapterConstants.ErrorMessage.SYNC_PUBLISH_FAILED_IO.formatMessage(url), e);
        }
    }

    @Override
    public boolean isSync(Map<String, String> dynamicProperties) {

        return Boolean.parseBoolean(
                dynamicProperties.get(HTTPEventAdapterConstants.ADAPTER_MESSAGE_HTTP_SYNC));
    }

    @Override
    public void destroy() {

        //not required
    }

    @Override
    public boolean isPolled() {
        
        return false;
    }

    private Map<String, String> resolveAuthProperties(String authType, Map<String, String> dynamicProperties) {
        Map<String, String> authProperties = new HashMap<>();
        if (StringUtils.isBlank(authType)) {
            char[] username;
            char[] password;
            try {
                username = decryptCredential(provider, BASIC, USERNAME);
                password = decryptCredential(provider, BASIC, PASSWORD);
            } catch (SecretManagementException e) {
                username = dynamicProperties.get(ADAPTER_USERNAME) != null ?
                        dynamicProperties.get(ADAPTER_USERNAME).toCharArray() : new char[0];
                password = dynamicProperties.get(ADAPTER_PASSWORD) != null ?
                        dynamicProperties.get(ADAPTER_PASSWORD).toCharArray() : new char[0];
            }
            authProperties.put(USERNAME, new String(username));
            authProperties.put(PASSWORD, new String(password));
            return authProperties;
        }
        switch (authType.toUpperCase()) {
            case CLIENT_CREDENTIAL:
                if (this.internalAccessToken == null) {
                    try {
                        if (log.isDebugEnabled()) {
                            log.debug("Retrieving the internal access token for client credential grant " +
                                    "type authentication from the secret manager.");
                        }
                        this.internalAccessToken = new String(decryptCredential(provider, CLIENT_CREDENTIAL,
                                INTERNAL_ACCESS_TOKEN));
                    } catch (SecretManagementException e) {
                        // Ignore the exception and generate a new access token as the internal access token is not
                        // available in the secret store manager.
                    }

                    if (StringUtils.isBlank(internalAccessToken)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Internal access token for client credential grant type authentication is " +
                                    "not available in the secret manager.");
                        }
                        // Either clientId and clientSecret are both encrypted or both are in plain text.
                        // Hence, failing to decrypt clientId or clientSecret means they are in plain text.
                        char[] clientId;
                        char[] clientSecret;
                        try {
                            clientId = decryptCredential(provider, CLIENT_CREDENTIAL, CLIENT_ID);
                            clientSecret = decryptCredential(provider, CLIENT_CREDENTIAL, CLIENT_SECRET);
                        } catch (SecretManagementException e) {
                            if (StringUtils.isBlank(eventAdapterConfiguration.getStaticProperties().get(ADAPTER_CLIENT_ID))
                                    || StringUtils.isBlank(eventAdapterConfiguration.getStaticProperties().get(ADAPTER_CLIENT_SECRET))) {
                                    throw new ConnectionUnavailableException("The adapter " + eventAdapterConfiguration.getName() +
                                        " failed to connect to the server due to missing client credentials");
                            }
                            clientId = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_CLIENT_ID).toCharArray();
                            clientSecret = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_CLIENT_SECRET).toCharArray();
                        }
                        String tokenEndpoint = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_TOKEN_ENDPOINT);
                        String scopes = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_SCOPES);

                        if (log.isDebugEnabled()) {
                            log.debug("Access token is not available. Generating a new access token for " +
                                    "HTTP-Based Event Publishing");
                        }
                        EventAdapterUtil.TokenResponse tokenResponse;
                        try {
                            tokenResponse = fetchOrRefreshToken(CLIENT_CREDENTIAL, clientId, clientSecret, null, null,
                                    tokenEndpoint, scopes);
                            this.internalAccessToken = tokenResponse.getAccessToken();
                            logEventPublishing(
                                    "Access token is successfully retrieved using client " +
                                            "credentials grant type for HTTP-based email publishing.",
                                    DiagnosticLog.ResultStatus.SUCCESS);
                        } catch (OutputEventAdapterRuntimeException e) {
                            logEventPublishingFailure(
                                    "Received failure response while retrieving access token " +
                                            "using client credentials grant for HTTP-based email publishing.", e);
                            throw e;
                        }
                        storeTokenResponse(CLIENT_CREDENTIAL, tokenResponse);
                    }
                }
                authProperties.put(INTERNAL_ACCESS_TOKEN, this.internalAccessToken);
                break;
            case PASSWORD_CREDENTIAL:
                if (this.internalAccessToken == null) {
                    try {
                        if (log.isDebugEnabled()) {
                            log.debug("Retrieving the internal access token for password credential grant " +
                                    "type authentication from the secret manager.");
                        }
                        this.internalAccessToken = new String(decryptCredential(provider, PASSWORD_CREDENTIAL,
                                INTERNAL_ACCESS_TOKEN));
                    } catch (SecretManagementException e) {
                        // Ignore the exception and generate a new access token as the internal access token is not
                        // available in the secret store manager.
                    }

                    if (StringUtils.isBlank(internalAccessToken)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Internal access token for password credential grant type authentication " +
                                    "is not available in the secret manager.");
                        }
                        char[] pwCredClientId;
                        char[] pwCredClientSecret;
                        char[] pwCredUsername;
                        char[] pwCredPassword;
                        try {
                            pwCredClientId = decryptCredential(provider, PASSWORD_CREDENTIAL, CLIENT_ID);
                            pwCredClientSecret = decryptCredential(provider, PASSWORD_CREDENTIAL, CLIENT_SECRET);
                            pwCredUsername = decryptCredential(provider, PASSWORD_CREDENTIAL, USERNAME);
                            pwCredPassword = decryptCredential(provider, PASSWORD_CREDENTIAL, PASSWORD);
                        } catch (SecretManagementException e) {
                            if (StringUtils.isBlank(eventAdapterConfiguration.getStaticProperties().get(ADAPTER_CLIENT_ID))
                                    || StringUtils.isBlank(eventAdapterConfiguration.getStaticProperties().get(ADAPTER_CLIENT_SECRET))
                                    || StringUtils.isBlank(eventAdapterConfiguration.getStaticProperties().get(ADAPTER_USERNAME))
                                    || StringUtils.isBlank(eventAdapterConfiguration.getStaticProperties().get(ADAPTER_PASSWORD))) {
                                    throw new ConnectionUnavailableException("The adapter " + eventAdapterConfiguration.getName() +
                                        " failed to connect to the server due to missing password credential grant credentials");
                            }
                            pwCredClientId = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_CLIENT_ID).toCharArray();
                            pwCredClientSecret = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_CLIENT_SECRET).toCharArray();
                            pwCredUsername = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_USERNAME).toCharArray();
                            pwCredPassword = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_PASSWORD).toCharArray();
                        }
                        String tokenEndpoint = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_TOKEN_ENDPOINT);
                        String scopes = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_SCOPES);

                        if (log.isDebugEnabled()) {
                            log.debug("Access token is not available. Generating a new access token for " +
                                    "HTTP-Based Event Publishing using the password credential grant type.");
                        }
                        EventAdapterUtil.TokenResponse tokenResponse;
                        try {
                            tokenResponse = fetchOrRefreshToken(PASSWORD_CREDENTIAL, pwCredClientId, pwCredClientSecret,
                                    pwCredUsername, pwCredPassword, tokenEndpoint, scopes);
                            this.internalAccessToken = tokenResponse.getAccessToken();
                            logEventPublishing(
                                    "Access token is successfully retrieved using password " +
                                            "credentials grant type for HTTP-based email publishing.",
                                    DiagnosticLog.ResultStatus.SUCCESS);
                        } catch (OutputEventAdapterRuntimeException e) {
                            logEventPublishingFailure(
                                    "Received failure response while retrieving access token " +
                                            "using password credentials grant for HTTP-based email publishing.", e);
                            throw e;
                        }
                        storeTokenResponse(PASSWORD_CREDENTIAL, tokenResponse);
                    }
                }
                authProperties.put(INTERNAL_ACCESS_TOKEN, this.internalAccessToken);
                break;
            case BEARER:
                char[] accessToken;
                try {
                    accessToken = decryptCredential(provider, BEARER, ACCESS_TOKEN);
                } catch (SecretManagementException e) {
                    if (StringUtils.isBlank(eventAdapterConfiguration.getStaticProperties().get(ADAPTER_ACCESS_TOKEN))) {
                            throw new ConnectionUnavailableException("The adapter " + eventAdapterConfiguration.getName() +
                                " failed to connect to the server due to missing access token");
                    }
                    accessToken = eventAdapterConfiguration.getStaticProperties()
                            .get(ADAPTER_ACCESS_TOKEN).toCharArray();
                }
                authProperties.put(ACCESS_TOKEN, new String(accessToken));
                break;
            case API_KEY:
                char[] apiKeyValue;
                try {
                    apiKeyValue = decryptCredential(provider, API_KEY, API_KEY_VALUE);
                } catch (SecretManagementException e) {
                    if (StringUtils.isBlank(eventAdapterConfiguration.getStaticProperties().get(ADAPTER_API_KEY_VALUE))) {
                            throw new ConnectionUnavailableException("The adapter " + eventAdapterConfiguration.getName() +
                                " failed to connect to the server due to missing API key value");
                    }
                    apiKeyValue = eventAdapterConfiguration.getStaticProperties()
                            .get(ADAPTER_API_KEY_VALUE).toCharArray();
                }
                String apiKeyHeader = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_API_KEY_HEADER);
                authProperties.put(API_KEY_HEADER, apiKeyHeader);
                authProperties.put(API_KEY_VALUE, new String(apiKeyValue));
                break;
            case BASIC:
                char[] username;
                char[] password;
                try {
                    username = decryptCredential(provider, BASIC, USERNAME);
                    password = decryptCredential(provider, BASIC, PASSWORD);
                } catch (SecretManagementException e) {
                    if (StringUtils.isBlank(dynamicProperties.get(ADAPTER_USERNAME))
                            || StringUtils.isBlank(dynamicProperties.get(ADAPTER_PASSWORD))) {
                            throw new ConnectionUnavailableException("The adapter " + eventAdapterConfiguration.getName() +
                                " failed to connect to the server due to missing credentials");
                    }
                    username = dynamicProperties.get(ADAPTER_USERNAME).toCharArray();
                    password = dynamicProperties.get(ADAPTER_PASSWORD).toCharArray();
                }
                authProperties.put(USERNAME, new String(username));
                authProperties.put(PASSWORD, new String(password));
                break;
            case NONE:
                break;
        }
        return authProperties;
    }

    private static final class AuthHeader {

        private final String name;
        private final String value;

        public AuthHeader(String name, String value) {

            this.name = name;
            this.value = value;
        }

        public String name() {

            return name;
        }

        public String value() {

            return value;
        }
    }

    private static Optional<AuthHeader> buildAuthHeader(String authType, Map<String, String> authProperties) {
        if (StringUtils.isBlank(authType)) {
            String user = authProperties != null ? authProperties.get(USERNAME) : null;
            String pass = authProperties != null ? authProperties.get(PASSWORD) : null;
            if (StringUtils.isNotBlank(user)) {
                return Optional.of(new AuthHeader("Authorization", "Basic " + Base64.encode(
                        (user + HTTPEventAdapterConstants.ENTRY_SEPARATOR +
                         (pass != null ? pass : "")).getBytes())));
            }
            return Optional.empty();
        }
        switch (authType.toUpperCase()) {
            case CLIENT_CREDENTIAL:
            case PASSWORD_CREDENTIAL:
                return Optional.of(new AuthHeader("Authorization",
                        "Bearer " + authProperties.get(INTERNAL_ACCESS_TOKEN)));
            case BEARER:
                return Optional.of(new AuthHeader("Authorization",
                        "Bearer " + authProperties.get(ACCESS_TOKEN)));
            case API_KEY:
                return Optional.of(new AuthHeader(
                        authProperties.get(API_KEY_HEADER), authProperties.get(API_KEY_VALUE)));
            case BASIC:
                String username = authProperties.get(USERNAME);
                String password = authProperties.get(PASSWORD);
                if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
                    return Optional.of(new AuthHeader("Authorization", "Basic " + Base64.encode(
                            (username + HTTPEventAdapterConstants.ENTRY_SEPARATOR + password).getBytes())));
                }
                return Optional.empty();
            default:
                return Optional.empty();
        }
    }

    private int getGlobalIntProperty(String name, int defaultValue) {

        String value = globalProperties.get(name);
        if (value != null) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                log.warn("Invalid value for global property '" + name + "': '" + value +
                        "'. Using default: " + defaultValue);
            }
        }
        return defaultValue;
    }

    private long getGlobalLongProperty(String name, long defaultValue) {
        
        String value = globalProperties.get(name);
        if (value != null) {
            try {
                return Long.parseLong(value.trim());
            } catch (NumberFormatException e) {
                log.warn("Invalid value for global property '" + name + "': '" + value +
                        "'. Using default: " + defaultValue);
            }
        }
        return defaultValue;
    }

    private static OutputEventAdapterException resolveHttpError(int statusCode, String url, String responseBody) {

        HTTPEventAdapterConstants.ErrorMessage error;
        String message;
        switch (statusCode) {
            case 400:
                error = HTTPEventAdapterConstants.ErrorMessage.SYNC_PUBLISH_BAD_REQUEST;
                message = error.formatMessage(url, responseBody);
                break;
            case 401:
                error = HTTPEventAdapterConstants.ErrorMessage.SYNC_PUBLISH_UNAUTHORIZED;
                message = error.formatMessage(url);
                break;
            case 403:
                error = HTTPEventAdapterConstants.ErrorMessage.SYNC_PUBLISH_FORBIDDEN;
                message = error.formatMessage(url);
                break;
            case 429:
                error = HTTPEventAdapterConstants.ErrorMessage.SYNC_PUBLISH_TOO_MANY_REQUESTS;
                message = error.formatMessage(url);
                break;
            case 500:
                error = HTTPEventAdapterConstants.ErrorMessage.SYNC_PUBLISH_SERVER_ERROR;
                message = error.formatMessage(url, responseBody);
                break;
            case 502:
            case 503:
            case 504:
                error = HTTPEventAdapterConstants.ErrorMessage.SYNC_PUBLISH_SERVICE_UNAVAILABLE;
                message = error.formatMessage(url, statusCode);
                break;
            default:
                error = HTTPEventAdapterConstants.ErrorMessage.SYNC_PUBLISH_FAILED_WITH_RESPONSE;
                message = error.formatMessage(url, statusCode, responseBody);
        }
        return new OutputEventAdapterException(error.getCode(), message);
    }

    /**
     * Attempts to obtain a fresh access token using a previously stored refresh token, falling back to a full
     * grant request (client credentials or password credentials) if no refresh token is stored, or if the
     * refresh attempt itself fails (e.g. the refresh token has expired or been revoked).
     * <p>
     * Per RFC 6749 &sect;4.4.3, an authorization server SHOULD NOT issue a refresh token for the client
     * credentials grant, since the client can simply re-authenticate with its client secret at any time -
     * there is no resource-owner credential whose repeated transmission needs to be avoided. Refresh-token
     * renewal is therefore only attempted for PASSWORD_CREDENTIAL.
     *
     * @param authType     CLIENT_CREDENTIAL or PASSWORD_CREDENTIAL.
     * @param clientId     Decrypted client ID.
     * @param clientSecret Decrypted client secret.
     * @param username     Decrypted resource owner username. Only used when authType is PASSWORD_CREDENTIAL.
     * @param password     Decrypted resource owner password. Only used when authType is PASSWORD_CREDENTIAL.
     * @param tokenEndpoint Token endpoint URL.
     * @param scopes       Scopes to be requested.
     * @return The resulting token response.
     */
    private EventAdapterUtil.TokenResponse fetchOrRefreshToken(String authType, char[] clientId, char[] clientSecret,
            char[] username, char[] password, String tokenEndpoint, String scopes) {

        boolean isPasswordCredential = StringUtils.equalsIgnoreCase(PASSWORD_CREDENTIAL, authType);
        if (isPasswordCredential) {
            try {
                char[] storedRefreshToken = decryptCredential(provider, authType.toUpperCase(),
                        INTERNAL_REFRESH_TOKEN);
                if (storedRefreshToken != null && storedRefreshToken.length > 0) {
                    try {
                        if (log.isDebugEnabled()) {
                            log.debug("Attempting to obtain a new access token using the stored refresh token for " +
                                    "auth type: " + authType);
                        }
                        return getTokenResponseUsingRefreshToken(new String(clientId), new String(clientSecret),
                                new String(storedRefreshToken), tokenEndpoint, scopes);
                    } catch (OutputEventAdapterRuntimeException e) {
                        if (log.isDebugEnabled()) {
                            log.debug("Refresh token grant failed for auth type: " + authType +
                                    ". Falling back to a full grant request.", e);
                        }
                    }
                }
            } catch (SecretManagementException e) {
                // No refresh token available in the secret store yet. Fall back to a full grant request.
            }
        }

        return isPasswordCredential
                ? getTokenResponsePasswordGrant(new String(clientId), new String(clientSecret), new String(username),
                        new String(password), tokenEndpoint, scopes)
                : getTokenResponse(new String(clientId), new String(clientSecret), tokenEndpoint, scopes);
    }

    /**
     * Persists the access token in the secret manager, along with the refresh token if one was issued or
     * rotated. Refresh tokens are only meaningful (and only ever expected) for PASSWORD_CREDENTIAL; see
     * {@link #fetchOrRefreshToken}.
     */
    private void storeTokenResponse(String authType, EventAdapterUtil.TokenResponse tokenResponse) {

        try {
            encryptAndStoreCredential(provider, authType.toUpperCase(), INTERNAL_ACCESS_TOKEN,
                    tokenResponse.getAccessToken());
            if (StringUtils.equalsIgnoreCase(PASSWORD_CREDENTIAL, authType)
                    && StringUtils.isNotBlank(tokenResponse.getRefreshToken())) {
                encryptAndStoreCredential(provider, authType.toUpperCase(), INTERNAL_REFRESH_TOKEN,
                        tokenResponse.getRefreshToken());
            }
        } catch (SecretManagementException e) {
            log.warn("Unable to store the newly generated token(s) in the secret manager.");
        }
    }

    private EventAdapterUtil.TokenResponse fetchNewAccessToken(String authType) throws OutputEventAdapterException {

        boolean isPasswordCredential = StringUtils.equalsIgnoreCase(PASSWORD_CREDENTIAL, authType);

        char[] clientId;
        char[] clientSecret;
        char[] username = null;
        char[] password = null;
        try {
            clientId = decryptCredential(provider, authType.toUpperCase(), CLIENT_ID);
            clientSecret = decryptCredential(provider, authType.toUpperCase(), CLIENT_SECRET);
            if (isPasswordCredential) {
                username = decryptCredential(provider, authType.toUpperCase(), USERNAME);
                password = decryptCredential(provider, authType.toUpperCase(), PASSWORD);
            }
        } catch (SecretManagementException e) {
            boolean missingBaseCreds =
                    StringUtils.isBlank(eventAdapterConfiguration.getStaticProperties().get(ADAPTER_CLIENT_ID))
                    || StringUtils.isBlank(eventAdapterConfiguration.getStaticProperties().get(ADAPTER_CLIENT_SECRET));
            boolean missingPasswordCreds = isPasswordCredential &&
                    (StringUtils.isBlank(eventAdapterConfiguration.getStaticProperties().get(ADAPTER_USERNAME))
                    || StringUtils.isBlank(eventAdapterConfiguration.getStaticProperties().get(ADAPTER_PASSWORD)));
            if (missingBaseCreds || missingPasswordCreds) {
                throw new OutputEventAdapterException(
                        HTTPEventAdapterConstants.ErrorMessage.SYNC_TOKEN_REFRESH_MISSING_CREDS.getCode(),
                        HTTPEventAdapterConstants.ErrorMessage.SYNC_TOKEN_REFRESH_MISSING_CREDS
                                .formatMessage(eventAdapterConfiguration.getName()));
            }
            clientId = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_CLIENT_ID).toCharArray();
            clientSecret = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_CLIENT_SECRET).toCharArray();
            if (isPasswordCredential) {
                username = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_USERNAME).toCharArray();
                password = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_PASSWORD).toCharArray();
            }
        }
        String tokenEndpoint = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_TOKEN_ENDPOINT);
        String scopes = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_SCOPES);
        try {
            EventAdapterUtil.TokenResponse tokenResponse = fetchOrRefreshToken(authType, clientId, clientSecret,
                    username, password, tokenEndpoint, scopes);
            logEventPublishing(
                    "Access token is successfully retrieved using " + (isPasswordCredential ?
                            "password credentials" : "client credentials") + " grant type " +
                            "for HTTP-based sync publishing.",
                    DiagnosticLog.ResultStatus.SUCCESS);
            return tokenResponse;
        } catch (OutputEventAdapterRuntimeException e) {
            logEventPublishingFailure("Failed to obtain a new access token for HTTP-based sync publishing.", e);
            throw new OutputEventAdapterException(
                    HTTPEventAdapterConstants.ErrorMessage.SYNC_TOKEN_FETCH_FAILED.getCode(),
                    HTTPEventAdapterConstants.ErrorMessage.SYNC_TOKEN_FETCH_FAILED
                            .formatMessage(eventAdapterConfiguration.getName()), e);
        }
    }

    private void checkHTTPClientInit(Map<String, String> staticProperties) {

        if (this.httpClient != null) {
            return;
        }

        synchronized (HTTPEventAdapter.class) {
            if (this.httpClient != null) {
                return;
            }

            httpClient = new HttpClient(connectionManager);
            String proxyHost = staticProperties.get(HTTPEventAdapterConstants.ADAPTER_PROXY_HOST);
            String proxyPort = staticProperties.get(HTTPEventAdapterConstants.ADAPTER_PROXY_PORT);
            if (proxyHost != null && proxyHost.trim().length() > 0) {
                try {
                    HttpHost host = new HttpHost(proxyHost, Integer.parseInt(proxyPort));
                    this.httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, host);
                } catch (NumberFormatException e) {
                    log.error("Invalid proxy port: " + proxyPort + ", "
                            + "ignoring proxy settings for HTTP output event adaptor...");
                }
            }

            initContentType();
        }

    }

    private void initContentType() {

        String messageFormat = eventAdapterConfiguration.getMessageFormat();
        if (messageFormat.equalsIgnoreCase(MessageType.JSON)) {
            contentType = "application/json";
        } else if (messageFormat.equalsIgnoreCase(MessageType.TEXT)) {
            contentType = "text/plain";
        } else if (messageFormat.equalsIgnoreCase(MessageType.FORM)) {
            contentType = "application/x-www-form-urlencoded";
        } else {
            contentType = "text/xml";
        }
    }

    private Map<String, String> extractHeaders(String headers) {
        if (headers == null || headers.trim().length() == 0) {
            return null;
        }

        String[] entries = headers.split(HTTPEventAdapterConstants.HEADER_SEPARATOR);
        String[] keyValue;
        Map<String, String> result = new HashMap<String, String>();
        for (String header : entries) {
            try {
                keyValue = header.split(HTTPEventAdapterConstants.ENTRY_SEPARATOR, 2);
                result.put(keyValue[0].trim(), keyValue[1].trim());
            } catch (Exception e) {
                log.warn("Header property '" + header + "' is not defined in the correct format.", e);
            }
        }
        return result;
    }

    private boolean isDiagnosticLogEnabledForEmailPublishing() {

        return CarbonUtils.isDiagnosticLogsEnabled() &&
                StringUtils.equalsIgnoreCase(eventAdapterConfiguration.getName(),
                        EMAIL_PUBLISHER_EVENT_ADAPTER_NAME);
    }

    private void logEventPublishing(String message, DiagnosticLog.ResultStatus status) {

        logEventPublishing(message, status, null);
    }

    private void logEventPublishing(String message, DiagnosticLog.ResultStatus status,
                                    Map<String, Object> inputParams) {

        if (!isDiagnosticLogEnabledForEmailPublishing()) {
            // Diagnostic logs are enabled only for http-based email publishing within the http-based output adapter.
            return;
        }
        DiagnosticLog.DiagnosticLogBuilder builder = new DiagnosticLog.DiagnosticLogBuilder(
                HTTP_EVENT_ADAPTER_SERVICE, SEND_EMAIL);
        builder.resultMessage(message)
                .resultStatus(status)
                .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION);
        if (inputParams != null) {
            for (Map.Entry<String, Object> entry : inputParams.entrySet()) {
                builder.inputParam(entry.getKey(), entry.getValue());
            }
        }
        EventAdapterUtil.triggerDiagnosticLogEvent(builder);
    }

    private void logEventPublishingFailure(String message, Throwable e) {

        Map<String, Object> params = new HashMap<>();
        params.put(ERROR_RESPONSE, e.getMessage());
        logEventPublishing(message, DiagnosticLog.ResultStatus.FAILED, params);
    }

    /**
     * This class represents a job to send an HTTP request to a target URL.
     */
    class HTTPSender implements Runnable {

        private String url;

        private String payload;

        private String username;

        private String password;

        private Map<String, String> headers;

        private HttpClient httpClient;
        private String authType;
        private Map<String, String> authProperties;

        public HTTPSender(String url, String payload, String username, String password, Map<String, String> headers,
                HttpClient httpClient) {
            this.url = url;
            this.payload = payload;
            this.username = username;
            this.password = password;
            this.headers = headers;
            this.httpClient = httpClient;
        }

        public HTTPSender(String url, String payload, Map<String, String> headers, HttpClient httpClient,
                          String authType, Map<String, String> authProperties) {
            this.url = url;
            this.payload = payload;
            this.headers = headers;
            this.httpClient = httpClient;
            this.authType = authType;
            this.authProperties = authProperties;
            this.username = authProperties.get(USERNAME);
            this.password = authProperties.get(PASSWORD);
        }

        public String getUrl() {
            return url;
        }

        public String getPayload() {
            return payload;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public HttpClient getHttpClient() {
            return httpClient;
        }

        public String getAuthType() {

            return authType;
        }

        public Map<String, String> getAuthProperties() {

            return authProperties;
        }

        public void run() {

            UUID uuid = UUID.randomUUID();
            HttpMethodBase method = null;

            try {

                if (clientMethod.equalsIgnoreCase(HTTPEventAdapterConstants.CONSTANT_HTTP_PUT)) {
                    method = new PutMethod(this.getUrl());
                } else if (clientMethod.equalsIgnoreCase(HTTPEventAdapterConstants.CONSTANT_HTTP_GET)) {
                    method = new GetMethod(this.getUrl());
                } else {
                    method = new PostMethod(this.getUrl());
                }

                if (hostConfiguration == null) {
                    URL hostUrl = new URL(this.getUrl());
                    hostConfiguration = new HostConfiguration();
                    hostConfiguration.setHost(hostUrl.getHost(), hostUrl.getPort(), hostUrl.getProtocol());
                    if (StringUtils.isNotBlank(proxyHost) && StringUtils.isNotBlank(proxyPort)) {
                        hostConfiguration.setProxy(proxyHost, Integer.parseInt(proxyPort));
                    }
                }

                if (method instanceof EntityEnclosingMethod) {
                    ((EntityEnclosingMethod) method).setRequestEntity(new StringRequestEntity(this.getPayload(), contentType, "UTF-8"));
                }

                if (this.getHeaders() != null) {
                    for (Map.Entry<String, String> header : this.getHeaders().entrySet()) {
                        method.setRequestHeader(header.getKey(), header.getValue());
                    }
                }

                Optional<AuthHeader> authHeader = buildAuthHeader(this.getAuthType(), this.getAuthProperties());
                if (authHeader.isPresent()) {
                    method.setRequestHeader(authHeader.get().name(), authHeader.get().value());
                }

                int responseCode = this.getHttpClient().executeMethod(hostConfiguration, method);
                if (responseCode / 100 == 2) {
                    if (log.isDebugEnabled()) {
                        log.debug("[Id: " + uuid +  "] " +
                                "Successfully connected to the endpoint: " + this.url +
                                ". Received HTTP response code is: " + responseCode +
                                ". Response body : " + method.getResponseBodyAsString());
                    }
                    logEventPublishing(
                            "Received success response from external endpoint: " + this.url +
                                    " for HTTP-based email publishing.",
                            DiagnosticLog.ResultStatus.SUCCESS);
                } else if ((responseCode == 401 || responseCode == 403) &&
                        (StringUtils.equalsIgnoreCase(CLIENT_CREDENTIAL, this.getAuthType()) ||
                                StringUtils.equalsIgnoreCase(PASSWORD_CREDENTIAL, this.getAuthType()))) {
                    if (log.isDebugEnabled()) {
                        log.debug("[Id: " + uuid + "] " +
                                "Received an unauthorized response from the endpoint: " + this.url +
                                ". Response code: " + responseCode +
                                ". Response body: " + method.getResponseBodyAsString() +
                                ". Hence refreshing the access token and retrying.");
                    }
                    logEventPublishing(
                            "Received unauthorized response from external endpoint: " +
                                    this.url + ". Refreshing access token and retrying.",
                            DiagnosticLog.ResultStatus.SUCCESS);
                    retryWithNewAccessToken(method);
                } else {
                    log.error("[Id: " + uuid + "] Error while connecting to the endpoint: " + this.url +
                            ". Received HTTP response code is: " + responseCode +
                            ". Response body: " + method.getResponseBodyAsString());
                    Map<String, Object> params = new HashMap<>();
                    params.put(RESPONSE_CODE, responseCode);
                    params.put(ERROR_RESPONSE, method.getResponseBodyAsString());
                    logEventPublishing(
                            "Received error response from external endpoint: " + this.url +
                                    " for HTTP-based email publishing.",
                            DiagnosticLog.ResultStatus.FAILED, params);
                }
            } catch (UnknownHostException e) {
                EventAdapterUtil.logAndDrop(eventAdapterConfiguration.getName(), this.getPayload(),
                        "Cannot connect to " + this.getUrl(), e, log, tenantId);
                logEventPublishingFailure("Received failure response from external endpoint: " + this.url +
                        " for HTTP-based email publishing.", e);
            } catch (Throwable e) {
                logEventPublishingFailure("Received failure response from external endpoint: " + this.url +
                        " for HTTP-based email publishing.", e);
                EventAdapterUtil
                        .logAndDrop(eventAdapterConfiguration.getName(), this.getPayload(), null, e, log, tenantId);
            } finally {
                if (method != null) {
                    method.releaseConnection();
                }
            }
        }

        private void retryWithNewAccessToken(HttpMethodBase method) throws IOException {

            if (log.isDebugEnabled()) {
                log.debug("Access token is expired. Generating a new access token and retrying the HTTP-Based" +
                        " Event Publishing.");
            }
            UUID uuid = UUID.randomUUID();
            boolean isPasswordCredential = StringUtils.equalsIgnoreCase(PASSWORD_CREDENTIAL, this.getAuthType());
            String tokenAuthType = isPasswordCredential ? PASSWORD_CREDENTIAL : CLIENT_CREDENTIAL;
            char[] clientId;
            char[] clientSecret;
            char[] retryUsername = null;
            char[] retryPassword = null;
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext
                        .getThreadLocalCarbonContext();
                privilegedCarbonContext.setTenantId(tenantId);
                try {
                    clientId = decryptCredential(provider, tokenAuthType, CLIENT_ID);
                    clientSecret = decryptCredential(provider, tokenAuthType, CLIENT_SECRET);
                    if (isPasswordCredential) {
                        retryUsername = decryptCredential(provider, tokenAuthType, USERNAME);
                        retryPassword = decryptCredential(provider, tokenAuthType, PASSWORD);
                    }
                } catch (SecretManagementException e) {
                    boolean missingBaseCreds =
                            StringUtils.isBlank(eventAdapterConfiguration.getStaticProperties().get(ADAPTER_CLIENT_ID))
                            || StringUtils.isBlank(eventAdapterConfiguration.getStaticProperties().get(ADAPTER_CLIENT_SECRET));
                    boolean missingPasswordCreds = isPasswordCredential &&
                            (StringUtils.isBlank(eventAdapterConfiguration.getStaticProperties().get(ADAPTER_USERNAME))
                            || StringUtils.isBlank(eventAdapterConfiguration.getStaticProperties().get(ADAPTER_PASSWORD)));
                    if (missingBaseCreds || missingPasswordCreds) {
                        throw new ConnectionUnavailableException("The adapter " + eventAdapterConfiguration.getName() +
                                " failed to connect to the server due to missing credentials");
                    }
                    clientId = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_CLIENT_ID).toCharArray();
                    clientSecret = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_CLIENT_SECRET).toCharArray();
                    if (isPasswordCredential) {
                        retryUsername = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_USERNAME).toCharArray();
                        retryPassword = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_PASSWORD).toCharArray();
                    }
                }

                String tokenEndpoint = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_TOKEN_ENDPOINT);
                String scopes = eventAdapterConfiguration.getStaticProperties().get(ADAPTER_SCOPES);

                int attempts = 0;
                while (attempts < MAX_RETRY_ATTEMPTS) {
                    attempts++;
                    EventAdapterUtil.TokenResponse tokenResponse;
                    try {
                        tokenResponse = fetchOrRefreshToken(tokenAuthType, clientId, clientSecret, retryUsername,
                                retryPassword, tokenEndpoint, scopes);
                        internalAccessToken = tokenResponse.getAccessToken();
                        logEventPublishing(
                                "Access token is successfully retrieved using " + (isPasswordCredential ?
                                        "password credentials" : "client credentials") + " grant type " +
                                        "for HTTP-based email publishing.",
                                DiagnosticLog.ResultStatus.SUCCESS);
                    } catch (OutputEventAdapterRuntimeException e) {
                        logEventPublishingFailure(
                                "Received failure response while retrieving access token " +
                                        "using " + (isPasswordCredential ? "password credentials" :
                                        "client credentials") + " grant type for HTTP-based email publishing.", e);
                        throw e;
                    }
                    method.setRequestHeader("Authorization", "Bearer " + internalAccessToken);

                    int responseCode = this.getHttpClient().executeMethod(hostConfiguration, method);
                    if (responseCode / 100 == 2) {
                        if (log.isDebugEnabled()) {
                            log.debug("[Id: " + uuid + "] " +
                                    "Successfully connected to the endpoint: " + this.url +
                                    ". Received HTTP response code is: " + responseCode +
                                    ". Response body : " + method.getResponseBodyAsString());
                        }
                        logEventPublishing(
                                "Received success response from external endpoint",
                                DiagnosticLog.ResultStatus.SUCCESS);
                        storeTokenResponse(tokenAuthType, tokenResponse);
                        return;
                    } else {
                        Map<String, Object> params = new HashMap<>();
                        params.put(RESPONSE_CODE, responseCode);
                        params.put(ERROR_RESPONSE, method.getResponseBodyAsString());
                        if (attempts == MAX_RETRY_ATTEMPTS) {
                            log.error("[Id: " + uuid + "] Error while connecting even after retrying with " +
                                    "new token to the endpoint: " + this.url +
                                    ". Received HTTP response code is: " + responseCode +
                                    ". Response body: " + method.getResponseBodyAsString());
                            logEventPublishing(
                                    "Received error response from external endpoint: " + this.url +
                                            " and maximum retry attempts reached.",
                                    DiagnosticLog.ResultStatus.FAILED, params);
                        } else {
                            log.warn("[Id: " + uuid + "] Error while connecting to the endpoint: " + this.url +
                                    ". Received HTTP response code is: " + responseCode +
                                    ". Retry (attempt " + attempts + ")");
                            logEventPublishing(
                                    "Received error response from external endpoint: " + this.url +
                                            " Retrying again.",
                                    DiagnosticLog.ResultStatus.SUCCESS, params);
                        }
                    }
                }
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

}
