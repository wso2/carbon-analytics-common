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

package org.wso2.carbon.event.template.manager.admin.internal.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.event.template.manager.admin.dto.configuration.AttributeMappingDTO;
import org.wso2.carbon.event.template.manager.admin.dto.configuration.ScenarioConfigurationDTO;
import org.wso2.carbon.event.template.manager.admin.dto.configuration.StreamMappingDTO;
import org.wso2.carbon.event.template.manager.core.structure.configuration.ScenarioConfiguration;
import org.wso2.carbon.event.template.manager.core.structure.configuration.StreamMapping;

import java.util.Collections;
import java.util.List;

@RunWith(PowerMockRunner.class)
public class ConfigurationMapperTest {

    @Test
    public void testMapStreamMapping() {
        AttributeMappingDTO attributeMappingDTO = new AttributeMappingDTO();
        attributeMappingDTO.setAttributeType("attribute-type");
        attributeMappingDTO.setFromAttribute("attribute-from");
        attributeMappingDTO.setToAttribute("attribute-to");
        StreamMappingDTO streamMappingDTO = new StreamMappingDTO();
        streamMappingDTO.setFromStream("from-stream");
        streamMappingDTO.setToStream("to-stream");
        streamMappingDTO.setAttributeMappingDTOs(new AttributeMappingDTO[]{attributeMappingDTO});
        StreamMappingDTO[] streamMappingDTOs = new StreamMappingDTO[]{streamMappingDTO};

        List<StreamMapping> streamMappings = ConfigurationMapper.mapStreamMapping(streamMappingDTOs);

        Assert.assertEquals("Incorrect number of stream mappings", streamMappingDTOs.length, streamMappings.size());
    }

    @Test
    public void testMapConfigurations() {
        List<ScenarioConfiguration> configurations = Collections.singletonList(new ScenarioConfiguration());
        ScenarioConfigurationDTO[] configurationDTOS = ConfigurationMapper.mapConfigurations(configurations);
        Assert.assertEquals("Incorrect number of configuration DTOs", configurations.size(), configurationDTOS.length);
    }
}
