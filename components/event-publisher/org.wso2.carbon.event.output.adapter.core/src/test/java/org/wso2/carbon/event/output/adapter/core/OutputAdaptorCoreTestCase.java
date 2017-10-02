package org.wso2.carbon.event.output.adapter.core;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.internal.config.AdapterConfig;
import org.wso2.carbon.event.output.adapter.core.internal.config.AdapterConfigs;
import org.wso2.carbon.event.output.adapter.core.internal.config.Property;
import org.wso2.carbon.event.output.adapter.core.internal.ds.OutputEventAdapterServiceValueHolder;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * .
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(CarbonContext.class)
public class OutputAdaptorCoreTestCase {
    private static final Logger logger = Logger.getLogger(OutputAdaptorCoreTestCase.class);
    private static final Path testDir = Paths.get("src", "test", "resources");

    @Test
    public void OutputAdaptorUtilTestCase() throws OutputEventAdapterException {
        logger.info("Test case for testing the utils of Output Adapter Core.");
        final TestLogAppender appender = new TestLogAppender();
        final Logger testLogger = Logger.getRootLogger();
        testLogger.addAppender(appender);
        AdapterConfigs globalAdapterConfigs = new AdapterConfigs();
        List<AdapterConfig> adapterConfigs = new ArrayList<>();
        AdapterConfig config = new AdapterConfig();
        config.setType("Test Adaptor");
        List<org.wso2.carbon.event.output.adapter.core.internal.config.Property> globalProperties = new ArrayList<>();
        Property property = new Property();
        property.setKey("test key");
        property.setValue("test Value");
        globalProperties.add(property);
        config.setGlobalProperties(globalProperties);
        adapterConfigs.add(config);
        globalAdapterConfigs.setAdapterConfigs(adapterConfigs);
        OutputEventAdapterServiceValueHolder.setGlobalAdapterConfigs(globalAdapterConfigs);
        setupCarbonConfig();
        mockStatic(CarbonContext.class);
        CarbonContext context = PowerMockito.mock(CarbonContext.getThreadLocalCarbonContext().getClass());
        when(context.getTenantId()).thenReturn(-1233);
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1233);
        ConfigurationContext context1 = mock(ConfigurationContext.class);
        ConfigurationContextService configurationContextService = new ConfigurationContextService(context1, context1);
        mockStatic(OutputEventAdapterServiceValueHolder.class);
        OutputEventAdapterServiceValueHolder.setConfigurationContextService(configurationContextService);
        AxisConfiguration axisConfiguration = new AxisConfiguration();
        when(context1.getAxisConfiguration()).thenReturn(axisConfiguration);
        EventAdapterUtil.getAxisConfiguration();

        String adapterName = "logger adaptor";
        Object event = "Event:{ name: wso2}";
        String message = "Test Log";
        Log log = LogFactory.getLog(OutputAdaptorCoreTestCase.class);
        int tenantId = -1233;
        EventAdapterUtil.logAndDrop(adapterName, event, message, log, tenantId);
        EventAdapterUtil.logAndDrop(adapterName, event, message, new RuntimeException("Test Run time exception"), log,
                tenantId);
        Map<String, String> map = EventAdapterUtil.getGlobalProperties("Test Adaptor");
        Assert.assertEquals(map.get("test key"), "test Value");
        final List<LoggingEvent> logEntries = appender.getLog();
        List<Object> logMessages = new ArrayList<>();
        for (LoggingEvent logEvent : logEntries) {
            logMessages.add(logEvent.getMessage());
        }
        PrivilegedCarbonContext.unloadTenant(-1233);
        Assert.assertEquals(logMessages.contains("Error at Output Adapter 'logger adaptor' for tenant id '-1233', " +
                "dropping event: \nEvent:{ name: wso2}"), true);
    }

    @Test
    public void PropertyTest() {
        logger.info("Test case for testing the properties of Output Adapter Core.");
        org.wso2.carbon.event.output.adapter.core.Property property = new org.wso2.carbon.event.output.adapter.core.Property("email.address");
        property.setRequired(true);
        property.setSecured(false);
        property.setEncrypted(false);
        property.setDisplayName("Email Address");
        property.setDefaultValue("Test Default");
        property.setOptions(new String[]{"option1", "option2"});
        property.setHint("Register publisher for multiple email IDs' separated by \",\"");

        Assert.assertEquals(property.getHint(), "Register publisher for multiple email IDs' separated by \",\"");
        Assert.assertEquals(property.getDefaultValue(), "Test Default");
        Assert.assertEquals(property.getPropertyName(), "email.address");
        Assert.assertEquals(property.getOptions(), new String[]{"option1", "option2"});
        Assert.assertEquals(property.getDisplayName(), "Email Address");
    }

    @Test
    public void OutputAdaptorTenantTestCase() throws OutputEventAdapterException {
        logger.info("Test case for testing the adding tenant configuration to  TenantConfigHolder.");
        final TestLogAppender appender = new TestLogAppender();
        final Logger testLogger = Logger.getRootLogger();
        testLogger.addAppender(appender);
        AdapterConfigs globalAdapterConfigs = new AdapterConfigs();
        List<AdapterConfig> adapterConfigs = new ArrayList<>();
        AdapterConfig config = new AdapterConfig();
        config.setType("Test Adaptor");
        List<org.wso2.carbon.event.output.adapter.core.internal.config.Property> globalProperties = new ArrayList<>();
        Property property = new Property();
        property.setKey("test key");
        property.setValue("test Vale");
        globalProperties.add(property);
        config.setGlobalProperties(globalProperties);
        adapterConfigs.add(config);
        globalAdapterConfigs.setAdapterConfigs(adapterConfigs);
        OutputEventAdapterServiceValueHolder.setGlobalAdapterConfigs(globalAdapterConfigs);
        setupCarbonConfig();
        mockStatic(CarbonContext.class);
        CarbonContext context = PowerMockito.mock(CarbonContext.getThreadLocalCarbonContext().getClass());
        when(context.getTenantId()).thenReturn(-1233);
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1233);
        ConfigurationContext context1 = mock(ConfigurationContext.class);
        ConfigurationContextService configurationContextService = new ConfigurationContextService(context1, context1);
        mockStatic(OutputEventAdapterServiceValueHolder.class);
        TenantConfigHolder.addTenantConfig(-1233, context1);
        OutputEventAdapterServiceValueHolder.setConfigurationContextService(configurationContextService);
        AxisConfiguration axisConfiguration = new AxisConfiguration();
        when(context1.getAxisConfiguration()).thenReturn(axisConfiguration);
        EventAdapterUtil.getAxisConfiguration();
        PrivilegedCarbonContext.unloadTenant(-1233);
        final List<LoggingEvent> logEntries = appender.getLog();
        List<Object> logMessages = new ArrayList<>();
        for (LoggingEvent logEvent : logEntries) {
            logMessages.add(logEvent.getMessage());
        }
        Assert.assertEquals(logMessages.contains("Unload Tenant Task: org.wso2.carbon.context.internal." +
                "CarbonContextDataHolder$CarbonContextCleanupTask was registered."), true);

    }

    private void setupCarbonConfig() {
        System.setProperty("carbon.home",
                Paths.get(testDir.toString(), "carbon-context").toString());
        System.setProperty("tenant.name", "tenant.name");
    }

}
