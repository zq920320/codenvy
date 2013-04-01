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
package com.codenvy.dashboard.scripts;

import org.apache.pig.data.DefaultDataBag;

import java.io.IOException;
import java.util.Map;

/**
 * Enumeration of all available Pig-latin scripts.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public enum ScriptType {

    EVENT_COUNT_WORKSPACE_CREATED {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.LONG;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{ScriptParameters.FROM_DATE, ScriptParameters.TO_DATE};
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{};
        }
    },

    EVENT_COUNT_USER_CREATED {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.LONG;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{ScriptParameters.FROM_DATE, ScriptParameters.TO_DATE};
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{};
        }

    },

    EVENT_COUNT_USER_REMOVED {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.LONG;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{ScriptParameters.FROM_DATE, ScriptParameters.TO_DATE};
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{};
        }

    },

    EVENT_COUNT_PROJECT_CREATED {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.LONG;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{ScriptParameters.FROM_DATE, ScriptParameters.TO_DATE};
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{};
        }

    },

    EVENT_COUNT_DIST_PROJECT_BUILD {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.LONG;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{ScriptParameters.FROM_DATE, ScriptParameters.TO_DATE};
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{};
        }

    },

    EVENT_COUNT_WORKSPACE_DESTROYED {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.LONG;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{ScriptParameters.FROM_DATE, ScriptParameters.TO_DATE};
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{};
        }

    },

    EVENT_COUNT_PROJECT_DESTROYED {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.LONG;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{ScriptParameters.FROM_DATE, ScriptParameters.TO_DATE};
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{};
        }

    },

    EVENT_COUNT_USER_INVITE {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.LONG;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{ScriptParameters.FROM_DATE, ScriptParameters.TO_DATE};
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{};
        }

    },

    EVENT_COUNT_JREBEL_USAGE {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.LONG;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{ScriptParameters.FROM_DATE, ScriptParameters.TO_DATE};
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{};
        }

    },

    ACTIVE_WORKSPACES {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.LONG;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{ScriptParameters.FROM_DATE, ScriptParameters.TO_DATE};
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{};
        }

    },

    ACTIVE_PROJECTS {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.LONG;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{ScriptParameters.FROM_DATE, ScriptParameters.TO_DATE};
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{};
        }

    },

    ACTIVE_USERS {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.LONG;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{ScriptParameters.FROM_DATE, ScriptParameters.TO_DATE};
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{};
        }

    },

    DETAILS_USER_ADDED_TO_WS {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.PROPERTIES;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{ScriptParameters.FROM_DATE, ScriptParameters.TO_DATE};
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{};
        }

    },

    DETAILS_PROJECT_CREATED_TYPES {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.PROPERTIES;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{ScriptParameters.FROM_DATE, ScriptParameters.TO_DATE};
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{};
        }

    },

    DETAILS_APPLICATION_CREATED_PAAS {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.PROPERTIES;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{ScriptParameters.FROM_DATE, ScriptParameters.TO_DATE};
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{};
        }

    },

    DETAILS_USER_SSO_LOGGED_IN {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.PROPERTIES;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{ScriptParameters.FROM_DATE, ScriptParameters.TO_DATE};
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{};
        }

    },

    DETAILS_JREBEL_USAGE {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.PROPERTIES;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{ScriptParameters.FROM_DATE, ScriptParameters.TO_DATE};
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{};
        }

    },

    USERS_WITHOUT_PROJECTS {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.LIST;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{};
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{ScriptParameters.FROM_DATE, ScriptParameters.TO_DATE};
        }

        @Override
        public boolean isStoreSupport() {
            return false;
        }
    },

    USERS_WITHOUT_BUILDS {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.LIST;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{};
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{ScriptParameters.FROM_DATE, ScriptParameters.TO_DATE};
        }

        @Override
        public boolean isStoreSupport() {
            return false;
        }
    },

    USERS_WITHOUT_DEPLOYS {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.LIST;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{};
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{ScriptParameters.FROM_DATE, ScriptParameters.TO_DATE};
        }

        @Override
        public boolean isStoreSupport() {
            return false;
        }
    },

    USERS_WITHOUT_INVITES {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.LIST;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{};
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{ScriptParameters.FROM_DATE, ScriptParameters.TO_DATE};
        }

        @Override
        public boolean isStoreSupport() {
            return false;
        }
    },

    PRODUCT_USAGE_TIME {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.LONG;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{ScriptParameters.FROM_DATE, ScriptParameters.TO_DATE};
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{ScriptParameters.INACTIVE_INTERVAL};
        }
    },

    TOP_WS_BY_USERS {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.PROPERTIES;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{ScriptParameters.FROM_DATE, ScriptParameters.TO_DATE};
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{ScriptParameters.TOP};
        }
    },

    TOP_WS_BY_INVITATIONS {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.PROPERTIES;
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{ScriptParameters.TOP};
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{ScriptParameters.FROM_DATE, ScriptParameters.TO_DATE};
        }
    },

    TOP_WS_BY_PROJECTS {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.PROPERTIES;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{ScriptParameters.FROM_DATE, ScriptParameters.TO_DATE};
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{ScriptParameters.TOP};
        }
    },

    TOP_WS_BY_BUILDS {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.PROPERTIES;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{ScriptParameters.FROM_DATE, ScriptParameters.TO_DATE};
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{ScriptParameters.TOP};
        }
    },

    REALTIME_WS_WITH_SEVERAL_USERS {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.PROPERTIES;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{};
        }

        @Override
        public boolean isStoreSupport() {
            return false;
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{ScriptParameters.LAST_MINUTES, ScriptParameters.SESSIONS_COUNT};
        }
    },

    REALTIME_USER_SSO_LOGGED_IN {
        @Override
        public ScriptTypeResult getResultType() {
            return ScriptTypeResult.LIST;
        }

        @Override
        public ScriptParameters[] getMandatoryParams() {
            return new ScriptParameters[]{};
        }

        @Override
        public boolean isStoreSupport() {
            return false;
        }

        @Override
        public ScriptParameters[] getAdditionalParams() {
            return new ScriptParameters[]{ScriptParameters.LAST_MINUTES};
        }
    };

    /** @return the script file name */
    public String getScriptFileName() {
        return toString().toLowerCase() + ".pig";
    }

    /**
     * Every Pig-latin script return result of specific type. The type define the data format to be stored.
     *
     * @return corresponding {@link ScriptTypeResult}.
     */
    public abstract ScriptTypeResult getResultType();

    /** @return list of mandatory parameters that have to be passed to the script */
    public abstract ScriptParameters[] getMandatoryParams();

    /** @return list of additional parameters (not mandatory) that might be passed to the script */
    public abstract ScriptParameters[] getAdditionalParams();

    /** @return true if script result might be stored and false otherwise */
    public boolean isStoreSupport() {
        return true;
    }

    /** Factory class. */
    public FileObject createFileObject(String baseDir, Map<String, String> executionParams, Object value)
            throws IOException {
        return new FileObject(baseDir, this, executionParams, value);
    }

    /** Factory class. The value will be loaded from the file. */
    public FileObject createFileObject(String baseDir, Map<String, String> executionParams) throws IOException {
        return new FileObject(baseDir, this, executionParams);
    }

    /** Enumeration of all Pig-latin script's results. */
    public enum ScriptTypeResult {

        PROPERTIES {
            @Override
            public ValueTranslator getValueTranslator() {
                return new Bag2PropertiesTranslator();
            }

            @Override
            public Object getEmptyResult() {
                return new DefaultDataBag();
            }
        },

        LONG {
            @Override
            public ValueTranslator getValueTranslator() {
                return new Object2LongTranslator();
            }

            @Override
            public Object getEmptyResult() {
                return Long.valueOf(0);
            }
        },

        LIST {
            @Override
            public ValueTranslator getValueTranslator() {
                return new Bag2ListTranslator();
            }

            @Override
            public Object getEmptyResult() {
                return new DefaultDataBag();
            }
        };

        /** @return corresponding {@link ValueTranslator} instance */
        public abstract ValueTranslator getValueTranslator();

        /** @return particular object for default value */
        public abstract Object getEmptyResult();

    }
}
