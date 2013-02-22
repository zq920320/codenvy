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
import java.util.Properties;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestFileStorage extends BasePigTest
{

   /**
    * Runs script and check if file with results is created. Checks the content also. 
    */
   @Test
   public void testEventPigScript() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createTenantCreatedEvent("ws1-1", "user1").withDate("2010-10-01").build());
      events.add(Event.Builder.createTenantCreatedEvent("ws2-1", "user2").withDate("2010-10-02").build());

      File log = LogGenerator.generateLog(events);

      File file = new File("target/event/tenant/created/2010/10/01/value");
      file.delete();

      Assert.assertFalse(file.exists());

      runPigScript("event.pig", log, new String[][]{{PigConstants.EVENT_PARAM, "tenant-created"}});

      Assert.assertTrue(file.exists());

      FileObject fileObject = new EventFileObject("target", "tenant-created", 20101001);
      Assert.assertEquals(fileObject.getValue(), "1");

      file = new File("target/event/tenant/created/2010/10/02/value");
      Assert.assertTrue(file.exists());

      fileObject = new EventFileObject("target", "tenant-created", 20101002);
      Assert.assertEquals(fileObject.getValue(), "1");
   }
   
   /**
    * Runs script and check if file with results is created. Checks the content also. 
    */
   @Test
   public void testEventAllPigScript() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createTenantCreatedEvent("ws1-1", "user1").withDate("2010-10-01").build());
      events.add(Event.Builder.createTenantDestroyedEvent("ws1-1").withDate("2010-10-02").build());

      File log = LogGenerator.generateLog(events);

      File file = new File("target/event/tenant/created/2010/10/01/value");
      file.delete();

      Assert.assertFalse(file.exists());

      runPigScript("event-all.pig", log, new String[][]{{PigConstants.EVENT_PARAM, "tenant-created"}});

      Assert.assertTrue(file.exists());

      FileObject fileObject = new EventFileObject("target", "tenant-created", 20101001);
      Assert.assertEquals(fileObject.getValue(), "1");

      file = new File("target/event/tenant/destroyed/2010/10/02/value");
      Assert.assertTrue(file.exists());

      fileObject = new EventFileObject("target", "tenant-destroyed", 20101002);
      Assert.assertEquals(fileObject.getValue(), "1");
   }

   /**
    * Runs script and check if file with results is created. Checks the content also. 
    */
   @Test
   public void testEventParamPigScript() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createTenantCreatedEvent("ws1-1", "user1").withDate("2010-10-01").build());
      events.add(Event.Builder.createTenantCreatedEvent("ws1-2", "user1").withDate("2010-10-01").build());

      File log = LogGenerator.generateLog(events);

      File file = new File("target/event_param/tenant/created/user/2010/10/01/value");
      file.delete();

      Assert.assertFalse(file.exists());

      runPigScript("event-param.pig", log, new String[][]{{PigConstants.EVENT_PARAM, "tenant-created"},
         {PigConstants.PARAM_NAME_PARAM, "USER"}});

      Assert.assertTrue(file.exists());

      FileObject fileObject = new EventParamFileObject(BASE_DIR, "tenant-created", "USER", 20101001);
      Assert.assertTrue(fileObject.getValue() instanceof Properties);
      Assert.assertEquals(((Properties)fileObject.getValue()).getProperty("user1"), "2");
   }
}
