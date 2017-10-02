package org.wso2.carbon.event.output.adapter.core;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.output.adapter.core.internal.CarbonOutputEventAdapterService;
import org.wso2.carbon.event.output.adapter.core.internal.DecayTimer;
import org.wso2.carbon.event.output.adapter.core.internal.OutputAdapterRuntime;
import org.wso2.carbon.event.output.adapter.core.internal.config.AdapterConfig;
import org.wso2.carbon.event.output.adapter.core.internal.config.AdapterConfigs;
import org.wso2.carbon.event.output.adapter.core.internal.config.Property;
import org.wso2.carbon.event.output.adapter.core.internal.ds.OutputEventAdapterServiceValueHolder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * .
 */
public class OutputAdaptorServiceTestCase {
    private static final Logger logger = Logger.getLogger(OutputAdaptorServiceTestCase.class);
    private static final Path testDir = Paths.get("src", "test", "resources");

    private void setupCarbonConfig() {
        System.setProperty("carbon.home",
                Paths.get(testDir.toString(), "carbon-context").toString());
        System.setProperty("tenant.name", "tenant.name");
    }

    @Test
    public void OutputAdaptorTestCase() throws OutputEventAdapterException, TestConnectionNotSupportedException {
        logger.info("Test case for testing the publishing the events using test adaptor.");
        setupCarbonConfig();
        CarbonOutputEventAdapterService adapterService = new CarbonOutputEventAdapterService();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1233);
        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("TestAdapter");
        eventAdapterConfiguration.setType("test");
        eventAdapterConfiguration.setMessageFormat("text");
        OutputEventAdapterConfiguration eventAdapterConfiguration2 = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration2.setName("TestAdapter");
        eventAdapterConfiguration2.setType("test");
        eventAdapterConfiguration2.setMessageFormat("text");
        eventAdapterConfiguration.equals(null);
        eventAdapterConfiguration.equals(eventAdapterConfiguration);
        eventAdapterConfiguration.equals(eventAdapterConfiguration2);
        eventAdapterConfiguration.setOutputStreamIdOfWso2eventMessageFormat("test");
        eventAdapterConfiguration.getOutputStreamIdOfWso2eventMessageFormat();
        eventAdapterConfiguration.hashCode();
        eventAdapterConfiguration.getMessageFormat();
        eventAdapterConfiguration.getName();
        eventAdapterConfiguration.hashCode();
        eventAdapterConfiguration.getType();
        Map<String, String> staticProperties = new HashMap<>();
        staticProperties.put("company", "WSO2");
        eventAdapterConfiguration.setStaticProperties(staticProperties);
        OutputEventAdapterFactory outputEventAdapterFactory = new TestEventAdapterFactory();
        adapterService.registerEventAdapterFactory(outputEventAdapterFactory);
        AdapterConfigs globalAdapterConfigs = new AdapterConfigs();
        List<AdapterConfig> adapterConfigs = new ArrayList<>();
        AdapterConfig config = new AdapterConfig();
        config.setType("test");
        List<org.wso2.carbon.event.output.adapter.core.internal.config.Property> gProperties = new ArrayList<>();
        org.wso2.carbon.event.output.adapter.core.internal.config.Property property = new org.wso2.carbon.event.output.adapter.core.internal.config.Property();
        property.setKey("maxThread");
        property.setValue("100");
        gProperties.add(property);
        property = new Property();
        property.setKey("keepAliveTimeInMillis");
        property.setValue("20000");
        gProperties.add(property);
        property = new Property();
        property.setKey("jobQueueSize");
        property.setValue("10000");
        gProperties.add(property);
        property = new Property();
        property.setKey("minThread");
        property.setValue("8");
        gProperties.add(property);
        config.setGlobalProperties(gProperties);
        adapterConfigs.add(config);
        globalAdapterConfigs.setAdapterConfigs(adapterConfigs);
        OutputEventAdapterServiceValueHolder.setGlobalAdapterConfigs(globalAdapterConfigs);
        adapterService.create(eventAdapterConfiguration);
        adapterService.getOutputEventAdapterTypes();
        adapterService.getOutputEventAdapterSchema("test");
        adapterService.isPolled("TestAdapter");
        Map<String, String> dynamicProperties = new HashMap<>();
        dynamicProperties.put("user.address", "No 20, Plam Grove, Colombo 03");
        adapterService.publish("TestAdapter", dynamicProperties, "Test Message");
        adapterService.unRegisterEventAdapterFactory(outputEventAdapterFactory);
        adapterService.destroy("TestAdapter");
        PrivilegedCarbonContext.unloadTenant(-1233);
    }
    @Test
    public void OutputAdaptorTestCaseException() throws OutputEventAdapterException,
            TestConnectionNotSupportedException {
        logger.info("Test case for testing the publishing while Runtime exception happen in message sending.");
        final TestLogAppender appender = new TestLogAppender();
        final Logger testLogger = Logger.getLogger(OutputAdapterRuntime.class);
        testLogger.addAppender(appender);
        setupCarbonConfig();
        CarbonOutputEventAdapterService adapterService = new CarbonOutputEventAdapterService();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1233);
        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("TestAdapter");
        eventAdapterConfiguration.setType("test");
        Map<String, String> staticProperties = new HashMap<>();
        staticProperties.put("company", "WSO2");
        eventAdapterConfiguration.setStaticProperties(staticProperties);
        OutputEventAdapterFactory outputEventAdapterFactory = new TestEventAdapterFactory();
        adapterService.registerEventAdapterFactory(outputEventAdapterFactory);
        AdapterConfigs globalAdapterConfigs = new AdapterConfigs();
        List<AdapterConfig> adapterConfigs = new ArrayList<>();
        AdapterConfig config = new AdapterConfig();
        config.setType("test");
        List<org.wso2.carbon.event.output.adapter.core.internal.config.Property> gProperties = new ArrayList<>();
        org.wso2.carbon.event.output.adapter.core.internal.config.Property property = new org.wso2.carbon.event.output.adapter.core.internal.config.Property();
        property.setKey("maxThread");
        property.setValue("100");
        gProperties.add(property);
        property = new Property();
        property.setKey("keepAliveTimeInMillis");
        property.setValue("20000");
        gProperties.add(property);
        property = new Property();
        property.setKey("jobQueueSize");
        property.setValue("10000");
        gProperties.add(property);
        property = new Property();
        property.setKey("minThread");
        property.setValue("8");
        gProperties.add(property);
        config.setGlobalProperties(gProperties);
        adapterConfigs.add(config);
        globalAdapterConfigs.setAdapterConfigs(adapterConfigs);
        OutputEventAdapterServiceValueHolder.setGlobalAdapterConfigs(globalAdapterConfigs);
        adapterService.create(eventAdapterConfiguration);
        adapterService.getOutputEventAdapterTypes();
        adapterService.getOutputEventAdapterSchema("test");
        Assert.assertEquals(adapterService.isPolled("TestAdapter"),false);
        Map<String, String> dynamicProperties = new HashMap<>();
        dynamicProperties.put("user.address", "No 20, Plam Grove, Colombo 03");
        adapterService.publish("TestAdapter", dynamicProperties, "exception");
        adapterService.unRegisterEventAdapterFactory(outputEventAdapterFactory);
        adapterService.destroy("TestAdapter");
        final List<LoggingEvent> logEntries = appender.getLog();
        List<Object> logMessages = new ArrayList<>();
        for (LoggingEvent logEvent : logEntries) {
            logMessages.add(logEvent.getMessage());
        }
        PrivilegedCarbonContext.unloadTenant(-1233);
        Assert.assertEquals(logMessages.contains("Event dropped at Output Adapter 'TestAdapter' for tenant id '-1233', Test " +
                "Mock exception"),true);
    }

    @Test
    public void OutputAdaptorTestCaseException2() throws OutputEventAdapterException,
            TestConnectionNotSupportedException {
        logger.info("Test case for testing the publishing while connection is not available");
        final TestLogAppender appender = new TestLogAppender();
        final Logger testLogger = Logger.getLogger(OutputAdapterRuntime.class);
        testLogger.addAppender(appender);
        setupCarbonConfig();
        CarbonOutputEventAdapterService adapterService = new CarbonOutputEventAdapterService();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1233);
        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("TestAdapter");
        eventAdapterConfiguration.setType("test");
        Map<String, String> staticProperties = new HashMap<>();
        staticProperties.put("company", "WSO2");
        eventAdapterConfiguration.setStaticProperties(staticProperties);
        OutputEventAdapterFactory outputEventAdapterFactory = new TestEventAdapterFactory();
        adapterService.registerEventAdapterFactory(outputEventAdapterFactory);
        AdapterConfigs globalAdapterConfigs = new AdapterConfigs();
        List<AdapterConfig> adapterConfigs = new ArrayList<>();
        AdapterConfig config = new AdapterConfig();
        config.setType("test");
        List<org.wso2.carbon.event.output.adapter.core.internal.config.Property> gProperties = new ArrayList<>();
        org.wso2.carbon.event.output.adapter.core.internal.config.Property property = new org.wso2.carbon.event.output.adapter.core.internal.config.Property();
        property.setKey("maxThread");
        property.setValue("100");
        gProperties.add(property);
        property = new Property();
        property.setKey("keepAliveTimeInMillis");
        property.setValue("20000");
        gProperties.add(property);
        property = new Property();
        property.setKey("jobQueueSize");
        property.setValue("10000");
        gProperties.add(property);
        property = new Property();
        property.setKey("minThread");
        property.setValue("8");
        gProperties.add(property);
        config.setGlobalProperties(gProperties);
        adapterConfigs.add(config);
        globalAdapterConfigs.setAdapterConfigs(adapterConfigs);
        OutputEventAdapterServiceValueHolder.setGlobalAdapterConfigs(globalAdapterConfigs);
        adapterService.create(eventAdapterConfiguration);
        adapterService.getOutputEventAdapterTypes();
        adapterService.getOutputEventAdapterSchema("test");
        adapterService.isPolled("TestAdapter");
        Map<String, String> dynamicProperties = new HashMap<>();
        dynamicProperties.put("user.address", "No 20, Plam Grove, Colombo 03");
        adapterService.publish("TestAdapter", dynamicProperties, "exceptionC");
        adapterService.unRegisterEventAdapterFactory(outputEventAdapterFactory);
        adapterService.destroy("TestAdapter");
        final List<LoggingEvent> logEntries = appender.getLog();
        List<Object> logMessages = new ArrayList<>();
        for (LoggingEvent logEvent : logEntries) {
            logMessages.add(logEvent.getMessage());
        }
        PrivilegedCarbonContext.unloadTenant(-1233);
        Assert.assertEquals(logMessages.contains("Connection unavailable for Output Adopter 'TestAdapter' reconnecting."),true);
    }
    @Test(expectedExceptions = {TestConnectionNotSupportedException.class})
    public void OutputAdaptorConnectionTestCase() throws OutputEventAdapterException,
            TestConnectionNotSupportedException {
        setupCarbonConfig();
        CarbonOutputEventAdapterService adapterService = new CarbonOutputEventAdapterService();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1233);
        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("TestAdapter");
        eventAdapterConfiguration.setType("test");
        Map<String, String> staticProperties = new HashMap<>();
        staticProperties.put("company", "WSO2");
        eventAdapterConfiguration.setStaticProperties(staticProperties);
        OutputEventAdapterFactory outputEventAdapterFactory = new TestEventAdapterFactory();
        adapterService.registerEventAdapterFactory(outputEventAdapterFactory);
        AdapterConfigs globalAdapterConfigs = new AdapterConfigs();
        List<AdapterConfig> adapterConfigs = new ArrayList<>();
        AdapterConfig config = new AdapterConfig();
        config.setType("test");
        List<org.wso2.carbon.event.output.adapter.core.internal.config.Property> gProperties = new ArrayList<>();
        org.wso2.carbon.event.output.adapter.core.internal.config.Property property = new org.wso2.carbon.event.output.adapter.core.internal.config.Property();
        property.setKey("maxThread");
        property.setValue("100");
        gProperties.add(property);
        property = new Property();
        property.setKey("keepAliveTimeInMillis");
        property.setValue("20000");
        gProperties.add(property);
        property = new Property();
        property.setKey("jobQueueSize");
        property.setValue("10000");
        gProperties.add(property);
        property = new Property();
        property.setKey("minThread");
        property.setValue("8");
        gProperties.add(property);
        config.setGlobalProperties(gProperties);
        adapterConfigs.add(config);
        globalAdapterConfigs.setAdapterConfigs(adapterConfigs);
        OutputEventAdapterServiceValueHolder.setGlobalAdapterConfigs(globalAdapterConfigs);
        adapterService.create(eventAdapterConfiguration);
        adapterService.getOutputEventAdapterTypes();
        adapterService.getOutputEventAdapterSchema("test");
        adapterService.isPolled("TestAdapter");
        Map<String, String> dynamicProperties = new HashMap<>();
        dynamicProperties.put("user.address", "No 20, Plam Grove, Colombo 03");
        adapterService.publish("TestAdapter", dynamicProperties, "Test Message");
        adapterService.testConnection(eventAdapterConfiguration);
        adapterService.unRegisterEventAdapterFactory(outputEventAdapterFactory);
        adapterService.destroy("TestAdapter");
        PrivilegedCarbonContext.unloadTenant(-1233);
    }
}
