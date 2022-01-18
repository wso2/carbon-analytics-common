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

import feign.codec.EncodeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.wso2.carbon.analytics.idp.client.core.utils.IdPClientConstants;
import org.wso2.carbon.analytics.idp.client.core.utils.config.IdPClientConfiguration;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.database.query.manager.QueryProvider;
import org.wso2.carbon.database.query.manager.config.Queries;
import org.wso2.carbon.database.query.manager.exception.QueryMappingNotAvailableException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query Manager.
 */
public class ExternalIdPClientUtil {

    private static final Logger log = LogManager.getLogger(ExternalIdPClientUtil.class);

    private static final IdPClientConfiguration idPClientConfiguration = getIdPClientConfiguration();

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

    public static String getRequestBody(Map<String, String> params) {

        return params.entrySet().stream()
                .map(ExternalIdPClientUtil::urlEncodeKeyValuePair)
                .collect(Collectors.joining("&"));
    }

    public static String getClientConfigurationProperty(String key) {

        return idPClientConfiguration.getProperties().get(key);
    }

    private static IdPClientConfiguration getIdPClientConfiguration() {

        BundleContext bundleContext = FrameworkUtil.getBundle(ConfigProvider.class).getBundleContext();
        ServiceReference serviceReference =
                bundleContext.getServiceReference(ConfigProvider.class.getName());
        ConfigProvider configProvider = (ConfigProvider) bundleContext.getService(serviceReference);
        IdPClientConfiguration idPClientConfiguration;
        try {
            if (configProvider.getConfigurationObject(IdPClientConstants.SP_AUTH_NAMESPACE) == null) {
                idPClientConfiguration = new IdPClientConfiguration();
            } else {
                idPClientConfiguration = configProvider.
                        getConfigurationObject(IdPClientConfiguration.class);
            }
        } catch (ConfigurationException e) {
            log.error("Error in reading '" + IdPClientConstants.SP_AUTH_NAMESPACE + "' from file.", e);
            return null;
        }
        return idPClientConfiguration;
    }

    private static String urlEncodeKeyValuePair(Map.Entry<String, String> entry) {

        try {
            return URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()) + '='
                    + URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new EncodeException("Error occurred while URL encoding message", ex);
        }
    }

}
