package org.wso2.carbon.analytics.common.data.provider.exception;

/**
 * Created by sajithd on 11/21/17.
 */
public class DataProviderException extends Exception {
    private String errorMessage;

    public DataProviderException(String message) {
        super(message);
        errorMessage = message;
    }

    public DataProviderException(String message, Throwable cause) {
        super(message, cause);
        errorMessage = message;
    }

    public DataProviderException(Throwable cause) {
        super(cause);
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
