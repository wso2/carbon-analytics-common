package org.wso2.carbon.event.output.adapter.email;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.ConnectionUnavailableException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;

public class TenantAwareEmailEventAdapter extends EmailEventAdapter{

    private static final Log log = LogFactory.getLog(TenantAwareEmailEventAdapter.class);
    private static ThreadPoolExecutor threadPoolExecutor;
    private static Session session;
    private OutputEventAdapterConfiguration eventAdapterConfiguration;
    private Map<String, String> globalProperties;
    private int tenantId;
    /**
     * Default from address for outgoing messages.
     */
    private InternetAddress smtpFromAddress = null;
    public TenantAwareEmailEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration,
            Map<String, String> globalProperties) {
        super(eventAdapterConfiguration, globalProperties);
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }

    public void connect() throws ConnectionUnavailableException {
        Map<String, String> propertiesMap = new HashMap<String, String>();
        propertiesMap.put("mail.smtp.user","resourcesiam5");
        propertiesMap.put("mail.smtp.port","587");
        propertiesMap.put("maxThread","100");
        propertiesMap.put("keepAliveTimeInMillis","20000");
        propertiesMap.put("mail.smtp.password","xxxx");
        propertiesMap.put("mail.smtp.from","resourcesiam5@gmail.com");
        propertiesMap.put("mail.smtp.starttls.enable","true");
        propertiesMap.put("mail.smtp.auth","true");
        propertiesMap.put("mail.jobQueueSize","10000");
        propertiesMap.put("mail.smtp.host","smtp.gmail.com");
        propertiesMap.put("minThread","8");

        int tenantid = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        log.info("sending email for tenant id: "+ tenantid);
        if(tenantid == 1) {

            overrideGlobalPropertiesWithTenantProperties(globalProperties, propertiesMap);
        }

        super.connect();

    }


    /**
     * This method override the global properties if tenant properties are present.
     *
     * @param globalProps global properties from output-event-adapters.xml.
     * @param tenantProps tenant wise properties configured in tenant wise Publishers.
     * @param tenantProps
     */
    private void overrideGlobalPropertiesWithTenantProperties(Map<String, String> globalProps,
            Map<String, String> tenantProps) {

        for (String key : tenantProps.keySet()) {
            if (globalProps.containsKey(key) && tenantProps.get(key) != null) {
                globalProps.put(key, tenantProps.get(key));
            }
        }
    }
}
