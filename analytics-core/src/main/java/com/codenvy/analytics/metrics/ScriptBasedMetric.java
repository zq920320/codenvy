/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.scripts.ScriptType;
import com.codenvy.analytics.scripts.executor.ScriptExecutor;
import com.codenvy.analytics.scripts.executor.pig.PigScriptExecutor;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
abstract public class ScriptBasedMetric extends AbstractMetric {

    ScriptBasedMetric(MetricType metricType) {
        super(metricType);
    }

    /** {@inheritedDoc} */
    protected ValueData evaluate(Map<String, String> context) throws IOException {
        if (Utils.isTimeUnitDay(context)) {
            return executeScript(context);
        }

        Calendar fromDate = Utils.getFromDate(context);
        Calendar toDate = Utils.getToDate(context);

        ValueData total = null;

        Map<String, String> dayContext = new HashMap<String, String>(context);
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
    protected boolean isStoreAllowed(Map<String, String> context) throws IOException {
        return Utils.isTimeUnitDay(context) && super.isStoreAllowed(context);
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
