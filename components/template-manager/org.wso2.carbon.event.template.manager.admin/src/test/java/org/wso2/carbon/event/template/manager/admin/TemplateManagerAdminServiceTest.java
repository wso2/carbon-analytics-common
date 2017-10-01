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

package org.wso2.carbon.event.template.manager.admin;

import org.apache.axis2.AxisFault;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.event.template.manager.admin.dto.configuration.ConfigurationParameterDTO;
import org.wso2.carbon.event.template.manager.admin.dto.configuration.ScenarioConfigurationDTO;
import org.wso2.carbon.event.template.manager.admin.dto.configuration.ScenarioConfigurationInfoDTO;
import org.wso2.carbon.event.template.manager.admin.dto.configuration.StreamMappingDTO;
import org.wso2.carbon.event.template.manager.admin.dto.domain.DomainInfoDTO;
import org.wso2.carbon.event.template.manager.admin.dto.domain.DomainParameterDTO;
import org.wso2.carbon.event.template.manager.admin.dto.domain.ScenarioInfoDTO;
import org.wso2.carbon.event.template.manager.admin.internal.ds.TemplateManagerAdminServiceValueHolder;
import org.wso2.carbon.event.template.manager.core.TemplateManagerService;
import org.wso2.carbon.event.template.manager.core.exception.TemplateManagerException;
import org.wso2.carbon.event.template.manager.core.structure.configuration.AttributeMapping;
import org.wso2.carbon.event.template.manager.core.structure.configuration.AttributeMappings;
import org.wso2.carbon.event.template.manager.core.structure.configuration.ScenarioConfiguration;
import org.wso2.carbon.event.template.manager.core.structure.configuration.StreamMapping;
import org.wso2.carbon.event.template.manager.core.structure.configuration.StreamMappings;
import org.wso2.carbon.event.template.manager.core.structure.domain.Domain;
import org.wso2.carbon.event.template.manager.core.structure.domain.Parameter;
import org.wso2.carbon.event.template.manager.core.structure.domain.Parameters;
import org.wso2.carbon.event.template.manager.core.structure.domain.Scenario;
import org.wso2.carbon.event.template.manager.core.structure.domain.Scenarios;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class TemplateManagerAdminServiceTest {

    @Test
    public void testGetDomainInfo() throws Exception {
        Parameter parameter = new Parameter();
        parameter.setName("parameter-name");
        parameter.setType("parameter-type");
        parameter.setDefaultValue("parameter-default-value");
        parameter.setDescription("parameter-description");
        parameter.setDisplayName("parameter-display-name");
        parameter.setOptions("parameter-options");
        Parameters parameters = new Parameters();
        parameters.setParameter(Collections.singletonList(parameter));
        Scenario scenario = new Scenario();
        scenario.setType("scenario-type");
        scenario.setDescription("scenario-description");
        scenario.setParameters(parameters);
        Scenarios scenarios = new Scenarios();
        scenarios.setScenario(Collections.singletonList(scenario));
        final Domain domain = new Domain();
        domain.setName("domain-name");
        domain.setDescription("domain-description");
        domain.setScenarios(scenarios);

        TemplateManagerService templateManagerService = mock(TemplateManagerService.class);
        when(templateManagerService.getDomain(anyString())).thenReturn(domain);
        TemplateManagerAdminServiceValueHolder.setTemplateManagerService(templateManagerService);

        DomainInfoDTO domainInfo = new TemplateManagerAdminService().getDomainInfo("domain-name");

        Assert.assertEquals("Incorrect name", domain.getName(), domainInfo.getName());
        Assert.assertEquals("Incorrect description", domain.getDescription(), domainInfo.getDescription());
        Assert.assertEquals("Incorrect scenarios", domain.getScenarios().getScenario().size(),
                            domainInfo.getScenarioInfoDTOs().length);

        ScenarioInfoDTO scenarioInfoDTO = domainInfo.getScenarioInfoDTOs()[0];
        Assert.assertEquals("Incorrect scenario type", scenario.getType(), scenarioInfoDTO.getType());
        Assert.assertEquals("Incorrect scenario description", scenario.getDescription(),
                            scenarioInfoDTO.getDescription());
        Assert.assertEquals("Incorrect scenario parameters", scenario.getParameters().getParameter().size(),
                            scenarioInfoDTO.getDomainParameterDTOs().length);

        DomainParameterDTO domainParameterDTO = scenarioInfoDTO.getDomainParameterDTOs()[0];
        Assert.assertEquals("Incorrect parameter name", parameter.getName(), domainParameterDTO.getName());
        Assert.assertEquals("Incorrect parameter type", parameter.getType(), domainParameterDTO.getType());
        Assert.assertEquals("Incorrect parameter default value", parameter.getDefaultValue(),
                            domainParameterDTO.getDefaultValue());
        Assert.assertEquals("Incorrect parameter description", parameter.getDescription(),
                            domainParameterDTO.getDescription());
        Assert.assertEquals("Incorrect parameter display name", parameter.getDisplayName(),
                            domainParameterDTO.getDisplayName());
        Assert.assertEquals("Incorrect parameter options", parameter.getOptions(), domainParameterDTO.getOptions());
    }

    @Test(expected = AxisFault.class)
    public void testGetDomainInfoThrowsException() throws Exception {
        TemplateManagerService templateManagerService = mock(TemplateManagerService.class);
        when(templateManagerService.getDomain(anyString())).thenThrow(new RuntimeException("some exception"));
        TemplateManagerAdminServiceValueHolder.setTemplateManagerService(templateManagerService);

        new TemplateManagerAdminService().getDomainInfo(null);
    }

    @Test
    public void testGetAllDomainInfos() throws Exception {
        Parameter parameter = new Parameter();
        parameter.setName("parameter-name");
        parameter.setType("parameter-type");
        parameter.setDefaultValue("parameter-default-value");
        parameter.setDescription("parameter-description");
        parameter.setDisplayName("parameter-display-name");
        parameter.setOptions("parameter-options");
        Parameters parameters = new Parameters();
        parameters.setParameter(Collections.singletonList(parameter));
        Scenario scenario = new Scenario();
        scenario.setType("scenario-type");
        scenario.setDescription("scenario-description");
        scenario.setParameters(parameters);
        Scenarios scenarios = new Scenarios();
        scenarios.setScenario(Collections.singletonList(scenario));
        Domain domain = new Domain();
        domain.setName("domain-name");
        domain.setDescription("domain-description");
        domain.setScenarios(scenarios);
        final Set<Domain> domains = Collections.singleton(domain);

        TemplateManagerService templateManagerService = mock(TemplateManagerService.class);
        when(templateManagerService.getAllDomains()).thenReturn(domains);
        TemplateManagerAdminServiceValueHolder.setTemplateManagerService(templateManagerService);

        DomainInfoDTO[] domainInfos = new TemplateManagerAdminService().getAllDomainInfos();
        Assert.assertEquals("Incorrect number of domains", domains.size(), domainInfos.length);
    }

    @Test(expected = AxisFault.class)
    public void testGetAllDomainInfosThrowsException() throws Exception {
        TemplateManagerService templateManagerService = mock(TemplateManagerService.class);
        when(templateManagerService.getAllDomains()).thenThrow(new RuntimeException("some exception"));
        TemplateManagerAdminServiceValueHolder.setTemplateManagerService(templateManagerService);

        new TemplateManagerAdminService().getAllDomainInfos();
    }

    @Test
    public void testGetConfiguration() throws Exception {
        AttributeMapping attributeMapping = new AttributeMapping();
        attributeMapping.setFrom("attributeMapping-from");
        attributeMapping.setTo("attributeMapping-to");
        AttributeMappings attributeMappings = new AttributeMappings();
        attributeMappings.setAttributeMapping(Collections.singletonList(attributeMapping));
        StreamMapping streamMapping = new StreamMapping();
        streamMapping.setFrom("streamMapping-from");
        streamMapping.setTo("streamMapping-to");
        streamMapping.setAttributeMappings(attributeMappings);
        StreamMappings streamMappings = new StreamMappings();
        streamMappings.setStreamMapping(Collections.singletonList(streamMapping));
        final ScenarioConfiguration configuration = new ScenarioConfiguration();
        configuration.setName("configuration-name");
        configuration.setScenario("configuration-scenario");
        configuration.setDescription("configuration-description");
        configuration.setDomain("configuration-domain");
        configuration.setParameterMap(Collections.singletonMap("parameter-key", "parameter-value"));
        configuration.setStreamMappings(streamMappings);

        TemplateManagerService templateManagerService = mock(TemplateManagerService.class);
        when(templateManagerService.getConfiguration(anyString(), anyString())).thenReturn(configuration);
        TemplateManagerAdminServiceValueHolder.setTemplateManagerService(templateManagerService);

        ScenarioConfigurationDTO configurationDTO = new TemplateManagerAdminService().getConfiguration("domain-name",
                                                                                                       "configuration-name");
        Assert.assertEquals("Incorrect name", configuration.getName(), configurationDTO.getName());
        Assert.assertEquals("Incorrect type", configuration.getScenario(), configurationDTO.getType());
        Assert.assertEquals("Incorrect description", configuration.getDescription(), configurationDTO.getDescription());
        Assert.assertEquals("Incorrect domain", configuration.getDomain(), configurationDTO.getDomain());
        Assert.assertEquals("Incorrect parameters", configuration.getParameterMap().size(),
                            configurationDTO.getConfigurationParameterDTOs().length);
        Assert.assertEquals("Incorrect stream mappings", configuration.getStreamMappings().getStreamMapping().size(),
                            configurationDTO.getStreamMappingDTOs().length);
    }

    @Test(expected = AxisFault.class)
    public void testGetConfigurationThrowsException() throws Exception {
        TemplateManagerService templateManagerService = mock(TemplateManagerService.class);
        when(templateManagerService.getConfiguration(anyString(), anyString()))
                .thenThrow(new TemplateManagerException("some exception"));
        TemplateManagerAdminServiceValueHolder.setTemplateManagerService(templateManagerService);

        new TemplateManagerAdminService().getConfiguration(null, null);
    }

    @Test
    public void testGetConfigurationInfos() throws Exception {
        AttributeMapping attributeMapping = new AttributeMapping();
        attributeMapping.setFrom("attributeMapping-from");
        attributeMapping.setTo("attributeMapping-to");
        AttributeMappings attributeMappings = new AttributeMappings();
        attributeMappings.setAttributeMapping(Collections.singletonList(attributeMapping));
        StreamMapping streamMapping = new StreamMapping();
        streamMapping.setFrom("streamMapping-from");
        streamMapping.setTo("streamMapping-to");
        streamMapping.setAttributeMappings(attributeMappings);
        StreamMappings streamMappings = new StreamMappings();
        streamMappings.setStreamMapping(Collections.singletonList(streamMapping));
        ScenarioConfiguration configuration = new ScenarioConfiguration();
        configuration.setName("configuration-name");
        configuration.setScenario("configuration-scenario");
        configuration.setDescription("configuration-description");
        configuration.setDomain("configuration-domain");
        configuration.setParameterMap(Collections.singletonMap("parameter-key", "parameter-value"));
        configuration.setStreamMappings(streamMappings);
        List<ScenarioConfiguration> configurations = Collections.singletonList(configuration);

        TemplateManagerService templateManagerService = mock(TemplateManagerService.class);
        when(templateManagerService.getConfigurations(anyString())).thenReturn(configurations);
        TemplateManagerAdminServiceValueHolder.setTemplateManagerService(templateManagerService);

        ScenarioConfigurationInfoDTO[] configurationInfos = new TemplateManagerAdminService()
                .getConfigurationInfos("domain-name");
        Assert.assertEquals("Incorrect number of configurations", configurations.size(), configurationInfos.length);
    }

    @Test(expected = AxisFault.class)
    public void testGetConfigurationInfosThrowsException() throws Exception {
        TemplateManagerService templateManagerService = mock(TemplateManagerService.class);
        when(templateManagerService.getConfigurations(anyString()))
                .thenThrow(new TemplateManagerException("some exception"));
        TemplateManagerAdminServiceValueHolder.setTemplateManagerService(templateManagerService);

        new TemplateManagerAdminService().getConfigurationInfos(null);
    }

    @Test
    public void testDeleteConfiguration() throws Exception {
        TemplateManagerService templateManagerService = mock(TemplateManagerService.class);
        TemplateManagerAdminServiceValueHolder.setTemplateManagerService(templateManagerService);
        Assert.assertTrue("Returned false", new TemplateManagerAdminService().deleteConfiguration(null, null));
    }

    @Test
    public void testSaveConfiguration() throws Exception {
        TemplateManagerService templateManagerService = mock(TemplateManagerService.class);
        when(templateManagerService.saveConfiguration(any(ScenarioConfiguration.class))).thenReturn(null);
        TemplateManagerAdminServiceValueHolder.setTemplateManagerService(templateManagerService);

        TemplateManagerAdminService templateManagerAdminService = new TemplateManagerAdminService();

        ScenarioConfigurationDTO configuration = new ScenarioConfigurationDTO();
        ConfigurationParameterDTO configurationParameterDTO = new ConfigurationParameterDTO();
        configuration.setConfigurationParameterDTOs(new ConfigurationParameterDTO[]{configurationParameterDTO});

        Assert.assertNull("Return value should be null when Template Manager Service returns null",
                          templateManagerAdminService.saveConfiguration(configuration));

        final List<String> streamIds = Collections.singletonList("stream-id");
        when(templateManagerService.saveConfiguration(any(ScenarioConfiguration.class))).thenReturn(streamIds);
        Assert.assertEquals("Incorrect number of stream IDs", streamIds.size(),
                            templateManagerAdminService.saveConfiguration(configuration).length);
    }

    @Test
    public void testEditConfiguration() throws Exception {
        TemplateManagerService templateManagerService = mock(TemplateManagerService.class);
        when(templateManagerService.editConfiguration(any(ScenarioConfiguration.class))).thenReturn(null);
        TemplateManagerAdminServiceValueHolder.setTemplateManagerService(templateManagerService);

        TemplateManagerAdminService templateManagerAdminService = new TemplateManagerAdminService();

        Assert.assertNull("Return value should be null when Template Manager Service returns null",
                          templateManagerAdminService.editConfiguration(null));

        final List<String> streamIds = Collections.singletonList("stream-id");
        when(templateManagerService.editConfiguration(any(ScenarioConfiguration.class))).thenReturn(streamIds);
        Assert.assertEquals("Incorrect number of stream IDs", streamIds.size(),
                            templateManagerAdminService.editConfiguration(new ScenarioConfigurationDTO()).length);
    }

    @Test
    public void testSaveStreamMapping() throws Exception {
        TemplateManagerService templateManagerService = mock(TemplateManagerService.class);
        TemplateManagerAdminServiceValueHolder.setTemplateManagerService(templateManagerService);

        boolean saved = new TemplateManagerAdminService().saveStreamMapping(new StreamMappingDTO[0], null, null);
        Assert.assertTrue("Stream mappings didn't saved", saved);
    }
}
