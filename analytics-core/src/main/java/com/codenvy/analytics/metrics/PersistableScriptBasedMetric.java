/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.FSValueDataManager;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.Filter;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
abstract public class PersistableScriptBasedMetric extends ScriptBasedMetric {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistableScriptBasedMetric.class);

    PersistableScriptBasedMetric(MetricType metricType) {
        super(metricType);
    }

    /** {@inheritedDoc} */
    public ValueData getValue(Map<String, String> context) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Calculation " + getType() + " with context " + context.toString());
        }

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

        return doFilter(total, context);
    }


    protected ValueData evaluate(Map<String, String> context) throws IOException {
        String executionKey = metricType.name() + makeUUID(context).toString();

        acquire(executionKey);
        try {
            ValueData valueData;

            try {
                valueData = load(makeUUID(context));
            } catch (FileNotFoundException e) {
                valueData = executeScript(context);

                if (isStoreAllowed(context)) {
                    store(valueData, makeUUID(context));
                }
            }

            return valueData;
        } finally {
            release(executionKey);
        }
    }

    /**
     * Filtering data result depending on execution context.
     */
    protected ValueData doFilter(ValueData valueData, Map<String, String> context) {
        if (isFilterSupported()) {
            for (MetricFilter filterKey : MetricFilter.values()) {
                String filterValue = context.get(filterKey.name());

                if (filterValue != null) {
                    Filter filter = createFilter(valueData);
                    valueData = filter.apply(filterKey, filterValue);
                }
            }
        }

        return valueData;
    }

    /**
     * @return {@link ValueDataFilter} over {@link ValueData}
     */
    protected Filter createFilter(ValueData valueData) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return true if metric supports filtering otherwise return false
     */
    protected boolean isFilterSupported() {
        return false;
    }

    /** Stores value into the file. */
    protected void store(ValueData value, LinkedHashMap<String, String> uuid) throws IOException {
        FSValueDataManager.store(value, metricType, uuid);
    }

    /** Loads value from the file. */
    protected ValueData load(LinkedHashMap<String, String> uuid) throws IOException {
        return FSValueDataManager.load(metricType, uuid);
    }

    /** @return if it is allowed to preserve calculated data. */
    protected boolean isStoreAllowed(Map<String, String> context) throws IOException {
        Calendar toDate = Utils.getToDate(context);
        Calendar currentDate = DateUtils.truncate(Calendar.getInstance(), Calendar.DAY_OF_MONTH);

        return currentDate.after(toDate);
    }
}
