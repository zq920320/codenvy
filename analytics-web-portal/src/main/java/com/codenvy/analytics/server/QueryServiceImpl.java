package com.codenvy.analytics.server;

import com.codenvy.analytics.client.QueryService;

import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.scripts.ScriptParameters;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

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

    public Map<String, String> getMetricTypes() throws IOException {
        Map<String, String> types = new LinkedHashMap<String, String>();

        for (MetricType st : MetricType.values()) {
            types.put(MetricFactory.createMetric(st).getTitle(), st.name());
        }

        return types;
    }

    public Map<String, String> getMetricParameters(String metricTitle) throws IOException {
        Map<String, String> parameters = new HashMap<String, String>();

        Metric metric = MetricFactory.createMetric(metricTitle);

        for (ScriptParameters sp : metric.getMandatoryParams()) {
            parameters.put(sp.getName(), sp.getDefaultValue());
        }

        for (ScriptParameters sp : metric.getAdditionalParams()) {
            parameters.put(sp.getName(), sp.getDefaultValue());
        }

        return parameters;
    }

    /**
     * Escape an html string. Escaping data received from the client helps to prevent cross-site script vulnerabilities.
     * 
     * @param html the html string to escape
     * @return the escaped string
     */
    private String escapeHtml(String html) {
        if (html == null) {
            return null;
        }
        return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }

    public String calculateMetric(String metricTitle, Map<String, String> parameters) throws IOException {
        return MetricFactory.createMetric(metricTitle).getValue(parameters).toString();
    }

    public List<ArrayList<String>> getMetricParametersList(String metricName) throws IOException {

        List<ArrayList<String>> parameters = new ArrayList<ArrayList<String>>();

        Metric metric = MetricFactory.createMetric(metricName);

        for (ScriptParameters sp : metric.getMandatoryParams()) {
            ArrayList<String> metricParameter = new ArrayList<String>();
            metricParameter.add(sp.getName());
            metricParameter.add(sp.getDefaultValue());
            metricParameter.add(sp.getDescription());
            metricParameter.add(sp.getTitle());
            parameters.add(metricParameter);
        }

        for (ScriptParameters sp : metric.getAdditionalParams()) {
            ArrayList<String> metricParameter = new ArrayList<String>();
            metricParameter.add(sp.getName());
            metricParameter.add(sp.getDefaultValue());
            metricParameter.add(sp.getDescription());
            metricParameter.add(sp.getTitle());
            parameters.add(metricParameter);
        }

        return parameters;
    }
}
