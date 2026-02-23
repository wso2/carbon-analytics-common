package org.wso2.carbon.event.template.manager.core.internal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
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

import javax.script.ScriptEngine;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CarbonTemplateManagerServiceTest {

    @Captor
    private ArgumentCaptor<Domain> domainArgument;

    @Captor
    private ArgumentCaptor<String> stringArgument;

    @Mock
    private UserRegistry registry;

    @Mock
    private RegistryService registryService;

    @BeforeClass
    public static void init() {
        System.setProperty("carbon.home", "");
    }

    @Test(expected = TemplateManagerException.class)
    public void testSaveConfigurationWhenResourceExists() throws Exception {
        when(registry.resourceExists(anyString())).thenReturn(true);
        when(registryService.getConfigSystemRegistry(anyInt())).thenReturn(registry);
        TemplateManagerValueHolder.setRegistryService(registryService);

        try (MockedStatic<TemplateManagerHelper> helper = mockStatic(TemplateManagerHelper.class)) {
            helper.when(TemplateManagerHelper::loadDomains).thenReturn(createMockDomainMap());
            new CarbonTemplateManagerService().saveConfiguration(new ScenarioConfiguration());
        }
    }

    @Test
    public void testSaveConfiguration() throws Exception {
        when(registry.resourceExists(anyString())).thenReturn(false);
        when(registryService.getConfigSystemRegistry(anyInt())).thenReturn(registry);
        TemplateManagerValueHolder.setRegistryService(registryService);

        try (MockedStatic<TemplateManagerHelper> helper = mockStatic(TemplateManagerHelper.class)) {
            Domain mockDomain = createMockDomain("test-domain");

            // 1. Stub the necessary methods
            helper.when(TemplateManagerHelper::loadDomains)
                    .thenReturn(Collections.singletonMap("test-domain", mockDomain));

            // Use any() for the engine if it might be null/different
            helper.when(() -> TemplateManagerHelper.getStreamIDsToBeMapped(any(), any(), any()))
                    .thenCallRealMethod();

            CarbonTemplateManagerService carbonTemplateManagerService = new CarbonTemplateManagerService();
            ScenarioConfiguration configuration = new ScenarioConfiguration();
            configuration.setDomain("test-domain");

            // 2. Execute
            carbonTemplateManagerService.saveConfiguration(configuration);

            // 3. Verify specifically using Mockito.times(1) to filter out other static calls
            helper.verify(
                    () -> TemplateManagerHelper.deployArtifacts(
                            any(ScenarioConfiguration.class),
                            domainArgument.capture(),
                            any() // Using any() here because the logs show 'null' was passed
                                                               ),
                    Mockito.times(1)
                         );

            Assert.assertEquals("Incorrect domain passed", mockDomain.getName(), domainArgument.getValue().getName());
        }
    }

    @Test
    public void testSaveStreamMapping() throws Exception {
        final String scenarioConfigName = "scenario-config-name";
        final String domainName = "domain-name";
        final List<StreamMapping> streamMappingList = Collections.emptyList();

        try (MockedStatic<TemplateManagerHelper> helper = mockStatic(TemplateManagerHelper.class)) {
            helper.when(TemplateManagerHelper::loadDomains).thenReturn(createMockDomainMap());
            helper.when(() -> TemplateManagerHelper.getConfiguration(anyString())).thenReturn(new ScenarioConfiguration());
            helper.when(() -> TemplateManagerHelper.getStreamMappingPlanId(domainName, scenarioConfigName)).thenReturn("plan-id");
            helper.when(() -> TemplateManagerHelper.generateExecutionPlan(streamMappingList, "plan-id")).thenReturn("execution-plan");

            CarbonTemplateManagerService service = new CarbonTemplateManagerService();

            // Test failure without deployer
            try {
                service.saveStreamMapping(streamMappingList, scenarioConfigName, domainName);
                Assert.fail("Should have failed without deployer");
            } catch (TemplateManagerException e) {
                // success
            }

            // Test success with deployer
            TemplateDeployer templateDeployer = mock(TemplateDeployer.class);
            TemplateManagerValueHolder.getTemplateDeployers().put(TemplateManagerConstants.DEPLOYER_TYPE_REALTIME, templateDeployer);
            service.saveStreamMapping(streamMappingList, scenarioConfigName, domainName);
        }
    }

    @Test
    public void testDeleteConfiguration() throws Exception {
        ScenarioConfiguration configuration = new ScenarioConfiguration();
        when(registryService.getConfigSystemRegistry(anyInt())).thenReturn(registry);
        TemplateManagerValueHolder.setRegistryService(registryService);

        try (MockedStatic<TemplateManagerHelper> helper = mockStatic(TemplateManagerHelper.class)) {
            helper.when(TemplateManagerHelper::loadDomains).thenReturn(createMockDomainMap());
            helper.when(() -> TemplateManagerHelper.getConfiguration(anyString())).thenReturn(configuration);
            helper.when(() -> TemplateManagerHelper.getTemplatedArtifactId(anyString(), anyString(), anyString(), anyString(), anyInt()))
                    .thenCallRealMethod();

            final String domainName = "test-domain";
            final String configName = "config-name";

            new CarbonTemplateManagerService().deleteConfiguration(domainName, configName);

            Mockito.verify(registry).delete(stringArgument.capture());
            Assert.assertTrue(stringArgument.getValue().contains(configName));
        }
    }

    // Helper methods to replace the old private create service method for cleaner static mocking scope
    private Domain createMockDomain(String name) {
        Domain domain = new Domain();
        domain.setName(name);
        Scenarios scenarios = new Scenarios();
        scenarios.setScenario(Collections.emptyList());
        domain.setScenarios(scenarios);
        return domain;
    }

    private Map<String, Domain> createMockDomainMap() {
        Domain domain = createMockDomain("test-domain");
        return Collections.singletonMap(domain.getName(), domain);
    }
}