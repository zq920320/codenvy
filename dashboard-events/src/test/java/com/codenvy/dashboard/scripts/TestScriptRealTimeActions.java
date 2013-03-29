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

import com.codenvy.dashboard.scripts.util.Event;
import com.codenvy.dashboard.scripts.util.LogGenerator;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestScriptRealTimeActions extends BasePigTest
{

   @Test
   public void testRealtimeUsersWhoLoggedInInLastMinutes() throws Exception
   {
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss,SSS");

      Calendar cal = Calendar.getInstance();

      List<Event> events = new ArrayList<Event>();

      // current time
      events.add(Event.Builder.createUserSSOLoggedInEvent("user1", "google").withDate(dateFormat.format(cal.getTime()))
         .withTime(timeFormat.format(cal.getTime())).build());

      // in 1 minutes
      cal.add(Calendar.MINUTE, -1);
      events.add(Event.Builder.createUserSSOLoggedInEvent("user2", "google").withDate(dateFormat.format(cal.getTime()))
         .withTime(timeFormat.format(cal.getTime())).build());

      // in 5 minutes
      cal.add(Calendar.MINUTE, -4);
      events.add(Event.Builder.createUserSSOLoggedInEvent("user3", "google").withDate(dateFormat.format(cal.getTime()))
         .withTime(timeFormat.format(cal.getTime())).build());

      // in 10 minutes 
      cal.add(Calendar.MINUTE, -5);
      events.add(Event.Builder.createUserSSOLoggedInEvent("user4", "google").withDate(dateFormat.format(cal.getTime()))
         .withTime(timeFormat.format(cal.getTime())).build());

      File log = LogGenerator.generateLog(events);

      Map<String, String> params = new HashMap<String, String>();
      params.put(ScriptParameters.LAST_MINUTES.getName(), "6");

      FileObject fileObject = executeAndReturnResult(ScriptType.REALTIME_USER_SSO_LOGGED_IN, log, params);

      List<String> list = (List<String>)fileObject.getValue();
      Assert.assertEquals(list.size(), 3);
   }

   @Test
   public void testRealtimeWorkspacesWithSeveralUsers() throws Exception
   {
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss,SSS");

      Calendar cal = Calendar.getInstance();

      List<Event> events = new ArrayList<Event>();

      // current time
      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws1", "session", "project")
         .withDate(dateFormat.format(cal.getTime())).withTime(timeFormat.format(cal.getTime())).build());
      events.add(Event.Builder.createProjectCreatedEvent("user2", "ws1", "session", "project")
         .withDate(dateFormat.format(cal.getTime())).withTime(timeFormat.format(cal.getTime())).build());

      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws2", "session", "project")
         .withDate(dateFormat.format(cal.getTime())).withTime(timeFormat.format(cal.getTime())).build());

      // in 10 minutes 
      cal.add(Calendar.MINUTE, -10);
      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws2", "session", "project")
         .withDate(dateFormat.format(cal.getTime())).withTime(timeFormat.format(cal.getTime())).build());

      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws3", "session", "project")
         .withDate(dateFormat.format(cal.getTime())).withTime(timeFormat.format(cal.getTime())).build());
      events.add(Event.Builder.createProjectCreatedEvent("user2", "ws3", "session", "project")
         .withDate(dateFormat.format(cal.getTime())).withTime(timeFormat.format(cal.getTime())).build());

      File log = LogGenerator.generateLog(events);

      Map<String, String> params = new HashMap<String, String>();
      params.put(ScriptParameters.LAST_MINUTES.getName(), "6");

      FileObject fileObject = executeAndReturnResult(ScriptType.REALTIME_WS_WITH_SEVERAL_USERS, log, params);

      Properties props = (Properties)fileObject.getValue();
      Assert.assertEquals(props.size(), 1);
      Assert.assertEquals(props.getProperty("ws1"), "2");
   }
}
