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
package com.codenvy.dashboard.pig.store.fs;

import org.apache.pig.data.DataBag;
import org.apache.pig.data.Tuple;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

/**
 * Class represent the result of Pig-latin script execution, which
 * returns amount of every value of additional parameter
 * relatively to particular event and  date.<br>
 *  
 * Incoming tuple should meet the requirement:<br> 
 * (chararray: event, chararray: paramName, int: date, bag: {(chararray: paramValue, long: value)}).
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class EventParamFileObject extends FileObject
{

   /**
    * The list of actual key field names.
    */
   public final static String[] KEY_FIELDS = {"event", "param", "date"};

   /**
    * Corresponding {@link ScriptResultType}.
    */
   private final static ScriptResultType type = ScriptResultType.EVENT_PARAM;

   /**
    * {@link EventParamFileObject} constructor.
    */
   public EventParamFileObject(String baseDir, String event, String param, int date) throws IOException
   {
      super(baseDir, type, makeKeys(KEY_FIELDS, event, param, date));
   }

   /**
    * {@link EventParamFileObject} constructor.
    */
   public EventParamFileObject(String baseDir, Tuple tuple) throws IOException
   {
      super(baseDir, type, tuple, KEY_FIELDS);
   }

   /**
    * {@inheritedDoc)
    */
   @Override
   protected Properties doRead(BufferedReader reader) throws IOException
   {
      Properties props = new Properties();
      props.load(reader);

      return new ImmutableProperties(props);
   }

   /**
    * {@inheritedDoc)
    */
   @Override
   protected void doWrite(BufferedWriter writer, Object value) throws IOException
   {
      Properties props = translateValue(value);
      props.store(writer, null);
   }

   /**
    * {@inheritedDoc)
    */
   @Override
   protected Properties translateValue(Object value) throws IOException
   {
      if (value instanceof Properties)
      {
         return (Properties)value;
      }
      else if (value instanceof DataBag)
      {
         Properties props = new Properties();

         Iterator<Tuple> iter = ((DataBag)value).iterator();
         while (iter.hasNext())
         {
            Tuple tuple = iter.next();
            props.put(tuple.get(0).toString(), tuple.get(1).toString());
         }

         return new ImmutableProperties(props);
      }

      throw new IOException("Unknown class " + value.getClass().getName());
   }
}
