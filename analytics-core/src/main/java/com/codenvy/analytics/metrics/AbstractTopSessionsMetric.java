/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.SetStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractTopSessionsMetric extends CalculatedMetric {

    public static final int LIFE_TIME_PERIOD = -1;
    private final int period;

    public AbstractTopSessionsMetric(MetricType metricType, int period) {
        super(metricType, MetricType.FACTORY_SESSIONS_LIST);
        this.period = period;
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        if (MetricFilter.FACTORY_URL.exists(context)) {
            updateFilterInContext(context);
        }

        Calendar toDate = Utils.parseDate(MetricParameter.TO_DATE.getDefaultValue());
        Calendar fromDate;
        if (period == LIFE_TIME_PERIOD) {
            fromDate = Utils.parseDate(MetricParameter.FROM_DATE.getDefaultValue());
        } else {
            fromDate = (Calendar)toDate.clone();
            fromDate.add(Calendar.DAY_OF_MONTH, 1 - period);
        }

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
    }

    public void updateFilterInContext(Map<String, String> context) throws IOException {
        String factoryUrls = MetricFilter.FACTORY_URL.get(context);

        Set<String> activeWs = new HashSet<>();
        for (String factoryUrl : factoryUrls.split(",")) {
            activeWs.addAll(getCreatedTemporaryWs(context, factoryUrl));
        }

        MetricFilter.FACTORY_URL.remove(context);
        if (!activeWs.isEmpty()) {
            MetricFilter.WS.put(context, Utils.removeBracket(activeWs.toString()));
        }
    }

    /** @return all created temporary workspaces fro given factory url. */
    private Set<String> getCreatedTemporaryWs(Map<String, String> context, String factoryUrl) throws IOException {
        Metric metric = MetricFactory.createMetric(MetricType.FACTORY_URL_ACCEPTED);

        Map<String, String> clonedContext = Utils.clone(context);
        MetricFilter.FACTORY_URL.put(clonedContext, factoryUrl);

        SetStringValueData value = (SetStringValueData)metric.getValue(clonedContext);

        return value.getAll();
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
