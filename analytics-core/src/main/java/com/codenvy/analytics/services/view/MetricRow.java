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

import com.codenvy.analytics.Injector;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.*;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.InitialValueNotFoundException;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.pig.scripts.EventsHolder;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Anatoliy Bazko
 */
public class MetricRow extends AbstractRow {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("00");

    private static final String DEFAULT_NUMERIC_FORMAT = "%,.0f";
    private static final String DEFAULT_DATE_FORMAT    = "yyyy-MM-dd HH:mm:ss";
    private static final String DEFAULT_TIME_FORMAT    = "HH:mm:ss";

    /** The name of the metric. */
    private static final String NAME        = "name";
    private static final String DESCRIPTION = "description";

    /** The names of fields to fetch from multi-valued metric. */
    private static final String FIELDS = "fields";

    /** The names of fields of multi-valued metric which should be formatted corresponding to their type. */
    private static final String BOOLEAN_FIELDS = "boolean-fields";
    private static final String DATE_FIELDS    = "date-fields";
    private static final String TIME_FIELDS    = "time-fields";
    private static final String EVENT_FIELDS   = "event-fields";

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

    /**
     * The format for numeric fields. All fields are supposed to be numeric if there is no any indicator to tell
     * opposite. The value can be any supported java format.
     */
    private static final String NUMERIC_FORMAT = "numeric-format";

    private final Metric              metric;
    private final String              numericFormat;
    private final String[]            fields;
    private final boolean             hideNegativeValues;
    private final List<String>        booleanFields;
    private final Map<String, String> dateFields;
    private final Map<String, String> timeFields;
    private final List<String>        eventFields;
    private final boolean             isTimeField;

    private final EventsHolder eventsHolder;

    public MetricRow(Map<String, String> parameters) {
        this(MetricFactory.getMetric(parameters.get(NAME)), parameters);
    }

    /**
     * For testing purpose.
     *
     * @param metric
     *         the underlying Metric
     * @param parameters
     *         the list of additional parameters
     */
    protected MetricRow(Metric metric, Map<String, String> parameters) {
        super(parameters);

        this.metric = metric;
        this.eventsHolder = Injector.getInstance(EventsHolder.class);

        numericFormat = parameters.containsKey(NUMERIC_FORMAT)
                        ? parameters.get(NUMERIC_FORMAT)
                        : DEFAULT_NUMERIC_FORMAT;
        fields = parameters.containsKey(FIELDS) ? parameters.get(FIELDS).split(",") : new String[0];

        hideNegativeValues = parameters.containsKey(HIDE_NEGATIVE_VALUES) &&
                             Boolean.parseBoolean(parameters.get(HIDE_NEGATIVE_VALUES));

        booleanFields = parameters.containsKey(BOOLEAN_FIELDS)
                        ? Arrays.asList(parameters.get(BOOLEAN_FIELDS).split(","))
                        : new ArrayList<String>();

        this.dateFields = new HashMap<>();
        this.timeFields = new HashMap<>();

        readFieldsParameters(parameters, DATE_FIELDS, dateFields, DEFAULT_DATE_FORMAT);
        readFieldsParameters(parameters, TIME_FIELDS, timeFields, DEFAULT_TIME_FORMAT);

        if (parameters.containsKey(TIME_FIELDS)) {
            for (String timeField : parameters.get(TIME_FIELDS).split(",")) {
                if (timeField.contains("=")) {
                    String[] fieldAndFormat = timeField.split("=");
                    timeFields.put(fieldAndFormat[0], fieldAndFormat[1]);
                } else {
                    timeFields.put(timeField, DEFAULT_TIME_FORMAT);
                }
            }
        }

        eventFields = parameters.containsKey(EVENT_FIELDS)
                      ? Arrays.asList(parameters.get(EVENT_FIELDS).split(","))
                      : new ArrayList<String>();

        isTimeField = parameters.containsKey(TIME_FIELD) && Boolean.parseBoolean(parameters.get(TIME_FIELD));
    }

    private void readFieldsParameters(Map<String, String> parameters,
                                      String parameter,
                                      Map<String, String> fields,
                                      String defaultValue) {

        if (parameters.containsKey(parameter)) {
            for (String dateField : parameters.get(parameter).split(",")) {
                if (dateField.contains("=")) {
                    String[] fieldAndFormat = dateField.split("=");
                    fields.put(fieldAndFormat[0], fieldAndFormat[1]);
                } else {
                    fields.put(dateField, defaultValue);
                }
            }
        }
    }

