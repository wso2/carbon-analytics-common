package org.wso2.carbon.analytics.permissions;

/**
 * Manages permission providers.
 */
public interface PermissionManager {
    /**
     * Get permission provider.
     *
     * @return
     */
    PermissionProvider getProvider();
}
