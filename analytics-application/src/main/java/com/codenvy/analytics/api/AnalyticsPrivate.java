/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.api;


import com.codenvy.analytics.metrics.MetricNotFoundException;
import com.codenvy.analytics.metrics.MetricRestrictionException;
import com.codenvy.analytics.util.Utils;
import com.codenvy.api.analytics.MetricHandler;
import com.codenvy.api.analytics.shared.dto.MetricInfoDTO;
import com.codenvy.api.analytics.shared.dto.MetricInfoListDTO;
import com.codenvy.api.analytics.shared.dto.MetricValueDTO;
import com.codenvy.api.analytics.shared.dto.MetricValueListDTO;
import com.codenvy.api.core.rest.annotations.GenerateLink;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.text.ParseException;
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
public class AnalyticsPrivate {

    private static final Logger LOG = LoggerFactory.getLogger(AnalyticsPrivate.class);

    private final MetricHandler metricHandler;
    private final Utils         utils;

    @Inject
    public AnalyticsPrivate(MetricHandler metricHandler, Utils utils) {
        this.metricHandler = metricHandler;
        this.utils = utils;
    }

    @GenerateLink(rel = "metric value")
    @GET
    @Path("metric/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public Response getValue(@PathParam("name") String metricName,
                             @QueryParam("page") String page,
                             @QueryParam("per_page") String perPage,
                             @Context UriInfo uriInfo,
                             @Context SecurityContext securityContext) {
        try {
            Map<String, String> context = utils.extractParams(uriInfo,
                                                              page,
                                                              perPage,
                                                              securityContext);

            MetricValueDTO value = metricHandler.getValue(metricName, context, uriInfo);
            return Response.status(Response.Status.OK).entity(value).build();
        } catch (ParseException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (MetricNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GenerateLink(rel = "metric value")
    @POST
    @Path("metric/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public Response getValueByJson(@PathParam("name") String metricName,
                                   @QueryParam("page") String page,
                                   @QueryParam("per_page") String perPage,
                                   @Context UriInfo uriInfo,
                                   @Context SecurityContext securityContext,
                                   Map<String, String> parameters) {
        try {
            Map<String, String> context = utils.extractParams(uriInfo,
                                                              page,
                                                              perPage,
                                                              securityContext);

            MetricValueDTO value = metricHandler.getValueByJson(metricName, parameters, context, uriInfo);
            return Response.status(Response.Status.OK).entity(value).build();
        } catch (ParseException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (MetricNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GenerateLink(rel = "metric value")
    @POST
    @Path("metric/{name}/list")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public Response getListValues(@PathParam("name") String metricName,
                                  @Context UriInfo uriInfo,
                                  @Context SecurityContext securityContext,
                                  List<Map<String, String>> parameters) {
        try {
            Map<String, String> context = utils.extractParams(uriInfo, securityContext);
            MetricValueListDTO list = metricHandler.getListValues(metricName, parameters, context, uriInfo);
            return Response.status(Response.Status.OK).entity(list).build();
        } catch (ParseException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (MetricNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GenerateLink(rel = "metric value")
    @GET
    @Path("public-metric/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPublicValue(@PathParam("name") String metricName,
                                   @Context UriInfo uriInfo,
                                   @Context SecurityContext securityContext) {
        try {
            MetricInfoDTO metricInfoDTO = metricHandler.getInfo(metricName, uriInfo);
            if (!utils.isRolesAllowed(metricInfoDTO, securityContext)) {
                throw new MetricRestrictionException("Security violation. User probably hasn't access to the metric");
            }

            Map<String, String> context = utils.extractParams(uriInfo);
            MetricValueDTO value = metricHandler.getValue(metricName, context, uriInfo);
            return Response.status(Response.Status.OK).entity(value).build();
        } catch (ParseException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (MetricNotFoundException e) {
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
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public Response getUserValues(List<String> metricNames,
                                  @Context UriInfo uriInfo,
                                  @Context SecurityContext securityContext) {
        try {
            Map<String, String> context = utils.extractParams(uriInfo,
                                                              securityContext);

            MetricValueListDTO list = metricHandler.getUserValues(metricNames, context, uriInfo);
            return Response.status(Response.Status.OK).entity(list).build();
        } catch (ParseException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (MetricNotFoundException e) {
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
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public Response getInfo(@PathParam("name") String metricName,
                            @Context UriInfo uriInfo) {
        try {
            MetricInfoDTO metricInfoDTO = metricHandler.getInfo(metricName, uriInfo);
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
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public Response getAllInfo(@Context UriInfo uriInfo,
                               @Context SecurityContext securityContext) {
        try {
            MetricInfoListDTO metricInfoListDTO = metricHandler.getAllInfo(uriInfo);

            Iterator<MetricInfoDTO> iterator = metricInfoListDTO.getMetrics().iterator();
            while (iterator.hasNext()) {
                if (!utils.isRolesAllowed(iterator.next(), securityContext)) {
                    iterator.remove();
                }
            }

            return Response.status(Response.Status.OK).entity(metricInfoListDTO).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
