/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;

import com.codenvy.analytics.metrics.value.FSValueDataManager;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.ValueDataManager;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class AbstractMetric implements Metric {

    protected final ValueDataManager valueDataManager;
    protected final MetricType       metricType;

    AbstractMetric(MetricType metricType) {
        this.metricType = metricType;
        this.valueDataManager = new FSValueDataManager(metricType);
    }

    /** {@inheritedDoc} */
    @Override
    public MetricType getType() {
        return metricType;
    }

    /** {@inheritedDoc} */
    public synchronized ValueData getValue(Map<String, String> context) throws IOException {
        try {
            return load(context);
        } catch (FileNotFoundException e) {
            ValueData valueData = evaluate(context);
            storeIfAllowed(valueData, context);

            return valueData;
        }
    }

    /**
     * Stores value into the file.
     */
    protected void storeIfAllowed(ValueData value, Map<String, String> context) throws IOException {
        if (isStoreAllowed(context)) {
            valueDataManager.store(value, makeUUID(context));
        }
    }

    /** Loads value from the file. */
    protected ValueData load(Map<String, String> context) throws IOException {
        return valueDataManager.load(makeUUID(context));
    }

    /** @return if it is allowed to preserve evaluated result. */
    protected boolean isStoreAllowed(Map<String, String> context) throws IOException {
        if (Utils.getTimeUnitParam(context) == null || Utils.getToDateParam(context) == null) {
            return false;
        }

        Calendar toDate = Utils.getToDate(context);
        Calendar currentDate = DateUtils.truncate(Calendar.getInstance(), Calendar.DAY_OF_MONTH);

        return currentDate.after(toDate);
    }

    /** Preparation unique sequences to identify stored value. */
    protected LinkedHashMap<String, String> makeUUID(Map<String, String> context) throws IOException {
        LinkedHashMap<String, String> keys = new LinkedHashMap<String, String>();

        for (MetricParameter param : getParams()) {
            String paramKey = param.getName();
            String paramValue = context.get(paramKey);

            if (paramValue == null) {
                throw new IOException("There is no parameter " + paramKey + " in context");
            }

            keys.put(paramKey, paramValue);
        }

        return keys;
    }

    /** @return what data type is represented in result */
    protected abstract Class< ? extends ValueData> getValueDataClass();

    /** Evaluating */
    protected abstract ValueData evaluate(Map<String, String> context) throws IOException;
}
