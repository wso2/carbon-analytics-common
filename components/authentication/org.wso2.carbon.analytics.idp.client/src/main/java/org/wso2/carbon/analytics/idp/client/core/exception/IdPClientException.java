package org.wso2.carbon.analytics.idp.client.core.exception;

/**
 * IdP Client Exception.
 */
public class IdPClientException extends Exception {
    public IdPClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdPClientException(Throwable cause) {
        super(cause);
    }

    public IdPClientException(String message) {
        super(message);
    }
}
