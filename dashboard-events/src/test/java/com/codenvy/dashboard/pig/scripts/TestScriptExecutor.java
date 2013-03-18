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
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestScriptExecutor extends BasePigTest
{
   @Test
   public void testExecuteAndReturnResult() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createTenantCreatedEvent("ws1-1", "user1").withDate("2010-10-01").build());
      File log = LogGenerator.generateLog(events);

      ScriptExecutor scriptExecutor = new ScriptExecutor(ScriptType.ALL_EVENTS);
      scriptExecutor.setParam(Constants.DATE, "20101001");
      scriptExecutor.setParam(Constants.LOG, log.getAbsolutePath());
      Tuple tuple = scriptExecutor.executeAndReturnResult();

      Assert.assertNotNull(tuple);
   }

   @Test
   public void testExecuteAndStoreActualResult() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createTenantCreatedEvent("ws1-1", "user1").withDate("2010-10-01").build());
      File log = LogGenerator.generateLog(events);

      ScriptExecutor scriptExecutor = new ScriptExecutor(ScriptType.ALL_EVENTS);
      scriptExecutor.setParam(Constants.DATE, "20101001");
      scriptExecutor.setParam(Constants.LOG, log.getAbsolutePath());

      File file = new File(BASE_DIR + "/" + ScriptType.ALL_EVENTS.toString().toLowerCase() + "/2010/10/01/value");
      file.delete();

      Assert.assertFalse(file.exists());

      scriptExecutor.executeAndStoreResult(BASE_DIR);

      Assert.assertTrue(file.exists());
   }

   @Test
   public void testExecuteAndStoreDefaultResult() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createTenantCreatedEvent("ws1-1", "user1").withDate("2010-10-01").build());
      File log = LogGenerator.generateLog(events);

      ScriptExecutor scriptExecutor = new ScriptExecutor(ScriptType.ALL_EVENTS);
      scriptExecutor.setParam(Constants.DATE, "20101002"); // there are no events for given time frame
      scriptExecutor.setParam(Constants.LOG, log.getAbsolutePath());

      File file1 = new File(BASE_DIR + "/" + ScriptType.ALL_EVENTS.toString().toLowerCase() + "/2010/10/01/value");
      File file2 = new File(BASE_DIR + "/" + ScriptType.ALL_EVENTS.toString().toLowerCase() + "/2010/10/02/value");

      file1.delete();
      file2.delete();

      Assert.assertFalse(file1.exists());
      Assert.assertFalse(file2.exists());

      scriptExecutor.executeAndStoreResult(BASE_DIR);

      Assert.assertFalse(file1.exists());
      Assert.assertTrue(file2.exists());
   }
}
