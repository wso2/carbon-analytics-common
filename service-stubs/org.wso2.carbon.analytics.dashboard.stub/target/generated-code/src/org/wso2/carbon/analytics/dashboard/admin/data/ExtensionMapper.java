
/**
 * ExtensionMapper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v11  Built on : Nov 25, 2014 (03:48:43 IST)
 */

        
            package org.wso2.carbon.analytics.dashboard.admin.data;
        
            /**
            *  ExtensionMapper class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://data.admin.dashboard.analytics.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "DataView".equals(typeName)){
                   
                            return  org.wso2.carbon.analytics.dashboard.admin.data.DataView.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://data.admin.dashboard.analytics.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "WidgetDimensions".equals(typeName)){
                   
                            return  org.wso2.carbon.analytics.dashboard.admin.data.WidgetDimensions.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://data.admin.dashboard.analytics.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "WidgetMetaData".equals(typeName)){
                   
                            return  org.wso2.carbon.analytics.dashboard.admin.data.WidgetMetaData.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://data.admin.dashboard.analytics.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "Column".equals(typeName)){
                   
                            return  org.wso2.carbon.analytics.dashboard.admin.data.Column.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://data.admin.dashboard.analytics.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "Dashboard".equals(typeName)){
                   
                            return  org.wso2.carbon.analytics.dashboard.admin.data.Dashboard.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://data.admin.dashboard.analytics.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "Widget".equals(typeName)){
                   
                            return  org.wso2.carbon.analytics.dashboard.admin.data.Widget.Factory.parse(reader);
                        

                  }

              
             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    