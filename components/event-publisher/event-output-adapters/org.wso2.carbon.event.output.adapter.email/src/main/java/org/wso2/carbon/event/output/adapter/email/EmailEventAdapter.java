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
package org.wso2.carbon.event.output.adapter.email;

import org.apache.axis2.transport.mail.MailConstants;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.core.EventAdapterUtil;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.ConnectionUnavailableException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.output.adapter.email.internal.util.EmailEventAdapterConstants;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.utils.DiagnosticLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


/**
 * The Email event adapter sends mail using an SMTP server configuration defined
 * in output-event-adapters.xml email adapter sender definition.
 */

public class EmailEventAdapter implements OutputEventAdapter {

    private static final Log log = LogFactory.getLog(EmailEventAdapter.class);
    private static ThreadPoolExecutor threadPoolExecutor;
    private Session session;
    private OutputEventAdapterConfiguration eventAdapterConfiguration;
    private Map<String, String> globalProperties;
    private int tenantId;
    private String tenantDomain;
    private String loggableEmail;

    /**
     * Default from address for outgoing messages.
     */
    private InternetAddress smtpFromAddress = null;

    /**
     * Optional replyTO address for outgoing messages.
     */
    private InternetAddress[] smtpReplyToAddress = null;

    /**
     * Optional Signature of sender address for outgoing messages.
     */
    private String signature = null;


