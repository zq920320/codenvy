/*
 *    Copyright (C) 2013 eXo Platform SAS.
 *
 *    This is free software; you can redistribute it and/or modify it
 *    under the terms of the GNU Lesser General Public License as
 *    published by the Free Software Foundation; either version 2.1 of
 *    the License, or (at your option) any later version.
 *
 *    This software is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this software; if not, write to the Free
 *    Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *    02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
