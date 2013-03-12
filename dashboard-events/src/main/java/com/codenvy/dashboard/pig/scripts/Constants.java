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
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class Constants
{
   /**
    * The relation name in the Pig script, contains the execution result.
    */
   public static final String FINAL_RELATION = "result";
   
   /**
    * The parameter for Pig script. Contains the value of event identifier.
    */
   public static final String EVENT = "event";

   /**
    * The parameter for Pig script.
    * Contains the name of some additional identifier.
    */
   public static final String PARAM_NAME = "paramName";
   
   /**
    * The parameter for Pig script.
    * Contains the name of some additional identifier.
    */
   public static final String SECOND_PARAM_NAME = "secondParamName";
   
   /**
    * Parameter name in Pig script. Contains the date to check.
    * For instance: 20101201. What means the first of December in 2010 year.
    * Als is used as beginning of the time frame.
    */
   public static final String DATE = "date";

   /**
    * Time-frame ending date. Has format YYYYMMDD.
    */
   public static final String TO_DATE = "toDate";

   /**
    * Parameter name in Pig script contains the resources are needed to be loaded.
    * Can be either the name of single resource (file or directory) or the list of 
    * comma separated resources. Wildcard characters are supported.
    */
   public static final String LOG = "log";

   /**
    * Private constructor.
    */
   private Constants()
   {
   }
}
