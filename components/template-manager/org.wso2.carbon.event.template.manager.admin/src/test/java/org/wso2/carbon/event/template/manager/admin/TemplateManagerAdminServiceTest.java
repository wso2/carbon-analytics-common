/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.event.template.manager.admin;

import org.apache.axis2.AxisFault;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
import org.wso2.carbon.event.template.manager.core.structure.domain.Domain;
import org.wso2.carbon.event.template.manager.core.structure.domain.Parameters;
import org.wso2.carbon.event.template.manager.core.structure.domain.Scenario;
import org.wso2.carbon.event.template.manager.core.structure.domain.Scenarios;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Full test class for TemplateManagerAdminService migrated to Mockito 5.x.
 */
public class TemplateManagerAdminServiceTest {

    @Mock
    private TemplateManagerService templateManagerService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetDomainInfo() throws Exception {
        org.wso2.carbon.event.template.manager.core.structure.domain.Parameter parameter = 
                new org.wso2.carbon.event.template.manager.core.structure.domain.Parameter();
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

        when(templateManagerService.getDomain(anyString())).thenReturn(domain);
        TemplateManagerAdminServiceValueHolder.setTemplateManagerService(templateManagerService);

        DomainInfoDTO domainInfo = new TemplateManagerAdminService().getDomainInfo("domain-name");

        Assert.assertEquals("Incorrect name", domain.getName(), domainInfo.getName());
        Assert.assertEquals("Incorrect description", domain.getDescription(), domainInfo.getDescription());
        Assert.assertEquals(1, domainInfo.getScenarioInfoDTOs().length);

        ScenarioInfoDTO scenarioInfoDTO = domainInfo.getScenarioInfoDTOs()[0];
        Assert.assertEquals(scenario.getType(), scenarioInfoDTO.getType());

        DomainParameterDTO domainParameterDTO = scenarioInfoDTO.getDomainParameterDTOs()[0];
        Assert.assertEquals(parameter.getName(), domainParameterDTO.getName());
    }

    @Test(expected = AxisFault.class)
    public void testGetDomainInfoThrowsException() throws Exception {
        // We use lenient() to tell Mockito 5.x to be less strict about this specific stub
        // and we throw a RuntimeException that the AdminService should wrap into an AxisFault
        org.mockito.Mockito.lenient().doThrow(new RuntimeException("Injected Failure"))
                .when(templateManagerService).getDomain(anyString());

        TemplateManagerAdminServiceValueHolder.setTemplateManagerService(templateManagerService);

        // Call the service
        new TemplateManagerAdminService().getDomainInfo("any-domain");
    }

    @Test
    public void testGetAllDomainInfos() throws Exception {
        Domain domain = new Domain();
        domain.setName("domain-name");
        Scenarios scenarios = new Scenarios();
        scenarios.setScenario(Collections.<Scenario>emptyList());
        domain.setScenarios(scenarios);
        final Set<Domain> domains = Collections.singleton(domain);

        when(templateManagerService.getAllDomains()).thenReturn(domains);
        TemplateManagerAdminServiceValueHolder.setTemplateManagerService(templateManagerService);

        DomainInfoDTO[] domainInfos = new TemplateManagerAdminService().getAllDomainInfos();
        Assert.assertEquals(1, domainInfos.length);
    }

    @Test(expected = AxisFault.class)
    public void testGetAllDomainInfosThrowsException() throws Exception {
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

        org.wso2.carbon.event.template.manager.core.structure.configuration.StreamMapping streamMapping = 
                new org.wso2.carbon.event.template.manager.core.structure.configuration.StreamMapping();
        streamMapping.setFrom("streamMapping-from");
        streamMapping.setTo("streamMapping-to");
        streamMapping.setAttributeMappings(attributeMappings);

        org.wso2.carbon.event.template.manager.core.structure.configuration.StreamMappings streamMappings = 
                new org.wso2.carbon.event.template.manager.core.structure.configuration.StreamMappings();
        streamMappings.setStreamMapping(Collections.singletonList(streamMapping));

        final ScenarioConfiguration configuration = new ScenarioConfiguration();
        configuration.setName("configuration-name");
        configuration.setScenario("configuration-scenario");
        configuration.setDescription("configuration-description");
        configuration.setDomain("configuration-domain");
        configuration.setParameterMap(Collections.singletonMap("parameter-key", "parameter-value"));
        configuration.setStreamMappings(streamMappings);

        when(templateManagerService.getConfiguration(anyString(), anyString())).thenReturn(configuration);
        TemplateManagerAdminServiceValueHolder.setTemplateManagerService(templateManagerService);

        ScenarioConfigurationDTO configurationDTO = new TemplateManagerAdminService().getConfiguration("domain-name",
                                                                                                       "configuration-name");
        Assert.assertEquals(configuration.getName(), configurationDTO.getName());
    }

