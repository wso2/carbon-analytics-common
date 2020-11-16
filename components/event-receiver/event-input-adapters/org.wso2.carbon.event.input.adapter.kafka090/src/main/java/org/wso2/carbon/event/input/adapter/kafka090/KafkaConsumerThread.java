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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class KafkaConsumerThread implements Runnable {

    private InputEventAdapterListener brokerListener;
    private int tenantId;
    private Log log = LogFactory.getLog(KafkaConsumerThread.class);
    private final Lock consumerLock = new ReentrantLock();
    private final KafkaConsumer<byte[], byte[]> consumer;
    private boolean enableAsyncCommit;

    public KafkaConsumerThread(Properties props, String topic,
                               InputEventAdapterListener inBrokerListener, int inTenantId, boolean enableAsyncCommit) {
        consumer = new KafkaConsumer(props);
        consumer.subscribe(Arrays.asList(topic));
        log.info("Subscribed for topic: " + topic);
        brokerListener = inBrokerListener;
        tenantId = inTenantId;
        this.enableAsyncCommit = enableAsyncCommit;
    }

    public void run() {
        final Lock consumerLock = this.consumerLock;
        while (true) {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);

                // The time, in milliseconds, spent waiting in poll if data is not available. If 0, returns
                // immediately with any records that are available now. Must not be negative
                ConsumerRecords<byte[], byte[]> records = null;
                try {
                    consumerLock.lock();
                    // TODO add a huge value because, when there are so many equal group ids, the group balancing
                    // takes time and if this value is small, there will be an CommitFailedException while
                    // trying to retrieve data
                    records = consumer.poll(100);
                } catch (CommitFailedException ex) {
                    log.warn("Consumer poll() failed." + ex.getMessage(), ex);
                } finally {
                    consumerLock.unlock();
                }
                if (null != records) {
                    for (ConsumerRecord record : records) {
                        brokerListener.onEvent(record.value());
                    }
                    try {
                        consumerLock.lock();
                        if (!records.isEmpty()) {
                            if (enableAsyncCommit) {
                                consumer.commitAsync(new KafkaOffsetCommitCallback());
                            } else {
                                try {
                                    consumer.commitSync();
                                } catch (KafkaException e) {
                                    log.error("Exception occurred when committing offsets Synchronously", e);
                                }
                            }
                        }
                    } catch (CommitFailedException e) {
                        log.error("Kafka commit failed for topic kafka_result_topic", e);
                    } finally {
                        consumerLock.unlock();
                    }
                }
                try { //To avoid thread spin
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } catch (Throwable t) {
                log.error("Error while consuming event ", t);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    private static class KafkaOffsetCommitCallback implements OffsetCommitCallback {
        private Log log = LogFactory.getLog(KafkaConsumerThread.class);
        @Override
        public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
            if (exception == null) {
                if (log.isDebugEnabled()) {
                    for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : offsets.entrySet()) {
                        log.debug("Asynchronously commit offset done for " + entry.getKey().topic() +
                                " with offset of: " + entry.getValue().offset());
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : offsets.entrySet()) {
                        log.debug("Commit offset exception for " + entry.getKey().topic() +
                                " with offset of: " + entry.getValue().offset());
                    }
                }
                log.error("Exception occurred when committing offsets asynchronously.", exception);
            }
        }
    }
}