    @Override
    public List<List<ValueData>> getData(Context initialContext, int iterationsCount) throws IOException {
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
        return metric.getValueDataClass() == ListValueData.class || metric.getValueDataClass() == SetValueData.class;
    }

    private List<List<ValueData>> getSingleValue(Context initialContext,
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
                formatAndAddSingleValue(DoubleValueData.DEFAULT, result);
            }

            initialContext = Utils.prevDateInterval(new Context.Builder(initialContext));
        }

        return Arrays.asList(result);
    }

    private void formatAndAddSingleValue(ValueData valueData, List<ValueData> singleValue) throws IOException {
        Class<? extends ValueData> clazz = valueData.getClass();

        if (clazz == StringValueData.class) {
            singleValue.add(valueData);

        } else if (clazz == LongValueData.class || clazz == DoubleValueData.class) {
            double value = ((NumericValueData)valueData).getAsDouble();

            ValueData formattedValue;
            if (isTimeField) {
                formattedValue = formatTimeValue(valueData, DEFAULT_TIME_FORMAT);
            } else {
                if (Double.isInfinite(value)
                    || Double.isNaN(value)
                    || (value < 0 && hideNegativeValues)) {

                    value = DoubleValueData.DEFAULT.getAsDouble();
                }

                formattedValue = new StringValueData(String.format(numericFormat, value));
            }

            singleValue.add(formattedValue);

        } else {
            throw new IOException("Unsupported class " + clazz);
        }
    }

    private List<List<ValueData>> getMultipleValues(Context initialContext) throws
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
                } else if (dateFields.containsKey(field)) {
                    singleValue.add(formatDateValue(item, dateFields.get(field)));
                } else if (timeFields.containsKey(field)) {
                    singleValue.add(formatTimeValue(item, timeFields.get(field)));
                } else if (eventFields.contains(field)) {
                    singleValue.add(formatEventValue(item));
                } else {
                    formatAndAddSingleValue(item, singleValue);
                }
            }

            multipleValues.add(singleValue);

        } else if (clazz == ListValueData.class) {
            for (ValueData item : ((ListValueData)valueData).getAll()) {
                formatAndAddMultipleValues(item, multipleValues);
            }

        } else if (clazz == SetValueData.class) {
            for (final ValueData item : ((SetValueData)valueData).getAll()) {
                Map<String, ValueData> map = new HashMap<String, ValueData>() {{
                    put(fields[0], item);
                }};

                formatAndAddMultipleValues(new MapValueData(map), multipleValues);
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

    private StringValueData formatDateValue(ValueData valueData, String format) {
        if (valueData == null || valueData.getAsString().isEmpty()) {
            return StringValueData.DEFAULT;
        }

        Long value = Long.valueOf(valueData.getAsString());
        String formattedValue = new SimpleDateFormat(format).format(value);

        return new StringValueData(formattedValue);
    }

    /**
     * @return the description of the event
     */
    private StringValueData formatEventValue(ValueData valueData) {
        try {
            String description = eventsHolder.getDescription(valueData.getAsString());
            return new StringValueData(description);
        } catch (IllegalArgumentException e) {
            return StringValueData.DEFAULT;
        }
    }

    private StringValueData formatTimeValue(ValueData valueData, String format) {
        long milliseconds = valueData.equals(StringValueData.DEFAULT) ? 0 : Long.parseLong(valueData.getAsString());

        format = convertToTimeString(milliseconds, format);

        return StringValueData.valueOf(format);
    }

    public static String convertToTimeString(long milliseconds, String format) {
        if (format == null) {
            format = DEFAULT_TIME_FORMAT;
        }
        
        int secs = (int)((milliseconds / 1000) % 60);
        int minutes = (int)((milliseconds / (1000 * 60)) % 60);
        int hours = (int)(milliseconds / (1000 * 60 * 60));

        format = format.replace("ss", DECIMAL_FORMAT.format(secs));
        format = format.replace("mm", DECIMAL_FORMAT.format(minutes));
        format = format.replace("HH", DECIMAL_FORMAT.format(hours));
        format = format.replace("\'", "");
        return format;
    }

    protected ValueData getMetricValue(Context context) throws IOException {
        return metric.getValue(context);
    }
}