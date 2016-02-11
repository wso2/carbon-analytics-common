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
package org.wso2.carbon.throttle.event.core.internal.util;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.throttle.event.core.exception.ThrottleConfigurationException;
import org.wso2.carbon.throttle.event.core.internal.Policy;
import org.wso2.carbon.throttle.event.core.internal.ds.ThrottleServiceValueHolder;

public class ThrottlingPolicyDeployer extends AbstractDeployer{

    private static final Log log = LogFactory.getLog(ThrottlingPolicyDeployer.class);

    @Override
    public void init(ConfigurationContext configurationContext) {
        //todo tenant
    }

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        try {
            processDeploy(deploymentFileData);
        } catch (Throwable t) {
            log.error("Can't deploy the Throttling configuration: " + deploymentFileData.getName(), t);
            throw new DeploymentException("Can't deploy the Throttling configuration: " + deploymentFileData.getName(), t);
        }
    }

    public void undeploy(String fileName) throws DeploymentException {
        try {
            processUndeploy(fileName);
        } catch (Throwable t) {
            log.error("Can't undeploy the Throttling configuration: " + fileName, t);
            throw new DeploymentException("Can't undeploy the Throttling configuration: " + fileName, t);
        }
    }

    private void processUndeploy(String fileName) {
        //During deployment it is validated that fileName == policyName
        String[] splitResults = fileName.split("/");
        String name = splitResults[splitResults.length-1].split("\\.")[0];
        ThrottleServiceValueHolder.getThrottlerService().undeployLocalCEPRules(name);
        log.info("Successfully undeployed throttle policy named " + name);
    }

    private void processDeploy(DeploymentFileData deploymentFileData) throws ThrottleConfigurationException {
        Policy policy = ThrottleHelper.loadThrottlingPolicies(deploymentFileData);
        ThrottleServiceValueHolder.getThrottlerService().deployLocalCEPRules(policy);
        log.info("Successfully deployed throttle policy named " + policy.getName());
    }

    @Override
    public void setDirectory(String s) {

    }

    @Override
    public void setExtension(String s) {

    }


}
