/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.event.input.adapter.sqs.internal.util;

/**
 * Util class to keep all the constants used in the SQSEventAdapter implementation
 **/
public final class SQSEventAdapterConstants {

    private SQSEventAdapterConstants() {
    }

    // Reference names for properties
    public static final String ADAPTER_TYPE_SQS = "sqs";
    public static final String ACCESS_KEY = "accessKey";
    public static final String SECRET_KEY = "secretKey";
    public static final String QUEUE_URL = "queueURL";
    public static final String POLLING_INTERVAL = "pollingInterval";
    public static final String WAIT_TIME = "waitTime";
    public static final String MAX_NUMBER_OF_MSGS_NAME = "maxNumberOfMessages";
    public static final String REGION = "region";
    public static final String VISIBILITY_TIMEOUT = "visibilityTimeout";
    public static final String DELETE_AFTER_CONSUMING = "deleteAfterConsuming";
    public static final String RETRY_INTERVAL = "retryInterval";
    public static final String RETRY_ATTEMPT_LIMIT = "retryAttemptLimit";

    // Default values for properties
    public static final int DEFAULT_POLLING_INTERVAL = 5; //seconds
    public static final int DEFAULT_MAX_NUMBER_OF_MSGS = 1;
    public static final int DEFAULT_RETRY_INTERVAL = 5000; // milliseconds
    public static final int DEFAULT_RETRY_ATTEMPT_LIMIT = 10;
    public static final boolean DEFAULT_DELETE_AFTER_CONSUMING = true;

    // Hint texts to be displayed in the UI
    public static final String ACCESS_KEY_HINT = "Access key for the amazon web services";
    public static final String SECRET_KEY_HINT = "Secret key of the amazon user";
    public static final String QUEUE_NAME_HINT = "Name of the queue to which is going to be consumed";
    public static final String POLLING_INTERVAL_HINT = "Interval (in seconds) between two message retrieval operations";
    public static final String WAIT_TIME_HINT = "The maximum amount of time (in seconds) that a polling call will wait for a " +
            "message to become available in the queue.";
    public static final String MAX_NUMBER_OF_MSGS_HINT = "Maximum number of messages retrieved from the queue per " +
            " polling call";
    public static final String REGION_HINT = "Amazon web service region";
    public static final String VISIBILITY_TIMEOUT_HINT = "The length of time (in seconds) for which " +
            "a message received from a queue will be invisible to other receiving components.\n(This is applicable " +
            "only if deleting messages after consuming is disabled).";
    public static final String DELETE_AFTER_CONSUMING_HINT = "Whether message to be deleted from the queue after " +
            "consuming it or not";
    public static final String RETRY_INTERVAL_HINT = "Time interval (in milliseconds) for a retry to be happened when" +
            " a failure occured when deleting operation failed.";
    public static final String RETRY_ATTEMPT_LIMIT_HINT = "Maximum number of retry attempts for a failure.";

}
