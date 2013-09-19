/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.*;

import java.io.IOException;
import java.util.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractTopFactoriesMetric extends AbstractTopFactoryStatisticsMetric {

    public AbstractTopFactoriesMetric(MetricType metricType, int period) {
        super(metricType, MetricType.ACTIVE_FACTORY_SET, period);
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        context = getContextWithDatePeriod(context);

        Map<String, Long> factoryByTime = new HashMap<>();
        Map<String, Set<String>> factoryByWs = new HashMap<>();

        for (String factoryUrl : getInvolvedFactories(context)) {
            Set<String> activeWs = getInvolvedTemporaryWs(factoryUrl, context);
            Long usageTime = getUsageTime(context, activeWs);

            factoryByWs.put(factoryUrl, activeWs);
            factoryByTime.put(factoryUrl, usageTime);
        }

        List<Map.Entry<String, Long>> top = new ArrayList<>(factoryByTime.entrySet());
        Collections.sort(top, new Comparator<Map.Entry<String, Long>>() {
            @Override
            public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
                long delta = o1.getValue() - o2.getValue();
                return delta > 0 ? -1 : (delta < 0 ? 1 : 0);
            }
        });

        List<ListStringValueData> result = new ArrayList<>(100);
        for (int i = 0; i < Math.min(TOP, top.size()); i++) {
            String factoryUrl = top.get(i).getKey();

            context = Utils.cloneAndClearFilters(context);
            MetricFilter.WS.put(context, Utils.removeBracket(factoryByWs.get(factoryUrl).toString()));

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

            Map<String, String> fullContext = Utils.newContext();
            MetricParameter.FROM_DATE.putDefaultValue(fullContext);
            MetricParameter.TO_DATE.putDefaultValue(fullContext);
            MetricFilter.WS.put(fullContext, MetricFilter.WS.get(context));

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

            result.add(new ListStringValueData(item));
        }

        return new ListListStringValueData(result);
    }

    protected Set<String> getInvolvedTemporaryWs(String factoryUrl, Map<String, String> context) throws IOException {
        context = Utils.cloneAndClearFilters(context);
        MetricFilter.FACTORY_URL.put(context, factoryUrl);

        return getTemporaryWsCreated(context);
    }

    private Collection<String> getInvolvedFactories(Map<String, String> context) throws IOException {
        if (Utils.getAvailableFilters(context).isEmpty()) {
            return ((SetStringValueData)super.getValue(context)).getAll();
        }

        return getFactoriesCreated(context);
    }

    /** @return time spent by users in temporary workspaces */
    private Long getUsageTime(Map<String, String> context, Set<String> activeWs) throws IOException {
        Metric metric = MetricFactory.createMetric(MetricType.PRODUCT_USAGE_TIME_FACTORY);

        context = Utils.cloneAndClearFilters(context);
        MetricFilter.WS.put(context, Utils.removeBracket(activeWs.toString()));

        LongValueData value = (LongValueData)metric.getValue(context);

        return value.getAsLong();
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListListStringValueData.class;
    }
}
