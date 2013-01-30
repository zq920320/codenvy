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
package com.codenvy.dashboard.storeload;

import com.codenvy.dashboard.storeload.PigConstants;

import junit.framework.Assert;

import org.apache.pig.ExecType;
import org.apache.pig.PigServer;
import org.apache.pig.data.Tuple;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a>
 */
public class TestPigScripts
{
   protected PigServer pigServer;

   protected URL asLog;

   protected URL sysLog;

   @BeforeTest
   public void setUp() throws Exception
   {
      pigServer = new PigServer(ExecType.LOCAL);
      asLog = TestPigScripts.class.getClassLoader().getResource("as.log");
      sysLog = TestPigScripts.class.getClassLoader().getResource("sys.log");
   }

   /**
    * Finds users who created workspace but did not create project.
    */
   @Test
   public void shouldFindUsersWhoCreatedWsButProject() throws Exception
   {
      Iterator<Tuple> iter =
         executePigScript("created-ws-but-project.pig", new String[][]{{PigConstants.AS_LOG_PARAM, asLog.getFile()},
            {PigConstants.SYS_LOG_PARAM, sysLog.getFile()}});

      Tuple tuple = iter.next();

      Assert.assertEquals(tuple.get(1), "user2@mail.com");
      Assert.assertEquals(tuple.get(2), "tenant3");
      Assert.assertNull(iter.next());
   }

   /**
    * Checks if script 'event-occurrence.pig' return correct results. If event found,
    * the resulted tuple will have next format, for instance: (tenant-created, 1)
    */
   @Test(dataProvider = "events")
   public void shouldReturnCorrectAmoutEventOccurrenceSysLog(String eventId, long count, String log) throws Exception
   {
      Iterator<Tuple> iter =
         executePigScript("event-occurrence.pig", new String[][]{{PigConstants.LOG_PARAM, log},
            {PigConstants.EVENT_PARAM, eventId}});

      Tuple tuple = iter.next();

      Assert.assertEquals(tuple.get(0), eventId);
      Assert.assertEquals(tuple.get(1), count);
   }

   /**
    * Checks if event not found in log, the resulted tuple equals to Null. 
    */
   @Test(dataProvider = "unknownEvents")
   public void shouldReturnNullIfEventNotFound(String eventId) throws Exception
   {
      Iterator<Tuple> iter =
         executePigScript("event-occurrence.pig", new String[][]{{PigConstants.LOG_PARAM, sysLog.getFile()},
            {PigConstants.EVENT_PARAM, eventId}});

      Assert.assertNull(iter.next());
   }

   /**
    * Run pig script with parameters.
    */
   private Iterator<Tuple> executePigScript(String script, String[][] data) throws IOException
   {
      InputStream in = TestPigScripts.class.getClassLoader().getResourceAsStream(script);

      Map<String, String> params = new HashMap<String, String>(data.length);
      for (String[] str : data)
      {
         params.put(str[0], str[1]);
      }

      pigServer.registerScript(in, params);
      return pigServer.openIterator(PigConstants.FINAL_RELATION);
   }

   @DataProvider(name = "events")
   public Object[][] createEventsSeq()
   {
      return new Object[][]{{"tenant-created", 3L, sysLog.getFile()}, {"tenant-destroyed", 1L, sysLog.getFile()},
         {"project-created", 2L, asLog.getFile()}};
   }

   @DataProvider(name = "unknownEvents")
   public Object[][] createUnknownEventsSeq()
   {
      return new Object[][]{{"unknown-event-id"}};
   }
}
