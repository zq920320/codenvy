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

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestScriptUsersWithoutProjects extends BasePigTest
{

   /**
    * Run script which find all events.  
    */
   @Test
   public void testEventFound() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createUserCreatedEvent("user", "user1").withDate("2010-10-01").build());
      events.add(Event.Builder.createUserCreatedEvent("user", "user2").withDate("2010-10-01").build());
      events.add(Event.Builder.createUserCreatedEvent("user", "user3").withDate("2010-10-02").build());

      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws", "session", "project").withDate("2010-10-01")
         .build());
      events.add(Event.Builder.createProjectCreatedEvent("user3", "ws", "session", "project").withDate("2010-10-03")
         .build());

      File log = LogGenerator.generateLog(events);

      executePigScript(ScriptType.USERS_WITHOUT_PROJECTS, log, new String[][]{{Constants.FROM_DATE, "20101001"},
         {Constants.TO_DATE, "20101002"}});

      FileObject fileObject = ScriptType.USERS_WITHOUT_PROJECTS.createFileObject(BASE_DIR, 20101001, 20101002);

      List<String> list = (List<String>)fileObject.getValue();
      Assert.assertEquals(list.size(), 1);
      Assert.assertEquals(list.get(0), "user2");
   }
}
