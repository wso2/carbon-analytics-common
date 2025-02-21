/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.event.stream.core.internal;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.event.stream.core.WSO2EventConsumer;

/**
 * This class is used to consume events from the event stream.
 */
public class EventConsumerThread implements Runnable {
    private WSO2EventConsumer consumer;
    private Event event;
    private int tenantId;

    public EventConsumerThread(WSO2EventConsumer consumer, Event event, int tenantId) {
        this.consumer = consumer;
        this.event = event;
        this.tenantId = tenantId;
    }

    @Override
    public void run() {
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
        consumer.onEvent(event);
    }
}
