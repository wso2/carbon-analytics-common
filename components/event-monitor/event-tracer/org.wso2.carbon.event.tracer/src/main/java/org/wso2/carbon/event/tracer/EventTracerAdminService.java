/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.tracer;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.wso2.carbon.log4j2.plugins.MemoryAppender;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// MemoryAppender should not be used here,
// this should be handled using a logging provider

public class EventTracerAdminService {
    private static final Log log = LogFactory.getLog(EventTracerAdminService.class);
    private static final String EVENT_TRACE_LOGGER = "EVENT_TRACE_LOGGER";
    private static final String EVENT_TRACE_MEMORYAPPENDER = "EVENT_TRACE_MEMORYAPPENDER";

    public String[] getTraceLogs() {
        int amount;
        int DEFAULT_NO_OF_LOGS = 100;
        Logger logger = (Logger) LogManager.getLogger(EVENT_TRACE_LOGGER);
        Map<String, Appender> appenders = logger.getAppenders();
        Appender appender = appenders.get(EVENT_TRACE_MEMORYAPPENDER);
        if (appender instanceof MemoryAppender) {
            MemoryAppender memoryAppender = (MemoryAppender) appender;
            if ((memoryAppender.getCircularQueue() != null)) {
                amount = memoryAppender.getBufferSize();
            } else {
                return new String[]{
                        "--- No trace entries found. " +
                        "You can enable tracing on event receivers, event publishers or execution plans by vising the relevant pages ---"};
            }
            if ((memoryAppender.getCircularQueue().getObjects(amount) == null) ||
                (memoryAppender.getCircularQueue().getObjects(amount).length == 0)) {
                return new String[]{
                        "--- No trace entries found. " +
                        "You can enable tracing on event receivers, event publishers or execution plans by vising the relevant pages ---"};
            }
            Object[] objects;
            if (amount < 1) {
                objects = memoryAppender.getCircularQueue().getObjects(DEFAULT_NO_OF_LOGS);
            } else {
                objects = memoryAppender.getCircularQueue().getObjects(amount);
            }
            String[] resp = new String[objects.length];
            Layout<? extends Serializable> layout = memoryAppender.getLayout();
            for (int i = 0; i < objects.length; i++) {
                LogEvent logEvt = (LogEvent) objects[i];
                if (logEvt != null) {
                    resp[i] = StringEscapeUtils.escapeHtml(String.valueOf(layout.toSerializable(logEvt)));
                }
            }
            return resp;
        } else {
            return new String[]{"The trace log must be configured to use the " +
                                "org.wso2.carbon.logging.appenders.MemoryAppender to view entries through the admin console"};
        }
    }

