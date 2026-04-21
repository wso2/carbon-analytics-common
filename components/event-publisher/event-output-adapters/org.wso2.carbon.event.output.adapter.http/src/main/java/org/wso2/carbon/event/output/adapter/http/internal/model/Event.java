/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package org.wso2.carbon.event.output.adapter.http.internal.model;

import com.google.gson.JsonObject;

/**
 * This class contains getters and setters for Event object attributes.
 */
public class Event {

    private JsonObject payloadData;

    @Override
    public String toString() {

        return "\"payloadData\":" + payloadData.toString();
    }

    public void setPayloadData(JsonObject payloadData) {

        this.payloadData = payloadData;
    }

    public JsonObject getPayloadData() {

        return payloadData;
    }
}
