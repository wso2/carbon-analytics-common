/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.template.manager.core.internal.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;
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
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.script.ScriptEngine;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Test cases for {@link TemplateManagerHelper} class.
 */
@RunWith(PowerMockRunner.class)
public class TemplateManagerHelperTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();
    @Captor
    private ArgumentCaptor<String> stringArgument;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        System.setProperty("carbon.home", "");
    }

    @Test
    public void testLoadDomains() {
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

        try {
            TemplateManagerHelper.deployArtifacts(configuration, domain, scriptEngine);
            Assert.fail("Artifacts deployed without a template deployer for type '" + artifact.getType() + "'");
        } catch (TemplateDeploymentException e) {
            // fine
        }

        TemplateDeployer artifactDeployer = mock(TemplateDeployer.class);
        ConcurrentHashMap<String, TemplateDeployer> deployers = TemplateManagerValueHolder.getTemplateDeployers();
        deployers.put(artifact.getType(), artifactDeployer);
        try {
            TemplateManagerHelper.deployArtifacts(configuration, domain, scriptEngine);
            Assert.fail("Artifacts deployed without a template deployer for type '" + template.getType() + "'");
        } catch (TemplateDeploymentException e) {
            // fine
        }

        TemplateDeployer templateDeployer = mock(TemplateDeployer.class);
        deployers.put(template.getType(), templateDeployer);
        TemplateManagerHelper.deployArtifacts(configuration, domain, scriptEngine);
    }

    @Test
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

    @Test
    public void testValidateTemplateDomainConfig() {
        Domain domain = new Domain();

        try {
            domain.setName(null);
            TemplateManagerHelper.validateTemplateDomainConfig(domain);
            Assert.fail("Domain name cannot be null.");
        } catch (TemplateManagerException e) {
            // fine
        }

        try {
            domain.setName("");
            domain.setScenarios(null);
            TemplateManagerHelper.validateTemplateDomainConfig(domain);
            Assert.fail("Domain scenarios cannot be null.");
        } catch (TemplateManagerException e) {
            // fine
        }

        try {
            domain.setName("");
            Scenarios scenarios = new Scenarios();
            scenarios.setScenario(Collections.<Scenario>emptyList());
            domain.setScenarios(scenarios);
            TemplateManagerHelper.validateTemplateDomainConfig(domain);
            Assert.fail("Domain scenarios cannot be empty.");
        } catch (TemplateManagerException e) {
            // fine
        }
    }

    @Test
    public void testGetStreamIDsToBeMapped() throws Exception {
        final Domain domain = new Domain();
        Scenario scenario = new Scenario();
        scenario.setType("scenario-type");
        scenario.setStreamMappings(null);
        Scenarios scenarios = new Scenarios();
        scenarios.setScenario(Collections.singletonList(scenario));
        domain.setScenarios(scenarios);

        final ScenarioConfiguration configuration = new ScenarioConfiguration();
        configuration.setScenario(scenario.getType());
        configuration.setParameterMap(Collections.singletonMap("key", "value"));

        final ScriptEngine scriptEngine = TemplateManagerHelper.createJavaScriptEngine(null);

        List<String> streamIds = TemplateManagerHelper.getStreamIDsToBeMapped(configuration, domain, scriptEngine);
        Assert.assertNull("When scenario stream mappings is null, stream IDs should be null", streamIds);

        org.wso2.carbon.event.template.manager.core.structure.domain.StreamMapping
                streamMapping = new org.wso2.carbon.event.template.manager.core.structure.domain.StreamMapping();
        streamMapping.setTo("foo");
        StreamMappings streamMappings = new StreamMappings();
        streamMappings.setStreamMapping(Collections.singletonList(streamMapping));
        scenario.setStreamMappings(streamMappings);
        streamIds = TemplateManagerHelper.getStreamIDsToBeMapped(configuration, domain, scriptEngine);
        Assert.assertNotNull("When scenario stream mappings is present, stream IDs cannot be null", streamIds);
    }

    @Test
    public void testGenerateExecutionPlan() throws Exception {
        StreamDefinition streamDefinition = mock(StreamDefinition.class);
        when(streamDefinition.getMetaData()).thenReturn(Collections.<Attribute>emptyList());
        when(streamDefinition.getCorrelationData()).thenReturn(Collections.<Attribute>emptyList());
        when(streamDefinition.getPayloadData()).thenReturn(Collections.<Attribute>emptyList());
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
        final String executionPlan = "@Plan:name('" + planName + "') \n" +
                                     "\n" +
                                     "@IMPORT('" + streamMapping.getFrom() + "')\n" +
                                     "define stream " + streamMapping.getFrom() + "();\n" +
                                     "\n" +
                                     "@EXPORT('" + streamMapping.getTo() + "')\n" +
                                     "define stream " + streamMapping.getTo() + "();\n" +
                                     "\n" +
                                     "from " + streamMapping.getFrom() + " \n" +
                                     "select " + attributeMapping.getFrom() + " as " + attributeMapping.getTo() +
                                     " \n" +
                                     "insert into " + streamMapping.getTo() + ";\n" +
                                     "\n";
        Assert.assertEquals("Incorrect execution plan", executionPlan,
                            TemplateManagerHelper.generateExecutionPlan(streamMappingList, planName));
    }

    @Test
    public void testDeleteConfigWithoutUndeploy() throws Exception {
        final String domainName = "test-domainName";
        final String configName = "test-configName";
        final String registryPath = TemplateManagerConstants.TEMPLATE_CONFIG_PATH + RegistryConstants.PATH_SEPARATOR +
                                    domainName + RegistryConstants.PATH_SEPARATOR + configName +
                                    TemplateManagerConstants.CONFIG_FILE_EXTENSION;

        UserRegistry registry = mock(UserRegistry.class);
        RegistryService registryService = mock(RegistryService.class);
        when(registryService.getConfigSystemRegistry(anyInt())).thenReturn(registry);
        TemplateManagerValueHolder.setRegistryService(registryService);

        TemplateManagerHelper.deleteConfigWithoutUndeploy(domainName, configName);
        verify(registry).delete(stringArgument.capture());
        Assert.assertEquals("Incorrect registry path for deletion", registryPath, stringArgument.getValue());
    }

    @Test
    public void testGetConfiguration() throws Exception {
        final String resourcePath = "/path/to/resource";

        UserRegistry registry = mock(UserRegistry.class);
        when(registry.resourceExists(resourcePath)).thenReturn(false);
        RegistryService registryService = mock(RegistryService.class);
        when(registryService.getConfigSystemRegistry(anyInt())).thenReturn(registry);
        TemplateManagerValueHolder.setRegistryService(registryService);

        ScenarioConfiguration configuration = TemplateManagerHelper.getConfiguration(resourcePath);
        Assert.assertNull("When resource doesn't exists, scenario configuration must be null", configuration);

        when(registry.get(resourcePath)).thenReturn(null);
        configuration = TemplateManagerHelper.getConfiguration(resourcePath);
        Assert.assertNull("When registry.get(path) returns null scenario configuration must be null", configuration);
    }

    @Test
    public void testGetTemplatedArtifactId() {
        final String domainName = "test-domainName";
        final String scenarioName = "test-scenarioName";
        final String scenarioConfigName = "test-scenarioConfigName";
        final String artifactType = "test-artifactType";
        final int sequenceNumber = 0;
        final String templateArtifactId = domainName + TemplateManagerConstants.CONFIG_NAME_SEPARATOR + scenarioName +
                                          TemplateManagerConstants.CONFIG_NAME_SEPARATOR + scenarioConfigName +
                                          TemplateManagerConstants.CONFIG_NAME_SEPARATOR + artifactType +
                                          sequenceNumber;

        Assert.assertEquals("Incorrect template artifact ID", templateArtifactId,
                            TemplateManagerHelper.getTemplatedArtifactId(domainName, scenarioName, scenarioConfigName,
                                                                         artifactType, sequenceNumber));
    }

    @Test
    public void testGetStreamMappingPlanId() {
        final String domainName = "test-domainName";
        final String scenarioConfigName = "test-scenarioConfigName";
        final String streamMappingPlanId =
                domainName + TemplateManagerConstants.CONFIG_NAME_SEPARATOR + scenarioConfigName +
                TemplateManagerConstants.CONFIG_NAME_SEPARATOR + TemplateManagerConstants.STREAM_MAPPING_PLAN_SUFFIX;

        Assert.assertEquals("Incorrect stream mapping plan ID", streamMappingPlanId,
                            TemplateManagerHelper.getStreamMappingPlanId(domainName, scenarioConfigName));
    }

    @Test
    public void testGetCommonArtifactId() {
        final String domainName = "test-domainName";
        final String artifactType = "test-artifactType";
        final int sequenceNumber = 0;
        final String commonArtifactId =
                domainName + TemplateManagerConstants.CONFIG_NAME_SEPARATOR + artifactType + sequenceNumber;

        Assert.assertEquals("Incorrect stream mapping plan ID", commonArtifactId,
                            TemplateManagerHelper.getCommonArtifactId(domainName, artifactType, sequenceNumber));
    }
}
