/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
*
*/
package org.wso2.carbon.event.receiver.core.internal.management;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.event.receiver.core.internal.util.EventReceiverUtil;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
/**
 * This implementation is mostly referenced from the Java LinkedBlockingQueue implementation http://grepcode
 * .com/file/repository.grepcode.com/java/root/jdk/openjdk/8u40-b25/java/util/concurrent/LinkedBlockingQueue.java
 */
public class BlockingEventQueue implements Serializable {

    private static final Log log = LogFactory.getLog(BlockingEventQueue.class);
    private final ReentrantLock takeLock = new ReentrantLock();
    private final ReentrantLock putLock = new ReentrantLock();
    private final Condition notFull = putLock.newCondition();
    private final Condition notEmpty = takeLock.newCondition();
    private int maxSizeInBytes;
    private BlockingQueue<WrappedEvent> queue;
    private AtomicInteger currentSize;
    private int currentEventSize;

    public BlockingEventQueue(int maxSizeInMb, int maxNumOfEvents) {
        this.maxSizeInBytes = maxSizeInMb * 1000000; // to convert to bytes
        this.queue = new LinkedBlockingQueue<>(maxNumOfEvents);
        this.currentSize = new AtomicInteger(0);
        this.currentEventSize = 0;
    }

    public void put(Event event) throws InterruptedException {
        final ReentrantLock putLock = this.putLock;
        final AtomicInteger currentSize = this.currentSize;
        int c = -1;

        putLock.lockInterruptibly();
        try {
            this.currentEventSize = EventReceiverUtil.getSize(event) + 4; //for the int value for size field.
            while (currentSize.get() >= maxSizeInBytes) {
                // waits if the queue has exceeded the max size
                notFull.await();
            }
            this.queue.put(new WrappedEvent(this.currentEventSize, event));
            c = currentSize.getAndAdd(this.currentEventSize);

        } finally {
            putLock.unlock();
        }
        if (c == 0) {
            // if the queue was empty, signal that it is non-empty now
            signalNotEmpty();
        }
        if (log.isDebugEnabled()) {
            log.debug("Current queue size in bytes : " + currentSize + ", remaining capacity : " +
                    this.queue.remainingCapacity());
        }
    }

    public Event take() throws InterruptedException {
        WrappedEvent wrappedEvent;
        int c = -1;
        final ReentrantLock takeLock = this.takeLock;
        final AtomicInteger currentSize = this.currentSize;

        takeLock.lockInterruptibly();
        try {
            while (currentSize.get() == 0) {
                // waits if the queue is empty
                notEmpty.await();
            }
            wrappedEvent = this.queue.take();
            c = currentSize.getAndAdd(-wrappedEvent.getSize());

        } finally {
            takeLock.unlock();
        }
        if (c >= maxSizeInBytes) {
            // if the queue previously had reached its max, signal that now it's not
            signalNotFull();
        }
        return wrappedEvent.getEvent();
    }
    

    public Event poll() {
        final AtomicInteger currentSize = this.currentSize;
        if (currentSize.get() == 0) {
            return null;
        }
        WrappedEvent wrappedEvent = null;
        int c = -1;
        final ReentrantLock takeLock = this.takeLock;

        takeLock.lock();
        try {
            if (currentSize.get() > 0) {
                wrappedEvent = this.queue.poll();
                if (wrappedEvent != null) {
                    c = currentSize.getAndAdd(-wrappedEvent.getSize());

                    if (currentSize.get() > 0) {
                        // if the queue is still not empty, signal that.
                        notEmpty.signal();
                    }
                }
            }
        } finally {
            takeLock.unlock();
        }
        if (wrappedEvent != null && c >= maxSizeInBytes) {
            // if the queue previously had reached its max, signal that now it's not
            signalNotFull();
        }
        return wrappedEvent == null ? null : wrappedEvent.getEvent();
    }

    public Event peek() {
        WrappedEvent wrappedEvent = this.queue.peek();
        if (wrappedEvent != null) {
            return wrappedEvent.getEvent();
        } else {
            return null;
        }
    }

    /**
     * Signals a waiting take. Called only from put.
     */
    private void signalNotEmpty() {
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
    }

    /**
     * Signals a waiting put. Called only from take/poll.
     */
    private void signalNotFull() {
        final ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            notFull.signal();
        } finally {
            putLock.unlock();
        }
    }


    private class WrappedEvent implements Serializable{
        private int size;
        private Event event;

        public WrappedEvent(int size, Event event) {
            this.size = size;
            this.event = event;
        }

        public int getSize() {
            return size;
        }

        public Event getEvent() {
            return event;
        }
    }
}
