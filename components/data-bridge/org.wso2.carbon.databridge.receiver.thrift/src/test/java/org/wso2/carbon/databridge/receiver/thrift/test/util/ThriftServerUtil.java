/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.databridge.receiver.thrift.test.util;

import org.apache.log4j.Logger;
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.core.conf.DataBridgeConfiguration;
import org.wso2.carbon.databridge.core.conf.DatabridgeConfigurationFileResolver;
import org.wso2.carbon.databridge.core.exception.DataBridgeConfigurationException;
import org.wso2.carbon.databridge.core.internal.utils.DataBridgeConstants;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

/**
 * Util class for Thrift Receiver Test Cases.
 */
public class ThriftServerUtil {

    private static Logger log = Logger.getLogger(ThriftServerUtil.class);

    public static final Path TEST_DIR = Paths.get("src", "test", "resources");

    public static DataBridgeConfiguration getDataBridgeConfiguration(String file) throws
            DataBridgeConfigurationException {

        String configPath = TEST_DIR + File.separator + file;

        Path configFile = Paths.get(configPath);
        DataBridgeConfiguration dataBridgeConfiguration;
        try {
            if (Files.exists(configFile)) {
                ConfigProvider configProvider = ConfigProviderFactory.getConfigProvider(configFile);
                dataBridgeConfiguration = DatabridgeConfigurationFileResolver.
                        resolveAndSetDatabridgeConfiguration((LinkedHashMap) configProvider.
                                getConfigurationObject(DataBridgeConstants.DATABRIDGE_CONFIG_NAMESPACE));
                return dataBridgeConfiguration;
            } else {
                log.error("Cannot find data bridge configuration file : " + configPath);
                return null;
            }
        } catch (ConfigurationException e) {
            throw new DataBridgeConfigurationException("Error in when loading databridge configuration", e);
        }
    }

    public static void setupCarbonConfig(String tenantName) {
        System.setProperty("carbon.home", Paths.get(TEST_DIR.toString(), "carbon-context").toString());
        System.setProperty("tenant.name", tenantName);
    }

    public static StreamDefinition getSampleStreamDefinition(String streamName, String version) {
        StreamDefinition streamDefinition = null;
        try {
            streamDefinition = new StreamDefinition(streamName, version);
        } catch (MalformedStreamDefinitionException e) {
            log.error("Error Creating Stream Definition");
        }
        streamDefinition.addMetaData("ipAdd", AttributeType.STRING);
        streamDefinition.addPayloadData("symbol", AttributeType.STRING);
        streamDefinition.addPayloadData("price", AttributeType.DOUBLE);
        streamDefinition.addPayloadData("volume", AttributeType.INT);
        streamDefinition.addPayloadData("max", AttributeType.DOUBLE);
        streamDefinition.addPayloadData("min", AttributeType.DOUBLE);

        return streamDefinition;
    }
}
