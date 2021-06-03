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

    public static final String BASE_URL = "baseUrl";
    public static final String GRANT_TYPE = "grantType";
    public static final String KM_TOKEN_URL = "kmTokenUrl";
    public static final String KM_DCR_URL = "kmDcrUrl";
    public static final String KM_CERT_ALIAS = "kmCertAlias";
    public static final String KM_USERNAME = "kmUsername";
    public static final String KM_PASSWORD = "kmPassword";
    public static final String OIDC_USER_INFO_ALGO = "oidcUserInfoAlgo";
    public static final String IDP_BASE_URL = "idpBaseUrl";
    public static final String IDP_CERT_ALIAS = "idpCertAlias";
    public static final String IDP_USERNAME = "idpUsername";
    public static final String IDP_PASSWORD = "idpPassword";
    public static final String PORTAL_APP_CONTEXT = "portalAppContext";
    public static final String STATUS_DB_APP_CONTEXT = "statusDashboardAppContext";
    public static final String BR_DB_APP_CONTEXT = "businessRulesAppContext";
    public static final String SP_CLIENT_ID = "spClientId";
    public static final String PORTAL_CLIENT_ID = "portalClientId";
    public static final String STATUS_DB_CLIENT_ID = "statusDashboardClientId";
    public static final String BR_DB_CLIENT_ID = "businessRulesClientId";
    public static final String SP_CLIENT_SECRET = "spClientSecret";
    public static final String PORTAL_CLIENT_SECRET = "portalClientSecret";
    public static final String STATUS_DB_CLIENT_SECRET = "statusDashboardClientSecret";
    public static final String BR_DB_CLIENT_SECRET = "businessRulesClientSecret";
    public static final String CACHE_TIMEOUT = "cacheTimeout";
    public static final String DATABASE_NAME = "databaseName";
    public static final String DCR_APP_OWNER = "dcrAppOwner";
    public static final String CONNECTION_TIMEOUT = "connectionTimeout";
    public static final String READ_TIMEOUT = "readTimeout";
    public static final String EXTERNAL_SSO_LOGOUT_URL = "externalLogoutUrl";

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
    public static final String DEFAULT_CACHE_TIMEOUT = "900";
    public static final String DEFAULT_DATABASE_NAME = "WSO2_OAUTH_APP_DB";
    public static final String DEFAULT_CONNECTION_TIMEOUT = "10000";
    public static final String DEFAULT_READ_TIMEOUT = "60000";
    public static final String DEFAULT_EXTERNAL_SSO_LOGOUT_URL = "https://localhost:9443/oidc/logout";
    public static final String SSO_LOGING_ID_TOKEN_TAIL = "?id_token_hint=";
    public static final String JWKS_URL = "jwksUrl";
    public static final String DEFULT_JWKS_URL = "https://localhost:9443/oauth2/jwks";
    public static final String DEFAULT_FILTERED_GROUPS_FLAG = "false";

    public static final String JWT_PROPERTY_PREFERRED_USERNAME = "preferred_username";
    public static final String JWT_PROPERTY_UNIQUE_NAME = "unique_name";

    public static final String SP_APP_NAME = "sp";
    public static final String PORTAL_APP_NAME = "sp_portal";
    public static final String STATUS_DB_APP_NAME = "sp_status_dashboard";
    public static final String BR_DB_APP_NAME = "sp_business_rules";

    public static final String CLIENT_ID = "Client_Id";
    public static final String REDIRECT_URL = "Redirect_Url";
    public static final String CALLBACK_URL_NAME = "Callback_Url";
    public static final String SCOPE = "Scope";
    public static final String IS_FILTERED_GROUPS_ENABLED = "enableFilteredGroups";
    public static final String REQUEST_URL = "REQUEST_URL";
    public static final String USER_STORE = "defaultUserStore";
    public static final String DEFAULT_USER_STORE = "PRIMARY";
    public static final String INTROSPECTION_URL = "introspectionUrl";
    public static final String DEFAULT_SCOPE = "openid";

    public static final String CALLBACK_URL = "/login/callback/";
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
    public static final String SCIM2_ID = "id";

    public static final String OAUTHAPP_TABLE = "OAUTHAPP";
    public static final String OAUTH_APP_TABLE_CHECK = "OAUTH_APP_TABLE_CHECK";
    public static final String CREATE_TABLE_TEMPLATE = "CREATE_OAUTH_APP_TABLE";
    public static final String ADD_OAUTH_APP_TEMPLATE = "ADD_OAUTH_APP";
    public static final String UPDATE_OAUTH_APP_TEMPLATE = "UPDATE_OAUTH_APP";
    public static final String RETRIEVE_OAUTH_APP_TEMPLATE = "RETRIEVE_OAUTH_APP";

    private ExternalIdPClientConstants() {
    }
}
