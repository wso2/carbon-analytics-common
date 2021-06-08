package org.wso2.carbon.analytics.idp.client.core.utils;

import org.wso2.carbon.analytics.idp.client.core.utils.config.SSLConfiguration;
import org.wso2.carbon.utils.StringUtils;

/**
 * SSL Configs.
 */
public class SSLConfig {

    private boolean sslConfigsExistInConfigProvider = false;
    private final String keyStorePassword;
    private final String trustStorePassword;
    private final String keyStoreLocation;
    private final String trustStoreLocation;

    public SSLConfig(SSLConfiguration sslConfiguration) {

        this.keyStorePassword = sslConfiguration.getKeyStorePassword();
        this.trustStorePassword = sslConfiguration.getTrustStorePassword();
        this.keyStoreLocation = sslConfiguration.getKeyStoreLocation();
        this.trustStoreLocation = sslConfiguration.getTrustStoreLocation();
        if (!StringUtils.isNullOrEmptyAfterTrim(keyStorePassword)
                && !StringUtils.isNullOrEmptyAfterTrim(keyStoreLocation)
                && !StringUtils.isNullOrEmptyAfterTrim(trustStorePassword)
                && !StringUtils.isNullOrEmptyAfterTrim(trustStoreLocation)) {
            sslConfigsExistInConfigProvider = true;
        }
    }

    public boolean isSSLConfigsExistInConfigProvider() {

        return sslConfigsExistInConfigProvider;
    }

    public void exportSSLConfigsExistInConfigProvider() {

        System.setProperty(IdPClientConstants.SSL.KEY_STORE_PASSWORD, this.keyStorePassword);
        System.setProperty(IdPClientConstants.SSL.TRUST_STORE_PASSWORD, this.trustStorePassword);
        System.setProperty(IdPClientConstants.SSL.KEY_STORE, this.keyStoreLocation);
        System.setProperty(IdPClientConstants.SSL.TRUST_STORE, this.trustStoreLocation);
    }
}
