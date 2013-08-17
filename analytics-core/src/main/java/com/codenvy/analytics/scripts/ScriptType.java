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
    NUMBER_ACTIVE_USERS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return LongValueData.class;
        }

        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(
                    Arrays.asList(new MetricParameter[]{MetricParameter.FROM_DATE,
                                                        MetricParameter.TO_DATE,
                                                        MetricParameter.LOAD_DIR,
                                                        MetricParameter.STORE_DIR,
                                                        MetricParameter.EVENT}));
        }
    },
    TEMPORARY_WORKSPACE_CREATED {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return LongValueData.class;
        }

        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(
                    Arrays.asList(new MetricParameter[]{MetricParameter.FROM_DATE,
                                                        MetricParameter.TO_DATE}));
        }
    },
    TEMPORARY_WORKSPACE_CREATED_BY_WS {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringLongValueData.class;
        }

        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(
                    Arrays.asList(new MetricParameter[]{MetricParameter.FROM_DATE,
                                                        MetricParameter.TO_DATE}));
        }

        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
        }
    },
    FACTORY_SESSIONS_AND_EVENT {
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
    FACTORY_SESSIONS_AND_EVENT_BY_WS {
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

    JREBEL_USER_PROFILE_INFO,

    PROJECTS_CREATED,
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

    USERS_UPDATE_PROFILE,

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
            return new LinkedHashSet<>(
                    Arrays.asList(new MetricParameter[]{MetricParameter.FROM_DATE,
                                                        MetricParameter.TO_DATE,
                                                        MetricParameter.LOAD_DIR}));
        }
    },

    PRODUCT_USAGE_SESSIONS,
    PRODUCT_USAGE_SESSIONS_FACTORY,
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

    PRODUCT_USAGE_SESSIONS_BY_USERS {
        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.ALIAS};
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringListListStringValueData.class;
        }

        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(
                    Arrays.asList(new MetricParameter[]{MetricParameter.FROM_DATE,
                                                        MetricParameter.TO_DATE}));
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

        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(
                    Arrays.asList(new MetricParameter[]{MetricParameter.FROM_DATE,
                                                        MetricParameter.TO_DATE,
                                                        MetricParameter.EVENT}));
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

        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(
                    Arrays.asList(new MetricParameter[]{MetricParameter.FROM_DATE,
                                                        MetricParameter.TO_DATE,
                                                        MetricParameter.EVENT}));
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

        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(
                    Arrays.asList(new MetricParameter[]{MetricParameter.FROM_DATE,
                                                        MetricParameter.TO_DATE,
                                                        MetricParameter.EVENT}));
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

    NUMBER_EVENTS_BY_WS {
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
    NUMBER_EVENTS_WITH_TYPE_BY_WS {
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

    USERS_PROFILE_PREPARATION {
        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringListListStringValueData.class;
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

    USER_UPDATE_PROFILE {
        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(Arrays.asList(new MetricParameter[]{
                    MetricParameter.FROM_DATE,
                    MetricParameter.TO_DATE,
                    MetricParameter.LOAD_DIR,
                    MetricParameter.STORE_DIR}));
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return LongValueData.class;
        }
    },

    USERS_BY_COMPANY {
        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(Arrays.asList(new MetricParameter[]{
                    MetricParameter.LOAD_DIR,
                    MetricParameter.PARAM,
                    MetricParameter.TO_DATE}));
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return ListStringValueData.class;
        }
    },

    FACTORY_URL_BY_ENTITY {
        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(Arrays.asList(new MetricParameter[]{
                    MetricParameter.LOAD_DIR,
                    MetricParameter.PARAM,
                    MetricParameter.FIELD,
                    MetricParameter.TO_DATE}));
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

    TEMP_WS_BY_FACTORY_URL {
        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(Arrays.asList(new MetricParameter[]{
                    MetricParameter.LOAD_DIR,
                    MetricParameter.PARAM,
                    MetricParameter.TO_DATE}));
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
    },

    FACTORY_CREATED {
        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(
                    Arrays.asList(new MetricParameter[]{MetricParameter.FROM_DATE,
                                                        MetricParameter.TO_DATE,
                                                        MetricParameter.LOAD_DIR,
                                                        MetricParameter.STORE_DIR}));
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return LongValueData.class;
        }
    },
    FACTORY_CREATED_BY_URL {
        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(
                    Arrays.asList(new MetricParameter[]{MetricParameter.FROM_DATE,
                                                        MetricParameter.TO_DATE}));
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return MapStringLongValueData.class;
        }

        @Override
        public MetricParameter[] getResultScheme() {
            return new MetricParameter[]{MetricParameter.URL};
        }
    },

    FACTORY_URL_ACCEPTED {
        @Override
        public Set<MetricParameter> getParams() {
            return new LinkedHashSet<>(
                    Arrays.asList(new MetricParameter[]{MetricParameter.FROM_DATE,
                                                        MetricParameter.TO_DATE,
                                                        MetricParameter.LOAD_DIR,
                                                        MetricParameter.STORE_DIR}));
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return LongValueData.class;
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

    public boolean isLogRequired() { // TODO
        return true;
    }

    // TODO toDATE mandatory?
}
