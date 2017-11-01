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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.idp.client.core.api.IdPClient;
import org.wso2.carbon.analytics.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.analytics.idp.client.core.models.Role;
import org.wso2.carbon.analytics.idp.client.core.models.User;
import org.wso2.carbon.analytics.idp.client.core.spi.IdPClientFactory;
import org.wso2.carbon.analytics.idp.client.core.utils.IdPClientConstants;
import org.wso2.carbon.analytics.idp.client.external.factories.DCRMServiceStubFactory;
import org.wso2.carbon.analytics.idp.client.external.factories.OAuth2ServiceStubFactory;
import org.wso2.carbon.analytics.idp.client.external.factories.SCIM2ServiceStubFactory;
import org.wso2.carbon.analytics.idp.client.external.impl.DCRMServiceStub;
import org.wso2.carbon.analytics.idp.client.external.impl.OAuth2ServiceStubs;
import org.wso2.carbon.analytics.idp.client.external.impl.SCIM2ServiceStub;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Factory for External IdPClient.
 */
@Component(
        name = "org.wso2.carbon.analytics.idp.client.external.ExternalIdPClientFactory",
        immediate = true
)
public class ExternalIdPClientFactory implements IdPClientFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ExternalIdPClientFactory.class);

    @Activate
    protected void activate(BundleContext bundleContext) {
        if (LOG.isDebugEnabled()) {
            LOG.info("External IdP Client factory registered...");
        }
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
    }

    @Override
    public String getType() {
        return ExternalIdPClientConstants.EXTERNAL_IDP_CLIENT_TYPE;
    }

    @Override
    public IdPClient getIdPClient(Map<String, String> properties, List<User> users, List<Role> roles)
            throws IdPClientException {
        String dcrEndpoint = properties.getOrDefault(ExternalIdPClientConstants.KM_DCR_URL,
                ExternalIdPClientConstants.DEFAULT_KM_DCR_URL);
        String kmUsername = properties.getOrDefault(ExternalIdPClientConstants.KM_USERNAME,
                ExternalIdPClientConstants.DEFAULT_KM_USERNAME);
        String kmPassword = properties.getOrDefault(ExternalIdPClientConstants.KM_PASSWORD,
                ExternalIdPClientConstants.DEFAULT_KM_PASSWORD);
        String kmCertAlias = properties.getOrDefault(ExternalIdPClientConstants.KM_CERT_ALIAS,
                ExternalIdPClientConstants.DEFAULT_KM_CERT_ALIAS);
        String kmTokenUrl = properties.getOrDefault(ExternalIdPClientConstants.KM_TOKEN_URL,
                ExternalIdPClientConstants.DEFAULT_KM_TOKEN_URL);

        String idPBaseUrl = properties.getOrDefault(ExternalIdPClientConstants.IDP_BASE_URL,
                ExternalIdPClientConstants.DEFAULT_IDP_BASE_URL);
        String idPCertAlias = properties.getOrDefault(ExternalIdPClientConstants.IDP_CERT_ALIAS,
                ExternalIdPClientConstants.DEFAULT_IDP_CERT_ALIAS);
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

        String spAppName = properties.getOrDefault(ExternalIdPClientConstants.SP_APP_NAME,
                ExternalIdPClientConstants.DEFAULT_SP_APP_NAME);

        String portalAppName = properties.getOrDefault(ExternalIdPClientConstants.PORTAL_APP_NAME,
                ExternalIdPClientConstants.DEFAULT_PORTAL_APP_NAME);
        String statusAppName = properties.getOrDefault(ExternalIdPClientConstants.STATUS_DB_APP_NAME,
                ExternalIdPClientConstants.DEFAULT_STATUS_DB_APP_NAME);
        String businessAppName = properties.getOrDefault(ExternalIdPClientConstants.BR_DB_APP_NAME,
                ExternalIdPClientConstants.DEFAULT_BR_DB_APP_NAME);

        List<String> oAuthAppNames = new ArrayList<>();
        oAuthAppNames.add(spAppName);
        oAuthAppNames.add(portalAppName);
        oAuthAppNames.add(statusAppName);
        oAuthAppNames.add(businessAppName);



        DCRMServiceStub dcrmServiceStub = DCRMServiceStubFactory
                .getDCRMServiceStub(dcrEndpoint, kmUsername, kmPassword, kmCertAlias);
        OAuth2ServiceStubs keyManagerServiceStubs = OAuth2ServiceStubFactory.getKeyManagerServiceStubs(
                kmTokenUrl + ExternalIdPClientConstants.TOKEN_POSTFIX,
                kmTokenUrl + ExternalIdPClientConstants.REVOKE_POSTFIX,
                kmTokenUrl + ExternalIdPClientConstants.INTROSPECT_POSTFIX,
                kmCertAlias, kmUsername, kmPassword);
        SCIM2ServiceStub scimServiceStub = SCIM2ServiceStubFactory
                .getSCIMServiceStub(idPBaseUrl, idPUserName, idPPassword, idPCertAlias);

        return new ExternalIdPClient(baseUrl, kmTokenUrl + ExternalIdPClientConstants.AUTHORIZE_POSTFIX,
                grantType, signingAlgo, spAppName, oAuthAppNames, dcrmServiceStub, keyManagerServiceStubs,
                scimServiceStub);
    }

}
