/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.vew.layout;

import com.codenvy.analytics.metrics.InitialValueNotFoundException;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.ValueData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MetricRowLayoutImpl extends AbstractRow {

    private static final String DEFAULT_FORMAT = "%.0f";

    private final Metric        metric;
    private final String        format;
    private final String        title;

    MetricRowLayoutImpl(Metric metric, String title, String format) {
        this.metric = metric;
        this.title = title;
        this.format = format == null || format.isEmpty() ? DEFAULT_FORMAT : format;
    }

    /**
     * Fills row by the next rule:<br>
     * <li>First column is {@link #title}</li><br>
     * <li>Others columns are filed by metric's value.</li><br>
     * {@inheritedDoc}
     */
    public List<String> fill(Map<String, String> context, int length) throws Exception {
        List<String> row = new ArrayList<String>(length);
        row.add(title);

        for (int i = 1; i < length; i++) {
            try {
                ValueData valueData = metric.getValue(context);

                if (isPrintable(valueData)) {
                    row.add(print(format, valueData));
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
    
    /**
     * @return {@link #format}
     */
    public String getFormat() {
        return format;
    }

    /**
     * @return {@link #title}
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return {@link #metric}
     */
    public Metric getMetric() {
        return metric;
    }
}