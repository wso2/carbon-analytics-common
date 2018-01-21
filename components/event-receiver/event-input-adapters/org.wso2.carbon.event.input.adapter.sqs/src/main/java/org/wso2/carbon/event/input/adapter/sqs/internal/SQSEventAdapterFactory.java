/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.input.adapter.sqs.internal;


import org.wso2.carbon.event.input.adapter.core.*;
import org.wso2.carbon.event.input.adapter.sqs.SQSEventAdapter;
import org.wso2.carbon.event.input.adapter.sqs.internal.util.SQSEventAdapterConstants;

import java.util.*;

/**
 * The SQS event adapter factory class to create an SQS input adapter
 */
public class SQSEventAdapterFactory extends InputEventAdapterFactory {

    private ResourceBundle resourceBundle =
            ResourceBundle.getBundle("org.wso2.carbon.event.input.adapter.sqs.i18n.Resources", Locale.getDefault());

    public SQSEventAdapterFactory() {

    }

    @Override
    public String getType() {
        return SQSEventAdapterConstants.ADAPTER_TYPE_SQS;
    }

    @Override
    public List<String> getSupportedMessageFormats() {
        List<String> supportInputMessageTypes = new ArrayList<String>();
        supportInputMessageTypes.add(MessageType.XML);
        supportInputMessageTypes.add(MessageType.JSON);
        supportInputMessageTypes.add(MessageType.TEXT);
        return supportInputMessageTypes;
    }

