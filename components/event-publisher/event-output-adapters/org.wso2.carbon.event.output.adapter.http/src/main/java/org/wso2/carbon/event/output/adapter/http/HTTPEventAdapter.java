/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.output.adapter.http;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.axiom.om.util.Base64;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.HttpClient;
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
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.output.adapter.http.internal.model.Event;
import org.wso2.carbon.event.output.adapter.http.internal.model.ReceivedEvent;
import org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static org.wso2.carbon.event.output.adapter.core.EventAdapterSecretProcessor.decryptCredential;
import static org.wso2.carbon.event.output.adapter.core.EventAdapterSecretProcessor.encryptAndStoreCredential;
import static org.wso2.carbon.event.output.adapter.core.EventAdapterUtil.getAccessToken;
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
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.MAX_RETRY_ATTEMPTS;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.NONE;
import static org.wso2.carbon.event.output.adapter.http.internal.util.HTTPEventAdapterConstants.PASSWORD;
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

    @Override
    public void publish(Object message, Map<String, String> dynamicProperties) {
        //Load dynamic properties
        String url = dynamicProperties.get(HTTPEventAdapterConstants.ADAPTER_MESSAGE_URL);
        String authType = eventAdapterConfiguration.getStaticProperties().get(HTTPEventAdapterConstants.ADAPTER_AUTH_TYPE);
        Map<String, String> headers = this
                .extractHeaders(dynamicProperties.get(HTTPEventAdapterConstants.ADAPTER_HEADERS));
        boolean eventPayloadOnly = Boolean.parseBoolean(eventAdapterConfiguration.getStaticProperties()
                .get(HTTPEventAdapterConstants.ADAPTER_PUBLISH_EVENT_PAYLOAD_ONLY));
        String payload;
        if (eventPayloadOnly) {
            ReceivedEvent receivedEvent = new Gson().fromJson(message.toString(), ReceivedEvent.class);
            Event event = receivedEvent.getEvent();
            JsonObject payloadData = event.getPayloadData();
            payload = new Gson().toJson(payloadData);
        } else {
            payload = message.toString();
        }

        Map<String, String> authProperties = new HashMap<>();
        if (StringUtils.isNotBlank(authType)) {
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
                            this.internalAccessToken = getAccessToken(new String(clientId),
                                    new String(clientSecret), tokenEndpoint, scopes);
                            try {
                                encryptAndStoreCredential(provider, CLIENT_CREDENTIAL, INTERNAL_ACCESS_TOKEN,
                                        internalAccessToken);
                            } catch (SecretManagementException e) {
                                log.warn("Unable to store the newly generated access token in the secret manager.");
                            }
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
            try {
                executorService.submit(new HTTPSender(url, payload, headers, httpClient, authType, authProperties));
            } catch (RejectedExecutionException e) {
                EventAdapterUtil.logAndDrop(eventAdapterConfiguration.getName(), message,
                        "Job queue is full", e, log, tenantId);
            }
        } else {
            char[] username;
            char[] password;
            try {
                username = decryptCredential(provider, BASIC, USERNAME);
                password = decryptCredential(provider, BASIC, PASSWORD);
            } catch (SecretManagementException e) {
                username = dynamicProperties.get(ADAPTER_USERNAME).toCharArray();
                password = dynamicProperties.get(ADAPTER_PASSWORD).toCharArray();
            }
            try {
                executorService.submit(new HTTPSender(url, payload, new String(username), new String(password), headers,
                        httpClient));
            } catch (RejectedExecutionException e) {
                EventAdapterUtil.logAndDrop(eventAdapterConfiguration.getName(), message,
                        "Job queue is full", e, log, tenantId);
            }
        }
    }

    @Override
    public void disconnect() {
        //not required
    }

    @Override
    public void destroy() {
        //not required
    }

    @Override
    public boolean isPolled() {
        return false;
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

                if (this.getAuthType() != null) {
                    switch (this.getAuthType().toUpperCase()) {
                        case CLIENT_CREDENTIAL:
                            method.setRequestHeader("Authorization",
                                    "Bearer " + this.getAuthProperties().get(INTERNAL_ACCESS_TOKEN));
                            break;
                        case BEARER:
                            method.setRequestHeader("Authorization",
                                    "Bearer " + this.getAuthProperties().get(ACCESS_TOKEN));
                            break;
                        case API_KEY:
                            method.setRequestHeader(this.getAuthProperties().get(API_KEY_HEADER),
                                    this.getAuthProperties().get(API_KEY_VALUE));
                            break;
                        case BASIC:
                            if (this.getUsername() != null && !this.getUsername().trim().isEmpty() &&
                                    this.getPassword() != null && !this.getPassword().trim().isEmpty()) {
                                method.setRequestHeader("Authorization", "Basic " + Base64
                                        .encode((this.getUsername() + HTTPEventAdapterConstants.ENTRY_SEPARATOR + this
                                                .getPassword()).getBytes()));
                            }
                            break;
                        case NONE:
                            // Do nothing as there is no authentication.
                            break;
                    }
                } else if (this.getUsername() != null && !this.getUsername().trim().isEmpty()) {
                    method.setRequestHeader("Authorization", "Basic " + Base64
                            .encode((this.getUsername() + HTTPEventAdapterConstants.ENTRY_SEPARATOR + this
                                    .getPassword()).getBytes()));
                }

                int responseCode = this.getHttpClient().executeMethod(hostConfiguration, method);
                if (responseCode / 100 == 2) {
                    if (log.isDebugEnabled()) {
                        log.debug("[Id: " + uuid +  "] " +
                                "Successfully connected to the endpoint: " + this.url +
                                ". Received HTTP response code is: " + responseCode +
                                ". Response body : " + method.getResponseBodyAsString());
                    }
                } else if ((responseCode == 401 || responseCode == 403) &&
                        StringUtils.equalsIgnoreCase(CLIENT_CREDENTIAL, this.getAuthType())) {
                    if (log.isDebugEnabled()) {
                        log.debug("[Id: " + uuid + "] " +
                                "Received an unauthorized response from the endpoint: " + this.url +
                                ". Response code: " + responseCode +
                                ". Response body: " + method.getResponseBodyAsString() +
                                ". Hence refreshing the access token and retrying.");
                    }
                    retryWithNewAccessToken(method);
                } else {
                    log.error("[Id: " + uuid + "] Error while connecting to the endpoint: " + this.url +
                            ". Received HTTP response code is: " + responseCode +
                            ". Response body: " + method.getResponseBodyAsString());
                }
            } catch (UnknownHostException e) {
                EventAdapterUtil.logAndDrop(eventAdapterConfiguration.getName(), this.getPayload(),
                        "Cannot connect to " + this.getUrl(), e, log, tenantId);
            } catch (Throwable e) {
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
            char[] clientId;
            char[] clientSecret;
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext
                        .getThreadLocalCarbonContext();
                privilegedCarbonContext.setTenantId(tenantId);
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

                int attempts = 0;
                while (attempts < MAX_RETRY_ATTEMPTS) {
                    attempts++;
                    internalAccessToken =
                            EventAdapterUtil.getAccessToken(new String(clientId), new String(clientSecret), tokenEndpoint, scopes);
                    method.setRequestHeader("Authorization", "Bearer " + internalAccessToken);

                    int responseCode = this.getHttpClient().executeMethod(hostConfiguration, method);
                    if (responseCode / 100 == 2) {
                        if (log.isDebugEnabled()) {
                            log.debug("[Id: " + uuid + "] " +
                                    "Successfully connected to the endpoint: " + this.url +
                                    ". Received HTTP response code is: " + responseCode +
                                    ". Response body : " + method.getResponseBodyAsString());
                        }
                        try {
                            encryptAndStoreCredential(provider, CLIENT_CREDENTIAL, INTERNAL_ACCESS_TOKEN,
                                    internalAccessToken);
                        } catch (SecretManagementException e) {
                            log.warn("Unable to store the newly generated access token in the secret manager.");
                        }
                        return;
                    } else {
                        log.warn("[Id: " + uuid + "] Error while connecting to the endpoint: " + this.url +
                                ". Received HTTP response code is: " + responseCode +
                                ". Retry (attempt " + attempts + ")");
                        if (attempts == MAX_RETRY_ATTEMPTS) {
                            log.error("[Id: " + uuid + "] Error while connecting even after retrying with " +
                                    "new token to the endpoint: " + this.url +
                                    ". Received HTTP response code is: " + responseCode +
                                    ". Response body: " + method.getResponseBodyAsString());
                        }
                    }
                }
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

}
