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

package org.wso2.carbon.event.output.adapter.core;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterRuntimeException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Integration tests for {@link EventAdapterUtil}'s OAuth2 token acquisition methods.
 * These tests stand up a real local HTTP server and exercise the actual token-request wire
 * protocol end-to-end (form params, grant_type, response parsing) rather than mocking the
 * HTTP call, since that request shape is exactly what an external token endpoint (e.g. the
 * Verifone Messaging Service) will receive in production.
 */
public class EventAdapterUtilTest {

    private HttpServer server;

    @AfterMethod
    public void tearDown() {

        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    private String startTokenServer(int statusCode, String responseBody,
                                     AtomicReference<Map<String, String>> capturedParams) throws IOException {

        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/token", exchange -> handleTokenRequest(exchange, statusCode, responseBody,
                capturedParams));
        server.start();
        return "http://localhost:" + server.getAddress().getPort() + "/token";
    }

    private void handleTokenRequest(HttpExchange exchange, int statusCode, String responseBody,
                                     AtomicReference<Map<String, String>> capturedParams) throws IOException {

        try {
            byte[] requestBytes = exchange.getRequestBody().readAllBytes();
            String body = new String(requestBytes, StandardCharsets.UTF_8);
            capturedParams.set(parseFormParams(body));

            byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        } finally {
            exchange.close();
        }
    }

    private Map<String, String> parseFormParams(String body) {

        Map<String, String> params = new HashMap<>();
        Arrays.stream(body.split("&")).forEach(pair -> {
            String[] kv = pair.split("=", 2);
            String key = urlDecode(kv[0]);
            String value = kv.length > 1 ? urlDecode(kv[1]) : "";
            params.put(key, value);
        });
        return params;
    }

    private String urlDecode(String value) {

        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return value;
        }
    }

    // -----------------------------------------------------------------------
    // getAccessTokenPasswordGrant — the new PASSWORD_CREDENTIAL grant support
    // -----------------------------------------------------------------------

    @Test
    public void testGetAccessTokenPasswordGrant_sendsCorrectRequestAndParsesToken() throws Exception {

        AtomicReference<Map<String, String>> capturedParams = new AtomicReference<>();
        String tokenEndpoint = startTokenServer(200,
                "{\"access_token\":\"password-grant-access-token\",\"token_type\":\"Bearer\"}", capturedParams);

        String token = EventAdapterUtil.getAccessTokenPasswordGrant(
                "test-client-id", "test-client-secret", "svc-verifone-notify", "S3cretPass!",
                tokenEndpoint, "send:sms");

        Assert.assertEquals(token, "password-grant-access-token");

        Map<String, String> params = capturedParams.get();
        Assert.assertNotNull(params, "Token endpoint did not receive a request");
        Assert.assertEquals(params.get("grant_type"), "password");
        Assert.assertEquals(params.get("client_id"), "test-client-id");
        Assert.assertEquals(params.get("client_secret"), "test-client-secret");
        Assert.assertEquals(params.get("username"), "svc-verifone-notify");
        Assert.assertEquals(params.get("password"), "S3cretPass!");
        Assert.assertEquals(params.get("scope"), "send:sms");
    }

    @Test(expectedExceptions = OutputEventAdapterRuntimeException.class)
    public void testGetAccessTokenPasswordGrant_unsuccessfulResponse_throwsException() throws Exception {

        AtomicReference<Map<String, String>> capturedParams = new AtomicReference<>();
        String tokenEndpoint = startTokenServer(401, "{\"error\":\"invalid_grant\"}", capturedParams);

        EventAdapterUtil.getAccessTokenPasswordGrant(
                "bad-client-id", "bad-client-secret", "svc-verifone-notify", "wrong-password",
                tokenEndpoint, "send:sms");
    }

    @Test(expectedExceptions = OutputEventAdapterRuntimeException.class)
    public void testGetAccessTokenPasswordGrant_missingAccessTokenInResponse_throwsException() throws Exception {

        AtomicReference<Map<String, String>> capturedParams = new AtomicReference<>();
        String tokenEndpoint = startTokenServer(200, "{\"token_type\":\"Bearer\"}", capturedParams);

        EventAdapterUtil.getAccessTokenPasswordGrant(
                "test-client-id", "test-client-secret", "svc-verifone-notify", "S3cretPass!",
                tokenEndpoint, "send:sms");
    }

    // -----------------------------------------------------------------------
    // getAccessToken (CLIENT_CREDENTIAL) — regression coverage for the request-building refactor
    // -----------------------------------------------------------------------

    @Test
    public void testGetAccessToken_clientCredential_sendsCorrectRequestAndParsesToken() throws Exception {

        AtomicReference<Map<String, String>> capturedParams = new AtomicReference<>();
        String tokenEndpoint = startTokenServer(200,
                "{\"access_token\":\"client-credential-access-token\"}", capturedParams);

        String token = EventAdapterUtil.getAccessToken(
                "test-client-id", "test-client-secret", tokenEndpoint, "openid");

        Assert.assertEquals(token, "client-credential-access-token");

        Map<String, String> params = capturedParams.get();
        Assert.assertEquals(params.get("grant_type"), "client_credentials");
        Assert.assertEquals(params.get("client_id"), "test-client-id");
        Assert.assertEquals(params.get("client_secret"), "test-client-secret");
        Assert.assertNull(params.get("username"), "Client credentials request must not include a username param");
        Assert.assertNull(params.get("password"), "Client credentials request must not include a password param");
    }
}
