/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.stream.core.internal.util;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.event.stream.core.internal.config.EventPublisherConfigs;
import org.wso2.carbon.event.stream.core.internal.ds.EventStreamServiceValueHolder;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.commons.MiscellaneousUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

/**
 * This class is used to load the global output event adapter configurations from the output-event-adapters.xml file.
 */
public class EventPublisherConfigHelper {

    private static final Log log = LogFactory.getLog(EventPublisherConfigHelper.class);
    private static SecretResolver secretResolver;

    public static void secureResolveOmElement(OMElement doc) throws EventStreamConfigurationException {

        if (doc != null) {
            try {
                secretResolver = SecretResolverFactory.create(doc, true);
                secureLoadOMElement(doc);
            } catch (CryptoException e) {
                throw new EventStreamConfigurationException("Error in secure load of global output event adapter properties: " +
                        e.getMessage(), e);
            }
        }
    }

    private static void secureLoadOMElement(OMElement element) throws CryptoException {

        String alias = MiscellaneousUtil.getProtectedToken(element.getText());
        if (alias != null && !alias.isEmpty()) {
            element.setText(loadFromSecureVault(alias));
        } else {
            OMAttribute secureAttr = element.getAttribute(new QName(EventStreamConstants.SECURE_VAULT_NS,
                    EventStreamConstants.SECRET_ALIAS_ATTR_NAME));
            if (secureAttr != null) {
                element.setText(loadFromSecureVault(secureAttr.getAttributeValue()));
                element.removeAttribute(secureAttr);
            }
        }
        Iterator<OMElement> childNodes = element.getChildElements();
        while (childNodes.hasNext()) {
            OMElement tmpNode = childNodes.next();
            secureLoadOMElement(tmpNode);
        }
    }

    public static OMElement convertToOmElement(File file) throws EventStreamConfigurationException {

        try {
            StAXOMBuilder builder = new StAXOMBuilder(new FileInputStream(file));
            return builder.getDocumentElement();
        } catch (Exception e) {
            throw new EventStreamConfigurationException("Error in creating an XML document from file: " +
                    e.getMessage(), e);
        }
    }

    private static synchronized String loadFromSecureVault(String alias) {
        if (secretResolver == null) {
            secretResolver = SecretResolverFactory.create((OMElement) null, false);
            secretResolver.init(EventStreamServiceValueHolder.
                    getSecretCallbackHandlerService().getSecretCallbackHandler());
        }
        return secretResolver.resolve(alias);
    }

    public static EventPublisherConfigs loadGlobalConfigs() {

        String path = CarbonUtils.getCarbonConfigDirPath() + File.separator + EventStreamConstants.GLOBAL_CONFIG_FILE_NAME;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(EventPublisherConfigs.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            File configFile = new File(path);
            if (!configFile.exists()) {
                log.warn(EventStreamConstants.GLOBAL_CONFIG_FILE_NAME + " can not found in " + path + "," +
                        " hence Output Event Adapters will be running with default global configs.");
            }
            OMElement globalConfigDoc = convertToOmElement(configFile);
            secureResolveOmElement(globalConfigDoc);
            return (EventPublisherConfigs) unmarshaller.unmarshal(globalConfigDoc.getXMLStreamReader());
        } catch (JAXBException e) {
            log.error("Error in loading " + EventStreamConstants.GLOBAL_CONFIG_FILE_NAME + " from " + path + "," +
                    " hence Output Event Adapters will be running with default global configs.");
        } catch (EventStreamConfigurationException e) {
            log.error("Error in loading " + EventStreamConstants.GLOBAL_CONFIG_FILE_NAME + " from " + path + "," +
                    " hence Output Event Adapters will be running with default global configs.");
        }
        return new EventPublisherConfigs();
    }
}