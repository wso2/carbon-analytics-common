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
*/
package org.wso2.carbon.databridge.agent;

import org.apache.log4j.Logger;
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.databridge.agent.conf.Agent;
import org.wso2.carbon.databridge.agent.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.conf.DataAgentConfigurationFileResolver;
import org.wso2.carbon.databridge.agent.conf.DataAgentsConfiguration;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.agent.internal.DataAgentServiceValueHolder;
import org.wso2.carbon.utils.Utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.wso2.carbon.databridge.agent.util.DataEndpointConstants.DATABRIDGE_CONFIG_NAMESPACE;
import static org.wso2.carbon.databridge.agent.util.DataEndpointConstants.DATABRIDGE_SENDER_CONFIG_NAMESPACE;
import static org.wso2.carbon.databridge.agent.util.DataEndpointConstants.DATA_AGENT_CONFIG_NAMESPACE;
import static org.wso2.carbon.databridge.agent.util.DataEndpointConstants.TRANSPORTS_NAMESPACE;

/**
 * The holder for all Agents created and this is singleton class.
 * The Agents will be loaded by reading a configuration file data.agent.config.yaml default.
 */

public class AgentHolder {

    private static final Logger log = Logger.getLogger(AgentHolder.class);
    private static String configPath;
    private static AgentHolder instance;
    private Map<String, DataEndpointAgent> dataEndpointAgents;
    /**
     * If there is no data publisher type is passed from,then the default Agent/Publisher will be used.
     * The first element in the data.agent.config.yaml is taken as default data publisher type.
     */
    private String defaultDataEndpointAgentName;

    private AgentHolder() throws DataEndpointAgentConfigurationException {
        try {
            dataEndpointAgents = new HashMap<String, DataEndpointAgent>();
            DataAgentsConfiguration dataAgentsConfiguration = loadConfiguration();
            boolean isDefault = true;
            for (Agent agent : dataAgentsConfiguration.getAgents()) {
                addAgentConfiguration(agent.getAgentConfiguration(), isDefault);
                if (isDefault) {
                    isDefault = false;
                }
            }
        } catch (DataEndpointAgentConfigurationException e) {
            log.error("Unable to complete initialization of agents." + e.getMessage(), e);
            throw e;
        }
    }

    public static synchronized AgentHolder getInstance() throws DataEndpointAgentConfigurationException {
        if (instance == null) {
            instance = new AgentHolder();
        }
        return instance;
    }

    /**
     * Set the data.agent.config.yaml path from which the Agents for all endpoint types will be loaded.
     * This is a one time operation, and if you are changing form default config path,
     * then it needs to be done as first step when the JVM started.
     *
     * @param configPath The path of the data-bridge-conf.xml
     */
    public static void setConfigPath(String configPath) {
        AgentHolder.configPath = configPath;
    }

    public static synchronized void shutdown() throws DataEndpointException {
        if (instance != null) {
            for (DataEndpointAgent dataEndpointAgent : instance.dataEndpointAgents.values()) {
                dataEndpointAgent.shutDown();
            }
            instance = null;
        }
    }

    public synchronized DataEndpointAgent getDataEndpointAgent(String type)
            throws DataEndpointAgentConfigurationException {
        DataEndpointAgent agent = this.dataEndpointAgents.get(type.toLowerCase());
        if (agent == null) {
            throw new DataEndpointAgentConfigurationException("No data agent configured for the type: " +
                    type.toLowerCase());
        }
        return agent;
    }

