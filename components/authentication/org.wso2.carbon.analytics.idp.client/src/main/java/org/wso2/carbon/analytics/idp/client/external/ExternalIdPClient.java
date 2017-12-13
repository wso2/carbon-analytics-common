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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import feign.Response;
import feign.gson.GsonDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.idp.client.core.api.IdPClient;
import org.wso2.carbon.analytics.idp.client.core.exception.AuthenticationException;
import org.wso2.carbon.analytics.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.analytics.idp.client.core.models.Role;
import org.wso2.carbon.analytics.idp.client.core.models.User;
import org.wso2.carbon.analytics.idp.client.core.utils.IdPClientConstants;
import org.wso2.carbon.analytics.idp.client.external.dao.OAuthAppDAO;
import org.wso2.carbon.analytics.idp.client.external.dto.DCRClientInfo;
import org.wso2.carbon.analytics.idp.client.external.dto.DCRError;
import org.wso2.carbon.analytics.idp.client.external.dto.OAuth2IntrospectionResponse;
import org.wso2.carbon.analytics.idp.client.external.dto.OAuth2TokenInfo;
import org.wso2.carbon.analytics.idp.client.external.dto.SCIMGroupList;
import org.wso2.carbon.analytics.idp.client.external.impl.DCRMServiceStub;
import org.wso2.carbon.analytics.idp.client.external.impl.OAuth2ServiceStubs;
import org.wso2.carbon.analytics.idp.client.external.impl.SCIM2ServiceStub;
import org.wso2.carbon.analytics.idp.client.external.models.ExternalSession;
import org.wso2.carbon.analytics.idp.client.external.models.OAuthApplicationInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implementation class for external IdP based on OAuth2 and SCIM2.
 */
