package org.wso2.carbon.event.output.adapter.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.MockedStatic;
import org.mockito.verification.VerificationMode;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.core.EventAdapterSecretProcessor;
import org.wso2.carbon.event.output.adapter.core.EventAdapterUtil;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.Property;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.output.adapter.core.internal.ds.OutputEventAdapterServiceValueHolder;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.timeout;

/**
 * Unit tests for HTTP Output Adaptor.
 */
public class HttpOutputAdaptorTestCase {
    private static final Log logger = LogFactory.getLog(HttpOutputAdaptorTestCase.class);
    private static final Path testDir = Paths.get("src", "test", "resources");

    // Timeout for async operations in milliseconds
    private static final int ASYNC_TIMEOUT_MS = 5000;

    /**
     * Returns a Mockito verification mode that waits up to ASYNC_TIMEOUT_MS for the interaction.
     */
    private static VerificationMode asyncVerification() {
        return timeout(ASYNC_TIMEOUT_MS).times(1);
    }

    /**
     * Returns a Mockito verification mode that waits up to ASYNC_TIMEOUT_MS for at least one interaction.
     */
    private static VerificationMode asyncVerificationAtLeastOnce() {
        return timeout(ASYNC_TIMEOUT_MS).atLeastOnce();
    }

    private void setupCarbonConfig() {
        System.setProperty("carbon.home",
                Paths.get(testDir.toString(), "carbon-context").toString());
        System.setProperty("tenant.name", "tenant.name");
    }

