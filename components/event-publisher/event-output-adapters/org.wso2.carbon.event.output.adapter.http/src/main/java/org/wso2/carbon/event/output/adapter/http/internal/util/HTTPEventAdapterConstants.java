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
}
