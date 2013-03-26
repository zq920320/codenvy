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
public class Constants
{
   /**
    * The relation name in the Pig script, contains the execution result.
    */
   public static final String FINAL_RELATION = "result";
   
   /**
    * Beginning of the time frame. Has format YYYYMMDD.
    */
   public static final String FROM_DATE = "fromDate";

   /**
    * Ending of the time frame. Has format YYYYMMDD.
    */
   public static final String TO_DATE = "toDate";

   /**
    * Parameter name in Pig script contains the resources are needed to be loaded.
    * Can be either the name of single resource (file or directory) or the list of 
    * comma separated resources. Wildcard characters are supported.
    */
   public static final String LOG = "log";

   /**
    * Not a mandatory parameter for {@link ScriptType#PRODUCT_USAGE_TIME}.
    */
   protected static final String INACTIVE_INTERVAL = "inactiveInterval";

   /**
    * Not a mandatory parameter for 'top workspaces by event' scripts.
    */
   protected static final String TOP = "top";

   /**
    * Not a mandatory parameter when script supports storing result.
    */
   protected static final String STORE_LOCATION = "storeLocation";

   /**
    * Private constructor.
    */
   private Constants()
   {
   }
}
