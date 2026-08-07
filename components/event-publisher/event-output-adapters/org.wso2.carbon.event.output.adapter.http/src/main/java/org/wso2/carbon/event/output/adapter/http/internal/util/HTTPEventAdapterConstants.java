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

package org.wso2.carbon.event.output.adapter.http.internal.util;

public class HTTPEventAdapterConstants {

    public static final String ADAPTER_TYPE_HTTP = "http";
    public static final String ADAPTER_MESSAGE_URL = "http.url";
    public static final String ADAPTER_MESSAGE_URL_HINT = "http.url.hint";
    public static final int ADAPTER_MIN_THREAD_POOL_SIZE = 8;
    public static final int ADAPTER_MAX_THREAD_POOL_SIZE = 100;
    public static final int ADAPTER_EXECUTOR_JOB_QUEUE_SIZE = 2000;
    public static final long DEFAULT_KEEP_ALIVE_TIME_IN_MILLIS = 20000;
    public static final String ADAPTER_MIN_THREAD_POOL_SIZE_NAME = "minThread";
    public static final String ADAPTER_MAX_THREAD_POOL_SIZE_NAME = "maxThread";
    public static final String ADAPTER_KEEP_ALIVE_TIME_NAME = "keepAliveTimeInMillis";
    public static final String ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME = "jobQueueSize";
    public static final String ADAPTER_PROXY_HOST = "http.proxy.host";
    public static final String ADAPTER_PROXY_HOST_HINT = "http.proxy.host.hint";
    public static final String ADAPTER_PROXY_PORT = "http.proxy.port";
    public static final String ADAPTER_PROXY_PORT_HINT = "http.proxy.port.hint";
    public static final String ADAPTER_USERNAME = "http.username";
    public static final String ADAPTER_USERNAME_HINT = "http.username.hint";
    public static final String ADAPTER_PASSWORD = "http.password";
    public static final String ADAPTER_PASSWORD_HINT = "http.password.hint";
    public static final String ADAPTER_AUTH_TYPE = "http.authType";
    public static final String ADAPTER_CLIENT_ID = "http.clientId";
    public static final String ADAPTER_CLIENT_SECRET = "http.clientSecret";
    public static final String ADAPTER_TOKEN_ENDPOINT = "http.tokenEndpoint";
    public static final String ADAPTER_SCOPES = "http.scopes";
    public static final String ADAPTER_ACCESS_TOKEN = "http.accessToken";
    public static final String ADAPTER_API_KEY_HEADER = "http.apiKeyHeader";
    public static final String ADAPTER_API_KEY_VALUE = "http.apiKeyValue";
    public static final String ADAPTER_HEADERS = "http.headers";
    public static final String ADAPTER_HEADERS_HINT = "http.headers.hint";
    public static final String ADAPTER_SECRET_PROVIDER = "http.secret.provider";
    public static final String ADAPTER_SECRET_PROVIDER_HINT = "Provider identifier for secret management. " +
            "Defaults to EMAIL_PROVIDER if not set.";
    public static final String HEADER_SEPARATOR = ",";
    public static final String ENTRY_SEPARATOR = ":";
    public static final String ADAPTER_HTTP_CLIENT_METHOD = "http.client.method";
    public static final String CONSTANT_HTTP_POST = "HttpPost";
    public static final String CONSTANT_HTTP_PUT = "HttpPut";
    public static final String CONSTANT_HTTP_GET = "HttpGet";
    public static final String ENABLE_FORM_URL_ENCODED = "enableFormUrlEncoded";
    public static final int MAX_RETRY_ATTEMPTS = 2;

    //configurations for the httpConnectionManager
    public static final String DEFAULT_MAX_CONNECTIONS_PER_HOST = "defaultMaxConnectionsPerHost";
    public static final int DEFAULT_DEFAULT_MAX_CONNECTIONS_PER_HOST = 2;
    public static final String MAX_TOTAL_CONNECTIONS = "maxTotalConnections";
    public static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 20;

    // Authentication types.
    public static final String CLIENT_CREDENTIAL = "CLIENT_CREDENTIAL";
    public static final String BASIC = "BASIC";
    public static final String BEARER = "BEARER";
    public static final String API_KEY = "API_KEY";
    public static final String PASSWORD_CREDENTIAL = "PASSWORD_CREDENTIAL";
    public static final String NONE = "NONE";

    // Credential properties.
    public static final String EMAIL_PROVIDER = "EMAIL_PROVIDER";
    public static final String USERNAME = "userName";
    public static final String PASSWORD = "password";
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String INTERNAL_ACCESS_TOKEN = "internalAccessToken";
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String API_KEY_HEADER = "apiKeyHeader";
    public static final String API_KEY_VALUE = "apiKeyValue";

    /**
     * Constants related to log management.
     */
    public static class LogConstants {

        public static final String EMAIL_PUBLISHER_EVENT_ADAPTER_NAME = "EmailPublisher";
        public static final String HTTP_EVENT_ADAPTER_SERVICE = "http-output-event-adapter";

        /**
         * Define action IDs for diagnostic logs.
         */
        public static class ActionIDs {

            public static final String SEND_EMAIL = "send-email";
        }

