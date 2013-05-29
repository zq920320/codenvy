/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.vew.layout;

import com.codenvy.analytics.metrics.InitialValueNotFoundException;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TotalRowLayoutImpl extends AbstractRow {

    private final List<Metric> metrics;
    private final String       format;

    TotalRowLayoutImpl(String types, String format) throws IOException {
        String[] splitted = types.split(",");

        this.format = format;
        this.metrics = new ArrayList<>(splitted.length);
        for (String str : splitted) {
            this.metrics.add(MetricFactory.createMetric(str));
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<String> fill(Map<String, String> context, int length) throws Exception {
        List<String> row = new ArrayList<String>(length);
        row.add("Total");

        for (int i = 1; i < length; i++) {
            try {
                ValueData total = null;
                for (Metric metric : metrics) {
                    total = total == null ? metric.getValue(context) : total.union(metric.getValue(context));
                }

                if (isPrintable(total)) {
                    row.add(print(format, total));
                } else {
                    row.add("");
                }
            } catch (InitialValueNotFoundException e) {
                row.add("");
            }

            if (length > 2) {
                context = Utils.prevDateInterval(context);
            }
        }

        return row;
    }
}
