/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.old_metrics;

import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.old_metrics.value.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractProductUsageTimeMetric extends CalculatedMetric {

    public static final int LIFE_TIME_PERIOD = -1;
    private final int topPeriod;

    public AbstractProductUsageTimeMetric(MetricType metricType, MetricType basedMetric, int topPeriod) {
        super(metricType, basedMetric);
        this.topPeriod = topPeriod;
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        context = Utils.newContext();
        Parameters.TO_DATE.putDefaultValue(context);

        Map<String, FixedListLongValueData> byTopDay = getUsageByPeriod(context, topPeriod);
        Map<String, FixedListLongValueData> by1day = getUsageByPeriod(context, 1);
        Map<String, FixedListLongValueData> by7day = getUsageByPeriod(context, 7);
        Map<String, FixedListLongValueData> by30day = getUsageByPeriod(context, 30);
        Map<String, FixedListLongValueData> by60day = getUsageByPeriod(context, 60);
        Map<String, FixedListLongValueData> by90day = getUsageByPeriod(context, 90);
        Map<String, FixedListLongValueData> by365day = getUsageByPeriod(context, 365);
        Map<String, FixedListLongValueData> byAllDay = getUsageByPeriod(context, LIFE_TIME_PERIOD);

        List<Map.Entry<String, FixedListLongValueData>> top = new ArrayList<>(byTopDay.entrySet());
        Collections.sort(top, new Comparator<Map.Entry<String, FixedListLongValueData>>() {
            @Override
            public int compare(Map.Entry<String, FixedListLongValueData> o1,
                               Map.Entry<String, FixedListLongValueData> o2) {
                long delta = o1.getValue().getAll().get(0) - o2.getValue().getAll().get(0);
                return delta > 0 ? -1 : (delta < 0 ? 1 : 0);
            }
        });

        List<ListStringValueData> result = new ArrayList<>(100);
        for (int i = 0; i < Math.min(TOP, top.size()); i++) {
            List<String> item = new ArrayList<>(8);

            String user = top.get(i).getKey();

            item.add(user); // user name
            item.add(getTotalNumberOfSessions(byAllDay, user).toString());
            item.add(getUsageTime(by1day, user).toString());
            item.add(getUsageTime(by7day, user).toString());
            item.add(getUsageTime(by30day, user).toString());
            item.add(getUsageTime(by60day, user).toString());
            item.add(getUsageTime(by90day, user).toString());
            item.add(getUsageTime(by365day, user).toString());
            item.add(getUsageTime(byAllDay, user).toString());

            result.add(new ListStringValueData(item));
        }

        return new ListListStringValueData(result);
    }

    private Long getUsageTime(Map<String, FixedListLongValueData> byPeriod, String user) {
        return byPeriod.containsKey(user) ? byPeriod.get(user).getAll().get(0) : 0;
    }

    private Long getTotalNumberOfSessions(Map<String, FixedListLongValueData> byAllDay, String user) {
        return byAllDay.get(user).getAll().get(1);
    }

    private Map<String, FixedListLongValueData> getUsageByPeriod(Map<String, String> context, int period)
            throws IOException {
        try {
            Calendar date = Utils.getToDate(context);
            date.add(Calendar.DAY_OF_MONTH, 1 - period);

            if (period == LIFE_TIME_PERIOD) {
                Parameters.FROM_DATE.putDefaultValue(context);
            } else {
                Utils.putFromDate(context, date);
            }

            return ((MapStringFixedLongListValueData)super.getValue(context)).getAll();
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListListStringValueData.class;
    }
}
