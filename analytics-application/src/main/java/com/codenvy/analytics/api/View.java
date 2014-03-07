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

import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.MetricNotFoundException;
import com.codenvy.analytics.services.view.SectionData;
import com.codenvy.analytics.services.view.ViewBuilder;
import com.codenvy.analytics.services.view.ViewData;
import com.codenvy.analytics.util.Utils;
import com.codenvy.api.analytics.MetricHandler;
import com.codenvy.api.analytics.dto.MetricValueDTO;
import com.codenvy.dto.server.JsonStringMapImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Reshetnyak
 * @author Anatoliy Bazko
 */
@Path("view")
@Singleton
public class View {

    private static final Logger        LOG            = LoggerFactory.getLogger(View.class);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("00");

    @Inject
    private ViewBuilder viewBuilder;

    @Inject
    private MetricHandler metricHandler;

    @GET
    @Path("metric/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public Response getMetricValue(@PathParam("name") String metricName,
                                   @QueryParam("page") String page,
                                   @QueryParam("per_page") String perPage,
                                   @Context UriInfo uriInfo,
                                   @Context SecurityContext securityContext) {

        try {
            Map<String, String> context = Utils.extractContext(uriInfo,
                                                               page,
                                                               perPage,
                                                               securityContext);

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

    @GET
    @Path("{name}")
    @Produces({"application/json"})
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public Response getViewData(@PathParam("name") String name,
                                @Context UriInfo uriInfo,
                                @Context SecurityContext securityContext) {
        try {
            Map<String, String> context = Utils.extractContext(uriInfo, securityContext);

            ViewData result = viewBuilder.getViewData(name, context);
            String json = transform(result).toJson();

            return Response.status(Response.Status.OK).entity(json).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * Transforms map into json format.
     *
     * @param data
     *         the view data
     * @return the resulted format will be: {"t00" : {"r00" : {"c00" : ...} ...} ...}, where txx - the sequences
     * numbers of tables, rxx - the sequences numbers of rows and cxx - the sequences numbers of columns.
     */
    private JsonStringMapImpl transform(ViewData data) {
        Map<String, Object> result = new LinkedHashMap<>(data.size());

        int t = 0;
        for (Map.Entry<String, SectionData> sectionEntry : data.entrySet()) {
            Map<String, Object> newSectionData = new LinkedHashMap<>(sectionEntry.getValue().size());

            for (int i = 0; i < sectionEntry.getValue().size(); i++) {
                List<ValueData> rowData = sectionEntry.getValue().get(i);
                Map<String, String> newRowData = new LinkedHashMap<>(rowData.size());

                for (int j = 0; j < rowData.size(); j++) {
                    newRowData.put("c" + DECIMAL_FORMAT.format(j), rowData.get(j).getAsString());
                }

                newSectionData.put("r" + DECIMAL_FORMAT.format(i), newRowData);
            }

            result.put("t" + DECIMAL_FORMAT.format(t++), newSectionData);
        }

        return new JsonStringMapImpl(result);
    }
}
