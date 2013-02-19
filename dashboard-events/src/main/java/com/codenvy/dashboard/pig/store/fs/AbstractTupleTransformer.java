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

import com.codenvy.dashboard.pig.store.fs.TupleTransformerFactory.ScriptType;

import java.io.File;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class AbstractTupleTransformer implements TupleTransformer
{
   /**
    * Based script type.
    */
   protected final ScriptType type;

   /**
    * AbstractTupleTransformer constructor.
    */
   AbstractTupleTransformer(ScriptType type)
   {
      this.type = type;
   }

   /**
    * Parses date represented like YYYYMMDD and returns date
    * like YYYY/MM/DD delimited by {@link File#separatorChar} character.
    */
   protected String parseDate(int date)
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
