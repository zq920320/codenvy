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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Enumeration of all available Pig-latin scripts.
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public enum ScriptType {

    EVENT_COUNT_WORKSPACE_CREATED {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2LongTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.FROM_DATE,
                                                               ScriptParameters.TO_DATE}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{}));
        }
    },

    EVENT_COUNT_USER_CREATED {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2LongTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.FROM_DATE,
                                                               ScriptParameters.TO_DATE}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{}));
        }

    },

    EVENT_COUNT_USER_REMOVED {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2LongTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.FROM_DATE,
                                                               ScriptParameters.TO_DATE}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{}));
        }

    },

    EVENT_COUNT_PROJECT_CREATED {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2LongTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.FROM_DATE,
                                                               ScriptParameters.TO_DATE}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{}));
        }

    },

    EVENT_COUNT_DIST_PROJECT_BUILD {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2LongTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.FROM_DATE,
                                                               ScriptParameters.TO_DATE}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{}));
        }

    },

    EVENT_COUNT_WORKSPACE_DESTROYED {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2LongTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.FROM_DATE,
                                                               ScriptParameters.TO_DATE}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{}));
        }

    },

    EVENT_COUNT_PROJECT_DESTROYED {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2LongTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.FROM_DATE,
                                                               ScriptParameters.TO_DATE}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{}));
        }

    },

    EVENT_COUNT_USER_INVITE {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2LongTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.FROM_DATE,
                                                               ScriptParameters.TO_DATE}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{}));
        }

    },

    EVENT_COUNT_JREBEL_USAGE {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2LongTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.FROM_DATE,
                                                               ScriptParameters.TO_DATE}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{}));
        }

    },

    ACTIVE_WORKSPACES {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2LongTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.FROM_DATE,
                                                               ScriptParameters.TO_DATE}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{}));
        }

    },

    ACTIVE_PROJECTS {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2LongTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.FROM_DATE,
                                                               ScriptParameters.TO_DATE}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{}));
        }

    },

    ACTIVE_USERS {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2LongTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.FROM_DATE,
                                                               ScriptParameters.TO_DATE}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{}));
        }

    },

    DETAILS_USER_ADDED_TO_WS {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2MapTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.FROM_DATE,
                                                               ScriptParameters.TO_DATE}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{}));
        }

    },

    DETAILS_PROJECT_CREATED_TYPES {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2MapTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.FROM_DATE,
                                                               ScriptParameters.TO_DATE}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{}));
        }

    },

    DETAILS_APPLICATION_CREATED_PAAS {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2MapTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.FROM_DATE,
                                                               ScriptParameters.TO_DATE}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{}));
        }

    },

    DETAILS_USER_SSO_LOGGED_IN {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2MapTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.FROM_DATE,
                                                               ScriptParameters.TO_DATE}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{}));
        }

    },

    DETAILS_JREBEL_USAGE {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2MapTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.FROM_DATE,
                                                               ScriptParameters.TO_DATE}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{}));
        }

    },

    USERS_WITHOUT_PROJECTS {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2ListTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.FROM_DATE,
                                                               ScriptParameters.TO_DATE}));
        }
    },

    USERS_WITHOUT_BUILDS {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2ListTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.FROM_DATE,
                                                               ScriptParameters.TO_DATE}));
        }
    },

    USERS_WITHOUT_DEPLOYS {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2ListTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.FROM_DATE,
                                                               ScriptParameters.TO_DATE}));
        }
    },

    USERS_WITHOUT_INVITES {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2ListTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.FROM_DATE,
                                                               ScriptParameters.TO_DATE}));
        }
    },

    PRODUCT_USAGE_TIME {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2LongTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.FROM_DATE,
                                                               ScriptParameters.TO_DATE}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{ScriptParameters.INACTIVE_INTERVAL}));
        }
    },

    TOP_WS_BY_USERS {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2MapTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.FROM_DATE,
                                                               ScriptParameters.TO_DATE}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{ScriptParameters.TOP}));
        }
    },

    TOP_WS_BY_INVITATIONS {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2MapTransformer();
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{ScriptParameters.TOP}));
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.FROM_DATE,
                                                               ScriptParameters.TO_DATE}));
        }
    },

    TOP_WS_BY_PROJECTS {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2MapTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.FROM_DATE,
                                                               ScriptParameters.TO_DATE}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{ScriptParameters.TOP}));
        }
    },

    TOP_WS_BY_BUILDS {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2MapTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.FROM_DATE,
                                                               ScriptParameters.TO_DATE}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{ScriptParameters.TOP}));
        }
    },

    REALTIME_WS_WITH_SEVERAL_USERS {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2MapTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(
                                                       Arrays.asList(new ScriptParameters[]{ScriptParameters.LAST_MINUTES,
                                                               ScriptParameters.SESSIONS_COUNT}));
        }
    },

    REALTIME_USER_SSO_LOGGED_IN {
        @Override
        public TupleTransformer getTupleTransformer() {
            return new Tuple2ListTransformer();
        }

        @Override
        public Set<ScriptParameters> getMandatoryParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{}));
        }

        @Override
        public Set<ScriptParameters> getAdditionalParams() {
            return new LinkedHashSet<ScriptParameters>(Arrays.asList(new ScriptParameters[]{ScriptParameters.LAST_MINUTES}));
        }
    };

    /** @return the script file name */
    public String getScriptFileName() {
        return toString().toLowerCase() + ".pig";
    }

    /** @return corresponding {@link TupleTransformer} instance. */
    public abstract TupleTransformer getTupleTransformer();

    /** @return list of mandatory parameters that have to be passed to the script */
    public abstract Set<ScriptParameters> getMandatoryParams();

    /** @return list of additional parameters (not mandatory) that might be passed to the script */
    public abstract Set<ScriptParameters> getAdditionalParams();
}
