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
package org.wso2.carbon.analytics.dashboard.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dashboard.admin.data.*;
import org.wso2.carbon.analytics.dashboard.admin.exception.InvalidRequestException;
import org.wso2.carbon.analytics.dashboard.admin.exception.RegistryResourceException;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.RegistryConstants;

import java.util.ArrayList;
import java.util.List;

public class DashboardAdminService extends AbstractAdmin {

	/**
	 * Relative Registry locations for dataViews and dashboards.
	 */
	private static final String DATAVIEWS_DIR =
			RegistryConstants.PATH_SEPARATOR + "repository" + RegistryConstants.PATH_SEPARATOR +
			"components" + RegistryConstants.PATH_SEPARATOR +
			"org.wso2.carbon.analytics.dataviews" + RegistryConstants.PATH_SEPARATOR;

	private static final String DASHBOARDS_DIR =
			RegistryConstants.PATH_SEPARATOR + "repository" + RegistryConstants.PATH_SEPARATOR +
			"components" + RegistryConstants.PATH_SEPARATOR +
			"org.wso2.carbon.analytics.dashboards" + RegistryConstants.PATH_SEPARATOR;

	/**
	 * Logger
	 */
	private Log logger = LogFactory.getLog(DashboardAdminService.class);

	/**
	 * @return All the dataView objects saved in the registry.
	 * @throws RegistryResourceException
	 */
	public DataView[] getDataViewsInfo() throws RegistryResourceException {

		List<DataView> dataViews = new ArrayList<>();
		Collection dataViewsCollection = RegistryUtils.readCollection(DATAVIEWS_DIR);
		String[] resourceNames;
		try {
			resourceNames = dataViewsCollection.getChildren();
		} catch (RegistryException e) {
			logger.error(e);
			throw new RegistryResourceException(
					"Unable to extract child resources from dataViews directory", e);
		}
		for (String resourceName : resourceNames) {
			DataView dataView = getDataView(resourceName.replace(DATAVIEWS_DIR, ""));
			dataView.setWidgets(new Widget[0]);
			dataViews.add(dataView);
		}
		DataView[] dataViewArray = new DataView[dataViews.size()];
		return dataViews.toArray(dataViewArray);
	}

	/**
	 * @param dataViewID Id of the target dataView to be read.
	 * @return DataView object which is read from the registry.
	 * @throws RegistryResourceException
	 */
	public DataView getDataView(String dataViewID) throws RegistryResourceException {
		return (DataView) RegistryUtils.readResource(DATAVIEWS_DIR + dataViewID, DataView.class);
	}

	/**
	 * @return DataViewPrimalInfo containing only the basic information of the dataView with given ID
	 */
	public DataViewPrimitives getDataViewPrimitives(String dataViewID)
			throws RegistryResourceException {
		DataViewPrimitives dataViewPrimitives = new DataViewPrimitives();
		DataView dataView = getDataView(dataViewID);

		dataViewPrimitives.setId(dataView.getId());
		dataViewPrimitives.setType(dataView.getType());
		dataViewPrimitives.setColumns(dataView.getColumns());
		dataViewPrimitives.setDataSource(dataView.getDataSource());
		dataViewPrimitives.setFilter(dataView.getFilter());
		dataViewPrimitives.setName(dataView.getName());

		return dataViewPrimitives;
	}

	/**
	 * Appends a dataView object to the registry with the dataView as the resource content.
	 *
	 * @param dataView Object to be appended to the registry.
	 * @throws RegistryResourceException
	 */
	public boolean addDataView(DataView dataView) throws RegistryResourceException {

		if (!RegistryUtils.isResourceExist(DATAVIEWS_DIR + dataView.getId())) {
			RegistryUtils.writeResource(DATAVIEWS_DIR + dataView.getId(), dataView);
			return true;
		} else {
			String errorMessage = "DataView with ID:" + dataView.getId() + " already exists";
			logger.error(errorMessage);
			throw new RegistryResourceException(errorMessage);
		}
	}

	/**
	 * Updates an existing dataView.
	 *
	 * @param dataView Object to be updated.
	 * @throws RegistryResourceException If a matching dataView does not exist.
	 */
	public boolean updateDataView(DataView dataView) throws RegistryResourceException {

		if (RegistryUtils.isResourceExist(DATAVIEWS_DIR + dataView.getId())) {
			RegistryUtils.writeResource(DATAVIEWS_DIR + dataView.getId(), dataView);
			return true;
		} else {
			String errorMessage = "DataView with ID:" + dataView.getId() + " does not exist";
			logger.debug(errorMessage);
			return false;
		}
	}

	/**
	 * Deletes the dataView resource with the given name.
	 *
	 * @param dataViewID Id of the dataView to be deleted.
	 */
	public boolean deleteDataView(String dataViewID) throws RegistryResourceException {
		if (RegistryUtils.isResourceExist(DATAVIEWS_DIR + dataViewID)) {
			RegistryUtils.deleteResource(DATAVIEWS_DIR + dataViewID);
			return true;
		} else {
			String errorMessage = "DataView with ID:" + dataViewID + " does not exist";
			logger.debug(errorMessage);
			return false;
		}
	}

