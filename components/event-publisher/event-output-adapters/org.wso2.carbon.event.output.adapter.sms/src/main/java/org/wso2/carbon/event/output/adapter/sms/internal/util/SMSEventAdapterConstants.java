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
package org.wso2.carbon.event.output.adapter.sms.internal.util;


public final class SMSEventAdapterConstants {

    private SMSEventAdapterConstants() {
    }
    public static final String ADAPTER_TYPE_SMS = "sms";

    public static final String ADAPTER_MESSAGE_SMS_NO = "sms.no";
    public static final String ADAPTER_CONF_SMS_HINT_NO = "sms.no.default.hint";
    public static final String MIN_THREAD_NAME = "minThread";
    public static final String MAX_THREAD_NAME = "maxThread";
    public static final String DEFAULT_KEEP_ALIVE_TIME_NAME = "defaultKeepAliveTime";
    public static final int MIN_THREAD = 8;
    public static final int MAX_THREAD = 100;
    public static final long DEFAULT_KEEP_ALIVE_TIME = 20;
    public static final String SMS_SEPARATOR =  ",";
}
