/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.vew.template;


import com.codenvy.analytics.metrics.InitialValueNotFoundException;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.shared.RowData;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MetricRow extends AbstractRow {

    private static final String ATTRIBUTE_FORMAT   = "format";
    private static final String ATTRIBUTE_TYPE     = "type";
    private static final String ATTRIBUTE_TITLE    = "title";
    private static final String ATTRIBUTE_NO_TITLE = "noTitle";

    private static final String DEFAULT_FORMAT     = "%.0f";

    private final Metric        metric;
    private final String        format;
    private final String        title;
    private final boolean       noTitle;

    private MetricRow(Metric metric, String title, boolean noTitle, String format) {
        this.metric = metric;
        this.title = title;
        this.noTitle = noTitle;
        this.format = format == null || format.isEmpty() ? DEFAULT_FORMAT : format;
    }

    /**
     * Fills row by the next rule:<br>
     * <li>First column is {@link #title}</li><br>
     * <li>Others columns are filed by metric's value.</li><br>
     * {@inheritedDoc}
     */
    public List<RowData> fill(Map<String, String> context, int length) throws Exception {
        RowData row = new RowData();

        if (!noTitle) {
            row.add(title);
        }

        int startIndex = noTitle ? 0 : 1;

        for (int i = startIndex; i < length; i++) {
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

        ArrayList<RowData> result = new ArrayList<>();
        result.add(row);

        return result;
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

    /** Factory method */
    public static MetricRow initialize(Element element) {
        Metric metric = MetricFactory.createMetric(element.getAttribute(ATTRIBUTE_TYPE));
        String format = element.getAttribute(ATTRIBUTE_FORMAT);
        String title = element.getAttribute(ATTRIBUTE_TITLE);
        String noTitle = element.getAttribute(ATTRIBUTE_NO_TITLE);

        return new MetricRow(metric, title, noTitle == null || noTitle.isEmpty() ? false : Boolean.valueOf(noTitle), format);
    }
}