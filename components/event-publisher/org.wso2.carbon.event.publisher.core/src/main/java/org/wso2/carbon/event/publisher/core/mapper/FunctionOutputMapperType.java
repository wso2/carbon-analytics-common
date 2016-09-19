/*
 *
 *  *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  *  WSO2 Inc. licenses this file to you under the Apache License,
 *  *  Version 2.0 (the "License"); you may not use this file except
 *  *  in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *  http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing,
 *  *  software distributed under the License is distributed on an
 *  *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  *  KIND, either express or implied.  See the License for the
 *  *  specific language governing permissions and limitations
 *  *  under the License.
 *  *
 *
 */

package org.wso2.carbon.event.publisher.core.mapper;

import org.wso2.carbon.event.publisher.core.config.CustomMapperFunction;
import org.wso2.siddhi.core.event.Event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FunctionOutputMapperType extends OutputMapperType {

    public FunctionOutputMapperType(String mappingText) {
        super(mappingText);
    }

    @Override
    public Object getValue(Event event, Map<String, Integer> propertyPositionMap, ConcurrentHashMap<String, CustomMapperFunction> customMapperFunctionsMap) {
        String functionName = mappingText.substring(0, mappingText.indexOf("("));
        String[] parameterAttributes = mappingText.substring(mappingText.indexOf("(") + 1, mappingText.indexOf(")")).split(",");
        Object[] parameterValues = new Object[parameterAttributes.length];
        for (int i = 0; i < parameterAttributes.length; i++) {
            if (parameterAttributes[i].endsWith("'")) {
                parameterValues[i] = parameterAttributes[i].replaceAll("'","");
            } else {
                parameterValues[i] = getPropertyValue(event, parameterAttributes[i], propertyPositionMap);
            }
        }
        CustomMapperFunction customMapperFunction = customMapperFunctionsMap.get(functionName);
        return customMapperFunction.call(parameterValues);
    }
}
