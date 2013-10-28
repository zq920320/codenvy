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

import com.codenvy.analytics.Utils;
import com.codenvy.analytics.metrics.value.CassandraDataManager;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.ValueDataFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Map;

/**
 * It is supposed to loadValue calculated {@link com.codenvy.analytics.metrics.value.ValueData} from the storage.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class ReadBasedMetric extends AbstractMetric {

    public ReadBasedMetric(String metricName) {
        super(metricName);
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        try {
            Calendar fromDate = Utils.getFromDate(context);
            Calendar toDate = Utils.getToDate(context);

            ValueData total = ValueDataFactory.createDefaultValue(getValueDataClass());

            Map<String, String> dailyContext = Utils.clone(context);
            while (!fromDate.after(toDate)) {
                Utils.putFromDate(dailyContext, fromDate);
                Utils.putToDate(dailyContext, fromDate);
                Parameters.TIME_UNIT.put(dailyContext, Parameters.TimeUnit.DAY.name());

                ValueData dailyValue = evaluate(dailyContext);
                total = total.union(dailyValue);

                fromDate.add(Calendar.DAY_OF_MONTH, 1);
            }

            return total;
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    /** @return {@link com.codenvy.analytics.metrics.value.ValueData} */
    protected ValueData evaluate(Map<String, String> dailyContext) throws IOException {
        return loadValue(dailyContext);
    }

    private ValueData loadValue(Map<String, String> dailyContext) throws IOException {
        return CassandraDataManager.loadValue(this, dailyContext);
    }
}

