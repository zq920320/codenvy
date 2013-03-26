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

import java.util.Map;

/**
 * Enumeration of all Pig-latin script's results. 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public enum ScriptTypeResult {

   PROPERTIES {
      @Override
      public ValueTranslator getValueTranslator()
      {
         return new Bag2PropertiesTranslator();
      }

      @Override
      public Object getEmptyObject()
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
      public Object getEmptyObject()
      {
         return Long.valueOf(0);
      }
   },

   NO_KEY_FIELDS_LIST {
      @Override
      public ValueTranslator getValueTranslator()
      {
         return new Bag2ListTranslator();
      }

      @Override
      public Object getEmptyObject()
      {
         return new DefaultDataBag();
      }

      @Override
      public String[] getKeyFields()
      {
         return new String[]{};
      }

      @Override
      public Tuple getEmptyResult(Map<String, String> executionContext) throws ExecException
      {
         Tuple tuple = TupleFactory.getInstance().newTuple(1);
         tuple.set(1, getEmptyObject());

         return tuple;
      }
   };
   
   /**
    * @return the list of actual key field names.
    */
   public String[] getKeyFields()
   {
      return new String[]{Constants.FROM_DATE, Constants.TO_DATE};
   }

   /**
    * @return corresponding {@link ValueTranslator} instance
    */
   public abstract ValueTranslator getValueTranslator();

   /**
    * @return particular object for default value
    */
   public abstract Object getEmptyObject();

   /**
    * @return the default value
    */
   public Tuple getEmptyResult(Map<String, String> executionContext) throws ExecException
   {
      Tuple tuple = TupleFactory.getInstance().newTuple(3);
      tuple.set(0, executionContext.get(Constants.FROM_DATE));
      tuple.set(1, executionContext.get(Constants.TO_DATE));
      tuple.set(2, getEmptyObject());

      return tuple;
   }
}