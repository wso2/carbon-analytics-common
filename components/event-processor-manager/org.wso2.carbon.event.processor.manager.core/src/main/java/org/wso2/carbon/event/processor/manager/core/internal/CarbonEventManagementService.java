/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.event.processor.manager.core.internal;

import com.hazelcast.map.IMap;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.core.HazelcastInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.event.processor.manager.core.EventManagementService;
import org.wso2.carbon.event.processor.manager.core.EventProcessorManagementService;
import org.wso2.carbon.event.processor.manager.core.EventPublisherManagementService;
import org.wso2.carbon.event.processor.manager.core.EventReceiverManagementService;
import org.wso2.carbon.event.processor.manager.core.EventSync;
import org.wso2.carbon.event.processor.manager.core.Manager;
import org.wso2.carbon.event.processor.manager.core.config.DistributedConfiguration;
import org.wso2.carbon.event.processor.manager.core.config.HAConfiguration;
import org.wso2.carbon.event.processor.manager.core.config.ManagementModeInfo;
import org.wso2.carbon.event.processor.manager.core.config.Mode;
import org.wso2.carbon.event.processor.manager.core.config.PersistenceConfiguration;
import org.wso2.carbon.event.processor.manager.core.exception.EventManagementException;
import org.wso2.carbon.event.processor.manager.core.exception.ManagementConfigurationException;
import org.wso2.carbon.event.processor.manager.core.internal.ds.EventManagementServiceValueHolder;
import org.wso2.carbon.event.processor.manager.core.internal.util.ConfigurationConstants;
import org.wso2.carbon.event.processor.manager.core.internal.util.ManagementModeConfigurationLoader;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CarbonEventManagementService implements EventManagementService {

    private static Log log = LogFactory.getLog(CarbonEventManagementService.class);

    private Mode mode = Mode.SingleNode;
    private ManagementModeInfo managementModeInfo;

    private EventProcessorManagementService processorManager;
    private EventReceiverManagementService receiverManager;
    private List<EventPublisherManagementService> publisherManager;

    private EventHandler receiverEventHandler = new EventHandler();
    private EventHandler presenterEventHandler = new EventHandler();

    private ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(3);

    private HAManager haManager = null;
    private IMap<String, Long> haEventPublisherTimeSyncMap = null;

    private PersistenceManager persistenceManager = null;

    private StormReceiverCoordinator stormReceiverCoordinator = null;

    private boolean isManagerNode = false;
    private boolean isWorkerNode = false;
    private boolean isPresenterNode = false;


    public CarbonEventManagementService() {
        try {
            managementModeInfo = ManagementModeConfigurationLoader.loadManagementModeInfo();
            mode = managementModeInfo.getMode();
            publisherManager = new CopyOnWriteArrayList<>();
        } catch (ManagementConfigurationException e) {
            throw new EventManagementException("Error getting management mode information", e);
        }
        if (mode == Mode.SingleNode) {
            PersistenceConfiguration persistConfig = managementModeInfo.getPersistenceConfiguration();
            if (persistConfig != null) {
                ScheduledExecutorService scheduledExecutorService = Executors
                        .newScheduledThreadPool(persistConfig.getThreadPoolSize());
                long persistenceTimeInterval = persistConfig.getPersistenceTimeInterval();
                if (persistenceTimeInterval > 0) {
                    persistenceManager = new PersistenceManager(scheduledExecutorService, persistenceTimeInterval);
                    persistenceManager.init();
                }
            }
        }
    }

    public void init(HazelcastInstance hazelcastInstance) {
        if (mode == Mode.SingleNode) {
            log.warn("CEP started with clustering enabled, but SingleNode configuration given.");
        }

        hazelcastInstance.getCluster().addMembershipListener(new MembershipListener() {
            @Override
            public void memberAdded(MembershipEvent membershipEvent) {
                presenterEventHandler.registerLocalMember();
                receiverEventHandler.registerLocalMember();
                checkMemberUpdate();
            }

            @Override
            public void memberRemoved(MembershipEvent membershipEvent) {
                receiverEventHandler.removeMember(membershipEvent.getMember().getUuid().toString());
                presenterEventHandler.removeMember(membershipEvent.getMember().getUuid().toString());
                checkMemberUpdate();
            }

        });

    }

    private boolean validateHostName(String hostname) {

        return !(hostname.trim().equals("0.0.0.0") || hostname.trim().equals("localhost") ||
                hostname.trim().equals("127.0.0.1") || hostname.trim().equals("::1"));
    }

    public void init(ConfigurationContextService configurationContextService) {
        if ((mode == Mode.SingleNode || isWorkerNode ) && receiverManager != null) {
            receiverManager.start();
        }
        if (mode == Mode.SingleNode && receiverManager != null)  {
            executorService.schedule(new Runnable() {
                @Override
                public void run() {
                    try {
                        log.info("Starting polling event receivers");
                        EventReceiverManagementService eventReceiverManagementService = getEventReceiverManagementService();
                        if (eventReceiverManagementService != null) {
                            eventReceiverManagementService.startPolling();
                        } else {
                            log.error("Adapter polling failed as EventReceiverManagementService not available");
                        }
                    } catch (Exception e) {
                        log.error("Unexpected error occurred when start polling event adapters", e);
                    }
                }
            }, ConfigurationConstants.AXIS_TIME_INTERVAL_IN_MILLISECONDS * 4, TimeUnit.MILLISECONDS);
        }
    }

    public void shutdown() {
        if (haManager != null) {
            haManager.shutdown();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
        if (persistenceManager != null) {
            persistenceManager.shutdown();
        }
        receiverEventHandler.shutdown();
        presenterEventHandler.shutdown();
    }

    public byte[] getState() {
        return null;
    }

    public ManagementModeInfo getManagementModeInfo() {
        return managementModeInfo;
    }

    public void subscribe(Manager manager) {
        if (manager.getType() == Manager.ManagerType.Processor) {
            this.processorManager = (EventProcessorManagementService) manager;
        } else if (manager.getType() == Manager.ManagerType.Receiver) {
            this.receiverManager = (EventReceiverManagementService) manager;
        } else if (manager.getType() == Manager.ManagerType.Publisher) {
            this.publisherManager.add((EventPublisherManagementService) manager);
        }
    }

    @Override
    public void unsubscribe(Manager manager) {
        if (manager.getType() == Manager.ManagerType.Processor) {
            this.processorManager = null;
        } else if (manager.getType() == Manager.ManagerType.Receiver) {
            this.receiverManager = null;
        } else if (manager.getType() == Manager.ManagerType.Publisher) {
            this.publisherManager.remove(manager);
        }
    }

    @Override
    public void syncEvent(String syncId, Manager.ManagerType type, Event event) {
        if (type == Manager.ManagerType.Receiver) {
            receiverEventHandler.syncEvent(syncId, event);
        } else {
            presenterEventHandler.syncEvent(syncId, event);
        }
    }

    @Override
    public void registerEventSync(EventSync eventSync, Manager.ManagerType type) {
        if (type == Manager.ManagerType.Receiver) {
            receiverEventHandler.registerEventSync(eventSync);
        } else {
            presenterEventHandler.registerEventSync(eventSync);
        }
    }

    @Override
    public void unregisterEventSync(String syncId, Manager.ManagerType type) {
        if (type == Manager.ManagerType.Receiver) {
            receiverEventHandler.unregisterEventSync(syncId);
        } else {
            presenterEventHandler.unregisterEventSync(syncId);
        }
    }

    public EventProcessorManagementService getEventProcessorManagementService() {
        return processorManager;
    }

    public EventReceiverManagementService getEventReceiverManagementService() {
        return receiverManager;
    }

    public List<EventPublisherManagementService> getEventPublisherManagementService() {
        return publisherManager;
    }

    private void checkMemberUpdate() {
        // Distributed and HA has been removed
    }


    @Override
    public void updateLatestEventSentTime(String publisherName, int tenantId, long timestamp) {
        haEventPublisherTimeSyncMap.putAsync(tenantId + "-" + publisherName, EventManagementServiceValueHolder.getHazelcastInstance().getCluster().getClusterTime());
    }

    @Override
    public long getLatestEventSentTime(String publisherName, int tenantId) {
        if (haEventPublisherTimeSyncMap == null) {
            haEventPublisherTimeSyncMap = EventManagementServiceValueHolder.getHazelcastInstance()
                    .getMap(ConfigurationConstants.HA_EVENT_PUBLISHER_TIME_SYNC_MAP);
        }
        Long latestTimePublished = haEventPublisherTimeSyncMap.get(tenantId + "-" + publisherName);
        if (latestTimePublished != null) {
            return latestTimePublished;
        }
        return 0;
    }

    @Override
    public long getClusterTimeInMillis() {

        if (EventManagementServiceValueHolder.getHazelcastInstance() == null) {
            throw new RuntimeException("No HazelcastInstance found.");
        }

        if (EventManagementServiceValueHolder.getHazelcastInstance().getCluster() ==  null) {
            throw new RuntimeException("No Cluster was found in the HazelcastInstance.");
        }

        try {
            return EventManagementServiceValueHolder.getHazelcastInstance().getCluster().getClusterTime();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void initPersistence() {
        if (persistenceManager != null) {
            persistenceManager.init();
        }
    }

    public void stopPersistence() {
        if (persistenceManager != null) {
            persistenceManager.shutdown();
        }
    }
}
