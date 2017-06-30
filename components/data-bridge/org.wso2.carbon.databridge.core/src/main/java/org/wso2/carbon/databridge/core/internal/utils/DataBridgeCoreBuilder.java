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
package org.wso2.carbon.databridge.core.internal.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.core.exception.DataBridgeConfigurationException;
import org.wso2.carbon.utils.Utils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Helper class to build Agent Server Initial Configurations.
 */
public final class DataBridgeCoreBuilder {
    private static final Log log = LogFactory.getLog(DataBridgeCoreBuilder.class);

    private DataBridgeCoreBuilder() {
    }

    // TODO: 2/2/17 stream definitions are temporarily loaded from a file in <product-sp>/deployment
    public static List<String> loadStreamDefinitionYAML() throws DataBridgeConfigurationException {
        List<String> streamDefinitionList = new ArrayList<String>();
        String path = Utils.getCarbonHome().toString() + File.separator + "deployment" + File.separator +
                "stream-definitions.yaml";
        Yaml yaml = new Yaml();
        File file = new File(path);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            Map<String, List<String>> streams = (Map<String, List<String>>) yaml.load(fileInputStream);
            streamDefinitionList.addAll(streams.get(DataBridgeConstants.STREAM_DEFINITIONS_ELEMENT));
        } catch (FileNotFoundException e) {
            log.error("File " + path + " could not be found", e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    log.error("Error when loading stream definition yaml file", e);
                }
            }

        }
        return streamDefinitionList;
    }
}
