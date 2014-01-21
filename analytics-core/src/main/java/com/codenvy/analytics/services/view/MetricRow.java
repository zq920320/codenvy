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
import com.codenvy.analytics.metrics.Parameters;

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
    public static final  String DATE_FORMAT            = "yyyy-MM-dd HH:mm:ss";

    private static final String NAME                           = "name";
    private static final String FORMAT                         = "format";
    private static final String DESCRIPTION                    = "description";
    private static final String FIELDS                         = "fields";
    private static final String HIDE_NEGATIVE_VALUES           = "hide-negative-values";
    private static final String SET_FROM_DATE_TO_DEFAULT_VALUE = "set-from-date-to-default-value";
    private static final String BOOLEAN_FIELDS                 = "boolean-fields";
    private static final String DATE_FIELDS                    = "date-fields";
    private static final String TIME_FIELDS                    = "time-fields";


    private final Metric       metric;
    private final String       format;
    private final String[]     fields;
    private final boolean      hideNegativeValues;
    private final List<String> booleanFields;
    private final List<String> dateFields;
    private final List<String> timeFields;

    public MetricRow(Map<String, String> parameters) {
        super(parameters);

        metric = MetricFactory.getMetric(parameters.get(NAME));
        format = parameters.containsKey(FORMAT) ? parameters.get(FORMAT) : DEFAULT_NUMERIC_FORMAT;
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

    private void formatTimeValue(ValueData valueData, List<ValueData> singleValue) {
        long timeInSeconds = valueData.equals(StringValueData.DEFAULT) ? 0 : Long.parseLong(valueData.getAsString());
        singleValue.add(new StringValueData(
                (timeInSeconds / 60)
                + " min."
                + ((timeInSeconds % 60) == 0 ? "" : (timeInSeconds % 60) + " sec.")));
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
            } else {
                formattedValue = new StringValueData(String.format(format, value));
            }

            singleValue.add(formattedValue);

        } else if (clazz == DoubleValueData.class) {
            double value = ((DoubleValueData)valueData).getAsDouble();

            ValueData formattedValue;
            if (value == 0 || Double.isInfinite(value) || Double.isNaN(value) || (value < 0 && hideNegativeValues)) {
                formattedValue = StringValueData.DEFAULT;
            } else {
                formattedValue = new StringValueData(String.format(format, value));
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
                    formatAndAddBooleanValue(item, singleValue);
                } else if (dateFields.contains(field)) {
                    formatAndAddDateValue(item, singleValue);
                } else if (timeFields.contains(field)) {
                    formatTimeValue(item, singleValue);
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

    private void formatAndAddBooleanValue(ValueData valueData, List<ValueData> singleValue) {
        String value = valueData.getAsString();
        String formattedValue = value.equals("1") ? "Yes" : "No";

        singleValue.add(new StringValueData(formattedValue));
    }

    private void formatAndAddDateValue(ValueData valueData, List<ValueData> singleValue) {
        Long value = new Long(valueData.getAsString());
        String formattedValue = new SimpleDateFormat(DATE_FORMAT).format(value);

        singleValue.add(new StringValueData(formattedValue));
    }


    protected ValueData getMetricValue(Map<String, String> context) throws IOException {
        if (parameters.containsKey(SET_FROM_DATE_TO_DEFAULT_VALUE)
            && Boolean.parseBoolean(parameters.get(SET_FROM_DATE_TO_DEFAULT_VALUE))) {

            context = Utils.clone(context);
            Parameters.FROM_DATE.putDefaultValue(context);
        }

        return metric.getValue(context);
    }
}