/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.input.adapter.email;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapter;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.exception.ConnectionUnavailableException;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.wso2.carbon.event.input.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.input.adapter.email.internal.util.EmailEventAdapterConstants;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Session.class, Store.class, Folder.class, Message.class})
public class EmailEventAdapterTest {

    private static final String LOCALHOST = "localhost";
    private static final String PROTOCOL = "imap";
    private static final String PORT = "443";
    private static final String EMAIL_SUBJECT = "email_subject";
    private static final Path testDir = Paths.get("src", "test", "resources");

    @Before
    public void initialize() throws MessagingException, IOException {

        mockStatic(Session.class);
        Session session = mock(Session.class);
        Store store = mock(Store.class);
        Folder folder = mock(Folder.class);
        Message messageRO = mock(Message.class);
        Message messageSeen = mock(Message.class);
        Message messageDeleted = mock(Message.class);
        Message message = mock(Message.class);

        Message[] messages = new Message[4];
        messages[0] = messageRO;
        messages[1] = messageSeen;
        messages[2] = messageDeleted;
        messages[3] = message;
        String[] roArray = {"RO"};

        Properties properties = new Properties();
        // server setting
        properties.put(String.format("mail.%s.host", PROTOCOL), LOCALHOST);
        properties.put(String.format("mail.%s.port", PROTOCOL), PORT);

        // SSL setting
        properties.setProperty(
                String.format("mail.%s.socketFactory.class", PROTOCOL),
                "javax.net.ssl.SSLSocketFactory");
        properties.setProperty(
                String.format("mail.%s.socketFactory.fallback", PROTOCOL),
                "false");
        properties.setProperty(
                String.format("mail.%s.socketFactory.port", PROTOCOL),
                String.valueOf(PORT));

        when(Session.getDefaultInstance(properties)).thenReturn(session);
        when(session.getStore(PROTOCOL)).thenReturn(store);
        when(store.isConnected()).thenReturn(true);
        when(store.getFolder("INBOX")).thenReturn(folder);
        when(folder.getMessageCount()).thenReturn(4);
        when(messageRO.getHeader("Status")).thenReturn(roArray);
        when(messageSeen.isSet(Flags.Flag.SEEN)).thenReturn(true);
        when(messageDeleted.isSet(Flags.Flag.DELETED)).thenReturn(true);
        when(message.getSubject()).thenReturn(EMAIL_SUBJECT);
        when(message.getContentType()).thenReturn("text/plain");
        when(message.getContent()).thenReturn("email text");
        when(folder.getMessages()).thenReturn(messages);

        System.setProperty("carbon.home", Paths.get(testDir.toString(), "carbon-context").toString());
        System.setProperty("tenant.name", "testTenant");
    }

    private InputEventAdapterConfiguration getEmailConfigurations(String email, String interval, String port) {
        InputEventAdapterConfiguration configuration = new InputEventAdapterConfiguration();
        configuration.setType("email");
        Map<String, String> properties = new HashMap<>();
        properties.put(EmailEventAdapterConstants.ADAPTER_CONF_RECEIVING_EMAIL_ADDRESS, email);
        properties.put(EmailEventAdapterConstants.ADAPTER_CONF_RECEIVING_EMAIL_POLL_INTERVAL, interval);
        properties.put(EmailEventAdapterConstants.ADAPTER_CONF_RECEIVING_EMAIL_PROTOCOL_PORT, port);
        properties.put(EmailEventAdapterConstants.ADAPTER_CONF_RECEIVING_EMAIL_PROTOCOL, PROTOCOL);
        properties.put(EmailEventAdapterConstants.ADAPTER_CONF_RECEIVING_EMAIL_PROTOCOL_HOST, LOCALHOST);
        properties.put(EmailEventAdapterConstants.ADAPTER_CONF_RECEIVING_EMAIL_USERNAME, "admin");
        properties.put(EmailEventAdapterConstants.ADAPTER_CONF_RECEIVING_EMAIL_PASSWORD, "admin");
        properties.put(EmailEventAdapterConstants.ADAPTER_MESSAGE_RECEIVING_EMAIL_SUBJECT, EMAIL_SUBJECT);
        configuration.setProperties(properties);
        return configuration;
    }

