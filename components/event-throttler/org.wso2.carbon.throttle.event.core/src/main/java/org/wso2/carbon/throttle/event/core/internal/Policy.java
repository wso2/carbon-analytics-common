/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.throttle.event.core.internal;

/**
 * Container object for Throttling Policy.
 */
public class Policy {

    private String name;
    private String level;
    private String tier;
    private String eligibilityQuery;
    private String decisionQuery;
    private String description;

    /**
     * All relevant information should be provided when constructing policy. Hence enforcing in constructor.
     * @param name Name of Throttling policy
     * @param level Throttling level in which the policy applies
     * @param tier Throttling tier name
     * @param eligibilityQuery Siddhi query use to check eligibility of the request for the policy
     * @param decisionQuery Siddhi query use to take the throttling decision for a given request
     */
    public Policy(String name, String level, String tier, String eligibilityQuery, String decisionQuery) {
        this.name = name;
        this.level = level;
        this.tier = tier;
        this.eligibilityQuery = eligibilityQuery;
        this.decisionQuery = decisionQuery;
    }

    public String getName() {
        return name;
    }

    public String getLevel() {
        return level;
    }

    public String getTier() {
        return tier;
    }

    public String getEligibilityQuery() {
        return eligibilityQuery;
    }

    public String getDecisionQuery() {
        return decisionQuery;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
