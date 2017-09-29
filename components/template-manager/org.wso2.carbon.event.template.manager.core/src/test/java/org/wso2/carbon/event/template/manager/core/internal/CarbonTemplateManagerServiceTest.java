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

package org.wso2.carbon.event.template.manager.core.internal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.event.template.manager.core.TemplateDeployer;
import org.wso2.carbon.event.template.manager.core.exception.TemplateManagerException;
import org.wso2.carbon.event.template.manager.core.internal.ds.TemplateManagerValueHolder;
import org.wso2.carbon.event.template.manager.core.internal.util.TemplateManagerConstants;
import org.wso2.carbon.event.template.manager.core.internal.util.TemplateManagerHelper;
import org.wso2.carbon.event.template.manager.core.structure.configuration.ScenarioConfiguration;
import org.wso2.carbon.event.template.manager.core.structure.configuration.StreamMapping;
import org.wso2.carbon.event.template.manager.core.structure.domain.Domain;
import org.wso2.carbon.event.template.manager.core.structure.domain.Scenario;
import org.wso2.carbon.event.template.manager.core.structure.domain.Scenarios;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.script.ScriptEngine;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TemplateManagerHelper.class})
public class CarbonTemplateManagerServiceTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();
    @Captor
    private ArgumentCaptor<Domain> domainArgument;
    @Captor
    private ArgumentCaptor<String> stringArgument;

    @BeforeClass
    public static void init() {
        System.setProperty("carbon.home", "");
    }

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        mockStatic(TemplateManagerHelper.class);
    }

    @Test
    public void testSaveConfigurationWhenResourceExists() throws Exception {
        UserRegistry registry = mock(UserRegistry.class);
        when(registry.resourceExists(anyString())).thenReturn(true);
        RegistryService registryService = mock(RegistryService.class);
        when(registryService.getConfigSystemRegistry(anyInt())).thenReturn(registry);
        TemplateManagerValueHolder.setRegistryService(registryService);

        thrown.expect(TemplateManagerException.class);
        createCarbonTemplateManagerService().saveConfiguration(new ScenarioConfiguration());
    }

    @Test
    public void testSaveConfiguration() throws Exception {
        UserRegistry registry = mock(UserRegistry.class);
        when(registry.resourceExists(anyString())).thenReturn(false);
        RegistryService registryService = mock(RegistryService.class);
        when(registryService.getConfigSystemRegistry(anyInt())).thenReturn(registry);
        TemplateManagerValueHolder.setRegistryService(registryService);

        when(TemplateManagerHelper.getStreamIDsToBeMapped(any(ScenarioConfiguration.class), any(Domain.class),
                                                          any(ScriptEngine.class))).thenCallRealMethod();

        CarbonTemplateManagerService carbonTemplateManagerService = createCarbonTemplateManagerService();
        Domain domain = carbonTemplateManagerService.getDomain("test-domain");
        ScenarioConfiguration configuration = new ScenarioConfiguration();
        configuration.setDomain(domain.getName());

        Assert.assertNull("Return value should be null", carbonTemplateManagerService.saveConfiguration(configuration));

        verifyStatic();
        TemplateManagerHelper.deployArtifacts(any(ScenarioConfiguration.class), domainArgument.capture(),
                                              any(ScriptEngine.class));
        Assert.assertEquals("Incorrect domain was passed to deployArtifacts method", domain, domainArgument.getValue());

        verifyStatic();
        TemplateManagerHelper.getStreamIDsToBeMapped(any(ScenarioConfiguration.class), domainArgument.capture(),
                                                     any(ScriptEngine.class));
        Assert.assertEquals("Incorrect domain was passed to getStreamIDsToBeMapped method", domain,
                            domainArgument.getValue());
    }

    @Test
    public void testSaveStreamMapping() throws Exception {
        final String scenarioConfigName = "scenario-config-name";
        final String domainName = "domain-name";
        final List<StreamMapping> streamMappingList = Collections.emptyList();
        when(TemplateManagerHelper.getConfiguration(anyString())).thenReturn(new ScenarioConfiguration());
        when(TemplateManagerHelper.getStreamMappingPlanId(domainName, scenarioConfigName)).thenReturn("plan-id");
        when(TemplateManagerHelper.generateExecutionPlan(streamMappingList, "plan-id")).thenReturn("execution-plan");

        try {
            createCarbonTemplateManagerService().saveStreamMapping(streamMappingList, scenarioConfigName, domainName);
            Assert.fail("Artifacts deployed without a template deployer for type '" +
                        TemplateManagerConstants.DEPLOYER_TYPE_REALTIME + "'");
        } catch (TemplateManagerException e) {
            // fine
        }

        TemplateDeployer templateDeployer = mock(TemplateDeployer.class);
        ConcurrentHashMap<String, TemplateDeployer> deployers = TemplateManagerValueHolder.getTemplateDeployers();
        deployers.put(TemplateManagerConstants.DEPLOYER_TYPE_REALTIME, templateDeployer);
        createCarbonTemplateManagerService().saveStreamMapping(streamMappingList, scenarioConfigName, domainName);
    }

    @Test
    public void testGetDomain() throws Exception {
        final String domainName = "test-domain";
        Domain domain = createCarbonTemplateManagerService().getDomain(domainName);
        Assert.assertNotNull("domain cannot be null", domain);
        Assert.assertEquals("incorrect domain name", domainName, domain.getName());
    }

    @Test
    public void testGetConfiguration() throws Exception {
        final String domainName = "domain-name";
        final String configName = "config-name";
        final String registryPath = TemplateManagerConstants.TEMPLATE_CONFIG_PATH + "/" + domainName + "/" +
                                    configName + TemplateManagerConstants.CONFIG_FILE_EXTENSION;
        createCarbonTemplateManagerService().getConfiguration(domainName, configName);

        verifyStatic();
        TemplateManagerHelper.getConfiguration(stringArgument.capture());
        Assert.assertEquals("Incorrect registry path", registryPath, stringArgument.getValue());
    }

    @Test
    public void testDeleteConfiguration() throws Exception {
        ScenarioConfiguration configuration = new ScenarioConfiguration();
        when(TemplateManagerHelper.getConfiguration(anyString())).thenReturn(configuration);

        UserRegistry registry = mock(UserRegistry.class);
        RegistryService registryService = mock(RegistryService.class);
        when(registryService.getConfigSystemRegistry(anyInt())).thenReturn(registry);
        TemplateManagerValueHolder.setRegistryService(registryService);

        when(TemplateManagerHelper.getTemplatedArtifactId(anyString(), anyString(), anyString(), anyString(), anyInt()))
                .thenCallRealMethod();

        final String domainName = "test-domain";
        final String configName = "config-name";

        createCarbonTemplateManagerService().deleteConfiguration(domainName, configName);

        Mockito.verify(registry).delete(stringArgument.capture());
        Assert.assertEquals("Incorrect registry path for deletion",
                            TemplateManagerConstants.TEMPLATE_CONFIG_PATH + RegistryConstants.PATH_SEPARATOR +
                            domainName + RegistryConstants.PATH_SEPARATOR + configName +
                            TemplateManagerConstants.CONFIG_FILE_EXTENSION,
                            stringArgument.getValue());
    }

    private CarbonTemplateManagerService createCarbonTemplateManagerService() throws Exception {
        Scenarios scenarios = new Scenarios();
        scenarios.setScenario(Collections.<Scenario>emptyList());
        Domain domain = new Domain();
        domain.setName("test-domain");
        domain.setScenarios(scenarios);
        Map<String, Domain> domains = Collections.singletonMap(domain.getName(), domain);
        when(TemplateManagerHelper.loadDomains()).thenReturn(domains);
        return new CarbonTemplateManagerService();
    }
}
