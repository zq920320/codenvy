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
public class TestScriptActiveWorkspaces extends BasePigTest
{
   @Test
   public void testEventFound1UseCase() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1").withDate("2010-10-01").build());
      events.add(Event.Builder.createTenantCreatedEvent("ws2", "user2").withDate("2010-10-02").build());
      events.add(Event.Builder.createTenantCreatedEvent("ws3", "user3").withDate("2010-10-02").build());

      // events should not be taken in account
      events.add(Event.Builder.createTenantDestroyedEvent("ws3").withDate("2010-10-05").build());
      events.add(Event.Builder.createTenantStoppedEvent("ws4").withDate("2010-10-05").build());
      events.add(Event.Builder.createUserSSOLoggedOutEvent("user1").withDate("2010-10-05").build());

      File log = LogGenerator.generateLog(events);

      executePigScript(ScriptType.ACTIVE_WORKSPACES, log, new String[][]{{Constants.FROM_DATE, "20101001"},
         {Constants.TO_DATE, "20101005"}});

      FileObject fileObject = ScriptType.ACTIVE_WORKSPACES.createFileObject(BASE_DIR, 20101001, 20101005);

      Long value = (Long)fileObject.getValue();
      Assert.assertEquals(value, Long.valueOf(3));
   }

   @Test
   public void testEventFound2UseCase() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1").withDate("2010-10-01").build());
      events.add(Event.Builder.createProjectCreatedEvent("user2", "ws2", "session", "project").withDate("2010-10-02")
         .build());
      events.add(Event.Builder.createProjectCreatedEvent("user2", "ws3", "session", "project").withDate("2010-10-02")
         .build());
      events.add(Event.Builder.createUserCreatedEvent("userId", "hello@gmail").withDate("2010-10-03").build());
      events.add(Event.Builder.createProjectCreatedEvent("user2", "ws5", "session", "project").withDate("2010-10-11")
         .build());

      File log = LogGenerator.generateLog(events);

      executePigScript(ScriptType.ACTIVE_WORKSPACES, log, new String[][]{{Constants.FROM_DATE, "20101001"},
         {Constants.TO_DATE, "20101005"}});

      FileObject fileObject = ScriptType.ACTIVE_WORKSPACES.createFileObject(BASE_DIR, 20101001, 20101005);

      Long value = (Long)fileObject.getValue();
      Assert.assertEquals(value, Long.valueOf(3));
   }
}
