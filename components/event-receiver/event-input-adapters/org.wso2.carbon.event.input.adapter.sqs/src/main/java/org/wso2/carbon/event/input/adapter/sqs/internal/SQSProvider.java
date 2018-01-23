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
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;

/**
 * Class to handle basic SQS instantiation and to create new threads for polling the queue.
 */
public class SQSProvider {
    private AmazonSQS sqs;
    private InputEventAdapterListener eventAdapterListener;
    private SQSConfig configs;
    private int tenantID;

    public SQSProvider(SQSConfig configs, InputEventAdapterListener eventAdapterListener, int tenantID)
            throws InputEventAdapterException {
        this.configs = configs;
        this.eventAdapterListener = eventAdapterListener;
        this.tenantID = tenantID;
        BasicAWSCredentials credentials = new BasicAWSCredentials(configs.getAccessKey(), configs.getSecretKey());
        try {
            sqs = AmazonSQSClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(configs.getRegion())
                    .build();
        } catch (SdkClientException e) {
            throw new InputEventAdapterException("Failed to create SQS receiver due to invalid configuration.", e);
        }
    }

    /**
     * Method to get new thread for polling the queue.
     *
     * @return SQSTask : newly created Thread to execute operations related to consuming the queue
     */
    public SQSTask getNewSQSTask() {
        return new SQSTask(sqs, configs, eventAdapterListener, tenantID);
    }
}
