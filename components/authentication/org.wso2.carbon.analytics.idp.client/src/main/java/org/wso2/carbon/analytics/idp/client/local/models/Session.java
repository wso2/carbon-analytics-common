/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.analytics.idp.client.local.models;

import java.util.UUID;

/**
 * Session class.
 */
public class Session {
    private UUID sessionId;
    private boolean internalUser;
    private int userHash;
    private String username;
    private Long expiryTime;

    public Session(int userHash, boolean internalUser, String username, Long expiryTime) {
        this.userHash = userHash;
        this.username = username;
        this.internalUser = internalUser;
        this.sessionId = UUID.randomUUID();
        this.expiryTime = expiryTime;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public Long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(Long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public int getUserHash() {
        return userHash;
    }

    public boolean isInternalUser() {
        return internalUser;
    }

    public String getUsername() {
        return username;
    }
}
