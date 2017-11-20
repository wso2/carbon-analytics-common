package org.wso2.carbon.analytics.common.data.provider.spi;

import org.wso2.carbon.analytics.common.data.provider.exception.DataProviderException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by sajithd on 11/21/17.
 */
public abstract class AbstractStreamingDataProvider implements DataProvider {
    private String sessionID;
    private ScheduledExecutorService dataPublishingExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService dataPurgingExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ProviderConfig providerConfig;

    @Override
    public DataProvider init(String sessionID, ProviderConfig providerConfig)
            throws DataProviderException {
        if(configValidator(providerConfig)){
            this.sessionID = sessionID;
            this.providerConfig = providerConfig;
            return this;
        } else {
            throw new DataProviderException("Invalid configuration provided. Unable to complete initialization " +
                    "of streaming data provider.");
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
            publish(this.sessionID, this.providerConfig);
        }, 0, providerConfig.getPublishingPollingInterval(), TimeUnit.SECONDS);
        if (this.providerConfig.isPurgingEnable()) {
            dataPublishingExecutorService.scheduleAtFixedRate(() -> {
                purging(this.providerConfig);
            }, 0, providerConfig.getPurgingPollingInterval(), TimeUnit.SECONDS);
        }
    }

    @Override
    public abstract boolean configValidator(ProviderConfig providerConfig);

    public abstract void publish(String sessionID, ProviderConfig providerConfig);

    public abstract void purging(ProviderConfig providerConfig);
}
