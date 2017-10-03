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

package org.wso2.carbon.gadget.template.deployer;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.api.mockito.PowerMockito;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import org.wso2.carbon.event.template.manager.core.DeployableTemplate;
import org.wso2.carbon.gadget.template.deployer.internal.GadgetTemplateDeployerConstants;
import org.wso2.carbon.gadget.template.deployer.internal.util.GadgetTemplateDeployerUtility;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;


@RunWith(PowerMockRunner.class)
@PrepareForTest({GadgetTemplateDeployerUtility.class, CarbonUtils.class})
public class GadgetTemplateDeployerTest {

    public static final String ARTIFACT_ID = "SensorDataAnalysis-SensorAnalytics-SensorAnalytics-gadget1";

    private GadgetTemplateDeployer gadgetTemplateDeployer;
    private DeployableTemplate deployableTemplate;
    private Registry registry;
    private Resource resource;
    private final HashMap registryHashMap = new HashMap();

    @Before
    public void initialize() throws IOException, RegistryException {
        gadgetTemplateDeployer = new GadgetTemplateDeployer();

        deployableTemplate = new DeployableTemplate();
        String artifact = new String(Files.readAllBytes(Paths.get(getTestResourceLocation(), File.separator,
                "SensorAnalytics-gadget1.xml")));
        deployableTemplate.setArtifactId(ARTIFACT_ID);
        deployableTemplate.setArtifact(artifact);

        registry = PowerMockito.mock(Registry.class);
        resource = new ResourceImpl();
        mockStatic(CarbonUtils.class);
        mockStatic(GadgetTemplateDeployerUtility.class);

        when(CarbonUtils.getCarbonConfigDirPath()).thenReturn(getTestResourceLocation());
        when(GadgetTemplateDeployerUtility.getGadgetArtifactPath()).thenReturn(getTestGadgetArtifactPath());
        when(GadgetTemplateDeployerUtility.getRegistry()).thenReturn(registry);
        when(registry.newResource()).thenReturn(new ResourceImpl());

        registryHashMap.clear();

        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                String key = (String) args[0];
                Resource resource = (Resource) args[1];
                registryHashMap.put(key, resource);
                return null;
            }
        }).when(registry).put(anyString(), (Resource) anyObject());

    }

    @Test
    public void testGetArtifactType() {
        gadgetTemplateDeployer.getType();
        Assert.assertEquals("gadget", gadgetTemplateDeployer.getType());
    }

    @Test
    public void testDeployNewGadgetResource() throws Exception {
        when(registry.resourceExists(GadgetTemplateDeployerConstants.ARTIFACT_DIRECTORY_MAPPING_PATH)).thenReturn(false);
        createGadgetArtifactDir();
        gadgetTemplateDeployer.deployArtifact(deployableTemplate);
        Assert.assertTrue("Failure at copying generic gadget files", validateDeployedGadget(getTestResourceLocation() +
                        File.separator + GadgetTemplateDeployerConstants.TEMPLATE_MANAGER +
                        File.separator + GadgetTemplateDeployerConstants.GADGET_TEMPLATES +
                        File.separator + "numberchart" + File.separator,
                getTestGadgetArtifactPath() + File.separator + "temperature-count-chart" + File.separator));
        Assert.assertTrue("Failure at copying templated files", validateDeployedGadget(getTestResourceLocation() +
                        File.separator + "generatedGadgetFiles",
                getTestGadgetArtifactPath() + File.separator + "temperature-count-chart" + File.separator));
        Assert.assertTrue(registryHashMap.containsKey(GadgetTemplateDeployerConstants.ARTIFACT_DIRECTORY_MAPPING_PATH));
        resetEnvironment();

    }

    private String getTestResourceLocation() {
        return "src" + File.separator + "test" + File.separator + "resources";
    }

    private String getPackLocation() {
        return System.getProperty("java.io.tmpdir") + File.separator + "gadgetDeployerTest";
    }

    private String getTestGadgetArtifactPath() {
        return getPackLocation() + File.separator + "jaggeryapps" + File.separator + GadgetTemplateDeployerConstants.APP_NAME +
                File.separator + "store" + File.separator + "carbon.super" + File.separator +
                GadgetTemplateDeployerConstants.DEFAULT_STORE_TYPE + File.separator + GadgetTemplateDeployerConstants.ARTIFACT_TYPE +
                File.separator;
    }

    private void createGadgetArtifactDir() {
        String dir = getTestGadgetArtifactPath();
        new File(dir).mkdirs();
    }

    private boolean validateDeployedGadget(String sourcePath, String destinationPath) throws IOException {
        ArrayList<File> sourceFiles = listFilesInDir(sourcePath,new ArrayList<File>());
        ArrayList<File> destinationFiles = listFilesInDir(destinationPath,new ArrayList<File>());

        for (File sourceFile : sourceFiles) {
            for (File destinationFile : destinationFiles) {
                if (sourceFile.getName().equals(destinationFile.getName())) {
                    if (!FileUtils.contentEquals(sourceFile, destinationFile)) {
                        return false;
                    } else {
                        break;
                    }
                }
            }
        }
        return true;
    }

    public ArrayList<File> listFilesInDir(String path, ArrayList<File> files) {
        File directory = new File(path);
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                listFilesInDir(file.getAbsolutePath(), files);
            }
        }
        return files;
    }

    public void resetEnvironment () throws IOException {
        registryHashMap.clear();
        FileUtils.deleteDirectory(new File(getPackLocation()));
    }


}
