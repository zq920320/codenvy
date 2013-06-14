/*
 *    Copyright (C) 2013 eXo Platform SAS.
 *
 *    This is free software; you can redistribute it and/or modify it
 *    under the terms of the GNU Lesser General Public License as
 *    published by the Free Software Foundation; either version 2.1 of
 *    the License, or (at your option) any later version.
 *
 *    This software is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this software; if not, write to the Free
 *    Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *    02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public enum MetricParameter {
    ALIAS {
        @Override
        public String getDefaultValue() {
            return null;
        }

        @Override
        public void validate(String value, Map<String, String> context) throws IllegalStateException {
            if (value == null) {
                throw new IllegalArgumentException("ALIAS parameter is null");
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
        USERS,
        COMPANIES,
        DOMAINS
    }
}
