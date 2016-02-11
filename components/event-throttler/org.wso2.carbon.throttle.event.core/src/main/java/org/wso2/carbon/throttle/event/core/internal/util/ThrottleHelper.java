/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.throttle.event.core.internal.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;
import org.wso2.carbon.ndatasource.core.DataSourceManager;
import org.wso2.carbon.throttle.event.core.exception.ThrottleConfigurationException;
import org.wso2.carbon.throttle.event.core.internal.CEPConfig;
import org.wso2.carbon.throttle.event.core.internal.Policy;
import org.wso2.carbon.throttle.event.core.internal.ds.ThrottleServiceValueHolder;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.siddhi.core.SiddhiManager;

import javax.sql.DataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class ThrottleHelper {
    private static final Logger log = Logger.getLogger(ThrottleHelper.class);

    private ThrottleHelper(){
        //avoids initialization
    }

    /**
     * Reads throttling policy config file and populate a {@link org.wso2.carbon.throttle.event.core.internal.Policy} objects.
     * @return {@link org.wso2.carbon.throttle.event.core.internal.Policy} object
     * @throws ThrottleConfigurationException
     */
    public static Policy loadThrottlingPolicies(DeploymentFileData deploymentFileData) throws ThrottleConfigurationException {
        OMElement policyElement = loadConfigXML(deploymentFileData.getAbsolutePath());
        String name;
        String level;
        String tier;

        if ((name = policyElement.getAttributeValue(new QName(ThrottleConstants.NAME))) == null || !name.equals
                (deploymentFileData.getName().split("\\.")[0])) {
            throw new ThrottleConfigurationException("Invalid policy element with invalid name attribute in " +
                    deploymentFileData.getName() + " policy file. Name attribute should not be null and as same as " +
                    "policy file name.");
        }

        if ((tier = policyElement.getAttributeValue(new QName(ThrottleConstants.TIER))) == null) {
            throw new ThrottleConfigurationException("Invalid policy element with no tier attribute in " +
                    deploymentFileData.getName() + " policy file.");
        }

        if ((level = policyElement.getAttributeValue(new QName(ThrottleConstants.LEVEL))) == null) {
            throw new ThrottleConfigurationException("Invalid policy element with no level attribute in " +
                    deploymentFileData.getName() + " policy file.");
        }

        OMElement descriptionElement = policyElement.getFirstChildWithName(new QName(ThrottleConstants.DESCRIPTION));

        OMElement eligibilityQueryElement;
        if ((eligibilityQueryElement = policyElement.getFirstChildWithName(new QName(ThrottleConstants
                .ELIGIBILITY_QUERY))) == null) {
            throw new ThrottleConfigurationException("Invalid policy element with no eligibility query in " +
                    ThrottleConstants.THROTTLE_POLICY_XML);
        }

        OMElement decisionQueryElement;
        if ((decisionQueryElement = policyElement.getFirstChildWithName(new QName(ThrottleConstants
                .DECISION_QUERY))) == null) {
            throw new ThrottleConfigurationException("Invalid policy element with no decision query in " +
                    ThrottleConstants.THROTTLE_POLICY_XML);
        }

        Policy policy = new Policy(name, level, tier, eligibilityQueryElement.getText(), decisionQueryElement.getText());
        if (descriptionElement != null) {
            policy.setDescription(descriptionElement.getText());
        }

        return policy;
    }

    public static CEPConfig loadCEPConfig() throws ThrottleConfigurationException {
        String carbonHome = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
        String path = carbonHome + File.separator + ThrottleConstants.CEP_CONFIG_XML;
        OMElement configElement = loadConfigXML(path);

        OMElement hostNameElement;
        OMElement tcpPortElement;
        OMElement sslPortElement;
        OMElement usernameElement;
        OMElement passwordElement;
        OMElement streamNameElement;
        OMElement streamVersionElement;

        if ((hostNameElement = configElement.getFirstChildWithName(new QName(ThrottleConstants.HOST_NAME))) == null) {
            throw new ThrottleConfigurationException("Invalid config element with no host name in " +
                    ThrottleConstants.CEP_CONFIG_XML);
        }
        if ((tcpPortElement = configElement.getFirstChildWithName(new QName(ThrottleConstants.TCP_PORT))) == null) {
            throw new ThrottleConfigurationException("Invalid config element with no TCP port in " +
                    ThrottleConstants.CEP_CONFIG_XML);
        }
        if ((sslPortElement = configElement.getFirstChildWithName(new QName(ThrottleConstants.SSL_PORT))) == null) {
            throw new ThrottleConfigurationException("Invalid config element with no SSL port in " +
                    ThrottleConstants.CEP_CONFIG_XML);
        }
        if ((usernameElement = configElement.getFirstChildWithName(new QName(ThrottleConstants.USERNAME))) == null) {
            throw new ThrottleConfigurationException("Invalid config element with no username in " +
                    ThrottleConstants.CEP_CONFIG_XML);
        }
        if ((passwordElement = configElement.getFirstChildWithName(new QName(ThrottleConstants.PASSWORD))) == null) {
            throw new ThrottleConfigurationException("Invalid config element with no password in " +
                    ThrottleConstants.CEP_CONFIG_XML);
        }
        if ((streamNameElement = configElement.getFirstChildWithName(new QName(ThrottleConstants.STREAM_NAME))) == null) {
            throw new ThrottleConfigurationException("Invalid config element with no stream name in " +
                    ThrottleConstants.CEP_CONFIG_XML);
        }
        if ((streamVersionElement = configElement.getFirstChildWithName(new QName(ThrottleConstants.STREAM_VERSION))) == null) {
            throw new ThrottleConfigurationException("Invalid config element with no stream version in " +
                    ThrottleConstants.CEP_CONFIG_XML);
        }

        return new CEPConfig(hostNameElement.getText(), tcpPortElement.getText(), sslPortElement.getText(),
                usernameElement.getText(), passwordElement.getText(), streamNameElement.getText(),
                streamVersionElement.getText());
    }

    /**
     * Loads the configuration file in the given path as an OM element
     * @return OMElement of config file
     * @throws ThrottleConfigurationException
     */
    private static OMElement loadConfigXML(String path) throws ThrottleConfigurationException {

        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(new File(path)));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement omElement = builder.getDocumentElement();
            omElement.build();
            return omElement;
        } catch (FileNotFoundException e) {
            throw new ThrottleConfigurationException("Configuration file cannot be found in the path : " + path, e);
        } catch (XMLStreamException e) {
            throw new ThrottleConfigurationException("Invalid XML syntax for configuration file located in the path :" + path, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error("Can not shutdown the input stream", e);
            }
        }
    }

    /**
     * Loads carbon data-sources into local Siddhi Manager instance
     * @param siddhiManager local siddhi manager instance
     */
    public static void loadDataSourceConfiguration(SiddhiManager siddhiManager) {
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            if (tenantId > -1) {
                DataSourceManager.getInstance().initTenant(tenantId);
            }
            List<CarbonDataSource> dataSources = ThrottleServiceValueHolder.getDataSourceService().getAllDataSources();
            for (CarbonDataSource cds : dataSources) {
                setDatasources(siddhiManager, cds);
            }
        } catch (DataSourceException e) {
            log.error("Unable to populate the data sources in Siddhi engine.", e);
        }
    }

    private static void setDatasources(SiddhiManager siddhiManager, CarbonDataSource carbonDataSource){
        try {
            if (carbonDataSource.getDSObject() instanceof DataSource) {
                siddhiManager.setDataSource(carbonDataSource.getDSMInfo().getName(), (DataSource) carbonDataSource.getDSObject());
            }
        } catch (Exception e) {
            log.error("Unable to add the datasource" + carbonDataSource.getDSMInfo().getName(), e);
        }
    }


}
