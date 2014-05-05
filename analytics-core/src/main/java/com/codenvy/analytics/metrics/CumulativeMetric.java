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

import com.codenvy.analytics.Injector;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;

/**
 * The value of the metric will be calculated as: previous value + added value - removed value.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class CumulativeMetric extends AbstractMetric {

    private final Metric                addedMetric;
    private final Metric                removedMetric;
    private final InitialValueContainer initialValueContainer;


    protected CumulativeMetric(MetricType metricType, Metric addedMetric, Metric removedMetric) {
        this(metricType.toString(), addedMetric, removedMetric);
    }

    public CumulativeMetric(String metricType, Metric addedMetric, Metric removedMetric) {
        super(metricType);

        this.addedMetric = addedMetric;
        this.removedMetric = removedMetric;

        initialValueContainer = Injector.getInstance(InitialValueContainer.class);
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        initialValueContainer.validateExistenceInitialValueBefore(context);

        Calendar fromDate = (Calendar)initialValueContainer.getInitialValueDate().clone();
        fromDate.add(Calendar.DAY_OF_MONTH, 1);

        context = context.cloneAndPut(Parameters.FROM_DATE, fromDate);

        try {
            return doGetValue(context);
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    private boolean isSimplified(Context context) {
        return !context.exists(Parameters.SORT) &&
               !context.exists(Parameters.PAGE) &&
               context.getFilters().isEmpty();
    }

    private ValueData doGetValue(Context context) throws IOException, ParseException {
        LongValueData addedEntity = ValueDataUtil.getAsLong(addedMetric, context);
        LongValueData removedEntity = ValueDataUtil.getAsLong(removedMetric, context);
        LongValueData initialValue = isSimplified(context) ? initialValueContainer.getInitialValue(metricName) : LongValueData.DEFAULT;

        return new LongValueData(initialValue.getAsLong() + addedEntity.getAsLong() - removedEntity.getAsLong());
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }
}
