/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractTopSessionsMetric extends AbstractTopFactoryStatisticsMetric {

    public AbstractTopSessionsMetric(MetricType metricType, int period) {
        super(metricType, MetricType.FACTORY_SESSIONS_LIST, period);
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        try {
            context = getContextWithDatePeriod(context);

            Calendar fromDate = Utils.getFromDate(context);
            Calendar toDate = Utils.getToDate(context);

            List<ListStringValueData> top = new ArrayList<>();
            Map<String, String> dayContext = Utils.clone(context);
            do {
                Utils.putFromDate(dayContext, fromDate);
                Utils.putToDate(dayContext, fromDate);

                ListListStringValueData sessions = (ListListStringValueData)super.getValue(dayContext);
                top.addAll(sessions.getAll());
                top = keepTopItems(top);

                fromDate.add(Calendar.DAY_OF_MONTH, 1);
            } while (!fromDate.after(toDate));

            return new ListListStringValueData(top);
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    private List<ListStringValueData> keepTopItems(List<ListStringValueData> top) {
        Collections.sort(top, new Comparator<ListStringValueData>() {
            @Override
            public int compare(ListStringValueData o1, ListStringValueData o2) {
                long delta = Long.valueOf(o1.getAll().get(0)) - Long.valueOf(o2.getAll().get(0));
                return delta > 0 ? -1 : (delta < 0 ? 1 : 0);
            }
        });

        return new ArrayList<>(top.subList(0, Math.min(TOP, top.size())));
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListListStringValueData.class;
    }
}
