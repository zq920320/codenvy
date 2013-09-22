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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public enum ScriptType {
    USER_CREATED_FROM_FACTORY {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return LongValueData.class;
        }
    },
    USER_CREATED_FROM_FACTORY_BY_USERS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringLongValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }
    },
    SET_USER_CREATED_FROM_FACTORY_BY_FACTORY_URL {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringSetValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }
    },
    USER_CREATED_FROM_FACTORY_BY_DOMAINS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringLongValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }
    },
    USER_CREATED_FROM_FACTORY_BY_WS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringLongValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }
    },
    USERS_COMPLETED_PROFILE {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return LongValueData.class;
        }

        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = new HashSet<>();
            params.add(MetricParameter.LOAD_DIR);
            return params;
        }
    },
    NUMBER_ACTIVE_USERS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return LongValueData.class;
        }

        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.LOAD_DIR);
            params.add(MetricParameter.STORE_DIR);
            params.add(MetricParameter.EVENT);
            return params;
        }
    },
    NUMBER_PROJECT_WITH_JREBEL {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return LongValueData.class;
        }
    },
    NUMBER_PROJECT_RUNNED_WITH_JREBEL {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return LongValueData.class;
        }
    },
    NUMBER_PROJECT_DEPLOYED_WITH_JREBEL {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return LongValueData.class;
        }
    },
    FACTORY_SESSIONS_AND_EVENT {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return LongValueData.class;
        }

        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.EVENT);
            return params;
        }
    },
    FACTORY_SESSIONS_AND_EVENT_BY_WS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringLongValueData.class;
        }

        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.EVENT);
            return params;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }
    },
    JREBEL_USER_PROFILE_INFO {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return ListListStringValueData.class;
        }
    },
    PROJECT_DEPLOYED {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringLongValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.PARAM};
        }
    },
    PROJECT_DEPLOYED_BY_USERS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapListLongValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.PARAM, MetricParameter.FILTER};
        }
    },
    PROJECT_DEPLOYED_BY_DOMAINS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapListLongValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.PARAM, MetricParameter.FILTER};
        }
    },
    PRODUCT_USAGE_TIME_USERS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringFixedLongListValueData.class;
        }
    },
    PRODUCT_USAGE_TIME_DOMAINS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringFixedLongListValueData.class;
        }
    },
    PRODUCT_USAGE_TIME_COMPANIES {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringFixedLongListValueData.class;
        }

        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.LOAD_DIR);
            return params;
        }
    },
    PRODUCT_USAGE_SESSIONS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return ListListStringValueData.class;
        }
    },
    PRODUCT_USAGE_SESSIONS_FACTORY {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return ListListStringValueData.class;
        }
    },
    PRODUCT_USAGE_SESSIONS_FACTORY_BY_WS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringListListStringValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }
    },
    PRODUCT_USAGE_TIME_FACTORY {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return LongValueData.class;
        }
    },
    PRODUCT_USAGE_TIME_FACTORY_BY_WS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringLongValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }
    },
    PRODUCT_USAGE_SESSIONS_BY_USERS {
        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringListListStringValueData.class;
        }
    },
    PRODUCT_USAGE_SESSIONS_BY_DOMAINS {
        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringListListStringValueData.class;
        }
    },
    SET_ACTIVE {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return SetStringValueData.class;
        }

        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.FIELD);
            params.add(MetricParameter.EVENT);
            return params;
        }
    },
    SET_ACTIVE_BY_USERS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringSetValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }

        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.FIELD);
            params.add(MetricParameter.EVENT);
            return params;
        }
    },
    SET_ACTIVE_BY_DOMAINS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringSetValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }

        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.FIELD);
            params.add(MetricParameter.EVENT);
            return params;
        }
    },
    SET_ACTIVE_BY_WS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringSetValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }

        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.FIELD);
            params.add(MetricParameter.EVENT);
            return params;
        }
    },
    FACTORY_SESSIONS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return ListListStringValueData.class;
        }

        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.LOAD_DIR);
            return params;
        }
    },
    FACTORY_SESSIONS_BY_WS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringListListStringValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }

        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.LOAD_DIR);
            return params;
        }
    },
    FACTORY_URL_ACCEPTED_BY_FACTORY_URL {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringSetValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }
    },
    FACTORY_URL_ACCEPTED_BY_REFERRER_URL {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringSetValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }
    },
    NUMBER_EVENTS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return LongValueData.class;
        }

        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.EVENT);
            return params;
        }
    },
    FACTORY_SESSIONS_TYPE {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringLongValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.PARAM};
        }
    },
    FACTORY_SESSIONS_TYPE_BY_WS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapListLongValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.PARAM, MetricParameter.FILTER};
        }
    },
    NUMBER_EVENTS_WITH_TYPE {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringLongValueData.class;
        }

        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.EVENT);
            params.add(MetricParameter.PARAM);
            return params;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
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
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.EVENT);
            return params;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }
    },
    NUMBER_EVENTS_BY_WS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringLongValueData.class;
        }

        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.EVENT);
            return params;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }
    },
    NUMBER_EVENTS_WITH_TYPE_BY_USERS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapListLongValueData.class;
        }

        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.EVENT);
            params.add(MetricParameter.PARAM);
            return params;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.PARAM, MetricParameter.FILTER};
        }
    },
    NUMBER_EVENTS_WITH_TYPE_BY_WS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapListLongValueData.class;
        }

        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.EVENT);
            params.add(MetricParameter.PARAM);
            return params;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.PARAM, MetricParameter.FILTER};
        }
    },
    NUMBER_EVENTS_WITH_TYPE_BY_DOMAINS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapListLongValueData.class;
        }

        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.EVENT);
            params.add(MetricParameter.PARAM);
            return params;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.PARAM, MetricParameter.FILTER};
        }
    },
    NUMBER_EVENTS_BY_DOMAINS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringLongValueData.class;
        }

        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.EVENT);
            return params;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }
    },
    ACTIVITY_BY_USERS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringListValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }
    },
    UPDATE_PROFILE_BY_USERS {
        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.LOAD_DIR);
            params.add(MetricParameter.STORE_DIR);
            return params;
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringListValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }
    },
    USERS_BY_COMPANY {
        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(Arrays.asList(new MetricParameter[]{
                    MetricParameter.LOAD_DIR,
                    MetricParameter.PARAM}));
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return ListStringValueData.class;
        }

        @Override
        public boolean isLogRequired() {
            return false;
        }
    },
    ERROR_TYPES {
        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = super.getParams();
            params.remove(MetricParameter.USER);
            params.remove(MetricParameter.WS);
            return params;
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringLongValueData.class;
        }
    },
    TIME_BETWEEN_EVENTS {
        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.EVENT);
            return params;
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return LongValueData.class;
        }
    },
    TIME_BETWEEN_EVENTS_BY_USERS {
        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.EVENT);
            return params;
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringLongValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }
    },
    TIME_BETWEEN_EVENTS_BY_DOMAINS {
        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.EVENT);
            return params;
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringLongValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }
    },
    /**
     * Is responsible to find invalid messages like with empty or null value parameters, without user or workspace
     * data.
     */
    CHECK_LOGS_1 {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return ListListStringValueData.class;
        }
    },
    /** Returns the list of unique events that were generated. */
    CHECK_LOGS_2 {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return ListListStringValueData.class;
        }
    },
    FACTORY_URL_ACCEPTED {
        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.LOAD_DIR);
            params.add(MetricParameter.STORE_DIR);
            return params;
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return SetStringValueData.class;
        }
    },
    SET_ACTIVE_FACTORY {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return SetStringValueData.class;
        }
    },
    SET_ACTIVE_FACTORY_BY_WS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringSetValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }
    },
    SET_FACTORY_CREATED {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return SetStringValueData.class;
        }
    },
    SET_FACTORY_CREATED_BY_WS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringSetValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }
    },
    SET_FACTORY_CREATED_BY_PROJECT_TYPE {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringSetValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }
    },
    SET_FACTORY_CREATED_BY_AFFILIATE_ID {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringSetValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }
    },
    SET_FACTORY_CREATED_BY_ORG_ID {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringSetValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }
    },
    SET_FACTORY_CREATED_BY_REPOSITORY_URL {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringSetValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }
    },
    SET_FACTORY_CREATED_BY_USERS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringSetValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }
    },
    ACTON {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringFixedLongListValueData.class;
        }
    },
    USERS_PROFILES {
        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = new HashSet<>();
            params.add(MetricParameter.LOAD_DIR);
            return params;
        }

        @Override
        public boolean isLogRequired() {
            return false;
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringListValueData.class;
        }
    },
    REFERRERS {
        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = new HashSet<>();
            params.add(MetricParameter.LOAD_DIR);
            return params;
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringFixedLongListValueData.class;
        }
    },
    REFERRERS_BY_WS {
        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = new HashSet<>();
            params.add(MetricParameter.LOAD_DIR);
            return params;
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringMapSFLLValueData.class;
        }

        @Override
        public MetricParameter[] getKeyScheme() {
            return new MetricParameter[]{MetricParameter.FILTER};
        }
    },
    /** Script for testing purpose. */
    TEST_EXTRACT_USER {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return ListStringValueData.class;
        }
    },
    /** Script for testing purpose. */
    TEST_EXTRACT_WS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return ListStringValueData.class;
        }
    },
    /** Script for testing purpose. */
    TEST_COMBINE_SMALL_SESSIONS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return ListListStringValueData.class;
        }
    },
    /** Script for testing purpose. */
    TEST_TIME_BETWEEN_PAIRS_OF_EVENTS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return ListListStringValueData.class;
        }
    };


    /** @return list of mandatory parameters required to be passed to the script */
    public Set<MetricParameter> getParams() {
        return new LinkedHashSet<>(
                Arrays.asList(new MetricParameter[]{MetricParameter.FROM_DATE,
                                                    MetricParameter.TO_DATE,
                                                    MetricParameter.USER,
                                                    MetricParameter.WS}));
    }

    /**
     * If script returns {@link MapValueData} result type, then it is necessary to provide also the scheme of keys.
     * In a word, it provides additional information for building unique keys sequences for result.
     */
    public MetricParameter[] getKeyScheme() {
        return new MetricParameter[0];
    }

    /** @return true if script requires {@link MetricParameter#LOG} being  executed. */
    public boolean isLogRequired() {
        return true;
    }

    /** @return what date type is represented in result */
    public abstract Class<? extends ValueData> getValueDataClass();
}
