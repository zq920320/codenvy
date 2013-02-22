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

import org.apache.pig.data.DataBag;
import org.apache.pig.data.Tuple;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a>
 */
public class TestPigScripts extends BasePigTest
{

   /**
    * Finds users who created workspace but did not create project.
    */
   @Test
   public void shouldFindUsersWhoCreatedWsButProject() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createUserAddedToWsEvent("", "", "", "ws1", "user1").build());
      events.add(Event.Builder.createUserAddedToWsEvent("", "", "", "ws1", "user1").build());
      events.add(Event.Builder.createUserAddedToWsEvent("", "", "", "ws2", "user1").build());
      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws1", "ses", "project1").build());
      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws1", "ses", "project1").build());
      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws1", "ses", "project2").build());
      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws2", "ses", "project1").build());

      events.add(Event.Builder.createUserAddedToWsEvent("", "", "", "ws1", "user2").build());
      events.add(Event.Builder.createUserAddedToWsEvent("", "", "", "ws2", "user2").build());
      events.add(Event.Builder.createProjectCreatedEvent("user2", "ws2", "ses", "project2").build());

      events.add(Event.Builder.createUserAddedToWsEvent("", "", "", "ws2", "user3").build());

      File log = LogGenerator.generateLog(events);

      Iterator<Tuple> iter = runPigScriptAndGetResult("created-ws-but-project.pig", log, new String[][]{});

      Tuple tuple = iter.next();

      Assert.assertEquals(tuple.get(0), "user3");
      Assert.assertEquals(tuple.get(1), "ws2");
      Assert.assertNull(iter.next());
   }

   /**
    * Finds users who created workspace but did not create project between two dates.
    */
   @Test
   public void shouldFindUsersWhoCreatedWsButProjectBetweenDates() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createUserAddedToWsEvent("", "", "", "ws1", "user1").withDate("2010-10-01").build());
      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws1", "ses", "project1").withDate("2010-10-01")
         .build());

      events.add(Event.Builder.createUserAddedToWsEvent("", "", "", "ws2", "user2").withDate("2010-10-02").build());
      events.add(Event.Builder.createProjectCreatedEvent("user2", "ws2", "ses", "project1").withDate("2010-10-03")
         .build());

      events.add(Event.Builder.createUserAddedToWsEvent("", "", "", "ws3", "user3").withDate("2010-10-03").build());
      events.add(Event.Builder.createProjectCreatedEvent("user3", "ws3", "ses", "project1").withDate("2010-10-03")
         .build());

      File log = LogGenerator.generateLog(events);

      Iterator<Tuple> iter =
         runPigScriptAndGetResult("created-ws-but-project.pig", log, new String[][]{{PigConstants.FROM_PARAM, "20101001"},
            {PigConstants.TO_PARAM, "20101002"}});

      Tuple tuple = iter.next();

      Assert.assertEquals(tuple.get(0), "user2");
      Assert.assertEquals(tuple.get(1), "ws2");
      Assert.assertNull(iter.next());
   }

   /**
    * Run script which find specific event between time frame. 
    */
   @Test
   public void testEventScript() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createTenantCreatedEvent("ws1-1", "user1").withDate("2010-10-01").build());
      events.add(Event.Builder.createTenantCreatedEvent("ws1-1", "user1").withDate("2010-10-01").build()); // duplicate
      events.add(Event.Builder.createTenantCreatedEvent("ws4-1", "user4").withDate("2010-10-01").build());
      events.add(Event.Builder.createTenantDestroyedEvent("ws1-1").withDate("2010-10-01").build());
      events.add(Event.Builder.createTenantCreatedEvent("ws2-2", "user2").withDate("2010-10-02").build());
      events.add(Event.Builder.createTenantCreatedEvent("ws3-1", "user3").withDate("2010-10-03").build());

      File log = LogGenerator.generateLog(events);

      Iterator<Tuple> iter =
         runPigScriptAndGetResult("event.pig", log, new String[][]{
            {PigConstants.EVENT_PARAM, "tenant-created"},
            {PigConstants.FROM_PARAM, "20101001"}, {PigConstants.TO_PARAM, "20101002"}});

      Tuple tuple = iter.next();
      Assert.assertEquals(tuple.get(0), "tenant-created");
      Assert.assertEquals(tuple.get(1), 20101001);
      Assert.assertEquals(tuple.get(2), 2L);

      tuple = iter.next();
      Assert.assertEquals(tuple.get(0), "tenant-created");
      Assert.assertEquals(tuple.get(1), 20101002);
      Assert.assertEquals(tuple.get(2), 1L);
   }

   /**
    * Run script which find specific event. 
    */
   @Test
   public void testEventScriptWithoutTimeFrame() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1").withDate("2010-10-01").build());
      events.add(Event.Builder.createTenantCreatedEvent("ws2", "user2").withDate("2010-10-02").build());
      events.add(Event.Builder.createTenantCreatedEvent("ws3", "user3").withDate("2010-10-10").build());

      File log = LogGenerator.generateLog(events);
      
      Iterator<Tuple> iter =
         runPigScriptAndGetResult("event.pig", log,
            new String[][]{{PigConstants.EVENT_PARAM, "tenant-created"}});

      Tuple tuple = iter.next();
      Assert.assertEquals(tuple.get(0), "tenant-created");
      Assert.assertEquals(tuple.get(1), 20101001);
      Assert.assertEquals(tuple.get(2), 1L);

      tuple = iter.next();
      Assert.assertEquals(tuple.get(0), "tenant-created");
      Assert.assertEquals(tuple.get(1), 20101002);
      Assert.assertEquals(tuple.get(2), 1L);

      tuple = iter.next();
      Assert.assertEquals(tuple.get(0), "tenant-created");
      Assert.assertEquals(tuple.get(1), 20101010);
      Assert.assertEquals(tuple.get(2), 1L);
   }

   /**
    * Run script which find all events.  
    */
   @Test
   public void testEventAllScriptWithoutTimeFrame() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1").withDate("2010-10-01").build());
      events.add(Event.Builder.createTenantCreatedEvent("ws2", "user2").withDate("2010-10-01").build());
      events.add(Event.Builder.createTenantDestroyedEvent("ws3").withDate("2010-10-10").build());

      File log = LogGenerator.generateLog(events);

      Iterator<Tuple> iter = runPigScriptAndGetResult("event-all.pig", log, new String[][]{});

      Tuple tuple = iter.next();
      Assert.assertEquals(tuple.get(0), "tenant-created");
      Assert.assertEquals(tuple.get(1), 20101001);
      Assert.assertEquals(tuple.get(2), 2L);

      tuple = iter.next();
      Assert.assertEquals(tuple.get(0), "tenant-destroyed");
      Assert.assertEquals(tuple.get(1), 20101010);
      Assert.assertEquals(tuple.get(2), 1L);
   }

   /**
    * Find amount of project-created event with pair of every project name.
    */
   @Test
   public void testEventParamScript() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws1", "ses", "project1").withDate("2010-10-01")
         .build());
      events.add(Event.Builder.createProjectCreatedEvent("user2", "ws1", "ses", "project1").withDate("2010-10-01")
         .build());
      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws2", "ses", "project2").withDate("2010-10-03")
         .build());
      events.add(Event.Builder.createProjectCreatedEvent("user2", "ws2", "ses", "project1").withDate("2010-10-03")
         .build());

      File log = LogGenerator.generateLog(events);

      Iterator<Tuple> iter =
         runPigScriptAndGetResult("event-param.pig", log, new String[][]{{PigConstants.EVENT_PARAM, "project-created"},
            {PigConstants.PARAM_NAME_PARAM, "PROJECT"}});

      Tuple tuple = iter.next();
      Assert.assertEquals(tuple.get(0), "project-created");
      Assert.assertEquals(tuple.get(1), "PROJECT");
      Assert.assertEquals(tuple.get(2), 20101001);

      DataBag bag = (DataBag)tuple.get(3);
      Assert.assertEquals(bag.size(), 1L);

      Iterator<Tuple> bagIter = bag.iterator();
      Assert.assertTrue(bagIter.hasNext());

      tuple = bagIter.next();
      Assert.assertEquals(tuple.get(0), "project1");
      Assert.assertEquals(tuple.get(1), 2L);

      tuple = iter.next();
      Assert.assertEquals(tuple.get(0), "project-created");
      Assert.assertEquals(tuple.get(1), "PROJECT");
      Assert.assertEquals(tuple.get(2), 20101003);

      bag = (DataBag)tuple.get(3);
      Assert.assertEquals(bag.size(), 2L);

      bagIter = bag.iterator();
      Assert.assertTrue(bagIter.hasNext());

      tuple = bagIter.next();

      if (tuple.get(0).equals("project1"))
      {
         Assert.assertEquals(tuple.get(1), 1L);

         tuple = bagIter.next();
         Assert.assertEquals(tuple.get(0), "project2");
         Assert.assertEquals(tuple.get(1), 1L);
      }
      else if (tuple.get(0).equals("project2"))
      {
         Assert.assertEquals(tuple.get(1), 1L);

         tuple = bagIter.next();
         Assert.assertEquals(tuple.get(0), "project2");
         Assert.assertEquals(tuple.get(1), 1L);
      }
      else
      {
         Assert.fail("Wrong value");
      }
   }

   /**
    * Test 'unknown' event occurrence. Checks if tuple equals NULL.
    */
   @Test
   public void testUnknownEventOccurrence() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws", "ses1", "project1").build());

      File log = LogGenerator.generateLog(events);

      Iterator<Tuple> iter =
         runPigScriptAndGetResult("event.pig", log, new String[][]{{PigConstants.EVENT_PARAM,
            "unknown-id-event"}});

      Assert.assertNull(iter.next());
   }
}
