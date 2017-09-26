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

package org.wso2.carbon.dashboard.template.deployer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.wso2.carbon.dashboard.template.deployer.internal.DashboardTemplateDeployerConstants;
import org.wso2.carbon.dashboard.template.deployer.internal.util.DashboardTemplateDeployerUtility;
import org.wso2.carbon.event.template.manager.core.DeployableTemplate;
import org.wso2.carbon.event.template.manager.core.structure.configuration.ScenarioConfiguration;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DashboardTemplateDeployerUtility.class)
public class DashboardTemplateDeployerTest {

    public static final String ARTIFACT_ID = "analytics-temperature-dashboard";

    private Registry registry;
    private Resource resource;
    private DeployableTemplate deployableTemplate;
    private DashboardTemplateDeployer dashboardTemplateDeployer;
    private ScenarioConfiguration scenarioConfiguration;
    private final HashMap hashMap = new HashMap();

    @Before
    public void initialize() throws RegistryException, IOException {
        mockStatic(DashboardTemplateDeployerUtility.class);
        registry = PowerMockito.mock(Registry.class);
        PowerMockito.mock(Resource.class);

        resource = new ResourceImpl();
        deployableTemplate = new DeployableTemplate();
        dashboardTemplateDeployer = new DashboardTemplateDeployer();
        scenarioConfiguration = new ScenarioConfiguration();

        String artifact = new String(Files.readAllBytes(Paths.get("src", File.separator, "test", File.separator,
                "resources", File.separator, "dashboardTemplate.xml")));

        deployableTemplate.setArtifactId(ARTIFACT_ID);
        scenarioConfiguration.setName(ARTIFACT_ID);
        scenarioConfiguration.setDescription("This is a sample SensorDataAnalysis");
        scenarioConfiguration.setDomain("SensorDataAnalysis");
        scenarioConfiguration.setScenario("SensorAnalytics");
        deployableTemplate.setConfiguration(scenarioConfiguration);
        deployableTemplate.setArtifact(artifact);

        when(DashboardTemplateDeployerUtility.getRegistry()).thenReturn(registry);
        when(registry.newResource()).thenReturn(new ResourceImpl());

        hashMap.clear();
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                String key = (String) args[0];
                Resource resource = (Resource) args[1];
                hashMap.put(key, resource);
                return null;
            }
        }).when(registry).put(anyString(), (Resource) anyObject());
    }

    @Test
    public void testDeployNewDashboardResource() throws Exception {
        dashboardTemplateDeployer.deployIfNotDoneAlready(deployableTemplate);
        Assert.assertTrue(hashMap.containsKey(DashboardTemplateDeployerConstants.ARTIFACT_DASHBOARD_ID_MAPPING_PATH));
        Assert.assertTrue(hashMap.containsKey(DashboardTemplateDeployerConstants.DASHBOARDS_RESOURCE_PATH +
                deployableTemplate.getArtifactId()));

    }

    @Test
    public void testDeployExistingDashboardResource() throws Exception {
        doReturn(true).when(registry, "resourceExists", anyString());
        resource.setProperty(deployableTemplate.getArtifactId(), ARTIFACT_ID);
        doReturn(resource).when(registry, "get", anyString());
        dashboardTemplateDeployer.deployArtifact(deployableTemplate);
        Assert.assertTrue(hashMap.containsKey(DashboardTemplateDeployerConstants.ARTIFACT_DASHBOARD_ID_MAPPING_PATH));
        Assert.assertTrue(hashMap.containsKey(DashboardTemplateDeployerConstants.DASHBOARDS_RESOURCE_PATH +
                deployableTemplate.getArtifactId()));
    }

    @Test
    public void testDeploySharedDashboardResource() throws Exception {
        doReturn(true).when(registry, "resourceExists", anyString());

        Properties properties = new Properties();
        List<String> list = new ArrayList();
        list.add(ARTIFACT_ID);
        properties.put("dashboardid", list);
        resource.setProperties(properties);
        resource.setProperty(deployableTemplate.getArtifactId(), ARTIFACT_ID);

        doReturn(resource).when(registry, "get", anyString());
        dashboardTemplateDeployer.deployArtifact(deployableTemplate);
        Assert.assertTrue(hashMap.containsKey(DashboardTemplateDeployerConstants.ARTIFACT_DASHBOARD_ID_MAPPING_PATH));
        Assert.assertTrue(hashMap.containsKey(DashboardTemplateDeployerConstants.DASHBOARDS_RESOURCE_PATH +
                deployableTemplate.getArtifactId()));

    }

    @Test
    public void testGetArtifactType() {
        DashboardTemplateDeployer dashboardTemplateDeployer = new DashboardTemplateDeployer();
        dashboardTemplateDeployer.getType();
        Assert.assertEquals("dashboard", dashboardTemplateDeployer.getType());
    }
}