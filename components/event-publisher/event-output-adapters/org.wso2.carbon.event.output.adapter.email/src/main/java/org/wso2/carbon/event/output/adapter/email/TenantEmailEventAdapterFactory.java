package org.wso2.carbon.event.output.adapter.email;

import org.wso2.carbon.event.output.adapter.core.MessageType;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TenantEmailEventAdapterFactory extends EmailEventAdapterFactory {

    public String getType() {
        return "email-is";
    }

    public List<String> getSupportedMessageFormats() {
        List<String> supportedMessageFormats = new ArrayList<String>();
        supportedMessageFormats.add(MessageType.TEXT);
        supportedMessageFormats.add(MessageType.XML);
        supportedMessageFormats.add(MessageType.JSON);
        return supportedMessageFormats;
    }

    public List<Property> getStaticPropertyList() {
        return null;
    }


    public String getUsageTips() {
        return null;
    }


    public OutputEventAdapter createEventAdapter(OutputEventAdapterConfiguration outputEventAdapterConfiguration,
            Map<String, String> map) {

        return new TenantAwareEmailEventAdapter(outputEventAdapterConfiguration, map);
    }
}
