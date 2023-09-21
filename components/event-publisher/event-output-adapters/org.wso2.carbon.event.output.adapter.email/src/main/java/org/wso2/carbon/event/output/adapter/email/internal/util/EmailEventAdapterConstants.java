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
package org.wso2.carbon.event.output.adapter.email.internal.util;

public class EmailEventAdapterConstants {

    private EmailEventAdapterConstants() {
    }

    public static final String ADAPTER_TYPE_EMAIL = "email";
    public static final String ADAPTER_MESSAGE_EMAIL_ADDRESS = "email.address";
    public static final String ADAPTER_MESSAGE_EMAIL_ADDRESS_HINT = "emailAddress.hint";
    public static final String ADAPTER_MESSAGE_EMAIL_SUBJECT = "email.subject";
    public static final String APAPTER_MESSAGE_EMAIL_TYPE = "email.type";
    public static final String ADAPTER_MESSAGE_EMAIL_TYPE_HINT = "emailType.hint";
    public static final String ADAPTER_EMAIL_SMTP_PORT = "mail.smtp.port";
    public static final String ADAPTER_EMAIL_SMTP_USER  = "mail.smtp.user";
    public static final String ADAPTER_EMAIL_SMTP_PASSWORD  = "mail.smtp.password";
    public static final String ADAPTER_EMAIL_SMTP_FROM  = "mail.smtp.from";
    public static final String ADAPTER_EMAIL_SMTP_HOST  = "mail.smtp.host";
    public static final String ADAPTER_EMAIL_SMTP_AUTH  = "mail.smtp.auth";
    public static final String ADAPTER_EMAIL_SMTP_STARTTLS_ENABLE  = "mail.smtp.starttls.enable";
    public static final String MIN_THREAD_NAME = "minThread";
    public static final String MAX_THREAD_NAME = "maxThread";
    public static final String ADAPTER_KEEP_ALIVE_TIME_NAME = "keepAliveTimeInMillis";
    public static final String EMAIL_SEPARATOR = ",";
    public static final int MIN_THREAD = 8;
    public static final int MAX_THREAD = 100;
    public static final long DEFAULT_KEEP_ALIVE_TIME_IN_MILLS = 20000;
    public static final String ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME = "jobQueueSize";
    public static final int ADAPTER_EXECUTOR_JOB_QUEUE_SIZE = 2000;


    /**
     * SMTP property constants.
     */

    public static final String MAIL_SMTP_HOST = "mail.smtp.host";
    public static final String MAIL_SMTP_PORT = "mail.smtp.port";

    /**
     * Email Adapter MIME types.
     */

    public static final String MAIL_TEXT_PLAIN = "text/plain";
    public static final String MAIL_TEXT_HTML = "text/html";

    /**
     * SMTP optional property constants.
     */
    public static final String MAIL_SMTP_REPLY_TO = "mail.smtp.replyTo";
    public static final String MAIL_SMTP_SIGNATURE = "mail.smtp.signature";

    /**
     * Define logging constants.
     */
    public static class LogConstants {

        private LogConstants() {
        }
        public static final String EMAIL_EVENT_ADAPTER_SERVICE = "email-event-adapter-service";

        /**
         * Define action IDs for diagnostic logs.
         */
        public static class ActionIDs {

            private ActionIDs() {
            }

            public static final String HANDOVER_EVENT = "handover-event";
        }

        /**
         * Define common and reusable Input keys for diagnostic logs.
         */
        public static class InputKeys {

            private InputKeys() {
            }
            public static final String EMAIL_TO = "email to";
            public static final String TENANT_DOMAIN = "tenant domain";
        }
    }
}
