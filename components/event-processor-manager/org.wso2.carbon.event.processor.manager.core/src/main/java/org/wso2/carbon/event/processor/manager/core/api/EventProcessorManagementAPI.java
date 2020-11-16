/*
 *
 *  * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied. See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */
package org.wso2.carbon.event.processor.manager.core.api;


import org.wso2.carbon.event.processor.manager.core.exception.EventManagementException;

/**
 * This is the interface which expose the operations to perform on the event event processor management.
 * The operations can be analytics data manipulation in non-secured and secured manner.
 */
public interface EventProcessorManagementAPI {


    /**
     * Creates a table, if not already there in the given record store, or else, it will re-create the table in the given record store,
     * and also, the columns are not defined here, but can contain any arbitrary number of columns when data is added.
     * The table names are not case sensitive.
     * @throws EventManagementException
     */
    void persistStates() throws EventManagementException;

    /**
     * Returns the record store name given the table information.
     * @return The record store name
     * @throws EventManagementException
     */
    boolean getIsActiveNode() throws EventManagementException;

}
