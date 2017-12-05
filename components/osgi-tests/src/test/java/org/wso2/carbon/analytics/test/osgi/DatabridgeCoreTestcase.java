/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.wso2.carbon.analytics.test.osgi;

import org.awaitility.Awaitility;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.SessionTimeoutException;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;


/**
 * OSGI Testcase for Databridge core.
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class DatabridgeCoreTestcase {

    private static final String DATABRIDGE_CORE_BUNDLE_NAME = "org.wso2.carbon.databridge.core";
    private String sessionId = "";

//    private static final int HTTP_PORT = 9090;
//    private static final String HOSTNAME = "localhost";
//    private static final String API_CONTEXT_PATH = "/stores/query";
//    private static final String CONTENT_TYPE_JSON = "application/json";
//    private static final String HTTP_METHOD_POST = "POST";
//    private static final String TABLENAME = "SmartHomeTable";
//    private final String DEFAULT_USER_NAME = "admin";
//    private final String DEFAULT_PASSWORD = "admin";
//    private final Gson gson = new Gson();

    @Inject
    private BundleContext bundleContext;

    @Inject
    private DataBridgeReceiverService dataBridgeReceiverService;

    @Configuration
    public Option[] createConfiguration() {
        return new Option[]{
        };
    }

    private Bundle getBundle(String name) {
        Bundle bundle = null;
        for (Bundle b : bundleContext.getBundles()) {
            if (b.getSymbolicName().equals(name)) {
                bundle = b;
                break;
            }
        }
        Assert.assertNotNull(bundle, "Bundle should be available. Name: " + name);
        return bundle;
    }

    @Test
    public void testDatabridgeCoreBundle() {
        Bundle coreBundle = getBundle(DATABRIDGE_CORE_BUNDLE_NAME);
        Assert.assertEquals(coreBundle.getState(), Bundle.ACTIVE);
    }

    @Test(dependsOnMethods = "testDatabridgeCoreBundle")
    public void defineStreamForDataReceiver() throws InterruptedException {

        Awaitility.await().atLeast(4000, TimeUnit.MILLISECONDS);

        String streamDefinition = "{" +
                "  'name':'org.wso2.esb.MediatorStatistics'," +
                "  'version':'2.3.0'," +
                "  'nickName': 'Stock Quote Information'," +
                "  'description': 'Some Desc'," +
                "  'tags':['foo', 'bar']," +
                "  'metaData':[" +
                "          {'name':'ipAdd','type':'STRING'}" +
                "  ]," +
                "  'payloadData':[" +
                "          {'name':'symbol','type':'STRING'}," +
                "          {'name':'price','type':'DOUBLE'}," +
                "          {'name':'volume','type':'INT'}," +
                "          {'name':'max','type':'DOUBLE'}," +
                "          {'name':'min','type':'Double'}" +
                "  ]" +
                "}";

        //Verify the Databridge Receiver Login operataion
        try {
            sessionId = dataBridgeReceiverService.login("admin", "admin");
        } catch (AuthenticationException e) {
            Assert.fail("Authentication error when login to databridge. " + e);
        }

        //Verify define stream API
        try {
            dataBridgeReceiverService.defineStream(sessionId, streamDefinition, null);
        } catch (DifferentStreamDefinitionAlreadyDefinedException | MalformedStreamDefinitionException |
                SessionTimeoutException e) {
            Assert.fail("Exception when defining stream definition. ", e);
        }

        //Verify the getStreamDefinition method of Databridge Receiver
        try {
            StreamDefinition streamDefinitionObject = dataBridgeReceiverService.getStreamDefinition(sessionId,
                    "org.wso2.esb.MediatorStatistics", "2.3.0");
            if (streamDefinitionObject != null) {
                Assert.assertNotNull(streamDefinitionObject, "Defined stream definition successfully retrieved");
            }
        } catch (SessionTimeoutException | StreamDefinitionNotFoundException | StreamDefinitionStoreException e) {
            Assert.fail("Exception when retrieving the defined stream definition", e);
        }
    }

    @Test(dependsOnMethods = "defineStreamForDataReceiver")
    public void saveStreamDefinitionForDataReceiver() throws InterruptedException {

        //Verify the Databridge Receiver Login operataion
        try {
            sessionId = dataBridgeReceiverService.login("admin", "admin");
        } catch (AuthenticationException e) {
            Assert.fail("Authentication error when login to databridge. " + e);
        }

        try {
            StreamDefinition newStreamDefinition = new StreamDefinition("org.wso2.sp.temperatureStream", "1.0.0");

            List<Attribute> payloadAttributeList = new ArrayList<>();
            payloadAttributeList.add(new Attribute("sensorId", AttributeType.INT));
            payloadAttributeList.add(new Attribute("temperature", AttributeType.DOUBLE));
            payloadAttributeList.add(new Attribute("location", AttributeType.STRING));
            newStreamDefinition.setPayloadData(payloadAttributeList);

            newStreamDefinition.setPayloadData(payloadAttributeList);
            try {
                dataBridgeReceiverService.saveStreamDefinition(sessionId, newStreamDefinition);

                StreamDefinition streamDefinitionObject = null;
                try {
                    streamDefinitionObject = dataBridgeReceiverService.getStreamDefinition(sessionId,
                            "org.wso2.sp.temperatureStream", "1.0.0");
                    if (streamDefinitionObject != null) {
                        Assert.assertNotNull(streamDefinitionObject,
                                "Defined stream definition successfully retrieved");
                    }
                } catch (StreamDefinitionNotFoundException e) {
                    Assert.fail("Exception when retrieving the defined stream definition", e);
                }

            } catch (SessionTimeoutException | StreamDefinitionStoreException |
                    DifferentStreamDefinitionAlreadyDefinedException e) {
                Assert.fail("Exception when saving the stream definition", e);
            }

        } catch (MalformedStreamDefinitionException e) {
            Assert.fail("Exception when initialising the stream definition", e);
        }
    }

    @Test(dependsOnMethods = "saveStreamDefinitionForDataReceiver")
    public void getAllStreamDefinitionsForDataReceiver() throws InterruptedException {

        //Verify the Databridge Receiver Login operataion
        try {
            sessionId = dataBridgeReceiverService.login("admin", "admin");
        } catch (AuthenticationException e) {
            Assert.fail("Authentication error when login to databridge. " + e);
        }

        try {
            List<StreamDefinition> streamDefinitionList = dataBridgeReceiverService.getAllStreamDefinitions(sessionId);
            Assert.assertEquals(streamDefinitionList.size(), 2, "Expected stream definition count is not retrieved");

        } catch (SessionTimeoutException e) {
            Assert.fail("Exception when retrieving all the stream definitions defined in the server");
        }

        try {
            dataBridgeReceiverService.logout(sessionId);
        } catch (Exception e) {
            Assert.fail("Exception when logout the databridge receiver session.", e);
        }
    }


}

