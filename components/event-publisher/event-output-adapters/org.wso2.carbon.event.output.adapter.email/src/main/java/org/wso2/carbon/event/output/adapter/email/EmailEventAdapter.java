/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.carbon.event.output.adapter.email;

import com.sun.mail.smtp.SMTPTransport;
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
import org.wso2.carbon.event.output.adapter.email.internal.util.EmailEventAdapterUtil;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
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

import static org.wso2.carbon.event.output.adapter.core.EventAdapterSecretProcessor.decryptCredential;
import static org.wso2.carbon.event.output.adapter.core.EventAdapterUtil.getAccessToken;
import static org.wso2.carbon.event.output.adapter.email.internal.util.EmailEventAdapterConstants.ADAPTER_EMAIL_AUTH_TYPE;
import static org.wso2.carbon.event.output.adapter.email.internal.util.EmailEventAdapterConstants.ADAPTER_EMAIL_CLIENT_ID;
import static org.wso2.carbon.event.output.adapter.email.internal.util.EmailEventAdapterConstants.ADAPTER_EMAIL_CLIENT_SECRET;
import static org.wso2.carbon.event.output.adapter.email.internal.util.EmailEventAdapterConstants.ADAPTER_EMAIL_SCOPES;
import static org.wso2.carbon.event.output.adapter.email.internal.util.EmailEventAdapterConstants.ADAPTER_EMAIL_SMTP_AUTH_MECHANISMS;
import static org.wso2.carbon.event.output.adapter.email.internal.util.EmailEventAdapterConstants.ADAPTER_EMAIL_SMTP_FROM;
import static org.wso2.carbon.event.output.adapter.email.internal.util.EmailEventAdapterConstants.ADAPTER_EMAIL_SMTP_SSL_TRUST;
import static org.wso2.carbon.event.output.adapter.email.internal.util.EmailEventAdapterConstants.ADAPTER_EMAIL_TOKEN_ENDPOINT;
import static org.wso2.carbon.event.output.adapter.email.internal.util.EmailEventAdapterConstants.BASIC;
import static org.wso2.carbon.event.output.adapter.email.internal.util.EmailEventAdapterConstants.CLIENT_CREDENTIAL;
import static org.wso2.carbon.event.output.adapter.email.internal.util.EmailEventAdapterConstants.CLIENT_ID;
import static org.wso2.carbon.event.output.adapter.email.internal.util.EmailEventAdapterConstants.CLIENT_SECRET;
import static org.wso2.carbon.event.output.adapter.email.internal.util.EmailEventAdapterConstants.MAIL_SMTP_HOST;
import static org.wso2.carbon.event.output.adapter.email.internal.util.EmailEventAdapterConstants.MAX_RETRY_ATTEMPTS;
import static org.wso2.carbon.event.output.adapter.email.internal.util.EmailEventAdapterConstants.PASSWORD;
import static org.wso2.carbon.event.output.adapter.email.internal.util.EmailEventAdapterConstants.SMTP_PROTOCOL;
import static org.wso2.carbon.event.output.adapter.email.internal.util.EmailEventAdapterConstants.USERNAME;
import static org.wso2.carbon.event.output.adapter.email.internal.util.EmailEventAdapterConstants.XOAUTH2;

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
    private String smtpPassword;
    private String smtpUsername;
    private String clientId;
    private String clientSecret;

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
            // Default SMTP properties for outgoing messages.
            String smtpFrom;
            String smtpHost;
            String smtpPort;

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
                String msg = "failed to connect to the mail server due to error in retrieving smtp from address";
                if (log.isDebugEnabled()) {
                    log.debug(msg + ": " + smtpFrom);
                }
                throw new ConnectionUnavailableException
                        ("The adapter " + eventAdapterConfiguration.getName() + " " + msg, e);
            }

            Map<String, String> credentials = resolveCredentials(globalProperties);
            smtpUsername = credentials.get(USERNAME);
            smtpPassword = credentials.get(PASSWORD);

            //initializing SMTP server to create session object.
            if (smtpUsername != null && smtpPassword != null && !smtpUsername.isEmpty() && !smtpPassword.isEmpty()) {
                if (CLIENT_CREDENTIAL.equalsIgnoreCase(props.getProperty(ADAPTER_EMAIL_AUTH_TYPE))) {
                    props.put(ADAPTER_EMAIL_SMTP_AUTH_MECHANISMS, XOAUTH2);
                    props.put(ADAPTER_EMAIL_SMTP_SSL_TRUST, props.getProperty(MAIL_SMTP_HOST));
                }
                session = Session.getInstance(props, new Authenticator() {
                    public PasswordAuthentication
                    getPasswordAuthentication() {
                        return new PasswordAuthentication(smtpUsername, smtpPassword);
                    }
                });
            } else {
                session = Session.getInstance(props);
                log.info("Connecting adapter " + eventAdapterConfiguration.getName() +
                        " without user authentication for tenant " + tenantId);
            }
        }
    }

    private Map<String, String> resolveCredentials(Map<String, String> props) {

        Map<String, String> credentials = new HashMap<>();
        if (CLIENT_CREDENTIAL.equalsIgnoreCase(props.get(ADAPTER_EMAIL_AUTH_TYPE))) {
            try {
                clientId = decryptCredential(EmailEventAdapterConstants.EMAIL_PROVIDER, CLIENT_CREDENTIAL,
                        CLIENT_ID);
            } catch (SecretManagementException e) {
                clientId = props.get(ADAPTER_EMAIL_CLIENT_ID);
            }
            try {
                clientSecret = decryptCredential(EmailEventAdapterConstants.EMAIL_PROVIDER, CLIENT_CREDENTIAL,
                        CLIENT_SECRET);
            } catch (SecretManagementException e) {
                clientSecret = props.get(ADAPTER_EMAIL_CLIENT_SECRET);
            }
            String tokenEndpoint = props.get(EmailEventAdapterConstants.ADAPTER_EMAIL_TOKEN_ENDPOINT);
            String scopes = props.get(EmailEventAdapterConstants.ADAPTER_EMAIL_SCOPES);

            if (StringUtils.isEmpty(clientId) || StringUtils.isEmpty(clientSecret) ||
                    StringUtils.isEmpty(tokenEndpoint) || StringUtils.isEmpty(scopes)) {
                throw new ConnectionUnavailableException("The adapter " + eventAdapterConfiguration.getName() +
                        " failed to connect to the mail server due to missing client credentials");
            }
            credentials.put(USERNAME, props.get(ADAPTER_EMAIL_SMTP_FROM));
            credentials.put(PASSWORD, getAccessToken(clientId, clientSecret, tokenEndpoint, scopes));
        } else {
            String userName;
            String password;
            try {
                userName = decryptCredential(EmailEventAdapterConstants.EMAIL_PROVIDER, BASIC, USERNAME);
            } catch (SecretManagementException e) {
                userName = props.get(MailConstants.MAIL_SMTP_USERNAME);
            }
            try {
                password = decryptCredential(EmailEventAdapterConstants.EMAIL_PROVIDER, BASIC, PASSWORD);
            } catch (SecretManagementException e) {
                password = props.get(MailConstants.MAIL_SMTP_PASSWORD);
            }
            credentials.put(USERNAME, userName);
            credentials.put(PASSWORD, password);
        }
        return credentials;
    }

    /**
     * This will be invoked upon a successful trigger of a data stream.
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

                message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
                message.setSubject(subject);
                message.setSentDate(new Date());
                message.setContent(body, type);

                if (log.isDebugEnabled()) {
                    log.debug("Meta data of the email configured successfully");
                }

                // Sending the email using SMTP transport.
                sendWithRetry(message);

                if (log.isDebugEnabled()) {
                    log.debug("Mail sent to the EmailID " + to + " Successfully");
                }
            } catch (MessagingException e) {
                LogMessagingException(e, to, 0);
                EmailEventAdapterUtil.logAndDrop(eventAdapterConfiguration.getName(), message,
                        "Error in message format", e, log, tenantId);
            } catch (Exception e) {
                EventAdapterUtil.logAndDrop(eventAdapterConfiguration.getName(), message,
                        "Error sending email to '" + to + "'", e, log, tenantId);
            }
        }
    }

    private void sendWithRetry(Message message) throws MessagingException {

        try {
            Transport.send(message);
        } catch (AuthenticationFailedException exception) {
            // Retry only for CLIENT_CREDENTIAL authentication type.
            if (!CLIENT_CREDENTIAL.equalsIgnoreCase(globalProperties.get(ADAPTER_EMAIL_AUTH_TYPE))) {
                throw new AuthenticationFailedException();
            }
            handleRetry(message);
            log.warn("Authentication failed, attempting token refresh and retry...", exception);
        }
    }

    private void handleRetry(Message message) throws MessagingException {

        int attempts = 0;

        while (attempts < MAX_RETRY_ATTEMPTS) {
            SMTPTransport transport;
            attempts++;

            try {
                transport = (SMTPTransport) session.getTransport(SMTP_PROTOCOL);
                smtpPassword = getAccessToken(clientId, clientSecret,
                        globalProperties.get(ADAPTER_EMAIL_TOKEN_ENDPOINT),
                        globalProperties.get(ADAPTER_EMAIL_SCOPES));
                transport.connect(globalProperties.get(MAIL_SMTP_HOST), smtpUsername, smtpPassword);

                transport.sendMessage(message, message.getAllRecipients());
                if (log.isDebugEnabled()) {
                    log.debug("Mail sent to the EmailID " + Arrays.toString(message.getAllRecipients()) +
                            " Successfully with retry.");
                }
                return;

            } catch (Exception e) {
                log.warn("Authentication failed, attempting token refresh and retry (attempt " + attempts + ")", e);
            }
            if (attempts == MAX_RETRY_ATTEMPTS) {
                throw new MessagingException("Authentication failed even after retrying with new token.");
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

        String errorMessage = "Exception occurred when sending email to " + mailRecipient + ". " + e.getMessage();
        if (e instanceof AuthenticationFailedException) {
            log.warn(errorMessage, e);
        } else {
            log.error(errorMessage, e);
        }

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
