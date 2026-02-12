/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.application.deployer;

import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import org.wso2.carbon.CarbonException;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.config.ApplicationConfiguration;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.persistence.CarbonAppPersistenceManager;
import org.wso2.carbon.event.application.deployer.utils.EventProcessingAppDeployerTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class EventProcessingAppDeployerTest {

    private static final String CAppName = "SampleCarbonApp";

    private CarbonApplication carbonApplication;
    private AxisConfiguration axisConfiguration;
    private ApplicationConfiguration applicationConfiguration;
    private EventProcessingDeployer eventProcessingDeployer;
    private EventProcessingAppDeployer eventProcessingAppDeployer;
    private MockedStatic<AppDeployerUtils> appDeployerUtilsMockedStatic;

    @Before
    public void initialize() throws CarbonException, IOException {
        String archPathToProcess = "src" + File.separator + "test" + File.separator + "resources" + File.separator +
                CAppName + ".car";
        EventProcessingAppDeployerTestUtils.unzipCApp(Paths.get(archPathToProcess));
        String extractedPath = "target" + File.separator + CAppName;

        Map map = new HashMap<>();
        map.put("event/receiver", true);
        map.put("event/publisher", true);
        map.put("event/execution-plan", true);
        map.put("event/stream", true);

        carbonApplication = new CarbonApplication();
        carbonApplication.setAppFilePath(archPathToProcess);
        carbonApplication.setExtractedPath(extractedPath);
        carbonApplication.setAppName(CAppName);
        carbonApplication.setAppVersion("1.0.0");
        axisConfiguration = new AxisConfiguration();

        CarbonAppPersistenceManager carbonAppPersistenceManager = mock(CarbonAppPersistenceManager.class);
        eventProcessingDeployer = mock(EventProcessingDeployer.class);

        appDeployerUtilsMockedStatic = Mockito.mockStatic(AppDeployerUtils.class, Mockito.CALLS_REAL_METHODS);
        appDeployerUtilsMockedStatic.when(() -> AppDeployerUtils.readServerRoles(any()))
                .thenReturn(new String[]{"DataAnalyticsServer"});
        appDeployerUtilsMockedStatic.when(() -> AppDeployerUtils.buildAcceptanceList(any()))
                .thenReturn(map);
        appDeployerUtilsMockedStatic.when(() -> AppDeployerUtils.getArtifactDeployer(any(AxisConfiguration.class),
                anyString(), anyString())).thenReturn(eventProcessingDeployer);

        applicationConfiguration = new ApplicationConfiguration(carbonAppPersistenceManager, extractedPath +
                File.separator + "artifacts.xml");
        carbonApplication.setAppConfig(applicationConfiguration);
        eventProcessingAppDeployer = new EventProcessingAppDeployer();
    }

    @After
    public void tearDown() {
        if (appDeployerUtilsMockedStatic != null) {
            appDeployerUtilsMockedStatic.close();
        }
    }

    @Test
    public void testDeployCApp() throws IOException {
        EventProcessingAppDeployerTestUtils.searchArtifacts(carbonApplication.getExtractedPath(),
                carbonApplication);
        eventProcessingAppDeployer.deployArtifacts(carbonApplication, axisConfiguration);
        Assert.assertTrue(EventProcessingAppDeployerTestUtils.checkCAppArtifactDeployment(carbonApplication,
                "Deployed"));
    }

    @Test
    public void testUnDeployCApp() throws IOException {
        EventProcessingAppDeployerTestUtils.searchArtifacts(carbonApplication.getExtractedPath(),
                carbonApplication);
        List<Artifact.Dependency> dependencyList = applicationConfiguration.getApplicationArtifact().getDependencies();
        for (Artifact.Dependency dependency : dependencyList) {
            dependency.getArtifact().setDeploymentStatus("Deployed");
        }
        eventProcessingAppDeployer.undeployArtifacts(carbonApplication, axisConfiguration);
        Assert.assertTrue(EventProcessingAppDeployerTestUtils.checkCAppArtifactDeployment(carbonApplication,
                "Pending"));
    }

    @Test(expected = DeploymentException.class)
    public void testExceptionInDeployingCApp() throws Exception {
        EventProcessingAppDeployerTestUtils.searchArtifacts(carbonApplication.getExtractedPath(),
                carbonApplication);
        doThrow(IOException.class).when(eventProcessingDeployer).processDeployment(any(DeploymentFileData.class));
        eventProcessingAppDeployer.deployArtifacts(carbonApplication, axisConfiguration);
    }

    @Test(expected = DeploymentException.class)
    public void testExceptionInUnDeployingCApp() throws Exception {
        EventProcessingAppDeployerTestUtils.searchArtifacts(carbonApplication.getExtractedPath(),
                carbonApplication);
        doThrow(IOException.class).when(eventProcessingDeployer).processUndeployment(anyString());
        List<Artifact.Dependency> dependencyList = applicationConfiguration.getApplicationArtifact().getDependencies();
        for (Artifact.Dependency dependency : dependencyList) {
            dependency.getArtifact().setDeploymentStatus("Deployed");
        }
        eventProcessingAppDeployer.undeployArtifacts(carbonApplication, axisConfiguration);
    }

    @Test
    public void testDeployCAppWithoutArtifact() throws DeploymentException {
        eventProcessingAppDeployer.deployArtifacts(carbonApplication, axisConfiguration);
        List<Artifact.Dependency> applicationDependencyList = carbonApplication.getAppConfig().getApplicationArtifact()
                .getDependencies();
        for (Artifact.Dependency dependency : applicationDependencyList) {
            Assert.assertEquals(null, dependency.getArtifact());
        }
    }
}
