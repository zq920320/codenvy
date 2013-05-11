/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.vew.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.codenvy.analytics.metrics.InitialValueNotFoundException;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.DoubleValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MetricRowLayoutImpl implements RowLayout {

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
    
    private String print(String format, ValueData valueData) {
        if (format.contains("d")) {
            return String.format(format, valueData.getAsLong());
        } else if (format.contains("f")) {
            return String.format(format, valueData.getAsDouble());
        } else if (format.contains("s")) {
            return String.format(format, valueData.getAsString());
        }
        
        return valueData.getAsString();
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

    /** Checks if received value might be displayed in view. */
    private boolean isPrintable(ValueData value) {
        if (value instanceof DoubleValueData && (Double.valueOf(value.getAsDouble()).isNaN() || value.getAsDouble() == 0)) {
            return false;
        } else if (value instanceof LongValueData && value.getAsLong() == 0) {
            return false;
        }

        return true;
    }
}