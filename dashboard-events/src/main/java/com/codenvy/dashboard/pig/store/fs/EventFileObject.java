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

import org.apache.pig.data.Tuple;

import java.io.IOException;

/**
 * Class represent the result of Pig-latin script execution, which
 * returns the amount of specific event occurrence for particular
 * date. Incoming tuple should meet the requirement:<br> 
 * (chararray: event, int: date, long: value).
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class EventFileObject extends FileObject
{

   /**
    * The list of actual key field names.
    */
   public final static String[] KEY_FIELDS = {"event", "date"};

   /**
    * Corresponding {@link ScriptResultType}.
    */
   private final static ScriptResultType type = ScriptResultType.EVENT;

   /**
    * {@link EventFileObject} constructor.
    */
   public EventFileObject(String baseDir, String event, int date) throws IOException
   {
      super(baseDir, type, makeKeys(KEY_FIELDS, event, date));
   }

   /**
    * {@link EventFileObject} constructor.
    */
   public EventFileObject(String baseDir, Tuple tuple) throws IOException
   {
      super(baseDir, type, tuple, KEY_FIELDS);
   }
}
