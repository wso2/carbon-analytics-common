/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.template.manager.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.event.template.manager.core.TemplateDeployer;

@Component(
        name = "template.deployer.service.tracker.component",
        immediate = true)
public class TemplateDeployerServiceTrackerDS {

    private static final Log log = LogFactory.getLog(TemplateDeployerServiceTrackerDS.class);

    /**
     * initialize the Template deployer core service here.
     *
     * @param context bundle context
     */
    @Activate
    protected void activate(ComponentContext context) {

        try {
            log.info("Successfully deployed the execution manager tracker service");
        } catch (RuntimeException e) {
            log.error("Can not create the execution manager tracker service ", e);
        }
    }

    @Reference(
            name = "template.deployer.tracker.service",
            service = org.wso2.carbon.event.template.manager.core.TemplateDeployer.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetTemplateDeployer")
    protected void setTemplateDeployer(TemplateDeployer templateDeployer) {

        try {
            TemplateManagerValueHolder.getTemplateDeployers().put(templateDeployer.getType(), templateDeployer);
        } catch (Throwable t) {
            log.error(t);
        }
    }

    protected void unSetTemplateDeployer(TemplateDeployer templateDeployer) {

        TemplateManagerValueHolder.getTemplateDeployers().remove(templateDeployer.getType());
    }
}
