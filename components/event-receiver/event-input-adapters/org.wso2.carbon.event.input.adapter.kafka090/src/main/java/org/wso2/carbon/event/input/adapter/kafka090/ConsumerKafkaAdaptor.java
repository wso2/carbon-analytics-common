/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.input.adapter.kafka090;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsumerKafkaAdaptor {
    private final Properties props;
    private final String topic;
    private ExecutorService executor;
    private Log log = LogFactory.getLog(ConsumerKafkaAdaptor.class);
    private int tenantId;
    private boolean enableAsyncCommit;

    public ConsumerKafkaAdaptor(String inTopic, int tenantId,
                                Properties props, boolean enableAsyncCommit) {
        this.props = props;
        this.topic = inTopic;
        this.tenantId = tenantId;
        this.enableAsyncCommit = enableAsyncCommit;
    }

    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    public void run(int numThreads, InputEventAdapterListener brokerListener) {
        try {
            // now launch all the threads
            executor = Executors.newFixedThreadPool(numThreads, new ThreadFactoryBuilder().
                    setNameFormat("Thread pool- component - ConsumerKafka090Adaptor.executor").build());
            executor.submit(new KafkaConsumerThread(props, topic, brokerListener, tenantId, enableAsyncCommit));
        } catch (Throwable t) {
            log.error("Error while creating Kafka090Consumer ", t);
        }
    }
}