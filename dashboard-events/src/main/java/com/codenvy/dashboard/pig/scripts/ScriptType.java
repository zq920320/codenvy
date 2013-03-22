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
package com.codenvy.dashboard.pig.scripts;

import org.apache.pig.data.Tuple;

import java.io.IOException;

/**
 * Enumeration of all available Pig-latin scripts.
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public enum ScriptType {

   EVENT_COUNT_TENANT_CREATED {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LONG;
      }
   },

   EVENT_COUNT_USER_CREATED {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LONG;
      }
   },

   EVENT_COUNT_USER_REMOVED {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LONG;
      }
   },

   EVENT_COUNT_PROJECT_CREATED {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LONG;
      }
   },

   EVENT_COUNT_DIST_PROJECT_BUILD {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LONG;
      }
   },

   EVENT_COUNT_TENANT_DESTROYED {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LONG;
      }
   },

   EVENT_COUNT_PROJECT_DESTROYED {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LONG;
      }
   },

   ACTIVE_WORKSPACES {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LONG;
      }
   },

   ACTIVE_PROJECTS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LONG;
      }
   },

   ACTIVE_USERS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LONG;
      }
   },
   
   DETAILS_USER_ADDED_TO_WS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.PROPERTIES;
      }
   },

   DETAILS_PROJECT_CREATED_TYPES {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.PROPERTIES;
      }
   },

   DETAILS_APPLICATION_CREATED_PAAS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.PROPERTIES;
      }
   },

   DETAILS_USER_SSO_LOGGED_IN {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.PROPERTIES;
      }
   },

   USERS_WITHOUT_PROJECTS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.NO_KEY_FIELDS_LIST;
      }
   },

   USERS_WITHOUT_BUILDS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.NO_KEY_FIELDS_LIST;
      }
   },

   USERS_WITHOUT_DEPLOYS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.NO_KEY_FIELDS_LIST;
      }
   },

   USERS_WITHOUT_INVITES {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.NO_KEY_FIELDS_LIST;
      }
   },

   PRODUCT_USAGE_TIME {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LONG;
      }
   },

   TOP_WS_BY_USERS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.PROPERTIES;
      }
   },

   TOP_WS_BY_INVITATIONS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.PROPERTIES;
      }
   },

   TOP_WS_BY_PROJECTS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.PROPERTIES;
      }
   },

   TOP_WS_BY_BUILDS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.PROPERTIES;
      }
   };

   /**
    * @return the script file name 
    */
   public String getScriptFileName()
   {
      return toString().toLowerCase().replace('_', '-') + ".pig";
   }

   /**
    * Every Pig-latin script return result of specific type. The type define the data format to be stored.
    * 
    * @return corresponding {@link ScriptTypeResult}.
    */
   public abstract ScriptTypeResult getResultType();

   /**
    * Factory class. Creates specific {@link FileObject} with given corresponding {@link ScriptTypeResult}.
    * The value will be obtained from {@link Tuple}.
    */
   public FileObject createFileObject(String baseDir, Tuple tuple) throws IOException
   {
      return new FileObject(baseDir, this, tuple);
   }

   /**
    * Factory class. Creates specific {@link FileObject} with given corresponding {@link ScriptTypeResult}.
    * The value will be loaded from the file.
    */
   public FileObject createFileObject(String baseDir, Object... keysValues) throws IOException
   {
      return new FileObject(baseDir, this, keysValues);
   }
}