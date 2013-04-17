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
package com.codenvy.analytics.scripts;

import com.codenvy.analytics.metrics.TimeUnit;

import java.util.Date;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public enum ScriptParameters {
    TIME_UNIT {
        @Override
        public String getDefaultValue() {
            return TimeUnit.DAY.toString();
        }

        @Override
        public String getName() {
            return "timeUnit";
        }

        @Override
        public String getDescription() {
            return "";
        }

        @Override
        public String getTitle() {
            return "Time Unit";
        }
    },

    FROM_DATE {
        @Override
        public String getDefaultValue() {
            return "20120101";
        }

        @Override
        public String getName() {
            return "fromDate";
        }

        @Override
        public String getDescription() {
            return "Observation period starting date";
        }

        @Override
        public String getTitle() {
            return "From Date";
        }
    },

    TO_DATE {
        @Override
        public String getDefaultValue() {
            return ScriptExecutor.PARAM_DATE_FORMAT.format(new Date());
        }

        @Override
        public String getName() {
            return "toDate";
        }

        @Override
        public String getDescription() {
            return "Observation period finishing date";
        }

        @Override
        public String getTitle() {
            return "To Date";
        }
    },

    LAST_MINUTES {
        @Override
        public String getDefaultValue() {
            return "60";
        }

        @Override
        public String getName() {
            return "lastMinutes";
        }

        @Override
        public String getDescription() {
            return "Observation period in munites till current moment";
        }

        @Override
        public String getTitle() {
            return "Last Minutes";
        }
    },

    SESSIONS_COUNT {
        @Override
        public String getDefaultValue() {
            return "2";
        }

        @Override
        public String getName() {
            return "sessionsCount";
        }

        @Override
        public String getDescription() {
            return "Number of sessions";
        }

        @Override
        public String getTitle() {
            return "Sessions number";
        }
    },

    TOP {
        @Override
        public String getDefaultValue() {
            return "10";
        }

        @Override
        public String getName() {
            return "top";
        }

        @Override
        public String getDescription() {
            return "Number of values in the top of the list to be observed";
        }

        @Override
        public String getTitle() {
            return "Top";
        }
    },

    INACTIVE_INTERVAL {
        @Override
        public String getDefaultValue() {
            return "10";
        }

        @Override
        public String getName() {
            return "inactiveInterval";
        }

        @Override
        public String getDescription() {
            return "If the user does not do any operation during specified number of minutes he is considered to be inactive";
        }

        @Override
        public String getTitle() {
            return "Inactive Interval";
        }
    };

    public abstract String getDefaultValue();

    public abstract String getName();

    public abstract String getTitle();

    public abstract String getDescription();
}
