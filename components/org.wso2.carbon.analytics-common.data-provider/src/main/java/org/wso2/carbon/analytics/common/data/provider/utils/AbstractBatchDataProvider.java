package org.wso2.carbon.analytics.common.data.provider.utils;

import org.wso2.carbon.analytics.common.data.provider.exception.DataProviderException;
import org.wso2.carbon.analytics.common.data.provider.spi.DataProvider;
import org.wso2.carbon.analytics.common.data.provider.spi.ProviderConfig;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by sajithd on 11/21/17.
 */
public abstract class AbstractBatchDataProvider implements DataProvider {
    private String sessionID;
    private ScheduledExecutorService dataPublishingExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService dataPurgingExecutorService = Executors.newSingleThreadScheduledExecutor();
    private long publishingPollingInterval;
    private long purgingPollingInterval;
    private boolean isPurgingEnable;

    @Override
    public DataProvider init(String sessionID, ProviderConfig providerConfig)
            throws DataProviderException {
        if(configValidator(providerConfig)){
            this.sessionID = sessionID;
            this.publishingPollingInterval = providerConfig.getPublishingInterval();
            this.purgingPollingInterval = providerConfig.getPurgingInterval();
            this.isPurgingEnable = providerConfig.isPurgingEnable();
            setProviderConfig(providerConfig);
            return this;
        } else {
            throw new DataProviderException("Invalid configuration provided. Unable to complete initialization " +
                    "of batch data provider.");
        }
    }

    @Override
    public void stop() {
        dataPublishingExecutorService.shutdown();
        dataPurgingExecutorService.shutdown();
    }

    @Override
    public void start() {
        dataPublishingExecutorService.scheduleAtFixedRate(() -> {
            publish(this.sessionID);
        }, 0, publishingPollingInterval, TimeUnit.SECONDS);
        if (isPurgingEnable) {
            dataPublishingExecutorService.scheduleAtFixedRate(() -> {
                purging();
            }, 0, purgingPollingInterval, TimeUnit.SECONDS);
        }
    }

    @Override
    public abstract boolean configValidator(ProviderConfig providerConfig);

    public abstract void publish(String sessionID);

    public abstract void purging();

    public abstract void setProviderConfig(ProviderConfig providerConfig);
}
