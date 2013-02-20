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

import com.codenvy.dashboard.pig.store.fs.FileObjectFactory.ScriptResultType;

import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.Tuple;

import java.io.File;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
@SuppressWarnings("serial")
public class EventOccurrenceFileObject extends FileObject
{

   /**
    * Related {@link ScriptResultType}.
    */
   private final static ScriptResultType type = ScriptResultType.EVENT_OCCURRENCE;

   /**
    * {@link EventOccurrenceFileObject} constructor. Constrains all necessary
    * parameters to make unique identifier of object.     
    */
   public EventOccurrenceFileObject(String event, int date) throws ExecException
   {
      super(type, makeId(event, date));
   }

   /**
    * Transform the result of {@link ScriptResultType#EVENT_OCCURRENCE} type
    * into the {@link FileObject}<br>
    *   
    * Incoming tuple: (int: date, chararray: event, long: number)<br>
    */
   public static FileObject valueOf(Tuple tuple) throws ExecException
   {
      FileObject props = new EventOccurrenceFileObject(fetchEvent(tuple), fetchDate(tuple));
      props.put("event", fetchEvent(tuple));
      props.put("date", fetchDate(tuple).toString());
      props.put("count", fetchCount(tuple).toString());

      return props;
   }

   /**
    * Returns the unique identifier of the calculated data.
    */
   private static String makeId(String event, int date) throws ExecException
   {
      StringBuilder id = new StringBuilder();
      id.append(event.replace('-', File.separatorChar)).append(File.separatorChar);
      id.append(parseDate(date));

      return id.toString();
   }

   private static Long fetchCount(Tuple tuple) throws ExecException
   {
      return (Long)tuple.get(2);
   }

   private static Integer fetchDate(Tuple tuple) throws ExecException
   {
      return (Integer)tuple.get(0);
   }

   private static String fetchEvent(Tuple tuple) throws ExecException
   {
      return (String)tuple.get(1);
   }

   /**
    * Parses date represented like YYYYMMDD and returns date
    * like YYYY/MM/DD delimited by {@link File#separatorChar} character.
    */
   protected static String parseDate(int date)
   {
      int year = date / 10000;
      int month = (date - year * 10000) / 100;
      int day = date - year * 10000 - month * 100;

      StringBuilder dateInStr = new StringBuilder();
      dateInStr.append(year).append(File.separatorChar);

      if (month < 10)
      {
         dateInStr.append("0");
      }
      dateInStr.append(month).append(File.separatorChar);

      if (day < 10)
      {
         dateInStr.append("0");
      }
      dateInStr.append(day);

      return dateInStr.toString();
   }

}
