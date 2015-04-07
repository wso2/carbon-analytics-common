/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.analytics.dashboard.admin;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dashboard.admin.Authentication.UserAdminClient;
import org.wso2.carbon.analytics.dashboard.admin.data.*;
import org.wso2.carbon.analytics.dashboard.admin.exception.InvalidRequestException;
import org.wso2.carbon.analytics.dashboard.admin.exception.RegistryResourceException;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.RegistryException;

public class DashboardAdminService extends AbstractAdmin {

    private Log logger = LogFactory.getLog(DashboardAdminService.class);

	/**
	 * @return All the existing dashboards as an Array-list.
	 * @throws RegistryResourceException
	 */
    public Dashboard[] getDashboards() throws RegistryResourceException {
        try {
            createCollections();
            Dashboard[] dashboards;
            Collection dashboardsCollection = RegistryUtils.readCollection(DashboardConstants.DASHBOARDS_DIR);
            String[] resourceNames = dashboardsCollection.getChildren();
	        dashboards=new Dashboard[resourceNames.length];
            for (int i=0;i<resourceNames.length;i++) {
                Dashboard dashboard = getDashboard(
		                resourceNames[i].replace(DashboardConstants.DASHBOARDS_DIR, ""));
                dashboards[i]=dashboard;
            }
            return dashboards;
        } catch (Exception e) {
	        String errorMessage="An error occurred while retrieving dashboards.";
            logger.error(errorMessage, e);
	        throw new RegistryResourceException(errorMessage, e);
        }
    }

	/**
	 * @param dashboardID Target dashboard ID.
	 * @return Dashboard with widget-meta-Data.
	 * @throws RegistryResourceException
	 */
    public Dashboard getDashboard(String dashboardID) throws RegistryResourceException {
        Dashboard dashboard;
        try {
            dashboard = (Dashboard) RegistryUtils.readResource(DashboardConstants.DASHBOARDS_DIR
                    + dashboardID, Dashboard.class);
            if (dashboard.getWidgetInstances() == null) {
                dashboard.setWidgetInstances(new WidgetInstance[0]);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Returning Dashboard with title " + dashboard.getTitle());
            }
        } catch (RegistryException e) {
            logger.error("Error while reading dashboard with id: " + dashboardID, e);
            throw new RegistryResourceException("Error while reading dashboard with id: " + dashboardID, e);
        }
        return dashboard;
    }

	/**
	 * Adds a new dashboard to the registry, does not allow to replace existing dashboard.
	 *
	 * @param dashboard Object to be appended to the registry.
	 * @throws RegistryResourceException
	 */
    public boolean addDashboard(Dashboard dashboard) throws RegistryResourceException {
        try {
            if (!RegistryUtils.isResourceExist(DashboardConstants.DASHBOARDS_DIR + dashboard.getId())) {
                RegistryUtils.writeResource(DashboardConstants.DASHBOARDS_DIR + dashboard.getId(), dashboard);
                return true;
            } else {
                String errorMessage = "Dashboard with id:" + dashboard.getId() + " already exists";
	            if(logger.isDebugEnabled()) {
		            logger.debug(errorMessage);
	            }
	            throw new RegistryResourceException(errorMessage);
            }
        } catch (RegistryException e) {
            String message = "Error occurred while adding dashboard with id: " + dashboard.getId();
            logger.error(message, e);
            throw new RegistryResourceException(message, e);
        }
    }

    	/**
	 * @return All the dataView objects saved in the registry.
	 * @throws RegistryResourceException//TODO check this method
	 */
	public DataView[] getDataViewsInfo() throws RegistryResourceException {

		DataView[] dataViews;
		String[] resourceNames;
		try {
			Collection dataViewsCollection = RegistryUtils.readCollection(DashboardConstants.DATAVIEWS_DIR);
			resourceNames = dataViewsCollection.getChildren();
		} catch (RegistryException e) {
			throw new RegistryResourceException(
					"Unable to extract child resources from dataViews directory", e);
		}
		dataViews=new DataView[resourceNames.length];
		int count=0;
		for (String resourceName : resourceNames) {
			DataView dataView = getDataView(resourceName.replace(DashboardConstants.DATAVIEWS_DIR, ""));
			dataView.setWidgets(new Widget[0]);
			dataViews[count++]=dataView;
		}
		return dataViews;
	}

	/**
	 * @param dataViewID Id of the target dataView to be read.
	 * @return DataView object which is read from the registry.
	 * @throws RegistryResourceException
	 */
    public DataView getDataView(String dataViewID) throws RegistryResourceException {
        DataView dataView ;
        try {
            dataView = (DataView) RegistryUtils.readResource(DashboardConstants.DATAVIEWS_DIR +
                    dataViewID, DataView.class);
            if (dataView.getWidgets() == null) {
                dataView.setWidgets(new Widget[0]);
            }
            if (dataView.getColumns() == null) {
                dataView.setColumns(new Column[0]);
            }
        } catch (RegistryException re) {
            String message = "Error occurred while retrieving DataView with id: " + dataViewID;
            logger.error(message, re);
	        throw new RegistryResourceException("Unable to read dataView object from registry",re);
        }
        return dataView;
    }

	/**
	 * @return DataViewPrimalInfo containing only the basic information of the dataView with given ID
	 */
	public DataViewPrimitive getDataViewPrimitive(String dataViewID)
			throws RegistryResourceException {
		DataViewPrimitive dataViewPrimitive = new DataViewPrimitive();
		DataView dataView = getDataView(dataViewID);

		dataViewPrimitive.setId(dataView.getId());
		dataViewPrimitive.setType(dataView.getType());
		dataViewPrimitive.setColumns(dataView.getColumns());
		dataViewPrimitive.setDataSource(dataView.getDataSource());
		dataViewPrimitive.setFilter(dataView.getFilter());
		dataViewPrimitive.setName(dataView.getName());

		return dataViewPrimitive;
	}