    @Override
    public List<Property> getPropertyList() {

        List<Property> propertyList = new ArrayList<Property>();

        // Access Key for SQS
        Property accessKeyProperty = new Property(SQSEventAdapterConstants.ACCESS_KEY);
        accessKeyProperty.setRequired(true);
        accessKeyProperty.setSecured(true);
        accessKeyProperty.setEncrypted(true);
        accessKeyProperty.setHint(SQSEventAdapterConstants.ACCESS_KEY_HINT);
        accessKeyProperty.setDisplayName(
                resourceBundle.getString(SQSEventAdapterConstants.ACCESS_KEY));

        // Secret Key for SQS
        Property secretKeyProperty = new Property(SQSEventAdapterConstants.SECRET_KEY);
        secretKeyProperty.setRequired(true);
        secretKeyProperty.setSecured(true);
        secretKeyProperty.setEncrypted(true);
        secretKeyProperty.setHint(SQSEventAdapterConstants.SECRET_KEY_HINT);
        secretKeyProperty.setDisplayName(
                resourceBundle.getString(SQSEventAdapterConstants.SECRET_KEY));

        // URL of SQS Queue
        Property queueURLProperty = new Property(SQSEventAdapterConstants.QUEUE_URL);
        queueURLProperty.setRequired(true);
        queueURLProperty.setHint(SQSEventAdapterConstants.QUEUE_NAME_HINT);
        queueURLProperty.setDisplayName(
                resourceBundle.getString(SQSEventAdapterConstants.QUEUE_URL));

        // Polling interval
        Property pollingIntervalProperty = new Property(SQSEventAdapterConstants.POLLING_INTERVAL);
        pollingIntervalProperty.setRequired(false);
        pollingIntervalProperty.setHint(SQSEventAdapterConstants.POLLING_INTERVAL_HINT);
        pollingIntervalProperty.setDisplayName(
                resourceBundle.getString(SQSEventAdapterConstants.POLLING_INTERVAL));

        // Wait time until messages available in the queue
        Property waitTimeProperty = new Property(SQSEventAdapterConstants.WAIT_TIME);
        waitTimeProperty.setRequired(false);
        waitTimeProperty.setHint(SQSEventAdapterConstants.WAIT_TIME_HINT);
        waitTimeProperty.setDisplayName(
                resourceBundle.getString(SQSEventAdapterConstants.WAIT_TIME));

        // Wait time until messages available in the queue
        Property maxNumberOfMsgsProperty = new Property(SQSEventAdapterConstants.MAX_NUMBER_OF_MSGS_NAME);
        maxNumberOfMsgsProperty.setRequired(false);
        maxNumberOfMsgsProperty.setHint(SQSEventAdapterConstants.MAX_NUMBER_OF_MSGS_HINT);
        maxNumberOfMsgsProperty.setDisplayName(
                resourceBundle.getString(SQSEventAdapterConstants.MAX_NUMBER_OF_MSGS_NAME));

        // AWS service region
        Property regionProperty = new Property(SQSEventAdapterConstants.REGION);
        regionProperty.setRequired(true);
        regionProperty.setHint(SQSEventAdapterConstants.REGION_HINT);
        regionProperty.setDisplayName(
                resourceBundle.getString(SQSEventAdapterConstants.REGION)
        );

        /*
        The length of time (in seconds) for which a message received from a queue will be invisible to other receiving
        components.
        */
        Property visibilityTimeoutProperty = new Property(SQSEventAdapterConstants.VISIBILITY_TIMEOUT);
        visibilityTimeoutProperty.setRequired(false);
        visibilityTimeoutProperty.setHint(SQSEventAdapterConstants.VISIBILITY_TIMEOUT_HINT);
        visibilityTimeoutProperty.setDisplayName(
                resourceBundle.getString(SQSEventAdapterConstants.VISIBILITY_TIMEOUT)
        );

        // Whether the message should be deleted after get consumed or not
        Property deleteMessageOptionProperty = new Property(SQSEventAdapterConstants.DELETE_AFTER_CONSUMING);
        deleteMessageOptionProperty.setRequired(false);
        deleteMessageOptionProperty.setOptions(new String[]{"true", "false"});
        deleteMessageOptionProperty.setHint(SQSEventAdapterConstants.DELETE_AFTER_CONSUMING_HINT);
        deleteMessageOptionProperty.setDisplayName(
                resourceBundle.getString(SQSEventAdapterConstants.DELETE_AFTER_CONSUMING)
        );

        // Time period between two consecutive retry attempts if a failure occured.
        Property retryIntervalProperty = new Property(SQSEventAdapterConstants.RETRY_INTERVAL);
        retryIntervalProperty.setRequired(false);
        retryIntervalProperty.setHint(SQSEventAdapterConstants.RETRY_INTERVAL_HINT);
        retryIntervalProperty.setDisplayName(
                resourceBundle.getString(SQSEventAdapterConstants.RETRY_INTERVAL)
        );

        // Maximum number of retry attempts for a failure
        Property retryAttemptLimitProperty = new Property(SQSEventAdapterConstants.RETRY_ATTEMPT_LIMIT);
        retryAttemptLimitProperty.setRequired(false);
        retryAttemptLimitProperty.setHint(SQSEventAdapterConstants.RETRY_ATTEMPT_LIMIT_HINT);
        retryAttemptLimitProperty.setDisplayName(
                resourceBundle.getString(SQSEventAdapterConstants.RETRY_ATTEMPT_LIMIT)
        );

        propertyList.add(queueURLProperty);
        propertyList.add(accessKeyProperty);
        propertyList.add(secretKeyProperty);
        propertyList.add(regionProperty);
        propertyList.add(pollingIntervalProperty);
        propertyList.add(waitTimeProperty);
        propertyList.add(maxNumberOfMsgsProperty);
        propertyList.add(visibilityTimeoutProperty);
        propertyList.add(deleteMessageOptionProperty);
        propertyList.add(retryIntervalProperty);
        propertyList.add(retryAttemptLimitProperty);

        return propertyList;
    }

    @Override
    public String getUsageTips() {
        return null;
    }

    @Override
    public InputEventAdapter createEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration,
                                                Map<String, String> globalProperties) {
        return new SQSEventAdapter(eventAdapterConfiguration, globalProperties);
    }
}
