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
import org.apache.pig.data.DefaultDataBag;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestScriptAllParamsToDist2ParamEventWs extends BasePigTest
{

   @Test
   public void testEventFound() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createApplicationCreatedEvent("user1", "ws1", "session", "project1", "type1", "paas1")
         .withDate("2010-10-01").build());
      events.add(Event.Builder.createApplicationCreatedEvent("user1", "ws1", "session", "project1", "type1", "paas1")
         .withDate("2010-10-01").build());
      events.add(Event.Builder.createApplicationCreatedEvent("user1", "ws2", "session", "project1", "type1", "paas1")
         .withDate("2010-10-02").build());
      events.add(Event.Builder.createApplicationCreatedEvent("user1", "ws1", "session", "project2", "type1", "paas2")
         .withDate("2010-10-03").build());
      events.add(Event.Builder.createApplicationCreatedEvent("user1", "ws1", "session", "project1", "type1", "paas3")
         .withDate("2010-10-04").build());

      File log = LogGenerator.generateLog(events);

      executePigScript(ScriptType.ALL_PARAMS_TO_DIST_2PARAMS_EVENT_WS, log, new String[][]{
         {Constants.EVENT, "application-created"}, {Constants.PARAM_NAME, "PROJECT"},
         {Constants.SECOND_PARAM_NAME, "PAAS"}, {Constants.DATE, "20101001"}, {Constants.TO_DATE, "20101003"}});

      FileObject fileObject =
         ScriptType.ALL_PARAMS_TO_DIST_2PARAMS_EVENT_WS.createFileObject(BASE_DIR, "application-created", "PROJECT", "PAAS", 20101001,
            20101003);

      Properties props = (Properties)fileObject.getValue();
      Assert.assertEquals(props.getProperty("paas1"), "2");
      Assert.assertEquals(props.getProperty("paas2"), "1");
      Assert.assertNull(props.getProperty("paas3"));
   }

   @Test
   public void testEventNotFoundStoredDefaultValue() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createApplicationCreatedEvent("user1", "ws1", "session", "project1", "type1", "paas1")
         .withDate("2010-10-01").build());


      File log = LogGenerator.generateLog(events);

      executePigScript(ScriptType.ALL_PARAMS_TO_DIST_2PARAMS_EVENT_WS, log, new String[][]{
         {Constants.EVENT, "application-created"}, {Constants.PARAM_NAME, "PROJECT"},
         {Constants.SECOND_PARAM_NAME, "PAAS"}, {Constants.DATE, "20101003"}, {Constants.TO_DATE, "20101003"}});

      FileObject fileObject =
         ScriptType.ALL_PARAMS_TO_DIST_2PARAMS_EVENT_WS.createFileObject(BASE_DIR, "application-created", "PROJECT", "PAAS", 20101003,
            20101003);

      Properties props = (Properties)fileObject.getValue();
      Assert.assertTrue(props.isEmpty());
   }

   @Test
   public void fileObjectShouldReturnCorrectProperties() throws Exception
   {
      Tuple tuple = TupleFactory.getInstance().newTuple();
      tuple.append("application-created");
      tuple.append("PROJECT");
      tuple.append("PAAS");
      tuple.append(20121103);
      tuple.append(20121105);
      
      Tuple innerTuple = TupleFactory.getInstance().newTuple();
      innerTuple.append("GAE");
      innerTuple.append(2L);
      DataBag bag = new DefaultDataBag();
      bag.add(innerTuple);
      
      tuple.append(bag);

      FileObject fileObject = ScriptType.ALL_PARAMS_TO_DIST_2PARAMS_EVENT_WS.createFileObject(BASE_DIR, tuple);

      Assert.assertNotNull(fileObject.getKeys().get(Constants.EVENT));
      Assert.assertNotNull(fileObject.getKeys().get(Constants.PARAM_NAME));
      Assert.assertNotNull(fileObject.getKeys().get(Constants.SECOND_PARAM_NAME));
      Assert.assertNotNull(fileObject.getKeys().get(Constants.DATE));
      Assert.assertNotNull(fileObject.getKeys().get(Constants.TO_DATE));

      Iterator<String> iter = fileObject.getKeys().keySet().iterator();
      Assert.assertEquals(iter.next(), Constants.EVENT);
      Assert.assertEquals(iter.next(), Constants.PARAM_NAME);
      Assert.assertEquals(iter.next(), Constants.SECOND_PARAM_NAME);
      Assert.assertEquals(iter.next(), Constants.DATE);
      Assert.assertEquals(iter.next(), Constants.TO_DATE);

      Assert.assertEquals(fileObject.getTypeResult(), ScriptTypeResult.EVENT_2PARAM_TIMEFRAME_FOR_BAG);
      Assert.assertEquals(fileObject.getKeys().get(Constants.EVENT), "application-created");
      Assert.assertEquals(fileObject.getKeys().get(Constants.PARAM_NAME), "PROJECT");
      Assert.assertEquals(fileObject.getKeys().get(Constants.SECOND_PARAM_NAME), "PAAS");
      Assert.assertEquals(fileObject.getKeys().get(Constants.DATE), "20121103");
      Assert.assertEquals(fileObject.getKeys().get(Constants.TO_DATE), "20121105");
      Assert.assertEquals(((Properties)fileObject.getValue()).getProperty("GAE"), "2");

      File file =
         new File(BASE_DIR + "/" + ScriptType.ALL_PARAMS_TO_DIST_2PARAMS_EVENT_WS.toString().toLowerCase()
            + "/application/created/project/paas/2012/11/03/20121105/value");
      file.delete();

      Assert.assertFalse(file.exists());

      fileObject.store();

      Assert.assertTrue(file.exists());
   }
}
