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
import com.codenvy.api.analytics.exception.MetricNotFoundException;
import com.codenvy.api.core.rest.annotations.GenerateLink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Map;


/**
 * Service is responsible for processing REST requests for analytics data.
 * Private API. No authentication is required.
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
    public Response getValue(@PathParam("name") String metricName,
                             @QueryParam("page") String page,
                             @QueryParam("per_page") String perPage,
                             @Context UriInfo uriInfo) {
        try {
            Map<String, String> metricContext = Utils.extractContext(uriInfo, page, perPage);

            MetricValueDTO value = metricHandler.getValue(metricName, metricContext, uriInfo);
            return Response.status(Response.Status.OK).entity(value).build();
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
    public Response getInfo(@PathParam("name") String metricName, @Context UriInfo uriInfo) {
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
    public Response getAllInfo(@Context UriInfo uriInfo) {
        try {
            MetricInfoListDTO metricInfoListDTO = metricHandler.getAllInfo(uriInfo);
            return Response.status(Response.Status.OK).entity(metricInfoListDTO).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
