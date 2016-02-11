/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.throttle.event.core.internal;

import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.throttle.event.core.ThrottlerService;
import org.wso2.carbon.throttle.event.core.exception.ThrottleConfigurationException;
import org.wso2.carbon.throttle.event.core.internal.util.ThrottleHelper;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class which does throttling.
 * 1. Get an instance
 * 2. Start
 * 3. Add rules
 * 4. Invoke isThrottled with {@link org.wso2.carbon.throttle.event.core.internal.CarbonThrottlerService} object
 */
public class CarbonThrottlerService implements ThrottlerService {
    private static final Logger log = Logger.getLogger(CarbonThrottlerService.class);

    private SiddhiManager siddhiManager;
    private InputHandler eligibilityStreamInputHandler;
    private Map<String,InputHandler> requestStreamInputHandlerMap;
    private Map<String,ExecutionPlanRuntime> ruleRuntimeMap;
    private Map<String, ResultContainer> resultMap;

    private AtomicInteger ruleCount = new AtomicInteger(0);

    private ExecutionPlanRuntime commonExecutionPlanRuntime;

    private DataPublisher dataPublisher = null;
    private String streamID;

    private CEPConfig cepConfig;

    public CarbonThrottlerService() {
        requestStreamInputHandlerMap = new ConcurrentHashMap<String, InputHandler>();
        ruleRuntimeMap = new ConcurrentHashMap<String, ExecutionPlanRuntime>();
        resultMap = new ConcurrentHashMap<String, ResultContainer>();
        this.start();
    }

    /**
     * Starts throttler engine. Calling method should catch the exceptions and call stop to clean up.
     */
    private void start() {
        siddhiManager = new SiddhiManager();
        ThrottleHelper.loadDataSourceConfiguration(siddhiManager);

        String commonExecutionPlan = "" +
                "define stream EligibilityStream (rule string, messageID string, isEligible bool, key string);\n" +
                "\n" +
                "@From(eventtable='rdbms', datasource.name='org_wso2_throttle_DataSource', " +
                "table.name='ThrottleTable', bloom.filters = 'enable', bloom.validity='100')" +
                "define table ThrottleTable (THROTTLE_KEY string, isThrottled bool);\n" +
                "\n" +
                "FROM EligibilityStream[isEligible==false]\n" +
                "SELECT rule, messageID, false AS isThrottled\n" +
                "INSERT INTO ThrottleStream;\n" +
                "\n" +
                "FROM EligibilityStream[isEligible==true]#window.length(1) LEFT OUTER JOIN ThrottleTable\n" +
                "\tON ThrottleTable.THROTTLE_KEY == EligibilityStream.key\n" +
                "SELECT rule, messageID, ifThenElse((ThrottleTable.isThrottled is null),false,ThrottleTable.isThrottled) AS isThrottled\n" +
                "INSERT INTO ThrottleStream;";

        commonExecutionPlanRuntime = siddhiManager.createExecutionPlanRuntime(commonExecutionPlan);

        //add callback to get local throttling result and add it to ResultContainer
        commonExecutionPlanRuntime.addCallback("ThrottleStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                for (Event event : events) {
                    resultMap.get(event.getData(1).toString()).addResult((String) event.getData(0), (Boolean) event.getData(2));
                }
            }
        });

        //get and register inputHandler
        this.eligibilityStreamInputHandler = commonExecutionPlanRuntime.getInputHandler("EligibilityStream");

        commonExecutionPlanRuntime.start();

        try {
            cepConfig = ThrottleHelper.loadCEPConfig();
        } catch (ThrottleConfigurationException e) {
            log.error("Error in initializing throttling engine. " + e.getMessage(), e);
        }

        //initialize binary data publisher to send requests to global CEP instance
        initDataPublisher();
    }

