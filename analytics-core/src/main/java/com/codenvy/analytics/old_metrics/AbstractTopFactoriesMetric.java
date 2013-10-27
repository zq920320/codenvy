/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.old_metrics;

import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.old_metrics.value.ListListStringValueData;
import com.codenvy.analytics.old_metrics.value.ListStringValueData;
import com.codenvy.analytics.old_metrics.value.SetStringValueData;
import com.codenvy.analytics.old_metrics.value.ValueData;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractTopFactoriesMetric extends AbstractTopFactoryStatisticsMetric {

    public AbstractTopFactoriesMetric(MetricType metricType, int period) {
        super(metricType, MetricType.ACTIVE_FACTORY_SET, period);
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        try {
            context = getContextWithDatePeriod(context);
        } catch (ParseException e) {
            throw new IOException(e);
        }

        SetStringValueData activeFactories = (SetStringValueData)super.getValue(context);
        Map<String, Long> factoryByTime = getFactoryTimeUsage(context, activeFactories);

        List<Map.Entry<String, Long>> top = new ArrayList<>(factoryByTime.entrySet());

        keepTopItems(top);

        List<ListStringValueData> result = new ArrayList<>(100);
        for (
                int i = 0;
                i < Math.min(TOP, top.size()); i++)

        {
            String factoryUrl = top.get(i).getKey();

            context = Utils.cloneAndClearFilters(context);
            MetricFilter.FACTORY_URL.put(context, factoryUrl);

            List<String> item = new ArrayList<>(13);

            item.add(factoryUrl);
            item.add(MetricFactory.createMetric(MetricType.USER_CREATED_FROM_FACTORY).getValue(context).getAsString());
            item.add(
                    MetricFactory.createMetric(MetricType.TEMPORARY_WORKSPACE_CREATED).getValue(context).getAsString());
            item.add(MetricFactory.createMetric(MetricType.FACTORY_SESSIONS).getValue(context).getAsString());
            item.add(MetricFactory.createMetric(MetricType.FACTORY_SESSIONS_ANON_PERCENT).getValue(context)
                                  .getAsString());
            item.add(MetricFactory.createMetric(MetricType.FACTORY_SESSIONS_AUTH_PERCENT).getValue(context)
                                  .getAsString());
            item.add(MetricFactory.createMetric(MetricType.FACTORY_SESSIONS_ABAN_PERCENT).getValue(context)
                                  .getAsString());
            item.add(MetricFactory.createMetric(MetricType.FACTORY_SESSIONS_CONV_PERCENT).getValue(context)
                                  .getAsString());
            item.add(MetricFactory.createMetric(MetricType.FACTORY_SESSIONS_AND_BUILT_PERCENT).getValue(context)
                                  .getAsString());
            item.add(MetricFactory.createMetric(MetricType.FACTORY_SESSIONS_AND_RUN_PERCENT).getValue(context)
                                  .getAsString());
            item.add(MetricFactory.createMetric(MetricType.FACTORY_SESSIONS_AND_DEPLOY_PERCENT).getValue(context)
                                  .getAsString());
            item.add(top.get(i).getValue().toString()); // mins

            Map<String, String> fullContext = Utils.clone(context);
            Parameters.FROM_DATE.putDefaultValue(fullContext);
            Parameters.TO_DATE.putDefaultValue(fullContext);

            FactorySessionFirstMetric sessionFirstMetric =
                    (FactorySessionFirstMetric)MetricFactory.createMetric(MetricType.FACTORY_SESSION_FIRST);
            ListStringValueData firstSession = (ListStringValueData)sessionFirstMetric.getValue(fullContext);
            String date = firstSession.size() == 0 ? "" : sessionFirstMetric.getDate(firstSession);
            item.add(date);

            FactorySessionsLastMetric factoryLastMetric =
                    (FactorySessionsLastMetric)MetricFactory.createMetric(MetricType.FACTORY_SESSION_LAST);
            ListStringValueData lastSession = (ListStringValueData)factoryLastMetric.getValue(fullContext);
            date = lastSession.size() == 0 ? "" : factoryLastMetric.getDate(lastSession);
            item.add(date);

            result.add(new ListStringValueData(item));
        }

        return new ListListStringValueData(result);
    }

    private Map<String, Long> getFactoryTimeUsage(Map<String, String> context,
                                                  SetStringValueData activeFactories) throws IOException {

        Map<String, Long> timeUsage = new HashMap<>(activeFactories.size());
        for (String factoryUrl : activeFactories.getAll()) {
            context = Utils.cloneAndClearFilters(context);
            MetricFilter.FACTORY_URL.put(context, factoryUrl);

            Metric metric = MetricFactory.createMetric(MetricType.PRODUCT_USAGE_TIME_FACTORY);

            timeUsage.put(factoryUrl, metric.getValue(context).getAsLong());
        }

        return timeUsage;
    }

    private void keepTopItems(List<Map.Entry<String, Long>> top) {
        Collections.sort(top, new Comparator<Map.Entry<String, Long>>() {
            @Override
            public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
                long delta = o1.getValue() - o2.getValue();
                return delta > 0 ? -1 : (delta < 0 ? 1 : 0);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListListStringValueData.class;
    }
}
