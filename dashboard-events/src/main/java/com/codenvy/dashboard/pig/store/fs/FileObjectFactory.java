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
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class FileObjectFactory
{
   /**
    * Creates {@link FileObject} based on given {@link ScriptResultType}.
    * 
    * @throws IllegalArgumentException if <code>resultType</code> is not supported or unknown
    * @throws IOException
    */
   public static FileObject createFileObject(String resultType, Tuple tuple) throws IllegalArgumentException,
      IOException
   {
      ScriptResultType type = ScriptResultType.valueOf(resultType.toUpperCase());

      switch (type)
      {
         case EVENT_OCCURRENCE :
            return EventOccurrenceFileObject.valueOf(tuple);

         default :
            throw new IllegalArgumentException("Script type " + type + " is not supported");
      }
   }
   
   /**
    * Enumeration of all Pig-latin script's results. 
    */
   public static enum ScriptResultType
   {
      /**
       * Represent simple result of specific event and date.
       * See {@link SpecificEventFileObject} more for details.
       */
      EVENT_OCCURRENCE
   }
}
