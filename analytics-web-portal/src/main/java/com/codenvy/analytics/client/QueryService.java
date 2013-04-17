package com.codenvy.analytics.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("query")
public interface QueryService extends RemoteService {
    Map<String, String> getMetricTypes() throws IOException;

    Map<String, String> getMetricParameters(String metricName) throws IOException;

    List<ArrayList<String>> getMetricParametersList(String metricName) throws IOException;

    String calculateMetric(String scriptTypeName, Map<String, String> parameters) throws IOException;
}
