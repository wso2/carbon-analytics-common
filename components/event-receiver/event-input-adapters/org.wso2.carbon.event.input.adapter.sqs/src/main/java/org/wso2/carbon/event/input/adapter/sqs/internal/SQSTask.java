/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.amazonaws.SdkClientException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteMessageResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;

import java.util.List;


/**
 * A runnable to handle the taks of polling the queue and delete messages after consuming them as requested.
 */
public class SQSTask implements Runnable {
    private static final Log log = LogFactory.getLog(SQSTask.class);

    private ReceiveMessageRequest receiveMessageRequest;
    private AmazonSQS sqs;
    private InputEventAdapterListener eventAdapterListener;
    private int tenantId;
    private SQSConfig configs;

    public SQSTask(AmazonSQS sqs, SQSConfig configs, InputEventAdapterListener eventAdapterListener, int tenantId) {
        this.tenantId = tenantId;
        this.sqs = sqs;
        this.eventAdapterListener = eventAdapterListener;
        this.configs = configs;
        this.receiveMessageRequest = new ReceiveMessageRequest()
                .withQueueUrl(configs.getQueueURL())
                .withMaxNumberOfMessages(configs.getMaxNumberOfMessages());

        if (configs.getWaitTime() != null) {
            receiveMessageRequest = receiveMessageRequest.withWaitTimeSeconds(configs.getWaitTime());
        }

        if (configs.getVisibilityTimeout() != null) {
            receiveMessageRequest = receiveMessageRequest.withVisibilityTimeout(configs.getVisibilityTimeout());
        }
    }

    @Override
    public void run() {
        int receiveRetryCount = 0;
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
        do {
            List<Message> messages = receiveMessageFromQueue();
            receiveRetryCount++;
            if (messages != null) {
                for (Message message : messages) {
                    String eventMsg = message.getBody();
                    eventAdapterListener.onEvent(eventMsg);
                    if (configs.shouldDeleteAfterConsuming()) {
                        delete(message);
                    }
                }
            } else {
                try {
                    Thread.sleep(configs.getRetryInterval());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } while (receiveRetryCount < configs.getRetryCountLimit());
    }

    private void delete(Message message) {
        int deleteRetryCount = 0;
        do {
            deleteRetryCount++;
            if (!deleteMessageFromQueue(message)) {
                try {
                    Thread.sleep(configs.getRetryInterval());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } while (deleteRetryCount < configs.getRetryCountLimit());
    }

    private boolean deleteMessageFromQueue(Message message) {
        String messageReceiptHandle = message.getReceiptHandle();
        try {
            DeleteMessageResult deleteMessageResult =
                    sqs.deleteMessage(new DeleteMessageRequest(configs.getQueueURL(), messageReceiptHandle));
            return deleteMessageResult.getSdkHttpMetadata().getHttpStatusCode() == 200;
        } catch (SdkClientException e) {
            log.error(String.format("Failed to delete message '%s' from the queue '%s'. Hence retrying.",
                    message.getBody(), configs.getQueueURL()), e);
            return false;
        }
    }

    private List<Message> receiveMessageFromQueue() {
        List<Message> messages = null;
        try {
            messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
        } catch (SdkClientException e) {
            log.error(String.format("Failed to receive message from the queue '%s'. Hence retrying.",
                    configs.getQueueURL()), e);
        }
        return messages;
    }
}