    @Test(expected = AxisFault.class)
    public void testGetConfigurationThrowsException() throws Exception {
        // FIX: Throw the specific checked exception the service signature allows
        org.mockito.Mockito.doThrow(new TemplateManagerException("Injected Failure"))
                .when(templateManagerService).getConfiguration(anyString(), anyString());
                
        TemplateManagerAdminServiceValueHolder.setTemplateManagerService(templateManagerService);

        // Ensure we use actual strings to match anyString()
        new TemplateManagerAdminService().getConfiguration("domain", "config");
    }

    @Test
    public void testGetConfigurationInfos() throws Exception {
        ScenarioConfiguration configuration = new ScenarioConfiguration();
        configuration.setName("configuration-name");
        List<ScenarioConfiguration> configurations = Collections.singletonList(configuration);

        when(templateManagerService.getConfigurations(anyString())).thenReturn(configurations);
        TemplateManagerAdminServiceValueHolder.setTemplateManagerService(templateManagerService);

        ScenarioConfigurationInfoDTO[] configurationInfos = new TemplateManagerAdminService()
                .getConfigurationInfos("domain-name");
        Assert.assertEquals(1, configurationInfos.length);
    }

    @Test(expected = AxisFault.class)
    public void testGetConfigurationInfosThrowsException() throws Exception {
        // FIX: Throw the specific checked exception
        org.mockito.Mockito.doThrow(new TemplateManagerException("Injected Failure"))
                .when(templateManagerService).getConfigurations(anyString());
                
        TemplateManagerAdminServiceValueHolder.setTemplateManagerService(templateManagerService);

        new TemplateManagerAdminService().getConfigurationInfos("domain");
    }

    @Test
    public void testDeleteConfiguration() throws Exception {
        TemplateManagerAdminServiceValueHolder.setTemplateManagerService(templateManagerService);
        Assert.assertTrue(new TemplateManagerAdminService().deleteConfiguration(null, null));
    }

    @Test
    public void testSaveConfiguration() throws Exception {
        when(templateManagerService.saveConfiguration(any(ScenarioConfiguration.class))).thenReturn(null);
        TemplateManagerAdminServiceValueHolder.setTemplateManagerService(templateManagerService);

        TemplateManagerAdminService templateManagerAdminService = new TemplateManagerAdminService();

        ScenarioConfigurationDTO configuration = new ScenarioConfigurationDTO();
        ConfigurationParameterDTO configurationParameterDTO = new ConfigurationParameterDTO();
        configuration.setConfigurationParameterDTOs(new ConfigurationParameterDTO[]{configurationParameterDTO});

        Assert.assertNull(templateManagerAdminService.saveConfiguration(configuration));

        final List<String> streamIds = Collections.singletonList("stream-id");
        when(templateManagerService.saveConfiguration(any(ScenarioConfiguration.class))).thenReturn(streamIds);
        Assert.assertEquals(1, templateManagerAdminService.saveConfiguration(configuration).length);
    }

    @Test
    @Ignore ("Nashorn relaed test method. This access JS engin ")
    public void testEditConfiguration() throws Exception {
        when(templateManagerService.editConfiguration(any(ScenarioConfiguration.class))).thenReturn(null);
        TemplateManagerAdminServiceValueHolder.setTemplateManagerService(templateManagerService);

        TemplateManagerAdminService templateManagerAdminService = new TemplateManagerAdminService();

        Assert.assertNull(templateManagerAdminService.editConfiguration(null));

        final List<String> streamIds = Collections.singletonList("stream-id");
        when(templateManagerService.editConfiguration(any(ScenarioConfiguration.class))).thenReturn(streamIds);
        Assert.assertEquals(1, templateManagerAdminService.editConfiguration(new ScenarioConfigurationDTO()).length);
    }

    @Test
    public void testSaveStreamMapping() throws Exception {
        TemplateManagerAdminServiceValueHolder.setTemplateManagerService(templateManagerService);
        boolean saved = new TemplateManagerAdminService().saveStreamMapping(new StreamMappingDTO[0], null, null);
        Assert.assertTrue(saved);
    }
}