    public EmailEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration,
                             Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }

    /**
     * Initialize the thread pool to send emails.
     *
     * @throws OutputEventAdapterException on error.
     */

    @Override
    public void init() throws OutputEventAdapterException {

        tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);

        //ThreadPoolExecutor will be assigned  if it is null.
        if (threadPoolExecutor == null) {
            int minThread;
            int maxThread;
            long defaultKeepAliveTime;
            int jobQueSize;


            //If global properties are available those will be assigned else constant values will be assigned
            if (globalProperties.get(EmailEventAdapterConstants.MIN_THREAD_NAME) != null) {
                minThread = Integer.parseInt(globalProperties.get(EmailEventAdapterConstants.MIN_THREAD_NAME));
            } else {
                minThread = EmailEventAdapterConstants.MIN_THREAD;
            }

            if (globalProperties.get(EmailEventAdapterConstants.MAX_THREAD_NAME) != null) {
                maxThread = Integer.parseInt(globalProperties.get(EmailEventAdapterConstants.MAX_THREAD_NAME));
            } else {
                maxThread = EmailEventAdapterConstants.MAX_THREAD;
            }

            if (globalProperties.get(EmailEventAdapterConstants.ADAPTER_KEEP_ALIVE_TIME_NAME) != null) {
                defaultKeepAliveTime = Integer.parseInt(globalProperties.get(
                        EmailEventAdapterConstants.ADAPTER_KEEP_ALIVE_TIME_NAME));
            } else {
                defaultKeepAliveTime = EmailEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_IN_MILLS;
            }

            if (globalProperties.get(EmailEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME) != null) {
                jobQueSize = Integer.parseInt(globalProperties.get(
                        EmailEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME));
            } else {
                jobQueSize = EmailEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE;
            }

            threadPoolExecutor = new ThreadPoolExecutor(minThread, maxThread, defaultKeepAliveTime,
                    TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(jobQueSize));
        }

    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("Test connection is not available");
    }

    /**
     * Initialize the Email SMTP session and be ready to send emails.
     *
     * @throws ConnectionUnavailableException on error.
     */


    @Override
    public void connect() throws ConnectionUnavailableException {

        if (session == null) {

            /**
             * Default SMTP properties for outgoing messages.
             */
            String smtpFrom;
            String smtpHost;
            String smtpPort;


            /**
             *  Default from username and password for outgoing messages.
             */
            final String smtpUsername;
            final String smtpPassword;


            // initialize SMTP session.
            Properties props = new Properties();
            props.putAll(globalProperties);

            //Verifying default SMTP properties of the SMTP server.

            smtpFrom = props.getProperty(MailConstants.MAIL_SMTP_FROM);
            smtpHost = props.getProperty(EmailEventAdapterConstants.MAIL_SMTP_HOST);
            smtpPort = props.getProperty(EmailEventAdapterConstants.MAIL_SMTP_PORT);
            signature = props.getProperty(EmailEventAdapterConstants.MAIL_SMTP_SIGNATURE);
            if (smtpFrom == null) {
                String msg = "failed to connect to the mail server due to null smtpFrom value";
                if (log.isDebugEnabled()) {
                    log.debug(msg);
                }
                throw new ConnectionUnavailableException("The adapter " +
                        eventAdapterConfiguration.getName() + " " + msg);

            }

            if (smtpHost == null) {
                String msg = "failed to connect to the mail server due to null smtpHost value";
                if (log.isDebugEnabled()) {
                    log.debug(msg);
                }
                throw new ConnectionUnavailableException
                        ("The adapter " + eventAdapterConfiguration.getName() + " " + msg);
            }

            if (smtpPort == null) {
                String msg = "failed to connect to the mail server due to null smtpPort value";
                if (log.isDebugEnabled()) {
                    log.debug(msg);
                }
                throw new ConnectionUnavailableException
                        ("The adapter " + eventAdapterConfiguration.getName() + " " + msg);
            }

            String replyTo = props.getProperty(EmailEventAdapterConstants.MAIL_SMTP_REPLY_TO);
            if (replyTo != null) {

                try {
                    smtpReplyToAddress = InternetAddress.parse(replyTo);
                } catch (AddressException e) {
                    log.error("Error in retrieving smtp replyTo address : " + smtpFrom, e);
                    String msg =
                            "failed to connect to the mail server due to error in retrieving " + "smtp replyTo address";
                    throw new ConnectionUnavailableException(
                            "The adapter " + eventAdapterConfiguration.getName() + " " + msg, e);
                }
            }

            try {
                smtpFromAddress = new InternetAddress(smtpFrom);
            } catch (AddressException e) {
                String msg = "failed to connect to the mail server due to error in retrieving " +
                        "smtp from address";
                if (log.isDebugEnabled()) {
                    log.debug(msg + ": " + smtpFrom);
                }
                throw new ConnectionUnavailableException
                        ("The adapter " + eventAdapterConfiguration.getName() + " " + msg, e);
            }

            //Retrieving username and password of SMTP server.
            smtpUsername = props.getProperty(MailConstants.MAIL_SMTP_USERNAME);
            smtpPassword = props.getProperty(MailConstants.MAIL_SMTP_PASSWORD);


            //initializing SMTP server to create session object.
            if (smtpUsername != null && smtpPassword != null && !smtpUsername.isEmpty() && !smtpPassword.isEmpty()) {
                session = Session.getInstance(props, new Authenticator() {
                    public PasswordAuthentication
                    getPasswordAuthentication() {
                        return new PasswordAuthentication(smtpUsername, smtpPassword);
                    }
                });
            } else {
                session = Session.getInstance(props);
                log.info("Connecting adapter " + eventAdapterConfiguration.getName() + "without user authentication for tenant " + tenantId);
            }
        }
    }

    /**
     * This will be invoked upon a successful trigger of
     * a data stream.
     *
     * @param message           the event stream data.
     * @param dynamicProperties the dynamic attributes of the email.
     */


    @Override
    public void publish(Object message, Map<String, String> dynamicProperties) {
        //Get subject and emailIds from dynamic properties
        String subject = dynamicProperties.get(EmailEventAdapterConstants.ADAPTER_MESSAGE_EMAIL_SUBJECT);
        String[] emailIds = dynamicProperties.get(EmailEventAdapterConstants.ADAPTER_MESSAGE_EMAIL_ADDRESS)
                .replaceAll(" ", "").split(EmailEventAdapterConstants.EMAIL_SEPARATOR);
        String emailType = dynamicProperties.get(EmailEventAdapterConstants.APAPTER_MESSAGE_EMAIL_TYPE);

        //Send email for each emailId
        for (String email : emailIds) {
            if (log.isDebugEnabled()) {
                log.debug("Attempting to send an email to " + email);
            }
            try {
                threadPoolExecutor.submit(new EmailSender(email, subject, message.toString(), emailType));
            } catch (RejectedExecutionException e) {
                EventAdapterUtil.logAndDrop(eventAdapterConfiguration.getName(), message, "Job queue is full", e, log, tenantId);
            }
        }
    }

    @Override
    public void disconnect() {
        //not required
    }

    @Override
    public void destroy() {
        //not required
    }

    @Override
    public boolean isPolled() {
        return false;
    }


    class EmailSender implements Runnable {
        String to;
        String subject;
        String body;
        String type;

        EmailSender(String to, String subject, String body, String type) {
            this.to = to;
            this.subject = subject;
            this.body = body;
            this.type = type;
        }

        /**
         * Sending emails to the corresponding Email IDs'.
         */
        @Override
        public void run() {

            if (log.isDebugEnabled()) {
                log.debug("Format of the email:" + " " + to + "->" + type);
            }

            //Creating MIME object using initiated session.
            MimeMessage message = new MimeMessage(session);

            //Setting up the Email attributes and Email payload.
            try {
                if (signature != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Email Signature is configured as: " + signature);
                    }
                    message.setFrom(new InternetAddress(smtpFromAddress.getAddress(), signature));
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Email Signature is not configured.");
                    }
                    message.setFrom(smtpFromAddress);
                }

                if (smtpReplyToAddress != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Email reply to address is configured as: " + smtpReplyToAddress[0].getAddress());
                    }
                    message.setReplyTo(smtpReplyToAddress);
                }

                message.addRecipient(Message.RecipientType.TO,
                        new InternetAddress(to));

                message.setSubject(subject);
                message.setSentDate(new Date());
                message.setContent(body, type);

                if (log.isDebugEnabled()) {
                    log.debug("Meta data of the email configured successfully");
                }

                loggableEmail = to;
                if (LoggerUtils.isLogMaskingEnable) {
                    loggableEmail = LoggerUtils.getMaskedContent(loggableEmail);
                }
                if (LoggerUtils.isDiagnosticLogsEnabled()) {
                    DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder = new DiagnosticLog.DiagnosticLogBuilder(
                            EmailEventAdapterConstants.LogConstants.EMAIL_EVENT_ADAPTER_SERVICE,
                            EmailEventAdapterConstants.LogConstants.ActionIDs.HANDOVER_EVENT);
                    diagnosticLogBuilder
                            .inputParam(EmailEventAdapterConstants.LogConstants.InputKeys.TENANT_DOMAIN, tenantDomain)
                            .inputParam(EmailEventAdapterConstants.LogConstants.InputKeys.EMAIL_TO, loggableEmail)
                            .resultMessage("Email will be handed over to the email server.")
                            .resultStatus(DiagnosticLog.ResultStatus.SUCCESS)
                            .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION);
                    LoggerUtils.triggerDiagnosticLogEvent(diagnosticLogBuilder);
                }
                log.info(String.format("Email will be handed over to the email server for tenant %s.", tenantDomain));
                Transport.send(message);

                if (log.isDebugEnabled()) {
                    log.debug("Mail sent to the EmailID " + loggableEmail + " Successfully");
                }
            } catch (MessagingException e) {
                LogMessagingException(e, to, 0);
                EventAdapterUtil.logAndDrop(eventAdapterConfiguration.getName(), message, "Error in message format", e, log, tenantId);
            } catch (Exception e) {
                EventAdapterUtil.logAndDrop(eventAdapterConfiguration.getName(), message, "Error sending email to '" + loggableEmail + "'", e, log, tenantId);
            }
        }

    }

    private void LogMessagingException(MessagingException e, String mailRecipient, int recurseCount) {

        if (e instanceof SendFailedException) {
            List<String> sentMails = new ArrayList<>();
            List<String> invalidMails = new ArrayList<>();
            List<String> unsentMails = new ArrayList<>();

            Address[] sent = ((SendFailedException) e).getValidSentAddresses();
            if (ArrayUtils.isNotEmpty(sent)) {
                for (Address address : sent) {
                    sentMails.add(address.toString());
                }
            }

            Address[] invalid = ((SendFailedException) e).getInvalidAddresses();
            if (ArrayUtils.isNotEmpty(invalid)) {
                for (Address address : invalid) {
                    invalidMails.add(address.toString());
                }
            }

            Address[] unsent = ((SendFailedException) e).getValidUnsentAddresses();
            if (ArrayUtils.isNotEmpty(unsent)) {
                for (Address address : unsent) {
                    unsentMails.add(address.toString());
                }
            }

            log.error(String.format("Exception occurred when sending email to %s. %s. Sent email=%s, " +
                            "Invalid emails=%s, Unsent valid emails=%s",
                    mailRecipient,
                    e.getMessage(),
                    StringUtils.join(sentMails, ","),
                    StringUtils.join(invalidMails, ","),
                    StringUtils.join(unsentMails, ",")), e);

        }

        log.error("Exception occurred when sending email to " + mailRecipient + ". " + e.getMessage(), e);

        // MessagingException has the capability to chain exceptions.
        // Therefore checking for any chained exceptions.
        Exception nextEx = e.getNextException();
        if (nextEx instanceof MessagingException) {
            if (recurseCount < 10) {
                LogMessagingException((MessagingException) nextEx, mailRecipient, recurseCount + 1);
            } else {
                log.warn("Over " + recurseCount + " chained exceptions found when logging MessagingExceptions. " +
                        "Stopping the exception check at this point.");
            }
        }
    }

}
