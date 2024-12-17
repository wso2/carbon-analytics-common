/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.output.adapter.email.internal.util;

import org.apache.commons.logging.Log;

import javax.mail.AuthenticationFailedException;

public class EmailEventAdapterUtil {

    private EmailEventAdapterUtil() {

    }

    public static void logAndDrop(String adapterName, Object event, String message, Throwable e, Log log,
                                  int tenantId) {

        if (message != null) {
            message = message + ", ";
        } else {
            message = "";
        }
        String errorMessage = "Event dropped at Output Adapter '" + adapterName + "' for tenant id '" +
                tenantId + "', " + message + e.getMessage();
        if (e instanceof AuthenticationFailedException) {
            log.warn(errorMessage, e);
        } else {
            log.error(errorMessage, e);
        }
        if (log.isDebugEnabled()) {
            log.debug(
                    "Error at Output Adapter '" + adapterName + "' for tenant id '" + tenantId + "', dropping event: \n"
                            + event, e);
        }
    }
}
