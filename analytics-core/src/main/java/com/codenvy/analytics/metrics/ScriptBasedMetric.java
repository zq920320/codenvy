/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.CacheableValueDataManager;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.ValueDataManager;
import com.codenvy.analytics.scripts.ScriptType;
import com.codenvy.analytics.scripts.executor.ScriptExecutor;
import com.codenvy.analytics.scripts.executor.pig.PigScriptExecutor;

import org.apache.commons.lang.time.DateUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
abstract public class ScriptBasedMetric extends AbstractMetric {

    protected final ValueDataManager valueDataManager;

    ScriptBasedMetric(MetricType metricType) {
        super(metricType);
        this.valueDataManager = new CacheableValueDataManager(metricType);
    }

    /** {@inheritedDoc} */
    public synchronized ValueData getValue(Map<String, String> context) throws IOException {
        ValueData valueData;
        try {
            valueData = load(context);
        } catch (FileNotFoundException e) {
            valueData = evaluate(context);
        }

        return doFilter(valueData, context);
    }

    /**
     * Filtering data result depending on execution context.
     */
    protected ValueData doFilter(ValueData valueData, Map<String, String> context) {
        return valueData;
    }

    /** Stores value into the file. */
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
        if (Utils.getToDateParam(context) == null) {
            return false;
        }

        Calendar toDate = Utils.getToDate(context);
        Calendar currentDate = DateUtils.truncate(Calendar.getInstance(), Calendar.DAY_OF_MONTH);

        return currentDate.after(toDate);
    }

    /** {@inheritedDoc} */
    protected ValueData evaluate(Map<String, String> context) throws IOException {
        if (Utils.isTimeUnitDay(context)) {
            ValueData valueData = executeScript(context);
            storeIfAllowed(valueData, context);

            return valueData;
        }

        Calendar fromDate = Utils.getFromDate(context);
        Calendar toDate = Utils.getToDate(context);

        ValueData total = null;

        Map<String, String> dayContext = Utils.newContext(context);
        while (!fromDate.after(toDate)) {
            Utils.putFromDate(dayContext, fromDate);
            Utils.putToDate(dayContext, fromDate);
            Utils.putTimeUnit(dayContext, TimeUnit.DAY);

            ValueData dayValue = getValue(dayContext);

            total = total == null ? dayValue : total.union(dayValue);

            fromDate.add(Calendar.DAY_OF_MONTH, 1);
        }

        return total;
    }

    /** {@inheritedDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return getScriptType().getParams();
    }

    protected ValueData executeScript(Map<String, String> context) throws IOException {
        ScriptExecutor sExecutor = getScriptExecutor();
        return sExecutor.execute(getScriptType(), context);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return getScriptType().getValueDataClass();
    }

    protected ScriptExecutor getScriptExecutor() {
        return new PigScriptExecutor();
    }

    /**
     * @return corresponding {@link ScriptType} for metric calculation.
     */
    abstract protected ScriptType getScriptType();
}
