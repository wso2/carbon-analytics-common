package org.wso2.carbon.event.template.manager.core.internal.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.event.template.manager.core.TemplateDeployer;
import org.wso2.carbon.event.template.manager.core.TemplateDeploymentException;
import org.wso2.carbon.event.template.manager.core.exception.TemplateManagerException;
import org.wso2.carbon.event.template.manager.core.internal.ds.TemplateManagerValueHolder;
import org.wso2.carbon.event.template.manager.core.structure.configuration.AttributeMapping;
import org.wso2.carbon.event.template.manager.core.structure.configuration.AttributeMappings;
import org.wso2.carbon.event.template.manager.core.structure.configuration.ScenarioConfiguration;
import org.wso2.carbon.event.template.manager.core.structure.configuration.StreamMapping;
import org.wso2.carbon.event.template.manager.core.structure.domain.Artifact;
import org.wso2.carbon.event.template.manager.core.structure.domain.CommonArtifacts;
import org.wso2.carbon.event.template.manager.core.structure.domain.Domain;
import org.wso2.carbon.event.template.manager.core.structure.domain.Scenario;
import org.wso2.carbon.event.template.manager.core.structure.domain.Scenarios;
import org.wso2.carbon.event.template.manager.core.structure.domain.Script;
import org.wso2.carbon.event.template.manager.core.structure.domain.Scripts;
import org.wso2.carbon.event.template.manager.core.structure.domain.StreamMappings;
import org.wso2.carbon.event.template.manager.core.structure.domain.Template;
import org.wso2.carbon.event.template.manager.core.structure.domain.Templates;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.script.ScriptEngine;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link TemplateManagerHelper} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class TemplateManagerHelperTest {

    @Captor
    private ArgumentCaptor<String> stringArgument;

    @Mock
    private UserRegistry registry;

    @Mock
    private RegistryService registryService;

    @Before
    public void init() {
        // No need for MockitoAnnotations.initMocks(this) when using MockitoJUnitRunner
        System.setProperty("carbon.home", "");
    }

    @Test
    public void testLoadDomains() {
        // Testing static method directly
        Assert.assertEquals("Domains should be empty as " + TemplateManagerConstants.TEMPLATE_DOMAIN_PATH +
                " directory doesn't exists", 0, TemplateManagerHelper.loadDomains().size());
    }

    @Test
    public void testDeployArtifacts() throws Exception {
        final Domain domain = new Domain();
        Artifact artifact = new Artifact();
        artifact.setType("artifact-type");
        CommonArtifacts commonArtifacts = new CommonArtifacts();
        commonArtifacts.setArtifact(Collections.singletonList(artifact));
        domain.setCommonArtifacts(commonArtifacts);

        Template template = mock(Template.class);
        when(template.getType()).thenReturn("template-type");
        when(template.getValue()).thenReturn("template-value");

        Templates templates = new Templates();
        templates.setTemplate(Collections.singletonList(template));
        Scenario scenario = new Scenario();
        scenario.setType("scenario-type");
        scenario.setTemplates(templates);
        Scenarios scenarios = new Scenarios();
        scenarios.setScenario(Collections.singletonList(scenario));
        domain.setScenarios(scenarios);

        final ScenarioConfiguration configuration = new ScenarioConfiguration();
        configuration.setScenario(scenario.getType());
        final ScriptEngine scriptEngine = TemplateManagerHelper.createJavaScriptEngine(null);

        // Test missing artifact deployer
        try {
            TemplateManagerHelper.deployArtifacts(configuration, domain, scriptEngine);
            Assert.fail("Artifacts deployed without a template deployer for type '" + artifact.getType() + "'");
        } catch (TemplateDeploymentException e) {
            // expected
        }

        TemplateDeployer artifactDeployer = mock(TemplateDeployer.class);
        ConcurrentHashMap<String, TemplateDeployer> deployers = TemplateManagerValueHolder.getTemplateDeployers();
        deployers.put(artifact.getType(), artifactDeployer);

        // Test missing template deployer
        try {
            TemplateManagerHelper.deployArtifacts(configuration, domain, scriptEngine);
            Assert.fail("Artifacts deployed without a template deployer for type '" + template.getType() + "'");
        } catch (TemplateDeploymentException e) {
            // expected
        }

        TemplateDeployer templateDeployer = mock(TemplateDeployer.class);
        deployers.put(template.getType(), templateDeployer);
        TemplateManagerHelper.deployArtifacts(configuration, domain, scriptEngine);
    }

    @Test
    @Ignore("Skipping because the nashorn is not available in latet jdk")
    public void testCreateJavaScriptEngine() throws Exception {
        final Domain domain = new Domain();
        Script script = new Script();
        Scripts scripts = new Scripts();
        scripts.setScript(Collections.singletonList(script));
        domain.setScripts(scripts);

        script.setSrc("script.js");
        script.setContent(null);
        ScriptEngine scriptEngine = TemplateManagerHelper.createJavaScriptEngine(domain);
        Assert.assertNotNull("JavaScript engine cannot be null", scriptEngine);

        script.setSrc(null);
        script.setContent("var a = 0;");
        scriptEngine = TemplateManagerHelper.createJavaScriptEngine(domain);
        Assert.assertNotNull("JavaScript engine cannot be null", scriptEngine);
    }

    @Test(expected = TemplateManagerException.class)
    public void testValidateTemplateDomainConfigNullName() throws TemplateManagerException {
        Domain domain = new Domain();
        domain.setName(null);
        TemplateManagerHelper.validateTemplateDomainConfig(domain);
    }

    @Test
    public void testGenerateExecutionPlan() throws Exception {
        StreamDefinition streamDefinition = mock(StreamDefinition.class);
        when(streamDefinition.getMetaData()).thenReturn(Collections.emptyList());
        when(streamDefinition.getCorrelationData()).thenReturn(Collections.emptyList());
        when(streamDefinition.getPayloadData()).thenReturn(Collections.emptyList());

        EventStreamService eventStreamService = mock(EventStreamService.class);
        when(eventStreamService.getStreamDefinition(anyString())).thenReturn(streamDefinition);
        TemplateManagerValueHolder.setEventStreamService(eventStreamService);

        AttributeMapping attributeMapping = new AttributeMapping();
        attributeMapping.setFrom("FOO");
        attributeMapping.setTo("BAR");
        AttributeMappings attributeMappings = new AttributeMappings();
        attributeMappings.setAttributeMapping(Collections.singletonList(attributeMapping));

        StreamMapping streamMapping = new StreamMapping();
        streamMapping.setFrom("foo");
        streamMapping.setTo("bar");
        streamMapping.setAttributeMappings(attributeMappings);

        final List<StreamMapping> streamMappingList = Collections.singletonList(streamMapping);
        final String planName = "plan-name";

        String result = TemplateManagerHelper.generateExecutionPlan(streamMappingList, planName);
        Assert.assertTrue(result.contains("@Plan:name('plan-name')"));
        Assert.assertTrue(result.contains("from foo"));
    }

    @Test
    public void testDeleteConfigWithoutUndeploy() throws Exception {
        final String domainName = "test-domainName";
        final String configName = "test-configName";

        when(registryService.getConfigSystemRegistry(anyInt())).thenReturn(registry);
        TemplateManagerValueHolder.setRegistryService(registryService);

        TemplateManagerHelper.deleteConfigWithoutUndeploy(domainName, configName);

        verify(registry).delete(stringArgument.capture());
        Assert.assertTrue(stringArgument.getValue().contains(domainName));
        Assert.assertTrue(stringArgument.getValue().contains(configName));
    }
}