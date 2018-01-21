package org.wso2.carbon.event.input.adapter.sqs.internal;

import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;

/**
 * Class to keep configuration related properties for SQS Event Adapter
 **/
public class SQSConfig {
    private String accessKey;
    private String secretKey;
    private String queueURL;
    private Integer waitTime;
    private Integer maxNumberOfMessages;
    private String signingRegion;
    private Integer visibilityTimeout;
    private boolean shouldDeleteAfterConsuming;
    private int retryCountLimit;
    private int retryInterval;

    public SQSConfig(String accessKey, String secretKey, String queueURL,
                     Integer waitTime, Integer maxNumberOfMessages, String signingRegion, Integer visibilityTimeout,
                     boolean shouldDeleteAfterConsuming, int retryCountLimit, int retryInterval)
            throws InputEventAdapterException {

        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.queueURL = queueURL;

        if (waitTime != null && !(waitTime >= 0 && waitTime <= 20)) {
            throw new InputEventAdapterException("Wait time should be between 0 and 20 seconds. But provided " +
                    waitTime + "seconds.");
        }
        this.waitTime = waitTime;

        if (!(maxNumberOfMessages >= 0 && maxNumberOfMessages <= 10)) {
            throw new InputEventAdapterException("Max number of messages should be between 0 and 10. But provided " +
                    maxNumberOfMessages + ".");
        }
        this.maxNumberOfMessages = maxNumberOfMessages;

        this.signingRegion = signingRegion;

        if (visibilityTimeout != null && !(visibilityTimeout >= 0 && visibilityTimeout <= 43200)) {
            throw new InputEventAdapterException("Visibility timeout should be between 0 and 43200 seconds. But " +
                    "provided " + visibilityTimeout + "seconds.");
        }
        this.visibilityTimeout = visibilityTimeout;

        this.shouldDeleteAfterConsuming = shouldDeleteAfterConsuming;
        this.retryCountLimit = retryCountLimit;
        this.retryInterval = retryInterval;
    }

    String getAccessKey() {
        return accessKey;
    }

    String getSecretKey() {
        return secretKey;
    }

    String getQueueURL() {
        return queueURL;
    }

    Integer getWaitTime() {
        return waitTime;
    }

    int getMaxNumberOfMessages() {
        return maxNumberOfMessages;
    }


    String getSigningRegion() {
        return signingRegion;
    }

    Integer getVisibilityTimeout() {
        return visibilityTimeout;
    }

    boolean shouldDeleteAfterConsuming() {
        return shouldDeleteAfterConsuming;
    }

    int getRetryCountLimit() {
        return retryCountLimit;
    }

    int getRetryInterval() {
        return retryInterval;
    }
}
