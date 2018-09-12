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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.database.query.manager.config.Queries;
import org.wso2.carbon.database.query.manager.exception.QueryMappingNotAvailableException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The query mapping provider which mapping the queries of default map with component default config and
 * deployment.yaml config.
 */
public class QueryProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryProvider.class);
    private static final String DEFAULT_TYPE = "default";

    /**
     * Merge the query mapping for component and deployment queries.
     *
     * @param databaseType      database type for query template lookup.
     * @param databaseVersion   database version for query template lookup.
     * @param componentQueries  queries array load from component queries config file.
     * @param deploymentQueries queries array load from carbon deployment config file.
     * @return results map of the component and deployment queries mapping.
     * @throws QueryMappingNotAvailableException
     */
    public static Map<String, String> mergeMapping(String databaseType, String databaseVersion,
                                                   List<Queries> componentQueries,
                                                   List<Queries> deploymentQueries)
            throws QueryMappingNotAvailableException {
        Set<String> defaultConfigSet = new HashSet<>();
        Map<String, String> deploymentConfigMap = new HashMap<>();
        Map<String, String> componentConfigMap = new HashMap<>();
        Map<String, String> result = new HashMap<>();
        if (deploymentQueries != null) {
            //populate the deployment.yaml RDBMS queries map for given database type and version.
            for (Queries queries : deploymentQueries) {
                String queriesType = queries.getType();
                String queriesVersion = queries.getVersion();
                if ((queriesType.equalsIgnoreCase(databaseType) || queriesType.equalsIgnoreCase(DEFAULT_TYPE)) &&
                        (queriesVersion.equalsIgnoreCase(databaseVersion) ||
                                queriesVersion.equalsIgnoreCase(DEFAULT_TYPE))) {
                    deploymentConfigMap = queries.getMappings();
                    break;
                }
            }
        }
        if (componentQueries != null) {
            //populate the default and component RDBMS queries map for given type and version.
            for (Queries queries : componentQueries) {
                String queriesType = queries.getType();
                String queriesVersion = queries.getVersion();
                if (queriesType.equalsIgnoreCase(DEFAULT_TYPE)) {
                    defaultConfigSet = queries.getMappings().keySet();
                } else if (queriesType.equalsIgnoreCase(databaseType)
                        && (queriesVersion.equalsIgnoreCase(databaseVersion) ||
                        queriesVersion.equalsIgnoreCase(DEFAULT_TYPE))) {
                    componentConfigMap = queries.getMappings();
                    break;
                }
            }
        } else {
            throw new QueryMappingNotAvailableException("Component configuration array list is null.");
        }
        if (!defaultConfigSet.isEmpty()) {
            for (String defaultEntry : defaultConfigSet) {
                String value;
                if (deploymentConfigMap != null && !deploymentConfigMap.isEmpty() &&
                        deploymentConfigMap.containsKey(defaultEntry)) {
                    value = deploymentConfigMap.get(defaultEntry);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Override the query : '" + removeCRLFCharacters(defaultEntry) +
                                "' with deployment config value: '" + removeCRLFCharacters(value) +
                                "'  for database type: '" + removeCRLFCharacters(databaseType) + "' and version '" +
                                removeCRLFCharacters(String.valueOf(databaseVersion)) + "'.");
                    }
                } else {
                    if (componentConfigMap != null && !componentConfigMap.isEmpty()
                            && componentConfigMap.containsKey(defaultEntry)) {
                        value = componentConfigMap.get(defaultEntry);
                    } else {
                        throw new QueryMappingNotAvailableException("Mapping value for query: '" +
                                defaultEntry + "' not found in Deployment config Map and Component " +
                                "config Map for database type: '" + databaseType + "' and version '" +
                                databaseVersion + "'.");
                    }
                }
                result.put(defaultEntry, value);
            }
            return result;
        } else {
            throw new QueryMappingNotAvailableException("Default configuration map is null or empty.");
        }
    }

    private static String removeCRLFCharacters(String str) {
        if (str != null) {
            str = str.replace('\n', '_').replace('\r', '_');
        }
        return str;
    }
}
