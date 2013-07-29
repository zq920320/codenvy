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

import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.shared.RowData;
import com.codenvy.analytics.shared.TableData;

import java.util.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class Table {

    private static final String ATTRIBUTE_LENGTH             = "length";
    private static final String ATTRIBUTE_TIME_INTERVAL_RULE = "time-interval-rule";

    private final Map<String, String> attributes;
    private final List<Row>           rows;

    /** {@link Table} constructor. */
    public Table(Map<String, String> attributes, List<Row> rows) {
        this.attributes = new HashMap<>(attributes);
        this.rows = new ArrayList<>(rows);
    }

    public TableData retrieveData(Map<String, String> context) throws Exception {
        TableData data = new TableData(attributes);

        context = overrideTimeInterval(context);

        TimeIntervalRule timeIntervalRule = getTimeIntervalRule();
        int columnsCount = getColumnsCount();

        // Timeline usecase with TimeUnit.LIFETIME
        if (Utils.containsTimeUnitParam(context) && Utils.getTimeUnit(context) == TimeUnit.LIFETIME &&
            timeIntervalRule == TimeIntervalRule.NONE) {
            columnsCount = 2;
        }

        for (Row row : rows) {
            List<RowData> rowDatas = row.retrieveData(context, columnsCount, timeIntervalRule);
            data.addAll(rowDatas);
        }

        return data;
    }

    private Map<String, String> overrideTimeInterval(Map<String, String> context) {
        switch (getTimeIntervalRule()) {
            case NONE:
                return context;

            case LIFETIME_DECREASED_MONTHLY:
                context = Utils.clone(context);

                Calendar toDate = Calendar.getInstance();
                toDate.set(Calendar.DAY_OF_MONTH, toDate.getActualMaximum(Calendar.DAY_OF_MONTH));

                Utils.putTimeUnit(context, TimeUnit.LIFETIME);
                Utils.putToDate(context, toDate);
                Utils.putFromDateDefault(context);

                return context;

            default:
                throw new IllegalStateException();
        }
    }

    private TimeIntervalRule getTimeIntervalRule() {
        String attr = attributes.get(ATTRIBUTE_TIME_INTERVAL_RULE);
        return attr == null ? TimeIntervalRule.NONE : TimeIntervalRule.valueOf(attr.toUpperCase());
    }

    public List<Row> getRows() {
        return Collections.unmodifiableList(rows);
    }

    public int getColumnsCount() {
        return Integer.valueOf(attributes.get(ATTRIBUTE_LENGTH));
    }

    public enum TimeIntervalRule {
        /** The default behaviour, parameters of context will not be overridden. */
        NONE,

        /**
         * The parameters of context will be overridden locally. The {@link com.codenvy.analytics.metrics
         * .MetricParameter#TO_DATE} is the last day of month and the
         * {@link com.codenvy.analytics.metrics.MetricParameter#FROM_DATE} is the very beginning day. The previous
         * period will be calculated by decreasing the month of {@link com.codenvy.analytics.metrics
         * .MetricParameter#TO_DATE} parameter.
         */
        LIFETIME_DECREASED_MONTHLY
    }
}