    /**
     * Loading by data.agent.config.yaml, and validating the configurations.
     *
     * @return Loaded DataAgentsConfiguration from config file.
     * @throws DataEndpointAgentConfigurationException Exception to be thrown for DataEndpointAgentConfiguration which
     *                                                 was specified in the data.agent.config.yaml.
     */
    private DataAgentsConfiguration loadConfiguration()
            throws DataEndpointAgentConfigurationException {
        DataAgentsConfiguration dataAgentsConfiguration = null;
        if (configPath == null) {
            try {
                ConfigProvider configProvider = DataAgentServiceValueHolder.getConfigProvider();
                dataAgentsConfiguration = getDataAgentsConfiguration(configProvider);
            } catch (ConfigurationException e) {
                throw new DataEndpointAgentConfigurationException("Error in when loading databridge agent " +
                        "configuration", e);
            }
        } else {
            try {
                Path dataAgentConfigPath = Paths.get(configPath);
                if (Files.exists(dataAgentConfigPath)) {
                    ConfigProvider configProvider = ConfigProviderFactory.getConfigProvider(dataAgentConfigPath);
                    dataAgentsConfiguration = getDataAgentsConfiguration(configProvider);
                } else {
                    throw new DataEndpointAgentConfigurationException("Cannot find the databridge agent " +
                            "configuration file in the specified path");
                }
            } catch (ConfigurationException e) {
                throw new DataEndpointAgentConfigurationException("Error in when loading databridge agent " +
                        "configuration", e);
            }

        }

        if (dataAgentsConfiguration != null) {
            for (Agent agent : dataAgentsConfiguration.getAgents()) {
                AgentConfiguration agentConfiguration = agent.getAgentConfiguration();

                if (agentConfiguration.getTrustStorePath() == null ||
                        agentConfiguration.getTrustStorePath().isEmpty()) {
                    agentConfiguration.setTrustStorePath(System.getProperty("javax.net.ssl.trustStore"));
                    if (agentConfiguration.getTrustStorePath() == null) {
                        if (DataAgentServiceValueHolder.getConfigProvider() == null) {
                            throw new DataEndpointAgentConfigurationException("No trustStore found for " +
                                    agentConfiguration.getName() + " Agent.");
                        }

                        String defaultTrustStore = Utils.getCarbonHome() + File.separator + "resources" +
                                File.separator + "security" + File.separator + "client-truststore.jks";
                        Path defaultTrustStoreFilePath = Paths.get(defaultTrustStore);
                        if (Files.exists(defaultTrustStoreFilePath)) {
                            agentConfiguration.setTrustStorePath(defaultTrustStore);
                        } else {
                            throw new DataEndpointAgentConfigurationException("No trustStore found for " +
                                    agentConfiguration.getName() + " Agent.");
                        }
                    }
                }

                if (agentConfiguration.getTrustStorePassword() == null ||
                        agentConfiguration.getTrustStorePassword().isEmpty()) {
                    agentConfiguration.setTrustStorePassword(System.getProperty(
                            "javax.net.ssl.trustStorePassword"));
                    if (agentConfiguration.getTrustStorePassword() == null) {
                        throw new DataEndpointAgentConfigurationException("No trustStore password found for " +
                                agentConfiguration.getName() + " Agent.");
                    }
                }

            }
        }
        return dataAgentsConfiguration;
    }

    private DataAgentsConfiguration getDataAgentsConfiguration(ConfigProvider configProvider)
            throws ConfigurationException, DataEndpointAgentConfigurationException {
        DataAgentsConfiguration dataAgentsConfiguration;
        LinkedHashMap transportConf = ((LinkedHashMap) configProvider.getConfigurationObject(TRANSPORTS_NAMESPACE));

        if (transportConf != null) {
            LinkedHashMap dataBridgeConfig = ((LinkedHashMap) transportConf.get(DATABRIDGE_CONFIG_NAMESPACE));
            if (dataBridgeConfig != null) {
                LinkedHashMap senderConf = ((LinkedHashMap) dataBridgeConfig.get(DATABRIDGE_SENDER_CONFIG_NAMESPACE));
                if (senderConf != null) {
                    dataAgentsConfiguration = DataAgentConfigurationFileResolver.
                            resolveAndSetDataAgentConfiguration(senderConf);
                } else {
                    dataAgentsConfiguration = new DataAgentsConfiguration();
                }
            } else {
                dataAgentsConfiguration = new DataAgentsConfiguration();
            }

        } else if (configProvider.getConfigurationObject(DATA_AGENT_CONFIG_NAMESPACE) != null) {
            dataAgentsConfiguration = DataAgentConfigurationFileResolver.
                    resolveAndSetDataAgentConfiguration
                            ((LinkedHashMap) configProvider.
                                    getConfigurationObject(DATA_AGENT_CONFIG_NAMESPACE));
        } else {
            dataAgentsConfiguration = new DataAgentsConfiguration();
        }
        return dataAgentsConfiguration;
    }

    private void addAgentConfiguration(AgentConfiguration agentConfiguration, boolean defaultAgent)
            throws DataEndpointAgentConfigurationException {
        DataEndpointAgent agent = new DataEndpointAgent(agentConfiguration);
        dataEndpointAgents.put(agent.getAgentConfiguration().getName().toLowerCase(), agent);
        if (defaultAgent) {
            defaultDataEndpointAgentName = agent.getAgentConfiguration().getName();
        }
    }

    /**
     * Returns the default agent,and the first element in the data.agent.config.yaml
     * is taken as default data publisher type.
     *
     * @return DataEndpointAgent for the default endpoint name.
     * @throws DataEndpointAgentConfigurationException Exception to be thrown for DataEndpointAgentConfiguration
     *                                                 which was specified in the data.agent.config.yaml.
     */
    public DataEndpointAgent getDefaultDataEndpointAgent() throws DataEndpointAgentConfigurationException {
        return getDataEndpointAgent(defaultDataEndpointAgentName);
    }
}
