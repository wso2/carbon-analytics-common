/*
 * Copyright (c) 2015, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.event.output.adapter.sms;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.core.EventAdapterUtil;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.output.adapter.sms.internal.ds.SMSEventAdapterServiceValueHolder;
import org.wso2.carbon.event.output.adapter.sms.internal.util.SMSEventAdapterConstants;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class SMSEventAdapter implements OutputEventAdapter {

    private static final Log log = LogFactory.getLog(SMSEventAdapter.class);
    private OutputEventAdapterConfiguration eventAdapterConfiguration;
    private static ThreadPoolExecutor threadPoolExecutor;
    private Map<String, String> globalProperties;
    private int tenantId;

    public SMSEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration, Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }

    @Override
    public void init() throws OutputEventAdapterException {

        tenantId= PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        //ThreadPoolExecutor will be assigned  if it is null
        if (threadPoolExecutor == null) {
            int minThread;
            int maxThread;
            long defaultKeepAliveTime;

            //If global properties are available those will be assigned else constant values will be assigned
            if (globalProperties.get(SMSEventAdapterConstants.MIN_THREAD_NAME) != null) {
                minThread = Integer.parseInt(globalProperties.get(SMSEventAdapterConstants.MIN_THREAD_NAME));
            } else {
                minThread = SMSEventAdapterConstants.MIN_THREAD;
            }

            if (globalProperties.get(SMSEventAdapterConstants.MAX_THREAD_NAME) != null) {
                maxThread = Integer.parseInt(globalProperties.get(SMSEventAdapterConstants.MAX_THREAD_NAME));
            } else {
                maxThread = SMSEventAdapterConstants.MAX_THREAD;
            }

            if (globalProperties.get(SMSEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_NAME) != null) {
                defaultKeepAliveTime = Integer.parseInt(globalProperties.get(
                        SMSEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_NAME));
            } else {
                defaultKeepAliveTime = SMSEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME;
            }

            threadPoolExecutor = new ThreadPoolExecutor(minThread, maxThread, defaultKeepAliveTime,
                    TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000),new ThreadFactoryBuilder().
                    setNameFormat("Thread pool- component - SMSEventAdapter.threadPoolExecutor;adapterName - " +
                            this.eventAdapterConfiguration.getName()).build());
        }
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("Test connection is not available");
    }

    @Override
    public void connect() {
        //not applicable.
    }

    @Override
    public void publish(Object message, Map<String, String> dynamicProperties) {
        String smsNos = dynamicProperties.get(SMSEventAdapterConstants.ADAPTER_MESSAGE_SMS_NO);

        if (smsNos != null) {
            String[] smsNoArray = smsNos.replaceAll(" ", "").split(SMSEventAdapterConstants.SMS_SEPARATOR);

            //Send sms for each sms no
            for (String smsNo : smsNoArray) {
                try {
                    threadPoolExecutor.submit(new SMSSender(smsNo, message.toString()));
                } catch (RejectedExecutionException e) {
                    EventAdapterUtil.logAndDrop(eventAdapterConfiguration.getName(), message, "Job queue is full", e, log, tenantId);
                }
            }
        }
    }

    @Override
    public void disconnect() {
        //Not applicable.
    }

    @Override
    public void destroy() {
        //Not required.
    }

    @Override
    public boolean isPolled() {
        return false;
    }

    class SMSSender implements Runnable {
        String smsNo;
        String message;

        SMSSender(String smsNo, String message) {
            this.smsNo = smsNo;
            this.message = message;
        }

        @Override
        public void run() {
            OMElement payload = OMAbstractFactory.getOMFactory().createOMElement(
                    BaseConstants.DEFAULT_TEXT_WRAPPER, null);
            payload.setText(message);
            ServiceClient serviceClient = null;
            try {
                ConfigurationContext configContext = SMSEventAdapterServiceValueHolder.getConfigurationContextService().getClientConfigContext();
                if (configContext != null) {
                    serviceClient = new ServiceClient(configContext, null);
                } else {
                    serviceClient = new ServiceClient();
                }
                Options options = new Options();
                options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
                options.setTo(new EndpointReference("sms://" + smsNo));
                serviceClient.setOptions(options);
                serviceClient.fireAndForget(payload);
                if (log.isDebugEnabled()) {
                    log.debug("Sending SMS to " + smsNo + " , message : " + message);
                }
            } catch (Exception ex) {
                EventAdapterUtil.logAndDrop(eventAdapterConfiguration.getName(), message, "Cannot send to '"
                        + smsNo + "'", ex, log, tenantId);
            } finally {
                if (serviceClient != null) {
                    try {
                        serviceClient.cleanup();
                    } catch (AxisFault axisFault) {
                        log.error("Error while cleaning-up service client resources at Output SMS Adapter '" + eventAdapterConfiguration.getName() + "'", axisFault);
                    }
                }
            }
        }
    }
}
