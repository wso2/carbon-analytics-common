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
package org.wso2.carbon.analytics.idp.client.local;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.idp.client.core.api.IdPClient;
import org.wso2.carbon.analytics.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.analytics.idp.client.core.models.Role;
import org.wso2.carbon.analytics.idp.client.core.spi.IdPClientFactory;
import org.wso2.carbon.analytics.idp.client.core.utils.config.IdPClientConfiguration;
import org.wso2.carbon.analytics.idp.client.core.utils.config.UserChildElement;
import org.wso2.carbon.analytics.idp.client.local.models.LocalUser;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Local IdP Client Factory.
 */
@Component(
        name = "org.wso2.carbon.analytics.idp.client.local.LocalIdPClientFactory",
        immediate = true
)
public class LocalIdPClientFactory implements IdPClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(LocalIdPClientFactory.class);

    @Activate
    protected void activate(BundleContext bundleContext) {
        if (LOG.isDebugEnabled()) {
            LOG.info("Local User Store Factory registered...");
        }
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
    }

    @Override
    public String getType() {
        return LocalIdPClientConstants.LOCAL_IDP_CLIENT_TYPE;
    }

    @Override
    public IdPClient getIdPClient(IdPClientConfiguration idPClientConfiguration) throws IdPClientException {
        int sessionTimeout;
        Map<String, String> properties = idPClientConfiguration.getProperties();
        try {
            sessionTimeout = Integer.parseInt(properties.getOrDefault(LocalIdPClientConstants.SESSION_TIME_OUT,
                    LocalIdPClientConstants.DEFAULT_SESSION_TIMEOUT));
        } catch (NumberFormatException e) {
            throw new IdPClientException("Session timeout overridden property '" +
                    properties.get(LocalIdPClientConstants.SESSION_TIME_OUT) + "' is invalid.");
        }

        List<Role> roles = idPClientConfiguration.getUserManager().getUserStore().getRoles().stream()
                .map(roleElement -> new Role(roleElement.getRole().getId(), roleElement.getRole().getDisplay())
        ).collect(Collectors.toList());

        String adminRoleDisplayName = idPClientConfiguration.getUserManager().getAdminRole();
        Role adminRole = roles.stream().filter(role -> role.getDisplayName().equalsIgnoreCase(adminRoleDisplayName))
                .findFirst()
                .orElseThrow(() -> new IdPClientException("Admin role '" + adminRoleDisplayName + "' is not available" +
                        " in the User Store."));

        List<LocalUser> users = idPClientConfiguration.getUserManager().getUserStore().getUsers().stream()
                .map(userElement -> {
                    UserChildElement user = userElement.getUser();
                    List<String> roleIdList = Arrays.asList(user.getRoles().replaceAll("\\s*", "").split(","));
                    List<Role> userRolesFromId = roles.stream()
                            .filter((role) -> roleIdList.contains(role.getId()))
                            .collect(Collectors.toList());
                    return new LocalUser(
                            user.getUsername(), user.getPassword().toCharArray(), user.getProperties(), userRolesFromId);
        }).collect(Collectors.toList());

        return new LocalIdPClient(sessionTimeout, users, roles, adminRole);
    }

}

