/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.MapStringListValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageTimeTopUsers1DayMetric extends CalculatedMetric {

    public ProductUsageTimeTopUsers1DayMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_TOP_USERS_BY_1DAY, MetricType.PRODUCT_USAGE_TIME_USERS);
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        context = Utils.newContext();
        Utils.putToDateDefault(context);

        Map<String, ListStringValueData> by1day = getUsageByPeriod(context, 1);
//        MapStringListValueData by7day = getUsageByPeriod(context, 7);
//        MapStringListValueData by30day = getUsageByPeriod(context, 30);
//        MapStringListValueData by60day = getUsageByPeriod(context, 60);
//        MapStringListValueData by90day = getUsageByPeriod(context, 90);
//        MapStringListValueData by365day = getUsageByPeriod(context, 365);
//        MapStringListValueData byAllDay = getUsageByPeriod(context, -1);

        List<Map.Entry<String, ListStringValueData>> list = new ArrayList<>(by1day.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, ListStringValueData>>() {
            @Override
            public int compare(Map.Entry<String, ListStringValueData> o1, Map.Entry<String, ListStringValueData> o2) {
                long delta = Long.valueOf(o1.getValue().getAll().get(0)) - Long.valueOf(o2.getValue().getAll().get(0));
                return delta > 0 ? -1 : (delta < 0 ? 1 : 0);
            }
        });

//        for (int i = 0; i < Math.min(TOP, items.size()); i++) {
//            List<String> s = new ArrayList<>();
//
//        }


        return null;
    }

    private Map<String, ListStringValueData> getUsageByPeriod(Map<String, String> context, int period) throws IOException {
        Calendar date = Utils.getToDate(context);
        date.add(Calendar.DAY_OF_MONTH, -(period + 1));

        if (period < 0) {
            Utils.putFromDateDefault(context);
        } else {
            Utils.putFromDate(context, date);
        }

        return ((MapStringListValueData)super.getValue(context)).getAll();
    }

//    private Map<String, Long> top(Map)

    /** {@inheritDoc} */
    @Override
    protected Class<? extends ValueData> getValueDataClass() {
        return ListListStringValueData.class;
    }
}
