/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.vew.template;


import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.value.ValueData;

import org.w3c.dom.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TotalRow extends AbstractRow {

    private static final String ATTRIBUTE_TYPES  = "types";
    private static final String ATTRIBUTE_FORMAT = "format";

    private final List<Metric> metrics;
    private final String       format;

    /** {@link TotalRow} constructor. */
    private TotalRow(List<Metric> metrics, String format) {
        super();

        this.format = format;
        this.metrics = metrics;
    }

    /** {@inheritDoc} */
    @Override
    protected String doRetrieve(Map<String, String> context, int columnNumber) throws IOException {
        switch (columnNumber) {
            case 0:
                return "Total";
            default:
                ValueData total = null;

                for (Metric metric : metrics) {
                    ValueData newValue = metric.getValue(context);
                    total = total == null ? newValue : total.union(newValue);
                }

                return getAsString(total, format);
        }
    }

    /** Factory method */
    public static TotalRow initialize(Element element) {
        String format = element.getAttribute(ATTRIBUTE_FORMAT);
        String types = element.getAttribute(ATTRIBUTE_TYPES);

        String[] splitted = types.split(",");
        List<Metric> metrics = new ArrayList<>(splitted.length);

        for (String str : splitted) {
            metrics.add(MetricFactory.createMetric(str));
        }


        return new TotalRow(metrics, format);
    }
}