	/**
	 * Appends a widget to an existing DataView.
	 *
	 * @param dataViewID Existing dataView.
	 * @param widget     Widget to be appended.
	 * @throws RegistryResourceException,InvalidRequestException
	 */
	public boolean addWidget(String dataViewID, Widget widget)
			throws RegistryResourceException, InvalidRequestException {
		DataView dataView = getDataView(dataViewID);
		dataView.addWidget(widget);
		return updateDataView(dataView);
	}

	/**
	 * Updates a widget of an existing DataView.
	 *
	 * @param dataViewID Existing dataView object.
	 * @param widget     Widget to be updated.
	 * @throws RegistryResourceException,InvalidRequestException
	 */
	public boolean updateWidget(String dataViewID, Widget widget)
			throws RegistryResourceException, InvalidRequestException {
		DataView dataView = getDataView(dataViewID);
		dataView.updateWidget(widget);
		return updateDataView(dataView);
	}

	//TODO nice to have feature- when dimensions are not given, find a place for it from the server side

	/**
	 * Returns a dataView object with a SINGLE widget.
	 *
	 * @param dataViewID DataView name in which the target widget resides.
	 * @param widgetID   Widget to be included in the dataView object.
	 * @return DataView object with a single widget in the widget array-list.
	 * @throws RegistryResourceException,InvalidRequestException
	 */
	public Widget getWidget(String dataViewID, String widgetID)
			throws RegistryResourceException, InvalidRequestException {
		DataView dataView = getDataView(dataViewID);
		Widget widget = dataView.getWidget(widgetID);
		return widget;
	}

	/**
	 * @param dataViewID Target dataView.
	 * @return Widget list of given dataView as an array.
	 * @throws RegistryResourceException
	 */
	public Widget[] getWidgets(String dataViewID) throws RegistryResourceException {
		return getDataView(dataViewID).getWidgets();
	}

	/**
	 * @return All the existing dashboards as an Array-list.
	 * @throws RegistryResourceException
	 */
	public Dashboard[] getDashboards() throws RegistryResourceException {
		try {
			List<Dashboard> dashboards = new ArrayList<>();
			Collection dashboardsCollection = RegistryUtils.readCollection(DASHBOARDS_DIR);
			String[] resourceNames = dashboardsCollection.getChildren();
			for (String resourceName : resourceNames) {
				Dashboard dashboard = getDashboard(resourceName.replace(DASHBOARDS_DIR, ""));
				dashboards.add(dashboard);
			}
			Dashboard[] dashboardArray = new Dashboard[dashboards.size()];
			return dashboards.toArray(dashboardArray);
		} catch (Exception e) {
			String errorMessage = "Unable to extract resources from collection";
			logger.error(errorMessage);
			throw new RegistryResourceException(errorMessage, e);
		}
	}

	/**
	 * @param dashboardID Target dashboard ID.
	 * @return Dashboard with widget-meta-Data.
	 * @throws RegistryResourceException
	 */
	public Dashboard getDashboard(String dashboardID) throws RegistryResourceException {
		return (Dashboard) RegistryUtils
				.readResource(DASHBOARDS_DIR + dashboardID, Dashboard.class);
	}

	/**
	 * Adds a new dashboard to the registry, does not allow to replace existing dashboard.
	 *
	 * @param dashboard Object to be appended to the registry.
	 * @throws RegistryResourceException
	 */
	public boolean addDashboard(Dashboard dashboard) throws RegistryResourceException {
		if (!RegistryUtils.isResourceExist(DASHBOARDS_DIR + dashboard.getId())) {
			RegistryUtils.writeResource(DASHBOARDS_DIR + dashboard.getId(), dashboard);
			return true;
		} else {
			String errorMessage = "Dashboard with name:" + dashboard.getId() + " already exists";
			logger.debug(errorMessage);
			throw new RegistryResourceException(errorMessage);
		}
	}

	/**
	 * Updates an existing dashboard.
	 *
	 * @param dashboard Object to be updated.
	 * @throws RegistryResourceException If a matching dashboard does not exist.
	 */
	public boolean updateDashboard(Dashboard dashboard) throws RegistryResourceException {

		if (RegistryUtils.isResourceExist(DASHBOARDS_DIR + dashboard.getId())) {
			RegistryUtils.writeResource(DASHBOARDS_DIR + dashboard.getId(), dashboard);
			return true;
		} else {
			String errorMessage = "Dashboard with name:" + dashboard.getId() + " does not exist";
			logger.debug(errorMessage);
			throw new RegistryResourceException(errorMessage);
		}
	}

	/**
	 * @param dashboardID    Id of the dashboard to which the widget-meta-data will be appended.
	 * @param widgetInstance Metadata to be appended.
	 * @throws RegistryResourceException, InvalidRequestException
	 */
	public boolean addWidgetInstance(String dashboardID, WidgetInstance widgetInstance)
			throws RegistryResourceException, InvalidRequestException {
		Dashboard dashboard = getDashboard(dashboardID);
		dashboard.addWidgetInstance(widgetInstance);
		return updateDashboard(dashboard);
	}

	/**
	 * @param dashboardID    Id of the dashboard to which the widget-meta-data will be updated.
	 * @param widgetInstance Metadata to be updated.
	 * @throws RegistryResourceException,InvalidRequestException
	 */
	public boolean updateWidgetInstance(String dashboardID, WidgetInstance widgetInstance)
			throws InvalidRequestException, RegistryResourceException {
		Dashboard dashboard = getDashboard(dashboardID);
		dashboard.updateWidgetInstance(widgetInstance);
		return updateDashboard(dashboard);
	}
}
