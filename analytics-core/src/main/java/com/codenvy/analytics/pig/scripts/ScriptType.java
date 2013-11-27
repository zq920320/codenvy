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

package com.codenvy.analytics.pig.scripts;


import com.codenvy.analytics.metrics.Parameters;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public enum ScriptType {
    NUMBER_OF_EVENTS {
        @Override
        public Set<Parameters> getParams() {
            Set<Parameters> params = super.getParams();
            params.add(Parameters.EVENT);
            return params;
        }
    },
    NUMBER_OF_EVENTS_BY_TYPES {
        @Override
        public Set<Parameters> getParams() {
            Set<Parameters> params = super.getParams();
            params.add(Parameters.EVENT);
            params.add(Parameters.PARAM);
            return params;
        }
    },
    NUMBER_OF_DEPLOYMENTS_BY_TYPES,
    NUMBER_OF_USERS_FROM_FACTORY,
    PRODUCT_USAGE_SESSIONS,
    ACTIVE_ENTITIES_LIST {
        @Override
        public Set<Parameters> getParams() {
            Set<Parameters> params = super.getParams();
            params.add(Parameters.EVENT);
            params.add(Parameters.PARAM);
            return params;
        }
    },
    TIME_SPENT_IN_ACTION {
        @Override
        public Set<Parameters> getParams() {
            Set<Parameters> params = super.getParams();
            params.add(Parameters.EVENT);
            return params;
        }
    },
    FACTORY_CREATED_LIST,
    FACTORY_ACCEPTED_LIST,
    PRODUCT_USAGE_FACTORY_SESSIONS {
        @Override
        public Set<Parameters> getParams() {
            Set<Parameters> params = super.getParams();
            params.add(Parameters.STORAGE_SRC);
            return params;
        }
    },
    NUMBER_OF_FACTORY_SESSIONS_WITH_EVENT {
        @Override
        public Set<Parameters> getParams() {
            Set<Parameters> params = super.getParams();
            params.add(Parameters.EVENT);
            return params;
        }
    },

    /** Script for testing purpose. */
    TEST_MONGO_LOADER {
        public Set<Parameters> getParams() {
            return new LinkedHashSet<>(
                    Arrays.asList(new Parameters[]{Parameters.STORAGE_URL,
                                                   Parameters.STORAGE_DST}));
        }
    },
    TEST_EXTRACT_WS,
    TEST_EXTRACT_USER,
    TEST_EXTRACT_QUERY_PARAM,
    TEST_COMBINE_SMALL_SESSIONS,
    TEST_COMBINE_CLOSEST_EVENTS,
    TEST_FIX_FACTORY_URL;


    /** @return list of mandatory parameters required to be passed to the script */
    public Set<Parameters> getParams() {
        return new LinkedHashSet<>(
                Arrays.asList(new Parameters[]{Parameters.WS,
                                               Parameters.USER,
                                               Parameters.TO_DATE,
                                               Parameters.FROM_DATE,
                                               Parameters.STORAGE_URL,
                                               Parameters.STORAGE_DST}));
    }

    /** @return true if script requires {@link com.codenvy.analytics.metrics.Parameters#LOG} being  executed. */
    public boolean isLogRequired() {
        return true;
    }
}
