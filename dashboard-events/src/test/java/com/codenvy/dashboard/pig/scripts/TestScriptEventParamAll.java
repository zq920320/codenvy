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

import com.codenvy.dashboard.pig.scripts.util.Event;
import com.codenvy.dashboard.pig.scripts.util.LogGenerator;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestScriptEventParamAll extends BasePigTest
{
   @Test
   public void testEventFound() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws1", "ses", "project1").withDate("2010-10-01")
         .build());
      events.add(Event.Builder.createProjectCreatedEvent("user2", "ws1", "ses", "project1").withDate("2010-10-01")
         .build());
      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws2", "ses", "project2").withDate("2010-10-01")
         .build());
      events.add(Event.Builder.createProjectCreatedEvent("user2", "ws2", "ses", "project1").withDate("2010-10-01")
         .build());

      File log = LogGenerator.generateLog(events);

      executePigScript(ScriptType.EVENT_PARAM_ALL, log, new String[][]{{Constants.EVENT, "project-created"},
         {Constants.PARAM_NAME, "PROJECT"}, {Constants.DATE, "20101001"}});
      
      FileObject fileObject =
         ScriptType.EVENT_PARAM_ALL.getResultType().createFileObject(BASE_DIR, "project-created", "project", 20101001);
      Properties props = (Properties)fileObject.getValue();
      
      Assert.assertNotNull(props.get("project1"));
      Assert.assertNotNull(props.get("project2"));

      Assert.assertEquals(props.get("project1"), "3");
      Assert.assertEquals(props.get("project2"), "1");
   }

   @Test
   public void testEventNotFoundStoredDefaultValue() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws1", "ses", "project1").withDate("2010-10-01")
         .build());
      events.add(Event.Builder.createProjectCreatedEvent("user2", "ws1", "ses", "project1").withDate("2010-10-01")
         .build());
      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws2", "ses", "project2").withDate("2010-10-01")
         .build());
      events.add(Event.Builder.createProjectCreatedEvent("user2", "ws2", "ses", "project1").withDate("2010-10-01")
         .build());

      File log = LogGenerator.generateLog(events);

      executePigScript(ScriptType.EVENT_PARAM_ALL, log, new String[][]{{Constants.EVENT, "project-created"},
         {Constants.PARAM_NAME, "PROJECT"}, {Constants.DATE, "20101002"}});

      FileObject fileObject =
         ScriptType.EVENT_PARAM_ALL.getResultType().createFileObject(BASE_DIR, "project-created", "project", 20101002);
      Properties props = (Properties)fileObject.getValue();
      Assert.assertTrue(props.isEmpty());
   }
}
