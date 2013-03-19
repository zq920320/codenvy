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

import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestScriptProductUsageTime extends BasePigTest
{

   @Test
   public void testEventFound() throws Exception
   {
      List<Event> events = new ArrayList<Event>();

      // 7 min
      events.add(Event.Builder.createUserSSOLoggedInEvent("user1", "google").withDate("2010-10-01")
         .withTime("20:00:00").build());
      events.add(Event.Builder.createUserSSOLoggedInEvent("user1", "google").withDate("2010-10-01")
         .withTime("20:05:00").build());
      events.add(Event.Builder.createUserSSOLoggedInEvent("user1", "google").withDate("2010-10-01")
         .withTime("20:07:00").build());

      // 4 min
      events.add(Event.Builder.createUserSSOLoggedInEvent("user1", "google").withDate("2010-10-01")
         .withTime("20:25:00").build());
      events.add(Event.Builder.createUserSSOLoggedInEvent("user1", "google").withDate("2010-10-01")
         .withTime("20:29:00").build());

      File log = LogGenerator.generateLog(events);

      executePigScript(ScriptType.PRODUCT_USAGE_TIME, log, new String[][]{{Constants.DATE, "20101001"},
         {Constants.TO_DATE, "20101003"}, {Constants.INACTIVE_INTERVAL, "600"}});

      FileObject fileObject = ScriptType.PRODUCT_USAGE_TIME.createFileObject(BASE_DIR, 20101001, 20101003, 600);
      Assert.assertEquals(fileObject.getValue(), 11L);
   }

   @Test
   public void testEventNotFoundStoredDefaultValue() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      File log = LogGenerator.generateLog(events);

      executePigScript(ScriptType.PRODUCT_USAGE_TIME, log, new String[][]{{Constants.DATE, "20101001"},
         {Constants.TO_DATE, "20101003"}, {Constants.INACTIVE_INTERVAL, "600"}});

      FileObject fileObject = ScriptType.PRODUCT_USAGE_TIME.createFileObject(BASE_DIR, 20101001, 20101003, 600);
      Assert.assertEquals(fileObject.getValue(), 0L);
   }

   @Test
   public void fileObjectShouldReturnCorrectProperties() throws Exception
   {
      Tuple tuple = TupleFactory.getInstance().newTuple();
      tuple.append(20121103);
      tuple.append(20121105);
      tuple.append(600);
      tuple.append(300L);

      FileObject fileObject = ScriptType.PRODUCT_USAGE_TIME.createFileObject(BASE_DIR, tuple);

      Assert.assertNotNull(fileObject.getKeys().get(Constants.DATE));
      Assert.assertNotNull(fileObject.getKeys().get(Constants.TO_DATE));
      Assert.assertNotNull(fileObject.getKeys().get(Constants.INACTIVE_INTERVAL));

      Iterator<String> iter = fileObject.getKeys().keySet().iterator();
      Assert.assertEquals(iter.next(), Constants.DATE);
      Assert.assertEquals(iter.next(), Constants.TO_DATE);
      Assert.assertEquals(iter.next(), Constants.INACTIVE_INTERVAL);

      Assert.assertEquals(fileObject.getTypeResult(), ScriptTypeResult.TIMEFRAME_INTERVAL_FOR_LONG);
      Assert.assertEquals(fileObject.getKeys().get(Constants.DATE), "20121103");
      Assert.assertEquals(fileObject.getKeys().get(Constants.TO_DATE), "20121105");
      Assert.assertEquals(fileObject.getKeys().get(Constants.INACTIVE_INTERVAL), "600");
      Assert.assertEquals(fileObject.getValue(), 300L);

      File file =
         new File(BASE_DIR + "/" + ScriptType.PRODUCT_USAGE_TIME.toString().toLowerCase()
            + "/2012/11/03/20121105/600/value");
      file.delete();

      Assert.assertFalse(file.exists());

      fileObject.store();

      Assert.assertTrue(file.exists());
   }
}
