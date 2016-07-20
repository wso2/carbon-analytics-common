package org.wso2.carbon.databridge.commons;

public class Credentials {
    private final String username;
    private final String password;
    private final String domainName;
    private int tenantId;
    private boolean isServerAuthEnabled;

    public Credentials(String username, String password, String domainName) {
        this.username = username;
        this.password = password;
        this.domainName = domainName;
    }

    public Credentials(String username, String password, String domainName, int tenantId, boolean isServerAuthEnabled) {
        this.username = username;
        this.password = password;
        this.domainName = domainName;
        this.tenantId = tenantId;
        this.isServerAuthEnabled = isServerAuthEnabled;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDomainName() {
        return domainName;
    }

    public int getTenantId() {
        return tenantId;
    }

    public boolean isServerAuthEnabled() {
        return isServerAuthEnabled;
    }

    public void setServerAuthEnabled(boolean serverAuthEnabled) {
        isServerAuthEnabled = serverAuthEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Credentials)) {
            return false;
        }

        Credentials that = (Credentials) o;

        if (!password.equals(that.password)) {
            return false;
        }
        if (!username.equals(that.username)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + password.hashCode();
        return result;
    }
}
