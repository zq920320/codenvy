/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.*;

import java.io.IOException;
import java.util.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractTopReferrersMetric extends AbstractTopFactoryStatisticsMetric {

    public AbstractTopReferrersMetric(MetricType metricType, int period) {
        super(metricType, MetricType.REFERRERS, period);
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        context = getContextWithDatePeriod(context);

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
            item.add("" + 100D * (sess - auth) / sess);
            item.add("" + 100D * auth / sess);
            item.add("" + 100D * (sess - conv) / sess);
            item.add("" + 100D * conv / sess);
            item.add("" + 100D * bld / sess);
            item.add("" + 100D * run / sess);
            item.add("" + 100D * dpl / sess);
            item.add("" + mins / 60);

            addFirstLastSessionsOccurrence(item, context);

            result.add(new ListStringValueData(item));
        }

        return new ListListStringValueData(result);
    }

    private void addFirstLastSessionsOccurrence(List<String> item, Map<String, String> context) throws IOException {
        context = Utils.clone(context);
        MetricParameter.FROM_DATE.putDefaultValue(context);
        MetricParameter.TO_DATE.putDefaultValue(context);

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

    /** @return not more than {@link #TOP} entities in terms of temporary workspaces created */
    private List<Map.Entry<String, FixedListLongValueData>> keepTopItems(MapStringFixedLongListValueData referrers) {
        List<Map.Entry<String, FixedListLongValueData>> entities = new ArrayList<>(referrers.getAll().entrySet());

        Collections.sort(entities, new Comparator<Map.Entry<String, FixedListLongValueData>>() {
            @Override
            public int compare(Map.Entry<String, FixedListLongValueData> o1,
                               Map.Entry<String, FixedListLongValueData> o2) {

                int index = 1;
                long delta = o1.getValue().getAll().get(index) - o2.getValue().getAll().get(index);
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
