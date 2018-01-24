/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.input.adapter.sqs.internal;

import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.wso2.carbon.event.input.adapter.sqs.internal.util.SQSEventAdapterConstants;

/**
 * Class to keep configuration related properties for SQS Event Adapter.
 */
public class SQSConfig {
    private String accessKey = null;
    private String secretKey = null;
    private String queueURL = null;
    private Integer waitTime = null;
    private String region = null;
    private Integer visibilityTimeout = null;
    private Integer maxNumberOfMessages = SQSEventAdapterConstants.DEFAULT_MAX_NUMBER_OF_MSGS;
    private boolean shouldDeleteAfterConsuming = SQSEventAdapterConstants.DEFAULT_DELETE_AFTER_CONSUMING;
    private int retryCountLimit = SQSEventAdapterConstants.DEFAULT_RETRY_ATTEMPT_LIMIT;
    private int retryInterval = SQSEventAdapterConstants.DEFAULT_RETRY_INTERVAL;

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

    String getRegion() {
        return region;
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

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setQueueURL(String queueURL) {
        this.queueURL = queueURL;
    }

    public void setWaitTime(Integer waitTime) throws InputEventAdapterException {
        if (waitTime != null && !(waitTime >= 0 && waitTime <= 20)) {
            throw new InputEventAdapterException("Wait time should be between 0 and 20 seconds. But provided " +
                    waitTime + "seconds.");
        }
        this.waitTime = waitTime;
    }

    public void setMaxNumberOfMessages(Integer maxNumberOfMessages) throws InputEventAdapterException {
        if (!(maxNumberOfMessages >= 0 && maxNumberOfMessages <= 10)) {
            throw new InputEventAdapterException("Max number of messages should be between 0 and 10. But provided " +
                    maxNumberOfMessages + ".");
        }
        this.maxNumberOfMessages = maxNumberOfMessages;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setVisibilityTimeout(Integer visibilityTimeout) throws InputEventAdapterException {
        if (visibilityTimeout != null && !(visibilityTimeout >= 0 && visibilityTimeout <= 43200)) {
            throw new InputEventAdapterException("Visibility timeout should be between 0 and 43200 seconds. But " +
                    "provided " + visibilityTimeout + "seconds.");
        }
        this.visibilityTimeout = visibilityTimeout;
    }

    public void setShouldDeleteAfterConsuming(boolean shouldDeleteAfterConsuming) {
        this.shouldDeleteAfterConsuming = shouldDeleteAfterConsuming;
    }

    public void setRetryCountLimit(int retryCountLimit) {
        this.retryCountLimit = retryCountLimit;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }
}
