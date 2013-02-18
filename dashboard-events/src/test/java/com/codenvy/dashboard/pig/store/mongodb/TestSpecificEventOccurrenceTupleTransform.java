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
package com.codenvy.dashboard.pig.store.mongodb;

import com.codenvy.dashboard.pig.store.mongodb.AbstractTupleTransformer;
import com.codenvy.dashboard.pig.store.mongodb.TupleTransformerFactory;
import com.codenvy.dashboard.pig.store.mongodb.TupleTransformerFactory.ScriptType;

import com.codenvy.dashboard.pig.scripts.BasePigTest;
import com.mongodb.DBObject;

import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a>
 */
public class TestSpecificEventOccurrenceTupleTransform extends BasePigTest
{
   private TupleFactory tupleFactory;

   private AbstractTupleTransformer transformer;

   private final String event = "tenant-created";

   private final int date = 20101010;

   private final long count = 5L;

   private Tuple tuple;
   
   @BeforeMethod
   public void prepare() throws Exception
   {
      tupleFactory = TupleFactory.getInstance();
      transformer =
         (AbstractTupleTransformer)TupleTransformerFactory.createTupleTransformer(ScriptType.SPECIFIC_EVENT_OCCURRENCE);

      tuple = tupleFactory.newTuple();
      tuple.append(event);
      tuple.append(date);
      tuple.append(count);
   }

   @Test
   public void transformShouldReturnCorrectDbObject() throws Exception
   {
      DBObject dbObject = transformer.transform(tuple);

      Assert.assertNotNull(dbObject.get("_id"));
      Assert.assertNotNull(dbObject.get("type"));
      Assert.assertNotNull(dbObject.get("event"));
      Assert.assertNotNull(dbObject.get("date"));
      Assert.assertNotNull(dbObject.get("count"));

      Assert.assertEquals(dbObject.get("_id"), transformer.getId(tuple));
      Assert.assertEquals(dbObject.get("type"), ScriptType.SPECIFIC_EVENT_OCCURRENCE.toString());
      Assert.assertEquals(dbObject.get("event"), event);
      Assert.assertEquals(dbObject.get("date"), date);
      Assert.assertEquals(dbObject.get("count"), count);
   }

   @Test
   public void idShouldChangedWithEvent() throws Exception
   {
      final long oldId = transformer.getId(tuple);
      final String newEvent = "another-event";
      tuple.set(0, newEvent);

      DBObject dbObject = transformer.transform(tuple);

      Assert.assertFalse(transformer.getId(tuple) == oldId);
      Assert.assertEquals(dbObject.get("_id"), transformer.getId(tuple));
      Assert.assertEquals(dbObject.get("event"), newEvent);
   }
   
   @Test
   public void idShouldChangedWithDate() throws Exception
   {
      final long oldId = transformer.getId(tuple);
      final int newDate = 20201010;
      tuple.set(1, newDate);

      DBObject dbObject = transformer.transform(tuple);

      Assert.assertFalse(transformer.getId(tuple) == oldId);
      Assert.assertEquals(dbObject.get("_id"), transformer.getId(tuple));
      Assert.assertEquals(dbObject.get("date"), newDate);
   }

   @Test
   public void idShouldNotChangedWithCount() throws Exception
   {
      final long oldId = transformer.getId(tuple);
      final long newCount = 20L;
      tuple.set(2, newCount);

      DBObject dbObject = transformer.transform(tuple);

      Assert.assertTrue(transformer.getId(tuple) == oldId);
      Assert.assertEquals(dbObject.get("_id"), transformer.getId(tuple));
      Assert.assertEquals(dbObject.get("count"), newCount);
   }
}
