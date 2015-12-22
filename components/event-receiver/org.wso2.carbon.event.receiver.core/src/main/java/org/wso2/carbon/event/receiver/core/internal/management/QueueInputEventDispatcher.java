/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.receiver.core.internal.management;

import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.processor.manager.core.EventManagementUtil;
import org.wso2.carbon.event.processor.manager.core.EventSync;
import org.wso2.carbon.event.processor.manager.core.Manager;
import org.wso2.carbon.event.receiver.core.internal.ds.EventReceiverServiceValueHolder;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.util.snapshot.ByteSerializer;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class QueueInputEventDispatcher extends AbstractInputEventDispatcher implements EventSync {

    private final StreamDefinition streamDefinition;
    private Logger log = Logger.getLogger(AbstractInputEventDispatcher.class);
    private final BlockingEventQueue eventQueue;
    private final BlockingWso2EventQueue wso2EventQueue;
    private Lock readLock;
    private String syncId;
    private int tenantId;
    private ReentrantLock threadBarrier = new ReentrantLock();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean arbitraryMapsEnabled = false;

    public QueueInputEventDispatcher(int tenantId, String syncId, Lock readLock,
                                     org.wso2.carbon.databridge.commons.StreamDefinition exportedStreamDefinition,
                                     int eventQueueSizeMb, int eventSyncQueueSize, boolean arbitraryMapsEnabled) {
        this.readLock = readLock;
        this.tenantId = tenantId;
        this.syncId = syncId;
        this.arbitraryMapsEnabled = arbitraryMapsEnabled;
        if (!arbitraryMapsEnabled) {
            this.eventQueue = new BlockingEventQueue(eventQueueSizeMb, eventSyncQueueSize);
            this.wso2EventQueue = null;
        } else {
            this.eventQueue = null;
            this.wso2EventQueue = new BlockingWso2EventQueue(eventQueueSizeMb, eventSyncQueueSize);
        }
        this.streamDefinition = EventManagementUtil.constructStreamDefinition(syncId, exportedStreamDefinition);
        if (!arbitraryMapsEnabled) {
            executorService.submit(new QueueInputEventDispatcherWorker());
        } else {
            executorService.submit(new QueueInputWso2EventDispatcherWorker());
        }
    }

    @Override
    public void onEvent(Event event) {
        try {
            threadBarrier.lock();
            eventQueue.put(event);
            threadBarrier.unlock();
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting to put the event to queue.", e);
        }
    }

    @Override
    public void onEvent(org.wso2.carbon.databridge.commons.Event event) {
        try {
            threadBarrier.lock();
            wso2EventQueue.put(event);
            threadBarrier.unlock();
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting to put the event to queue.", e);
        }
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }

    @Override
    public byte[] getState() {
        threadBarrier.lock();
        byte[] state;
        if (!arbitraryMapsEnabled) {
            state = ByteSerializer.OToB(eventQueue);
        } else {
            state = ByteSerializer.OToB(wso2EventQueue);
        }
        threadBarrier.unlock();
        return state;
    }

    @Override
    public void syncState(byte[] bytes) {
        if (!arbitraryMapsEnabled) {
            BlockingEventQueue events = (BlockingEventQueue) ByteSerializer.BToO(bytes);
            while (events.peek() != null) {
                if (events.poll().equals(eventQueue.peek())) {
                    eventQueue.poll();
                } else {
                    break;
                }
            }
        } else {
            BlockingWso2EventQueue events = (BlockingWso2EventQueue) ByteSerializer.BToO(bytes);
            while (events.peek() != null) {
                if (events.poll().equals(wso2EventQueue.peek())) {
                    wso2EventQueue.poll();
                } else {
                    break;
                }
            }
        }
    }

    @Override
    public void process(Event event) {
        readLock.lock();
        readLock.unlock();
        callBack.sendEvent(event);
    }

    @Override
    public StreamDefinition getStreamDefinition() {
        return streamDefinition;
    }

    private class QueueInputEventDispatcherWorker implements Runnable {

        @Override
        public void run() {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
                while (true) {
                    try {
                        readLock.lock();
                        readLock.unlock();
                        Event event = eventQueue.take();
                        readLock.lock();
                        readLock.unlock();
                        callBack.sendEvent(event);
                        if (isSendToOther()) {
                            EventReceiverServiceValueHolder.getEventManagementService().syncEvent(syncId, Manager.ManagerType.Receiver, event);
                        }
                    } catch (InterruptedException e) {
                        log.error("Interrupted while waiting to get an event from queue.", e);
                    }
                }
            } catch (Exception e) {
                log.error("Error in dispatching events:" + e.getMessage(), e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    private class QueueInputWso2EventDispatcherWorker implements Runnable {

        @Override
        public void run() {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
                while (true) {
                    try {
                        readLock.lock();
                        readLock.unlock();
                        org.wso2.carbon.databridge.commons.Event event = wso2EventQueue.take();
                        readLock.lock();
                        readLock.unlock();
                        callBack.sendEvent(event);
                        if (isSendToOther()) {
                            EventReceiverServiceValueHolder.getEventManagementService().syncEvent(syncId, Manager.ManagerType.Receiver, event);
                        }
                    } catch (InterruptedException e) {
                        log.error("Interrupted while waiting to get an event from queue.", e);
                    }
                }
            } catch (Exception e) {
                log.error("Error in dispatching events:" + e.getMessage(), e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }
}
