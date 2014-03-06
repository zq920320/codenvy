/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */

package com.codenvy.analytics.api;


import com.codenvy.api.analytics.MetricHandler;
import com.codenvy.api.analytics.Utils;
import com.codenvy.api.analytics.dto.MetricInfoDTO;
import com.codenvy.api.analytics.dto.MetricInfoListDTO;
import com.codenvy.api.analytics.dto.MetricValueDTO;
import com.codenvy.api.analytics.dto.MetricValueListDTO;
import com.codenvy.api.analytics.exception.MetricNotFoundException;
import com.codenvy.api.core.rest.annotations.GenerateLink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.security.Principal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Service is responsible for processing REST requests for analytics data.
 * Private API.
 *
 * @author Anatoliy Bazko
 */
@Path("analytics-private")
@Singleton
public class AnalyticsPrivate {

    private static final Logger LOG = LoggerFactory.getLogger(AnalyticsPrivate.class);

    @Inject
    private MetricHandler metricHandler;

    @GenerateLink(rel = "metric value")
    @GET
    @Path("metric/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"analytics-admin"})
    public Response getValue(@PathParam("name") String metricName,
                             @QueryParam("page") String page,
                             @QueryParam("per_page") String perPage,
                             @Context UriInfo uriInfo,
                             @Context SecurityContext securityContext) {
        try {
            MetricInfoDTO metricInfoDTO = metricHandler.getInfo(metricName, uriInfo);
            if (!isRolesAllowed(metricInfoDTO, securityContext)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            
            Map<String, String> context = Utils.extractContext(uriInfo, page, perPage);
            MetricValueDTO value = metricHandler.getValue(metricName, context, uriInfo);
            return Response.status(Response.Status.OK).entity(value).build();
        } catch (MetricNotFoundException e) {
            LOG.error(e.getMessage(), e);
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @GenerateLink(rel = "list of metric values")
    @POST
    @Path("/metric/user")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"analytics-admin"})
    public Response getUserValues(List<String> metricNames,
                             @Context UriInfo uriInfo,
                             @Context SecurityContext securityContext) {
        try {
            for (String metricName : metricNames) {
                MetricInfoDTO metricInfoDTO = metricHandler.getInfo(metricName, uriInfo);
                if (!isRolesAllowed(metricInfoDTO, securityContext)) {
                    return Response.status(Response.Status.UNAUTHORIZED).build();
                }
            }
            
            Map<String, String> context = Utils.extractContext(uriInfo);
            MetricValueListDTO list = metricHandler.getUserValues(metricNames, context, uriInfo);
            return Response.status(Response.Status.OK).entity(list).build();
        } catch (MetricNotFoundException e) {
            LOG.error(e.getMessage(), e);
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GenerateLink(rel = "metric info")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("metricinfo/{name}")
    @RolesAllowed({"analytics-admin"})
    public Response getInfo(@PathParam("name") String metricName, 
                             @Context UriInfo uriInfo,
                             @Context SecurityContext securityContext) {
        try {
            MetricInfoDTO metricInfoDTO = metricHandler.getInfo(metricName, uriInfo);
            
            if (!isRolesAllowed(metricInfoDTO, securityContext)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            
            return Response.status(Response.Status.OK).entity(metricInfoDTO).build();
        } catch (MetricNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GenerateLink(rel = "all metric info")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("metricinfo")
    @RolesAllowed({"analytics-admin"})
    public Response getAllInfo(@Context UriInfo uriInfo,
                               @Context SecurityContext securityContext) {
        try {
            MetricInfoListDTO metricInfoListDTO = metricHandler.getAllInfo(uriInfo);
            
            Iterator<MetricInfoDTO> iterator = metricInfoListDTO.getMetrics().iterator();
            while (iterator.hasNext()) {
                if (!isRolesAllowed(iterator.next(), securityContext)) {
                    iterator.remove();
                }
            }
            
            return Response.status(Response.Status.OK).entity(metricInfoListDTO).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    private boolean isRolesAllowed(MetricInfoDTO metricInfoDTO, SecurityContext securityContext) {
        Principal principal = securityContext.getUserPrincipal();

        if (principal != null && Utils.isSystemUser(principal.getName())) {
            return true;
        }

        List<String> rolesAllowed = metricInfoDTO.getRolesAllowed();
        if (rolesAllowed.isEmpty()) {
            return true;
        }

        for (String role : rolesAllowed) {
            if (securityContext.isUserInRole(role)) {
                return true;
            }
        }

        return false;
    }
}
