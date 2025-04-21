/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

import org.wso2.carbon.event.output.adapter.core.internal.ds.OutputEventAdapterServiceValueHolder;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.ResolvedSecret;

/**
 * Secret processor for event adapters.
 */
public class EventAdapterSecretProcessor {

    private static final String SECRET_PROPERTIES = "_SECRET_PROPERTIES";

    /**
     * Decrypt secret property.
     *
     * @param notificationSender    Notification Sender: EMAIL_PROVIDER.
     * @param authType              Authentication Type: BASIC, CLIENT_CREDENTIAL.
     * @param property              Authentication Property: userName, password, clientId, clientSecret.
     * @return Decrypted secret value.
     * @throws SecretManagementException If an error occurs while decrypting the secret.
     */
    public static String decryptCredential(String notificationSender, String authType, String property)
            throws SecretManagementException {

        String secretName = buildSecretName(notificationSender, authType, property);
        String secretType = notificationSender + SECRET_PROPERTIES;
        if (!isSecretPropertyExists(secretType, secretName)) {
            throw new SecretManagementException(String.format("Unable to find the Secret Property: %s of " +
                    "Auth Type: %s and Action ID: %s from the system.", property, authType, notificationSender));
        }
        ResolvedSecret resolvedSecret = OutputEventAdapterServiceValueHolder.getSecretResolveManager()
                .getResolvedSecret(secretType, secretName);

        return resolvedSecret.getResolvedSecretValue();
    }

    /**
     * Create secret name.
     *
     * @param notificationSender     Notification Sender.
     * @param authType     Authentication Type.
     * @param authProperty Authentication Property.
     * @return Secret Name.
     */
    private static String buildSecretName(String notificationSender, String authType, String authProperty) {

        return notificationSender + ":" + authType + ":" + authProperty;
    }

    /**
     * Check whether the secret property exists.
     *
     * @param secretName Secret Name.
     * @return True if the secret property exists.
     * @throws SecretManagementException If an error occurs while checking the existence of the secret.
     */
    private static boolean isSecretPropertyExists(String secretType, String secretName)
            throws SecretManagementException {

        return OutputEventAdapterServiceValueHolder.getSecretManager().isSecretExist(secretType, secretName);
    }
}
