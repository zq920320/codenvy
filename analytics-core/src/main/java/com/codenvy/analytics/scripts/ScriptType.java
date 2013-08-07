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

package com.codenvy.analytics.scripts;


import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.value.*;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public enum ScriptType {
    JREBEL_USER_PROFILE_GATHERING,
    USERS_REMOVED,
    USERS_INVITATIONS,
    USERS_SHELL_LAUNCHED,
    JREBEL_USAGE,
    ACTIVE_USERS_WORKSPACES,
    USERS_ADDED_TO_WS,
    PROJECTS_CREATED,
    PROJECTS_DEPLOYED,
    PROJECTS_DEPLOYED_LOCAL,
    PROJECTS_DEPLOYED_PAAS,
    PROJECTS_BUILT,
    USERS_UPDATE_PROFILE,

    PRODUCT_USAGE_TIME,

    PRODUCT_USAGE_TIME_BY_USERS {
        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
        }
    },

    PRODUCT_USAGE_TIME_BY_DOMAINS {
        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
        }
    },

    SET_ACTIVE_USERS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return SetStringValueData.class;
        }
    },

    SET_ACTIVE_USERS_BY_DOMAINS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringSetValueData.class;
        }

        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
        }
    },

    SET_ACTIVE_USERS_BY_USERS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringSetValueData.class;
        }

        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
        }

    },

    SET_ACTIVE_WS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return SetStringValueData.class;
        }
    },

    SET_ACTIVE_WS_BY_DOMAINS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringSetValueData.class;
        }

        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
        }
    },

    SET_ACTIVE_WS_BY_USERS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringSetValueData.class;
        }

        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
        }
    },

    NUMBER_EVENTS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return LongValueData.class;
        }

        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(
                    Arrays.asList(new MetricParameter[]{MetricParameter.FROM_DATE,
                                                        MetricParameter.TO_DATE,
                                                        MetricParameter.EVENT}));
        }
    },

    NUMBER_EVENTS_WITH_TYPE {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringLongValueData.class;
        }

        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(
                    Arrays.asList(new MetricParameter[]{MetricParameter.FROM_DATE,
                                                        MetricParameter.TO_DATE,
                                                        MetricParameter.EVENT,
                                                        MetricParameter.PARAM}));
        }

        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.PARAM};
        }
    },

    NUMBER_EVENTS_BY_USERS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringLongValueData.class;
        }

        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(
                    Arrays.asList(new MetricParameter[]{MetricParameter.FROM_DATE,
                                                        MetricParameter.TO_DATE,
                                                        MetricParameter.EVENT}));
        }

        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
        }
    },

    NUMBER_EVENTS_WITH_TYPE_BY_USERS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapListLongValueData.class;
        }

        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(
                    Arrays.asList(new MetricParameter[]{MetricParameter.FROM_DATE,
                                                        MetricParameter.TO_DATE,
                                                        MetricParameter.EVENT,
                                                        MetricParameter.PARAM}));
        }

        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.PARAM, MetricParameter.ALIAS};
        }
    },

    NUMBER_EVENTS_WITH_TYPE_BY_DOMAINS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapListLongValueData.class;
        }

        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(
                    Arrays.asList(new MetricParameter[]{MetricParameter.FROM_DATE,
                                                        MetricParameter.TO_DATE,
                                                        MetricParameter.EVENT,
                                                        MetricParameter.PARAM}));
        }

        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.PARAM, MetricParameter.ALIAS};
        }
    },

    NUMBER_EVENTS_BY_DOMAINS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringLongValueData.class;
        }

        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(
                    Arrays.asList(new MetricParameter[]{MetricParameter.FROM_DATE,
                                                        MetricParameter.TO_DATE,
                                                        MetricParameter.EVENT}));
        }

        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
        }
    },

    ACTON {
        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(Arrays.asList(new MetricParameter[]{
                    MetricParameter.RESULT_DIR,
                    MetricParameter.TO_DATE}));
        }
    },

    USERS_SEGMENT_ANALYSIS_1 {
        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(Arrays.asList(new MetricParameter[]{
                    MetricParameter.RESULT_DIR,
                    MetricParameter.TO_DATE}));
        }
    },

    USERS_SEGMENT_ANALYSIS_2 {
        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(Arrays.asList(new MetricParameter[]{
                    MetricParameter.RESULT_DIR,
                    MetricParameter.TO_DATE}));
        }
    },

    USERS_SEGMENT_ANALYSIS_3 {
        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(Arrays.asList(new MetricParameter[]{
                    MetricParameter.RESULT_DIR,
                    MetricParameter.TO_DATE}));
        }
    },

    USERS_PROFILE_PREPARATION {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringListListStringValueData.class;
        }
    },

    USERS_SESSIONS_PREPARATION {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringListListStringValueData.class;
        }
    },

    USERS_ACTIVITY_PREPARATION {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringListListStringValueData.class;
        }
    },

    USERS_PROFILE_LOG_PREPARATION {
        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(Arrays.asList(new MetricParameter[]{
                    MetricParameter.RESULT_DIR,
                    MetricParameter.FROM_DATE,
                    MetricParameter.TO_DATE}));
        }
    },

    PRODUCT_USAGE_TIME_LOG_PREPARATION {
        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(Arrays.asList(new MetricParameter[]{
                    MetricParameter.RESULT_DIR}));
        }
    },

    PRODUCT_USAGE_TIME_TOP {
        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(Arrays.asList(new MetricParameter[]{
                    MetricParameter.RESULT_DIR,
                    MetricParameter.INTERVAL,
                    MetricParameter.TO_DATE}));
        }
    },

    PRODUCT_USAGE_TIME_COMPANIES {
        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(Arrays.asList(new MetricParameter[]{
                    MetricParameter.RESULT_DIR,
                    MetricParameter.INTERVAL,
                    MetricParameter.TO_DATE}));
        }
    },

    PRODUCT_USAGE_TIME_USERS {
        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(Arrays.asList(new MetricParameter[]{
                    MetricParameter.RESULT_DIR,
                    MetricParameter.INTERVAL,
                    MetricParameter.TO_DATE}));
        }
    },

    PRODUCT_USAGE_TIME_DOMAINS {
        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(Arrays.asList(new MetricParameter[]{
                    MetricParameter.RESULT_DIR,
                    MetricParameter.INTERVAL,
                    MetricParameter.TO_DATE}));
        }
    },

    USERS_BY_COMPANY {
        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(Arrays.asList(new MetricParameter[]{
                    MetricParameter.RESULT_DIR,
                    MetricParameter.COMPANY_NAME,
                    MetricParameter.TO_DATE}));
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return ListStringValueData.class;
        }
    },

    /**
     * Is responsible to find invalid messages like with empty or null value parameters, without user or workspace
     * data.
     */
    CHECK_LOGS_1,

    /** Returns the list of unique events that were generated. */
    CHECK_LOGS_2,

    /** Script for testing purpose. */
    TEST_EXTRACT_USER {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return ListStringValueData.class;
        }
    };

    /** @return what date type is represented in result */
    public Class<? extends ValueData> getValueDataClass() {
        return ListListStringValueData.class;
    }

    /** @return list of mandatory parameters required to be passed to the script */
    public Set<MetricParameter> getParams() {
        return new LinkedHashSet<>(
                Arrays.asList(new MetricParameter[]{MetricParameter.FROM_DATE, MetricParameter.TO_DATE}));
    }

    /** @return  */
    public MetricParameter[] getResultScheme() {
        // TODO
        return new MetricParameter[0];
    }
}
