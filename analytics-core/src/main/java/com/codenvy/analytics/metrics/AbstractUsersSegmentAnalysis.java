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

import com.codenvy.analytics.metrics.value.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractUsersSegmentAnalysis extends CalculatedMetric {

    public AbstractUsersSegmentAnalysis(MetricType metricType) {
        super(metricType, MetricType.PRODUCT_USAGE_TIME_USERS);
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        context = Utils.newContext();
        Parameters.FROM_DATE.putDefaultValue(context);
        Parameters.TO_DATE.putDefaultValue(context);

        Map<String, FixedListLongValueData> by1day = getUsageByPeriod(context, 1);
        Map<String, FixedListLongValueData> by7day = getUsageByPeriod(context, 7);
        Map<String, FixedListLongValueData> by30day = getUsageByPeriod(context, 30);
        Map<String, FixedListLongValueData> by60day = getUsageByPeriod(context, 60);
        Map<String, FixedListLongValueData> by90day = getUsageByPeriod(context, 90);
        Map<String, FixedListLongValueData> by365day = getUsageByPeriod(context, 365);


        List<String> item = new ArrayList<>(6);
        item.add("" + getSuitedUsersNumber(by1day));
        item.add("" + getSuitedUsersNumber(by7day));
        item.add("" + getSuitedUsersNumber(by30day));
        item.add("" + getSuitedUsersNumber(by60day));
        item.add("" + getSuitedUsersNumber(by90day));
        item.add("" + getSuitedUsersNumber(by365day));

        return new ListListStringValueData(Arrays.asList(new ListStringValueData(item)));
    }

    private long getSuitedUsersNumber(Map<String, FixedListLongValueData> byPeriod) {
        long count = 0;
        for (FixedListLongValueData valueData : byPeriod.values()) {
            if (isAccepted(valueData)) {
                count++;
            }
        }

        return count;
    }

    private Map<String, FixedListLongValueData> getUsageByPeriod(Map<String, String> context, int period)
            throws IOException {

        try {

            context = Utils.clone(context);

            Calendar fromDate = Utils.getFromDate(context);
            Calendar toDate = Utils.getToDate(context);

            toDate.add(Calendar.DAY_OF_MONTH, 1 - period);

            if (fromDate.after(toDate)) {
                Utils.putToDate(context, fromDate);
            } else {
                Utils.putToDate(context, toDate);
            }

            return ((MapStringFixedLongListValueData)super.getValue(context)).getAll();
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListListStringValueData.class;
    }

    protected Long getUsageTime(FixedListLongValueData valueData) {
        return valueData.getAll().get(0);
    }

    protected Long getSessionsNumber(FixedListLongValueData valueData) {
        return valueData.getAll().get(1);
    }

    protected abstract boolean isAccepted(FixedListLongValueData valueData);
}
