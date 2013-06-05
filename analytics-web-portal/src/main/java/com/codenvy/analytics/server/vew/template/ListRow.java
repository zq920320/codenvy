/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.vew.template;


import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ListRow implements Row {

    private static final String ATTRIBUTE_TYPE   = "type";
    private static final String ATTRIBUTE_TITLE = "title";

    private final Metric        metric;
    private final String        title;

    private ListRow(Metric metric, String title) {
        this.metric = metric;
        this.title = title;
    }

    /** {@inheritDoc} */
    @Override
    public List<List<String>> fill(Map<String, String> context, int length) throws Exception {
        ArrayList<List<String>> result = new ArrayList<>();

        ListListStringValueData valueData = (ListListStringValueData)metric.getValue(context);

        for (ListStringValueData litVd : valueData.getAll()) {
            List<String> row = new ArrayList<String>(length);

            List<String> item = litVd.getAll();
            for (int i = 0; i < length; i++) {
                row.add(item.get(i));
            }
            result.add(row);
        }

        return result;
    }
    
    /**
     * @return {@link #metric}
     */
    public Metric getMetric() {
        return metric;
    }

    /** Factory method */
    public static ListRow initialize(Element element) {
        Metric metric = MetricFactory.createMetric(element.getAttribute(ATTRIBUTE_TYPE));
        String title = element.getAttribute(ATTRIBUTE_TITLE);
        return new ListRow(metric, title);
    }
}