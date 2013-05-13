/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.FSValueDataManager;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.ValueDataFilter;
import com.codenvy.analytics.scripts.ScriptType;
import com.codenvy.analytics.scripts.executor.ScriptExecutor;
import com.codenvy.analytics.scripts.executor.pig.PigScriptExecutor;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
abstract public class ScriptBasedMetric extends AbstractMetric {

    private static final Logger                                   LOGGER     = LoggerFactory.getLogger(ScriptBasedMetric.class);
    private static final ConcurrentHashMap<String, AtomicBoolean> executions = new ConcurrentHashMap<String, AtomicBoolean>();

    ScriptBasedMetric(MetricType metricType) {
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

        Map<String, String> dayContext = Utils.newContext(context);
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

    /**
     * Filtering data result depending on execution context.
     */
    protected ValueData doFilter(ValueData valueData, Map<String, String> context) {
        if (isFilterSupported()) {
            for (String param : FILTERS_PARAM) {
                if (context.containsKey(param)) {
                    ValueDataFilter filter = createFilter(valueData);
                    valueData = filter.doFilter(param, context.get(param));
                }
            }
        }

        return valueData;
    }

    /**
     * @return {@link ValueDataFilter} over {@link ValueData}
     */
    protected ValueDataFilter createFilter(ValueData valueData) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return true if metric supports filtering otherwise return false
     */
    protected boolean isFilterSupported() {
        return false;
    }

    /** Stores value into the file. */
    protected void store(ValueData value, Map<String, String> uuid) throws IOException {
        FSValueDataManager.store(value, metricType, uuid);
    }

    /** Loads value from the file. */
    protected ValueData load(Map<String, String> uuid) throws IOException {
        return FSValueDataManager.load(metricType, uuid);
    }

    /** @return if it is allowed to preserve calculated data. */
    protected boolean isStoreAllowed(Map<String, String> context) throws IOException {
        Calendar toDate = Utils.getToDate(context);
        Calendar currentDate = DateUtils.truncate(Calendar.getInstance(), Calendar.DAY_OF_MONTH);

        return currentDate.after(toDate);
    }

    /** {@inheritedDoc} */
    @Override
    protected ValueData evaluate(Map<String, String> context) throws IOException {
        Map<String, String> uuid = makeUUID(context);

        String executionKey = metricType.name() + uuid.toString();

        acquire(executionKey);
        try {
            ValueData valueData;

            try {
                valueData = load(uuid);
            } catch (FileNotFoundException e) {
                valueData = executeScript(context);

                if (isStoreAllowed(context)) {
                    store(valueData, uuid);
                }
            }

            return valueData;
        } finally {
            release(executionKey);
        }
    }

    protected void acquire(String executionKey) {
        AtomicBoolean monitor = new AtomicBoolean(true);

        AtomicBoolean currentValue = executions.putIfAbsent(executionKey, monitor);
        if (currentValue != null) {
            synchronized (currentValue) {
                while (!currentValue.get()) {
                    try {
                        currentValue.wait();
                    } catch (InterruptedException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    protected void release(String executionKey) {
        AtomicBoolean monitor = executions.remove(executionKey);
        if (monitor != null) {
            monitor.set(false);

            synchronized (monitor) {
                monitor.notifyAll();
            }
        }
    }

    /** {@inheritedDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return getScriptType().getParams();
    }

    protected ValueData executeScript(Map<String, String> context) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Script execution " + getScriptType() + " with context " + context.toString());
        }

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
