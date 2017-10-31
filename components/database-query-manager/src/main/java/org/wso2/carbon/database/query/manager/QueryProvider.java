/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.database.query.manager;

import org.apache.log4j.Logger;
import org.wso2.carbon.database.query.manager.config.Queries;
import org.wso2.carbon.database.query.manager.exception.QueryMappingNotAvailableException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The query mapping provider which mapping the queries of default map with component default config and
 * deployment.yaml config.
 */
public class QueryProvider {
    private static final Logger LOGGER = Logger.getLogger(QueryProvider.class);
    private static final String DEFAULT_TYPE = "default";

    public QueryProvider() {
    }

    public static Map<String, String> mergeMapping(String databaseType, String databaseVersion,
                                                   ArrayList<Queries> componentQueries,
                                                   ArrayList<Queries> deploymentQueries)
            throws QueryMappingNotAvailableException {
        Map<String, String> defaultConfigMap = new HashMap<>();
        Map<String, String> deploymentConfigMap = new HashMap<>();
        Map<String, String> componentConfigMap = new HashMap<>();
        Map<String, String> result = new HashMap<>();
        for (Queries queries : deploymentQueries) {
            if (queries.getType().equals(databaseType) && queries.getVersion() == null ?
                    databaseVersion == null : queries.getVersion().equals(databaseVersion)) {
                deploymentConfigMap = queries.getMappings();
            }
        }
        for (Queries queries : componentQueries) {
            if (queries.getType().equals(DEFAULT_TYPE) && queries.getVersion() == null) {
                defaultConfigMap = queries.getMappings();
            } else if (queries.getType().equals(databaseType) && queries.getVersion() == null ?
                    databaseVersion == null : queries.getVersion().equals(databaseVersion)) {
                componentConfigMap = queries.getMappings();
            }
        }
        if (defaultConfigMap != null && !defaultConfigMap.isEmpty()) {
            for (Map.Entry<String, String> defaultEntry : defaultConfigMap.entrySet()) {
                String value;
                if (deploymentConfigMap != null && !deploymentConfigMap.isEmpty() &&
                        deploymentConfigMap.containsKey(defaultEntry.getKey())) {
                    value = deploymentConfigMap.get(defaultEntry.getKey());
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Override the query : '" + defaultEntry.getKey() + "' with deployment " +
                                "config value: '" + value + "'  for database type: '" + databaseType
                                + "' and version '" + databaseVersion + "'.");
                    }
                } else {
                    if (componentConfigMap != null && !componentConfigMap.isEmpty()
                            && componentConfigMap.containsKey(defaultEntry.getKey())) {
                        value = componentConfigMap.get(defaultEntry.getKey());
                    } else {
                        throw new QueryMappingNotAvailableException("Mapping value for query: '" +
                                defaultEntry.getKey() + "' not found in Deployment config Map and Component " +
                                "config Map for database type: '" + databaseType + "' and version '" +
                                databaseVersion + "'.");
                    }
                }
                result.put(defaultEntry.getKey(), value);
            }
            return result;
        } else {
            throw new QueryMappingNotAvailableException("Default configuration map is null or empty.");
        }
    }
}
