/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.input.adapter.sqs.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterFactory;
import org.wso2.carbon.event.input.adapter.sqs.internal.SQSEventAdapterFactory;

/**
 * Class to handle registering SQS event adapter component as an input event adapter.
 * @scr.component name="input.sqsEventAdapterService.component" immediate="true"
 */

public class SQSEventAdapterServiceDS {
    private static final Log LOG = LogFactory.getLog(SQSEventAdapterServiceDS.class);

    /**
     * initialize the agent service here service here.
     *
     * @param context context of the SQS Event Adapter component
     */
    protected void activate(ComponentContext context) {
        try {
            InputEventAdapterFactory sqsEventEventAdapterFactory = new SQSEventAdapterFactory();
            context.getBundleContext().registerService(InputEventAdapterFactory.class.getName(),
                    sqsEventEventAdapterFactory, null);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Successfully deployed the input SQS adapter service.");
            }
        } catch (RuntimeException e) {
            LOG.error("Can not create the input SQS adapter service. ", e);
        }
    }
}
