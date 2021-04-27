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

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.idp.client.core.api.AnalyticsHttpClientBuilderService;
import org.wso2.carbon.analytics.idp.client.core.api.IdPClient;
import org.wso2.carbon.analytics.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.analytics.idp.client.core.spi.IdPClientFactory;
import org.wso2.carbon.analytics.idp.client.core.utils.IdPClientConstants;
import org.wso2.carbon.analytics.idp.client.core.utils.config.IdPClientConfiguration;
import org.wso2.carbon.analytics.idp.client.external.dao.OAuthAppDAO;
import org.wso2.carbon.analytics.idp.client.external.impl.DCRMServiceStub;
import org.wso2.carbon.analytics.idp.client.external.impl.OAuth2ServiceStubs;
import org.wso2.carbon.analytics.idp.client.external.impl.SCIM2ServiceStub;
import org.wso2.carbon.analytics.idp.client.external.models.OAuthApplicationInfo;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.secvault.SecretRepository;

import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.analytics.idp.client.external.ExternalIdPClientConstants.DEFAULT_SCOPE;
import static org.wso2.carbon.analytics.idp.client.external.ExternalIdPClientConstants.SCOPE;

/**
 * Factory for External IdPClient.
 */
@Component(
        name = "org.wso2.carbon.analytics.idp.client.external.ExternalIdPClientFactory",
        immediate = true
)
public class ExternalIdPClientFactory implements IdPClientFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ExternalIdPClientFactory.class);
    private DataSourceService dataSourceService;
    private SecretRepository secretRepository;
    private AnalyticsHttpClientBuilderService analyticsHttpClientBuilderService;

    @Activate
    protected void activate(BundleContext bundleContext) {
        LOG.debug("External IDP client factory activated.");
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        LOG.debug("External IDP client factory deactivated.");
    }

    /**
     * Register datasource service.
     *
     * @param dataSourceService
     */
    @Reference(
            name = "org.wso2.carbon.datasource.DataSourceService",
            service = DataSourceService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterDataSourceService"
    )
    protected void registerDataSourceService(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }

    /**
     * Unregister datasource service.
     *
     * @param dataSourceService datasource service
     */
    protected void unregisterDataSourceService(DataSourceService dataSourceService) {
        this.dataSourceService = null;
    }

    /**
     * Register secret repository.
     *
     * @param secretRepository
     */
    @Reference(
            name = "org.wso2.carbon.secvault.repository.DefaultSecretRepository",
            service = SecretRepository.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterSecretRepository"
    )
    protected void registerSecretRepository(SecretRepository secretRepository) {
        this.secretRepository = secretRepository;
    }

    /**
     * Unregister secret repository.
     *
     * @param secretRepository
     */
    protected void unregisterSecretRepository(SecretRepository secretRepository) {
        this.secretRepository = null;
    }

    @Reference(
            service = AnalyticsHttpClientBuilderService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterAnalyticsHttpClient"
    )
    protected void registerAnalyticsHttpClient(AnalyticsHttpClientBuilderService service) {
        this.analyticsHttpClientBuilderService = service;
        LOG.debug("@Reference(bind) AnalyticsHttpClientBuilderService at '{}'",
                AnalyticsHttpClientBuilderService.class.getName());
    }

    protected void unregisterAnalyticsHttpClient(AnalyticsHttpClientBuilderService service) {
        LOG.debug("@Reference(unbind) AnalyticsHttpClientBuilderService at '{}'",
                AnalyticsHttpClientBuilderService.class.getName());
        this.analyticsHttpClientBuilderService = null;
    }

    @Override
    public String getType() {
        return ExternalIdPClientConstants.EXTERNAL_IDP_CLIENT_TYPE;
    }

    @Override
    public IdPClient getIdPClient(IdPClientConfiguration idPClientConfiguration)
            throws IdPClientException {
        Map<String, String> properties = idPClientConfiguration.getProperties();
        String dcrEndpoint = properties.getOrDefault(ExternalIdPClientConstants.KM_DCR_URL,
                ExternalIdPClientConstants.DEFAULT_KM_DCR_URL);
        String kmUsername = properties.getOrDefault(ExternalIdPClientConstants.KM_USERNAME,
                ExternalIdPClientConstants.DEFAULT_KM_USERNAME);
        String kmPassword = properties.getOrDefault(ExternalIdPClientConstants.KM_PASSWORD,
                ExternalIdPClientConstants.DEFAULT_KM_PASSWORD);
        String kmTokenUrl = properties.getOrDefault(ExternalIdPClientConstants.KM_TOKEN_URL,
                ExternalIdPClientConstants.DEFAULT_KM_TOKEN_URL);
        String dcrAppOwner = properties.getOrDefault(ExternalIdPClientConstants.DCR_APP_OWNER, kmUsername);
        String introspectUrl = properties.getOrDefault(ExternalIdPClientConstants.INTROSPECTION_URL,
                null);

        String idPBaseUrl = properties.getOrDefault(ExternalIdPClientConstants.IDP_BASE_URL,
                ExternalIdPClientConstants.DEFAULT_IDP_BASE_URL);
        String idPUserName = properties.getOrDefault(ExternalIdPClientConstants.IDP_USERNAME,
                ExternalIdPClientConstants.DEFAULT_IDP_USERNAME);
        String idPPassword = properties.getOrDefault(ExternalIdPClientConstants.IDP_PASSWORD,
                ExternalIdPClientConstants.DEFAULT_IDP_PASSWORD);

        String baseUrl = properties.getOrDefault(ExternalIdPClientConstants.BASE_URL,
                ExternalIdPClientConstants.DEFAULT_BASE_URL);
        String grantType = properties.getOrDefault(ExternalIdPClientConstants.GRANT_TYPE,
                IdPClientConstants.PASSWORD_GRANT_TYPE);
        String signingAlgo = properties.getOrDefault(ExternalIdPClientConstants.OIDC_USER_INFO_ALGO,
                ExternalIdPClientConstants.DEFAULT_OIDC_USER_INFO_ALGO);


        String portalAppContext = properties.getOrDefault(ExternalIdPClientConstants.PORTAL_APP_CONTEXT,
                ExternalIdPClientConstants.DEFAULT_PORTAL_APP_CONTEXT);
        String statusAppContext = properties.getOrDefault(ExternalIdPClientConstants.STATUS_DB_APP_CONTEXT,
                ExternalIdPClientConstants.DEFAULT_STATUS_DB_APP_CONTEXT);
        String businessAppContext = properties.getOrDefault(ExternalIdPClientConstants.BR_DB_APP_CONTEXT,
                ExternalIdPClientConstants.DEFAULT_BR_DB_APP_CONTEXT);

        String defaultUserStore = properties.getOrDefault(ExternalIdPClientConstants.USER_STORE,
                ExternalIdPClientConstants.DEFAULT_USER_STORE);
        String scope = properties.getOrDefault(SCOPE, DEFAULT_SCOPE);

        OAuthApplicationInfo spOAuthApp = new OAuthApplicationInfo(
                ExternalIdPClientConstants.SP_APP_NAME,
                properties.get(ExternalIdPClientConstants.SP_CLIENT_ID),
                properties.get(ExternalIdPClientConstants.SP_CLIENT_SECRET));
        OAuthApplicationInfo portalOAuthApp = new OAuthApplicationInfo(
                ExternalIdPClientConstants.PORTAL_APP_NAME,
                properties.get(ExternalIdPClientConstants.PORTAL_CLIENT_ID),
                properties.get(ExternalIdPClientConstants.PORTAL_CLIENT_SECRET));
        OAuthApplicationInfo statusOAuthApp = new OAuthApplicationInfo(
                ExternalIdPClientConstants.STATUS_DB_APP_NAME,
                properties.get(ExternalIdPClientConstants.STATUS_DB_CLIENT_ID),
                properties.get(ExternalIdPClientConstants.STATUS_DB_CLIENT_SECRET));
        OAuthApplicationInfo businessOAuthApp = new OAuthApplicationInfo(
                ExternalIdPClientConstants.BR_DB_APP_NAME,
                properties.get(ExternalIdPClientConstants.BR_DB_CLIENT_ID),
                properties.get(ExternalIdPClientConstants.BR_DB_CLIENT_SECRET));

        Map<String, OAuthApplicationInfo> oAuthAppInfoMap = new HashMap<>();
        oAuthAppInfoMap.put(ExternalIdPClientConstants.DEFAULT_SP_APP_CONTEXT, spOAuthApp);
        oAuthAppInfoMap.put(portalAppContext, portalOAuthApp);
        oAuthAppInfoMap.put(statusAppContext, statusOAuthApp);
        oAuthAppInfoMap.put(businessAppContext, businessOAuthApp);

        int cacheTimeout, connectionTimeout, readTimeout;
        try {
            cacheTimeout = Integer.parseInt(properties.getOrDefault(ExternalIdPClientConstants.CACHE_TIMEOUT,
                    ExternalIdPClientConstants.DEFAULT_CACHE_TIMEOUT));
            connectionTimeout = Integer.parseInt(properties.getOrDefault(ExternalIdPClientConstants.CONNECTION_TIMEOUT,
                    ExternalIdPClientConstants.DEFAULT_CONNECTION_TIMEOUT));
            readTimeout = Integer.parseInt(properties.getOrDefault(ExternalIdPClientConstants.READ_TIMEOUT,
                    ExternalIdPClientConstants.DEFAULT_READ_TIMEOUT));
        } catch (NumberFormatException e) {
            throw new IdPClientException("Cache timeout overriding property '" +
                    properties.get(ExternalIdPClientConstants.CACHE_TIMEOUT) + "' is invalid.");
        }

        String databaseName = properties.getOrDefault(ExternalIdPClientConstants.DATABASE_NAME,
                ExternalIdPClientConstants.DEFAULT_DATABASE_NAME);
        OAuthAppDAO oAuthAppDAO = new OAuthAppDAO(this.dataSourceService, databaseName,
                idPClientConfiguration.getQueries(), this.secretRepository);

        DCRMServiceStub dcrmServiceStub = this.analyticsHttpClientBuilderService
                .build(kmUsername, kmPassword, connectionTimeout, readTimeout, DCRMServiceStub.class, dcrEndpoint);
        OAuth2ServiceStubs keyManagerServiceStubs = new OAuth2ServiceStubs(
                this.analyticsHttpClientBuilderService, kmTokenUrl + ExternalIdPClientConstants.TOKEN_POSTFIX,
                kmTokenUrl + ExternalIdPClientConstants.REVOKE_POSTFIX, introspectUrl,
                kmUsername, kmPassword, connectionTimeout, readTimeout);
        SCIM2ServiceStub scimServiceStub = this.analyticsHttpClientBuilderService
                .build(idPUserName, idPPassword, connectionTimeout, readTimeout, SCIM2ServiceStub.class, idPBaseUrl);

        String jwksUrl = properties.getOrDefault(ExternalIdPClientConstants.JWKS_URL,
                ExternalIdPClientConstants.DEFULT_JWKS_URL);
        String adminRoleDisplayName = idPClientConfiguration.getUserManager().getAdminRole();

        String targetURIForRedirection = properties.getOrDefault(ExternalIdPClientConstants.EXTERNAL_SSO_LOGOUT_URL,
                ExternalIdPClientConstants.DEFAULT_EXTERNAL_SSO_LOGOUT_URL);

        ExternalIdPClient externalIdPClient = new ExternalIdPClient(baseUrl,
                kmTokenUrl + ExternalIdPClientConstants.AUTHORIZE_POSTFIX, grantType, signingAlgo,
                adminRoleDisplayName, oAuthAppInfoMap, cacheTimeout, oAuthAppDAO, dcrmServiceStub,
                keyManagerServiceStubs, scimServiceStub, defaultUserStore, idPClientConfiguration.isSsoEnabled(),
                targetURIForRedirection, scope, jwksUrl);
        externalIdPClient.init(dcrAppOwner);
        return externalIdPClient;
    }

}
