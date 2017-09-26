package org.wso2.carbon.databridge.receiver.thrift.test.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.core.conf.DataBridgeConfiguration;
import org.wso2.carbon.databridge.core.internal.utils.DataBridgeConstants;
import org.wso2.carbon.databridge.receiver.thrift.test.ThriftEventConverterTest;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;

public class ThriftServerUtil {

    private static Logger log = Logger.getLogger(ThriftServerUtil.class);

    public static final Path testDir = Paths.get("src", "test", "resources");

    public static DataBridgeConfiguration getDataBridgeConfiguration(String file) throws IOException,
            XMLStreamException, JAXBException {

        String configPath = testDir + File.separator + file;

        File configFile = new File(configPath);
        DataBridgeConfiguration dataBridgeConfiguration;

        if (configFile.exists()) {
            try(FileInputStream fileInputStream = new FileInputStream(configFile)) {
                JAXBContext jaxbContext = JAXBContext.newInstance(DataBridgeConfiguration.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                dataBridgeConfiguration = (DataBridgeConfiguration) jaxbUnmarshaller.unmarshal(configFile);
                StAXOMBuilder builder = new StAXOMBuilder(fileInputStream);
                OMElement configElement = builder.getDocumentElement();
                SecretResolver secretResolver = SecretResolverFactory.create(configElement, true);
                if (secretResolver != null && secretResolver.isInitialized()) {
                    String resolvedPassword = getResolvedPassword(secretResolver,
                            DataBridgeConstants.DATA_BRIDGE_CONF_PASSWORD_ALIAS);
                    if (resolvedPassword != null) {
                        dataBridgeConfiguration.setKeyStorePassword(resolvedPassword);
                    }
                }
                return dataBridgeConfiguration;
            }
        } else {
            return null;
        }
    }

    private static String getResolvedPassword(SecretResolver secretResolver, String alias) {
        if (secretResolver.isTokenProtected(alias)) {
            String resolvedPassword = secretResolver.resolve(alias);
            if (resolvedPassword != null && !resolvedPassword.isEmpty()) {
                return resolvedPassword;
            }
        }
        return null;
    }

    public static void setupCarbonConfig(String tenantName) {
        System.setProperty("carbon.home", Paths.get(testDir.toString(), "carbon-context").toString());
        System.setProperty("tenant.name", tenantName);
        System.setProperty("portOffset", "0");
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
