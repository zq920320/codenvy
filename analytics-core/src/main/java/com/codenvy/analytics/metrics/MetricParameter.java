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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public enum MetricParameter {
    EVENT,
    URL,
    FIELD,
    PARAM,
    ALIAS,

    TIME_UNIT {
        @Override
        public String getDefaultValue() {
            return TimeUnit.DAY.toString();
        }

        @Override
        public void validate(String value, Map<String, String> context) throws IllegalStateException {
            TimeUnit.valueOf(value.toUpperCase());
        }
    },

    FROM_DATE {
        @Override
        public String getDefaultValue() {
            return "20120101";
        }

        @Override
        public void validate(String value, Map<String, String> context) throws IllegalStateException {
            try {
                Calendar fromDate = Utils.parseDate(value);
                Calendar minDate = Utils.parseDate(getDefaultValue());

                if (fromDate.before(minDate)) {
                    throw new IllegalArgumentException("The illegal FROM_DATE parameter value '"
                                                       + Utils.formatDate(fromDate)
                                                       + "' The lowest allowed date is '"
                                                       + Utils.formatDate(minDate)
                                                       + "'");
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("FROM_DATE parameter has illegal format '" + value
                                                   + "' The only supported format is '" + PARAM_DATE_FORMAT + "'");
            }
        }
    },

    ENTITY {
        @Override
        public void validate(String value, Map<String, String> context) throws IllegalStateException {
            for (ENTITY_TYPE eType : ENTITY_TYPE.values()) {
                if (eType.name().equals(value)) {
                    return;
                }
            }

            throw new IllegalArgumentException("The illegal ENTITY parameter value " + value);
        }
    },

    RESULT_DIR { // TODO remove

        @Override
        public String getDefaultValue() {
            return null;
        }
    },

    LOAD_DIR,
    STORE_DIR,

    TO_DATE {
        @Override
        public String getDefaultValue() {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -1);

            DateFormat df = new SimpleDateFormat(MetricParameter.PARAM_DATE_FORMAT);
            return df.format(calendar.getTime());
        }

        @Override
        public void validate(String value, Map<String, String> context) throws IllegalStateException {
            try {
                Calendar toDate = Utils.parseDate(value);
                Calendar maxDate = Utils.parseDate(getDefaultValue());

                if (toDate.after(maxDate)) {
                    throw new IllegalArgumentException("The illegal TO_DATE parameter value: '"
                                                       + Utils.formatDate(toDate)
                                                       + "' The higest allowed date is '"
                                                       + Utils.formatDate(maxDate)
                                                       + "'");

                }

                if (MetricParameter.FROM_DATE.exists(context)) {
                    Calendar fromDate = Utils.getFromDate(context);
                    if (fromDate.after(toDate)) {
                        throw new IllegalArgumentException("The illegal TO_DATE parameter value: '"
                                                           + Utils.formatDate(toDate)
                                                           + "'. Should be higher than fromDate parameter value: '"
                                                           + Utils.formatDate(fromDate)
                                                           + "'");
                    }
                }

            } catch (IOException e) {
                throw new IllegalArgumentException("TO_DATE parameter has illegal format '" + value
                                                   + "'. The only supported format is 'yyyyMMdd'");
            }
        }
    };

    /** Puts value into execution context */
    public void put(Map<String, String> context, String value) {
        context.put(this.name(), value);
    }

    /** Puts default value into execution context */
    public void putDefaultValue(Map<String, String> context) {
        context.put(this.name(), getDefaultValue());
    }

    /** Gets value from execution context */
    public String get(Map<String, String> context) {
        return context.get(this.name());
    }

    /** @return true if context contains given parameter */
    public boolean exists(Map<String, String> context) {
        return context.get(this.name()) != null;
    }

    /** @return true if name is the name of current parameter */
    public boolean isParam(String name) {
        return this.name().equals(name);
    }

    /** @return the default value for given parameter. */
    public String getDefaultValue() {
        return "";
    }

    /** Validates the value of parameter. Throws {@link IllegalArgumentException} if something wrong. */
    public void validate(String value, Map<String, String> context) throws IllegalStateException {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(this.name() + " parameter is null or empty");
        }
    }

    /** The date format is used in scripts. */
    public static final String PARAM_DATE_FORMAT = "yyyyMMdd";

    /** Enumeration for {@link MetricParameter#ENTITY} */
    public enum ENTITY_TYPE {
        WS,
        DOMAINS,
        USERS,
        COMPANIES,
        URL
    }
}
