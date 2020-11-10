/**
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
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

package org.wso2.carbon.event.processor.restapi.resources;

import com.google.gson.Gson;
import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.restapi.Constants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.processor.manager.core.api.EventProcessorManagementAPI;
import org.wso2.carbon.event.processor.manager.core.exception.AnalyticsServiceException;
import org.wso2.carbon.event.processor.restapi.UnauthenticatedUserException;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.nio.charset.Charset;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * The Class AnalyticsResource represents the REST APIs for
 * AnalyticsDataService.
 */

@Path(Constants.ResourcePath.ROOT_CONTEXT)
public class AnalyticsResource extends AbstractResource {

    private static final Gson gson = new Gson();
	/** The logger. */
	private static final Log logger = LogFactory.getLog(AnalyticsResource.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * Implements the OPTIONS HTTP method
     * @return The response
     */
    @OPTIONS
    public Response options() {
        return Response.ok().header(HttpHeaders.ALLOW, "GET POST DELETE").build();
    }

	/**
	 * Check if the node is active
     * @param authHeader
	 * @return the response
	 * @throws AnalyticsServiceException
	 */
	@GET
    @Path("processing/is_active_node")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getIsActiveNode(@HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsServiceException {
        authenticate(authHeader);
        logger.info("getIsActiveNode() invoking");
        EventProcessorManagementAPI analyticsDataService = getAnalyticsDataAPIs();
        boolean isActiveNode = analyticsDataService.getIsActiveNode();
        logger.info("getIsActiveNode result: " + isActiveNode);
        return handleResponse(ResponseStatus.SUCCESS, Boolean.toString(isActiveNode));
    }


    /**
     * Persist the state
     * @param authHeader
     * @return the response
     * @throws AnalyticsServiceException
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("processing/persist")
    public Response persist(@HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsServiceException {
        logger.info("Invoking persisting.");
        authenticate(authHeader);
        EventProcessorManagementAPI analyticsDataService = getAnalyticsDataAPIs();
        analyticsDataService.persistStates();
        return handleResponse(ResponseStatus.SUCCESS, "Successfully persisted");
    }

    /**
     * Gets the analytics data service.
     *
     * @return the analytics data service
     * @throws AnalyticsServiceException
     */
    public static EventProcessorManagementAPI getAnalyticsDataAPIs()
            throws AnalyticsServiceException {
        EventProcessorManagementAPI analyticsDataAPI;
        analyticsDataAPI = (EventProcessorManagementAPI) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(EventProcessorManagementAPI.class,
                        null);
        if (analyticsDataAPI == null) {
            throw new AnalyticsServiceException("Event Processor API is not available.");
        }
        return analyticsDataAPI;
    }

    private String authenticate(String authHeader) throws AnalyticsServiceException {

        String username;
        if (authHeader != null && authHeader.startsWith(Constants.BASIC_AUTH_HEADER)) {
            // Authorization: Basic base64credentials
            String base64Credentials = authHeader.substring(Constants.BASIC_AUTH_HEADER.length()).trim();
            String credentials = new String(Base64.decode(base64Credentials),
                    Charset.forName(DEFAULT_CHARSET));
            // credentials = username:password
            final String[] values = credentials.split(":", 2);
            username = values[0];
            String password = values[1];
            if ("".equals(username) || username == null || "".equals(password) || password == null) {
                throw new UnauthenticatedUserException("Username and password cannot be empty");
            }
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            String tenantLessUserName = MultitenantUtils.getTenantAwareUsername(username);
            try {
                // get super tenant context and get realm service which is an osgi service
                RealmService realmService = (RealmService) PrivilegedCarbonContext
                        .getThreadLocalCarbonContext().getOSGiService(RealmService.class, null);
                if (realmService != null) {
                    int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
                    if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
                        throw new UnauthenticatedUserException("Authentication failed - Invalid domain");

                    }
                    // get tenant's user realm
                    UserRealm userRealm = realmService.getTenantUserRealm(tenantId);
                    boolean isAuthenticated = userRealm.getUserStoreManager()
                            .authenticate(tenantLessUserName, password);
                    if (!isAuthenticated) {
                        throw  new UnauthenticatedUserException("User is not authenticated!");
                    } else {
                        return username;
                    }
                }
            } catch (UserStoreException e) {
                throw new AnalyticsServiceException("Error while accessing the user realm of user :"
                        + username, e);
            }
        } else {
            throw new UnauthenticatedUserException("Invalid authentication header");
        }
        return username;
    }
}
