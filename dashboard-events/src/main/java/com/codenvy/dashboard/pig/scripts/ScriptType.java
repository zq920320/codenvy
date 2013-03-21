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

   /**
    * Finds total number of 'tenant-created' events in given day.
    */
   EVENT_COUNT_TENANT_CREATED {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.DATE_FOR_LONG;
      }
   },

   /**
    * Finds total number of 'tenant-created' events in given day.
    */
   EVENT_COUNT_USER_CREATED {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.DATE_FOR_LONG;
      }
   },

   /**
    * Finds total number of 'user-removed' events in given day.
    */
   EVENT_COUNT_USER_REMOVED {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.DATE_FOR_LONG;
      }
   },

   /**
    * Finds total number of 'project-created' events in given day.
    */
   EVENT_COUNT_PROJECT_CREATED {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.DATE_FOR_LONG;
      }
   },

   /**
    * Finds total number of 'tenant-destroyed' events in given day.
    */
   EVENT_COUNT_TENANT_DESTROYED {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.DATE_FOR_LONG;
      }
   },

   /**
    * TODO
    */
   EVENT_COUNT_PROJECT_DESTROYED {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.DATE_FOR_LONG;
      }
   },

   /**
    * Find total number of active workspaces per time frame.
    */
   ACTIVE_WORKSPACES {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.TIMEFRAME_FOR_LONG;
      }
   },

   /**
    * Find total number of active projects per time frame.
    */
   ACTIVE_PROJECTS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.TIMEFRAME_FOR_LONG;
      }
   },

   /**
    * Find total number of active users per time frame.
    */
   ACTIVE_USERS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.TIMEFRAME_FOR_LONG;
      }
   },
   
   /**
    * TODO
    */
   DETAILS_USER_ADDED_TO_WS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.DATE_FOR_PROPERTIES;
      }
   },

   /**
    * TODO
    */
   DETAILS_PROJECT_CREATED_TYPES {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.DATE_FOR_PROPERTIES;
      }
   },

   /**
    * TODO
    */
   DETAILS_USER_SSO_LOGGED_IN {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.TIMEFRAME_FOR_PROPERTIES;
      }
   },

   /**
    * Find users who was created in given day but did not created project in
    * the follow time frame.
    */
   USERS_WITHOUT_PROJECTS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.TIMEFRAME_FOR_LIST;
      }
   },

   /**
    * Finds total working time for all users in workspace.
    * the follow time frame.
    */
   PRODUCT_USAGE_TIME {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.TIMEFRAME_INTERVAL_FOR_LONG;
      }
   },

   /**
   * Find top workspaces by amount of users.
   */
   TOP_WS_BY_USERS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.TIMEFRAME_TOP_FOR_PROPERTIES;
      }
   },

   /**
   * Find top workspaces by amount of invitations sent.
   */
   TOP_WS_BY_INVITATIONS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.TIMEFRAME_TOP_FOR_PROPERTIES;
      }
   },

   /**
   * Find top workspaces by amount of created projects.
   * If project was removed, then it will not be calculated.
   */
   TOP_WS_BY_PROJECTS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.TIMEFRAME_TOP_FOR_PROPERTIES;
      }
   },

   /**
   * Find top workspaces by amount of compile and run action.
   */
   TOP_WS_BY_BUILDS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.TIMEFRAME_TOP_FOR_PROPERTIES;
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