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

import org.apache.axis2.AxisFault;

import java.util.Arrays;
import java.util.List;

/**
 * Dashboard attributes and a set of widgetInstances
 * Contains a set of widget-Instances, definition of each residing inside a specific dataView object
 */
public class Dashboard {

	private String id;
	private String title;
	private String group;
	private String[] roles;
	private WidgetInstance[] widgetInstances;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String[] getRoles() {
		return roles;
	}

	public void setRoles(String[] roles) {
		this.roles = roles;
	}

	public WidgetInstance[] getWidgetInstances() {
		return widgetInstances;
	}

	public void setWidgetInstances(WidgetInstance[] widgetInstances) {
		this.widgetInstances = widgetInstances;
	}

	public void addWidgetInstance(WidgetInstance widgetInstance) throws AxisFault {
		for (WidgetInstance existingWidget : widgetInstances) {
			if (existingWidget.getWidgetID().equals(widgetInstance.getWidgetID())) {
				throw new AxisFault("Widget Instance with given widget ID already exists in the dashboard");
			}
		}
		widgetInstances =Arrays.copyOf(widgetInstances, widgetInstances.length+1);
		widgetInstances[widgetInstances.length-1]=widgetInstance;
	}

	public boolean updateWidgetInstance(WidgetInstance widgetInstance) throws AxisFault {
		boolean updateStatus = deleteWidgetInstance(widgetInstance.getWidgetID());
		addWidgetInstance(widgetInstance);
		return updateStatus;
	}

	public boolean deleteWidgetInstance(String widgetID) throws AxisFault {
		List<WidgetInstance> instanceList = Arrays.asList(widgetInstances);
		for (WidgetInstance existingInstance : instanceList) {
			if (existingInstance.getWidgetID().equals(widgetID)) {
				instanceList.remove(existingInstance);
				widgetInstances = instanceList.toArray(new WidgetInstance[instanceList.size()]);
				return true;
			}
		}
		return false;
	}

}
