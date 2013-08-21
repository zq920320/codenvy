/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.ValueDataFactory;

import java.io.IOException;
import java.util.Calendar;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactorySessionsLastMetric extends CalculatedMetric {

    public FactorySessionsLastMetric() {
        super(MetricType.FACTORY_SESSION_LAST, MetricType.PRODUCT_USAGE_SESSIONS_FACTORY);
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        Calendar fromDate = Utils.getFromDate(context);
        Calendar toDate = Utils.getToDate(context);

        Map<String, String> dayContext = Utils.clone(context);
        MetricParameter.TIME_UNIT.put(dayContext, TimeUnit.DAY.name());
        Utils.putFromDate(dayContext, toDate);
        Utils.putToDate(dayContext, toDate);

        do {
            ListListStringValueData result = (ListListStringValueData)super.getValue(dayContext);
            if (result.size() != 0) {
                return result.getAll().get(result.size() - 1);
            }

            dayContext = Utils.prevDateInterval(dayContext);
        } while (!Utils.getToDate(dayContext).before(fromDate));

        return ValueDataFactory.createDefaultValue(getValueDataClass());
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListStringValueData.class;
    }
}
