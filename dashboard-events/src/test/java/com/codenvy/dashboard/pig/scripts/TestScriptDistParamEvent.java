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
public class TestScriptDistParamEvent extends BasePigTest
{

   @Test
   public void testEventFound() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "session", "project1", "type")
         .withDate("2010-10-01").build());
      events.add(Event.Builder.createProjectBuiltEvent("user1", "ws2", "session", "project1", "type")
         .withDate("2010-10-01").build());
      events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "session", "project1", "type")
         .withDate("2010-10-01").build());
      events.add(Event.Builder.createProjectBuiltEvent("user1", "ws1", "session", "project2", "type")
         .withDate("2010-10-01").build());

      File log = LogGenerator.generateLog(events);

      executePigScript(ScriptType.DIST_PARAM_EVENT, log, new String[][]{{Constants.EVENT, "project-built"},
         {Constants.PARAM_NAME, "PROJECT"}, {Constants.DATE, "20101001"}, {Constants.TO_DATE, "20101003"}});

      FileObject fileObject =
         ScriptType.DIST_PARAM_EVENT.createFileObject(BASE_DIR, "project-built", "PROJECT", 20101001,
            20101003);

      Assert.assertEquals(fileObject.getValue(), 3L);
   }

   @Test
   public void fileObjectShouldReturnCorrectProperties() throws Exception
   {
      Tuple tuple = TupleFactory.getInstance().newTuple();
      tuple.append("project-built");
      tuple.append("PROJECT");
      tuple.append(20121103);
      tuple.append(20121105);
      tuple.append(1L);

      FileObject fileObject = ScriptType.DIST_PARAM_EVENT.createFileObject(BASE_DIR, tuple);

      Assert.assertNotNull(fileObject.getKeys().get(Constants.EVENT));
      Assert.assertNotNull(fileObject.getKeys().get(Constants.PARAM_NAME));
      Assert.assertNotNull(fileObject.getKeys().get(Constants.DATE));
      Assert.assertNotNull(fileObject.getKeys().get(Constants.TO_DATE));

      Iterator<String> iter = fileObject.getKeys().keySet().iterator();
      Assert.assertEquals(iter.next(), Constants.EVENT);
      Assert.assertEquals(iter.next(), Constants.PARAM_NAME);
      Assert.assertEquals(iter.next(), Constants.DATE);
      Assert.assertEquals(iter.next(), Constants.TO_DATE);

      Assert.assertEquals(fileObject.getTypeResult(), ScriptTypeResult.EVENT_PARAM_TIMEFRAME_FOR_LONG);
      Assert.assertEquals(fileObject.getKeys().get(Constants.EVENT), "project-built");
      Assert.assertEquals(fileObject.getKeys().get(Constants.PARAM_NAME), "PROJECT");
      Assert.assertEquals(fileObject.getKeys().get(Constants.DATE), "20121103");
      Assert.assertEquals(fileObject.getKeys().get(Constants.TO_DATE), "20121105");
      Assert.assertEquals(fileObject.getValue(), 1L);

      File file =
         new File(BASE_DIR + "/" + ScriptType.DIST_PARAM_EVENT.toString().toLowerCase()
            + "/project/built/project/2012/11/03/20121105/value");
      file.delete();

      Assert.assertFalse(file.exists());

      fileObject.store();

      Assert.assertTrue(file.exists());
   }
}
