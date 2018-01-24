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
    private ScheduledExecutorService scheduler;

    private int pollingInterval = SQSEventAdapterConstants.DEFAULT_POLLING_INTERVAL;
    private int listenerWaitingTime = SQSEventAdapterConstants.DEFAULT_ADAPTER_LISTENER_WAITING_TIME;

    private SQSProvider sqsProvider;

    public SQSEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration,
                           Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }

    @Override
    public void init(InputEventAdapterListener eventAdaptorListener) throws InputEventAdapterException {
        Map<String, String> adapterProperties = eventAdapterConfiguration.getProperties();
        SQSConfig sqsConfig = new SQSConfig();

        // These are Mandatory properties. Therefore cannot be null.
        sqsConfig.setAccessKey(adapterProperties.get(SQSEventAdapterConstants.ACCESS_KEY));
        sqsConfig.setSecretKey(adapterProperties.get(SQSEventAdapterConstants.SECRET_KEY));
        sqsConfig.setRegion(adapterProperties.get(SQSEventAdapterConstants.REGION));
        sqsConfig.setQueueURL(adapterProperties.get(SQSEventAdapterConstants.QUEUE_URL));

        // Optional properties
        if (adapterProperties.get(SQSEventAdapterConstants.POLLING_INTERVAL) != null) {
            pollingInterval = Integer.parseInt(adapterProperties.get(SQSEventAdapterConstants.POLLING_INTERVAL));
        }

        if (adapterProperties.get(SQSEventAdapterConstants.WAIT_TIME) != null) {
            sqsConfig.setWaitTime(Integer.parseInt(adapterProperties.get(SQSEventAdapterConstants.WAIT_TIME)));
        }

        if (adapterProperties.get(SQSEventAdapterConstants.MAX_NUMBER_OF_MSGS) != null) {
            sqsConfig.setMaxNumberOfMessages(Integer.parseInt(adapterProperties.get(SQSEventAdapterConstants
                    .MAX_NUMBER_OF_MSGS)));
        }

        if (adapterProperties.get(SQSEventAdapterConstants.VISIBILITY_TIMEOUT) != null) {
            sqsConfig.setVisibilityTimeout(Integer.parseInt(adapterProperties.get(SQSEventAdapterConstants
                    .VISIBILITY_TIMEOUT)));
        }

        if (adapterProperties.get(SQSEventAdapterConstants.DELETE_AFTER_CONSUMING) != null) {
            sqsConfig.setShouldDeleteAfterConsuming(Boolean.parseBoolean(adapterProperties
                    .get(SQSEventAdapterConstants.DELETE_AFTER_CONSUMING)));
        }

        if (adapterProperties.get(SQSEventAdapterConstants.RETRY_INTERVAL) != null) {
            sqsConfig.setRetryInterval(Integer.parseInt(adapterProperties
                    .get(SQSEventAdapterConstants.RETRY_INTERVAL)));
        }

        if (adapterProperties.get(SQSEventAdapterConstants.RETRY_ATTEMPT_LIMIT) != null) {
            sqsConfig.setRetryCountLimit(Integer.parseInt(adapterProperties
                    .get(SQSEventAdapterConstants.RETRY_ATTEMPT_LIMIT)));
        }

        if (globalProperties.get(SQSEventAdapterConstants.ADAPTER_LISTENER_WAITING_TIME) != null) {
            listenerWaitingTime = Integer.parseInt(globalProperties
                    .get(SQSEventAdapterConstants.ADAPTER_LISTENER_WAITING_TIME));
        }

        scheduler = Executors.newScheduledThreadPool(1);

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        sqsProvider = new SQSProvider(sqsConfig, eventAdaptorListener, tenantId);
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("not-supported");
    }

    @Override
    public void connect() {
        if (scheduler != null && !scheduler.isShutdown()) {
            SQSTask sqsTask = sqsProvider.getNewSQSTask();
            scheduler.scheduleAtFixedRate(sqsTask, listenerWaitingTime, pollingInterval, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void disconnect() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    @Override
    public void destroy() {
        if (scheduler != null) {
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
