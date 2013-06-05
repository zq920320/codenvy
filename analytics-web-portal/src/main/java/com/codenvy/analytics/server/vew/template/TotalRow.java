/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.vew.template;


import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.shared.RowData;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TotalRow extends AbstractRow {

    private static final String ATTRIBUTE_TYPES = "types";
    private static final String ATTRIBUTE_FORMAT = "format";

    private final List<Metric> metrics;
    private final String       format;

    /**
     * {@link TotalRow} constructor.
     */
    private TotalRow(List<Metric> metrics, String format) {
        this.format = format;
        this.metrics = metrics;
    }

    /** {@inheritDoc} */
    @Override
    public List<RowData> fill(Map<String, String> context, int length) throws Exception {
        RowData row = new RowData();

        row.add("Total");

        for (int i = 1; i < length; i++) {
            ValueData total = null;

            for (Metric metric : metrics) {
                ValueData newValue = metric.getValue(context);
                total = total == null ? newValue : total.union(newValue);
            }

            if (isPrintable(total)) {
                row.add(print(format, total));
            } else {
                row.add("");
            }

            context = Utils.prevDateInterval(context);
        }

        ArrayList<RowData> result = new ArrayList<>();
        result.add(row);

        return result;
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
