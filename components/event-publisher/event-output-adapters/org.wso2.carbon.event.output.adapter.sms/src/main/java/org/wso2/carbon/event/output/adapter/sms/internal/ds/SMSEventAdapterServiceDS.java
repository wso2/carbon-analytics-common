/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.output.adapter.sms.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterFactory;
import org.wso2.carbon.event.output.adapter.sms.SMSEventAdapterFactory;
import org.wso2.carbon.utils.ConfigurationContextService;

@Component(
        name = "output.sms.EventAdapterService.component",
        immediate = true)
public class SMSEventAdapterServiceDS {

    private static final Log log = LogFactory.getLog(SMSEventAdapterServiceDS.class);

    /**
     * initialize the sms service here service here.
     *
     * @param context
     */
    @Activate
    protected void activate(ComponentContext context) {

        try {
            OutputEventAdapterFactory smsEventAdapterFactory = new SMSEventAdapterFactory();
            context.getBundleContext().registerService(OutputEventAdapterFactory.class.getName(),
                    smsEventAdapterFactory, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed the output SMS event adapter service");
            }
        } catch (RuntimeException e) {
            log.error("Can not create the output SMS event adapter service ", e);
        }
    }

    @Reference(
            name = "configurationcontext.service",
            service = org.wso2.carbon.utils.ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {

        SMSEventAdapterServiceValueHolder.registerConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {

    }
}
