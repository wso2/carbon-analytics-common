
/**
 * ExtensionMapper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v11  Built on : Nov 25, 2014 (03:48:43 IST)
 */

        
            package org.wso2.carbon.event.stream.stub.types;
        
            /**
            *  ExtensionMapper class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://internal.admin.stream.event.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "EventStreamDefinitionDto".equals(typeName)){
                   
                            return  org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://internal.admin.stream.event.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "EventStreamInfoDto".equals(typeName)){
                   
                            return  org.wso2.carbon.event.stream.stub.types.EventStreamInfoDto.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://internal.admin.stream.event.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "EventStreamAttributeDto".equals(typeName)){
                   
                            return  org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto.Factory.parse(reader);
                        

                  }

              
             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    