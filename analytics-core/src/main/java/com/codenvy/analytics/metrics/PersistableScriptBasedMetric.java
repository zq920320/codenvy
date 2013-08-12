/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */


package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.FSValueDataManager;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.ValueDataFactory;

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

    /** {@inheritDoc} */
    public ValueData getValue(Map<String, String> context) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Calculation " + getType() + " with context " + context.toString());
        }

        Calendar fromDate = Utils.getFromDate(context);
        Calendar toDate = Utils.getToDate(context);

        ValueData total = ValueDataFactory.createDefaultValue(getValueDataClass());

        Map<String, String> dayContext = Utils.clone(context);
        while (!fromDate.after(toDate)) {
            Utils.putFromDate(dayContext, fromDate);
            Utils.putToDate(dayContext, fromDate);
            Utils.putTimeUnit(dayContext, TimeUnit.DAY);

            ValueData dayValue = evaluateAndStore(dayContext);
            total = total.union(dayValue);

            fromDate.add(Calendar.DAY_OF_MONTH, 1);
        }

        return total;
    }


    protected ValueData evaluateAndStore(Map<String, String> context) throws IOException {
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

    /** Stores value into the file. */
    protected void store(ValueData value, LinkedHashMap<String, String> uuid) throws IOException {
        FSValueDataManager.storeValue(value, metricType, uuid);
    }

    /** Loads value from the file. */
    protected ValueData load(LinkedHashMap<String, String> uuid) throws IOException {
        return FSValueDataManager.loadValue(metricType, uuid);
    }

    /** @return if it is allowed to preserve calculated data. */
    protected boolean isStoreAllowed(Map<String, String> context) throws IOException {
        Calendar toDate = Utils.getToDate(context);
        Calendar currentDate = DateUtils.truncate(Calendar.getInstance(), Calendar.DAY_OF_MONTH);

        return currentDate.after(toDate);
    }
}
