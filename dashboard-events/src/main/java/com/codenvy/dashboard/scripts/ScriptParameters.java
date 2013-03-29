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


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public enum ScriptParameters
{
   FROM_DATE {
      @Override
      public String getDefaultValue()
      {
         return "20000101";
      }

      @Override
      public String getName()
      {
         return "fromDate";
      }
   },

   TO_DATE {
      @Override
      public String getDefaultValue()
      {
         return "21001231";
      }

      @Override
      public String getName()
      {
         return "toDate";
      }
   },

   LAST_MINUTES {
      @Override
      public String getDefaultValue()
      {
         return "60";
      }

      @Override
      public String getName()
      {
         return "lastMinutes";
      }
   },

   SESSIONS_COUNT {
      @Override
      public String getDefaultValue()
      {
         return "2";
      }

      @Override
      public String getName()
      {
         return "sessionsCount";
      }
   },

   TOP {
      @Override
      public String getDefaultValue()
      {
         return "10";
      }

      @Override
      public String getName()
      {
         return "top";
      }
   },

   INACTIVE_INTERVAL {
      @Override
      public String getDefaultValue()
      {
         return "10";
      }

      @Override
      public String getName()
      {
         return "inactiveInterval";
      }
   };

   public abstract String getDefaultValue();

   public abstract String getName();
}
