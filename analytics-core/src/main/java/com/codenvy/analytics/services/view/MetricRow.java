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


package com.codenvy.analytics.services.view;


import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.*;
import com.codenvy.analytics.metrics.InitialValueNotFoundException;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MetricRow extends AbstractRow {

    private static final String DEFAULT_NUMERIC_FORMAT = "%,.0f";
    private static final String DEFAULT_TIME_FORMAT    = "mm";
    private static final String DEFAULT_DATE_FORMAT    = "yyyy-MM-dd HH:mm:ss";

    /** The name of the metric. */
    private static final String NAME        = "name";
    private static final String DESCRIPTION = "description";

    /** The names of fields to fetch from multi-valued metric. */
    private static final String FIELDS = "fields";

    /** The names of fields of multi-valued metric which should be formatted corresponding to their type. */
    private static final String BOOLEAN_FIELDS = "boolean-fields";
    private static final String DATE_FIELDS    = "date-fields";
    private static final String TIME_FIELDS    = "time-fields";

    /**
     * Indicates if single-value should be treated as time and formatted in appropriate way.
     * Possible values: 'true' or 'false'.
     */
    private static final String TIME_FIELD = "time-field";

    /**
     * Indicates if single-value shouldn't be printed if its value is negative.
     * Possible values: 'true' or 'false'.
     */
    private static final String HIDE_NEGATIVE_VALUES = "hide-negative-values";

    /** The format for time fields. Supports 'mm' as the number of minutes and 'ss' as the number of seconds. */
    private static final String TIME_FORMAT = "time-format";

    /**
     * The format for numeric fields. All fields are supposed to be numeric if there is no any indicator to tell
     * opposite. The value can be any supported java format.
     */
    private static final String NUMERIC_FORMAT = "numeric-format";

    private final Metric       metric;
    private final String       numericFormat;
    private final String[]     fields;
    private final boolean      hideNegativeValues;
    private final List<String> booleanFields;
    private final List<String> dateFields;
    private final List<String> timeFields;
    private final boolean      isTimeField;
    private final String       timeFormat;

    public MetricRow(Map<String, String> parameters) {
        super(parameters);

        metric = MetricFactory.getMetric(parameters.get(NAME));
        numericFormat =
                parameters.containsKey(NUMERIC_FORMAT) ? parameters.get(NUMERIC_FORMAT) : DEFAULT_NUMERIC_FORMAT;
        fields = parameters.containsKey(FIELDS) ? parameters.get(FIELDS).split(",") : new String[0];

        hideNegativeValues = parameters.containsKey(HIDE_NEGATIVE_VALUES) &&
                             Boolean.parseBoolean(parameters.get(HIDE_NEGATIVE_VALUES));
        booleanFields =
                parameters.containsKey(BOOLEAN_FIELDS) ? Arrays.asList(parameters.get(BOOLEAN_FIELDS).split(","))
                                                       : new ArrayList<String>();
        dateFields = parameters.containsKey(DATE_FIELDS) ? Arrays.asList(parameters.get(DATE_FIELDS).split(","))
                                                         : new ArrayList<String>();
        timeFields = parameters.containsKey(TIME_FIELDS) ? Arrays.asList(parameters.get(TIME_FIELDS).split(","))
                                                         : new ArrayList<String>();
        isTimeField = parameters.containsKey(TIME_FIELD) && Boolean.parseBoolean(parameters.get(TIME_FIELD));
        timeFormat = parameters.containsKey(TIME_FORMAT) ? parameters.get(TIME_FORMAT) : DEFAULT_TIME_FORMAT;
    }

    @Override
    public List<List<ValueData>> getData(Map<String, String> initialContext, int iterationsCount) throws IOException {
        try {
            if (isMultipleColumnsMetric()) {
                return getMultipleValues(initialContext);
            } else {
                return getSingleValue(initialContext, iterationsCount);
            }
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    private boolean isMultipleColumnsMetric() {
        return metric.getValueDataClass() == ListValueData.class;
    }

    private List<List<ValueData>> getSingleValue(Map<String, String> initialContext,
                                                 int iterationsCount) throws IOException,
                                                                             ParseException {
        List<ValueData> result = new ArrayList<>();

        boolean descriptionExists = parameters.containsKey(DESCRIPTION);
        if (descriptionExists) {
            result.add(new StringValueData(parameters.get(DESCRIPTION)));
        }

        for (int i = descriptionExists ? 1 : 0; i < iterationsCount; i++) {
            try {
                formatAndAddSingleValue(getMetricValue(initialContext), result);
            } catch (InitialValueNotFoundException e) {
                result.add(StringValueData.DEFAULT);
            }

            initialContext = Utils.prevDateInterval(initialContext);
        }

        return Arrays.asList(result);
    }

    private void formatAndAddSingleValue(ValueData valueData, List<ValueData> singleValue) throws IOException {
        Class<? extends ValueData> clazz = valueData.getClass();

        if (clazz == StringValueData.class) {
            singleValue.add(valueData);

        } else if (clazz == LongValueData.class) {
            double value = ((LongValueData)valueData).getAsDouble();

            ValueData formattedValue;
            if (value == 0 || (value < 0 && hideNegativeValues)) {
                formattedValue = StringValueData.DEFAULT;
            } else if (isTimeField) {
                formattedValue = formatTimeValue(valueData);
            } else {
                formattedValue = new StringValueData(String.format(numericFormat, value));
            }

            singleValue.add(formattedValue);

        } else if (clazz == DoubleValueData.class) {
            double value = ((DoubleValueData)valueData).getAsDouble();

            ValueData formattedValue;
            if (value == 0 || Double.isInfinite(value) || Double.isNaN(value) || (value < 0 && hideNegativeValues)) {
                formattedValue = StringValueData.DEFAULT;
            } else {
                formattedValue = new StringValueData(String.format(numericFormat, value));
            }

            singleValue.add(formattedValue);
        } else {
            throw new IOException("Unsupported class " + clazz);
        }
    }

    private List<List<ValueData>> getMultipleValues(Map<String, String> initialContext) throws
                                                                                        IOException,
                                                                                        ParseException {
        List<List<ValueData>> result = new ArrayList<>();
        formatAndAddMultipleValues(getMetricValue(initialContext), result);

        return result;
    }


    private void formatAndAddMultipleValues(ValueData valueData, List<List<ValueData>> multipleValues) throws
                                                                                                       IOException {
        Class<? extends ValueData> clazz = valueData.getClass();

        if (clazz == MapValueData.class) {
            Map<String, ValueData> items = ((MapValueData)valueData).getAll();

            List<ValueData> singleValue = new ArrayList<>();
            if (parameters.containsKey(DESCRIPTION)) {
                singleValue.add(new StringValueData(parameters.get(DESCRIPTION)));
            }

            for (String field : fields) {
                ValueData item = items.containsKey(field) ? items.get(field) : StringValueData.DEFAULT;
                if (booleanFields.contains(field)) {
                    singleValue.add(formatBooleanValue(item));
                } else if (dateFields.contains(field)) {
                    singleValue.add(formatDateValue(item));
                } else if (timeFields.contains(field)) {
                    singleValue.add(formatTimeValue(item));
                } else {
                    formatAndAddSingleValue(item, singleValue);
                }
            }

            multipleValues.add(singleValue);

        } else if (clazz == ListValueData.class) {
            for (ValueData item : ((ListValueData)valueData).getAll()) {
                formatAndAddMultipleValues(item, multipleValues);
            }
        } else {
            throw new IOException("Unsupported class " + clazz);
        }
    }

    private StringValueData formatBooleanValue(ValueData valueData) {
        String value = valueData.getAsString();
        String formattedValue = value.equals("1") ? "Yes" : "No";

        return new StringValueData(formattedValue);
    }

    private StringValueData formatDateValue(ValueData valueData) {
        Long value = new Long(valueData.getAsString());
        String formattedValue = new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(value);

        return new StringValueData(formattedValue);
    }

    private StringValueData formatTimeValue(ValueData valueData) {
        long timeInSeconds = valueData.equals(StringValueData.DEFAULT) ? 0 : Long.parseLong(valueData.getAsString());
        double min = timeInSeconds / 60;
        long sec = timeInSeconds % 60;

        if (min == 0 && sec == 0) {
            return StringValueData.DEFAULT;
        }

        String value = timeFormat.replace("mm", String.format(numericFormat, min)).replace("ss", "" + sec);
        return StringValueData.valueOf(value);
    }

    protected ValueData getMetricValue(Map<String, String> context) throws IOException {
        return metric.getValue(context);
    }
}