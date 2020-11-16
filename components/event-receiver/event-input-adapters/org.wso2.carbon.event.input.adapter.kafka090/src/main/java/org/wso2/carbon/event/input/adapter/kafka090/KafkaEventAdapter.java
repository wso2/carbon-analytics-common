/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.input.adapter.kafka090;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adapter.core.EventAdapterConstants;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapter;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterRuntimeException;
import org.wso2.carbon.event.input.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.input.adapter.kafka090.internal.util.KafkaEventAdapterConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public final class KafkaEventAdapter implements InputEventAdapter {

    private static final Log log = LogFactory.getLog(KafkaEventAdapter.class);
    private final InputEventAdapterConfiguration eventAdapterConfiguration;
    private final Map<String, String> globalProperties;
    private InputEventAdapterListener eventAdaptorListener;
    private final String id = UUID.randomUUID().toString();
    private int tenantId;
    private ConsumerKafkaAdaptor consumerKafkaAdaptor;


    public KafkaEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration, Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }

    @Override
    public void init(InputEventAdapterListener eventAdaptorListener) throws InputEventAdapterException {
        validateInputEventAdapterConfigurations();
        this.eventAdaptorListener = eventAdaptorListener;
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("not-supported");
    }

    @Override
    public void connect() {
        tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        createKafkaAdaptorListener(eventAdaptorListener, eventAdapterConfiguration);
    }

    @Override
    public void disconnect() {
        if (consumerKafkaAdaptor != null) {
            consumerKafkaAdaptor.shutdown();
            String topic = eventAdapterConfiguration.getProperties().get(KafkaEventAdapterConstants.ADAPTER_MESSAGE_TOPIC);
            log.debug("Adapter " + eventAdapterConfiguration.getName() + " disconnected " + topic);
        }
    }

    @Override
    public void destroy() {
    }

    public InputEventAdapterListener getEventAdaptorListener() {
        return eventAdaptorListener;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    private static Properties getConsumerProperties(String bootstrapServers, String groupId,
                                                    String optionalConfigs) {
        try {
            Properties props = new Properties();
            props.put(KafkaEventAdapterConstants.ADAPTOR_SUSCRIBER_BOOTSTRAP_SERVERS, bootstrapServers);
            props.put(KafkaEventAdapterConstants.ADAPTOR_SUSCRIBER_GROUP_ID, groupId);
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("partition.assignment.strategy", "org.apache.kafka.clients.consumer.RangeAssignor");

            if (optionalConfigs != null) {
                String[] optionalProperties = optionalConfigs.split(",");

                if (optionalProperties != null) {
                    for (String header : optionalProperties) {
                        String[] configPropertyWithValue = header.split(":", 2);
                        if (configPropertyWithValue.length == 2) {
                            props.put(configPropertyWithValue[0], configPropertyWithValue[1]);
                        } else {
                            log.warn("Optional configuration property not defined in the correct format.\nRequired - property_name1:property_value1,property_name2:property_value2\nFound - " + optionalConfigs);
                        }
                    }
                }
            }
            return props;
        } catch (NoClassDefFoundError e) {
            throw new InputEventAdapterRuntimeException("Cannot access kafka090 context due to missing jars", e);
        }
    }

    private void createKafkaAdaptorListener(
            InputEventAdapterListener inputEventAdapterListener,
            InputEventAdapterConfiguration inputEventAdapterConfiguration) {

        Map<String, String> brokerProperties = new HashMap<String, String>();
        brokerProperties.putAll(inputEventAdapterConfiguration.getProperties());
        String bootstrapServers = brokerProperties.get(KafkaEventAdapterConstants.ADAPTOR_SUSCRIBER_BOOTSTRAP_SERVERS);
        String groupID = brokerProperties.get(KafkaEventAdapterConstants.ADAPTOR_SUSCRIBER_GROUP_ID);
        String threadsStr = brokerProperties.get(KafkaEventAdapterConstants.ADAPTOR_SUSCRIBER_THREADS);
        String optionalConfiguration = brokerProperties.get(KafkaEventAdapterConstants.ADAPTOR_OPTIONAL_CONFIGURATION_PROPERTIES);
        int threads = Integer.parseInt(threadsStr);
        String topic = inputEventAdapterConfiguration.getProperties().get(KafkaEventAdapterConstants.ADAPTOR_SUSCRIBER_TOPIC);
        boolean enableAsyncCommit = Boolean.parseBoolean(inputEventAdapterConfiguration.getProperties().
                get(KafkaEventAdapterConstants.COMMIT_ASYNC));
        consumerKafkaAdaptor = new ConsumerKafkaAdaptor(topic, tenantId,
                KafkaEventAdapter.getConsumerProperties(bootstrapServers, groupID, optionalConfiguration), enableAsyncCommit);
        consumerKafkaAdaptor.run(threads, inputEventAdapterListener);
    }

    @Override
    public boolean isEventDuplicatedInCluster() {
        return Boolean.parseBoolean(eventAdapterConfiguration.getProperties().get(EventAdapterConstants.EVENTS_DUPLICATED_IN_CLUSTER));
    }

    @Override
    public boolean isPolling() {
        return true;
    }

    private void validateInputEventAdapterConfigurations() throws InputEventAdapterException {
        String threadsProperty = eventAdapterConfiguration.getProperties().get(KafkaEventAdapterConstants.ADAPTOR_SUSCRIBER_THREADS);
        try{
            Integer.parseInt(threadsProperty);
        } catch (NumberFormatException e){
            throw new InputEventAdapterException("Invalid value set for property 'Threads': " + threadsProperty, e);
        }
    }

}