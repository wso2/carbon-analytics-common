/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package org.wso2.carbon.data.provider.endpoint;


import com.google.gson.Gson;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.data.provider.rdbms.RDBMSBatchDataProvider;
import org.wso2.carbon.data.provider.rdbms.RDBMSStreamingDataProvider;
import org.wso2.carbon.data.provider.rdbms.config.RDBMSDataProviderConf;
import org.wso2.carbon.data.provider.spi.DataProvider;
import org.wso2.carbon.data.provider.spi.ProviderConfig;
import org.wso2.carbon.data.provider.utils.DataProviderValueHolder;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.msf4j.websocket.WebSocketEndpoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * Data provider web socket endpoint.
 */
@Component(
        name = "org.wso2.carbon.analytics.common.data.provider.endpoint",
        service = WebSocketEndpoint.class,
        immediate = true
)
@ServerEndpoint(value = "/data-provider/{sourceType}")
public class DataProviderEndPoint implements WebSocketEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataProviderEndPoint.class);
    private static final Map<String, Session> sessionMap = new HashMap<>();
    private final Map<String, DataProvider> providerMap = new HashMap<>();

    @Reference(
            name = "org.wso2.carbon.datasource.DataSourceService",
            service = DataSourceService.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterDataSourceService"
    )
    protected void registerDataSourceService(DataSourceService service) {
        DataProviderValueHolder.setDataSourceService(service);
    }

    protected void unregisterDataSourceService(DataSourceService service) {
        DataProviderValueHolder.setDataSourceService(null);
    }

    @Reference(service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigProvider")
    protected void setConfigProvider(ConfigProvider configProvider) {
        DataProviderValueHolder.setConfigProvider(configProvider);
    }

    protected void unsetConfigProvider(ConfigProvider configProvider) {
        DataProviderValueHolder.setConfigProvider(null);
    }

    /**
     * Handle initiation of the connection map the session object in the session map.
     *
     * @param session Session object associated with the connection
     */
    @OnOpen
    public void onOpen(Session session) {
        sessionMap.put(session.getId(), session);
    }

    /**
     * Create DataProvider instance, start it and store it in the providerMap.
     *
     * @param message    String message received from the web client
     * @param sourceType datasource type from the Path parameter
     * @param session    Session object associated with the connection
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sourceType") String sourceType, Session session) {
        try {
            DataProvider dataProvider;
            ProviderConfig providerConfig;
            switch (sourceType) {
                case "rdbms-batch":
                    providerConfig = new Gson().fromJson(message, RDBMSDataProviderConf.class);
                    dataProvider = new RDBMSBatchDataProvider().init(session.getId(), providerConfig);
                    dataProvider.start();
                    break;
                case "rdbms-streaming":
                    providerConfig = new Gson().fromJson(message, RDBMSDataProviderConf.class);
                    dataProvider = new RDBMSStreamingDataProvider().init(session.getId(), providerConfig);
                    dataProvider.start();
                    break;
                default:
                    throw new Exception("Provider type: " + sourceType + " not registered.");
            }
            if (providerMap.containsKey(session.getId())) {
                providerMap.get(session.getId()).stop();
            }
            providerMap.put(session.getId(), dataProvider);
        } catch (Exception e) {
            LOGGER.error("Error initializing the data provider endpoint for source type " + sourceType + ". "
                    + e.getMessage(), e);
            onError(e);
        }
    }

    /**
     * handle disconnection with the client.
     *
     * @param session Session object associated with the connection
     */
    @OnClose
    public void onClose(Session session) {
        providerMap.get(session.getId()).stop(); //stop the pushing service
        providerMap.remove(session.getId()); //remove the provider from the map
        sessionMap.remove(session.getId()); //remove the session from sessionMap
    }

    /**
     * handle on error.
     */
    @OnError
    public void onError(Throwable throwable) {
        LOGGER.error("Error found in method : " + throwable.toString());
    }

    /**
     * Send message to specific client.
     *
     * @param text      String message to be sent to the client
     * @param sessionId String session id of the session
     * @throws IOException If there is a problem delivering the message
     */
    public static void sendText(String text, String sessionId) throws IOException {
        sessionMap.get(sessionId).getBasicRemote().sendText(text);
    }
}
