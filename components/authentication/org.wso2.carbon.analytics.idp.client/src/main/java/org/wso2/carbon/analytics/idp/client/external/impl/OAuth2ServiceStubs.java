/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.analytics.idp.client.external.impl;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.Response;
import org.wso2.carbon.analytics.idp.client.core.api.AnalyticsHttpClientBuilderService;
import org.wso2.carbon.analytics.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.analytics.idp.client.core.utils.IdPClientConstants;
import org.wso2.carbon.analytics.idp.client.external.util.ExternalIdPClientUtil;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 service stubs.
 */
public class OAuth2ServiceStubs {
    private AnalyticsHttpClientBuilderService analyticsHttpClientBuilderService;
    private String tokenEndpoint;
    private String revokeEndpoint;
    private String introspectEndpoint;
    private String username;
    private String password;
    private int connectionTimeout;
    private int readTimeout;

    /**
     * Constructor.
     *
     * @param tokenEndpoint      Token endpoint URL
     * @param revokeEndpoint     Revoke endpoint URL
     * @param introspectEndpoint Token introspection endpoint
     * @param username           Username of Key Manager
     * @param password           Password of Key Manager
     */
    public OAuth2ServiceStubs(AnalyticsHttpClientBuilderService service, String tokenEndpoint, String revokeEndpoint,
                              String introspectEndpoint, String username, String password, int connectionTimeout,
                              int readTimeout) {
        this.analyticsHttpClientBuilderService = service;
        this.tokenEndpoint = tokenEndpoint;
        this.revokeEndpoint = revokeEndpoint;
        this.introspectEndpoint = introspectEndpoint;
        this.username = username;
        this.password = password;
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
    }

    /**
     * Create and return OAuth2 token service stubs.
     *
     * @return OAuth2 token service stub
     * @throws IdPClientException if error occurs while crating OAuth2 token service stub
     */
    public OAuth2ServiceStubs.TokenServiceStub getTokenServiceStub() throws IdPClientException {
        return this.analyticsHttpClientBuilderService
                .buildWithoutInterceptor(this.connectionTimeout, this.readTimeout,
                        OAuth2ServiceStubs.TokenServiceStub.class, tokenEndpoint);
    }

    /**
     * Create and return OAuth2 revoke service stubs.
     *
     * @return OAuth2 revoke service stub
     * @throws IdPClientException if error occurs while crating OAuth2 revoke service stub
     */
    public OAuth2ServiceStubs.RevokeServiceStub getRevokeServiceStub() throws IdPClientException {
        return this.analyticsHttpClientBuilderService
                .buildWithoutInterceptor(this.connectionTimeout, this.readTimeout,
                        OAuth2ServiceStubs.RevokeServiceStub.class, revokeEndpoint);
    }

    /**
     * Create and return OAuth2 Introspection service stubs.
     *
     * @return OAuth2 introspection service stub
     * @throws IdPClientException if error occurs while crating OAuth2 introspection service stub
     */
    public OAuth2ServiceStubs.IntrospectionServiceStub getIntrospectionServiceStub() throws IdPClientException {
        return this.analyticsHttpClientBuilderService
                .build(username, password, this.connectionTimeout, this.readTimeout,
                        OAuth2ServiceStubs.IntrospectionServiceStub.class, introspectEndpoint);
    }

    /**
     * This interface is for /token API stub.
     */
    @Headers("Content-Type: application/x-www-form-urlencoded")
    public interface TokenServiceStub {

        @Headers("Authorization: Basic {auth_token}")
        @RequestLine("POST")
        Response generateAccessToken(@Param("auth_token") String authToken, String body);

        /**
         * Get a access token by Password grant type.
         *
         * @param username     Username of the user
         * @param password     Password of the user
         * @param scopes       Required scopes (space separated) for the access token
         * @param clientId     Consumer Key of the application
         * @param clientSecret Consumer Secret of the application
         * @return Feign Response Object
         */
        default Response generatePasswordGrantAccessToken(String username, String password, String scopes,
                                                          String clientId, String clientSecret) {
            String credentials = clientId + ':' + clientSecret;
            String authToken = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            Map<String, String> requestParam = new HashMap<>();
            requestParam.put("grant_type", IdPClientConstants.PASSWORD_GRANT_TYPE);
            requestParam.put("username", username);
            requestParam.put("password", password);
            return generateAccessToken(authToken, ExternalIdPClientUtil.getRequestBody(requestParam));
        }

        /**
         * Get a access token by Authorization Code grant type.
         *
         * @param code         Authorization Code
         * @param redirectUri  Callback URL
         * @param scopes       Required scopes (space separated) for the access token
         * @param clientId     Consumer Key of the application
         * @param clientSecret Consumer Secret of the application
         * @return Feign Response Object
         */
        default Response generateAuthCodeGrantAccessToken(String code, String redirectUri, String scopes,
                                                          String clientId, String clientSecret) {
            String credentials = clientId + ':' + clientSecret;
            String authToken = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            code = code.split("&")[0];
            Map<String, String> requestParam = new HashMap<>();
            requestParam.put("grant_type", IdPClientConstants.AUTHORIZATION_CODE_GRANT_TYPE);
            requestParam.put("code", code);
            requestParam.put("redirect_uri", redirectUri);
            requestParam.put("scope", "openid");
            return generateAccessToken(authToken, ExternalIdPClientUtil.getRequestBody(requestParam));
        }

        /**
         * Get a access token by Refresh grant type.
         *
         * @param refreshToken Refresh Token
         * @param scopes       Required scopes (space separated) for the access token
         * @param clientId     Consumer Key of the application
         * @param clientSecret Consumer Secret of the application
         * @return Feign Response Object
         */
        default Response generateRefreshGrantAccessToken(String refreshToken, String scopes,
                                                         String clientId, String clientSecret) {
            String credentials = clientId + ':' + clientSecret;
            String authToken = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            Map<String, String> requestParam = new HashMap<>();
            requestParam.put("grant_type", IdPClientConstants.REFRESH_GRANT_TYPE);
            requestParam.put("refresh_token", refreshToken);
            return generateAccessToken(authToken, ExternalIdPClientUtil.getRequestBody(requestParam));
        }
    }

    /**
     * This interface is for /revoke API stub.
     */
    @Headers("Content-Type: application/x-www-form-urlencoded")
    public interface RevokeServiceStub {

        @RequestLine("POST")
        @Headers("Authorization: Basic {auth_token}")
        Response revokeToken(@Param("auth_token") String authToken, String token);

        /**
         * Revoke access token.
         *
         * @param token        Access token that is required to be revoked
         * @param clientId     Consumer Key of the application
         * @param clientSecret Consumer Secret of the application
         * @return Feign Response Object
         */
        default Response revokeAccessToken(String token, String clientId, String clientSecret) {
            String credentials = clientId + ':' + clientSecret;
            String authToken = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            Map<String, String> requestParam = Collections.singletonMap("token", token);
            return revokeToken(authToken, ExternalIdPClientUtil.getRequestBody(requestParam));
        }
    }

    /**
     * This interface is for /introspect API stub.
     */
    @Headers("Content-Type: application/x-www-form-urlencoded")
    public interface IntrospectionServiceStub {
        @RequestLine("POST")
        Response introspectToken(String token);

        default Response introspectAccessToken(String token) {
            Map<String, String> requestParam = Collections.singletonMap("token", token);
            return introspectToken(ExternalIdPClientUtil.getRequestBody(requestParam));
        }
    }
}