	/**
	 * Appends a dataView object to the registry with the dataView as the resource content.
	 *
	 * @param dataView Object to be appended to the registry.
	 * @throws RegistryResourceException
	 */
    public boolean addDataView(DataView dataView) throws RegistryResourceException {
        try {
	        if (!RegistryUtils.isResourceExist(DashboardConstants.DATAVIEWS_DIR + dataView.getId())) {
		        RegistryUtils.writeResource(DashboardConstants.DATAVIEWS_DIR + dataView.getId(), dataView);
		        return true;
	        } else {
		        String errorMessage = "DataView with ID:" + dataView.getId() + " already exists";
		        if(logger.isDebugEnabled()) {
			        logger.debug(errorMessage);
		        }
		        throw new RegistryResourceException(errorMessage);
	        }
        } catch (RegistryException e) {
            String errorMessage = "Error occurred while adding DataView : " + dataView.getId();
            logger.error(errorMessage, e);
            throw new RegistryResourceException(errorMessage, e);
        }
    }

	/**
	 * Updates an existing dataView.
	 *
	 * @param dataView Object to be updated.
	 * @throws RegistryResourceException If a matching dataView does not exist.
	 */
    public boolean updateDataView(DataView dataView) throws RegistryResourceException {
        try {
            if (RegistryUtils.isResourceExist(DashboardConstants.DATAVIEWS_DIR + dataView.getId())) {
                RegistryUtils.writeResource(DashboardConstants.DATAVIEWS_DIR + dataView.getId(), dataView);
                return true;
            } else {
	            String errorMessage = "DataView with ID:" + dataView.getId() + " does not exist";
	            if(logger.isDebugEnabled()) {
		            logger.debug(errorMessage);
	            }
	            return false;//TODO add new dataView?
            }
        } catch (RegistryException e) {
            logger.error("Error occurred while updating the DataView : " + dataView.getId(), e);
            throw new RegistryResourceException("Error occurred while updating the DataView : " + dataView.getId(), e);
        }
    }

    /**
     * Deletes the dataView resource with the given name.
     *
     * @param dataViewID Id of the dataView to be deleted.
     */
    public boolean deleteDataView(String dataViewID) throws RegistryResourceException {
        try {
            if (RegistryUtils.isResourceExist(DashboardConstants.DATAVIEWS_DIR + dataViewID)) {
                RegistryUtils.deleteResource(DashboardConstants.DATAVIEWS_DIR + dataViewID);
                return true;
            } else {
                String errorMessage = "DataView with ID:" + dataViewID + " does not exist";
	            if(logger.isDebugEnabled()) {
		            logger.debug(errorMessage);
	            }
                return false;
            }
        } catch (RegistryException e) {
            logger.error("Error occurred while deleting the DataView [" + dataViewID + "]", e);
            throw new RegistryResourceException("Error occurred while deleting the DataView [" + dataViewID + "]", e);
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
    public boolean updateWidget(String dataViewID, Widget widget) throws RegistryResourceException, InvalidRequestException {
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
	 * Updates an existing dashboard.
	 *
	 * @param dashboard Object to be updated.
	 * @throws RegistryResourceException If a matching dashboard does not exist.
	 */
    public boolean updateDashboard(Dashboard dashboard) throws RegistryResourceException {
        try {
            if (RegistryUtils.isResourceExist(DashboardConstants.DASHBOARDS_DIR + dashboard.getId())) {
                RegistryUtils.writeResource(DashboardConstants.DASHBOARDS_DIR + dashboard.getId(), dashboard);
                return true;
            } else {
                String errorMessage = "Dashboard with name:" + dashboard.getId() + " does not exist";
	            if(logger.isDebugEnabled()) {
		            logger.debug(errorMessage);
	            }
	            return false;
            }
        } catch (RegistryException re) {
            logger.error(re);
	        throw new RegistryResourceException("Unable to write dashboard resource:"+dashboard.getId(),re);
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

    private void createCollections() throws RegistryException {
        if (!RegistryUtils.isResourceExist(DashboardConstants.DASHBOARDS_DIR)) {
            logger.info("Creating Registry collection for Dashboards");
            RegistryUtils.createCollection(DashboardConstants.DASHBOARDS_DIR);
        }
        if (!RegistryUtils.isResourceExist(DashboardConstants.DATAVIEWS_DIR)) {
            logger.info("Creating Registry collection for DataViews");
            RegistryUtils.createCollection(DashboardConstants.DATAVIEWS_DIR);
        }
    }

	/**
	 * @return boolean to indicate login status //TODO update this
	 */
	public boolean authenticateUser(String username,String password) throws Exception {
		String SEVER_URL = "https://localhost:9443/services/";//TODO update this
		try {
			ConfigurationContext configContext =
					ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
			UserAdminClient sampleUserAdminClient = new UserAdminClient(configContext, SEVER_URL);
			if (sampleUserAdminClient.authenticate(username, password)) {
				if (logger.isDebugEnabled()) {
					logger.debug("User authenticated with given credentials");
				}
				return true;
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("Invalid credentials given. Unable to authenticate user");
				}
				return false;
			}
		} catch (Exception e) {
			logger.error("Failed to execute authentication method",e);
			throw e;
		}
	}

}