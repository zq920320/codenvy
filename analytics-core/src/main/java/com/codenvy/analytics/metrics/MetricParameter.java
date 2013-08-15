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
    // TODO
    EVENT {
        @Override
        public String getDefaultValue() {
            return "";
        }

        @Override
        public void validate(String value, Map<String, String> context) throws IllegalStateException {
            if (value == null || value.isEmpty()) {
                throw new IllegalArgumentException("EVENT parameter is null or empty");
            }
        }
    },

    URL {
        @Override
        public String getDefaultValue() {
            return "";
        }

        @Override
        public void validate(String value, Map<String, String> context) throws IllegalStateException {
            if (value == null || value.isEmpty()) {
                throw new IllegalArgumentException("URL parameter is null or empty");
            }
        }
    },

    FIELD {
        @Override
        public String getDefaultValue() {
            return "";
        }

        @Override
        public void validate(String value, Map<String, String> context) throws IllegalStateException {
            if (value == null || value.isEmpty()) {
                throw new IllegalArgumentException("FIELD parameter is null or empty");
            }
        }
    },

    PARAM {
        @Override
        public String getDefaultValue() {
            return "";
        }

        @Override
        public void validate(String value, Map<String, String> context) throws IllegalStateException {
            if (value == null || value.isEmpty()) {
                throw new IllegalArgumentException("PARAM parameter is null or empty");
            }
        }
    },

    ALIAS {
        @Override
        public String getDefaultValue() {
            return "";
        }

        @Override
        public void validate(String value, Map<String, String> context) throws IllegalStateException {
            if (value == null || value.isEmpty()) {
                throw new IllegalArgumentException("ALIAS parameter is null or empty");
            }
        }
    },

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
                                                       + Utils.formatDate(fromDate, PARAM_DATE_FORMAT)
                                                       + "' The lowest allowed date is '"
                                                       + Utils.formatDate(minDate, PARAM_DATE_FORMAT)
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
        public String getDefaultValue() {
            return null;
        }

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

    INTERVAL {
        @Override
        public String getDefaultValue() {
            return null;
        }

        @Override
        public void validate(String value, Map<String, String> context) throws IllegalStateException {
            if (value == null) {
                throw new IllegalArgumentException("INTERVAL parameter is null");
            }
        }
    },

    RESULT_DIR {
        @Override
        public String getDefaultValue() {
            return null;
        }

        @Override
        public void validate(String value, Map<String, String> context) throws IllegalStateException {
            if (value == null) {
                throw new IllegalArgumentException("RESULT_DIR parameter is null");
            }
        }
    },

    LOAD_DIR {
        @Override
        public String getDefaultValue() {
            return null;
        }

        @Override
        public void validate(String value, Map<String, String> context) throws IllegalStateException {
            if (value == null) {
                throw new IllegalArgumentException("LOAD_DIR parameter is null");
            }
        }
    },

    STORE_DIR {
        @Override
        public String getDefaultValue() {
            return null;
        }

        @Override
        public void validate(String value, Map<String, String> context) throws IllegalStateException {
            if (value == null) {
                throw new IllegalArgumentException("STORE_DIR parameter is null");
            }
        }
    },

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
                                                       + Utils.formatDate(toDate, PARAM_DATE_FORMAT)
                                                       + "' The higest allowed date is '"
                                                       + Utils.formatDate(maxDate, PARAM_DATE_FORMAT)
                                                       + "'");

                }

                if (Utils.containsFromDateParam(context)) {
                    Calendar fromDate = Utils.getFromDate(context);
                    if (fromDate.after(toDate)) {
                        throw new IllegalArgumentException("The illegal TO_DATE parameter value: '"
                                                           + Utils.formatDate(toDate, PARAM_DATE_FORMAT)
                                                           + "'. Should be higher than fromDate parameter value: '"
                                                           + Utils.formatDate(fromDate, PARAM_DATE_FORMAT)
                                                           + "'");
                    }
                }

            } catch (IOException e) {
                throw new IllegalArgumentException("TO_DATE parameter has illegal format '" + value
                                                   + "'. The only supported format is 'yyyyMMdd'");
            }
        }
    };

    /** @return the default value for given parameter. */
    public abstract String getDefaultValue();

    /** Validates the value of parameter. Throws {@link IllegalArgumentException} if something wrong. */
    public abstract void validate(String value, Map<String, String> context) throws IllegalStateException;

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
