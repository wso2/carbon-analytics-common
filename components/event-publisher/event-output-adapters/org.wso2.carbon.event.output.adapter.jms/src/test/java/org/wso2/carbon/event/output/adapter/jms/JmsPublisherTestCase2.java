package org.wso2.carbon.event.output.adapter.jms;

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
public class JmsPublisherTestCase2 {
    private static final Logger jms = Logger.getLogger(JmsPublisherTestCase2.class);
    private static final Path testDir = Paths.get("src", "test", "resources");

    private void setupCarbonConfig(String tenantName) {
        System.setProperty("carbon.home",
                Paths.get(testDir.toString(), "carbon-context").toString());
        System.setProperty("tenant.name", tenantName);
    }

    private JMSEventAdapter getJmsAdaptor() {
        setupCarbonConfig("tenant.name");
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1234);
        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("JMS Test");
        eventAdapterConfiguration.setType("jms");
        eventAdapterConfiguration.setMessageFormat("text");
        Map<String, String> staticProperties = new HashMap<>();
        staticProperties.put("transport.jms.DestinationType","topic");
        //staticProperties.put("jms.properties",null);
        staticProperties.put("transport.jms.Destination","topicText");
        staticProperties.put("transport.jms.ConcurrentPublishers","allow");
        staticProperties.put("java.naming.factory.initial","org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        staticProperties.put("java.naming.provider.url","tcp://localhost:61616");
        staticProperties.put("transport.jms.ConnectionFactoryJNDIName","TopicConnectionFactory");
        staticProperties.put("transport.jms.UserName","admin");
        staticProperties.put("transport.jms.Password","admin");
        //staticProperties.put("jms.secured.properties",null);
        eventAdapterConfiguration.setStaticProperties(staticProperties);
        Map<String, String> globalProperties = new HashMap<>();
        globalProperties.put("keepAliveTimeInMillis", "20000");
        globalProperties.put("minThread", "8");
        globalProperties.put("jobQueueSize", "10000");
        globalProperties.put("maxThread", "100");
        return new JMSEventAdapter(eventAdapterConfiguration, globalProperties);
    }

    @Test
    public void testJmsPublisherInit() throws OutputEventAdapterException {
        JMSEventAdapter jmsEventAdapter = getJmsAdaptor();
        jmsEventAdapter.init();
        jmsEventAdapter.isPolled();
        jmsEventAdapter.destroy();
    }

    /**
     * Test connection of Jms output adaptor
     *
     * @throws TestConnectionNotSupportedException
     */
    @Test
    public void testJmsPublisherTestConnect() throws TestConnectionNotSupportedException {
        jms.info("Test case for test the connection ins valid which is not implemented of email output adaptor.");
        JMSEventAdapter jmsEventAdapter = getJmsAdaptor();
        jmsEventAdapter.testConnect();
    }

    @Test
    public void testJmsPublisherConnect() throws OutputEventAdapterException {
        jms.info("Test case for connection of email output adaptor.");
        JMSEventAdapter jmsEventAdapter = getJmsAdaptor();
        jmsEventAdapter.init();
        jmsEventAdapter.connect();
        jmsEventAdapter.disconnect();
        jmsEventAdapter.destroy();
    }

    @Test
    public void testJmsPublisherPublish() {
        jms.info("Test case for publishing email in email output adaptor.");
        JMSEventAdapter jmsEventAdapter = getJmsAdaptor();
        try {
            jmsEventAdapter.init();
            Map<String, String> dynamicProperties = new HashMap<>();
            dynamicProperties.put("uniqueId", "*****");
            jmsEventAdapter.publish("hi", dynamicProperties);
        } catch (OutputEventAdapterException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testJmsPublisherFactory() {
        jms.info("Test case for factory properties of email output adaptor.");
        JMSEventAdapterFactory adapterFactory = new JMSEventAdapterFactory();
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
        Assert.assertEquals("jms",adapterFactory.getType());

        setupCarbonConfig("tenant.name");
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1234);
        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("log");
        eventAdapterConfiguration.setType("jms");
        eventAdapterConfiguration.setMessageFormat("text");
        Map<String, String> staticProperties = new HashMap<>();
        eventAdapterConfiguration.setStaticProperties(staticProperties);
        Map<String, String> globalProperties = new HashMap<>();
        adapterFactory.createEventAdapter(eventAdapterConfiguration, globalProperties);
    }
}
