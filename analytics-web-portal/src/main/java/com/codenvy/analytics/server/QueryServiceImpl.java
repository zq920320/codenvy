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


package com.codenvy.analytics.server;

import com.codenvy.analytics.client.QueryService;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.server.vew.template.Display;
import com.codenvy.analytics.server.vew.template.MetricRow;
import com.codenvy.analytics.server.vew.template.Row;
import com.codenvy.analytics.server.vew.template.Table;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class QueryServiceImpl extends RemoteServiceServlet implements QueryService {

    private static final Logger  LOGGER       = LoggerFactory.getLogger(QueryServiceImpl.class);
    private static final String  VIEW_QUERY   = "view/query.xml";
    private static final String  SERVER_ERROR = "Server Error";

    /**
     * Query view layout.
     */
    private static final Display display      = Display.initialize(VIEW_QUERY);

    /** {@inheritedDoc} */
    public Map<String, String> getMetricTypes() {
        Map<String, String> types = new LinkedHashMap<String, String>();

        for (Row row : display.getLayout().get(0).getRows()) {
            if (row instanceof MetricRow) {
                MetricRow metricRow = (MetricRow)row;
                Metric metric = metricRow.getMetric();
                types.put(metricRow.getTitle(), metric.getType().name());
            }
        }

        return types;
    }

    /** {@inheritedDoc} */
    public Map<String, String> getMetricParameters(String metricTitle) throws IOException {
        Map<String, String> parameters = new HashMap<String, String>();

        Metric metric = MetricFactory.createMetric(metricTitle);

        for (MetricParameter sp : metric.getParams()) {
            parameters.put(sp.name(), sp.getDefaultValue());
        }

        return parameters;
    }

    /** {@inheritedDoc} */
    public String calculateMetric(String metricTitle, Map<String, String> context) {
        Metric metric;
        try {
            metric = MetricFactory.createMetric(metricTitle);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            return SERVER_ERROR;
        }

        try {
            Utils.validate(context, metric);
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage(), e);
            return e.getMessage();
        }

        try {
            return getValue(context, metric);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            return SERVER_ERROR;
        }
    }

    private String getValue(Map<String, String> context, Metric metric) throws Exception {
        for (Row row : display.getLayout().get(0).getRows()) {
            if (row instanceof MetricRow) {
                MetricRow metricRow = (MetricRow)row;

                if (metricRow.getMetric().getType() == metric.getType()) {
                    return metricRow.retrieveData(context, 2, Table.TimeIntervalRule.NONE).get(0).get(1);
                }
            }
        }

        throw new IllegalStateException("Layout for metric " + metric.getType() + " not found");
    }

    /** {@inheritedDoc} */
    public List<ArrayList<String>> getMetricParametersList(String metricName) throws IOException {
        List<ArrayList<String>> parameters = new ArrayList<ArrayList<String>>();

        Metric metric = MetricFactory.createMetric(metricName);

        for (MetricParameter sp : metric.getParams()) {
            ArrayList<String> metricParameter = new ArrayList<String>();
            metricParameter.add(sp.name());
            metricParameter.add(sp.getDefaultValue());
            metricParameter.add(prepareParamName(sp));
            parameters.add(metricParameter);
        }

        return parameters;
    }

    private String prepareParamName(MetricParameter parameter) {
        String name = parameter.name();
        StringBuilder builder = new StringBuilder(name.length());

        String[] splitted = name.split("_");
        for (String str : splitted) {
            builder.append(Character.toUpperCase(str.charAt(0)));
            builder.append(str.substring(1).toLowerCase());
            builder.append(' ');
        }

        return builder.toString();
    }
}
