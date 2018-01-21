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

package org.wso2.carbon.event.input.adapter.sqs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapter;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.wso2.carbon.event.input.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.input.adapter.sqs.internal.SQSConfig;
import org.wso2.carbon.event.input.adapter.sqs.internal.SQSProvider;
import org.wso2.carbon.event.input.adapter.sqs.internal.SQSTask;
import org.wso2.carbon.event.input.adapter.sqs.internal.util.SQSEventAdapterConstants;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * SQS Input Event Adapter is an adapter for consuming messages from a queue
 * provided by Amazon Simple Queue Service (SQS)
 **/
public final class SQSEventAdapter implements InputEventAdapter {

    private final InputEventAdapterConfiguration eventAdapterConfiguration;
    private final String id = UUID.randomUUID().toString();
    private ExecutorService scheduler;
    private static final Log log = LogFactory.getLog(SQSEventAdapter.class);

    private int pollingInterval;

    private SQSProvider sqsProvider;

    public SQSEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration,
                           Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
    }

    @Override
    public void init(InputEventAdapterListener eventAdaptorListener) throws InputEventAdapterException {
            Map adapterProperties = eventAdapterConfiguration.getProperties();

            String accessKey = adapterProperties.get(SQSEventAdapterConstants.ACCESS_KEY).toString();
            String secretKey = adapterProperties.get(SQSEventAdapterConstants.SECRET_KEY).toString();
            String region = adapterProperties.get(SQSEventAdapterConstants.REGION).toString();
            String queueName = adapterProperties.get(SQSEventAdapterConstants.QUEUE_URL).toString();

            pollingInterval = adapterProperties.get(SQSEventAdapterConstants.POLLING_INTERVAL) != null ?
                    Integer.parseInt(adapterProperties.get(SQSEventAdapterConstants.POLLING_INTERVAL).toString()) :
                    SQSEventAdapterConstants.DEFAULT_POLLING_INTERVAL;

            Integer waitTime =  null;
            if(adapterProperties.get(SQSEventAdapterConstants.WAIT_TIME) != null) {
                waitTime = Integer.parseInt(adapterProperties.get(SQSEventAdapterConstants.WAIT_TIME).toString());
            }

            Integer maxNumberOfMessages =
                    adapterProperties.get(SQSEventAdapterConstants.MAX_NUMBER_OF_MSGS_NAME) != null ?
                    Integer.parseInt(adapterProperties.get(SQSEventAdapterConstants.MAX_NUMBER_OF_MSGS_NAME).toString())
                        : SQSEventAdapterConstants.DEFAULT_MAX_NUMBER_OF_MSGS;

            Integer visibilityTimeout = null;
            if(adapterProperties.get(SQSEventAdapterConstants.VISIBILITY_TIMEOUT) != null) {
                Integer.parseInt(adapterProperties.get(SQSEventAdapterConstants.VISIBILITY_TIMEOUT).toString());
            }

            boolean shouldDeleteMsgAfterProcessing =
                    adapterProperties.get(SQSEventAdapterConstants.DELETE_AFTER_CONSUMING) != null ?
                            Boolean.parseBoolean(adapterProperties.get(SQSEventAdapterConstants
                                    .DELETE_AFTER_CONSUMING).toString()) : SQSEventAdapterConstants
                            .DEFAULT_DELETE_AFTER_CONSUMING;

            int retryInterval = adapterProperties.get(SQSEventAdapterConstants.RETRY_INTERVAL) != null ?
                        Integer.parseInt(adapterProperties.get(SQSEventAdapterConstants.RETRY_INTERVAL).toString())
                        : SQSEventAdapterConstants.DEFAULT_RETRY_INTERVAL;

            int retryCountLimit = adapterProperties.get(SQSEventAdapterConstants.RETRY_ATTEMPT_LIMIT) != null ?
                    Integer.parseInt(adapterProperties.get(SQSEventAdapterConstants.RETRY_ATTEMPT_LIMIT).toString())
                    : SQSEventAdapterConstants.DEFAULT_RETRY_ATTEMPT_LIMIT;

            SQSConfig sqsConfigs = new SQSConfig(accessKey, secretKey, queueName, waitTime, maxNumberOfMessages,
                    region, visibilityTimeout, shouldDeleteMsgAfterProcessing, retryCountLimit, retryInterval);

            if (scheduler == null) {
                scheduler = Executors.newScheduledThreadPool(1);
            }

            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            sqsProvider = new SQSProvider(sqsConfigs, eventAdaptorListener, tenantId);
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("not-supported");
    }

    @Override
    public void connect() {
        if(!scheduler.isShutdown()) {
            SQSTask sqsTask = sqsProvider.getNewSQSTask();
            ((ScheduledExecutorService)scheduler).scheduleAtFixedRate(sqsTask, 0, pollingInterval, TimeUnit.SECONDS);
        }
    }

    @Override
    public void disconnect() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    @Override
    public void destroy() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SQSEventAdapter))
            return false;
        SQSEventAdapter that = (SQSEventAdapter) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean isEventDuplicatedInCluster() {
        return false;
    }

    @Override
    public boolean isPolling() {
        return false;
    }
}