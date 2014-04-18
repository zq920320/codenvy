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

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Expandable;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricNotFoundException;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.services.view.SectionData;
import com.codenvy.analytics.services.view.ViewBuilder;
import com.codenvy.analytics.services.view.ViewData;
import com.codenvy.analytics.util.Utils;
import com.codenvy.api.analytics.dto.MetricValueDTO;
import com.codenvy.dto.server.DtoFactory;
import com.codenvy.dto.server.JsonArrayImpl;

/**
 * @author Alexander Reshetnyak
 * @author Anatoliy Bazko
 */
@Path("view")
@Singleton
public class View {

    private static final Logger LOG = LoggerFactory.getLogger(View.class);

    @Inject
    private ViewBuilder         viewBuilder;

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

            ValueData value = getMetricValue(metricName, com.codenvy.analytics.metrics.Context.valueOf(context));
            MetricValueDTO outputValue = getMetricValueDTO(metricName, value);
            return Response.status(Response.Status.OK).entity(outputValue).build();
        } catch (MetricNotFoundException e) {
            LOG.error(e.getMessage(), e);
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("metric/{name}/expand")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public Response getExpandedMetricValue(@PathParam("name") String metricName,
                                           @QueryParam("page") String page,
                                           @QueryParam("per_page") String perPage,
                                           @Context UriInfo uriInfo,
                                           @Context SecurityContext securityContext) {

        try {
            Map<String, String> context = Utils.extractParams(uriInfo,
                                                              page,
                                                              perPage,
                                                              securityContext);

            ListValueData value = getExpandedValue(metricName, com.codenvy.analytics.metrics.Context.valueOf(context));
            ViewData result = viewBuilder.getViewData(value);
            String json = transformToJson(result);

            return Response.status(Response.Status.OK).entity(json).build();
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
    public Response getViewDataAsCsv(@PathParam("name") String viewName,
                                     @Context UriInfo uriInfo,
                                     @Context SecurityContext securityContext) {
        try {
            Map<String, String> params = Utils.extractParams(uriInfo, securityContext);
            com.codenvy.analytics.metrics.Context context = com.codenvy.analytics.metrics.Context.valueOf(params);

            ViewData result = viewBuilder.getViewData(viewName, context);
            String csv = transformToCsv(result);

            return Response.status(Response.Status.OK).entity(csv).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("{name}/expandable-metric-list")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public Response getViewExpandableViewMetricList(@PathParam("name") String viewName,
                                                    @Context UriInfo uriInfo,
                                                    @Context SecurityContext securityContext) {
        try {
            List<Map<Integer, MetricType>> result = viewBuilder.getViewExpandableMetricMap(viewName);
            String json = transformToJson(result);

            return Response.status(Response.Status.OK).entity(json).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    private ValueData getMetricValue(String metricName, com.codenvy.analytics.metrics.Context context) throws IOException {
        return MetricFactory.getMetric(metricName).getValue(context);
    }

    private ListValueData getExpandedValue(String metricName, com.codenvy.analytics.metrics.Context context) throws IOException,
                                                                                                            ParseException {
        context = viewBuilder.initializeFirstInterval(context);
        
        Expandable expandableMetric = (Expandable)MetricFactory.getMetric(metricName);
        
        return expandableMetric.getExpandedValue(context);
    }

    /**
     * Transforms view data into table in to json format.
     * 
     * @return the resulted format will be: [ [ ["section0-row0-column0", "section0-row0-column1", ...]
     *         ["section0-row1-column0", "section0-row1-column1", ...] ... ], [ ["section1-row0-column0",
     *         "section0-row0-column1", ...] ["section1-row1-column0", "section0-row1-column1", ...] ... ], ... ]
     */
    protected String transformToJson(ViewData data) {
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
     * Transforms view data into table in to csv format.
     * 
     * @return the resulted format will be: section0-row0-column0, section0-row0-column1, ... section0-row1-column0,
     *         section0-row1-column1, ... ... section1-row0-column0, section0-row0-column1, ... section1-row1-column0,
     *         section0-row1-column1, ... ...
     */
    protected String transformToCsv(ViewData data) {
        StringBuilder result = new StringBuilder();

        for (Entry<String, SectionData> sectionEntry : data.entrySet()) {
            for (int i = 0; i < sectionEntry.getValue().size(); i++) {
                List<ValueData> rowData = sectionEntry.getValue().get(i);

                result.append(getCsvRow(rowData)).append("\n");
            }
        }

        result.setLength(result.length() - 1); // remove ended "\n"

        return result.toString();
    }

    /**
     * Transforms Map<sectionNumber, Map<rowNumber, metricType>> map into table in to json format.
     * 
     * @return the resulted format will be: [
     * {"1": "total_factories",   // first section
     *  "2": "created_factories",
     *  ...},
     *  
     * {},                        // second section
     * 
     * {"3": "active_workspaces", // third section
     *  "5": "active_users",
     *  ...},
     *  
     *  ...
     *  ]
     */
    private String transformToJson(List<Map<Integer, MetricType>> list) {
        if (list.size() == 0) {
            return "[]";
        }

        final String METRIC_ROW_PATTERN = "\"%1$s\":\"%2$s\",";

        StringBuilder result = new StringBuilder("[");
        
        for (Map<Integer, MetricType> sectionMetrics: list) {
            if (sectionMetrics.isEmpty()) {
                result.append("{},");
            } else {
                result.append("{");
                
                for (Entry<Integer, MetricType> entry : sectionMetrics.entrySet()) {
                    String rowNumber = entry.getKey().toString();
                    String metricType = entry.getValue().toString().toLowerCase();
                    result.append(String.format(METRIC_ROW_PATTERN, rowNumber, metricType));
                }
                
                // remove ended ","
                result.deleteCharAt(result.length()-1);
                result.append("},");
            }
        }
    
        // remove ended ","
        result.deleteCharAt(result.length()-1);
        result.append("]");

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

        return builder.toString();
    }

    MetricValueDTO getMetricValueDTO(String metricName, ValueData metricValue) {
        MetricValueDTO metricValueDTO = DtoFactory.getInstance().createDto(MetricValueDTO.class);
        metricValueDTO.setName(metricName);
        metricValueDTO.setType(metricValue.getType());
        metricValueDTO.setValue(metricValue.getAsString());

        return metricValueDTO;
    }
}
