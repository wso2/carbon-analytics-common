package org.wso2.carbon.event.output.adapter.jms;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.Property;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.jms.internal.util.JMSConnectionFactory;
import org.wso2.carbon.event.output.adapter.jms.internal.util.JMSEventAdapterConstants;
import org.wso2.carbon.event.output.adapter.jms.util.JMSClient;
import org.wso2.carbon.event.output.adapter.jms.util.ResultContainer;

import javax.naming.NamingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * .
 */

public class JmsPublisherTestCase {
    private OutputEventAdapterConfiguration eventAdapterConfiguration;
    private static final Logger LOGGER = Logger.getLogger(JmsPublisherTestCase.class);
    private static final Path testDir = Paths.get("src", "test", "resources");

    private void setupCarbonConfig(String tenantName) {
        System.setProperty("carbon.home",
                Paths.get(testDir.toString(), "carbon-context").toString());
        System.setProperty("tenant.name", tenantName);
    }

    private JMSEventAdapter getJmsAdaptor() throws NamingException {
        setupCarbonConfig("tenant.name");
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1234);
        eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("JMS Test");
        eventAdapterConfiguration.setType("LOGGER");
        eventAdapterConfiguration.setMessageFormat("text");
        Map<String, String> staticProperties = new HashMap<>();
        staticProperties.put("transport.jms.DestinationType","topic");
        staticProperties.put("transport.jms.Destination","DAS_JMS_OUTPUT_TEST");
        staticProperties.put("transport.jms.ConcurrentPublishers","disallow");
        staticProperties.put("java.naming.factory.initial","org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        staticProperties.put("java.naming.provider.url","vm://localhost");
        staticProperties.put("jms.properties","java.naming.factory.initial:org.apache.activemq.jndi" +
                ".ActiveMQInitialContextFactory");
        staticProperties.put("transport.jms.ConnectionFactoryJNDIName","TopicConnectionFactory");
        //staticProperties.put("transport.LOGGER.UserName","admin");
        //staticProperties.put("transport.LOGGER.Password","admin");
        eventAdapterConfiguration.setStaticProperties(staticProperties);
        Map<String, String> globalProperties = new HashMap<>();
        globalProperties.put("keepAliveTimeInMillis", "20000");
        globalProperties.put("minThread", "8");
        globalProperties.put("jobQueueSize", "10000");
        globalProperties.put("maxThread", "100");

        return new JMSEventAdapter(eventAdapterConfiguration, globalProperties);
    }

    @Test
    public void testConnectInit() throws Exception {
        JMSEventAdapter jmsEventAdapter = getJmsAdaptor();
        jmsEventAdapter.init();
        jmsEventAdapter.destroy();

    }
    @Test
    public void testConnectInitDefault() throws Exception {
        setupCarbonConfig("tenant.name");
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1234);
        eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("JMS Test");
        eventAdapterConfiguration.setType("LOGGER");
        eventAdapterConfiguration.setMessageFormat("text");
        Map<String, String> staticProperties = new HashMap<>();
        staticProperties.put("transport.jms.DestinationType","topic");
        staticProperties.put("transport.jms.Destination","DAS_JMS_OUTPUT_TEST");
        staticProperties.put("java.naming.factory.initial","org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        staticProperties.put("java.naming.provider.url","vm://localhost");
        staticProperties.put("jms.properties","java.naming.factory.initial:org.apache.activemq.jndi" +
                ".ActiveMQInitialContextFactory");
        staticProperties.put("transport.jms.ConnectionFactoryJNDIName","TopicConnectionFactory");
        eventAdapterConfiguration.setStaticProperties(staticProperties);
        Map<String, String> globalProperties = new HashMap<>();
        JMSEventAdapter jmsEventAdapter = new JMSEventAdapter(eventAdapterConfiguration, globalProperties);
        jmsEventAdapter.init();
        jmsEventAdapter.destroy();

    }
    @Test
    public void testTestConnection() throws Exception {
        ResultContainer resultContainer = new ResultContainer(2);
        JMSClient client = new JMSClient("activemq", "DAS_JMS_OUTPUT_TEST","", resultContainer);
        Thread listenerThread = new Thread(client);
        listenerThread.start();
        JMSEventAdapter jmsEventAdapter = getJmsAdaptor();
        jmsEventAdapter.init();
        jmsEventAdapter.testConnect();
        jmsEventAdapter.isPolled();
        client.shutdown();
        jmsEventAdapter.destroy();
    }

