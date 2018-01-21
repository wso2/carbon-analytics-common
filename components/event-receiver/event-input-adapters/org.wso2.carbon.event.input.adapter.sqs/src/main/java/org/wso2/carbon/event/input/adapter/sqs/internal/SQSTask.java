package org.wso2.carbon.event.input.adapter.sqs.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;

import java.util.List;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteMessageResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

/**
 * A runnable to handle the taks of polling the queue and delete messages after consuming them as requested
 **/
public class SQSTask implements Runnable{
    private static final Log log = LogFactory.getLog(SQSTask.class);

    private ReceiveMessageRequest request;
    private AmazonSQS sqs;
    private InputEventAdapterListener eventAdapterListener;
    private int tenantId;
    private SQSConfig configs;

    public SQSTask(AmazonSQS sqs, SQSConfig configs, InputEventAdapterListener eventAdapterListener, int tenantId) {
        this.tenantId = tenantId;
        this.sqs = sqs;
        this.eventAdapterListener = eventAdapterListener;
        this.configs = configs;
        this.request = new ReceiveMessageRequest()
                            .withQueueUrl(configs.getQueueURL())
                            .withMaxNumberOfMessages(configs.getMaxNumberOfMessages());

        if (configs.getWaitTime() != null) {
            request = request.withWaitTimeSeconds(configs.getWaitTime());
        }

        if (configs.getVisibilityTimeout() != null) {
            request = request.withVisibilityTimeout(configs.getVisibilityTimeout());
        }
    }

    @Override
    public void run() {
        int deleteRetryCount = 0;
        int receiveRetryCount = 0;
        int retryCount = 0;
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
        do {
            List<Message> messages = receiveMessageFromQueue();
            receiveRetryCount++;
            if (messages != null) {
                for(Message message : messages) {
                    String eventMsg = message.getBody();
                    eventAdapterListener.onEvent(eventMsg);
                    if (configs.shouldDeleteAfterConsuming()) {
                        do {
                            deleteRetryCount++;
                            if (deleteMessageFromQueue(message)) {
                                deleteRetryCount = 0;
                                break;
                            }
                            try {
                                Thread.sleep(configs.getRetryInterval());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } while (deleteRetryCount < configs.getRetryCountLimit());
                    }
                }
                receiveRetryCount = 0;
                break;
            } try {
                Thread.sleep(configs.getRetryInterval());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (receiveRetryCount < configs.getRetryCountLimit());
    }

    private boolean deleteMessageFromQueue(Message message){
        String messageRecieptHandle = message.getReceiptHandle();
        try {
            DeleteMessageResult deleteMessageResult =
                    sqs.deleteMessage(new DeleteMessageRequest(configs.getQueueURL(), messageRecieptHandle));
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
            messages = sqs.receiveMessage(request).getMessages();
        } catch (SdkClientException e) {
            log.error(String.format("Failed to receive message from the queue '%s'. Hence retrying.",
                    configs.getQueueURL()), e);
        }
        return messages;
    }
}
