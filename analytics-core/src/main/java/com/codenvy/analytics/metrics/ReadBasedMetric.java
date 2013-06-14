/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.FSValueDataManager;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.ValueDataFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Map;

/**
 * It is supposed to read precalculated {@link ValueData} from storage.
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class ReadBasedMetric extends AbstractMetric {

    ReadBasedMetric(MetricType metricType) {
        super(metricType);
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        Calendar fromDate = Utils.getFromDate(context);
        Calendar toDate = Utils.getToDate(context);

        ValueData total = null;

        Map<String, String> dayContext = Utils.clone(context);
        while (!fromDate.after(toDate)) {
            Utils.putFromDate(dayContext, fromDate);
            Utils.putToDate(dayContext, fromDate);
            Utils.putTimeUnit(dayContext, TimeUnit.DAY);

            ValueData dayValue = evaluate(dayContext);
            total = total == null ? dayValue : total.union(dayValue);

            fromDate.add(Calendar.DAY_OF_MONTH, 1);
        }

        return total;
    }

    protected ValueData evaluate(Map<String, String> dayContext) throws IOException {
        try {
            return FSValueDataManager.load(metricType, makeUUID(dayContext));
        } catch (FileNotFoundException e) {
            return createEmptyValueData();
        }
    }

    protected ValueData createEmptyValueData() throws IOException {
        return ValueDataFactory.createEmptyValueData(getValueDataClass());
    }
}

