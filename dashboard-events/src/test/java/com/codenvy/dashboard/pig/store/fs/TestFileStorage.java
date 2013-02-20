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
package com.codenvy.dashboard.pig.store.fs;

import com.codenvy.dashboard.pig.scripts.BasePigTest;
import com.codenvy.dashboard.pig.scripts.PigConstants;
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
public class TestFileStorage extends BasePigTest
{

   /**
    * Check <code>specific-event-occurrence.pig</code> script.
    * Runs script and check if file with results is created. Checks the content also. 
    */
   @Test
   public void testFileStorageSpecificEventOccurrencePigScript() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createTenantCreatedEvent("ws1-1", "user1").withDate("2010-10-01").build());
      events.add(Event.Builder.createTenantCreatedEvent("ws2-1", "user2").withDate("2010-10-02").build());

      File log = LogGenerator.generateLog(events);

      runPigScript("specific-event-occurrence.pig", log, new String[][]{{PigConstants.EVENT_PARAM, "tenant-created"}});

      File file = new File("target/event_occurrence/tenant/created/2010/10/01/value");
      Assert.assertTrue(file.exists());

      FileObject fileObject = new EventOccurrenceFileObject("tenant-created", 20101001);
      fileObject.load("target");

      Assert.assertEquals(fileObject.get("event"), "tenant-created");
      Assert.assertEquals(fileObject.get("date"), "20101001");
      Assert.assertEquals(fileObject.get("count"), "1");

      file = new File("target/event_occurrence/tenant/created/2010/10/02/value");
      Assert.assertTrue(file.exists());

      fileObject = new EventOccurrenceFileObject("tenant-created", 20101002);
      fileObject.load("target");

      Assert.assertEquals(fileObject.get("event"), "tenant-created");
      Assert.assertEquals(fileObject.get("date"), "20101002");
      Assert.assertEquals(fileObject.get("count"), "1");
   }
   
   /**
    * Check <code>all-event-occurrence.pig</code> script.
    * Runs script and check if file with results is created. Checks the content also. 
    */
   @Test
   public void testFileStorageAllEventOccurrencePigScript() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createTenantCreatedEvent("ws1-1", "user1").withDate("2010-10-01").build());
      events.add(Event.Builder.createTenantDestroyedEvent("ws1-1").withDate("2010-10-02").build());

      File log = LogGenerator.generateLog(events);

      runPigScript("all-event-occurrence.pig", log, new String[][]{{PigConstants.EVENT_PARAM, "tenant-created"}});

      File file = new File("target/event_occurrence/tenant/created/2010/10/01/value");
      Assert.assertTrue(file.exists());

      FileObject fileObject = new EventOccurrenceFileObject("tenant-created", 20101001);
      fileObject.load("target");

      Assert.assertEquals(fileObject.get("event"), "tenant-created");
      Assert.assertEquals(fileObject.get("date"), "20101001");
      Assert.assertEquals(fileObject.get("count"), "1");

      file = new File("target/event_occurrence/tenant/destroyed/2010/10/02/value");
      Assert.assertTrue(file.exists());

      fileObject = new EventOccurrenceFileObject("tenant-destroyed", 20101002);
      fileObject.load("target");

      Assert.assertEquals(fileObject.get("event"), "tenant-destroyed");
      Assert.assertEquals(fileObject.get("date"), "20101002");
      Assert.assertEquals(fileObject.get("count"), "1");
   }
}
