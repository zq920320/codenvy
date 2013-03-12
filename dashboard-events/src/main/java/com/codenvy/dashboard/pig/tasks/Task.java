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
package com.codenvy.dashboard.pig.tasks;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codenvy.dashboard.pig.scripts.Constants;
import com.codenvy.dashboard.pig.scripts.ScriptExecutor;
import com.codenvy.dashboard.pig.scripts.ScriptType;

/**
 * Represent task that can be mapped to Pig-script with specific parameters
 * and executed later by {@link ScriptExecutor}.
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class Task
{
   /**
    * Logger.
    */
   private static final Logger LOG = LoggerFactory.getLogger(Task.class);
   
   /**
    * Related {@link ScriptType}.
    */
   private final ScriptType scriptType;
   
   /**
    * The simple description for task. Gives the answer what actually task does.
    */
   private final String description;
   
   /**
    * What period the task should cover. The {@link Constants#DATE} parameter
    * depends on this value. 
    */
   private final int timeFrame;
   
   /**
    * The name for task.
    */
   private final String name;
   
   /**
    * Pig script execution context. Is used by {@link ScriptExecutor}.
    */
   private final Map<String, String> context;
   
   /**
    * {@link Task} constructor.
    * 
    * @param resources the list of resources to set for {@link Constants#LOG} parameter
    * @param name the task name
    * @param dayOfExecution the date when task is considering to be executed
    * @param props the task properties
    */
   public Task(String resources, String name, Calendar dayOfExecution, Properties props) throws IllegalArgumentException
   {
      this.name = name;
      this.timeFrame = Integer.valueOf(props.getProperty("time-frame"));
      this.scriptType = ScriptType.valueOf(props.getProperty("script-type").toUpperCase());
      this.description = props.getProperty("description");
      this.context = new HashMap<String, String>();

      initializeContext(resources, dayOfExecution, props);
   }

   private void initializeContext(String resources, Calendar dayOfExecution, Properties props)
   {
      context.put(Constants.LOG, resources);
      initializeTimeFrame(dayOfExecution);
      
      for (String keyField : scriptType.getResultType().getKeyFields())
      {
         if (!context.containsKey(keyField))
         {
            String value = props.getProperty(keyField);
            if (value == null)
            {
               throw new IllegalArgumentException("Context does not contains parameter '" + keyField + "'");
            }

            context.put(keyField, value);
         }
      }
      
      LOG.info("Task '{}' initialized with parameters: {}", name, context.toString());
   }

   /**
    * Puts {@link Constants#DATE} and {@link Constants#TO_DATE} parameters into {@link #context}.
    * {@link Constants#TO_DATE} parameter always will be set equal to <code>dayOfExecution</code>
    * and {@link Constants#DATE} parameter will be set the {@link #timeFrame} - 1 days before.
    * Respectively in case {@link #timeFrame} equals to 1 the both parameters will be the same.
    * 
    * @param dayOfExecution the date when task is considering to be executed
    */
   private void initializeTimeFrame(Calendar dayOfExecution)
   {
      context.put(Constants.TO_DATE, formatDate(dayOfExecution));
      
      dayOfExecution.add(Calendar.DATE, -timeFrame + 1);
      context.put(Constants.DATE, formatDate(dayOfExecution));
   }

   /**
    * Formats date in way used into Pig scripts.
    */
   private String formatDate(Calendar cal)
   {
      int year = cal.get(Calendar.YEAR);
      int month = cal.get(Calendar.MONTH) + 1; // month begins from 0
      int day = cal.get(Calendar.DATE);
      
      return Integer.valueOf(year * 10000 + month * 100 + day).toString();
   }

   /**
    * @return {@link #description}
    */
   public String getDescription()
   {
      return description;
   }

   /**
    * @return {@link #scriptType}
    */
   public ScriptType getScriptType()
   {
      return scriptType;
   }

   /**
    * @return {@link #timeFrame}
    */
   public int getTimeFrame()
   {
      return timeFrame;
   }

   /**
    * @return {@link #name}
    */
   public String getName()
   {
      return name;
   }

   /**
    * @return {@link #context}
    */
   public Map<String, String> getContext()
   {
      return context;
   }
}
