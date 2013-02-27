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

import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.DefaultDataBag;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Enumeration of all Pig-latin script's results. 
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public enum ScriptResultType {

   /**
    * Represents the result of Pig-latin script execution, which
    * returns the amount of specific event occurrence for particular
    * date. Incoming tuple should meet the requirement:<br> 
    * (int: date, bag: {(chararray: event, long: value)}).
    */
   EVENT_ALL {
      @Override
      public String[] getKeyFields()
      {
         return new String[]{Constants.DATE};
      }

      @Override
      public ValueTranslator getValueTranslator()
      {
         return new Bag2PropertiesTranslator();
      }

      @Override
      public Tuple getDefaultValue(Map<String, String> executionContext) throws ExecException
      {
         Tuple tuple = TupleFactory.getInstance().newTuple(2);
         tuple.set(0, executionContext.get(Constants.DATE));
         tuple.set(1, new DefaultDataBag());

         return tuple;
      }
   },

   /**
    * Represents the result of Pig-latin script execution, which
    * returns amount of every value of additional parameter
    * relatively to particular event and date.<br>
    *  
    * Incoming tuple should meet the requirement:<br> 
    * (chararray: event, chararray: paramName, int: date, bag: {(chararray: paramValue, long: value)}).
    */
   EVENT_PARAM_ALL {
      @Override
      public String[] getKeyFields()
      {
         return new String[]{Constants.EVENT, Constants.PARAM_NAME, Constants.DATE};
      }

      @Override
      public ValueTranslator getValueTranslator()
      {
         return new Bag2PropertiesTranslator();
      }

      @Override
      public Tuple getDefaultValue(Map<String, String> executionContext) throws ExecException
      {
         Tuple tuple = TupleFactory.getInstance().newTuple(4);
         tuple.set(0, executionContext.get(Constants.EVENT));
         tuple.set(1, executionContext.get(Constants.PARAM_NAME));
         tuple.set(2, executionContext.get(Constants.DATE));
         tuple.set(3, new DefaultDataBag());

         return tuple;
      }
   };

   /**
    * Factory class. Creates specific {@link FileObject} with given corresponding {@link ScriptResultType}.
    * The value will be obtained from {@link Tuple}.
    */
   public FileObject createFileObject(String baseDir, Tuple tuple) throws IOException
   {
      return new FileObject(baseDir, this, tuple);
   }

   /**
    * Factory class. Creates specific {@link FileObject} with given corresponding {@link ScriptResultType}.
    * The value will be loaded from the file.
    */
   public FileObject createFileObject(String baseDir, Object... keysValues) throws IOException
   {
      return new FileObject(baseDir, this, keysValues);
   }

   /**
    * @return the list of actual key field names.
    */
   public abstract String[] getKeyFields();

   /**
    * @return corresponding {@link ValueTranslator} instance
    */
   public abstract ValueTranslator getValueTranslator();

   /**
    * @return the default value
    */
   public abstract Tuple getDefaultValue(Map<String, String> executionContext) throws ExecException;
}