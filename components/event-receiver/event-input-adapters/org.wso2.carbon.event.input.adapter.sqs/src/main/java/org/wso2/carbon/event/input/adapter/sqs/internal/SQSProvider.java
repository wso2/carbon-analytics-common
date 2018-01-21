package org.wso2.carbon.event.input.adapter.sqs.internal;

import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

/**
 * Class to handle basic SQS instantiation
 * and to create new threads for polling the queue
 * */
public class SQSProvider {
    private AmazonSQS sqs;
    private InputEventAdapterListener eventAdapterListener;
    private SQSConfig configs;
    private int tenantID;

    public SQSProvider(SQSConfig configs, InputEventAdapterListener eventAdapterListener, int tenantID){
        this.configs = configs;
        this.eventAdapterListener = eventAdapterListener;
        this.tenantID = tenantID;
        BasicAWSCredentials credentials = new BasicAWSCredentials(configs.getAccessKey(), configs.getSecretKey());
        sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(configs.getSigningRegion())
                .build();
    }

    /**
     * Method to get new thread for polling the queue
     * @return SQSTask
     */
    public SQSTask getNewSQSTask() {
        return new SQSTask(sqs, configs, eventAdapterListener, tenantID);
    }
}
