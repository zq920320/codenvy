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


package com.codenvy.analytics.services.view;


import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MetricRow extends AbstractRow {

    private static final String NAME   = "name";
    private static final String FORMAT = "format";
    private static final String TYPE   = "type";
    private static final String TITLE  = "title";

    private static final String DEFAULT_FORMAT = "%.0f";

    private final Metric metric;

    private MetricRow(Map<String, String> parameters) {
        super(parameters);
        metric = MetricFactory.getMetric(parameters.get(NAME));
    }

    /** {@inheritDoc} */
    @Override
    public ValueData retrieveData(Map<String, String> context) throws IOException {
        return metric.getValue(context);
    }
}