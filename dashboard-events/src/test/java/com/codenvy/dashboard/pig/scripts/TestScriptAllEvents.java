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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestScriptAllEvents extends BasePigTest
{

   private TupleFactory tupleFactory;

   private final Integer date = 20111010;

   private Tuple tuple;

   private FileObject fileObject;

   @BeforeMethod
   public void prepare() throws Exception
   {
      tupleFactory = TupleFactory.getInstance();

      tuple = tupleFactory.newTuple();
      tuple.append(date);

      Tuple innerTuple = tupleFactory.newTuple();
      innerTuple.append("tenant-created");
      innerTuple.append(1L);

      DataBag bag = new DefaultDataBag();
      bag.add(innerTuple);

      tuple.append(bag);

      fileObject = ScriptType.ALL_EVENTS.createFileObject(BASE_DIR, tuple);
   }

   /**
    * Run script which find all events.  
    */
   @Test
   public void testEventFound() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1").withDate("2010-10-01").build());
      events.add(Event.Builder.createTenantCreatedEvent("ws2", "user2").withDate("2010-10-01").build());
      events.add(Event.Builder.createTenantDestroyedEvent("ws3").withDate("2010-10-01").build());

      File log = LogGenerator.generateLog(events);

      executePigScript(ScriptType.ALL_EVENTS, log, new String[][]{{Constants.DATE, "20101001"}});

      FileObject fileObject = ScriptType.ALL_EVENTS.createFileObject(BASE_DIR, 20101001);

      Properties props = (Properties)fileObject.getValue();
      Assert.assertEquals(props.getProperty("tenant-created"), "2");
      Assert.assertEquals(props.getProperty("tenant-destroyed"), "1");
   }

   @Test
   public void testEventNotFoundStoredDefaultValue() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1").withDate("2010-10-01").build());
      events.add(Event.Builder.createTenantCreatedEvent("ws2", "user2").withDate("2010-10-01").build());
      events.add(Event.Builder.createTenantDestroyedEvent("ws3").withDate("2010-10-01").build());

      File log = LogGenerator.generateLog(events);

      executePigScript(ScriptType.ALL_EVENTS, log, new String[][]{{Constants.DATE, "20101002"}});

      FileObject fileObject = ScriptType.ALL_EVENTS.createFileObject(BASE_DIR, 20101002);
      Properties props = (Properties)fileObject.getValue();
      Assert.assertTrue(props.isEmpty());
   }

   @Test
   public void fileObjectShouldReturnCorrectProperties() throws Exception
   {
      Assert.assertNotNull(fileObject.getKeys().get(Constants.DATE));

      Iterator<String> iter = fileObject.getKeys().keySet().iterator();
      Assert.assertEquals(iter.next(), Constants.DATE);

      Assert.assertEquals(fileObject.getTypeResult(), ScriptTypeResult.DATE_FOR_PROPERTIES);
      Assert.assertEquals(fileObject.getKeys().get(Constants.DATE), date.toString());
      Assert.assertEquals(((Properties)fileObject.getValue()).getProperty("tenant-created"), "1");

      File file = new File(BASE_DIR + "/" + ScriptType.ALL_EVENTS.toString().toLowerCase() + "/2011/10/10/value");
      file.delete();

      Assert.assertFalse(file.exists());

      fileObject.store();

      Assert.assertTrue(file.exists());
   }
}
