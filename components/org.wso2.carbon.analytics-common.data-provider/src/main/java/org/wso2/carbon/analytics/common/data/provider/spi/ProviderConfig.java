package org.wso2.carbon.analytics.common.data.provider.spi;

/**
 * Created by sajithd on 11/20/17.
 */
public interface ProviderConfig {
    long getPublishingInterval();
    long getPurgingInterval();
    boolean isPurgingEnable();
}
