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
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
        }
    },
    USER_CREATED_FROM_FACTORY_BY_DOMAINS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringLongValueData.class;
        }

        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
        }
    },
    USER_CREATED_FROM_FACTORY_BY_WS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringLongValueData.class;
        }

        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
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
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
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
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.PARAM};
        }
    },
    PROJECT_DEPLOYED_BY_USERS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapListLongValueData.class;
        }

        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.PARAM, MetricParameter.ALIAS};
        }
    },
    PROJECT_DEPLOYED_BY_DOMAINS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapListLongValueData.class;
        }

        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.PARAM, MetricParameter.ALIAS};
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
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
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
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
        }
    },
    PRODUCT_USAGE_SESSIONS_BY_USERS {
        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringListListStringValueData.class;
        }
    },
    PRODUCT_USAGE_SESSIONS_BY_DOMAINS {
        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
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
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
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
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
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
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
        }

        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.FIELD);
            params.add(MetricParameter.EVENT);
            return params;
        }
    },
    SET_ACTIVE_BY_URL {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringSetValueData.class;
        }

        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.URL};
        }

        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.FIELD);
            params.add(MetricParameter.EVENT);
            return params;
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
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.PARAM};
        }
    },
    FACTORY_SESSIONS_TYPE_BY_WS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapListLongValueData.class;
        }

        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.PARAM, MetricParameter.ALIAS};
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
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.EVENT);
            return params;
        }

        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
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
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.EVENT);
            params.add(MetricParameter.PARAM);
            return params;
        }

        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.PARAM, MetricParameter.ALIAS};
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
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.EVENT);
            params.add(MetricParameter.PARAM);
            return params;
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
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.EVENT);
            return params;
        }

        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
        }
    },
    ACTIVITY_BY_USERS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringListValueData.class;
        }

        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
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
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
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
    FACTORY_URL_BY_ENTITY {
        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(Arrays.asList(new MetricParameter[]{
                    MetricParameter.LOAD_DIR,
                    MetricParameter.PARAM,
                    MetricParameter.FIELD}));
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
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
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
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
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

    FACTORY_CREATED {
        @Override
        public Set<MetricParameter> getParams() {
            Set<MetricParameter> params = super.getParams();
            params.add(MetricParameter.LOAD_DIR);
            params.add(MetricParameter.STORE_DIR);
            return params;
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return LongValueData.class;
        }
    },
    FACTORY_CREATED_BY_URL {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringLongValueData.class;
        }

        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.URL};
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

    public MetricParameter[] getResultScheme() { // TODO refactor, unclear
        return new MetricParameter[0];
    }

    /** @return true if script requires {@link MetricParameter#LOG} being  executed. */
    public boolean isLogRequired() {
        return true;
    }

    /** @return what date type is represented in result */
    public abstract Class<? extends ValueData> getValueDataClass();
}
