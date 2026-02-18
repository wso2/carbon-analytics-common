/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.jaggeryapp.template.deployer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.wso2.carbon.event.template.manager.core.DeployableTemplate;
import org.wso2.carbon.event.template.manager.core.TemplateDeploymentException;
import org.wso2.carbon.event.template.manager.core.structure.configuration.ScenarioConfiguration;
import org.wso2.carbon.jaggeryapp.template.deployer.internal.JaggeryappTemplateDeployerConstants;
import org.wso2.carbon.jaggeryapp.template.deployer.internal.util.JaggeryappTemplateDeployerUtility;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import org.wso2.carbon.jaggeryapp.template.deployer.internal.JaggeryappTemplateDeployerException;

@RunWith(MockitoJUnitRunner.class)
public class JaggeryTemplateDeployerTest {

    private static final String ARTIFACT_ID = "TwitterAnalytics";
    private static final Log log = LogFactory.getLog(JaggeryappTemplateDeployer.class);

    private Registry registry;
    private ResourceImpl resource;
    private DeployableTemplate deployableTemplate;
    private JaggeryappTemplateDeployer jaggeryappTemplateDeployer;
    private ScenarioConfiguration scenarioConfiguration;
    private final Map<String, Object> mockRegistry = new HashMap<>();
    private Map<String, String> artifactContents = new HashMap<>();
    
    private MockedStatic<JaggeryappTemplateDeployerUtility> mockedUtility;
    private MockedStatic<CarbonUtils> mockedCarbonUtils;

