/*
*  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.

  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/
package org.wso2.carbon.analytics.common.jmx.agent.tasks.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.analytics.common.jmx.agent.JmxConstant;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;

@Component(
        name = "jmxservices.task",
        immediate = true)
public class JmxTaskServiceComponent {

    private static final Log log = LogFactory.getLog(JmxTaskServiceComponent.class);

    private static TaskService taskService;

    private static RegistryService registryService;

    private static TenantRegistryLoader tenantRegistryLoader;

    private static EventStreamService eventStreamService;

    @Activate
    protected void activate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.debug("Activating the tasks");
        }
        try {
            getTaskService().registerTaskType(JmxConstant.JMX_SERVICE_TASK_TYPE);
        } catch (TaskException e) {
            log.error("JmxTaskServiceComponent activation error.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.debug("Jmx Services task bundle is deactivated ");
        }
    }

    @Reference(
            name = "ntask.component",
            service = org.wso2.carbon.ntask.core.service.TaskService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTaskService")
    protected void setTaskService(TaskService taskService) {

        if (log.isDebugEnabled()) {
            log.debug("Setting the Task Service");
        }
        JmxTaskServiceComponent.taskService = taskService;
    }

    protected void unsetTaskService(TaskService taskService) {

        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Task Service");
        }
        JmxTaskServiceComponent.taskService = null;
    }

    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {

        if (log.isDebugEnabled()) {
            log.debug("RegistryService acquired");
        }
        JmxTaskServiceComponent.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {

        JmxTaskServiceComponent.registryService = null;
    }

    @Reference(
            name = "registry.loader.default",
            service = org.wso2.carbon.registry.core.service.TenantRegistryLoader.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryLoader")
    protected void setRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {

        if (log.isDebugEnabled()) {
            log.debug("Tenant registry loader acquired");
        }
        JmxTaskServiceComponent.tenantRegistryLoader = tenantRegistryLoader;
    }

    protected void unsetRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {

        JmxTaskServiceComponent.tenantRegistryLoader = null;
    }

    @Reference(
            name = "org.wso2.carbon.event.stream.core.EventStreamService",
            service = org.wso2.carbon.event.stream.core.EventStreamService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventStreamService")
    protected void setEventStreamService(EventStreamService eventStreamService) {

        JmxTaskServiceComponent.eventStreamService = eventStreamService;
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {

        JmxTaskServiceComponent.eventStreamService = null;
    }

    public static TaskService getTaskService() {

        return taskService;
    }

    public static RegistryService getRegistryService() {

        return registryService;
    }

    public static TenantRegistryLoader getTenantRegistryLoader() {

        return tenantRegistryLoader;
    }

    public static EventStreamService getEventStreamService() {

        return eventStreamService;
    }
}
