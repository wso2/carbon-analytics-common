package org.wso2.carbon.event.output.adapter.sms;

import org.apache.log4j.Logger;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.Property;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.output.adapter.sms.internal.ds.SMSEventAdapterServiceValueHolder;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * .
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(SMSEventAdapterServiceValueHolder.class)
public class SmsOutputAdaptorTestCase {
    private static final Logger logger = Logger.getLogger(SmsOutputAdaptorTestCase.class);
    private static final Path testDir = Paths.get("src", "test", "resources");

    private void setupCarbonConfig() {
        System.setProperty("carbon.home",
                Paths.get(testDir.toString(), "carbon-context").toString());
        System.setProperty("tenant.name", "tenant.name");
    }

    private SMSEventAdapter getHttpAdaptor() {
        setupCarbonConfig();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1234);
        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("TestHttpAdaptor");
        eventAdapterConfiguration.setType("sms");
        eventAdapterConfiguration.setMessageFormat("text");
        Map<String, String> staticPropertes = new HashMap<>();
        staticPropertes.put("sms.no", "+94771117673");
        eventAdapterConfiguration.setStaticProperties(staticPropertes);
        Map<String, String> globalProperties = new HashMap<>();
        globalProperties.put("jobQueueSize", "10000");
        globalProperties.put("defaultKeepAliveTime", "20000");
        globalProperties.put("maxThread", "100");
        globalProperties.put("minThread", "8");
        globalProperties.put("defaultMaxConnectionsPerHost", "50");
        globalProperties.put("maxTotalConnections", "1000");
        return new SMSEventAdapter(eventAdapterConfiguration, globalProperties);
    }

    @Test
    public void testSMSPublisherInit() throws OutputEventAdapterException {
        SMSEventAdapter smsEventAdapter = getHttpAdaptor();
        smsEventAdapter.init();
        smsEventAdapter.isPolled();
        smsEventAdapter.destroy();
    }
    @Test
    public void testSMSPublisherInitDefault() throws OutputEventAdapterException {
        setupCarbonConfig();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1234);
        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("TestSmsAdaptor");
        eventAdapterConfiguration.setType("sms");
        eventAdapterConfiguration.setMessageFormat("text");
        Map<String, String> staticPropertes = new HashMap<>();
        staticPropertes.put("sms.no", "+94771117673");
        eventAdapterConfiguration.setStaticProperties(staticPropertes);
        Map<String, String> globalProperties = new HashMap<>();
        globalProperties.clear();
        SMSEventAdapter smsEventAdapter = new SMSEventAdapter(eventAdapterConfiguration, globalProperties);
        smsEventAdapter.init();
        smsEventAdapter.isPolled();
        smsEventAdapter.destroy();
    }
    /**
     * Test connection of Http output adaptor
     *
     * @throws TestConnectionNotSupportedException
     */
    @Test(expectedExceptions = {TestConnectionNotSupportedException.class})
    public void testSMSPublisherTestConnect() throws TestConnectionNotSupportedException {
        logger.info("Test case for test the connection ins valid which is not implemented of email output adaptor.");
        SMSEventAdapter smsEventAdapter = getHttpAdaptor();
        smsEventAdapter.testConnect();
    }

    @Test
    public void testSMSPublisherConnect() throws OutputEventAdapterException {
        logger.info("Test case for connection of email output adaptor.");
        SMSEventAdapter smsEventAdapter = getHttpAdaptor();
        smsEventAdapter.init();
        smsEventAdapter.connect();
        smsEventAdapter.disconnect();
        smsEventAdapter.destroy();
    }

    @Test
    public void testHttpPublisherPublish() {
        logger.info("Test case for publishing email in email output adaptor.");
        SMSEventAdapter smsEventAdapter = getHttpAdaptor();
        try {
            smsEventAdapter.init();
            smsEventAdapter.connect();
            Map<String, String> dynamicProperties = new HashMap<>();
            dynamicProperties.put("sms.no", "+94771117673");
            smsEventAdapter.publish("hi", dynamicProperties);
        } catch (OutputEventAdapterException e) {
            e.printStackTrace();
        } finally {
            smsEventAdapter.disconnect();
            smsEventAdapter.destroy();
        }


    }

    @Test
    public void testHttpPublisherPublish2() {
        logger.info("Test case for publishing email in email output adaptor.");
        SMSEventAdapter smsEventAdapter = getHttpAdaptor();
        try {
            smsEventAdapter.init();
            smsEventAdapter.connect();
            Map<String, String> dynamicProperties = new HashMap<>();
            dynamicProperties.put("sms.password", "pwd123");
            dynamicProperties.put("sms.headers", "Content-Type:application/json");
            dynamicProperties.put("sms.url", "sms://localhost:8080/service");
            dynamicProperties.put("sms.username", "user123");
            smsEventAdapter.publish("hi", dynamicProperties);
        } catch (OutputEventAdapterException e) {
            e.printStackTrace();
        } finally {
            smsEventAdapter.disconnect();
            smsEventAdapter.destroy();
        }

    }
    @Test
    public void testSmsPublisherFactory() {
        logger.info("Test case for factory properties of email output adaptor.");
        SMSEventAdapterFactory adapterFactory = new SMSEventAdapterFactory();
        List<Property> dyPropertyList= adapterFactory.getDynamicPropertyList();
        List<Property> propertyList = new ArrayList<>();
        Property property = new Property("sms.no");
        property.setRequired(true);
        property.setSecured(false);
        property.setEncrypted(false);
        property.setDisplayName("Phone No");
        property.setDefaultValue(null);
        property.setOptions(null);
        property.setHint("Phone No where SMS needs to be send (eg: [country-code][number])");
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
        Assert.assertEquals("sms",adapterFactory.getType());

        setupCarbonConfig();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1234);
        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("TestHttpAdaptor");
        eventAdapterConfiguration.setType("sms");
        eventAdapterConfiguration.setMessageFormat("text");
        Map<String, String> staticPropertes = new HashMap<>();
        staticPropertes.put("sms.client.method", "HttpPost");
        staticPropertes.put("sms.proxy.port", "9090");
        staticPropertes.put("sms.proxy.host", "localhost");
        eventAdapterConfiguration.setStaticProperties(staticPropertes);
        Map<String, String> globalProperties = new HashMap<>();
        globalProperties.put("jobQueueSize", "10000");
        globalProperties.put("keepAliveTimeInMillis", "20000");
        globalProperties.put("maxThread", "100");
        globalProperties.put("minThread", "8");
        globalProperties.put("defaultMaxConnectionsPerHost", "50");
        globalProperties.put("maxTotalConnections", "1000");
        adapterFactory.createEventAdapter(eventAdapterConfiguration, globalProperties);
    }
}
