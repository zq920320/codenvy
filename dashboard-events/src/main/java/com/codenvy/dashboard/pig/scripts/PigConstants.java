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

/**
 * Constants list. For the record, all parameters will be substituted 
 * in Pig script at runtime.
 * 
 * @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a>
 */
public class PigConstants
{
   /**
    * Relation name in Pig script which contains execution results.
    */
   public static final String FINAL_RELATION = "result";
   
   /**
    * Parameter name in Pig script contains the event id.
    */
   public static final String EVENT_PARAM = "event";

   /**
    * Parameter name in Pig script contains the beginning of time frame.
    * For instance: 20101201. What means the first of December in 2010 year.
    */
   public static final String FROM_PARAM = "from";

   /**
    * Parameter name in Pig script contains the ending of time frame.
    * For instance: 20101202. What means the second of December in 2010 year.
    */
   public static final String TO_PARAM = "to";

   /**
    * Parameter name in Pig script contains the resources are needed to be loaded.
    * Can be either the name of single resource (file or directory) or the list of 
    * comma separated resources. Wildcard characters are supported.
    */
   public static final String LOG_PARAM = "log";

   /**
    * Private constructor.
    */
   private PigConstants()
   {
   }
}
