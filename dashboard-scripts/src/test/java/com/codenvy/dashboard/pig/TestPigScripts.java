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
package com.codenvy.dashboard.pig;

import com.codenvy.dashboard.pig.PigConstants;

import com.codenvy.dashboard.pig.util.Event;
import com.codenvy.dashboard.pig.util.LogGenerator;



import org.apache.pig.ExecType;
import org.apache.pig.PigServer;
import org.apache.pig.data.Tuple;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a>
 */
public class TestPigScripts extends BaseTest
{
   protected PigServer pigServer;

   @BeforeMethod
   public void setUp() throws Exception
   {
      Properties props = new Properties();
      pigServer = new PigServer(ExecType.LOCAL, props);
   }
   
   @AfterMethod
   public void tearDown() throws Exception
   {
      pigServer.shutdown();
   }

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

      Iterator<Tuple> iter = runPigScript("created-ws-but-project.pig", log, new String[][]{});

      Tuple tuple = iter.next();

      Assert.assertEquals(tuple.get(1), "user3");
      Assert.assertEquals(tuple.get(2), "ws2");
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
         runPigScript("created-ws-but-project.pig", log, new String[][]{{PigConstants.FROM_PARAM, "20101001"},
            {PigConstants.TO_PARAM, "20101002"}});

      Tuple tuple = iter.next();

