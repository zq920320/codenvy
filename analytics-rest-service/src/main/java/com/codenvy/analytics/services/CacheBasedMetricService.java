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

import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.services.model.MetricPojo;
import com.codenvy.api.analytics.server.MetricService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@Path("{" + MetricService.PATH_PARAM_NAME + "}")
public class CacheBasedMetricService implements MetricService {

    private static final Set<MetricParameter> sampleParameterSet = new LinkedHashSet<>();

    static {
        sampleParameterSet.add(MetricParameter.FROM_DATE);
        sampleParameterSet.add(MetricParameter.TO_DATE);
    }

    /** {@inheritDoc} */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getValue(@PathParam(PATH_PARAM_NAME) String metricName, @Context UriInfo uriInfo) {
        try {
            Metric metric = MetricFactory.createMetric(metricName);
            String value = metric.getValue(extractContext(uriInfo)).getAsString();

            return Response.status(Response.Status.OK).entity(value).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /** {@inheritDoc} */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("info")
    public Response getInfo(@PathParam(PATH_PARAM_NAME) String metricName, @Context UriInfo uriInfo) {
        try {

            List<MetricPojo> metricPojos = new ArrayList<>();
            if (MetricService.ALL_PATH_ELEMENT.equalsIgnoreCase(metricName)) {
                for (MetricType metricType : MetricType.values()) {
                    Metric metric = MetricFactory.createMetric(metricType);
                    if (sampleParameterSet.equals(metric.getParams())) {
                        metricPojos.add(generateMetricPojo(uriInfo, metricType.name(), metric));
                    }
                }
            } else {
                Metric metric = MetricFactory.createMetric(metricName);
                if (sampleParameterSet.equals(metric.getParams())) {
                    metricPojos.add(generateMetricPojo(uriInfo, metricName, metric));
                }
            }
            return Response.status(Response.Status.OK).entity(metricPojos).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /** Extract the execution context from passed query parameters. */
    private Map<String, String> extractContext(UriInfo info) {
        MultivaluedMap<String, String> parameters = info.getQueryParameters();
        Map<String, String> context = new HashMap<String, String>(parameters.size());

        for (String key : parameters.keySet()) {
            context.put(key, parameters.getFirst(key));
        }

        if (context.get(MetricParameter.FROM_DATE.name()) == null) {
            MetricParameter.FROM_DATE.putDefaultValue(context);
        }

        if (context.get(MetricParameter.TO_DATE.name()) == null) {
            MetricParameter.TO_DATE.putDefaultValue(context);
        }

        return context;
    }

    private MetricPojo generateMetricPojo(UriInfo uriInfo, String metricName, Metric metric) {
        MetricPojo metricPojo = new MetricPojo();
        metricPojo.setName(metricName);
        metricPojo.setDescription(metric.getDescription());
        metricPojo.setLink(uriInfo.getBaseUri() + "/" + metricName);

        return metricPojo;
    }
}
