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
package com.codenvy.analytics.metrics.top;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collections;

/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
public abstract class AbstractTopMetrics extends ReadBasedMetric {

    public static final long MAX_DOCUMENT_COUNT = 100;
    public static final int  LIFE_TIME_PERIOD   = -1;

    private int dayCount;

    public AbstractTopMetrics(MetricType metricType, int dayCount) {
        super(metricType);
        this.dayCount = dayCount;
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }

    @Override
    public Context applySpecificFilter(Context context) throws IOException {
        Context.Builder builder = new Context.Builder();
        builder.putAll(context);
        builder.putDefaultValue(Parameters.TO_DATE);

        if (this.dayCount == LIFE_TIME_PERIOD) {
            builder.putDefaultValue(Parameters.FROM_DATE);
        } else {
            try {
                Calendar date = context.getAsDate(Parameters.TO_DATE);
                date.add(Calendar.DAY_OF_MONTH, 1 - dayCount);

                builder.put(Parameters.FROM_DATE, date);
            } catch (ParseException e) {
                throw new IOException(e);
            }
        }

        return builder.build();
    }

    /** @return mongodb operation (100% * (subjectField / predicateField)) */
    protected BasicDBObject getRateOperation(String subjectField, String predicateField) {
        BasicDBList divideArgs = new BasicDBList();
        divideArgs.add(subjectField);
        divideArgs.add(predicateField);

        BasicDBList multiplyArgs = new BasicDBList();
        multiplyArgs.add(100);
        multiplyArgs.add(new BasicDBObject("$divide", divideArgs));

        return new BasicDBObject("$multiply", multiplyArgs);
    }

    /** @return mongodb operation (arg1 - arg2Field) */
    protected BasicDBObject getSubtractOperation(long arg1, String arg2Field) {
        BasicDBList subtractArgs = new BasicDBList();
        subtractArgs.add(arg1);
        subtractArgs.add(arg2Field);

        return new BasicDBObject("$subtract", subtractArgs);
    }

    protected BasicDBObject getAndOperation(BasicDBObject... predicates) {
        BasicDBList andArgs = new BasicDBList();

        Collections.addAll(andArgs, predicates);

        return new BasicDBObject("$and", andArgs);
    }
}
