package org.wso2.carbon.event.output.adapter.logger;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.Property;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * .
 */
public class LoggerPublisherTestCase {
    private static final Logger logger = Logger.getLogger(LoggerPublisherTestCase.class);
    private static final Path testDir = Paths.get("src", "test", "resources");

    private void setupCarbonConfig(String tenantName) {
        System.setProperty("carbon.home",
                Paths.get(testDir.toString(), "carbon-context").toString());
        System.setProperty("tenant.name", tenantName);
    }

    private LoggerEventAdapter getLoggerAdaptor() {
        setupCarbonConfig("tenant.name");
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1234);
        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("log");
        eventAdapterConfiguration.setType("logger");
        eventAdapterConfiguration.setMessageFormat("text");
        Map<String, String> staticProperties = new HashMap<>();
        eventAdapterConfiguration.setStaticProperties(staticProperties);
        Map<String, String> globalProperties = new HashMap<>();
        return new LoggerEventAdapter(eventAdapterConfiguration, globalProperties);
    }

    @Test
    public void testLoggerPublisherInit() throws OutputEventAdapterException {
        LoggerEventAdapter loggerEventAdapter = getLoggerAdaptor();
        loggerEventAdapter.init();
        loggerEventAdapter.isPolled();
        loggerEventAdapter.destroy();
    }

    /**
     * Test connection of Logger output adaptor
     *
     * @throws TestConnectionNotSupportedException
     */
    @Test(expectedExceptions = {TestConnectionNotSupportedException.class})
    public void testLoggerPublisherTestConnect() throws TestConnectionNotSupportedException {
        logger.info("Test case for test the connection ins valid which is not implemented of email output adaptor.");
        LoggerEventAdapter loggerEventAdapter = getLoggerAdaptor();
        loggerEventAdapter.testConnect();
    }

    @Test
    public void testLoggerPublisherConnect() throws OutputEventAdapterException {
        logger.info("Test case for connection of email output adaptor.");
        LoggerEventAdapter loggerEventAdapter = getLoggerAdaptor();
        loggerEventAdapter.init();
        loggerEventAdapter.connect();
        loggerEventAdapter.disconnect();
        loggerEventAdapter.destroy();
    }

    @Test
    public void testLoggerPublisherPublish() {
        logger.info("Test case for publishing email in email output adaptor.");
        LoggerEventAdapter loggerEventAdapter = getLoggerAdaptor();
        try {
            loggerEventAdapter.init();
            Map<String, String> dynamicProperties = new HashMap<>();
            dynamicProperties.put("uniqueId", "*****");
            loggerEventAdapter.publish("hi", dynamicProperties);
            dynamicProperties.clear();
        } catch (OutputEventAdapterException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLoggerPublisherPublishObject() {
        logger.info("Test case for publishing email in email output adaptor with object payload.");
        LoggerEventAdapter loggerEventAdapter = getLoggerAdaptor();
        try {
            loggerEventAdapter.init();
            Map<String, String> dynamicProperties = new HashMap<>();
            Object [] array = new Object[1];
            array[0]="test val";
            loggerEventAdapter.publish(array , dynamicProperties);
            dynamicProperties.clear();
        } catch (OutputEventAdapterException e) {
            logger.info(e.getMessage());
        }
    }

    @Test
    public void testLoggerPublisherFactory() {
        logger.info("Test case for factory properties of email output adaptor.");
        LoggerEventAdapterFactory adapterFactory = new LoggerEventAdapterFactory();
        List<Property> dyPropertyList= adapterFactory.getDynamicPropertyList();
        List<Property> propertyList = new ArrayList<>();
        Property property = new Property("uniqueId");
        property.setRequired(false);
        property.setSecured(false);
        property.setEncrypted(false);
        property.setDisplayName("Unique Identifier");
        property.setDefaultValue(null);
        property.setOptions(null);
        property.setHint("To uniquely identify a log entry");
        propertyList.add(property);

        Assert.assertEquals(1,dyPropertyList.size());
        int i=0;
        for (Property prop:propertyList) {
            Assert.assertEquals(prop.getPropertyName(),dyPropertyList.get(i).getPropertyName());
            Assert.assertEquals(prop.getDefaultValue(),dyPropertyList.get(i).getDefaultValue());
            Assert.assertEquals(prop.getDisplayName(),dyPropertyList.get(i).getDisplayName());
            Assert.assertEquals(prop.getHint(),dyPropertyList.get(i).getHint());
            i++;
        }
        List<String> types = new ArrayList<>();
        types.add("text");
        types.add("xml");
        types.add("json");
        List<String> supportedTypes=adapterFactory.getSupportedMessageFormats();
        Assert.assertEquals(supportedTypes.toString(),types.toString());
        Assert.assertEquals("logger",adapterFactory.getType());

        setupCarbonConfig("tenant.name");
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1234);
        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("log");
        eventAdapterConfiguration.setType("logger");
        eventAdapterConfiguration.setMessageFormat("text");
        Map<String, String> staticProperties = new HashMap<>();
        eventAdapterConfiguration.setStaticProperties(staticProperties);
        Map<String, String> globalProperties = new HashMap<>();
        adapterFactory.createEventAdapter(eventAdapterConfiguration, globalProperties);
        Assert.assertEquals(adapterFactory.getStaticPropertyList(),null);
        Assert.assertEquals(adapterFactory.getUsageTips(),null);
    }
}
