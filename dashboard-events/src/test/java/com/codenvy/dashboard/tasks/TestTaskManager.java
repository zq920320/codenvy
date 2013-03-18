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
package com.codenvy.dashboard.tasks;

import com.codenvy.dashboard.pig.scripts.BasePigTest;
import com.codenvy.dashboard.pig.scripts.Constants;
import com.codenvy.dashboard.pig.scripts.FileObject;
import com.codenvy.dashboard.pig.scripts.ScriptExecutor;
import com.codenvy.dashboard.pig.scripts.ScriptType;
import com.codenvy.dashboard.pig.scripts.util.Event;
import com.codenvy.dashboard.pig.scripts.util.LogGenerator;
import com.codenvy.dashboard.pig.tasks.Task;
import com.codenvy.dashboard.pig.tasks.TaskManager;

import org.junit.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestTaskManager extends  BasePigTest
{
   @Test
   public void testLoadAllTasks() throws Exception
   {
      TaskManager manager = new TaskManager(null, Calendar.getInstance());
      List<Task> tasks = manager.loadAll();
      
      Assert.assertFalse(tasks.isEmpty());
      
      for (Task task : tasks)
      {
         Assert.assertNotNull(task.getName());
         Assert.assertNotNull(task.getDescription());
         Assert.assertNotNull(task.getScriptType());
         Assert.assertTrue(task.getTimeFrame() >= 1);
         Assert.assertNotNull(task.getContext());
         Assert.assertTrue(task.getContext().size() >= 2);
      }
   }
   
   @Test
   public void testLoadSpecificTasks() throws Exception
   {
      Calendar cal = Calendar.getInstance();
      cal.setTime(Date.valueOf("2013-10-02"));
      
      TaskManager manager = new TaskManager(null, cal);
      Task task = manager.load("active-project-count");

      Assert.assertNotNull(task.getDescription());
      
      Assert.assertEquals("active-project-count", task.getName());
      Assert.assertEquals(ScriptType.ACTIVE_PROJECTS, task.getScriptType());
      Assert.assertEquals(7, task.getTimeFrame());
      
      Map<String, String> context = task.getContext();
      Assert.assertEquals(context.get(Constants.TO_DATE), "20131002");
      Assert.assertEquals(context.get(Constants.DATE), "20130926");
   }

   @Test
   public void testExecuteTask() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createProjectCreatedEvent("user2", "ws2", "session", "project1").withDate("2013-09-26")
         .build());
      events.add(Event.Builder.createProjectCreatedEvent("user2", "ws2", "session", "project1").withDate("2013-10-02")
         .build());
      events.add(Event.Builder.createProjectCreatedEvent("user2", "ws2", "session", "project2").withDate("2013-10-03")
         .build());
      File log = LogGenerator.generateLog(events);

      Calendar cal = Calendar.getInstance();
      cal.setTime(Date.valueOf("2013-10-02"));
      
      TaskManager manager = new TaskManager(log.getAbsolutePath(), cal);
      ScriptExecutor executor = ScriptExecutor.valueOf(manager.load("active-project-count"));
            
      executor.executeAndStoreResult(BASE_DIR);
      
      FileObject fileObject = ScriptType.ACTIVE_PROJECTS.createFileObject(BASE_DIR, 20130926, 20131002);

      Long value = (Long)fileObject.getValue();
      Assert.assertEquals(Long.valueOf(1), value);
   }
}
