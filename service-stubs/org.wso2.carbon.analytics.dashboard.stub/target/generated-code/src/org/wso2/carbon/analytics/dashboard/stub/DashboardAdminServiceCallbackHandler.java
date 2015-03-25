
/**
 * DashboardAdminServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v11  Built on : Nov 25, 2014 (03:48:14 IST)
 */

    package org.wso2.carbon.analytics.dashboard.stub;

    /**
     *  DashboardAdminServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class DashboardAdminServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public DashboardAdminServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public DashboardAdminServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for getDashboards method
            * override this method for handling normal response from getDashboards operation
            */
           public void receiveResultgetDashboards(
                    org.wso2.carbon.analytics.dashboard.admin.data.Dashboard[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getDashboards operation
           */
            public void receiveErrorgetDashboards(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for deleteDataView method
            * override this method for handling normal response from deleteDataView operation
            */
           public void receiveResultdeleteDataView(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from deleteDataView operation
           */
            public void receiveErrordeleteDataView(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for addDataView method
            * override this method for handling normal response from addDataView operation
            */
           public void receiveResultaddDataView(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from addDataView operation
           */
            public void receiveErroraddDataView(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for updateWidgetInDashboard method
            * override this method for handling normal response from updateWidgetInDashboard operation
            */
           public void receiveResultupdateWidgetInDashboard(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from updateWidgetInDashboard operation
           */
            public void receiveErrorupdateWidgetInDashboard(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for updateDashboard method
            * override this method for handling normal response from updateDashboard operation
            */
           public void receiveResultupdateDashboard(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from updateDashboard operation
           */
            public void receiveErrorupdateDashboard(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getDashboard method
            * override this method for handling normal response from getDashboard operation
            */
           public void receiveResultgetDashboard(
                    org.wso2.carbon.analytics.dashboard.admin.data.Dashboard result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getDashboard operation
           */
            public void receiveErrorgetDashboard(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getWidgets method
            * override this method for handling normal response from getWidgets operation
            */
           public void receiveResultgetWidgets(
                    org.wso2.carbon.analytics.dashboard.admin.data.Widget[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getWidgets operation
           */
            public void receiveErrorgetWidgets(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for updateWidget method
            * override this method for handling normal response from updateWidget operation
            */
           public void receiveResultupdateWidget(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from updateWidget operation
           */
            public void receiveErrorupdateWidget(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for updateDataView method
            * override this method for handling normal response from updateDataView operation
            */
           public void receiveResultupdateDataView(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from updateDataView operation
           */
            public void receiveErrorupdateDataView(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getDataView method
            * override this method for handling normal response from getDataView operation
            */
           public void receiveResultgetDataView(
                    org.wso2.carbon.analytics.dashboard.admin.data.DataView result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getDataView operation
           */
            public void receiveErrorgetDataView(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getWidgetWithDataViewInfo method
            * override this method for handling normal response from getWidgetWithDataViewInfo operation
            */
           public void receiveResultgetWidgetWithDataViewInfo(
                    org.wso2.carbon.analytics.dashboard.admin.data.DataView result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getWidgetWithDataViewInfo operation
           */
            public void receiveErrorgetWidgetWithDataViewInfo(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for addWidget method
            * override this method for handling normal response from addWidget operation
            */
           public void receiveResultaddWidget(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from addWidget operation
           */
            public void receiveErroraddWidget(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for addDashboard method
            * override this method for handling normal response from addDashboard operation
            */
           public void receiveResultaddDashboard(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from addDashboard operation
           */
            public void receiveErroraddDashboard(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getDataViewsInfo method
            * override this method for handling normal response from getDataViewsInfo operation
            */
           public void receiveResultgetDataViewsInfo(
                    org.wso2.carbon.analytics.dashboard.admin.data.DataView[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getDataViewsInfo operation
           */
            public void receiveErrorgetDataViewsInfo(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for addWidgetToDashboard method
            * override this method for handling normal response from addWidgetToDashboard operation
            */
           public void receiveResultaddWidgetToDashboard(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from addWidgetToDashboard operation
           */
            public void receiveErroraddWidgetToDashboard(java.lang.Exception e) {
            }
                


    }
    