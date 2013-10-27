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


package com.codenvy.analytics.old_metrics;

import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.old_metrics.value.ValueData;
import com.codenvy.analytics.old_metrics.value.ValueDataFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * It is supposed to read precalculated {@link com.codenvy.analytics.old_metrics.value.ValueData} from storage.
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
        try {
            Calendar fromDate = Utils.getFromDate(context);
            Calendar toDate = Utils.getToDate(context);

            ValueData total = createEmptyValueData();

            Map<String, String> dayContext = Utils.clone(context);
            while (!fromDate.after(toDate)) {
                Utils.putFromDate(dayContext, fromDate);
                Utils.putToDate(dayContext, fromDate);
                Parameters.TIME_UNIT.put(dayContext, TimeUnit.DAY.name());

                ValueData dayValue = evaluate(dayContext);
                total = total.union(dayValue);

                fromDate.add(Calendar.DAY_OF_MONTH, 1);
            }

            return total;
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    /** @return {@link com.codenvy.analytics.old_metrics.value.ValueData} */
    protected ValueData evaluate(Map<String, String> dayContext) throws IOException {
        return getFilteredValue(makeUUID(dayContext), dayContext);
    }

    private ValueData getFilteredValue(LinkedHashMap<String, String> uuid, Map<String, String> dayContext)
            throws IOException {

        Set<MetricFilter> availableFilters = Utils.getAvailableFilters(dayContext);

        if (availableFilters.isEmpty()) {
            return getDirectValue(uuid);
        } else if (availableFilters.size() > 1) {
            throw new IllegalStateException("Number of filters greater then 1");
        }

        ValueData valueData = createEmptyValueData();

        for (MetricFilter filter : availableFilters) {
            String[] allFilterValues = dayContext.get(filter.name()).split(",");

            for (String filterValue : allFilterValues) {
                putFilterValue(filter, filterValue.trim(), uuid);

                Map<String, String> clonedDayContext = Utils.clone(dayContext);
                clonedDayContext.remove(filter.name());

                valueData = valueData.union(getFilteredValue(uuid, clonedDayContext));
            }
        }

        return valueData;
    }

    private void putFilterValue(MetricFilter filterKey, String filterValue, LinkedHashMap<String, String> uuid) {
        Parameters.FILTER.put(uuid, filterKey.name());
        filterKey.put(uuid, filterValue);
    }

    /**
     * @return {@link com.codenvy.analytics.old_metrics.value.ValueData} from the storage, if data is absent then empty
     *         value will be returned
     */
    private ValueData getDirectValue(LinkedHashMap<String, String> uuid) throws IOException {
        try {
            return read(metricType, uuid);
        } catch (FileNotFoundException e) {
            return createEmptyValueData();
        }
    }

    /** @return empty {@link com.codenvy.analytics.old_metrics.value.ValueData} */
    private ValueData createEmptyValueData() throws IOException {
        return ValueDataFactory.createDefaultValue(getValueDataClass());
    }

    protected abstract ValueData read(MetricType metricType, LinkedHashMap<String, String> uuid) throws IOException;
}

