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
package org.wso2.carbon.analytics.idp.client.external;

/**
 * External IdP Client Constants.
 */
public class ExternalIdPClientConstants {

    public static final String EXTERNAL_IDP_CLIENT_TYPE = "external";

    public static final String BASE_URL = "base.url";
    public static final String GRANT_TYPE = "grant.type";
    public static final String KM_TOKEN_URL = "km.token.url";
    public static final String KM_DCR_URL = "km.dcr.url";
    public static final String KM_CERT_ALIAS = "km.cert.alias";
    public static final String KM_USERNAME = "km.username";
    public static final String KM_PASSWORD = "km.password";
    public static final String OIDC_USER_INFO_ALGO = "oidc.user.info.algo";
    public static final String IDP_BASE_URL = "idp.base.url";
    public static final String IDP_CERT_ALIAS = "idp.cert.alias";
    public static final String IDP_USERNAME = "idp.username";
    public static final String IDP_PASSWORD = "idp.password";
    public static final String PORTAL_APP_CONTEXT = "portal.app.context";
    public static final String STATUS_DB_APP_CONTEXT = "status.dashboard.app.context";
    public static final String BR_DB_APP_CONTEXT = "business.rules.app.context";
    public static final String SP_CLIENT_ID = "sp.client.id";
    public static final String PORTAL_CLIENT_ID = "portal.client.id";
    public static final String STATUS_DB_CLIENT_ID = "status.dashboard.client.id";
    public static final String BR_DB_CLIENT_ID = "business.rules.client.id";
    public static final String SP_CLIENT_SECRET = "sp.client.secret";
    public static final String PORTAL_CLIENT_SECRET = "portal.client.secret";
    public static final String STATUS_DB_CLIENT_SECRET = "status.dashboard.client.secret";
    public static final String BR_DB_CLIENT_SECRET = "business.rules.client.secret";

    public static final String DEFAULT_BASE_URL = "https://localhost:9643";
    public static final String DEFAULT_KM_TOKEN_URL = "https://localhost:9443/oauth2";
    public static final String DEFAULT_KM_DCR_URL = "https://localhost:9443/identity/connect/register";
    public static final String DEFAULT_KM_CERT_ALIAS = "wso2carbon";
    public static final String DEFAULT_KM_USERNAME = "admin";
    public static final String DEFAULT_KM_PASSWORD = "admin";
    public static final String DEFAULT_OIDC_USER_INFO_ALGO = "SHA256withRSA";
    public static final String DEFAULT_IDP_BASE_URL = "https://localhost:9443/scim2";
    public static final String DEFAULT_IDP_CERT_ALIAS = "wso2carbon";
    public static final String DEFAULT_IDP_USERNAME = "admin";
    public static final String DEFAULT_IDP_PASSWORD = "admin";
    public static final String DEFAULT_SP_APP_CONTEXT = "sp";
    public static final String DEFAULT_PORTAL_APP_CONTEXT = "portal";
    public static final String DEFAULT_STATUS_DB_APP_CONTEXT = "monitoring";
    public static final String DEFAULT_BR_DB_APP_CONTEXT = "business-rules";

    public static final String SP_APP_NAME = "sp";
    public static final String PORTAL_APP_NAME = "sp_portal";
    public static final String STATUS_DB_APP_NAME = "sp_status_dashboard";
    public static final String BR_DB_APP_NAME = "sp_business_rules";

    public static final String TOKEN_ID = "Token_Id";
    public static final String EXPIRES_IN = "Expires_In";
    public static final String CLIENT_ID = "Client_Id";
    public static final String REDIRECT_URL = "Redirect_Url";
    public static final String CALLBACK_URL_NAME = "Callback_Url";
    public static final String REQUEST_URL = "REQUEST_URL";

    public static final String CALLBACK_URL = "/login/callback";
    public static final String REGEX_BASE_START = "regexp=(";
    public static final String FORWARD_SLASH = "/";
    public static final String REGEX_BASE_END = ".*)";

    public static final String FILTER_PREFIX_USER = "userName Eq ";
    public static final String FILTER_PREFIX_GROUP = "displayName Eq ";
    public static final String EMPTY_STRING = "";
    public static final String RESOURCES = "Resources";

    public static final String REVOKE_POSTFIX = "/revoke";
    public static final String TOKEN_POSTFIX = "/token";
    public static final String INTROSPECT_POSTFIX = "/introspect";
    public static final String AUTHORIZE_POSTFIX = "/authorize";

    public static final String SCIM2_USERNAME = "userName";
    public static final String SCIM2_GROUPS = "groups";
    public static final String SCIM2_DISPLAY = "display";

    private ExternalIdPClientConstants() {
    }
}