public class ExternalIdPClient implements IdPClient {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalIdPClient.class);

    private DCRMServiceStub dcrmServiceStub;
    private OAuth2ServiceStubs oAuth2ServiceStubs;
    private SCIM2ServiceStub scimServiceStub;
    private String baseUrl;
    private String authorizeEndpoint;
    private String grantType;
    private String signingAlgo;
    private String adminRoleDisplayName;
    private OAuthAppDAO oAuthAppDAO;

    private Cache<String, ExternalSession> tokenCache;

    //Here the user given context are mapped to the OAuthApp Info.
    private Map<String, OAuthApplicationInfo> oAuthAppInfoMap;

    public ExternalIdPClient(String baseUrl, String authorizeEndpoint, String grantType, String singingAlgo,
                             String adminRoleDisplayName, Map<String, OAuthApplicationInfo> oAuthAppInfoMap,
                             int cacheTimeout, OAuthAppDAO oAuthAppDAO, DCRMServiceStub dcrmServiceStub,
                             OAuth2ServiceStubs oAuth2ServiceStubs, SCIM2ServiceStub scimServiceStub)
            throws IdPClientException {
        this.baseUrl = baseUrl;
        this.authorizeEndpoint = authorizeEndpoint;
        this.grantType = grantType;
        this.signingAlgo = singingAlgo;
        this.oAuthAppInfoMap = oAuthAppInfoMap;
        this.adminRoleDisplayName = adminRoleDisplayName;
        this.oAuthAppDAO = oAuthAppDAO;
        this.dcrmServiceStub = dcrmServiceStub;
        this.oAuth2ServiceStubs = oAuth2ServiceStubs;
        this.scimServiceStub = scimServiceStub;
        this.tokenCache = CacheBuilder.newBuilder()
                .expireAfterWrite(cacheTimeout, TimeUnit.SECONDS)
                .build();
    }

    private static String removeCRLFCharacters(String str) {
        return str.replace('\n', '_').replace('\r', '_');
    }

    public void init(String kmUserName) throws IdPClientException {
        this.oAuthAppDAO.init();
        this.oAuthAppDAO.createTable();
        for (Map.Entry<String, OAuthApplicationInfo> oAuthApplicationInfoEntry : this.oAuthAppInfoMap.entrySet()) {
            String clientId = oAuthApplicationInfoEntry.getValue().getClientId();
            String clientSecret = oAuthApplicationInfoEntry.getValue().getClientSecret();
            OAuthApplicationInfo persistedOAuthApp =
                    this.oAuthAppDAO.getOAuthApp(oAuthApplicationInfoEntry.getValue().getClientName());
            if (persistedOAuthApp != null) {
                if (clientId != null || clientSecret != null) {
                    if (clientId != null) {
                        persistedOAuthApp.setClientId(clientId);
                    }
                    if (clientSecret != null) {
                        persistedOAuthApp.setClientSecret(clientSecret);
                    }
                    this.oAuthAppDAO.updateOAuthApp(persistedOAuthApp);
                }
                this.oAuthAppInfoMap.replace(oAuthApplicationInfoEntry.getKey(), persistedOAuthApp);
            } else {
                registerApplication(oAuthApplicationInfoEntry.getKey(),
                        oAuthApplicationInfoEntry.getValue().getClientName(), kmUserName);
            }
        }
    }

    @Override
    public List<Role> getAllRoles() throws IdPClientException {
        Response response = scimServiceStub.getAllGroups();
        if (response == null) {
            String errorMessage = "Error occurred while retrieving all groups. Error : Response is null.";
            LOG.error(errorMessage);
            throw new IdPClientException(errorMessage);
        }
        if (response.status() == 200) {
            try {
                SCIMGroupList scimGroups = (SCIMGroupList) new GsonDecoder().decode(response, SCIMGroupList.class);
                List<SCIMGroupList.SCIMGroupResources> resources = scimGroups.getResources();
                if (resources != null) {
                    return resources.stream().map(
                            (resource) -> new Role(resource.getId(), resource.getDisplayName())
                    ).collect(Collectors.toList());
                }
                return new ArrayList<>();
            } catch (IOException e) {
                String errorMessage = "Error occurred while parsing the response when retrieving groups. Response: '"
                        + response.body().toString();
                LOG.error(errorMessage, e);
                throw new IdPClientException(errorMessage, e);
            }
        } else {
            String errorMessage = "Error occurred while retrieving groups. HTTP error code: " + response.status() +
                    " Error Response: " + getErrorMessage(response);
            LOG.error(errorMessage);
            throw new IdPClientException(errorMessage);
        }
    }

    @Override
    public Role getAdminRole() throws IdPClientException {
        Response response = scimServiceStub
                .getFilteredGroups(ExternalIdPClientConstants.FILTER_PREFIX_GROUP + this.adminRoleDisplayName);
        if (response == null) {
            String errorMessage = "Error occurred while retrieving admin group '" + this.adminRoleDisplayName +
                    "'. Error : Response is null.";
            LOG.error(errorMessage);
            throw new IdPClientException(errorMessage);
        }
        if (response.status() == 200) {
            try {
                SCIMGroupList scimGroups = (SCIMGroupList) new GsonDecoder().decode(response, SCIMGroupList.class);
                List<SCIMGroupList.SCIMGroupResources> resources = scimGroups.getResources();
                if (resources != null) {
                    return new Role(resources.get(0).getId(), resources.get(0).getDisplayName());
                }
                String errorMessage = "Error occurred while retrieving admin group '" + this.adminRoleDisplayName +
                        "'. Admin role not found in the user store.";
                LOG.error(errorMessage);
                throw new IdPClientException(errorMessage);
            } catch (IOException e) {
                String errorMessage = "Error occurred while parsing the response when retrieving admin group '"
                        + this.adminRoleDisplayName + "'. Response: '" + response.body().toString();
                LOG.error(errorMessage, e);
                throw new IdPClientException(errorMessage, e);
            }
        } else {
            String errorMessage = "Error occurred while retrieving admin group '" + this.adminRoleDisplayName + "'. " +
                    "HTTP error code: " + response.status() + " Error Response: " + getErrorMessage(response);
            LOG.error(errorMessage);
            throw new IdPClientException(errorMessage);
        }
    }

    @Override
    public User getUser(String name) throws IdPClientException {
        Response response = scimServiceStub.searchUser(ExternalIdPClientConstants.FILTER_PREFIX_USER + name);
        if (response == null) {
            String errorMessage =
                    "Error occurred while retrieving user, '" + name + "'. Error : Response is null.";
            LOG.error(errorMessage);
            throw new IdPClientException(errorMessage);
        }
        if (response.status() == 200) {
            String responseBody = response.body().toString();
            JsonParser parser = new JsonParser();
            JsonObject parsedResponseBody = (JsonObject) parser.parse(responseBody);
            JsonArray users = (JsonArray) parsedResponseBody.get(ExternalIdPClientConstants.RESOURCES);
            if (users != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Retrieving all roles for getting role id of the roles of the user '" + name + "'.");
                }
                List<Role> allRolesInUserStore = getAllRoles();

                Map<String, String> userProperties = new HashMap<>();
                List<Role> userRoles = new ArrayList<>();

                JsonObject scimUser = (JsonObject) users.get(0);
                for (Map.Entry<String, JsonElement> entry : scimUser.entrySet()) {
                    switch (entry.getKey()) {
                        case ExternalIdPClientConstants.SCIM2_USERNAME:
                            break;
                        case ExternalIdPClientConstants.SCIM2_GROUPS:
                            JsonArray scimGroups = scimUser.get(ExternalIdPClientConstants.SCIM2_GROUPS)
                                    .getAsJsonArray();
                            List<String> userGroupsDisplayNameList = new ArrayList<>();
                            scimGroups.forEach(scimGroup -> {
                                        JsonElement displayName = ((JsonObject) scimGroup)
                                                .get(ExternalIdPClientConstants.SCIM2_DISPLAY);
                                        userGroupsDisplayNameList.add(displayName.getAsString());
                                    }
                            );
                            userRoles = allRolesInUserStore.stream()
                                    .filter(role -> userGroupsDisplayNameList.contains(role.getDisplayName()))
                                    .collect(Collectors.toList());
                            break;
                        default:
                            userProperties.put(entry.getKey(), entry.getValue().toString());
                    }
                }
                return new User(name, userProperties, userRoles);
            } else {
                return null;
            }
        } else {
            String errorMessage = "Error occurred while retrieving user, '" + name + "'. HTTP error code: "
                    + response.status() + " Error Response: " + getErrorMessage(response);
            LOG.error(errorMessage);
            throw new IdPClientException(errorMessage);
        }
    }

    @Override
    public List<Role> getUserRoles(String name) throws IdPClientException {
        List<Role> userGroups = new ArrayList<>();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving user roles by retrieving user '" + name + "'.");
        }
        User user = getUser(name);
        if (user != null) {
            userGroups = user.getRoles();
        }
        return userGroups;
    }

    @Override
    public Map<String, String> login(Map<String, String> properties) throws IdPClientException {
        Map<String, String> returnProperties = new HashMap<>();
        String grantType = properties.getOrDefault(IdPClientConstants.GRANT_TYPE, this.grantType);

        Response response;
        String oAuthAppContext = properties.get(IdPClientConstants.APP_NAME);

        //Checking if these are the frontend-if not use sp
        if (!this.oAuthAppInfoMap.keySet().contains(oAuthAppContext)) {
            oAuthAppContext = ExternalIdPClientConstants.DEFAULT_SP_APP_CONTEXT;
        }

        String username = properties.get(IdPClientConstants.USERNAME);

        if (IdPClientConstants.AUTHORIZATION_CODE_GRANT_TYPE.equals(grantType)) {
            String callbackUrl = properties.get(IdPClientConstants.CALLBACK_URL);
            returnProperties.put(IdPClientConstants.LOGIN_STATUS, IdPClientConstants.LoginStatus.LOGIN_REDIRECTION);
            returnProperties.put(IdPClientConstants.CLIENT_ID, this.oAuthAppInfoMap.get(oAuthAppContext).getClientId());
            returnProperties.put(IdPClientConstants.REDIRECTION_URL, this.authorizeEndpoint);
            returnProperties.put(IdPClientConstants.CALLBACK_URL, this.baseUrl +
                    ExternalIdPClientConstants.CALLBACK_URL + callbackUrl);
            return returnProperties;
        } else if (IdPClientConstants.PASSWORD_GRANT_TYPE.equals(grantType)) {
            response = oAuth2ServiceStubs.getTokenServiceStub().generatePasswordGrantAccessToken(
                    username, properties.get(IdPClientConstants.PASSWORD),
                    null, this.oAuthAppInfoMap.get(oAuthAppContext).getClientId(),
                    this.oAuthAppInfoMap.get(oAuthAppContext).getClientSecret());
        } else {
            response = oAuth2ServiceStubs.getTokenServiceStub().generateRefreshGrantAccessToken(
                    properties.get(IdPClientConstants.REFRESH_TOKEN), null,
                    this.oAuthAppInfoMap.get(oAuthAppContext).getClientId(),
                    this.oAuthAppInfoMap.get(oAuthAppContext).getClientSecret());
        }

        if (response == null) {
            String error = "Error occurred while generating an access token for grant type '" +
                    removeCRLFCharacters(grantType) + "'. Response is null.";
            LOG.error(error);
            throw new IdPClientException(error);
        }
        if (response.status() == 200) {   //200 - Success
            if (LOG.isDebugEnabled()) {
                LOG.debug("A new access token is successfully generated.");
            }
            try {
                OAuth2TokenInfo oAuth2TokenInfo = (OAuth2TokenInfo) new GsonDecoder().decode(response,
                        OAuth2TokenInfo.class);
                returnProperties.put(IdPClientConstants.LOGIN_STATUS, IdPClientConstants.LoginStatus.LOGIN_SUCCESS);
                returnProperties.put(IdPClientConstants.USERNAME, username);
                returnProperties.put(IdPClientConstants.ACCESS_TOKEN, oAuth2TokenInfo.getAccessToken());
                returnProperties.put(IdPClientConstants.REFRESH_TOKEN, oAuth2TokenInfo.getRefreshToken());
                returnProperties.put(IdPClientConstants.VALIDITY_PERIOD,
                        Long.toString(oAuth2TokenInfo.getExpiresIn()));
                tokenCache.put(oAuth2TokenInfo.getAccessToken(),
                        new ExternalSession(username, oAuth2TokenInfo.getAccessToken()));
                return returnProperties;
            } catch (IOException e) {
                String error = "Error occurred while parsing token response for user. Response: '" +
                        response.body().toString() + "'.";
                LOG.error(error, e);
                throw new IdPClientException(error, e);
            }
        } else if (response.status() == 401) {
            String invalidResponse = "Unable to get access token for the request with grant type : '" + grantType +
                    "', for the user '" + username + "'.";
            LOG.error(invalidResponse);
            returnProperties.put(IdPClientConstants.LOGIN_STATUS, IdPClientConstants.LoginStatus.LOGIN_FAILURE);
            returnProperties.put(IdPClientConstants.ERROR, IdPClientConstants.Error.INVALID_CREDENTIALS);
            returnProperties.put(IdPClientConstants.ERROR_DESCRIPTION, invalidResponse);
            return returnProperties;
        } else {  //Error case
            String errorMessage = "Token generation request failed. HTTP error code: " + response.status() +
                    " Error Response: " + getErrorMessage(response);
            LOG.error(errorMessage);
            throw new IdPClientException(errorMessage);
        }
    }

    public Map<String, String> authCodeLogin(String appContext, String code) throws IdPClientException {
        Map<String, String> returnProperties = new HashMap<>();
        String oAuthAppContext = appContext.split("/\\|?")[0];
        if (!this.oAuthAppInfoMap.keySet().contains(oAuthAppContext)) {
            oAuthAppContext = ExternalIdPClientConstants.DEFAULT_SP_APP_CONTEXT;
        }
        OAuthApplicationInfo oAuthApplicationInfo = this.oAuthAppInfoMap.get(oAuthAppContext);
        Response response = oAuth2ServiceStubs.getTokenServiceStub().generateAuthCodeGrantAccessToken(code,
                this.baseUrl + ExternalIdPClientConstants.CALLBACK_URL + oAuthAppContext, null,
                oAuthApplicationInfo.getClientId(), oAuthApplicationInfo.getClientSecret());
        if (response == null) {
            String error = "Error occurred while generating an access token from code '" + code + "'. " +
                    "Response is null.";
            LOG.error(error);
            throw new IdPClientException(error);
        }
        if (response.status() == 200) {   //200 - Success
            if (LOG.isDebugEnabled()) {
                LOG.debug("A new access token from code is successfully generated for the code '" + code + "'.");
            }
            try {
                OAuth2TokenInfo oAuth2TokenInfo = (OAuth2TokenInfo) new GsonDecoder().decode(response,
                        OAuth2TokenInfo.class);
                returnProperties.put(IdPClientConstants.LOGIN_STATUS, IdPClientConstants.LoginStatus.LOGIN_SUCCESS);
                returnProperties.put(IdPClientConstants.ACCESS_TOKEN, oAuth2TokenInfo.getAccessToken());
                returnProperties.put(IdPClientConstants.REFRESH_TOKEN, oAuth2TokenInfo.getRefreshToken());
                returnProperties.put(IdPClientConstants.VALIDITY_PERIOD,
                        Long.toString(oAuth2TokenInfo.getExpiresIn()));
                returnProperties.put(ExternalIdPClientConstants.REDIRECT_URL, this.baseUrl + appContext);

                Response introspectTokenResponse = oAuth2ServiceStubs.getIntrospectionServiceStub()
                        .introspectToken(oAuth2TokenInfo.getAccessToken());
                String authUser = null;
                if (introspectTokenResponse.status() == 200) {   //200 - Success
                    OAuth2IntrospectionResponse introspectResponse = (OAuth2IntrospectionResponse) new GsonDecoder()
                            .decode(introspectTokenResponse, OAuth2IntrospectionResponse.class);
                    String username = introspectResponse.getUsername();
                    authUser = username.substring(0, username.indexOf("@carbon.super"));
                    returnProperties.put(IdPClientConstants.USERNAME, authUser);
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Unable to get the username from introspection of the token '" +
                                oAuth2TokenInfo.getAccessToken() + "'. Response : '" +
                                introspectTokenResponse.toString());
                    }
                }
                tokenCache.put(oAuth2TokenInfo.getAccessToken(),
                        new ExternalSession(authUser, oAuth2TokenInfo.getAccessToken()));
                return returnProperties;
            } catch (IOException e) {
                String error = "Error occurred while parsing token response. Response : '" +
                        response.body().toString() + "'";
                LOG.error(error, e);
                throw new IdPClientException(error, e);
            }
        } else if (response.status() == 401) {
            String invalidResponse = "Unauthorized user for accessing token form code '" + code + "'. for the app " +
                    "context, '" + appContext + "'";
            returnProperties.put(IdPClientConstants.LOGIN_STATUS, IdPClientConstants.LoginStatus.LOGIN_FAILURE);
            returnProperties.put(IdPClientConstants.ERROR, IdPClientConstants.Error.INVALID_CREDENTIALS);
            returnProperties.put(IdPClientConstants.ERROR_DESCRIPTION, invalidResponse);
            return returnProperties;
        } else {  //Error case
            String error = "Token generation request failed. HTTP error code: " + response.status() +
                    " Error Response Body: " + getErrorMessage(response);
            LOG.error(error);
            throw new IdPClientException(error);
        }
    }

    @Override
    public void logout(Map<String, String> properties) throws IdPClientException {
        String token = properties.get(IdPClientConstants.ACCESS_TOKEN);
        String oAuthAppContext = properties.getOrDefault(IdPClientConstants.APP_NAME,
                ExternalIdPClientConstants.DEFAULT_SP_APP_CONTEXT);
        if (!this.oAuthAppInfoMap.keySet().contains(oAuthAppContext)) {
            oAuthAppContext = ExternalIdPClientConstants.DEFAULT_SP_APP_CONTEXT;
        }
        tokenCache.invalidate(token);
        oAuth2ServiceStubs.getRevokeServiceStub().revokeAccessToken(
                token,
                this.oAuthAppInfoMap.get(oAuthAppContext).getClientId(),
                this.oAuthAppInfoMap.get(oAuthAppContext).getClientSecret());
    }

    @Override
    public String authenticate(String token) throws AuthenticationException, IdPClientException {
        ExternalSession ifPresent = tokenCache.getIfPresent(token);
        if (ifPresent != null) {
            return ifPresent.getUserName();
        }
        Response response = oAuth2ServiceStubs.getIntrospectionServiceStub().introspectToken(token);

        if (response == null) {
            String error = "Error occurred while authenticating token '" + token + "'. Response is null.";
            LOG.error(error);
            throw new IdPClientException(error);
        }
        try {
            if (response.status() == 200) {  //200 - OK
                OAuth2IntrospectionResponse introspectResponse = (OAuth2IntrospectionResponse) new GsonDecoder()
                        .decode(response, OAuth2IntrospectionResponse.class);
                if (introspectResponse.isActive()) {
                    return introspectResponse.getUsername();
                } else {
                    throw new AuthenticationException("The token is not active");
                }
            } else if (response.status() == 400) {  //400 - Known Error
                try {
                    DCRError error = (DCRError) new GsonDecoder().decode(response, DCRError.class);
                    throw new IdPClientException("Error occurred while introspecting the token. Error: " +
                            error.getError() + ". Error Description: " + error.getErrorDescription() +
                            ". Status Code: " + response.status());
                } catch (IOException e) {
                    throw new IdPClientException("Error occurred while parsing the Introspection error message.", e);
                }
            } else {  //Unknown Error
                throw new IdPClientException("Error occurred while authenticating. Error: " +
                        response.body().toString() + " Status Code: " + response.status());
            }
        } catch (IOException e) {
            throw new IdPClientException("Error occurred while parsing the authentication response.", e);
        }
    }

    private void registerApplication(String appContext, String clientName, String kmUserName)
            throws IdPClientException {
        List<String> grantTypes = new ArrayList<>();
        grantTypes.add(IdPClientConstants.PASSWORD_GRANT_TYPE);
        grantTypes.add(IdPClientConstants.AUTHORIZATION_CODE_GRANT_TYPE);
        grantTypes.add(IdPClientConstants.REFRESH_GRANT_TYPE);

        String callBackUrl;
        if (clientName.equals(ExternalIdPClientConstants.DEFAULT_SP_APP_CONTEXT)) {
            callBackUrl = ExternalIdPClientConstants.REGEX_BASE_START + this.baseUrl +
                    ExternalIdPClientConstants.CALLBACK_URL + ExternalIdPClientConstants.FORWARD_SLASH +
                    ExternalIdPClientConstants.REGEX_BASE_END;
        } else {
            callBackUrl = ExternalIdPClientConstants.REGEX_BASE_START + this.baseUrl +
                    ExternalIdPClientConstants.CALLBACK_URL + ExternalIdPClientConstants.FORWARD_SLASH
                    + appContext + ExternalIdPClientConstants.REGEX_BASE_END;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating OAuth2 application of name '" + clientName + "'.");
        }
        DCRClientInfo dcrClientInfo = new DCRClientInfo();
        dcrClientInfo.setClientName(clientName);
        dcrClientInfo.setGrantTypes(grantTypes);
        dcrClientInfo.addCallbackUrl(callBackUrl);
        dcrClientInfo.setUserinfoSignedResponseAlg(signingAlgo);
        dcrClientInfo.setExtParamOwner(kmUserName);

        Response response = dcrmServiceStub.registerApplication(dcrClientInfo);
        if (response == null) {
            String error = "Error occurred while DCR application '" + dcrClientInfo + "' creation. " +
                    "Response is null.";
            LOG.error(error);
            throw new IdPClientException(error);
        }
        if (response.status() == 201) {  //201 - Created
            try {
                DCRClientInfo dcrClientInfoResponse = (DCRClientInfo) new GsonDecoder()
                        .decode(response, DCRClientInfo.class);
                OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo(
                        clientName, dcrClientInfoResponse.getClientId(),
                        dcrClientInfoResponse.getClientSecret()
                );
                this.oAuthAppInfoMap.replace(appContext, oAuthApplicationInfo);
                this.oAuthAppDAO.addOAuthApp(oAuthApplicationInfo);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("OAuth2 application created: " + oAuthApplicationInfo.toString());
                }
            } catch (IOException e) {
                String error = "Error occurred while parsing the DCR application creation response " +
                        "message. Response: '" + response.body().toString() + "'.";
                LOG.error(error, e);
                throw new IdPClientException(error, e);
            }
        } else if (response.status() == 400) {  //400 - Known Error
            try {
                DCRError error = (DCRError) new GsonDecoder().decode(response, DCRError.class);
                String errorMessage = "Error occurred while DCR application creation. Error: " +
                        error.getError() + ". Error Description: " + error.getErrorDescription() +
                        ". Status Code: " + response.status();
                LOG.error(errorMessage);
                throw new IdPClientException(errorMessage);
            } catch (IOException e) {
                String error = "Error occurred while parsing the DCR error message. Error: " +
                        "'" + getErrorMessage(response) + "'.";
                LOG.error(error, e);
                throw new IdPClientException(error, e);
            }
        } else {  //Unknown Error
            String error = "Error occurred while DCR application creation. Error: " +
                    response.body().toString() + " Status Code: '" + response.status() + "'.";
            LOG.error(error);
            throw new IdPClientException(error);
        }
    }

    private String getErrorMessage(Response response) {
        StringBuilder errorMessage = new StringBuilder(ExternalIdPClientConstants.EMPTY_STRING);
        if (response != null && response.body() != null) {
            try {
                String errorDescription = new Gson().fromJson(response.body().toString(), JsonElement.class)
                        .getAsJsonObject().getAsString();
                errorMessage.append(errorDescription);
            } catch (Exception ex) {
                LOG.error("Error occurred while parsing error response. Response: '" + response.body().toString() +
                        "'", ex);
            }
        }
        return errorMessage.toString();
    }
}
