
/**
 * EventStreamAdminServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v11  Built on : Nov 25, 2014 (03:48:14 IST)
 */

    package org.wso2.carbon.event.stream.stub;

    /**
     *  EventStreamAdminServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class EventStreamAdminServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public EventStreamAdminServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public EventStreamAdminServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for editEventStreamDefinitionAsString method
            * override this method for handling normal response from editEventStreamDefinitionAsString operation
            */
           public void receiveResulteditEventStreamDefinitionAsString(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from editEventStreamDefinitionAsString operation
           */
            public void receiveErroreditEventStreamDefinitionAsString(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getStreamDefinitionAsString method
            * override this method for handling normal response from getStreamDefinitionAsString operation
            */
           public void receiveResultgetStreamDefinitionAsString(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getStreamDefinitionAsString operation
           */
            public void receiveErrorgetStreamDefinitionAsString(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for convertStringToEventStreamDefinitionDto method
            * override this method for handling normal response from convertStringToEventStreamDefinitionDto operation
            */
           public void receiveResultconvertStringToEventStreamDefinitionDto(
                    org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from convertStringToEventStreamDefinitionDto operation
           */
            public void receiveErrorconvertStringToEventStreamDefinitionDto(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getStreamDetailsForStreamId method
            * override this method for handling normal response from getStreamDetailsForStreamId operation
            */
           public void receiveResultgetStreamDetailsForStreamId(
                    java.lang.String[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getStreamDetailsForStreamId operation
           */
            public void receiveErrorgetStreamDetailsForStreamId(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getStreamNames method
            * override this method for handling normal response from getStreamNames operation
            */
           public void receiveResultgetStreamNames(
                    java.lang.String[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getStreamNames operation
           */
            public void receiveErrorgetStreamNames(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for removeEventStreamDefinition method
            * override this method for handling normal response from removeEventStreamDefinition operation
            */
           public void receiveResultremoveEventStreamDefinition(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from removeEventStreamDefinition operation
           */
            public void receiveErrorremoveEventStreamDefinition(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for editEventStreamDefinitionAsDto method
            * override this method for handling normal response from editEventStreamDefinitionAsDto operation
            */
           public void receiveResulteditEventStreamDefinitionAsDto(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from editEventStreamDefinitionAsDto operation
           */
            public void receiveErroreditEventStreamDefinitionAsDto(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAllEventStreamDefinitionDto method
            * override this method for handling normal response from getAllEventStreamDefinitionDto operation
            */
           public void receiveResultgetAllEventStreamDefinitionDto(
                    org.wso2.carbon.event.stream.stub.types.EventStreamInfoDto[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllEventStreamDefinitionDto operation
           */
            public void receiveErrorgetAllEventStreamDefinitionDto(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for generateSampleEvent method
            * override this method for handling normal response from generateSampleEvent operation
            */
           public void receiveResultgenerateSampleEvent(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from generateSampleEvent operation
           */
            public void receiveErrorgenerateSampleEvent(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for addEventStreamDefinitionAsString method
            * override this method for handling normal response from addEventStreamDefinitionAsString operation
            */
           public void receiveResultaddEventStreamDefinitionAsString(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from addEventStreamDefinitionAsString operation
           */
            public void receiveErroraddEventStreamDefinitionAsString(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getStreamDefinitionDto method
            * override this method for handling normal response from getStreamDefinitionDto operation
            */
           public void receiveResultgetStreamDefinitionDto(
                    org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getStreamDefinitionDto operation
           */
            public void receiveErrorgetStreamDefinitionDto(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for convertEventStreamDefinitionDtoToString method
            * override this method for handling normal response from convertEventStreamDefinitionDtoToString operation
            */
           public void receiveResultconvertEventStreamDefinitionDtoToString(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from convertEventStreamDefinitionDtoToString operation
           */
            public void receiveErrorconvertEventStreamDefinitionDtoToString(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for addEventStreamDefinitionAsDto method
            * override this method for handling normal response from addEventStreamDefinitionAsDto operation
            */
           public void receiveResultaddEventStreamDefinitionAsDto(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from addEventStreamDefinitionAsDto operation
           */
            public void receiveErroraddEventStreamDefinitionAsDto(java.lang.Exception e) {
            }
                


    }
    