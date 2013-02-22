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

import com.codenvy.dashboard.pig.store.fs.FileObject.ScriptResultType;

import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Iterator;

/**
 * @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a>
 */
public class TestEventFileObject
{
   private TupleFactory tupleFactory;

   private final String event = "tenant-created";

   private final Integer date = 20111010;

   private final Long count = 5L;

   private Tuple tuple;
   
   private FileObject fileObject;

   @BeforeMethod
   public void prepare() throws Exception
   {
      tupleFactory = TupleFactory.getInstance();

      tuple = tupleFactory.newTuple();
      tuple.append(event);
      tuple.append(date);
      tuple.append(count);

      fileObject = new EventFileObject("target", tuple);
   }

   @Test
   public void fileObjectShouldReturnCorrectProperties() throws Exception
   {
      Assert.assertNotNull(fileObject.getKeys().get("event"));
      Assert.assertNotNull(fileObject.getKeys().get("date"));

      Iterator<String> iter = fileObject.getKeys().keySet().iterator();
      Assert.assertEquals(iter.next(), "event");
      Assert.assertEquals(iter.next(), "date");

      Assert.assertEquals(fileObject.getType(), ScriptResultType.EVENT);
      Assert.assertEquals(fileObject.getKeys().get("event"), event);
      Assert.assertEquals(fileObject.getKeys().get("date"), date.toString());
      Assert.assertEquals(fileObject.getValue().toString(), count.toString());
      
      Assert.assertFalse(new File("target/event/tenant/created/2011/10/10/value").exists());

      fileObject.store();
      Assert.assertTrue(new File("target/event/tenant/created/2011/10/10/value").exists());
   }

   @Test
   public void fileShouldChangedWithEvent() throws Exception
   {
      final String newEvent = "another-event";
      tuple.set(0, newEvent);

      Assert.assertFalse(new File("target/event/another/event/2011/10/10/value").exists());

      FileObject fileObject = new EventFileObject("target", tuple);
      fileObject.store();

      Assert.assertTrue(new File("target/event/another/event/2011/10/10/value").exists());
   }
   
   @Test
   public void fileShouldChangedWithDate() throws Exception
   {
      final Integer newDate = 20201010;
      tuple.set(1, newDate);

      Assert.assertFalse(new File("target/event/tenant/created/2020/10/10/value").exists());

      FileObject fileObject = new EventFileObject("target", tuple);
      fileObject.store();

      Assert.assertTrue(new File("target/event/tenant/created/2020/10/10/value").exists());
   }

   @Test
   public void idShouldNotChangedWithCount() throws Exception
   {
      fileObject.store();
      Assert.assertTrue(new File("target/event/tenant/created/2011/10/10/value").exists());

      final Long newCount = 20L;
      tuple.set(2, newCount);

      FileObject fileObject = new EventFileObject("target", tuple);
      fileObject.store();

      Assert.assertTrue(new File("target/event/tenant/created/2011/10/10/value").exists());

      fileObject = new EventFileObject("target", "tenant-created", 20111010);
      Assert.assertEquals(fileObject.getValue().toString(), "20");
   }
}
