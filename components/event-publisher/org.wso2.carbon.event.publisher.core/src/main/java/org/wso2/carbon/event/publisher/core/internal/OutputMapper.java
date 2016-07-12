/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.event.publisher.core.internal;

import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;

public interface OutputMapper {

    /**
     * Converts the event according to the output mappings specified
     *
     * @param event the event to be converted
     * @return converted output event as an Object
     * @throws EventPublisherConfigurationException
     */
    Object convertToMappedOutputEvent(Event event) throws EventPublisherConfigurationException;

    /**
     * Converts the event according to a predefined type with the exact structure of the event passed in
     *
     * @param event the event to be converted
     * @return converted output event as an Object
     * @throws EventPublisherConfigurationException
     */
    Object convertToTypedOutputEvent(Event event) throws EventPublisherConfigurationException;

}
