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

import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.DefaultDataBag;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;

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
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LONG;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.TIMEFRAME;
      }
   },

   EVENT_COUNT_USER_CREATED {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LONG;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.TIMEFRAME;
      }
   },

   EVENT_COUNT_USER_REMOVED {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LONG;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.TIMEFRAME;
      }
   },

   EVENT_COUNT_PROJECT_CREATED {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LONG;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.TIMEFRAME;
      }
   },

   EVENT_COUNT_DIST_PROJECT_BUILD {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LONG;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.TIMEFRAME;
      }
   },

   EVENT_COUNT_WORKSPACE_DESTROYED {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LONG;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.TIMEFRAME;
      }
   },

   EVENT_COUNT_PROJECT_DESTROYED {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LONG;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.TIMEFRAME;
      }
   },

   EVENT_COUNT_USER_INVITE {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LONG;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.TIMEFRAME;
      }
   },

   EVENT_COUNT_JREBEL_USAGE {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LONG;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.TIMEFRAME;
      }
   },

   ACTIVE_WORKSPACES {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LONG;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.TIMEFRAME;
      }
   },

   ACTIVE_PROJECTS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LONG;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.TIMEFRAME;
      }
   },

   ACTIVE_USERS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LONG;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.TIMEFRAME;
      }
   },

   DETAILS_USER_ADDED_TO_WS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.PROPERTIES;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.TIMEFRAME;
      }
   },

   DETAILS_PROJECT_CREATED_TYPES {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.PROPERTIES;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.TIMEFRAME;
      }
   },

   DETAILS_APPLICATION_CREATED_PAAS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.PROPERTIES;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.TIMEFRAME;
      }
   },

   DETAILS_USER_SSO_LOGGED_IN {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.PROPERTIES;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.TIMEFRAME;
      }
   },

   DETAILS_JREBEL_USAGE {
      @Override
      public ScriptTypeResult getResultType()
      {
            return ScriptTypeResult.PROPERTIES;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.TIMEFRAME;
      }
   },

   USERS_WITHOUT_PROJECTS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LIST;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.EMPTY;
      }
   },

   USERS_WITHOUT_BUILDS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LIST;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.EMPTY;
      }
   },

   USERS_WITHOUT_DEPLOYS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LIST;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.EMPTY;
      }
   },

   USERS_WITHOUT_INVITES {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LIST;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.EMPTY;
      }
   },

   PRODUCT_USAGE_TIME {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LONG;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.TIMEFRAME;
      }
   },

   TOP_WS_BY_USERS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.PROPERTIES;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.TIMEFRAME;
      }
   },

   TOP_WS_BY_INVITATIONS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.PROPERTIES;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.TIMEFRAME;
      }
   },

   TOP_WS_BY_PROJECTS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.PROPERTIES;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.TIMEFRAME;
      }
   },

   TOP_WS_BY_BUILDS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.PROPERTIES;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.TIMEFRAME;
      }
   },

   REALTIME_WS_WITH_SEVERAL_USERS {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.PROPERTIES;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.EMPTY;
      }

      @Override
      public boolean isStoreSupport()
      {
         return false;
      }
   },

   REALTIME_USER_SSO_LOGGED_IN {
      @Override
      public ScriptTypeResult getResultType()
      {
         return ScriptTypeResult.LIST;
      }

      @Override
      public ScriptKeyFieldsType getKeyFieldsType()
      {
         return ScriptKeyFieldsType.EMPTY;
      }

      @Override
      public boolean isStoreSupport()
      {
         return false;
      }
   };

   /**
    * @return the script file name
    */
   public String getScriptFileName()
   {
      return toString().toLowerCase() + ".pig";
   }

   /**
    * Every Pig-latin script return result of specific type. The type define the data format to be stored.
    *
    * @return corresponding {@link ScriptTypeResult}.
    */
   public abstract ScriptTypeResult getResultType();

   /**
    * Every Pig-latin script results contains sequence of keys which uses in creation of unique identifier
    * of result. 
    * 
    * @return corresponding {@link ScriptKeyFieldsType}.
    */
   public abstract ScriptKeyFieldsType getKeyFieldsType();

   /**
    * @return {@link ScriptKeyFieldsType#getKeyFields()}.
    */
   public String[] getKeyFields()
   {
      return getKeyFieldsType().getKeyFields();
   }

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
   
   /**
    * Factory class. Creates specific {@link FileObject} with given corresponding {@link ScriptTypeResult}.
    * The value will be loaded from the file.
    */
   public FileObject createFileObject(String baseDir, Map<String, String> executionParams) throws IOException
   {
      return new FileObject(baseDir, this, executionParams);
   }

   /**
    * @return true if script result might be stored and false otherwise  
    */
   public boolean isStoreSupport()
   {
      return true;
   }

   /**
    * Returns tuple with no results. 
    */
   public Tuple getEmptyResult(Map<String, String> executionParams) throws ExecException
   {
      String[] keyFields = getKeyFields();
      
      Tuple tuple = TupleFactory.getInstance().newTuple(keyFields.length+1);
      for (int i = 0; i < keyFields.length; i++)
      {
         tuple.set(0, executionParams.get(keyFields[i]));
      }
      tuple.set(keyFields.length, getResultType().getEmptyResult());

      return tuple;
   }
   
   /**
    * Enumeration of all Pig-latin script's results. 
    */
   public enum ScriptTypeResult {

      PROPERTIES {
         @Override
         public ValueTranslator getValueTranslator()
         {
            return new Bag2PropertiesTranslator();
         }

         @Override
         public Object getEmptyResult()
         {
            return new DefaultDataBag();
         }
      },

      LONG {
         @Override
         public ValueTranslator getValueTranslator()
         {
            return new Object2LongTranslator();
         }

         @Override
         public Object getEmptyResult()
         {
            return Long.valueOf(0);
         }
      },

      LIST {
         @Override
         public ValueTranslator getValueTranslator()
         {
            return new Bag2ListTranslator();
         }

         @Override
         public Object getEmptyResult()
         {
            return new DefaultDataBag();
         }
      };
      
      /**
       * @return corresponding {@link ValueTranslator} instance
       */
      public abstract ValueTranslator getValueTranslator();

      /**
       * @return particular object for default value
       */
      public abstract Object getEmptyResult();

   }

   /**
    * Enumeration of all key fields types. 
    */
   public enum ScriptKeyFieldsType {

      TIMEFRAME {
         @Override
         public String[] getKeyFields()
         {
            return new String[]{Constants.FROM_DATE, Constants.TO_DATE};
         }
      },
      
      EMPTY {
         @Override
         public String[] getKeyFields()
         {
            return new String[]{};
         }
      };

      /**
       * @return the list of actual key field names.
       */
      public abstract String[] getKeyFields();
   }
}