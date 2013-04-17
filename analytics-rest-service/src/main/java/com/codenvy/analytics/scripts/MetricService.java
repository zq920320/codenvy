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
package com.codenvy.analytics.scripts;

import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;

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

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@Path("metric")
public class MetricService {

    /** Executes script and returns result in JSON. */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{metric}")
    public Response execute(@PathParam("metric") String metricName, @Context UriInfo info) {
        Map<String, String> params = new HashMap<String, String>();

        MultivaluedMap<String, String> queryParameters = info.getQueryParameters();
        for (String key : queryParameters.keySet()) {
            params.put(key, queryParameters.getFirst(key));
        }

        return getValue(metricName, params);
    }

    private Response getValue(String metricName, Map<String, String> params) {
        ScriptExecutionResult executionResult = new ScriptExecutionResult();

        try {
            Metric metric = MetricFactory.createMetric(metricName);

            executionResult.setResult(metric.getValue(params));
            return Response.status(Response.Status.OK).entity(executionResult).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /** Wraps result in POJO. */
    public class ScriptExecutionResult {
        /** Script execution result. */
        private Object result;

        /** Getter for {@link #result}. */
        public Object getResult() {
            return result;
        }

        /** Setter for {@link #result}. */
        public void setResult(Object result) {
            this.result = result;
        }
    }
}
