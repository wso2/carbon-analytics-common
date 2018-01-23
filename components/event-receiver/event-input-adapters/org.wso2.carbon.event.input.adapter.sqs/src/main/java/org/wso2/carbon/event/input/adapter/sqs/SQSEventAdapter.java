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

package org.wso2.carbon.event.input.adapter.sqs;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * SQS Input Event Adapter is an adapter for consuming messages from a queue provided by Amazon Simple Queue Service.
 */
public final class SQSEventAdapter implements InputEventAdapter {

    private final InputEventAdapterConfiguration eventAdapterConfiguration;
    private final Map<String, String> globalProperties;
    private final String id = UUID.randomUUID().toString();
    private ExecutorService scheduler;

    private int pollingInterval;
    private int listenerWaitingTime;

    private SQSProvider sqsProvider;

    public SQSEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration,
                           Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }

    @Override
    public void init(InputEventAdapterListener eventAdaptorListener) throws InputEventAdapterException {
        Map adapterProperties = eventAdapterConfiguration.getProperties();
        SQSConfig sqsConfigs = new SQSConfig();

        sqsConfigs.setAccessKey(adapterProperties.get(SQSEventAdapterConstants.ACCESS_KEY).toString());
        sqsConfigs.setSecretKey(adapterProperties.get(SQSEventAdapterConstants.SECRET_KEY).toString());
        sqsConfigs.setRegion(adapterProperties.get(SQSEventAdapterConstants.REGION).toString());
        sqsConfigs.setQueueURL(adapterProperties.get(SQSEventAdapterConstants.QUEUE_URL).toString());

        pollingInterval = adapterProperties.get(SQSEventAdapterConstants.POLLING_INTERVAL) != null ?
                Integer.parseInt(adapterProperties.get(SQSEventAdapterConstants.POLLING_INTERVAL).toString()) :
                SQSEventAdapterConstants.DEFAULT_POLLING_INTERVAL;

        Integer waitTime;
        if (adapterProperties.get(SQSEventAdapterConstants.WAIT_TIME) != null) {
            waitTime = Integer.parseInt(adapterProperties.get(SQSEventAdapterConstants.WAIT_TIME).toString());
            sqsConfigs.setWaitTime(waitTime);
        }

        Integer maxNumberOfMessages =
                adapterProperties.get(SQSEventAdapterConstants.MAX_NUMBER_OF_MSGS) != null ?
                        Integer.parseInt(adapterProperties.get(SQSEventAdapterConstants.MAX_NUMBER_OF_MSGS).toString())
                        : SQSEventAdapterConstants.DEFAULT_MAX_NUMBER_OF_MSGS;
        sqsConfigs.setMaxNumberOfMessages(maxNumberOfMessages);

        Integer visibilityTimeout = null;
        if (adapterProperties.get(SQSEventAdapterConstants.VISIBILITY_TIMEOUT) != null) {
            visibilityTimeout = Integer.parseInt(adapterProperties.get(SQSEventAdapterConstants.VISIBILITY_TIMEOUT)
                    .toString());
        }
        sqsConfigs.setVisibilityTimeout(visibilityTimeout);

        boolean shouldDeleteMsgAfterProcessing =
                adapterProperties.get(SQSEventAdapterConstants.DELETE_AFTER_CONSUMING) != null ?
                        Boolean.parseBoolean(adapterProperties.get(SQSEventAdapterConstants
                                .DELETE_AFTER_CONSUMING).toString())
                        : SQSEventAdapterConstants.DEFAULT_DELETE_AFTER_CONSUMING;
        sqsConfigs.setShouldDeleteAfterConsuming(shouldDeleteMsgAfterProcessing);

        int retryInterval = adapterProperties.get(SQSEventAdapterConstants.RETRY_INTERVAL) != null ?
                Integer.parseInt(adapterProperties.get(SQSEventAdapterConstants.RETRY_INTERVAL).toString())
                : SQSEventAdapterConstants.DEFAULT_RETRY_INTERVAL;
        sqsConfigs.setRetryInterval(retryInterval);

        int retryCountLimit = adapterProperties.get(SQSEventAdapterConstants.RETRY_ATTEMPT_LIMIT) != null ?
                Integer.parseInt(adapterProperties.get(SQSEventAdapterConstants.RETRY_ATTEMPT_LIMIT).toString())
                : SQSEventAdapterConstants.DEFAULT_RETRY_ATTEMPT_LIMIT;
        sqsConfigs.setRetryCountLimit(retryCountLimit);

        listenerWaitingTime = globalProperties.get(SQSEventAdapterConstants.ADAPTER_LISTENER_WAITING_TIME) != null ?
                Integer.parseInt(globalProperties.get(SQSEventAdapterConstants.ADAPTER_LISTENER_WAITING_TIME)) :
                SQSEventAdapterConstants.DEFAULT_ADAPTER_LISTENER_WAITING_TIME;

        scheduler = Executors.newScheduledThreadPool(1);

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        sqsProvider = new SQSProvider(sqsConfigs, eventAdaptorListener, tenantId);
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("not-supported");
    }

    @Override
    public void connect() {
        if (scheduler != null && !scheduler.isShutdown()) {
            SQSTask sqsTask = sqsProvider.getNewSQSTask();
            ((ScheduledExecutorService) scheduler).scheduleAtFixedRate(sqsTask, listenerWaitingTime, pollingInterval,
                    TimeUnit.MILLISECONDS);
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
        if (this == o) {
            return true;
        }
        if (!(o instanceof SQSEventAdapter)) {
            return false;
        }
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
