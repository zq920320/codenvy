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

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@Path("metric")
public class MetricService {

    /** Executes script and returns result in JSON. */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{metric}")
    public Response execute(@PathParam("metric") String metricName, @Context UriInfo info) {
        Map<String, String> context = extractContext(info);
        return getValue(metricName, context);
    }

    /**
     * Extract the execution context from passed query parameters.
     */
    private Map<String, String> extractContext(UriInfo info) {
        MultivaluedMap<String, String> parameters = info.getQueryParameters();
        Map<String, String> context = new HashMap<String, String>(parameters.size());

        for (String key : parameters.keySet()) {
            context.put(key, parameters.getFirst(key));
        }

        return context;
    }

    private Response getValue(String metricName, Map<String, String> context) {
        try {
            Metric metric = MetricFactory.createMetric(metricName);
            String value = metric.getValue(context).getAsString();

            return Response.status(Response.Status.OK).entity(value).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
