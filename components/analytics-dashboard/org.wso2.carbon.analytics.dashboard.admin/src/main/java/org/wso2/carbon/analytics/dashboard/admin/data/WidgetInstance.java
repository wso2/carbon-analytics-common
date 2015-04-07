/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.analytics.dashboard.admin.data;

/**
 * An instance of a Widget defined by a specific dataView
 * Contains the Widget ID of a widget which reside inside a dataView and the dimensions needed to place inside a dashboard
 */
public class WidgetInstance {

	/**
	 * ID of a widget which resides inside a dataView
	 */
	private String WidgetID;

	/**
	 * Dimensions and the position to place inside a Dashboard
	 */
	private WidgetDimensions dimensions;

	public String getWidgetID() {
		return WidgetID;
	}

	public void setWidgetID(String widgetID) {
		this.WidgetID = widgetID;
	}

	public WidgetDimensions getDimensions() {
		return dimensions;
	}

	public void setDimensions(WidgetDimensions dimensions) {
		this.dimensions = dimensions;
	}
}