    @Test
    public void testEmailEventAdapterFactory() throws InputEventAdapterException {
        EmailEventAdapterFactory emailEventAdapterFactory = new EmailEventAdapterFactory();
        Assert.assertNotNull(emailEventAdapterFactory.getPropertyList());
        Assert.assertEquals("email", emailEventAdapterFactory.getType());
        Assert.assertNotNull(emailEventAdapterFactory.getSupportedMessageFormats());

        InputEventAdapter emailEventAdapter = emailEventAdapterFactory.createEventAdapter(
                getEmailConfigurations("test@wso2.com", "10", "443"), new HashMap<String, String>());
        Assert.assertTrue(emailEventAdapter instanceof EmailEventAdapter);
        InputEventAdapterListener listener = new InputEventAdapterListener() {
            @Override
            public void onEvent(Object object) {

            }

            @Override
            public void connectionUnavailable(ConnectionUnavailableException connectionUnavailableException) {

            }
        };
        emailEventAdapter.init(listener);
        Assert.assertEquals(listener, ((EmailEventAdapter)emailEventAdapter).getEventAdaptorListener());
    }

    @Test(expected = InputEventAdapterException.class)
    public void testInvalidEmail() throws InputEventAdapterException {
        EmailEventAdapterFactory emailEventAdapterFactory = new EmailEventAdapterFactory();

        InputEventAdapter emailEventAdapter = emailEventAdapterFactory.createEventAdapter(
                getEmailConfigurations("invalidEmail", "10", "443"), new HashMap<String, String>());
        emailEventAdapter.init(new InputEventAdapterListener() {
            @Override
            public void onEvent(Object object) {

            }

            @Override
            public void connectionUnavailable(ConnectionUnavailableException connectionUnavailableException) {

            }
        });
    }

    @Test(expected = InputEventAdapterException.class)
    public void testInvalidInterval() throws InputEventAdapterException {
        EmailEventAdapterFactory emailEventAdapterFactory = new EmailEventAdapterFactory();

        InputEventAdapter emailEventAdapter = emailEventAdapterFactory.createEventAdapter(
                getEmailConfigurations("test@wso2.com", "ten", "443"), new HashMap<String, String>());
        emailEventAdapter.init(new InputEventAdapterListener() {
            @Override
            public void onEvent(Object object) {

            }

            @Override
            public void connectionUnavailable(ConnectionUnavailableException connectionUnavailableException) {

            }
        });
    }

    @Test(expected = InputEventAdapterException.class)
    public void testInvalidPort() throws InputEventAdapterException {

        EmailEventAdapterFactory emailEventAdapterFactory = new EmailEventAdapterFactory();
        InputEventAdapter emailEventAdapter = emailEventAdapterFactory.createEventAdapter(
                getEmailConfigurations("test@wso2.com", "10", "forty three"), new HashMap<String, String>());
        emailEventAdapter.init(new InputEventAdapterListener() {
            @Override
            public void onEvent(Object object) {

            }

            @Override
            public void connectionUnavailable(ConnectionUnavailableException connectionUnavailableException) {

            }
        });
    }

    @Test(expected = TestConnectionNotSupportedException.class)
    public void testEmailEventAdapterTestConnect() throws InputEventAdapterException,
            TestConnectionNotSupportedException {

        InputEventAdapter emailEventAdapter = new EmailEventAdapter(getEmailConfigurations(
                "test@wso2.com", "5", "443"), new HashMap<String, String>());

        InputEventAdapterListener listener = new InputEventAdapterListener() {
            @Override
            public void onEvent(Object object) {

            }

            @Override
            public void connectionUnavailable(ConnectionUnavailableException connectionUnavailableException) {

            }
        };
        emailEventAdapter.init(listener);
        emailEventAdapter.testConnect();
    }

    @Test
    public void testEmailEventAdapterConnect() throws InputEventAdapterException, TestConnectionNotSupportedException,
            InterruptedException {

        InputEventAdapter emailEventAdapter = new EmailEventAdapter(getEmailConfigurations(
                "test@wso2.com", "2", PORT), new HashMap<String, String>());

        InputEventAdapterListener listener = new InputEventAdapterListener() {
            @Override
            public void onEvent(Object object) {
                Assert.assertEquals("email text", object);
            }

            @Override
            public void connectionUnavailable(ConnectionUnavailableException connectionUnavailableException) {

            }
        };
        emailEventAdapter.init(listener);
        emailEventAdapter.connect();
        Thread.sleep(5000);
        emailEventAdapter.disconnect();
    }
}