    public boolean clearTraceLogs() {
        Logger logger = (Logger) LogManager.getLogger(EVENT_TRACE_LOGGER);
        Map<String, Appender> appenders = logger.getAppenders();
        Appender appender = appenders.get(EVENT_TRACE_MEMORYAPPENDER);
        if (appender instanceof MemoryAppender) {
            try {
                MemoryAppender memoryAppender = (MemoryAppender) appender;
                if (memoryAppender.getCircularQueue() != null) {
                    memoryAppender.getCircularQueue().clear();
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

    public String[] searchTraceLog(String keyword,
                                   boolean ignoreCase) throws EventTracerAdminServiceException {
        int DEFAULT_NO_OF_LOGS = 100;
        int definedamonut;
        if (keyword == null) {
            handleException("Key word can not be null");
        }
        if ("ALL".equals(keyword) || "".equals(keyword)) {
            return getTraceLogs();
        }
        Logger logger = (Logger) LogManager.getLogger(EVENT_TRACE_LOGGER);
        Map<String, Appender> appenders = logger.getAppenders();
        Appender appender = appenders.get(EVENT_TRACE_MEMORYAPPENDER);
        if (appender instanceof MemoryAppender) {
            MemoryAppender memoryAppender
                    = (MemoryAppender) appender;
            if ((memoryAppender.getCircularQueue() != null)) {
                definedamonut = memoryAppender.getBufferSize();
            } else {
                return new String[]{
                        "--- No trace entries found for " +
                        " " + keyword + " ---"
                };
            }
            if ((memoryAppender.getCircularQueue().getObjects(definedamonut) == null) ||
                (memoryAppender.getCircularQueue().getObjects(definedamonut).length == 0)) {
                return new String[]{
                        "--- No trace entries found for " +
                        "the " + keyword + " ---"
                };
            }
            Object[] objects;
            if (definedamonut < 1) {
                objects = memoryAppender.getCircularQueue().getObjects(DEFAULT_NO_OF_LOGS);
            } else {
                objects = memoryAppender.getCircularQueue().getObjects(definedamonut);
            }
            Layout layout = memoryAppender.getLayout();
            List<String> resultList = new ArrayList<String>();
            for (Object object : objects) {
                LogEvent logEvt = (LogEvent) object;
                if (logEvt != null) {
                    String result = layout.toSerializable(logEvt).toString();
                    if (result != null) {
                        if (!ignoreCase) {
                            if (result.indexOf(keyword) > -1) {
                                resultList.add(StringEscapeUtils.escapeHtml(result));
                            }
                        } else {
                            if (keyword != null &&
                                result.toLowerCase().indexOf(keyword.toLowerCase()) > -1) {
                                resultList.add(StringEscapeUtils.escapeHtml(result));
                            }
                        }
                    }
                }
            }
            if (resultList.isEmpty()) {
                return new String[]{
                        "--- No trace entries found for " +
                        "the  " + keyword + " ---"
                };
            }
            return resultList.toArray(new String[resultList.size()]);
        } else {
            return new String[]{"The trace log must be configured to use the " +
                                "org.wso2.carbon.logging.appenders.MemoryAppender to view entries through the " +
                                "admin console"};
        }

    }

    public String[] getLogs() {
        int DEFAULT_NO_OF_LOGS = 100;
        int definedamount;
        Logger logger = (Logger) LogManager.getRootLogger();
        Map<String, Appender> appenders = logger.getAppenders();
        Appender appender = appenders.get("LOG_MEMORYAPPENDER");

        if (appender instanceof MemoryAppender) {
            MemoryAppender memoryAppender = (MemoryAppender) appender;
            if ((memoryAppender.getCircularQueue() != null)) {
                definedamount = memoryAppender.getBufferSize();
            } else {
                return new String[]{
                        "--- No log entries found. " +
                        "You may try increasing the log level ---"
                };
            }
            if ((memoryAppender.getCircularQueue().getObjects(definedamount) == null) ||
                (memoryAppender.getCircularQueue().getObjects(definedamount).length == 0)) {
                return new String[]{
                        "--- No log entries found. " +
                        "You may try increasing the log level ---"
                };
            }
            Object[] objects;
            if (definedamount < 1) {
                objects = memoryAppender.getCircularQueue().getObjects(DEFAULT_NO_OF_LOGS);
            } else {
                objects = memoryAppender.getCircularQueue().getObjects(definedamount);
            }
            String[] resp = new String[objects.length];
            Layout layout = memoryAppender.getLayout();
            for (int i = 0; i < objects.length; i++) {
                LogEvent logEvt = (LogEvent) objects[i];
                if (logEvt != null) {
                    resp[i] = StringEscapeUtils.escapeHtml(layout.toSerializable(logEvt).toString());
                }
            }
            return resp;
        } else {
            return new String[]{"The log must be configured to use the " +
                                "org.wso2.carbon.logging.appenders.MemoryAppender to view entries on the admin console"};
        }
    }

    private void handleException(String msg) throws EventTracerAdminServiceException {
        log.error(msg);
        throw new EventTracerAdminServiceException(msg);
    }
}
