
/**
 * EventStreamDefinitionDto.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v11  Built on : Nov 25, 2014 (03:48:43 IST)
 */

            
                package org.wso2.carbon.event.stream.stub.types;
            

            /**
            *  EventStreamDefinitionDto bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class EventStreamDefinitionDto
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = EventStreamDefinitionDto
                Namespace URI = http://internal.admin.stream.event.carbon.wso2.org/xsd
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for CorrelationData
                        * This was an Array!
                        */

                        
                                    protected org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[] localCorrelationData ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCorrelationDataTracker = false ;

                           public boolean isCorrelationDataSpecified(){
                               return localCorrelationDataTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[]
                           */
                           public  org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[] getCorrelationData(){
                               return localCorrelationData;
                           }

                           
                        


                               
                              /**
                               * validate the array for CorrelationData
                               */
                              protected void validateCorrelationData(org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param CorrelationData
                              */
                              public void setCorrelationData(org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[] param){
                              
                                   validateCorrelationData(param);

                               localCorrelationDataTracker = true;
                                      
                                      this.localCorrelationData=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto
                             */
                             public void addCorrelationData(org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto param){
                                   if (localCorrelationData == null){
                                   localCorrelationData = new org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[]{};
                                   }

                            
                                 //update the setting tracker
                                localCorrelationDataTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localCorrelationData);
                               list.add(param);
                               this.localCorrelationData =
                             (org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[])list.toArray(
                            new org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[list.size()]);

                             }
                             

                        /**
                        * field for Description
                        */

                        
                                    protected java.lang.String localDescription ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDescriptionTracker = false ;

                           public boolean isDescriptionSpecified(){
                               return localDescriptionTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDescription(){
                               return localDescription;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Description
                               */
                               public void setDescription(java.lang.String param){
                            localDescriptionTracker = true;
                                   
                                            this.localDescription=param;
                                    

                               }
                            

                        /**
                        * field for Editable
                        */

                        
                                    protected boolean localEditable ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localEditableTracker = false ;

                           public boolean isEditableSpecified(){
                               return localEditableTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getEditable(){
                               return localEditable;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Editable
                               */
                               public void setEditable(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localEditableTracker =
                                       true;
                                   
                                            this.localEditable=param;
                                    

                               }
                            

                        /**
                        * field for MetaData
                        * This was an Array!
                        */

                        
                                    protected org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[] localMetaData ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localMetaDataTracker = false ;

                           public boolean isMetaDataSpecified(){
                               return localMetaDataTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[]
                           */
                           public  org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[] getMetaData(){
                               return localMetaData;
                           }

                           
                        


                               
                              /**
                               * validate the array for MetaData
                               */
                              protected void validateMetaData(org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param MetaData
                              */
                              public void setMetaData(org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[] param){
                              
                                   validateMetaData(param);

                               localMetaDataTracker = true;
                                      
                                      this.localMetaData=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto
                             */
                             public void addMetaData(org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto param){
                                   if (localMetaData == null){
                                   localMetaData = new org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[]{};
                                   }

                            
                                 //update the setting tracker
                                localMetaDataTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localMetaData);
                               list.add(param);
                               this.localMetaData =
                             (org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[])list.toArray(
                            new org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[list.size()]);

                             }
                             

                        /**
                        * field for Name
                        */

                        
                                    protected java.lang.String localName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localNameTracker = false ;

                           public boolean isNameSpecified(){
                               return localNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getName(){
                               return localName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Name
                               */
                               public void setName(java.lang.String param){
                            localNameTracker = true;
                                   
                                            this.localName=param;
                                    

                               }
                            

                        /**
                        * field for NickName
                        */

                        
                                    protected java.lang.String localNickName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localNickNameTracker = false ;

                           public boolean isNickNameSpecified(){
                               return localNickNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getNickName(){
                               return localNickName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param NickName
                               */
                               public void setNickName(java.lang.String param){
                            localNickNameTracker = true;
                                   
                                            this.localNickName=param;
                                    

                               }
                            

                        /**
                        * field for PayloadData
                        * This was an Array!
                        */

                        
                                    protected org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[] localPayloadData ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPayloadDataTracker = false ;

                           public boolean isPayloadDataSpecified(){
                               return localPayloadDataTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[]
                           */
                           public  org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[] getPayloadData(){
                               return localPayloadData;
                           }

                           
                        


                               
                              /**
                               * validate the array for PayloadData
                               */
                              protected void validatePayloadData(org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param PayloadData
                              */
                              public void setPayloadData(org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[] param){
                              
                                   validatePayloadData(param);

                               localPayloadDataTracker = true;
                                      
                                      this.localPayloadData=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto
                             */
                             public void addPayloadData(org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto param){
                                   if (localPayloadData == null){
                                   localPayloadData = new org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[]{};
                                   }

                            
                                 //update the setting tracker
                                localPayloadDataTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localPayloadData);
                               list.add(param);
                               this.localPayloadData =
                             (org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[])list.toArray(
                            new org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[list.size()]);

                             }
                             

                        /**
                        * field for StreamDefinitionString
                        */

                        
                                    protected java.lang.String localStreamDefinitionString ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localStreamDefinitionStringTracker = false ;

                           public boolean isStreamDefinitionStringSpecified(){
                               return localStreamDefinitionStringTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getStreamDefinitionString(){
                               return localStreamDefinitionString;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param StreamDefinitionString
                               */
                               public void setStreamDefinitionString(java.lang.String param){
                            localStreamDefinitionStringTracker = true;
                                   
                                            this.localStreamDefinitionString=param;
                                    

                               }
                            

                        /**
                        * field for Version
                        */

                        
                                    protected java.lang.String localVersion ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localVersionTracker = false ;

                           public boolean isVersionSpecified(){
                               return localVersionTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getVersion(){
                               return localVersion;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Version
                               */
                               public void setVersion(java.lang.String param){
                            localVersionTracker = true;
                                   
                                            this.localVersion=param;
                                    

                               }
                            

     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement (
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{


        
               org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName);
               return factory.createOMElement(dataSource,parentQName);
            
        }

         public void serialize(final javax.xml.namespace.QName parentQName,
                                       javax.xml.stream.XMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                           serialize(parentQName,xmlWriter,false);
         }

         public void serialize(final javax.xml.namespace.QName parentQName,
                               javax.xml.stream.XMLStreamWriter xmlWriter,
                               boolean serializeType)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
            
                


                java.lang.String prefix = null;
                java.lang.String namespace = null;
                

                    prefix = parentQName.getPrefix();
                    namespace = parentQName.getNamespaceURI();
                    writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);
                
                  if (serializeType){
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://internal.admin.stream.event.carbon.wso2.org/xsd");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":EventStreamDefinitionDto",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "EventStreamDefinitionDto",
                           xmlWriter);
                   }

               
                   }
                if (localCorrelationDataTracker){
                                       if (localCorrelationData!=null){
                                            for (int i = 0;i < localCorrelationData.length;i++){
                                                if (localCorrelationData[i] != null){
                                                 localCorrelationData[i].serialize(new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd","correlationData"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://internal.admin.stream.event.carbon.wso2.org/xsd", "correlationData", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://internal.admin.stream.event.carbon.wso2.org/xsd", "correlationData", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localDescriptionTracker){
                                    namespace = "http://internal.admin.stream.event.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "description", xmlWriter);
                             

                                          if (localDescription==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDescription);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localEditableTracker){
                                    namespace = "http://internal.admin.stream.event.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "editable", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("editable cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localEditable));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localMetaDataTracker){
                                       if (localMetaData!=null){
                                            for (int i = 0;i < localMetaData.length;i++){
                                                if (localMetaData[i] != null){
                                                 localMetaData[i].serialize(new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd","metaData"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://internal.admin.stream.event.carbon.wso2.org/xsd", "metaData", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://internal.admin.stream.event.carbon.wso2.org/xsd", "metaData", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localNameTracker){
                                    namespace = "http://internal.admin.stream.event.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "name", xmlWriter);
                             

                                          if (localName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localNickNameTracker){
                                    namespace = "http://internal.admin.stream.event.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "nickName", xmlWriter);
                             

                                          if (localNickName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localNickName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPayloadDataTracker){
                                       if (localPayloadData!=null){
                                            for (int i = 0;i < localPayloadData.length;i++){
                                                if (localPayloadData[i] != null){
                                                 localPayloadData[i].serialize(new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd","payloadData"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://internal.admin.stream.event.carbon.wso2.org/xsd", "payloadData", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://internal.admin.stream.event.carbon.wso2.org/xsd", "payloadData", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localStreamDefinitionStringTracker){
                                    namespace = "http://internal.admin.stream.event.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "streamDefinitionString", xmlWriter);
                             

                                          if (localStreamDefinitionString==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localStreamDefinitionString);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localVersionTracker){
                                    namespace = "http://internal.admin.stream.event.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "version", xmlWriter);
                             

                                          if (localVersion==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localVersion);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                    xmlWriter.writeEndElement();
               

        }

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://internal.admin.stream.event.carbon.wso2.org/xsd")){
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * Utility method to write an element start tag.
         */
        private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                       javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
            if (writerPrefix != null) {
                xmlWriter.writeStartElement(namespace, localPart);
            } else {
                if (namespace.length() == 0) {
                    prefix = "";
                } else if (prefix == null) {
                    prefix = generatePrefix(namespace);
                }

                xmlWriter.writeStartElement(prefix, localPart, namespace);
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
        }
        
        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                    java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            xmlWriter.writeAttribute(namespace,attName,attValue);
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                    java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName,attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace,attName,attValue);
            }
        }


           /**
             * Util method to write an attribute without the ns prefix
             */
            private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                             javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

                java.lang.String attributeNamespace = qname.getNamespaceURI();
                java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                if (attributePrefix == null) {
                    attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                }
                java.lang.String attributeValue;
                if (attributePrefix.trim().length() > 0) {
                    attributeValue = attributePrefix + ":" + qname.getLocalPart();
                } else {
                    attributeValue = qname.getLocalPart();
                }

                if (namespace.equals("")) {
                    xmlWriter.writeAttribute(attName, attributeValue);
                } else {
                    registerPrefix(xmlWriter, namespace);
                    xmlWriter.writeAttribute(namespace, attName, attributeValue);
                }
            }
        /**
         *  method to handle Qnames
         */

        private void writeQName(javax.xml.namespace.QName qname,
                                javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();
            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix,namespaceURI);
                }

                if (prefix.trim().length() > 0){
                    xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                }

            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

            if (qnames != null) {
                // we have to store this data until last moment since it is not possible to write any
                // namespace data after writing the charactor data
                java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                java.lang.String namespaceURI = null;
                java.lang.String prefix = null;

                for (int i = 0; i < qnames.length; i++) {
                    if (i > 0) {
                        stringToWrite.append(" ");
                    }
                    namespaceURI = qnames[i].getNamespaceURI();
                    if (namespaceURI != null) {
                        prefix = xmlWriter.getPrefix(namespaceURI);
                        if ((prefix == null) || (prefix.length() == 0)) {
                            prefix = generatePrefix(namespaceURI);
                            xmlWriter.writeNamespace(prefix, namespaceURI);
                            xmlWriter.setPrefix(prefix,namespaceURI);
                        }

                        if (prefix.trim().length() > 0){
                            stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                    }
                }
                xmlWriter.writeCharacters(stringToWrite.toString());
            }

        }


        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
            java.lang.String prefix = xmlWriter.getPrefix(namespace);
            if (prefix == null) {
                prefix = generatePrefix(namespace);
                while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                    prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                }
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            return prefix;
        }


  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                    throws org.apache.axis2.databinding.ADBException{


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localCorrelationDataTracker){
                             if (localCorrelationData!=null) {
                                 for (int i = 0;i < localCorrelationData.length;i++){

                                    if (localCorrelationData[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd",
                                                                          "correlationData"));
                                         elementList.add(localCorrelationData[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd",
                                                                          "correlationData"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd",
                                                                          "correlationData"));
                                        elementList.add(localCorrelationData);
                                    
                             }

                        } if (localDescriptionTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd",
                                                                      "description"));
                                 
                                         elementList.add(localDescription==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDescription));
                                    } if (localEditableTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd",
                                                                      "editable"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localEditable));
                            } if (localMetaDataTracker){
                             if (localMetaData!=null) {
                                 for (int i = 0;i < localMetaData.length;i++){

                                    if (localMetaData[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd",
                                                                          "metaData"));
                                         elementList.add(localMetaData[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd",
                                                                          "metaData"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd",
                                                                          "metaData"));
                                        elementList.add(localMetaData);
                                    
                             }

                        } if (localNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd",
                                                                      "name"));
                                 
                                         elementList.add(localName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localName));
                                    } if (localNickNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd",
                                                                      "nickName"));
                                 
                                         elementList.add(localNickName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localNickName));
                                    } if (localPayloadDataTracker){
                             if (localPayloadData!=null) {
                                 for (int i = 0;i < localPayloadData.length;i++){

                                    if (localPayloadData[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd",
                                                                          "payloadData"));
                                         elementList.add(localPayloadData[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd",
                                                                          "payloadData"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd",
                                                                          "payloadData"));
                                        elementList.add(localPayloadData);
                                    
                             }

                        } if (localStreamDefinitionStringTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd",
                                                                      "streamDefinitionString"));
                                 
                                         elementList.add(localStreamDefinitionString==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStreamDefinitionString));
                                    } if (localVersionTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd",
                                                                      "version"));
                                 
                                         elementList.add(localVersion==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localVersion));
                                    }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{

        
        

        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static EventStreamDefinitionDto parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            EventStreamDefinitionDto object =
                new EventStreamDefinitionDto();

            int event;
            java.lang.String nillableValue = null;
            java.lang.String prefix ="";
            java.lang.String namespaceuri ="";
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = null;
                    if (fullTypeName.indexOf(":") > -1){
                        nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    }
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    
                            if (!"EventStreamDefinitionDto".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (EventStreamDefinitionDto)org.wso2.carbon.event.stream.stub.types.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                        java.util.ArrayList list1 = new java.util.ArrayList();
                    
                        java.util.ArrayList list4 = new java.util.ArrayList();
                    
                        java.util.ArrayList list7 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd","correlationData").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list1.add(null);
                                                              reader.next();
                                                          } else {
                                                        list1.add(org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone1 = false;
                                                        while(!loopDone1){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone1 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd","correlationData").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list1.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list1.add(org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone1 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setCorrelationData((org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto.class,
                                                                list1));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd","description").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDescription(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd","editable").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"editable" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setEditable(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd","metaData").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list4.add(null);
                                                              reader.next();
                                                          } else {
                                                        list4.add(org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone4 = false;
                                                        while(!loopDone4){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone4 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd","metaData").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list4.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list4.add(org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone4 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setMetaData((org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto.class,
                                                                list4));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd","name").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd","nickName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setNickName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd","payloadData").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list7.add(null);
                                                              reader.next();
                                                          } else {
                                                        list7.add(org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone7 = false;
                                                        while(!loopDone7){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone7 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd","payloadData").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list7.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list7.add(org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone7 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setPayloadData((org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto.class,
                                                                list7));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd","streamDefinitionString").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setStreamDefinitionString(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://internal.admin.stream.event.carbon.wso2.org/xsd","version").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setVersion(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                  
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            
                                if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                            



            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
    