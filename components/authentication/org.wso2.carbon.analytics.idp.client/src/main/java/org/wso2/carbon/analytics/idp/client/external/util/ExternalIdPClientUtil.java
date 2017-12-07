/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.analytics.idp.client.external.util;

import org.wso2.carbon.analytics.idp.client.core.utils.config.IdPClientConfiguration;
import org.wso2.carbon.database.query.manager.QueryProvider;
import org.wso2.carbon.database.query.manager.config.Queries;
import org.wso2.carbon.database.query.manager.exception.QueryMappingNotAvailableException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Query Manager.
 */
public class ExternalIdPClientUtil {

    public static Map<String, String> getQueries(String databaseType, String databaseVersion,
                                                 List<Queries> deploymentQueries)
            throws QueryMappingNotAvailableException, IOException {
        try {
            List<Queries> componentQueries;
            URL url = ExternalIdPClientUtil.class.getClassLoader().getResource("queries.yaml");
            if (url != null) {
                IdPClientConfiguration idPClientConfiguration = readYamlContent(url.openStream());
                componentQueries = idPClientConfiguration.getQueries();
            } else {
                throw new RuntimeException("Unable to load queries.yaml file.");
            }
            return QueryProvider.mergeMapping(databaseType, databaseVersion, componentQueries, deploymentQueries);
        } catch (QueryMappingNotAvailableException e) {
            throw new QueryMappingNotAvailableException("Unable to load queries.", e);
        } catch (IOException e) {
            throw new IOException("Unable to load content from queries.yaml file.", e);
        }
    }

    private static IdPClientConfiguration readYamlContent(InputStream yamlContent) {
        Yaml yaml = new Yaml(new CustomClassLoaderConstructor(IdPClientConfiguration.class,
                IdPClientConfiguration.class.getClassLoader()));
        yaml.setBeanAccess(BeanAccess.FIELD);
        return yaml.loadAs(yamlContent, IdPClientConfiguration.class);
    }

}
