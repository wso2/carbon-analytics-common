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
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.SessionTimeoutException;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;

import javax.inject.Inject;


/**
 * OSGI Testcase for Databridge core.
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class DatabridgeCoreTestcase {

    private static final String DATABRIDGE_CORE_BUNDLE_NAME = "org.wso2.carbon.databridge.core";

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
    public void testStoreApiBundle() {
        Bundle coreBundle = getBundle(DATABRIDGE_CORE_BUNDLE_NAME);
        Assert.assertEquals(coreBundle.getState(), Bundle.ACTIVE);
    }

    @Test
    public void testDefineStreamForDataReceiver() throws InterruptedException {

        String sessionId = "";
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
        try {
            sessionId = dataBridgeReceiverService.login("admin", "admin");
        } catch (AuthenticationException e) {
            Assert.fail("Authentication error when login to databridge. " + e);
        }

        try {
            dataBridgeReceiverService.defineStream(sessionId, streamDefinition, null);
        } catch (DifferentStreamDefinitionAlreadyDefinedException | MalformedStreamDefinitionException |
                SessionTimeoutException e) {
            Assert.fail("Exception when defining stream definition. ", e);
        }

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
}

