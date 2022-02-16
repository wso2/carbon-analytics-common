/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.carbon.databridge.core.internal.queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.core.AgentCallback;
import org.wso2.carbon.databridge.core.RawDataAgentCallback;
import org.wso2.carbon.databridge.core.exception.EventConversionException;
import org.wso2.carbon.databridge.core.utils.EventComposite;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Will removes the events from queues and send then to the endpoints.
 */
public class QueueWorker implements Runnable {

    private static final Logger log = LogManager.getLogger(QueueWorker.class);
    private BlockingQueue<EventComposite> eventQueue;
    private List<AgentCallback> subscribers;
    private List<RawDataAgentCallback> rawDataSubscribers;
    private ExecutorService executorService;

    public QueueWorker(BlockingQueue<EventComposite> queue,
                       List<AgentCallback> subscribers,
                       List<RawDataAgentCallback> rawDataSubscribers,
                       ExecutorService executorService) {
        this.eventQueue = queue;
        this.subscribers = subscribers;
        this.rawDataSubscribers = rawDataSubscribers;
        this.executorService = executorService;
    }

    public void run() {
        List<Event> eventList = null;
        try {
            if (log.isDebugEnabled()) {
                // Useful log to determine if the server can handle the load
                // If the numbers go above 1000+, then it probably will.
                // Typically, for c = 300, n = 1000, the number stays < 100
                log.debug(eventQueue.size() + " messages in queue before " +
                          Thread.currentThread().getName() + " worker has polled queue");
            }
            ThreadPoolExecutor threadPoolExecutor = ((ThreadPoolExecutor) executorService);
            if (eventQueue.size() != 0) {
                do {
                    EventComposite eventComposite = eventQueue.poll();
                    if (eventComposite != null) {
                        if (rawDataSubscribers.size() > 0) {
                            for (RawDataAgentCallback agentCallback : rawDataSubscribers) {
                                try {
                                    agentCallback.receive(eventComposite);
                                } catch (Throwable e) {
                                    log.error("Error in passing event composite " + eventComposite + " to " +
                                            "subscriber " + agentCallback, e);
                                }
                            }
                        }
                        if (subscribers.size() > 0) {
                            try {
                                eventList = eventComposite.getEventConverter().toEventList(
                                        eventComposite.getEventBundle(), eventComposite.getStreamTypeHolder());

                                if (log.isDebugEnabled()) {
                                    log.debug("Dispatching event to " + subscribers.size() + " subscriber(s)");
                                }
                                for (AgentCallback agentCallback : subscribers) {
                                    try {
                                        agentCallback.receive(eventList,
                                                eventComposite.getAgentSession().getCredentials());
                                    } catch (Throwable e) {
                                        log.error("Error in passing event eventList " + eventList + " to " +
                                                "subscriber " + agentCallback, e);
                                    }
                                }
                                if (log.isDebugEnabled()) {
                                    log.debug(eventQueue.size() + " messages in queue after " +
                                            Thread.currentThread().getName() + " worker has finished work");
                                }

                            } catch (EventConversionException re) {
                                log.error("Dropping wrongly formatted event sent ", re);
                            }
                        }
                    }
                } while (threadPoolExecutor.getActiveCount() <= 3 && eventQueue.size() != 0);
            }
        } catch (Throwable e) {
            log.error("Error in passing events " + eventList + " to subscribers " + subscribers + " " +
                    rawDataSubscribers, e);
        }
    }


}