        /**
         * Define common and reusable Input keys for diagnostic logs.
         */
        public static class InputKeys {

            private InputKeys() {

            }

            public static final String RESPONSE_CODE = "Response code";
            public static final String ERROR_RESPONSE = "Error Response";
        }
    }


    // Default value.
    public static final String DEFAULT_SECRET_PROVIDER = EMAIL_PROVIDER;

    // Sync HTTP client connection pool configuration (read from global properties).
    public static final String SYNC_CONNECTION_TIMEOUT = "syncConnectionTimeout";
    public static final int DEFAULT_SYNC_CONNECTION_TIMEOUT_MS = 1000;
    public static final String SYNC_READ_TIMEOUT = "syncReadTimeout";
    public static final int DEFAULT_SYNC_READ_TIMEOUT_MS = 1000;
    public static final String SYNC_CONNECTION_REQUEST_TIMEOUT = "syncConnectionRequestTimeout";
    public static final int DEFAULT_SYNC_CONNECTION_REQUEST_TIMEOUT_MS = 1000;
    public static final String SYNC_POOL_SIZE = "syncPoolSize";
    public static final int DEFAULT_SYNC_POOL_SIZE = 20;
    public static final String SYNC_MAX_CONNECTIONS_PER_ROUTE = "syncMaxConnectionsPerRoute";
    public static final int DEFAULT_SYNC_MAX_CONNECTIONS_PER_ROUTE = 2;
    public static final String SYNC_RESPONSE_LIMIT_BYTES = "syncResponseLimitBytes";
    public static final long DEFAULT_SYNC_RESPONSE_LIMIT_BYTES = 1048576L;
    public static final String SYNC_RETRY_COUNT = "syncRetryCount";
    public static final int DEFAULT_SYNC_RETRY_COUNT = 0;

    // Dynamic property to request synchronous delivery.
    public static final String ADAPTER_MESSAGE_HTTP_SYNC = "http.sync";
    public static final String ADAPTER_MESSAGE_HTTP_SYNC_HINT = "http.sync.hint";

    /**
     * Error messages for the HTTP adapter sync publish path.
     */
    public enum ErrorMessage {

        SYNC_CLIENT_INIT_FAILED(
                "HTTP-OA-65000",
                "Adapter '%s' could not be configured for synchronous publishing. Check the adapter settings and try again."),

        SYNC_CLIENT_NOT_INITIALIZED(
                "HTTP-OA-65001",
                "Adapter '%s' is not ready; the connection has not been established. Ensure the adapter initialised successfully."),

        SYNC_PUBLISH_FAILED_WITH_RESPONSE(
                "HTTP-OA-65002",
                "Publishing to '%s' failed. Received HTTP %d. Server response: %s"),

        SYNC_PUBLISH_UNAUTHORIZED(
                "HTTP-OA-65003",
                "Publishing to '%s' failed with HTTP 401 (Unauthorized). Verify that the configured credentials or access token are correct and have not expired."),

        SYNC_PUBLISH_FORBIDDEN(
                "HTTP-OA-65004",
                "Publishing to '%s' failed with HTTP 403 (Forbidden). The configured account does not have permission to publish to this endpoint."),

        SYNC_PUBLISH_BAD_REQUEST(
                "HTTP-OA-65005",
                "Publishing to '%s' failed with HTTP 400 (Bad Request). Check the event format and required fields. Server response: %s"),

        SYNC_PUBLISH_TOO_MANY_REQUESTS(
                "HTTP-OA-65006",
                "Publishing to '%s' failed with HTTP 429 (Too Many Requests). Reduce the publishing rate or wait before retrying."),

        SYNC_PUBLISH_SERVICE_UNAVAILABLE(
                "HTTP-OA-65007",
                "Publishing to '%s' failed with HTTP %d. The server is temporarily unavailable. Wait a moment and retry."),

        SYNC_PUBLISH_SERVER_ERROR(
                "HTTP-OA-65008",
                "Publishing to '%s' failed with HTTP 500. The server could not process the request. Server response: %s"),

        SYNC_PUBLISH_FAILED_IO(
                "HTTP-OA-65009",
                "Publishing to '%s' failed. The connection to the server was interrupted or timed out. Check network connectivity and that the endpoint is reachable."),

        SYNC_TOKEN_REFRESH_MISSING_CREDS(
                "HTTP-OA-65010",
                "Adapter '%s' could not obtain a new access token because the client ID or client secret is missing. Verify the adapter credentials."),

        SYNC_TOKEN_FETCH_FAILED(
                "HTTP-OA-65011",
                "Adapter '%s' could not obtain a new access token. The token endpoint may be unavailable or the credentials may be incorrect."),

        SYNC_INVALID_PROXY_PORT(
                "HTTP-OA-65012",
                "Proxy port '%s' is not valid. The proxy configuration will be ignored for this adapter.");

        private final String code;
        private final String message;

        ErrorMessage(String code, String message) {

            this.code = code;
            this.message = message;
        }

        public String getCode() { 

                return code;
        }

        public String getMessage() {

                return message;
        }

        public String formatMessage(Object... args) {

                return String.format(message, args);
        }

        @Override
        public String toString() {
                
                return code + " | " + message;
        }
    }
}
