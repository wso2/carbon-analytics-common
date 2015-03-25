

/**
 * DashboardAdminService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v11  Built on : Nov 25, 2014 (03:48:14 IST)
 */

    package org.wso2.carbon.analytics.dashboard.stub;

    /*
     *  DashboardAdminService java interface
     */

    public interface DashboardAdminService {
          

        /**
          * Auto generated method signature
          * 
                    * @param getDashboards26
                
         */

         
                     public org.wso2.carbon.analytics.dashboard.admin.data.Dashboard[] getDashboards(

                        )
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getDashboards26
            
          */
        public void startgetDashboards(

            

            final org.wso2.carbon.analytics.dashboard.stub.DashboardAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param deleteDataView29
                
         */

         
                     public boolean deleteDataView(

                        java.lang.String dataViewID30)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param deleteDataView29
            
          */
        public void startdeleteDataView(

            java.lang.String dataViewID30,

            final org.wso2.carbon.analytics.dashboard.stub.DashboardAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param addDataView33
                
         */

         
                     public boolean addDataView(

                        org.wso2.carbon.analytics.dashboard.admin.data.DataView dataView34)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param addDataView33
            
          */
        public void startaddDataView(

            org.wso2.carbon.analytics.dashboard.admin.data.DataView dataView34,

            final org.wso2.carbon.analytics.dashboard.stub.DashboardAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param updateWidgetInDashboard37
                
         */

         
                     public boolean updateWidgetInDashboard(

                        java.lang.String dashboardID38,org.wso2.carbon.analytics.dashboard.admin.data.WidgetMetaData widget39)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param updateWidgetInDashboard37
            
          */
        public void startupdateWidgetInDashboard(

            java.lang.String dashboardID38,org.wso2.carbon.analytics.dashboard.admin.data.WidgetMetaData widget39,

            final org.wso2.carbon.analytics.dashboard.stub.DashboardAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param updateDashboard42
                
         */

         
                     public boolean updateDashboard(

                        org.wso2.carbon.analytics.dashboard.admin.data.Dashboard dashboard43)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param updateDashboard42
            
          */
        public void startupdateDashboard(

            org.wso2.carbon.analytics.dashboard.admin.data.Dashboard dashboard43,

            final org.wso2.carbon.analytics.dashboard.stub.DashboardAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getDashboard46
                
         */

         
                     public org.wso2.carbon.analytics.dashboard.admin.data.Dashboard getDashboard(

                        java.lang.String dashboardID47)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getDashboard46
            
          */
        public void startgetDashboard(

            java.lang.String dashboardID47,

            final org.wso2.carbon.analytics.dashboard.stub.DashboardAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getWidgets50
                
         */

         
                     public org.wso2.carbon.analytics.dashboard.admin.data.Widget[] getWidgets(

                        java.lang.String dataViewID51)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getWidgets50
            
          */
        public void startgetWidgets(

            java.lang.String dataViewID51,

            final org.wso2.carbon.analytics.dashboard.stub.DashboardAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param updateWidget54
                
         */

         
                     public boolean updateWidget(

                        java.lang.String dataViewID55,org.wso2.carbon.analytics.dashboard.admin.data.Widget widget56)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param updateWidget54
            
          */
        public void startupdateWidget(

            java.lang.String dataViewID55,org.wso2.carbon.analytics.dashboard.admin.data.Widget widget56,

            final org.wso2.carbon.analytics.dashboard.stub.DashboardAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param updateDataView59
                
         */

         
                     public boolean updateDataView(

                        org.wso2.carbon.analytics.dashboard.admin.data.DataView dataView60)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param updateDataView59
            
          */
        public void startupdateDataView(

            org.wso2.carbon.analytics.dashboard.admin.data.DataView dataView60,

            final org.wso2.carbon.analytics.dashboard.stub.DashboardAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getDataView63
                
         */

         
                     public org.wso2.carbon.analytics.dashboard.admin.data.DataView getDataView(

                        java.lang.String dataViewID64)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getDataView63
            
          */
        public void startgetDataView(

            java.lang.String dataViewID64,

            final org.wso2.carbon.analytics.dashboard.stub.DashboardAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getWidgetWithDataViewInfo67
                
         */

         
                     public org.wso2.carbon.analytics.dashboard.admin.data.DataView getWidgetWithDataViewInfo(

                        java.lang.String dataViewID68,java.lang.String widgetID69)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getWidgetWithDataViewInfo67
            
          */
        public void startgetWidgetWithDataViewInfo(

            java.lang.String dataViewID68,java.lang.String widgetID69,

            final org.wso2.carbon.analytics.dashboard.stub.DashboardAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param addWidget72
                
         */

         
                     public boolean addWidget(

                        java.lang.String dataViewID73,org.wso2.carbon.analytics.dashboard.admin.data.Widget widget74)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param addWidget72
            
          */
        public void startaddWidget(

            java.lang.String dataViewID73,org.wso2.carbon.analytics.dashboard.admin.data.Widget widget74,

            final org.wso2.carbon.analytics.dashboard.stub.DashboardAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param addDashboard77
                
         */

         
                     public boolean addDashboard(

                        org.wso2.carbon.analytics.dashboard.admin.data.Dashboard dashboard78)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param addDashboard77
            
          */
        public void startaddDashboard(

            org.wso2.carbon.analytics.dashboard.admin.data.Dashboard dashboard78,

            final org.wso2.carbon.analytics.dashboard.stub.DashboardAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getDataViewsInfo81
                
         */

         
                     public org.wso2.carbon.analytics.dashboard.admin.data.DataView[] getDataViewsInfo(

                        )
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getDataViewsInfo81
            
          */
        public void startgetDataViewsInfo(

            

            final org.wso2.carbon.analytics.dashboard.stub.DashboardAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param addWidgetToDashboard84
                
         */

         
                     public boolean addWidgetToDashboard(

                        java.lang.String dashboardID85,org.wso2.carbon.analytics.dashboard.admin.data.WidgetMetaData widget86)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param addWidgetToDashboard84
            
          */
        public void startaddWidgetToDashboard(

            java.lang.String dashboardID85,org.wso2.carbon.analytics.dashboard.admin.data.WidgetMetaData widget86,

            final org.wso2.carbon.analytics.dashboard.stub.DashboardAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    