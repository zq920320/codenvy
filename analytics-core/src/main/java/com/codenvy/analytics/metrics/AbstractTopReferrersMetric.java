/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.*;

import java.io.IOException;
import java.util.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractTopReferrersMetric extends CalculatedMetric {

    public static final int LIFE_TIME_PERIOD = -1;
    private final int period;

    public AbstractTopReferrersMetric(MetricType metricType, int period) {
        super(metricType, MetricType.REFERRERS);
        this.period = period;
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        context = overrideContext(context);

        MapStringFixedLongListValueData referrers = (MapStringFixedLongListValueData)super.getValue(context);

        List<ListStringValueData> result = new ArrayList<>(100);
        for (Map.Entry<String, FixedListLongValueData> refEntry : keepTopItems(referrers)) {
            List<String> item = new ArrayList<>(13);

            String referrer = refEntry.getKey();
            List<Long> values = refEntry.getValue().getAll();

            Long mins = values.get(0);
            Long tmpWs = values.get(1);
            Long sess = values.get(2);
            Long auth = values.get(3);
            Long conv = values.get(4);
            Long bld = values.get(5);
            Long run = values.get(6);
            Long dpl = values.get(7);

            item.add(referrer);
            item.add(tmpWs.toString());
            item.add(sess.toString());
            item.add("" + 100D * (sess.longValue() - auth.longValue()) / sess.longValue());
            item.add("" + 100D * auth.longValue() / sess.longValue());
            item.add("" + 100D * (sess.longValue() - conv.longValue()) / sess.longValue());
            item.add("" + 100D * conv.longValue() / sess.longValue());
            item.add("" + 100D * bld.longValue() / sess.longValue());
            item.add("" + 100D * run.longValue() / sess.longValue());
            item.add("" + 100D * dpl.longValue() / sess.longValue());
            item.add("" + mins.longValue() / 60);

            addFirstLastSessionsOccurrence(item);

            result.add(new ListStringValueData(item));
        }

        return new ListListStringValueData(result);
    }

    private void addFirstLastSessionsOccurrence(List<String> item) throws IOException {
        Map<String, String> context = Utils.newContext();
        MetricParameter.FROM_DATE.putDefaultValue(context);
        MetricParameter.TO_DATE.putDefaultValue(context);

        MetricFilter.REFERRER_URL.put(context, item.get(0));

        FactorySessionFirstMetric sessionFirstMetric =
                (FactorySessionFirstMetric)MetricFactory.createMetric(MetricType.FACTORY_SESSION_FIRST);
        ListStringValueData firstSession = (ListStringValueData)sessionFirstMetric.getValue(context);
        String date = firstSession.size() == 0 ? "" : sessionFirstMetric.getDate(firstSession);
        item.add(date);

        FactorySessionsLastMetric factoryLastMetric =
                (FactorySessionsLastMetric)MetricFactory.createMetric(MetricType.FACTORY_SESSION_LAST);
        ListStringValueData lastSession = (ListStringValueData)factoryLastMetric.getValue(context);
        date = lastSession.size() == 0 ? "" : factoryLastMetric.getDate(lastSession);
        item.add(date);
    }

    /**
     * Context initialization accordingly to given period. For instance, if period is equal to 7, then
     * context will cover last 7 days.
     */
    private Map<String, String> overrideContext(Map<String, String> context) throws IOException {
        String factoryUrls = MetricFilter.FACTORY_URL.get(context);

        context = Utils.newContext();
        MetricParameter.TO_DATE.putDefaultValue(context);

        if (period == LIFE_TIME_PERIOD) {
            MetricParameter.FROM_DATE.putDefaultValue(context);
        } else {
            Calendar date = Utils.getToDate(context);
            date.add(Calendar.DAY_OF_MONTH, 1 - period);

            Utils.putFromDate(context, date);
        }

        if (factoryUrls != null) {
            updateFilterInContext(context, factoryUrls);
        }

        return context;
    }

    /** Replaces {@link MetricFilter#FACTORY_URL} by {@link MetricFilter#WS} */
    private void updateFilterInContext(Map<String, String> context, String factoryUrls) throws IOException {
        Set<String> activeWs = new HashSet<>();
        for (String factoryUrl : factoryUrls.split(",")) {
            activeWs.addAll(getCreatedTemporaryWs(context, factoryUrl));
        }

        MetricFilter.FACTORY_URL.remove(context);
        if (!activeWs.isEmpty()) {
            MetricFilter.WS.put(context, Utils.removeBracket(activeWs.toString()));
        }
    }

    /** @return all created temporary workspaces for given factory url. */
    private Set<String> getCreatedTemporaryWs(Map<String, String> context, String factoryUrl) throws IOException {
        Metric metric = MetricFactory.createMetric(MetricType.FACTORY_URL_ACCEPTED);

        Map<String, String> clonedContext = Utils.clone(context);
        MetricFilter.FACTORY_URL.put(clonedContext, factoryUrl);

        SetStringValueData value = (SetStringValueData)metric.getValue(clonedContext);

        return value.getAll();
    }

    /** @return not more than {@link #TOP} entities in terms of usage time */
    private List<Map.Entry<String, FixedListLongValueData>> keepTopItems(MapStringFixedLongListValueData referrers) {
        List<Map.Entry<String, FixedListLongValueData>> entities = new ArrayList<>(referrers.getAll().entrySet());

        Collections.sort(entities, new Comparator<Map.Entry<String, FixedListLongValueData>>() {
            @Override
            public int compare(Map.Entry<String, FixedListLongValueData> o1,
                               Map.Entry<String, FixedListLongValueData> o2) {

                long delta = o1.getValue().getAll().get(0) - o2.getValue().getAll().get(0);
                return delta > 0 ? -1 : (delta < 0 ? 1 : 0);
            }
        });

        return entities.subList(0, Math.min(TOP, entities.size()));
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListListStringValueData.class;
    }
}
