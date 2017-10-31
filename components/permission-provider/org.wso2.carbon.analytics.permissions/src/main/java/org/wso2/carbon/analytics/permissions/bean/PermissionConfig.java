package org.wso2.carbon.analytics.permissions.bean;

import org.wso2.carbon.analytics.permissions.internal.impl.DefaultPermissionProvider;
import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

/**
 * Configuration bean class.
 */
@Configuration(namespace = "wso2.analytics.permissions", description = "WSO2 Analytics Permission Provider")
public class PermissionConfig {

    @Element(description = "Permission provider implementation class")
    private String permissionProvider = DefaultPermissionProvider.class.getName();

    @Element(description = "Permission provider datasource name")
    private String datasourceName = "WSO2_PERMISSIONS_DB";

    /**
     * Get permission provider.
     *
     * @return
     */
    public String getPermissionProvider() {
        return permissionProvider;
    }

    /**
     * Set permission provider.
     *
     * @param permissionProvider
     */
    public void setPermissionProvider(String permissionProvider) {
        this.permissionProvider = permissionProvider;
    }

    /**
     * Get datasource name.
     *
     * @return
     */
    public String getDatasourceName() {
        return datasourceName;
    }

    /**
     * Set datasource name.
     *
     * @param datasourceName
     */
    public void setDatasourceName(String datasourceName) {
        this.datasourceName = datasourceName;
    }
}
