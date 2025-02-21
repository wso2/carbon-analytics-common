/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.stream.core.internal.config;

import org.wso2.carbon.event.stream.core.internal.util.EventStreamConstants;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

/**
 * AdapterConfigs class is used to represent the configuration of the output event adapters.
 */
@XmlRootElement(name="outputEventAdaptersConfig")
public class EventPublisherConfigs {

    private int minThread;
    private int maxThread;
    private int keepAliveTime;
    private int jobQueueSize;

    @XmlElement(name="minThread")
    public void setMinThread(int minThread) {
        this.minThread = minThread;
    }

    @XmlElement(name="maxThread")
    public void setMaxThread(int maxThread) {
        this.maxThread = maxThread;
    }

    @XmlElement(name="keepAliveTime")
    public void setKeepAliveTime(int keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    @XmlElement(name="jobQueueSize")
    public void setJobQueueSize(int jobQueueSize) {
        this.jobQueueSize = jobQueueSize;
    }

    public Map<String, Integer> getEventPublisherThreadPoolConfigs() {
        Map<String, Integer> eventPublisherConfigs = new HashMap<>();
        eventPublisherConfigs.put(EventStreamConstants.EVENT_PUBLISHER_MIN_THREAD_POOL_SIZE, minThread);
        eventPublisherConfigs.put(EventStreamConstants.EVENT_PUBLISHER_MAX_THREAD_POOL_SIZE, maxThread);
        eventPublisherConfigs.put(EventStreamConstants.EVENT_PUBLISHER_KEEP_ALIVE_TIME, keepAliveTime);
        eventPublisherConfigs.put(EventStreamConstants.EVENT_PUBLISHER_JOB_QUEUE_SIZE, jobQueueSize);
        return eventPublisherConfigs;
    }
}
