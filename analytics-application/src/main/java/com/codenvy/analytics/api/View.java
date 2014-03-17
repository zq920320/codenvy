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
import com.codenvy.dto.server.JsonArrayImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Alexander Reshetnyak
 * @author Anatoliy Bazko
 */
@Path("view")
@Singleton
public class View {

    private static final Logger LOG = LoggerFactory.getLogger(View.class);

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
            Map<String, String> context = Utils.extractParams(uriInfo,
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
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public Response getViewDataAsJson(@PathParam("name") String name,
                                      @Context UriInfo uriInfo,
                                      @Context SecurityContext securityContext) {
        try {
            Map<String, String> params = Utils.extractParams(uriInfo, securityContext);
            com.codenvy.analytics.metrics.Context context = com.codenvy.analytics.metrics.Context.valueOf(params);

            ViewData result = viewBuilder.getViewData(name, context);
            String json = transformToJson(result);

            return Response.status(Response.Status.OK).entity(json).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("{name}.csv")
    @Produces({"text/csv"})
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public Response getViewDataAsCsv(@PathParam("name") String name,
                                     @Context UriInfo uriInfo,
                                     @Context SecurityContext securityContext) {
        try {
            Map<String, String> params = Utils.extractParams(uriInfo, securityContext);
            com.codenvy.analytics.metrics.Context context = com.codenvy.analytics.metrics.Context.valueOf(params);

            ViewData result = viewBuilder.getViewData(name, context);
            String csv = transformToCsv(result);

            return Response.status(Response.Status.OK).entity(csv).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * Transforms view data into table in json format.
     *
     * @return the resulted format will be:
     * [
     * [
     * ["section0-row0-column0", "section0-row0-column1", ...]
     * ["section0-row1-column0", "section0-row1-column1", ...]
     * ...
     * ],
     * [
     * ["section1-row0-column0", "section0-row0-column1", ...]
     * ["section1-row1-column0", "section0-row1-column1", ...]
     * ...
     * ],
     * ...
     * ]
     */
    private String transformToJson(ViewData data) {
        List<LinkedHashSet<Object>> result = new ArrayList<>(data.size());

        for (Entry<String, SectionData> sectionEntry : data.entrySet()) {
            LinkedHashSet<Object> newSectionData = new LinkedHashSet<>(sectionEntry.getValue().size());

            for (int i = 0; i < sectionEntry.getValue().size(); i++) {
                List<ValueData> rowData = sectionEntry.getValue().get(i);
                List<String> newRowData = new ArrayList<>(rowData.size());

                for (int j = 0; j < rowData.size(); j++) {
                    newRowData.add(rowData.get(j).getAsString());
                }

                newSectionData.add(newRowData);
            }

            result.add(newSectionData);
        }

        return new JsonArrayImpl<>(result).toJson();
    }

    /**
     * Transforms view data into table in csv format.
     *
     * @return the resulted format will be:
     * section0-row0-column0, section0-row0-column1, ...
     * section0-row1-column0, section0-row1-column1, ...
     * ...
     * section1-row0-column0, section0-row0-column1, ...
     * section1-row1-column0, section0-row1-column1, ...
     * ...
     */
    private String transformToCsv(ViewData data) {
        StringBuilder result = new StringBuilder();

        for (Entry<String, SectionData> sectionEntry : data.entrySet()) {
            for (int i = 0; i < sectionEntry.getValue().size(); i++) {
                List<ValueData> rowData = sectionEntry.getValue().get(i);

                result.append(getCsvRow(rowData));
            }
        }

        return result.toString();
    }


    public String getCsvRow(List<ValueData> data) {
        StringBuilder builder = new StringBuilder();

        for (ValueData valueData : data) {
            if (builder.length() != 0) {
                builder.append(',');
            }

            builder.append('\"');
            builder.append(valueData.getAsString().replace("\"", "\"\""));
            builder.append('\"');
        }
        builder.append('\n');

        return builder.toString();
    }
}
