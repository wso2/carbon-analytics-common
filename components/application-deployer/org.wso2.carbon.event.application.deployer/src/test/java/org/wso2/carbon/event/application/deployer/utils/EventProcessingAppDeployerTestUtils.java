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

package org.wso2.carbon.event.application.deployer.utils;

import org.apache.log4j.Logger;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.internal.ApplicationManager;
import org.wso2.carbon.event.application.deployer.EventProcessingAppDeployerTest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This class provides several utility functions that are used in the Tests.
 */
public class EventProcessingAppDeployerTestUtils {

    private static final Logger log = Logger.getLogger(EventProcessingAppDeployerTest.class);

    /**
     * Search through the given cApp directory and get the artifacts into carbon application instance.
     *
     * @param rootDirPath CarbonApp location
     * @param parentApp   instance of the CarbonApp
     * @throws IOException
     */
    public static void searchArtifacts(String rootDirPath, CarbonApplication parentApp) throws IOException {
        File extractedDir = new File(rootDirPath);
        File[] allFiles = extractedDir.listFiles();
        if (allFiles != null) {
            List<Artifact> allArtifacts = new ArrayList();
            int length = allFiles.length;

            for (int i = 0; i < length; ++i) {
                File artifactDirectory = allFiles[i];
                if (artifactDirectory.isDirectory()) {
                    String directoryPath = AppDeployerUtils.formatPath(artifactDirectory.getAbsolutePath());
                    String artifactXmlPath = directoryPath + File.separator + "artifact.xml";
                    File artifactFile = new File(artifactXmlPath);
                    if (artifactFile.exists()) {
                        Artifact artifact = null;
                        FileInputStream xmlInputStream = null;
                        try {
                            xmlInputStream = new FileInputStream(artifactFile);
                            artifact = ApplicationManager.getInstance().buildAppArtifact(parentApp, xmlInputStream);
                        } catch (FileNotFoundException e) {
                            log.error("artifacts.xml File cannot be loaded from " + artifactXmlPath, e);
                        } catch (CarbonException e) {
                            log.error("Error in building App artifact", e);
                        } finally {
                            if (xmlInputStream != null) {
                                try {
                                    xmlInputStream.close();
                                } catch (IOException e) {
                                    log.error("Error while closing input stream.", e);
                                }
                            }
                        }
                        if (artifact == null) {
                            return;
                        }
                        artifact.setExtractedPath(directoryPath);
                        allArtifacts.add(artifact);
                    }
                }
            }
            Artifact appArtifact = parentApp.getAppConfig().getApplicationArtifact();
            ApplicationManager.getInstance().buildDependencyTree(appArtifact, allArtifacts);
        }
    }

    /**
     * This method will unzip the given CarbonApp to the target directory.
     *
     * @param cAppDir CarbonApp location
     * @throws IOException
     */
    public static void unzipCApp(Path cAppDir) throws IOException {
        ZipFile file = new ZipFile(cAppDir.toString());
        Enumeration<? extends ZipEntry> entries = file.entries();
        String targetDirectory = "target" + File.separator + "SampleCarbonApp";
        Files.createDirectories(Paths.get(targetDirectory));

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
                Files.createDirectories(Paths.get(targetDirectory + File.separator + entry.getName()));
            } else {
                InputStream inputStream = file.getInputStream(entry);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                String uncompressedFileName = targetDirectory + File.separator + entry.getName();
                FileOutputStream fileOutput = new FileOutputStream(uncompressedFileName);
                while (bufferedInputStream.available() > 0) {
                    fileOutput.write(bufferedInputStream.read());
                }
                fileOutput.close();
            }
        }
    }

    /**
     * Check the deployment of the artifacts in the given CarbonApp.
     *
     * @param carbonApplication CarbonApp instance
     * @param status            deployment status - Deployed, Failed, Pending
     * @return
     */
    public static boolean checkCAppArtifactDeployment(CarbonApplication carbonApplication, String status) {
        List<Artifact.Dependency> dependencyList = carbonApplication.getAppConfig().getApplicationArtifact()
                .getDependencies();
        for (Artifact.Dependency dependency : dependencyList) {
            if (!dependency.getArtifact().getDeploymentStatus().equals(status)) {
                return false;
            }
        }
        return true;
    }
}