//    //todo: this method has not being implemented completely. Will be done after doing perf tests.
//    private void deployRuleToGlobalCEP(String templateID, String parameter1, String parameter2) {
//        String queries = QueryTemplateStore.constructEnforcementQuery();
//
//        ExecutionPlanRuntime ruleRuntime = siddhiManager.createExecutionPlanRuntime("define stream RequestStream (messageID string, app_key string, api_key string, resource_key string, app_tier string, api_tier string, resource_tier string); " +
//                queries);
//
//        GlobalCEPClient globalCEPClient = new GlobalCEPClient();
//        globalCEPClient.deployExecutionPlan(queries);
//    }

    /**
     * Deploy the provided policy file and store the references for usages. Each policy will be deployed as a
     * separate ExecutionPlans and will be connected with common ExecutionPlan using call backs. Each policy will
     * return a single throttling decision. Throttling decision of a request is the aggregation od these decisions.
     * @param policy
     * @throws ThrottleConfigurationException
     */
    public void deployLocalCEPRules(Policy policy) throws ThrottleConfigurationException {
        String name = policy.getName();
        StringBuilder eligibilityQueriesBuilder = new StringBuilder();
        eligibilityQueriesBuilder.append("define stream RequestStream (" + QueryTemplateStore.getInstance()
                .loadThrottlingAttributes() + "); \n");
        eligibilityQueriesBuilder.append(policy.getEligibilityQuery());

        ExecutionPlanRuntime ruleRuntime = siddhiManager.createExecutionPlanRuntime(
                eligibilityQueriesBuilder.toString());

        //Add call backs. Here, we take output events and insert into EligibilityStream
        ruleRuntime.addCallback("EligibilityStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                try {
                    getEligibilityStreamInputHandler().send(events);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Error occurred when publishing to EligibilityStream.", e);
                }
            }
        });

        ruleRuntime.start();

        //get and register input handler for RequestStream, so isThrottled() can use it.
        if (!requestStreamInputHandlerMap.containsKey(name)) {
            requestStreamInputHandlerMap.put(name, ruleRuntime.getInputHandler("RequestStream"));
            ruleRuntimeMap.put(name, ruleRuntime);
            ruleCount.incrementAndGet();
        } else {
            throw new ThrottleConfigurationException("Throttling policy configuration with name " + name + "is " +
                    "already defined. Please create a policy with a different name.");
        }

    }

    /**
     * Undeploy the throttling policy with given name if already deployed.
     * @param policyName
     */
    public void undeployLocalCEPRules(String policyName){
        ExecutionPlanRuntime ruleRuntime = ruleRuntimeMap.get(policyName);
        if(ruleRuntime != null){
            ruleCount.decrementAndGet();
            requestStreamInputHandlerMap.remove(policyName);
            ruleRuntime.shutdown();
            ruleRuntimeMap.remove(policyName);
        }

    }


    /**
     * Returns whether the given throttleRequest is throttled.
     *
     * @param throttleRequest User throttleRequest to APIM which needs to be checked whether throttled
     * @return Throttle status for current throttleRequest
     */
    public boolean isThrottled(Object[] throttleRequest) {
        if (ruleCount.get() != 0) {
            String uniqueKey = (String) throttleRequest[0];
            ResultContainer result = new ResultContainer(ruleCount.get());
            resultMap.put(uniqueKey.toString(), result);
            for(InputHandler inputHandler : requestStreamInputHandlerMap.values()) {
                try {
                    inputHandler.send(Arrays.copyOf(throttleRequest, throttleRequest.length));
                } catch (InterruptedException e) {
                    //interrupt current thread so that interrupt can propagate
                    Thread.currentThread().interrupt();
                    log.error(e.getMessage(), e);
                }
            }
            //Blocked call to return synchronous result
            boolean isThrottled = false;
            try {
                isThrottled = result.isThrottled();
                if (log.isDebugEnabled()) {
                    log.debug("Throttling status for request to API " + throttleRequest[2] + " is " + isThrottled);
                }
            } catch (InterruptedException e) {
                //interrupt current thread so that interrupt can propagate
                Thread.currentThread().interrupt();
                log.error(e.getMessage(), e);
            }
            if (!isThrottled) {                                           //Only send served throttleRequest to global throttler
                sendToGlobalThrottler(throttleRequest);
            }
            resultMap.remove(uniqueKey);
            return isThrottled;
        } else {
            return false;
        }
    }

    public void stop() {
        if (siddhiManager != null) {
            siddhiManager.shutdown();
        }
        if (commonExecutionPlanRuntime != null) {
            commonExecutionPlanRuntime.shutdown();
        }
    }


    private InputHandler getEligibilityStreamInputHandler() {
        return eligibilityStreamInputHandler;
    }

    private void sendToGlobalThrottler(Object[] throttleRequest) {
        org.wso2.carbon.databridge.commons.Event event = new org.wso2.carbon.databridge.commons.Event(streamID,
                System.currentTimeMillis(),null,null,throttleRequest);
        dataPublisher.tryPublish(event);
    }

    //todo exception handling
    private void initDataPublisher() {
        try {
            dataPublisher = new DataPublisher("Binary", "tcp://" + cepConfig.getHostname() + ":" + cepConfig.getBinaryTCPPort(),
                    "ssl://" + cepConfig.getHostname() + ":" + cepConfig.getBinarySSLPort(), cepConfig.getUsername(),
                    cepConfig.getPassword());
            streamID = DataBridgeCommonsUtils.generateStreamId(cepConfig.getStreamName(), cepConfig.getStreamVersion());
        } catch (DataEndpointAgentConfigurationException e) {
            log.error(e.getMessage(), e);
        } catch (DataEndpointException e) {
            log.error(e.getMessage(), e);
        } catch (DataEndpointConfigurationException e) {
            log.error(e.getMessage(), e);
        } catch (DataEndpointAuthenticationException e) {
            log.error(e.getMessage(), e);
        } catch (TransportException e) {
            log.error(e.getMessage(), e);
        }


    }

}
