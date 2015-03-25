

/**
 * EventStreamAdminService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v11  Built on : Nov 25, 2014 (03:48:14 IST)
 */

    package org.wso2.carbon.event.stream.stub;

    /*
     *  EventStreamAdminService java interface
     */

    public interface EventStreamAdminService {
          

        /**
          * Auto generated method signature
          * 
                    * @param editEventStreamDefinitionAsString20
                
         */

         
                     public void editEventStreamDefinitionAsString(

                        java.lang.String streamStringDefinition21,java.lang.String oldStreamId22)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param editEventStreamDefinitionAsString20
            
          */
        public void starteditEventStreamDefinitionAsString(

            java.lang.String streamStringDefinition21,java.lang.String oldStreamId22,

            final org.wso2.carbon.event.stream.stub.EventStreamAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getStreamDefinitionAsString24
                
         */

         
                     public java.lang.String getStreamDefinitionAsString(

                        java.lang.String streamId25)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getStreamDefinitionAsString24
            
          */
        public void startgetStreamDefinitionAsString(

            java.lang.String streamId25,

            final org.wso2.carbon.event.stream.stub.EventStreamAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param convertStringToEventStreamDefinitionDto28
                
         */

         
                     public org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto convertStringToEventStreamDefinitionDto(

                        java.lang.String streamStringDefinition29)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param convertStringToEventStreamDefinitionDto28
            
          */
        public void startconvertStringToEventStreamDefinitionDto(

            java.lang.String streamStringDefinition29,

            final org.wso2.carbon.event.stream.stub.EventStreamAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getStreamDetailsForStreamId32
                
         */

         
                     public java.lang.String[] getStreamDetailsForStreamId(

                        java.lang.String streamId33)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getStreamDetailsForStreamId32
            
          */
        public void startgetStreamDetailsForStreamId(

            java.lang.String streamId33,

            final org.wso2.carbon.event.stream.stub.EventStreamAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getStreamNames36
                
         */

         
                     public java.lang.String[] getStreamNames(

                        )
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getStreamNames36
            
          */
        public void startgetStreamNames(

            

            final org.wso2.carbon.event.stream.stub.EventStreamAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param removeEventStreamDefinition39
                
         */

         
                     public void removeEventStreamDefinition(

                        java.lang.String eventStreamName40,java.lang.String eventStreamVersion41)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param removeEventStreamDefinition39
            
          */
        public void startremoveEventStreamDefinition(

            java.lang.String eventStreamName40,java.lang.String eventStreamVersion41,

            final org.wso2.carbon.event.stream.stub.EventStreamAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param editEventStreamDefinitionAsDto43
                
         */

         
                     public void editEventStreamDefinitionAsDto(

                        org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto eventStreamDefinitionDto44,java.lang.String oldStreamId45)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param editEventStreamDefinitionAsDto43
            
          */
        public void starteditEventStreamDefinitionAsDto(

            org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto eventStreamDefinitionDto44,java.lang.String oldStreamId45,

            final org.wso2.carbon.event.stream.stub.EventStreamAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getAllEventStreamDefinitionDto47
                
         */

         
                     public org.wso2.carbon.event.stream.stub.types.EventStreamInfoDto[] getAllEventStreamDefinitionDto(

                        )
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAllEventStreamDefinitionDto47
            
          */
        public void startgetAllEventStreamDefinitionDto(

            

            final org.wso2.carbon.event.stream.stub.EventStreamAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param generateSampleEvent50
                
         */

         
                     public java.lang.String generateSampleEvent(

                        java.lang.String streamId51,java.lang.String eventType52)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param generateSampleEvent50
            
          */
        public void startgenerateSampleEvent(

            java.lang.String streamId51,java.lang.String eventType52,

            final org.wso2.carbon.event.stream.stub.EventStreamAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param addEventStreamDefinitionAsString55
                
         */

         
                     public void addEventStreamDefinitionAsString(

                        java.lang.String streamStringDefinition56)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param addEventStreamDefinitionAsString55
            
          */
        public void startaddEventStreamDefinitionAsString(

            java.lang.String streamStringDefinition56,

            final org.wso2.carbon.event.stream.stub.EventStreamAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getStreamDefinitionDto58
                
         */

         
                     public org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto getStreamDefinitionDto(

                        java.lang.String streamId59)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getStreamDefinitionDto58
            
          */
        public void startgetStreamDefinitionDto(

            java.lang.String streamId59,

            final org.wso2.carbon.event.stream.stub.EventStreamAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param convertEventStreamDefinitionDtoToString62
                
         */

         
                     public java.lang.String convertEventStreamDefinitionDtoToString(

                        org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto eventStreamDefinitionDto63)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param convertEventStreamDefinitionDtoToString62
            
          */
        public void startconvertEventStreamDefinitionDtoToString(

            org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto eventStreamDefinitionDto63,

            final org.wso2.carbon.event.stream.stub.EventStreamAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param addEventStreamDefinitionAsDto66
                
         */

         
                     public void addEventStreamDefinitionAsDto(

                        org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto eventStreamDefinitionDto67)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param addEventStreamDefinitionAsDto66
            
          */
        public void startaddEventStreamDefinitionAsDto(

            org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto eventStreamDefinitionDto67,

            final org.wso2.carbon.event.stream.stub.EventStreamAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    