    private HTTPEventAdapter getHttpAdaptor() {
        setupCarbonConfig();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1234);
        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("TestHttpAdaptor");
        eventAdapterConfiguration.setType("http");
        eventAdapterConfiguration.setMessageFormat("text");
        Map<String, String> staticPropertes = new HashMap<>();
        staticPropertes.put("http.client.method", "HttpPost");
        staticPropertes.put("http.proxy.port", "9090");
        staticPropertes.put("http.proxy.host", "localhost");
        eventAdapterConfiguration.setStaticProperties(staticPropertes);
        Map<String, String> globalProperties = new HashMap<>();
        globalProperties.put("jobQueueSize", "10000");
        globalProperties.put("keepAliveTimeInMillis", "20000");
        globalProperties.put("maxThread", "100");
        globalProperties.put("minThread", "8");
        globalProperties.put("defaultMaxConnectionsPerHost", "50");
        globalProperties.put("maxTotalConnections", "1000");
        return new HTTPEventAdapter(eventAdapterConfiguration, globalProperties);
    }

    @Test
    public void testHTTPPublisherInit() throws OutputEventAdapterException {
        HTTPEventAdapter httpEventAdapter = getHttpAdaptor();
        httpEventAdapter.init();
        httpEventAdapter.isPolled();
        httpEventAdapter.destroy();
    }

    /**
     * Test connection of Http output adaptor
     *
     * @throws TestConnectionNotSupportedException
     */
    @Test(expectedExceptions = {TestConnectionNotSupportedException.class})
    public void testHttpPublisherTestConnect() throws TestConnectionNotSupportedException {
        logger.info("Test case for test the connection ins valid which is not implemented of email output adaptor.");
        HTTPEventAdapter httpEventAdapter = getHttpAdaptor();
        httpEventAdapter.testConnect();
    }

    @Test
    public void testHttpPublisherConnect() throws OutputEventAdapterException {
        logger.info("Test case for connection of email output adaptor.");
        HTTPEventAdapter httpEventAdapter = getHttpAdaptor();
        httpEventAdapter.init();
        httpEventAdapter.connect();
        httpEventAdapter.disconnect();
        httpEventAdapter.destroy();
    }

    @Test
    public void testHttpPublisherConnect2() throws OutputEventAdapterException {
        logger.info("Test case for connection of email output adaptor.");
        setupCarbonConfig();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1234);
        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("TestHttpAdaptor");
        eventAdapterConfiguration.setType("http");
        eventAdapterConfiguration.setMessageFormat("text");
        Map<String, String> staticPropertes = new HashMap<>();
        staticPropertes.put("http.client.method", "HttpPost");
        staticPropertes.put("http.proxy.port", null);
        staticPropertes.put("http.proxy.host", null);
        eventAdapterConfiguration.setStaticProperties(staticPropertes);
        Map<String, String> globalProperties = new HashMap<>();
        globalProperties.put("jobQueueSize", "10000");
        globalProperties.put("keepAliveTimeInMillis", "20000");
        globalProperties.put("maxThread", "100");
        globalProperties.put("minThread", "8");
        globalProperties.put("defaultMaxConnectionsPerHost", "50");
        globalProperties.put("maxTotalConnections", "1000");
        HTTPEventAdapter httpEventAdapter = new HTTPEventAdapter(eventAdapterConfiguration, globalProperties);
        httpEventAdapter.init();
        httpEventAdapter.connect();
        httpEventAdapter.disconnect();
        httpEventAdapter.destroy();
    }

    @Test
    public void testHttpPublisherPublish() throws InterruptedException {
        logger.info("Test case for publishing email in email output adaptor.");
        SecretManager secretManager = new SecretManagerImpl();
        OutputEventAdapterServiceValueHolder.setSecretManager(secretManager);
        HTTPEventAdapter httpEventAdapter = getHttpAdaptor();
        try {
            httpEventAdapter.init();
            httpEventAdapter.connect();
            Map<String, String> dynamicProperties = new HashMap<>();
            dynamicProperties.put("http.password", "pwd123");
            dynamicProperties.put("http.headers", null);
            dynamicProperties.put("http.url", "http://localhost:8080/service");
            dynamicProperties.put("http.username", "user123");
            httpEventAdapter.publish("hi", dynamicProperties);
            Thread.sleep(1000);
        } catch (OutputEventAdapterException e) {
            logger.error(e.getMessage());
        } finally {
            httpEventAdapter.disconnect();
            httpEventAdapter.destroy();
        }


    }

    @Test
    public void testHttpPublisherPublish2() {
        logger.info("Test case for publishing email in email output adaptor.");
        SecretManager secretManager = new SecretManagerImpl();
        OutputEventAdapterServiceValueHolder.setSecretManager(secretManager);
        HTTPEventAdapter httpEventAdapter = getHttpAdaptor();
        try {
            httpEventAdapter.init();
            httpEventAdapter.connect();
            Map<String, String> dynamicProperties = new HashMap<>();
            dynamicProperties.put("http.password", "pwd123");
            dynamicProperties.put("http.headers", "Content-Type:application/json");
            dynamicProperties.put("http.url", "http://localhost:8080/service");
            dynamicProperties.put("http.username", "user123");
            httpEventAdapter.publish("hi", dynamicProperties);
        } catch (OutputEventAdapterException e) {
            logger.error(e.getMessage());
        } finally {
            httpEventAdapter.disconnect();
            httpEventAdapter.destroy();
        }

    }

    @Test
    public void testHttpPublisherFactory() {
        logger.info("Test case for factory properties of email output adaptor.");
        HTTPEventAdapterFactory adapterFactory = new HTTPEventAdapterFactory();
        List<Property> dyPropertyList = adapterFactory.getDynamicPropertyList();
        List<Property> propertyList = new ArrayList<>();
        Property property = new Property("http.url");
        property.setRequired(true);
        property.setSecured(false);
        property.setEncrypted(false);
        property.setDisplayName("URL");
        property.setDefaultValue(null);
        property.setOptions(null);
        property.setHint("The target HTTP/HTTPS URL, e.g. \"http://yourhost:8080/service\"");
        propertyList.add(property);
        property = new Property("http.username");
        property.setRequired(false);
        property.setSecured(false);
        property.setEncrypted(false);
        property.setDisplayName("Username");
        property.setDefaultValue(null);
        property.setOptions(null);
        property.setHint("HTTP BasicAuth username");
        propertyList.add(property);
        property = new Property("http.password");
        property.setRequired(false);
        property.setSecured(true);
        property.setEncrypted(true);
        property.setDisplayName("Password");
        property.setDefaultValue(null);
        property.setOptions(null);
        property.setHint("HTTP BasicAuth password");
        propertyList.add(property);
        property = new Property("http.headers");
        property.setRequired(false);
        property.setSecured(false);
        property.setEncrypted(false);
        property.setDisplayName("Headers");
        property.setDefaultValue(null);
        property.setOptions(null);
        property.setHint("Custom HTTP headers, e.g. \"header1: value1, header2: value2\"");
        propertyList.add(property);
        Assert.assertEquals(4, dyPropertyList.size());
        int i = 0;
        for (Property prop : propertyList) {
            Assert.assertEquals(prop.getPropertyName(), dyPropertyList.get(i).getPropertyName());
            Assert.assertEquals(prop.getDefaultValue(), dyPropertyList.get(i).getDefaultValue());
            Assert.assertEquals(prop.getDisplayName(), dyPropertyList.get(i).getDisplayName());
            Assert.assertEquals(prop.getHint(), dyPropertyList.get(i).getHint());
            i++;
        }
        List<String> types = new ArrayList<>();
        types.add("text");
        types.add("xml");
        types.add("json");
        List<String> supportedTypes = adapterFactory.getSupportedMessageFormats();
        Assert.assertEquals(supportedTypes.toString(), types.toString());
        Assert.assertEquals("http", adapterFactory.getType());

        setupCarbonConfig();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1234);
        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("TestHttpAdaptor");
        eventAdapterConfiguration.setType("http");
        eventAdapterConfiguration.setMessageFormat("text");
        Map<String, String> staticPropertes = new HashMap<>();
        staticPropertes.put("http.client.method", "HttpPost");
        staticPropertes.put("http.proxy.port", "9090");
        staticPropertes.put("http.proxy.host", "localhost");
        eventAdapterConfiguration.setStaticProperties(staticPropertes);
        Map<String, String> globalProperties = new HashMap<>();
        globalProperties.put("jobQueueSize", "10000");
        globalProperties.put("keepAliveTimeInMillis", "20000");
        globalProperties.put("maxThread", "100");
        globalProperties.put("minThread", "8");
        globalProperties.put("defaultMaxConnectionsPerHost", "50");
        globalProperties.put("maxTotalConnections", "1000");
        adapterFactory.createEventAdapter(eventAdapterConfiguration, globalProperties);
    }

    /**
     * Test publish with BASIC authentication type using plain text credentials.
     * Verifies that when decryption fails, the adapter falls back to plain text credentials.
     */
    @Test
    public void testPublishWithBasicAuthPlainText() throws OutputEventAdapterException {

        SecretManager secretManager = new SecretManagerImpl();
        OutputEventAdapterServiceValueHolder.setSecretManager(secretManager);

        try (MockedStatic<EventAdapterSecretProcessor> mockedSecretProcessor =
                     mockStatic(EventAdapterSecretProcessor.class)) {
            // Simulate SecretManagementException to fall back to plain text
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor.decryptCredential(anyString(), anyString(), anyString()))
                    .thenThrow(new SecretManagementException("Secret not found"));

            HTTPEventAdapter httpEventAdapter = getHttpAdaptorWithAuthType("BASIC");
            httpEventAdapter.init();
            httpEventAdapter.connect();

            // Publish should not throw any exception
            httpEventAdapter.publish("{\"message\": \"test\"}", getDefaultDynamicProperties());

            // Verify that decryptCredential was called for userName using timeout-based verification
            mockedSecretProcessor.verify(() ->
                    EventAdapterSecretProcessor.decryptCredential(eq("EMAIL_PROVIDER"), eq("BASIC"), eq("userName")),
                    asyncVerification());

            httpEventAdapter.disconnect();
            httpEventAdapter.destroy();
        }
    }

    /**
     * Test publish with BASIC authentication type using encrypted credentials.
     * Verifies that decrypted credentials are used when decryption succeeds.
     */
    @Test
    public void testPublishWithBasicAuthEncrypted() throws OutputEventAdapterException {

        SecretManager secretManager = new SecretManagerImpl();
        OutputEventAdapterServiceValueHolder.setSecretManager(secretManager);

        try (MockedStatic<EventAdapterSecretProcessor> mockedSecretProcessor =
                     mockStatic(EventAdapterSecretProcessor.class)) {
            // Simulate successful decryption
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor.decryptCredential(
                            eq("EMAIL_PROVIDER"), eq("BASIC"), eq("userName")))
                    .thenReturn("encryptedUser".toCharArray());
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor.decryptCredential(
                            eq("EMAIL_PROVIDER"), eq("BASIC"), eq("password")))
                    .thenReturn("encryptedPass".toCharArray());

            HTTPEventAdapter httpEventAdapter = getHttpAdaptorWithAuthType("BASIC");
            httpEventAdapter.init();
            httpEventAdapter.connect();

            // Publish should not throw any exception
            httpEventAdapter.publish("{\"message\": \"test\"}", getDefaultDynamicProperties());

            // Verify that decryptCredential was called for both username and password
            mockedSecretProcessor.verify(() ->
                    EventAdapterSecretProcessor.decryptCredential(eq("EMAIL_PROVIDER"), eq("BASIC"), eq("userName")),
                    asyncVerification());
            mockedSecretProcessor.verify(() ->
                    EventAdapterSecretProcessor.decryptCredential(eq("EMAIL_PROVIDER"), eq("BASIC"), eq("password")),
                    asyncVerification());

            httpEventAdapter.disconnect();
            httpEventAdapter.destroy();
        }
    }

    /**
     * Test publish with BEARER authentication type using plain text token.
     * Verifies that when decryption fails, the adapter falls back to plain text token.
     */
    @Test
    public void testPublishWithBearerAuthPlainText() throws OutputEventAdapterException {

        SecretManager secretManager = new SecretManagerImpl();
        OutputEventAdapterServiceValueHolder.setSecretManager(secretManager);

        try (MockedStatic<EventAdapterSecretProcessor> mockedSecretProcessor =
                     mockStatic(EventAdapterSecretProcessor.class)) {
            // Simulate SecretManagementException to fall back to plain text
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor.decryptCredential(anyString(), anyString(), anyString()))
                    .thenThrow(new SecretManagementException("Secret not found"));

            HTTPEventAdapter httpEventAdapter = getHttpAdaptorWithAuthType("BEARER");
            httpEventAdapter.init();
            httpEventAdapter.connect();

            // Publish should not throw any exception
            httpEventAdapter.publish("{\"message\": \"test\"}", getDefaultDynamicProperties());

            // Verify that decryptCredential was called for access token
            mockedSecretProcessor.verify(() ->
                    EventAdapterSecretProcessor.decryptCredential(eq("EMAIL_PROVIDER"), eq("BEARER"), eq("accessToken")),
                    asyncVerification());

            httpEventAdapter.disconnect();
            httpEventAdapter.destroy();
        }
    }

    /**
     * Test publish with BEARER authentication type using encrypted token.
     * Verifies that decrypted access token is used when decryption succeeds.
     */
    @Test
    public void testPublishWithBearerAuthEncrypted() throws OutputEventAdapterException {

        SecretManager secretManager = new SecretManagerImpl();
        OutputEventAdapterServiceValueHolder.setSecretManager(secretManager);

        try (MockedStatic<EventAdapterSecretProcessor> mockedSecretProcessor =
                     mockStatic(EventAdapterSecretProcessor.class)) {
            // Simulate successful decryption
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor.decryptCredential(
                            eq("EMAIL_PROVIDER"), eq("BEARER"), eq("accessToken")))
                    .thenReturn("encryptedAccessToken".toCharArray());

            HTTPEventAdapter httpEventAdapter = getHttpAdaptorWithAuthType("BEARER");
            httpEventAdapter.init();
            httpEventAdapter.connect();

            // Publish should not throw any exception
            httpEventAdapter.publish("{\"message\": \"test\"}", getDefaultDynamicProperties());

            // Verify that decryptCredential was called for access token
            mockedSecretProcessor.verify(() ->
                    EventAdapterSecretProcessor.decryptCredential(eq("EMAIL_PROVIDER"), eq("BEARER"), eq("accessToken")),
                    asyncVerification());

            httpEventAdapter.disconnect();
            httpEventAdapter.destroy();
        }
    }

    /**
     * Test publish with API_KEY authentication type using plain text API key.
     * Verifies that when decryption fails, the adapter falls back to plain text API key.
     */
    @Test
    public void testPublishWithApiKeyAuthPlainText() throws OutputEventAdapterException {

        SecretManager secretManager = new SecretManagerImpl();
        OutputEventAdapterServiceValueHolder.setSecretManager(secretManager);

        try (MockedStatic<EventAdapterSecretProcessor> mockedSecretProcessor =
                     mockStatic(EventAdapterSecretProcessor.class)) {
            // Simulate SecretManagementException to fall back to plain text
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor.decryptCredential(anyString(), anyString(), anyString()))
                    .thenThrow(new SecretManagementException("Secret not found"));

            HTTPEventAdapter httpEventAdapter = getHttpAdaptorWithAuthType("API_KEY");
            httpEventAdapter.init();
            httpEventAdapter.connect();

            // Publish should not throw any exception
            httpEventAdapter.publish("{\"message\": \"test\"}", getDefaultDynamicProperties());

            // Verify that decryptCredential was called for API key value
            mockedSecretProcessor.verify(() ->
                    EventAdapterSecretProcessor.decryptCredential(eq("EMAIL_PROVIDER"), eq("API_KEY"), eq("apiKeyValue")),
                    asyncVerification());

            httpEventAdapter.disconnect();
            httpEventAdapter.destroy();
        }
    }

    /**
     * Test publish with API_KEY authentication type using encrypted API key.
     * Verifies that decrypted API key is used when decryption succeeds.
     */
    @Test
    public void testPublishWithApiKeyAuthEncrypted() throws OutputEventAdapterException {

        SecretManager secretManager = new SecretManagerImpl();
        OutputEventAdapterServiceValueHolder.setSecretManager(secretManager);

        try (MockedStatic<EventAdapterSecretProcessor> mockedSecretProcessor =
                     mockStatic(EventAdapterSecretProcessor.class)) {
            // Simulate successful decryption
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor.decryptCredential(
                            eq("EMAIL_PROVIDER"), eq("API_KEY"), eq("apiKeyValue")))
                    .thenReturn("encryptedApiKey".toCharArray());

            HTTPEventAdapter httpEventAdapter = getHttpAdaptorWithAuthType("API_KEY");
            httpEventAdapter.init();
            httpEventAdapter.connect();

            // Publish should not throw any exception
            httpEventAdapter.publish("{\"message\": \"test\"}", getDefaultDynamicProperties());

            // Verify that decryptCredential was called for API key value
            mockedSecretProcessor.verify(() ->
                    EventAdapterSecretProcessor.decryptCredential(eq("EMAIL_PROVIDER"), eq("API_KEY"), eq("apiKeyValue")),
                    asyncVerification());

            httpEventAdapter.disconnect();
            httpEventAdapter.destroy();
        }
    }

    /**
     * Test publish with CLIENT_CREDENTIAL authentication type using plain text credentials.
     * Verifies that when decryption fails, the adapter falls back to plain text credentials
     * and generates a new access token.
     */
    @Test
    public void testPublishWithClientCredentialAuthPlainText() throws OutputEventAdapterException {

        logger.info("Test case for publishing with CLIENT_CREDENTIAL auth type using plain text credentials.");
        SecretManager secretManager = new SecretManagerImpl();
        OutputEventAdapterServiceValueHolder.setSecretManager(secretManager);

        try (MockedStatic<EventAdapterSecretProcessor> mockedSecretProcessor =
                     mockStatic(EventAdapterSecretProcessor.class);
             MockedStatic<EventAdapterUtil> mockedUtil = mockStatic(EventAdapterUtil.class)) {
            // Simulate SecretManagementException to fall back to plain text
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor.decryptCredential(anyString(), anyString(), anyString()))
                    .thenThrow(new SecretManagementException("Secret not found"));
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor.encryptAndStoreCredential(anyString(), anyString(), anyString(), anyString()))
                    .thenThrow(new SecretManagementException("Cannot store secret"));
            // Mock the access token generation
            mockedUtil.when(() -> EventAdapterUtil.getAccessToken(anyString(), anyString(), anyString(), anyString()))
                    .thenAnswer(invocation -> "generatedAccessToken");

            HTTPEventAdapter httpEventAdapter = getHttpAdaptorWithAuthType("CLIENT_CREDENTIAL");
            httpEventAdapter.init();
            httpEventAdapter.connect();

            // Publish should not throw any exception
            httpEventAdapter.publish("{\"message\": \"test\"}", getDefaultDynamicProperties());

            // Verify that decryptCredential was called for internalAccessToken
            mockedSecretProcessor.verify(() ->
                    EventAdapterSecretProcessor.decryptCredential(eq("EMAIL_PROVIDER"), eq("CLIENT_CREDENTIAL"), eq("internalAccessToken")),
                    asyncVerification());
            // Verify that getAccessToken was called to generate new token
            mockedUtil.verify(() ->
                    EventAdapterUtil.getAccessToken(anyString(), anyString(), anyString(), anyString()),
                    asyncVerification());

            httpEventAdapter.disconnect();
            httpEventAdapter.destroy();
        }
    }

    /**
     * Test publish with CLIENT_CREDENTIAL authentication type using encrypted credentials.
     * Verifies that decrypted clientId and clientSecret are used when decryption succeeds.
     */
    @Test
    public void testPublishWithClientCredentialAuthEncrypted() throws OutputEventAdapterException {

        logger.info("Test case for publishing with CLIENT_CREDENTIAL auth type using encrypted credentials.");
        SecretManager secretManager = new SecretManagerImpl();
        OutputEventAdapterServiceValueHolder.setSecretManager(secretManager);

        try (MockedStatic<EventAdapterSecretProcessor> mockedSecretProcessor =
                     mockStatic(EventAdapterSecretProcessor.class);
             MockedStatic<EventAdapterUtil> mockedUtil = mockStatic(EventAdapterUtil.class)) {
            // Simulate SecretManagementException for internal access token
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor.decryptCredential(
                            eq("EMAIL_PROVIDER"), eq("CLIENT_CREDENTIAL"), eq("internalAccessToken")))
                    .thenThrow(new SecretManagementException("No stored token"));
            // Simulate successful decryption for clientId and clientSecret
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor.decryptCredential(
                            eq("EMAIL_PROVIDER"), eq("CLIENT_CREDENTIAL"), eq("clientId")))
                    .thenAnswer(invocation -> "encryptedClientId".toCharArray());
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor.decryptCredential(
                            eq("EMAIL_PROVIDER"), eq("CLIENT_CREDENTIAL"), eq("clientSecret")))
                    .thenAnswer(invocation -> "encryptedClientSecret".toCharArray());
            // Mock the access token generation
            mockedUtil.when(() -> EventAdapterUtil.getAccessToken(anyString(), anyString(), anyString(), anyString()))
                    .thenAnswer(invocation -> "generatedAccessToken");

            HTTPEventAdapter httpEventAdapter = getHttpAdaptorWithAuthType("CLIENT_CREDENTIAL");
            httpEventAdapter.init();
            httpEventAdapter.connect();

            // Publish should not throw any exception
            httpEventAdapter.publish("{\"message\": \"test\"}", getDefaultDynamicProperties());

            // Verify that decryptCredential was called for clientId and clientSecret
            mockedSecretProcessor.verify(() ->
                    EventAdapterSecretProcessor.decryptCredential(eq("EMAIL_PROVIDER"), eq("CLIENT_CREDENTIAL"), eq("clientId")),
                    asyncVerification());
            mockedSecretProcessor.verify(() ->
                    EventAdapterSecretProcessor.decryptCredential(eq("EMAIL_PROVIDER"), eq("CLIENT_CREDENTIAL"), eq("clientSecret")),
                    asyncVerification());
            // Verify that getAccessToken was called
            mockedUtil.verify(() ->
                    EventAdapterUtil.getAccessToken(anyString(), anyString(), anyString(), anyString()),
                    asyncVerification());

            httpEventAdapter.disconnect();
            httpEventAdapter.destroy();
        }
    }

    /**
     * Test publish with CLIENT_CREDENTIAL authentication type when access token is already cached.
     * Verifies that when internalAccessToken is retrieved from the secret manager,
     * it should be used directly.
     */
    @Test
    public void testPublishWithClientCredentialAuthWithCachedToken() throws OutputEventAdapterException {

        logger.info("Test case for publishing with CLIENT_CREDENTIAL auth type with cached access token.");
        SecretManager secretManager = new SecretManagerImpl();
        OutputEventAdapterServiceValueHolder.setSecretManager(secretManager);

        try (MockedStatic<EventAdapterSecretProcessor> mockedSecretProcessor =
                     mockStatic(EventAdapterSecretProcessor.class)) {
            // Simulate successful retrieval of stored internal access token
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor.decryptCredential(
                            anyString(), anyString(), anyString()))
                    .thenAnswer(invocation -> {
                        String property = invocation.getArgument(2);
                        if ("internalAccessToken".equals(property)) {
                            return "cachedAccessToken".toCharArray();
                        }
                        if ("clientId".equals(property)) {
                            return "testClientId".toCharArray();
                        }
                        if ("clientSecret".equals(property)) {
                            return "testClientSecret".toCharArray();
                        }
                        throw new SecretManagementException("Unexpected call for: " + property);
                    });

            HTTPEventAdapter httpEventAdapter = getHttpAdaptorWithAuthType("CLIENT_CREDENTIAL");
            httpEventAdapter.init();
            httpEventAdapter.connect();

            // Publish should not throw any exception
            httpEventAdapter.publish("{\"message\": \"test\"}", getDefaultDynamicProperties());

            // Verify that decryptCredential was called for internalAccessToken
            mockedSecretProcessor.verify(() ->
                    EventAdapterSecretProcessor.decryptCredential(eq("EMAIL_PROVIDER"), eq("CLIENT_CREDENTIAL"), eq("internalAccessToken")),
                    asyncVerification());

            // Publish again to test caching behavior
            httpEventAdapter.publish("{\"message\": \"test2\"}", getDefaultDynamicProperties());

            // Verify decryptCredential called at least once (first publish may trigger it)
            mockedSecretProcessor.verify(() ->
                    EventAdapterSecretProcessor.decryptCredential(eq("EMAIL_PROVIDER"), eq("CLIENT_CREDENTIAL"), eq("internalAccessToken")),
                    asyncVerificationAtLeastOnce());

            httpEventAdapter.disconnect();
            httpEventAdapter.destroy();
        }
    }

    /**
     * Test publish with NONE authentication type.
     * Verifies that no authentication is attempted when auth type is NONE.
     */
    @Test
    public void testPublishWithNoneAuthType() throws OutputEventAdapterException {

        logger.info("Test case for publishing with NONE auth type.");
        SecretManager secretManager = new SecretManagerImpl();
        OutputEventAdapterServiceValueHolder.setSecretManager(secretManager);

        try (MockedStatic<EventAdapterSecretProcessor> mockedSecretProcessor =
                     mockStatic(EventAdapterSecretProcessor.class)) {
            // decryptCredential should NOT be called for NONE auth type
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor.decryptCredential(anyString(), anyString(), anyString()))
                    .thenThrow(new SecretManagementException("Should not be called"));

            HTTPEventAdapter httpEventAdapter = getHttpAdaptorWithAuthType("NONE");
            httpEventAdapter.init();
            httpEventAdapter.connect();

            // Publish should not throw any exception
            httpEventAdapter.publish("{\"message\": \"test\"}", getDefaultDynamicProperties());

            // Wait briefly to allow async task to complete, then verify no interactions
            // Use a short timeout since we expect NO interactions
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Verify that decryptCredential was NOT called since NONE auth doesn't need credentials
            mockedSecretProcessor.verifyNoInteractions();

            httpEventAdapter.disconnect();
            httpEventAdapter.destroy();
        }
    }

    /**
     * Test publish with no auth type (null) - should fall back to default basic auth.
     * Verifies that the adapter attempts BASIC authentication when no auth type is specified.
     */
    @Test
    public void testPublishWithNoAuthType() throws OutputEventAdapterException {

        logger.info("Test case for publishing with no auth type (null).");
        SecretManager secretManager = new SecretManagerImpl();
        OutputEventAdapterServiceValueHolder.setSecretManager(secretManager);

        try (MockedStatic<EventAdapterSecretProcessor> mockedSecretProcessor =
                     mockStatic(EventAdapterSecretProcessor.class)) {
            // Simulate SecretManagementException to fall back to plain text
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor.decryptCredential(anyString(), anyString(), anyString()))
                    .thenThrow(new SecretManagementException("Secret not found"));

            // Create adapter without auth type
            HTTPEventAdapter httpEventAdapter = getHttpAdaptor(); // Uses existing method which doesn't set authType
            httpEventAdapter.init();
            httpEventAdapter.connect();

            // Publish should not throw any exception
            httpEventAdapter.publish("{\"message\": \"test\"}", getDefaultDynamicProperties());

            // Verify that decryptCredential was called for BASIC auth (default fallback)
            mockedSecretProcessor.verify(() ->
                    EventAdapterSecretProcessor.decryptCredential(eq("EMAIL_PROVIDER"), eq("BASIC"), eq("userName")),
                    asyncVerification());

            httpEventAdapter.disconnect();
            httpEventAdapter.destroy();
        }
    }

    /**
     * Test publish with empty headers.
     * Verifies that publish works correctly when headers are empty string.
     */
    @Test
    public void testPublishWithEmptyHeaders() throws OutputEventAdapterException {

        logger.info("Test case for publishing with empty headers.");
        SecretManager secretManager = new SecretManagerImpl();
        OutputEventAdapterServiceValueHolder.setSecretManager(secretManager);

        try (MockedStatic<EventAdapterSecretProcessor> mockedSecretProcessor =
                     mockStatic(EventAdapterSecretProcessor.class)) {
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor.decryptCredential(anyString(), anyString(), anyString()))
                    .thenThrow(new SecretManagementException("Secret not found"));

            HTTPEventAdapter httpEventAdapter = getHttpAdaptorWithAuthType("BASIC");
            httpEventAdapter.init();
            httpEventAdapter.connect();

            Map<String, String> dynamicProperties = new HashMap<>();
            dynamicProperties.put("http.url", "http://localhost:8080/service");
            dynamicProperties.put("http.headers", "");
            dynamicProperties.put("http.username", "user123");
            dynamicProperties.put("http.password", "pwd123");

            // Publish should not throw any exception even with empty headers
            httpEventAdapter.publish("{\"message\": \"test\"}", dynamicProperties);

            // Verify that decryptCredential was called
            mockedSecretProcessor.verify(() ->
                    EventAdapterSecretProcessor.decryptCredential(eq("EMAIL_PROVIDER"), eq("BASIC"), eq("userName")),
                    asyncVerification());

            httpEventAdapter.disconnect();
            httpEventAdapter.destroy();
        }
    }

    /**
     * Test publish with multiple headers.
     * Verifies that publish correctly handles multiple comma-separated headers.
     */
    @Test
    public void testPublishWithMultipleHeaders() throws OutputEventAdapterException {

        logger.info("Test case for publishing with multiple headers.");
        SecretManager secretManager = new SecretManagerImpl();
        OutputEventAdapterServiceValueHolder.setSecretManager(secretManager);

        try (MockedStatic<EventAdapterSecretProcessor> mockedSecretProcessor =
                     mockStatic(EventAdapterSecretProcessor.class)) {
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor.decryptCredential(anyString(), anyString(), anyString()))
                    .thenThrow(new SecretManagementException("Secret not found"));

            HTTPEventAdapter httpEventAdapter = getHttpAdaptorWithAuthType("BASIC");
            httpEventAdapter.init();
            httpEventAdapter.connect();

            Map<String, String> dynamicProperties = new HashMap<>();
            dynamicProperties.put("http.url", "http://localhost:8080/service");
            dynamicProperties.put("http.headers", "Content-Type:application/json,X-Custom-Header:custom-value,Accept:application/json");
            dynamicProperties.put("http.username", "user123");
            dynamicProperties.put("http.password", "pwd123");

            // Publish should not throw any exception with multiple headers
            httpEventAdapter.publish("{\"message\": \"test\"}", dynamicProperties);

            // Verify that decryptCredential was called
            mockedSecretProcessor.verify(() ->
                    EventAdapterSecretProcessor.decryptCredential(eq("EMAIL_PROVIDER"), eq("BASIC"), eq("userName")),
                    asyncVerification());

            httpEventAdapter.disconnect();
            httpEventAdapter.destroy();
        }
    }

    /**
     * Test publish with PUT HTTP method.
     * Verifies that publish correctly handles HTTP PUT method.
     */
    @Test
    public void testPublishWithPutMethod() throws OutputEventAdapterException {

        logger.info("Test case for publishing with PUT HTTP method.");
        SecretManager secretManager = new SecretManagerImpl();
        OutputEventAdapterServiceValueHolder.setSecretManager(secretManager);

        try (MockedStatic<EventAdapterSecretProcessor> mockedSecretProcessor =
                     mockStatic(EventAdapterSecretProcessor.class)) {
            mockedSecretProcessor.when(() -> EventAdapterSecretProcessor.decryptCredential(anyString(), anyString(), anyString()))
                    .thenThrow(new SecretManagementException("Secret not found"));

            setupCarbonConfig();
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            privilegedCarbonContext.setTenantId(-1234);
            OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
            eventAdapterConfiguration.setName("TestHttpAdaptor");
            eventAdapterConfiguration.setType("http");
            eventAdapterConfiguration.setMessageFormat("json");
            Map<String, String> staticProperties = new HashMap<>();
            staticProperties.put("http.client.method", "HttpPut"); // PUT method
            staticProperties.put("http.authType", "BASIC");
            staticProperties.put("http.username", "testUser");
            staticProperties.put("http.password", "testPassword");
            eventAdapterConfiguration.setStaticProperties(staticProperties);
            Map<String, String> globalProperties = new HashMap<>();
            globalProperties.put("jobQueueSize", "10000");
            globalProperties.put("keepAliveTimeInMillis", "20000");
            globalProperties.put("maxThread", "100");
            globalProperties.put("minThread", "8");
            globalProperties.put("defaultMaxConnectionsPerHost", "50");
            globalProperties.put("maxTotalConnections", "1000");
            HTTPEventAdapter httpEventAdapter = new HTTPEventAdapter(eventAdapterConfiguration, globalProperties);

            httpEventAdapter.init();
            httpEventAdapter.connect();

            // Publish with PUT method should not throw any exception
            httpEventAdapter.publish("{\"message\": \"test\"}", getDefaultDynamicProperties());

            // Verify that decryptCredential was called
            mockedSecretProcessor.verify(() ->
                    EventAdapterSecretProcessor.decryptCredential(eq("EMAIL_PROVIDER"), eq("BASIC"), eq("userName")),
                    asyncVerification());

            httpEventAdapter.disconnect();
            httpEventAdapter.destroy();
        }
    }

    /**
     * Helper method to create an HTTP adapter with a specific auth type.
     */
    private HTTPEventAdapter getHttpAdaptorWithAuthType(String authType) {

        setupCarbonConfig();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1234);
        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("TestHttpAdaptor");
        eventAdapterConfiguration.setType("http");
        eventAdapterConfiguration.setMessageFormat("json");
        Map<String, String> staticProperties = new HashMap<>();
        staticProperties.put("http.client.method", "HttpPost");
        staticProperties.put("http.proxy.port", "9090");
        staticProperties.put("http.proxy.host", "localhost");
        staticProperties.put("http.authType", authType);
        // Add properties for BASIC auth (using correct keys from HTTPEventAdapterConstants)
        staticProperties.put("http.username", "testUser");
        staticProperties.put("http.password", "testPassword");
        // Add properties for CLIENT_CREDENTIAL (using correct keys)
        staticProperties.put("http.clientId", "testClientId");
        staticProperties.put("http.clientSecret", "testClientSecret");
        staticProperties.put("http.tokenEndpoint", "https://localhost:9443/oauth2/token");
        staticProperties.put("http.scopes", "openid profile");
        // Add properties for BEARER (using correct key)
        staticProperties.put("http.accessToken", "testAccessToken");
        // Add properties for API_KEY (using correct keys)
        staticProperties.put("http.apiKeyHeader", "X-API-Key");
        staticProperties.put("http.apiKeyValue", "testApiKeyValue");
        eventAdapterConfiguration.setStaticProperties(staticProperties);
        Map<String, String> globalProperties = new HashMap<>();
        globalProperties.put("jobQueueSize", "10000");
        globalProperties.put("keepAliveTimeInMillis", "20000");
        globalProperties.put("maxThread", "100");
        globalProperties.put("minThread", "8");
        globalProperties.put("defaultMaxConnectionsPerHost", "50");
        globalProperties.put("maxTotalConnections", "1000");
        return new HTTPEventAdapter(eventAdapterConfiguration, globalProperties);
    }

    private Map<String, String> getDefaultDynamicProperties() {

        Map<String, String> dynamicProperties = new HashMap<>();
        dynamicProperties.put("http.url", "http://localhost:8080/service");
        dynamicProperties.put("http.headers", "Content-Type:application/json");
        dynamicProperties.put("http.username", "user123");
        dynamicProperties.put("http.password", "pwd123");
        return dynamicProperties;
    }
}
