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


package com.codenvy.analytics.server.vew.template;

import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.DoubleValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.shared.RowData;

import java.io.IOException;
import java.util.*;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractRow implements Row {

    /** Transforms {@link ValueData} formatted string. */
    protected String getAsString(ValueData valueData, String format) {
        if (isZeroValue(valueData)) {
            return "";
        }

        if (format.contains("d")) {
            return String.format(format, valueData.getAsLong());
        } else if (format.contains("f")) {
            return String.format(format, valueData.getAsDouble());
        } else if (format.contains("s")) {
            return String.format(format, valueData.getAsString());
        }

        return valueData.getAsString();
    }

    /** Checks if we are dealing with zero value. */
    protected boolean isZeroValue(ValueData value) {
        if (value instanceof DoubleValueData &&
            (Double.isNaN(value.getAsDouble()) || Double.isInfinite(value.getAsDouble()) || value.getAsDouble() == 0)) {
            return true;
        } else if (value instanceof LongValueData && value.getAsLong() == 0) {
            return true;
        }

        return false;
    }

    /** Gets the previous time period. */
    protected Map<String, String> prevDateInterval(Map<String, String> context,
                                                   Table.TimeIntervalRule overrideContextRule) throws IOException {
        switch (overrideContextRule) {
            case NONE:
                return Utils.prevDateInterval(context);

            case LIFETIME_DECREASED_MONTHLY:
                context = Utils.clone(context);

                Calendar toDate = Utils.getToDate(context);
                toDate.add(Calendar.MONTH, -1);
                toDate.set(Calendar.DAY_OF_MONTH, toDate.getActualMaximum(Calendar.DAY_OF_MONTH));

                Utils.putToDate(context, toDate);

                return context;

            default:
                throw new IllegalStateException("Unknown parameter " + overrideContextRule);
        }
    }

    /** {@inheritedDoc} */
    public List<RowData> retrieveData(Map<String, String> context, int columnsCount,
                                      Table.TimeIntervalRule overrideContextRule) throws Exception {
        RowData row = new RowData();

        row.add(doRetrieve(context, 0));
        row.add(doRetrieve(context, 1));

        for (int i = 2; i < columnsCount; i++) {
            context = prevDateInterval(context, overrideContextRule);
            row.add(doRetrieve(context, i));
        }

        return new ArrayList<>(Arrays.asList(row));
    }

    protected abstract String doRetrieve(Map<String, String> context, int columnNumber) throws IOException;

}
