/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.ValueDataFactory;

import java.io.IOException;
import java.util.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactorySessionFirstMetric extends CalculatedMetric {

    public FactorySessionFirstMetric() {
        super(MetricType.FACTORY_SESSION_FIRST, MetricType.PRODUCT_USAGE_SESSIONS_FACTORY);
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        Calendar fromDate = Utils.getFromDate(context);
        Calendar toDate = Utils.getToDate(context);

        Map<String, String> dayContext = Utils.clone(context);
        MetricParameter.TIME_UNIT.put(dayContext, TimeUnit.DAY.name());
        Utils.putFromDate(dayContext, fromDate);
        Utils.putToDate(dayContext, fromDate);

        do {
            ListListStringValueData result = (ListListStringValueData)super.getValue(dayContext);
            if (result.size() != 0) {
                List<ListStringValueData> sessions = new ArrayList<>(result.getAll());
                Collections.sort(sessions, new Comparator<ListStringValueData>() {
                    @Override
                    public int compare(ListStringValueData o1, ListStringValueData o2) {
                        return o1.getAll().get(2).compareTo(o2.getAll().get(2));
                    }
                });

                return sessions.get(0);
            }
            dayContext = Utils.nextDateInterval(dayContext);
        } while (!Utils.getFromDate(dayContext).after(toDate));

        return ValueDataFactory.createDefaultValue(getValueDataClass());
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListStringValueData.class;
    }
}
