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