    @Before
    public void initialize() throws RegistryException, IOException {
        mockedUtility = mockStatic(JaggeryappTemplateDeployerUtility.class);
        mockedCarbonUtils = mockStatic(CarbonUtils.class);
        registry = mock(Registry.class);

        artifactContents.put("application-data.json", "{config :{file: 'application-data.json'}}");
        artifactContents.put("config.json", "{config: {file: 'config.json'}}");
        artifactContents.put("htag.xml", "<twitter-analytic-app><file>htag.xml</file></twitter-analytic-app>");

        resource = new ResourceImpl();
        jaggeryappTemplateDeployer = new JaggeryappTemplateDeployer();

        // Build the scenario
        scenarioConfiguration = new ScenarioConfiguration();
        scenarioConfiguration.setName(ARTIFACT_ID);
        scenarioConfiguration.setDescription("This is a sample SensorDataAnalysis");
        scenarioConfiguration.setDomain("SensorDataAnalysis");
        scenarioConfiguration.setScenario("SensorAnalytics");

        // Fetch artifact content
        String artifact = new String(Files.readAllBytes(Paths.get("src", File.separator, "test", File.separator,
                "resources", File.separator, "JaggeryAppTemplate.xml")));

        // Build template
        deployableTemplate = new DeployableTemplate();
        deployableTemplate.setArtifactId(ARTIFACT_ID);
        deployableTemplate.setConfiguration(scenarioConfiguration);
        deployableTemplate.setArtifact(artifact);

        String fsRoot = getFSRootDir();
        String[] dirs = new String[]{
                fsRoot + "/jaggeryapps/" + ARTIFACT_ID,
                fsRoot + "/conf/template-manager/jaggeryapp-templates/" + ARTIFACT_ID
        };

        when(registry.newResource()).thenReturn(new ResourceImpl());
        mockedCarbonUtils.when(() -> CarbonUtils.getCarbonConfigDirPath()).thenReturn(fsRoot + "/conf");
        mockedUtility.when(() -> JaggeryappTemplateDeployerUtility.getJaggeryappArtifactPath()).thenReturn(fsRoot + "/jaggeryapps/");
        mockedUtility.when(() -> JaggeryappTemplateDeployerUtility.getRegistry()).thenReturn(registry);

        for (String dir : dirs) {
            File d = new File(dir);
            if (!d.exists()) {
                d.mkdirs();
            }
        }

        mockRegistry.clear();
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            String key = (String) args[0];
            Object resource = args[1];
            mockRegistry.put(key, resource);
            return null;
        }).when(registry).put(anyString(), any());
    }
    
    @After
    public void cleanup() {
        if (mockedUtility != null) {
            mockedUtility.close();
        }
        if (mockedCarbonUtils != null) {
            mockedCarbonUtils.close();
        }
    }

    @Test
    public void testDeploy() throws TemplateDeploymentException, RegistryException, IOException {
        when(registry.resourceExists(anyString())).thenReturn(false);
        jaggeryappTemplateDeployer.deployIfNotDoneAlready(deployableTemplate);
        Assert.assertTrue(mockRegistry.containsKey(JaggeryappTemplateDeployerConstants.META_INFO_COLLECTION_PATH));
        Assert.assertTrue(validateFilesystem());
    }

    @Test(expected = JaggeryappTemplateDeployerException.class)
    public void testDeployInvalidTemplate() throws TemplateDeploymentException, RegistryException, IOException {
        String invalidArtifact = new String(Files.readAllBytes(Paths.get("src", File.separator, "test", File.separator,
                "resources", File.separator, "InvalidJaggeryAppTemplate.xml")));
        deployableTemplate.setArtifact(invalidArtifact);
        jaggeryappTemplateDeployer.deployArtifact(deployableTemplate);
    }

    @Test(expected = JaggeryappTemplateDeployerException.class)
    public void testDeployWhenDestinationDirExists() throws Exception {
        when(registry.resourceExists(anyString())).thenReturn(true);
        when(registry.get(anyString())).thenReturn(resource);
        resetEnvironment();
        addRegistryResource(null);
        buildJaggeryapp();
        jaggeryappTemplateDeployer.deployArtifact(deployableTemplate);
    }

    @Test
    public void testDeployWhenResourceExists() throws Exception {
        when(registry.resourceExists(anyString())).thenReturn(true);
        when(registry.get(anyString())).thenReturn(resource);
        addRegistryResource();
        jaggeryappTemplateDeployer.deployIfNotDoneAlready(deployableTemplate);
        Assert.assertTrue(mockRegistry.containsKey(JaggeryappTemplateDeployerConstants.META_INFO_COLLECTION_PATH));
    }

    @Test
    public void testDeployWithNoArtifactResource() throws Exception {
        when(registry.resourceExists(anyString())).thenReturn(true);
        when(registry.get(anyString())).thenReturn(resource);
        resetEnvironment();
        addRegistryResource(null);
        jaggeryappTemplateDeployer.deployIfNotDoneAlready(deployableTemplate);
        Assert.assertTrue(mockRegistry.containsKey(JaggeryappTemplateDeployerConstants.META_INFO_COLLECTION_PATH));
        Assert.assertTrue(validateFilesystem());
    }

    @Test
    public void testFreshDeployWhenResourceExists() throws Exception {
        when(registry.resourceExists(anyString())).thenReturn(true);
        when(registry.get(anyString())).thenReturn(resource);
        resetEnvironment();
        addRegistryResource();
        jaggeryappTemplateDeployer.deployArtifact(deployableTemplate);
        Assert.assertTrue(mockRegistry.containsKey(JaggeryappTemplateDeployerConstants.META_INFO_COLLECTION_PATH));
        Assert.assertTrue(validateFilesystem());
    }

    @Test
    public void testUndeploy() throws Exception {
        when(registry.resourceExists(anyString())).thenReturn(true);
        when(registry.get(anyString())).thenReturn(resource);
        addRegistryResource();
        buildJaggeryapp();
        jaggeryappTemplateDeployer.undeployArtifact(ARTIFACT_ID);
        Assert.assertFalse(((ResourceImpl) mockRegistry
                .get(JaggeryappTemplateDeployerConstants.META_INFO_COLLECTION_PATH))
                .getProperties().containsKey(ARTIFACT_ID));
        Assert.assertFalse(new File(getFSRootDir() + "/jaggeryapps/" + ARTIFACT_ID).exists());
    }

    @Test
    public void testUndeployWhenNoRegistryResource() throws Exception {
        when(registry.resourceExists(anyString())).thenReturn(false);
        buildJaggeryapp();
        jaggeryappTemplateDeployer.undeployArtifact(ARTIFACT_ID);
        Assert.assertFalse(mockRegistry.containsKey(JaggeryappTemplateDeployerConstants.META_INFO_COLLECTION_PATH));
    }

    @Test
    public void testUndeployWithNoArtifactResource() throws Exception {
        when(registry.resourceExists(anyString())).thenReturn(true);
        when(registry.get(anyString())).thenReturn(resource);
        resetEnvironment();
        addRegistryResource(null);
        jaggeryappTemplateDeployer.undeployArtifact(ARTIFACT_ID);
    }

    @Test
    public void testGetJaggeryAppType() {
        JaggeryappTemplateDeployer deployer = new JaggeryappTemplateDeployer();
        Assert.assertEquals("jaggeryapp", deployer.getType());
    }

    private String getFSRootDir() {
        return System.getProperty("java.io.tmpdir") + "/jaggeryappDeployerTest";
    }

    private boolean validateFilesystem() throws IOException {
        String dir = getFSRootDir() + "/jaggeryapps/" + ARTIFACT_ID + "/";
        for (Map.Entry<String, String> entry : artifactContents.entrySet()) {
            File file = new File(dir + entry.getKey());
            if (!file.exists()) {
                return false;
            }

            byte[] bytes = Files.readAllBytes(Paths.get(dir, entry.getKey()));
            String content = new String(bytes, Charset.defaultCharset());
            if (!content.equals(entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private void addRegistryResource() {
        addRegistryResource(ARTIFACT_ID);
    }

    private void addRegistryResource(String artifactId) {
        resource.setProperty(ARTIFACT_ID, artifactId);
        mockRegistry.put(JaggeryappTemplateDeployerConstants.META_INFO_COLLECTION_PATH, resource);
    }

    private void buildJaggeryapp() {
        String dir = getFSRootDir() + "/jaggeryapps/" + ARTIFACT_ID;
        new File(dir).mkdirs();

        for (Map.Entry<String, String> entry : artifactContents.entrySet()) {
            File targetFile = new File(dir, entry.getKey());
            FileWriter writer = null;
            try {
                writer = new FileWriter(targetFile);
                writer.write(entry.getValue());
            } catch (IOException e) {
                 log.error("Failed to write artifact to: " + targetFile.getAbsolutePath());
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                         log.warn("Failed to close FileWriter of " + targetFile.getAbsolutePath());
                    }
                }
            }
        }
    }

    private void resetEnvironment() {
        mockRegistry.clear();
        File dir = new File(getFSRootDir() + "/jaggeryapps/" + ARTIFACT_ID);
        if (dir.exists()) {
            String[] entries = dir.list();
            for (String filename : entries) {
                File currentFile = new File(dir.getPath(), filename);
                currentFile.delete();
            }
            dir.delete();
        }
    }
}
