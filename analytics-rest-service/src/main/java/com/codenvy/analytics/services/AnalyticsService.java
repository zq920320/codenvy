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

package com.codenvy.analytics.services;

import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.services.model.MetricPojo;
import com.codenvy.api.core.rest.Service;
import com.codenvy.api.core.rest.annotations.GenerateLink;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@Path("analytics")
public class AnalyticsService extends Service {

    @Inject
    private MetricHandler metricHandler;

    @GenerateLink(rel = "metric value")
    @GET
    @Path("metric/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getValue(@PathParam("name") String metricName, @QueryParam("page") String page, @QueryParam("per_page") String perPage,
                             @Context UriInfo uriInfo) {
        try {
            String value = metricHandler.getMetricValue(metricName, extractContext(uriInfo));
            if (value != null) {
                return Response.status(Response.Status.OK).entity(value).build();
            }
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GenerateLink(rel = "metric info")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("metricinfo/{name}")
    public Response getInfo(@PathParam("name") String metricName, @QueryParam("page") String page, @QueryParam("per_page") String perPage,
                            @Context UriInfo uriInfo) {
        try {
            MetricPojo metricPojo = metricHandler.getMetricInfo(metricName);

            if (metricPojo == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            } else {
                injectRefLink(uriInfo, metricPojo);
                return Response.status(Response.Status.OK).entity(metricPojo).build();
            }

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GenerateLink(rel = "all metric info")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("metricinfo")
    public Response getAllInfo(@Context UriInfo uriInfo, @QueryParam("page") String page, @QueryParam("per_page") String perPage) {
        try {
            List<MetricPojo> metricPojos = metricHandler.getAllMetricsInfo();
            injectRefLinks(uriInfo, metricPojos);
            return Response.status(Response.Status.OK).entity(metricPojos).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    private void injectRefLinks(UriInfo uriInfo, List<MetricPojo> metricPojos) {
        for (MetricPojo metricPojo : metricPojos) {
            injectRefLink(uriInfo, metricPojo);
        }
    }

    private void injectRefLink(UriInfo uriInfo, MetricPojo metricPojo) {
        metricPojo.setLink(uriInfo.getBaseUri() + "/analytics/metric/" + metricPojo.getName());
    }

    /** Extract the execution context from passed query parameters. */
    private Map<String, String> extractContext(UriInfo info) {
        MultivaluedMap<String, String> parameters = info.getQueryParameters();
        Map<String, String> context = new HashMap<String, String>(parameters.size());

        for (String key : parameters.keySet()) {
            context.put(key.toUpperCase(), parameters.getFirst(key));
        }

        if (context.get(MetricParameter.FROM_DATE.name()) == null) {
            MetricParameter.FROM_DATE.putDefaultValue(context);
        }

        if (context.get(MetricParameter.TO_DATE.name()) == null) {
            MetricParameter.TO_DATE.putDefaultValue(context);
        }

        return context;
    }
}
