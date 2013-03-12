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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TaskManager
{
   
   /**
    * Logger.
    */
   private static final Logger LOG = LoggerFactory.getLogger(TaskManager.class);

   /**
    * Runtime parameter name. Contains the name of directory where tasks are located.
    */
   public static final String DASHBOARD_SCRIPT_DIRECTORY_PROPERTY = "dashboard.tasks.directory";

   /**
    * The directory where tasks are located.
    */
   public static final File DIR = new File(System.getProperty(DASHBOARD_SCRIPT_DIRECTORY_PROPERTY));

   /**
    * File extension for task file.
    */
   protected static final String TASK_FILE_EXTENTION = ".task";

   /**
    * The date when tasks are supposed to be executed.
    */
   private final Calendar dayOfExecution;
   
   /**
    * The list of resources to inspect by tasks.
    */
   private final String resources; 
   
   /**
    * {@link TaskManager} constructor.
    * 
    * @param resources {@link #resources}
    * @param dayOfExecution {@link #dayOfExecution}
    */
   public TaskManager(String resources, Calendar dayOfExecution)
   {
      this.dayOfExecution = dayOfExecution;
      this.resources = resources;
   }

   /**
    * Loads all available tasks.
    */
   public List<Task> loadAll() throws IOException
   {
      File[] taskFiles = DIR.listFiles(new FilenameFilter()
      {
         @Override
         public boolean accept(File dir, String name)
         {
            return name.endsWith(TASK_FILE_EXTENTION);
         }
      });
      
      if (taskFiles == null || taskFiles.length == 0)
      {
         return Collections.emptyList();
      }
         
      List<Task> tasks = new ArrayList<Task>(taskFiles.length);
      for (File file : taskFiles)
      {
          tasks.add(doLoadTask(file));
      }
         
      return tasks;
   }

   /**
    * Loads task properties from given file.
    */
   private Task doLoadTask(File file) throws IOException
   {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      try
      {
         String fileName = file.getName();
         String taskName = fileName.substring(0, fileName.length() - TASK_FILE_EXTENTION.length());
         
         Properties props = new Properties();
         props.load(reader);
         
         Calendar dayOfExecution = Calendar.getInstance();
         dayOfExecution.setTime(this.dayOfExecution.getTime());
         
         return new Task(resources, taskName, dayOfExecution, props);
      }
      finally
      {
         try
         {
            reader.close();
         }
         catch (IOException e)
         {
            LOG.error("Can not close reader", e);
         }
      }
   }

   /**
    * Loads task by given name.
    */
   public Task load(String taskName) throws IOException
   {
      File taskFile = new File(DIR, taskName + TASK_FILE_EXTENTION);
      return doLoadTask(taskFile);
   }
}
