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
public class TestScriptTopWsByEvents extends BasePigTest
{

   @Test
   public void testTopWsByUsers() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createUserAddedToWsEvent("user1", "ws1", "ses", "ws1", "user1", "website")
         .withDate("2010-10-01")
         .build());
      events.add(Event.Builder.createUserAddedToWsEvent("user2", "ws1", "ses", "ws1", "user2", "website")
         .withDate("2010-10-01")
         .build());
      events.add(Event.Builder.createUserAddedToWsEvent("user3", "ws1", "ses", "ws1", "user3", "website")
         .withDate("2010-10-01")
         .build());
      events.add(Event.Builder.createUserAddedToWsEvent("user1", "ws2", "ses", "ws2", "user1", "website")
         .withDate("2010-10-01")
         .build());
      events.add(Event.Builder.createUserAddedToWsEvent("user2", "ws2", "ses", "ws2", "user2", "website")
         .withDate("2010-10-01")
         .build());

      File log = LogGenerator.generateLog(events);

      executePigScript(ScriptType.TOP_WS_BY_USERS, log, new String[][]{{Constants.FROM_DATE, "20101001"},
         {Constants.TO_DATE, "20101001"}});

      FileObject fileObject = ScriptType.TOP_WS_BY_USERS.createFileObject(BASE_DIR, 20101001, 20101001);


      Properties props = (Properties)fileObject.getValue();
      Assert.assertEquals(props.getProperty("ws1"), "3");
      Assert.assertEquals(props.getProperty("ws2"), "2");
   }

   @Test
   public void testTopWsByInvitations() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createUserInviteEvent("user1", "ws1", "ses", "email1").withDate("2010-10-01").build());
      events.add(Event.Builder.createUserInviteEvent("user1", "ws1", "ses", "email2").withDate("2010-10-01").build());
      events.add(Event.Builder.createUserInviteEvent("user1", "ws1", "ses", "email3").withDate("2010-10-01").build());
      events.add(Event.Builder.createUserInviteEvent("user1", "ws2", "ses", "email1").withDate("2010-10-01").build());
      events.add(Event.Builder.createUserInviteEvent("user1", "ws2", "ses", "email2").withDate("2010-10-01").build());
      events.add(Event.Builder.createUserInviteEvent("user1", "ws3", "ses", "email1").withDate("2010-10-01").build());
      File log = LogGenerator.generateLog(events);

      executePigScript(ScriptType.TOP_WS_BY_INVITATIONS, log, new String[][]{{Constants.FROM_DATE, "20101001"},
         {Constants.TO_DATE, "20101001"}});

      FileObject fileObject = ScriptType.TOP_WS_BY_INVITATIONS.createFileObject(BASE_DIR, 20101001, 20101001);

      Properties props = (Properties)fileObject.getValue();
      Assert.assertEquals(props.getProperty("ws1"), "3");
      Assert.assertEquals(props.getProperty("ws2"), "2");
      Assert.assertEquals(props.getProperty("ws3"), "1");
   }

   @Test
   public void testTopWsByBuilds() throws Exception
   {
      List<Event> events = new ArrayList<Event>();

      // ws1 - 2
      events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "ses", "project1", "type")
         .withDate("2010-10-01").build());
      events.add(Event.Builder.createApplicationCreatedEvent("user1", "ws1", "ses", "project2", "type", "paas")
         .withDate("2010-10-01").build());
      events.add(Event.Builder.createProjectDeployedEvent("user1", "ws1", "ses", "project3", "type", "paas")
         .withDate("2010-10-01").build());

      // ws2 - 2
      events.add(Event.Builder.createProjectBuiltEvent("user1", "ws2", "ses", "project1", "type")
         .withDate("2010-10-01").build());
      events.add(Event.Builder.createApplicationCreatedEvent("user1", "ws2", "ses", "project2", "type", "paas")
         .withDate("2010-10-01").build());

      // ws3 - 1
      events.add(Event.Builder.createProjectBuiltEvent("user1", "ws3", "ses", "project1", "type")
         .withDate("2010-10-01").build());
      File log = LogGenerator.generateLog(events);

      executePigScript(ScriptType.TOP_WS_BY_BUILDS, log, new String[][]{{Constants.FROM_DATE, "20101001"},
         {Constants.TO_DATE, "20101001"}});

      FileObject fileObject = ScriptType.TOP_WS_BY_BUILDS.createFileObject(BASE_DIR, 20101001, 20101001);

      Properties props = (Properties)fileObject.getValue();
      Assert.assertEquals(props.getProperty("ws1"), "3");
      Assert.assertEquals(props.getProperty("ws2"), "2");
      Assert.assertEquals(props.getProperty("ws3"), "1");
   }

   @Test
   public void testTopWsByProjects() throws Exception
   {
      List<Event> events = new ArrayList<Event>();

      // ws1 - 2
      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws1", "ses", "project1").withDate("2010-10-01")
         .build());
      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws1", "ses", "project2").withDate("2010-10-01")
         .build());
      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws1", "ses", "project3").withDate("2010-10-01")
         .build());
      events.add(Event.Builder.createProjectDestroyedEvent("user1", "ws1", "ses", "project2").withDate("2010-10-01")
         .build());

      // ws2 - 2
      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws2", "ses", "project1").withDate("2010-10-01")
         .build());
      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws2", "ses", "project2").withDate("2010-10-01")
         .build());

      // ws3 - 1
      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws3", "ses", "project1").withDate("2010-10-01")
         .build());

      File log = LogGenerator.generateLog(events);

      executePigScript(ScriptType.TOP_WS_BY_PROJECTS, log, new String[][]{{Constants.FROM_DATE, "20101001"},
         {Constants.TO_DATE, "20101001"}});

      FileObject fileObject = ScriptType.TOP_WS_BY_PROJECTS.createFileObject(BASE_DIR, 20101001, 20101001);

      Properties props = (Properties)fileObject.getValue();
      Assert.assertEquals(props.getProperty("ws1"), "2");
      Assert.assertEquals(props.getProperty("ws2"), "2");
      Assert.assertEquals(props.getProperty("ws3"), "1");
   }
}
