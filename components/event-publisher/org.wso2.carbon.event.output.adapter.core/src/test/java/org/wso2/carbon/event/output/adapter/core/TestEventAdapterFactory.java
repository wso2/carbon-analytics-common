package org.wso2.carbon.event.output.adapter.core;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The logger event adapter factory class to create a logger output adapter
 */
public class TestEventAdapterFactory extends OutputEventAdapterFactory {

    @Override
    public String getType() {
        return "test";
    }

    @Override
    public List<String> getSupportedMessageFormats() {
        List<String> supportedMessageFormats = new ArrayList<String>();
        supportedMessageFormats.add(MessageType.TEXT);
        supportedMessageFormats.add(MessageType.XML);
        supportedMessageFormats.add(MessageType.JSON);
        return supportedMessageFormats;
    }

    @Override
    public List<Property> getStaticPropertyList() {
        List<Property> propertyList = new ArrayList<>();
        Property property = new Property("company");
        property.setRequired(true);
        property.setSecured(false);
        property.setEncrypted(false);
        property.setDisplayName("Company Name");
        property.setDefaultValue(null);
        property.setOptions(null);
        property.setHint("To test User ID for Testing");
        propertyList.add(property);
        return propertyList;
    }

    @Override
    public List<Property> getDynamicPropertyList() {
        List<Property> propertyList = new ArrayList<>();
        Property property = new Property("user.address");
        property.setRequired(true);
        property.setSecured(false);
        property.setEncrypted(false);
        property.setDisplayName("User Address");
        property.setDefaultValue(null);
        property.setOptions(null);
        property.setHint("To test User ID for Testing");
        propertyList.add(property);
        return propertyList;
    }

    @Override
    public String getUsageTips() {
        return null;
    }

    @Override
    public OutputEventAdapter createEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration, Map<String, String> globalProperties) {
        return new TestEventAdapter(eventAdapterConfiguration, globalProperties);
    }
}