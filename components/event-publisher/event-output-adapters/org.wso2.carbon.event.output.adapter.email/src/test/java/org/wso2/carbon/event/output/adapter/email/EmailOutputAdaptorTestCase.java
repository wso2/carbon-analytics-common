package org.wso2.carbon.event.output.adapter.email;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.Property;
import org.wso2.carbon.event.output.adapter.core.exception.ConnectionUnavailableException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test case.
 */
public class EmailOutputAdaptorTestCase {
    private static final Logger logger = Logger.getLogger(EmailOutputAdaptorTestCase.class);
    private static final Path testDir = Paths.get("src", "test", "resources");

    private void setupCarbonConfig() {
        System.setProperty("carbon.home",
                Paths.get(testDir.toString(), "carbon-context").toString());
        System.setProperty("tenant.name", "tenant.name");
    }

    private EmailEventAdapter getEmailAdaptor() {
        setupCarbonConfig();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1234);
        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("MLEmailAdapter");
        eventAdapterConfiguration.setType("email");
        Map<String, String> globalProperties = new HashMap<>();
        globalProperties.put("mail.smtp.user", "abcd");
        globalProperties.put("mail.smtp.port", "587");
        globalProperties.put("maxThread", "100");
        globalProperties.put("keepAliveTimeInMillis", "20000");
        globalProperties.put("mail.smtp.password", "xxxx");
        globalProperties.put("mail.smtp.from", "abcd@gmail.com");
        globalProperties.put("mail.smtp.starttls.enable", "true");
        globalProperties.put("mail.smtp.auth", "true");
        globalProperties.put("jobQueueSize", "10000");
        globalProperties.put("mail.smtp.host", "smtp.gmail.com");
        globalProperties.put("minThread", "8");
        return new EmailEventAdapter(eventAdapterConfiguration, globalProperties);
    }

    @Test(expectedExceptions = {ConnectionUnavailableException.class})
    public void testEmailPublisherConnect1() throws OutputEventAdapterException {
        logger.info("Test case for adaptor with no port.");
        setupCarbonConfig();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1234);
        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("MLEmailAdapter");
        eventAdapterConfiguration.setType("email");
        Map<String, String> globalProperties = new HashMap<>();
        globalProperties.put("mail.smtp.user", "abcd");
        globalProperties.put("maxThread", "100");
        globalProperties.put("keepAliveTimeInMillis", "20000");
        globalProperties.put("mail.smtp.password", "xxxx");
        globalProperties.put("mail.smtp.starttls.enable", "true");
        globalProperties.put("mail.smtp.auth", "true");
        globalProperties.put("jobQueueSize", "10000");
        globalProperties.put("minThread", "8");
        globalProperties.put("mail.smtp.from", "abcd@gmail.com");
        globalProperties.put("mail.smtp.host", "smtp.gmail.com");
        EmailEventAdapter emailEveAdapter = new EmailEventAdapter(eventAdapterConfiguration, globalProperties);
        emailEveAdapter.init();
        emailEveAdapter.connect();
    }

    /**
     * Offline flaw of Email output adaptor
     *
     * @throws OutputEventAdapterException
     */
    @Test
    public void testEmailPublisherInit() throws OutputEventAdapterException {
        logger.info("Test case for initial validation of email output adaptor.");
        EmailEventAdapter emailEventAdapter = getEmailAdaptor();
        emailEventAdapter.init();
        Assert.assertEquals(false, emailEventAdapter.isPolled());
        emailEventAdapter.destroy();
    }

    /**
     * Test connection of Email output adaptor
     *
     * @throws TestConnectionNotSupportedException
     */
    @Test(expectedExceptions = {TestConnectionNotSupportedException.class})
    public void testEmailPublisherTestConnect() throws TestConnectionNotSupportedException {
        logger.info("Test case for test the connection ins valid which is not implemented of email output adaptor.");
        EmailEventAdapter emailEventAdapter = getEmailAdaptor();
        emailEventAdapter.testConnect();
    }

    @Test(dependsOnMethods = {"testEmailPublisherConnect1", "testEmailPublisherConnect2", "testEmailPublisherConnect3",
            "testEmailPublisherConnect5"})
    public void testEmailPublisherConnect() {
        logger.info("Test case for connection of email output adaptor.");
        EmailEventAdapter emailEventAdapter = getEmailAdaptor();
        emailEventAdapter.connect();
        emailEventAdapter.disconnect();
        emailEventAdapter.destroy();
    }

    @Test(expectedExceptions = {ConnectionUnavailableException.class})
    public void testEmailPublisherConnect2() {
        logger.info("Test case for without from url of email output adaptor.");
        setupCarbonConfig();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1234);
        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("MLEmailAdapter");
        eventAdapterConfiguration.setType("email");
        Map<String, String> properties = new HashMap<>();
        properties.put("mail.smtp.user", "abcd");
        properties.put("maxThread", "100");
        properties.put("keepAliveTimeInMillis", "20000");
        properties.put("mail.smtp.password", "xxxx");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.auth", "true");
        properties.put("jobQueueSize", "10000");
        properties.put("minThread", "8");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        EmailEventAdapter emailEventAdapter2 = new EmailEventAdapter(eventAdapterConfiguration, properties);
        emailEventAdapter2.connect();
    }

    @Test(expectedExceptions = {ConnectionUnavailableException.class})
    public void testEmailPublisherConnect3() {
        logger.info("Test case for without port of email output adaptor.");
        setupCarbonConfig();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1234);
        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("MLEmailAdapter");
        eventAdapterConfiguration.setType("email");
        Map<String, String> globalProperties = new HashMap<>();
        globalProperties.put("mail.smtp.user", "abcd");
        globalProperties.put("maxThread", "100");
        globalProperties.put("keepAliveTimeInMillis", "20000");
        globalProperties.put("mail.smtp.starttls.enable", "true");
        globalProperties.put("mail.smtp.auth", "true");
        globalProperties.put("jobQueueSize", "10000");
        globalProperties.put("minThread", "8");
        globalProperties.put("mail.smtp.from", "abcd@gmail.com");
        globalProperties.put("mail.smtp.host", "smtp.gmail.com");
        EmailEventAdapter emailEventAdapter2 = new EmailEventAdapter(eventAdapterConfiguration, globalProperties);
        emailEventAdapter2.connect();
    }

    @Test(expectedExceptions = {ConnectionUnavailableException.class})
    public void testEmailPublisherConnect5() {
        logger.info("Test case for wrong email address of email output adaptor.");
        setupCarbonConfig();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1234);
        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("MLEmailAdapter");
        eventAdapterConfiguration.setType("email");
        Map<String, String> globalProperties = new HashMap<>();
        globalProperties.put("mail.smtp.user", "abcd");
        globalProperties.put("mail.smtp.port", "587");
        globalProperties.put("maxThread", "100");
        globalProperties.put("keepAliveTimeInMillis", "20000");
        globalProperties.put("mail.smtp.password", "xxxx");
        globalProperties.put("mail.smtp.from", "abcd@gmail.com,abcd@gmail.com");
        globalProperties.put("mail.smtp.starttls.enable", "true");
        globalProperties.put("mail.smtp.auth", "true");
        globalProperties.put("jobQueueSize", "10000");
        globalProperties.put("mail.smtp.host", "smtp.gmail.com");
        globalProperties.put("minThread", "8");
        EmailEventAdapter emailEventAdapter2 = new EmailEventAdapter(eventAdapterConfiguration, globalProperties);
        emailEventAdapter2.connect();

    }

    @Test(dependsOnMethods = {"testEmailPublisherConnect1", "testEmailPublisherConnect2", "testEmailPublisherConnect3",
            "testEmailPublisherConnect5"})
    public void testEmailPublisherConnectPasswordEmpty() {
        logger.info("Test case for without password of email output adaptor.");
        setupCarbonConfig();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1234);
        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("MLEmailAdapter");
        eventAdapterConfiguration.setType("email");
        Map<String, String> globalProperties = new HashMap<>();
        globalProperties.put("mail.smtp.user", "abcd");
        globalProperties.put("mail.smtp.port", "587");
        globalProperties.put("maxThread", "100");
        globalProperties.put("keepAliveTimeInMillis", "20000");
        globalProperties.put("mail.smtp.from", "abcd@gmail.com");
        globalProperties.put("mail.smtp.starttls.enable", "true");
        globalProperties.put("mail.smtp.auth", "true");
        globalProperties.put("jobQueueSize", "10000");
        globalProperties.put("mail.smtp.host", "smtp.gmail.com");
        globalProperties.put("minThread", "8");
        EmailEventAdapter emailEventAdapter2 = new EmailEventAdapter(eventAdapterConfiguration, globalProperties);
        emailEventAdapter2.connect();
        // TODO: 9/26/17 logger reader
    }

    @Test(dependsOnMethods = {"testEmailPublisherConnect1", "testEmailPublisherConnect2", "testEmailPublisherConnect3",
            "testEmailPublisherConnect5"})
    public void testEmailPublisherConnectDefaultThreadPoolSize() {
        logger.info("Test case for default thread pool size of email output adaptor.");
        setupCarbonConfig();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1234);
        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("MLEmailAdapter");
        eventAdapterConfiguration.setType("email");
        Map<String, String> globalProperties = new HashMap<>();
        globalProperties.put("mail.smtp.user", "abcd");
        globalProperties.put("mail.smtp.port", "587");
        globalProperties.put("mail.smtp.from", "abcd@gmail.com");
        globalProperties.put("mail.smtp.starttls.enable", "true");
        globalProperties.put("mail.smtp.auth", "true");
        globalProperties.put("mail.smtp.host", "smtp.gmail.com");
        globalProperties.put("mail.smtp.password", "xxxx");
        EmailEventAdapter emailEventAdapter2 = new EmailEventAdapter(eventAdapterConfiguration, globalProperties);
        emailEventAdapter2.connect();
        // TODO: 9/26/17 logger reader
    }

    @Test(dependsOnMethods = {"testEmailPublisherConnect1", "testEmailPublisherConnect2", "testEmailPublisherConnect3",
            "testEmailPublisherConnect5"})
    public void testEmailPublisherPublish() throws InterruptedException {
        logger.info("Test case for publishing email in email output adaptor.");
        EmailEventAdapter emailEventAdapter = getEmailAdaptor();
        try {
            emailEventAdapter.init();
            emailEventAdapter.connect();
            Map<String, String> dynamicProperties = new HashMap<>();
            dynamicProperties.put("email.subject", "test");
            dynamicProperties.put("email.address", "ann@gmail.com");
            dynamicProperties.put("email.type", "text/plain");
            emailEventAdapter.publish("hi", dynamicProperties);
            Thread.sleep(1000);
        } catch (OutputEventAdapterException e) {
            e.printStackTrace();
        } finally {
            emailEventAdapter.disconnect();
            emailEventAdapter.destroy();
        }


    }

    @Test
    public void testEmailPublisherFactory() {
        logger.info("Test case for factory properties of email output adaptor.");
        EmailEventAdapterFactory adapterFactory = new EmailEventAdapterFactory();
        List<Property> dyPropertyList = adapterFactory.getDynamicPropertyList();
        List<Property> propertyList = new ArrayList<>();
        Property property = new Property("email.address");
        property.setRequired(true);
        property.setSecured(false);
        property.setEncrypted(false);
        property.setDisplayName("Email Address");
        property.setDefaultValue(null);
        property.setOptions(null);
        property.setHint("Register publisher for multiple email IDs' separated by \",\"");
        propertyList.add(property);
        property = new Property("email.subject");
        property.setRequired(true);
        property.setSecured(false);
        property.setEncrypted(false);
        property.setDisplayName("Subject");
        property.setDefaultValue(null);
        property.setOptions(null);
        property.setHint(null);
        propertyList.add(property);
        property = new Property("email.type");
        property.setRequired(false);
        property.setSecured(false);
        property.setEncrypted(false);
        property.setDisplayName("Email Type");
        property.setDefaultValue("text/plain");
        property.setOptions(new String[]{"text/plain", "text/html"});
        property.setHint("Select the email format to be sent");
        propertyList.add(property);
        Assert.assertEquals(3, dyPropertyList.size());
        int i = 0;
        for (Property prop : propertyList) {
            Assert.assertEquals(prop.getPropertyName(), dyPropertyList.get(i).getPropertyName());
            Assert.assertEquals(prop.getDefaultValue(), dyPropertyList.get(i).getDefaultValue());
            Assert.assertEquals(prop.getDisplayName(), dyPropertyList.get(i).getDisplayName());
            Assert.assertEquals(prop.getHint(), dyPropertyList.get(i).getHint());
            i++;
        }
        List<String> types = new ArrayList<>();
        types.add("text");
        types.add("xml");
        types.add("json");
        List<String> supportedTypes = adapterFactory.getSupportedMessageFormats();
        Assert.assertEquals(supportedTypes.toString(), types.toString());
        Assert.assertEquals("email", adapterFactory.getType());

        setupCarbonConfig();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1234);

        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("MLEmailAdapter");
        eventAdapterConfiguration.setType("email");
        Map<String, String> globalProperties = new HashMap<>();
        globalProperties.put("mail.smtp.user", "abcd");
        globalProperties.put("mail.smtp.port", "587");
        globalProperties.put("maxThread", "100");
        globalProperties.put("keepAliveTimeInMillis", "20000");
        globalProperties.put("mail.smtp.password", "xxxx");
        globalProperties.put("mail.smtp.from", "abcd@gmail.com");
        globalProperties.put("mail.smtp.starttls.enable", "true");
        globalProperties.put("mail.smtp.auth", "true");
        globalProperties.put("jobQueueSize", "10000");
        globalProperties.put("mail.smtp.host", "smtp.gmail.com");
        globalProperties.put("minThread", "8");
        adapterFactory.createEventAdapter(eventAdapterConfiguration, globalProperties);
    }
}
