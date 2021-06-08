package org.wso2.carbon.analytics.idp.client.core.utils.config;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

/**
 * SSL configurations.
 */
@Configuration(namespace = "ssl.configs", description = "SSL Configuration Parameters")
public class SSLConfiguration {

    @Element(description = "Keystore Password", required = true)
    private String keyStorePassword = null;

    @Element(description = "Keystore Location", required = true)
    private String keyStoreLocation = null;

    @Element(description = "Truststore Password")
    private String trustStorePassword = null;

    @Element(description = "Truststore Location")
    private String trustStoreLocation = null;

    public String getKeyStorePassword() {

        return keyStorePassword;
    }

    public String getTrustStorePassword() {

        return trustStorePassword;
    }

    public String getTrustStoreLocation() {

        return trustStoreLocation;
    }

    public String getKeyStoreLocation() {

        return keyStoreLocation;
    }
}