    @Test
    public void testConnectionFactory() throws Exception {
        getJmsAdaptor();
        Hashtable<String, String> adaptorProperties = new Hashtable<String, String>();
        adaptorProperties.putAll(eventAdapterConfiguration.getStaticProperties());
        JMSConnectionFactory jmsConnectionFactory = new JMSConnectionFactory(adaptorProperties,
                eventAdapterConfiguration.getName(), adaptorProperties.get(JMSEventAdapterConstants.ADAPTER_JMS_DESTINATION),
                1);
        jmsConnectionFactory.createConnection();
        jmsConnectionFactory.getConnectionFromPool();
        jmsConnectionFactory.getContext();
        jmsConnectionFactory.getDestination();
        jmsConnectionFactory.getReplyDestinationType();
        jmsConnectionFactory.getReplyToDestination();
        jmsConnectionFactory.isJmsSpec11();
        jmsConnectionFactory.isQueue();
    }
    @Test
    public void testConnect() throws Exception {
        JMSEventAdapter jmsEventAdapter = getJmsAdaptor();
        jmsEventAdapter.init();
        jmsEventAdapter.connect();
        jmsEventAdapter.disconnect();
        jmsEventAdapter.destroy();

    }

    @Test
    public void testJmsPublisherPublish() throws NamingException {
        LOGGER.info("Test case for publishing email in email output adaptor.");
        JMSEventAdapter jmsEventAdapter = getJmsAdaptor();
        ResultContainer resultContainer = new ResultContainer(2);
        JMSClient client = new JMSClient("activemq", "DAS_JMS_OUTPUT_TEST","", resultContainer);
        Thread listenerThread = new Thread(client);
        listenerThread.start();
        try {
            jmsEventAdapter.init();
            jmsEventAdapter.connect();
            Map<String, String> dynamicProperties = new HashMap<>();
            dynamicProperties.put("transport.jms.Header","header:val");
            jmsEventAdapter.publish("hi", dynamicProperties);
            Thread.sleep(1000);
            jmsEventAdapter.disconnect();
            jmsEventAdapter.destroy();
        } catch (OutputEventAdapterException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        client.shutdown();
    }

    @Test
    public void testJmsPublisherFactory() {
        LOGGER.info("Test case for factory properties of email output adaptor.");
        JMSEventAdapterFactory adapterFactory = new JMSEventAdapterFactory();
        List<Property> dyPropertyList= adapterFactory.getDynamicPropertyList();
        List<Property> propertyList = new ArrayList<>();
        Property property = new Property("transport.jms.Header");
        property.setRequired(false);
        property.setSecured(false);
        property.setEncrypted(false);
        property.setDisplayName("Header");
        property.setDefaultValue(null);
        property.setOptions(null);
        property.setHint("Define Transport Headers (eg header_name1:header_value1,header_name2:header_value2 )");
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
        types.add("map");
        types.add("xml");
        types.add("json");
        List<String> supportedTypes=adapterFactory.getSupportedMessageFormats();
        Assert.assertEquals(supportedTypes.toString(),types.toString());
        Assert.assertEquals("jms",adapterFactory.getType());

        setupCarbonConfig("tenant.name");
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1234);
        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("JMS");
        eventAdapterConfiguration.setType("jms");
        eventAdapterConfiguration.setMessageFormat("text");
        Map<String, String> staticProperties = new HashMap<>();
        eventAdapterConfiguration.setStaticProperties(staticProperties);
        Map<String, String> globalProperties = new HashMap<>();
        adapterFactory.createEventAdapter(eventAdapterConfiguration, globalProperties);
        adapterFactory.getStaticPropertyList();
    }
}