      Assert.assertEquals(tuple.get(1), "user2");
      Assert.assertEquals(tuple.get(2), "ws2");
      Assert.assertNull(iter.next());
   }

   /**
    * Check if script return correct amount of events between two dates. 
    */
   @Test
   public void testReturnAmountEventBetweenDates() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createTenantCreatedEvent("user1").withDate("2010-10-01").build());
      events.add(Event.Builder.createTenantCreatedEvent("user2").withDate("2010-10-02").build());
      events.add(Event.Builder.createTenantCreatedEvent("user3").withDate("2010-10-03").build());

      File log = LogGenerator.generateLog(events);

      Iterator<Tuple> iter =
         runPigScript("event-occurrence.pig", log, new String[][]{{PigConstants.EVENT_PARAM, "tenant-created"},
            {PigConstants.FROM_PARAM, "20101001"}, {PigConstants.TO_PARAM, "20101002"}});

      Tuple tuple = iter.next();

      Assert.assertEquals(tuple.get(0), "tenant-created");
      Assert.assertEquals(tuple.get(1), 2L);
   }

   /**
    * Test 'tenant-created' event occurrence. 
    */
   @Test
   public void testTenantCreatedEventOccurrence() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createTenantCreatedEvent("user1").build());
      events.add(Event.Builder.createTenantCreatedEvent("user1").build());
      events.add(Event.Builder.createTenantCreatedEvent("user2").build());

      File log = LogGenerator.generateLog(events);
      
      Iterator<Tuple> iter =
         runPigScript("event-occurrence.pig", log, new String[][]{{PigConstants.EVENT_PARAM, "tenant-created"}});

      Tuple tuple = iter.next();

      Assert.assertEquals(tuple.get(0), "tenant-created");
      Assert.assertEquals(tuple.get(1), 2L);
   }

   /**
    * Test 'tenant-destroyed' event occurrence. 
    */
   @Test
   public void testTenantDesctroyedEventOccurrence() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createTenantDestroyedEvent("user1").build());
      events.add(Event.Builder.createTenantDestroyedEvent("user1").build());
      events.add(Event.Builder.createTenantDestroyedEvent("user2").build());

      File log = LogGenerator.generateLog(events);

      Iterator<Tuple> iter =
         runPigScript("event-occurrence.pig", log, new String[][]{{PigConstants.EVENT_PARAM, "tenant-destroyed"}});

      Tuple tuple = iter.next();

      Assert.assertEquals(tuple.get(0), "tenant-destroyed");
      Assert.assertEquals(tuple.get(1), 2L);
   }

   /**
    * Test 'project-created' event occurrence. 
    */
   @Test
   public void testProjectCreatedEventOccurrence() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws", "ses1", "project1").build());
      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws", "ses1", "project1").build());
      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws", "ses1", "project2").build());
      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws1", "ses2", "project2").build());
      events.add(Event.Builder.createProjectCreatedEvent("user2", "ws2", "ses3", "project2").build());

      File log = LogGenerator.generateLog(events);

      Iterator<Tuple> iter =
         runPigScript("event-occurrence.pig", log, new String[][]{{PigConstants.EVENT_PARAM, "project-created"}});

      Tuple tuple = iter.next();

      Assert.assertEquals(tuple.get(0), "project-created");
      Assert.assertEquals(tuple.get(1), 4L);
   }

   /**
    * Test 'project-destroyed' event occurrence. 
    */
   @Test
   public void testProjectDestroyedEventOccurrence() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createProjectDestroyedEvent("user1", "ws", "ses1", "project1").build());
      events.add(Event.Builder.createProjectDestroyedEvent("user1", "ws", "ses1", "project1").build());
      events.add(Event.Builder.createProjectDestroyedEvent("user1", "ws", "ses1", "project2").build());
      events.add(Event.Builder.createProjectDestroyedEvent("user1", "ws1", "ses2", "project2").build());
      events.add(Event.Builder.createProjectDestroyedEvent("user2", "ws2", "ses3", "project2").build());

      File log = LogGenerator.generateLog(events);

      Iterator<Tuple> iter =
         runPigScript("event-occurrence.pig", log, new String[][]{{PigConstants.EVENT_PARAM, "project-destroyed"}});

      Tuple tuple = iter.next();

      Assert.assertEquals(tuple.get(0), "project-destroyed");
      Assert.assertEquals(tuple.get(1), 4L);
   }

   /**
    * Test 'user-added-to-ws' event occurrence. 
    */
   @Test
   public void testUserAddedToWsEventOccurrence() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createUserAddedToWsEvent("", "ws", "ses1", "ws", "user1").build());
      events.add(Event.Builder.createUserAddedToWsEvent("", "ws", "ses1", "ws", "user1").build());
      events.add(Event.Builder.createUserAddedToWsEvent("", "ws", "ses1", "ws", "user2").build());
      events.add(Event.Builder.createUserAddedToWsEvent("", "", "", "ws2", "user2").build());

      File log = LogGenerator.generateLog(events);

      Iterator<Tuple> iter =
         runPigScript("event-occurrence.pig", log, new String[][]{{PigConstants.EVENT_PARAM, "user-added-to-ws"}});

      Tuple tuple = iter.next();

      Assert.assertEquals(tuple.get(0), "user-added-to-ws");
      Assert.assertEquals(tuple.get(1), 3L);
   }

   /**
    * Test 'uknown' event occurrence. Checks if tuple equals NULL.
    */
   @Test
   public void testUnkownEventOccurrence() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createProjectCreatedEvent("user1", "ws", "ses1", "project1").build());

      File log = LogGenerator.generateLog(events);

      Iterator<Tuple> iter =
         runPigScript("event-occurrence.pig", log, new String[][]{{PigConstants.EVENT_PARAM, "unknown-id-event"}});

      Assert.assertNull(iter.next());
   }

   /**
    * Run pig script with parameters.
    */
   private Iterator<Tuple> runPigScript(String script, File log, String[][] data) throws IOException
   {
      InputStream scriptContent = Thread.currentThread().getContextClassLoader().getResourceAsStream(script);
      scriptContent = replaceImportCommand(scriptContent);

      Map<String, String> params = new HashMap<String, String>(data.length);
      params.put(PigConstants.LOG_PARAM, log.getAbsolutePath());

      for (String[] str : data)
      {
         params.put(str[0], str[1]);
      }

      pigServer.registerScript(scriptContent, params);
      return pigServer.openIterator(PigConstants.FINAL_RELATION);
   }

   private InputStream replaceImportCommand(InputStream scriptContent) throws IOException, UnsupportedEncodingException
   {
      URL url = Thread.currentThread().getContextClassLoader().getResource("macros.pig");
      String content = getStreamContentAsString(scriptContent);
      content = content.replace("macros.pig", url.getFile());

      return new ByteArrayInputStream(content.getBytes("UTF-8"));
   }
}
