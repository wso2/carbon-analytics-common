package org.wso2.carbon.event.output.adapter.sms;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
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
public class SmsOutputAdaptorTestCase2 {
    PrivilegedCarbonContext privilegedCarbonContext;
    private static final Logger logger = Logger.getLogger(SmsOutputAdaptorTestCase2.class);
    private static final Path testDir = Paths.get("src", "test", "resources");

    private void setupCarbonConfig() {
        System.setProperty("carbon.home",
                Paths.get(testDir.toString(), "carbon-context").toString());
        System.setProperty("tenant.name", "tenant.name");
    }

    private SMSEventAdapter getHttpAdaptor() {
        setupCarbonConfig();
        privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
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
        globalProperties.put("keepAliveTimeInMillis", "20000");
        globalProperties.put("maxThread", "100");
        globalProperties.put("minThread", "8");
        globalProperties.put("defaultMaxConnectionsPerHost", "50");
        globalProperties.put("maxTotalConnections", "1000");
        return new SMSEventAdapter(eventAdapterConfiguration, globalProperties);
    }


    @Test
    public void testHttpPublisherPublish() throws AxisFault {
        AxisConfiguration axisConfiguration = new AxisConfiguration();
        axisConfiguration.addParameter("systemId","dasA");
        axisConfiguration.addParameter("systemType","DAS");
        axisConfiguration.addParameter("password","admin");
        axisConfiguration.addParameter("host","localhost");
        axisConfiguration.addParameter("port",2775);
        axisConfiguration.addParameter("phoneNumber","+94771117673");
        org.apache.axis2.context.ConfigurationContext serverConfigContext= new ConfigurationContext(axisConfiguration);
        ConfigurationContext clientConfigContext= new ConfigurationContext(axisConfiguration);
        ConfigurationContextService contextService = new ConfigurationContextService(serverConfigContext,
                clientConfigContext);
        SMSEventAdapterServiceValueHolder.registerConfigurationContextService(contextService);
        logger.info("Test case for publishing email in email output adaptor.");
        SMSEventAdapter smsEventAdapter = getHttpAdaptor();
        try {
            smsEventAdapter.init();
            smsEventAdapter.connect();
            Map<String, String> dynamicProperties = new HashMap<>();
            dynamicProperties.put("sms.no", "+94771117673");
            smsEventAdapter.publish("hi", dynamicProperties);
            Thread.sleep(1000);
        } catch (OutputEventAdapterException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            smsEventAdapter.disconnect();
            smsEventAdapter.destroy();
        }

    }
    @Test
    public void testHttpPublisherPublish2() throws AxisFault {
        logger.info("Test case for publishing email in email output adaptor.");
        SMSEventAdapter smsEventAdapter = getHttpAdaptor();
        try {
            smsEventAdapter.init();
            smsEventAdapter.connect();
            Map<String, String> dynamicProperties = new HashMap<>();
            dynamicProperties.put("sms.no", "+94771117673");
            smsEventAdapter.publish("hi", dynamicProperties);
            Thread.sleep(1000);
        } catch (OutputEventAdapterException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            smsEventAdapter.disconnect();
            smsEventAdapter.destroy();
        }

    }

}
