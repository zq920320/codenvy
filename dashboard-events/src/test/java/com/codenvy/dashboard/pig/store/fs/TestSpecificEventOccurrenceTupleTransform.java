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

import com.codenvy.dashboard.pig.store.fs.FileObjectFactory.ScriptResultType;

import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a>
 */
public class TestSpecificEventOccurrenceTupleTransform
{
   private TupleFactory tupleFactory;

   private final String event = "tenant-created";

   private final Integer date = 20101010;

   private final Long count = 5L;

   private Tuple tuple;
   
   private FileObject props;

   @BeforeMethod
   public void prepare() throws Exception
   {
      tupleFactory = TupleFactory.getInstance();

      tuple = tupleFactory.newTuple();
      tuple.append(date);
      tuple.append(event);
      tuple.append(count);

      props = EventOccurrenceFileObject.valueOf(tuple);
   }

   @Test
   public void transformShouldReturnCorrectProperties() throws Exception
   {
      Assert.assertNotNull(props.get("event"));
      Assert.assertNotNull(props.get("date"));
      Assert.assertNotNull(props.get("count"));

      Assert.assertEquals(props.getId(), "tenant/created/2010/10/10");
      Assert.assertEquals(props.getType(), ScriptResultType.EVENT_OCCURRENCE);
      Assert.assertEquals(props.get("event"), event);
      Assert.assertEquals(props.get("date"), date.toString());
      Assert.assertEquals(props.get("count"), count.toString());
   }

   @Test
   public void idShouldChangedWithEvent() throws Exception
   {
      final String oldId = props.getId();
      final String newEvent = "another-event";
      tuple.set(1, newEvent);

      FileObject props = EventOccurrenceFileObject.valueOf(tuple);

      Assert.assertNotEquals(props.getId(), oldId);
      Assert.assertEquals(props.get("event"), newEvent);
   }
   
   @Test
   public void idShouldChangedWithDate() throws Exception
   {
      final String oldId = props.getId();
      final Integer newDate = 20201010;
      tuple.set(0, newDate);

      FileObject props = EventOccurrenceFileObject.valueOf(tuple);

      Assert.assertNotEquals(props.getId(), oldId);
      Assert.assertEquals(props.get("date"), newDate.toString());
   }

   @Test
   public void idShouldNotChangedWithCount() throws Exception
   {
      final String oldId = props.getId();
      final Long newCount = 20L;
      tuple.set(2, newCount);

      FileObject props = EventOccurrenceFileObject.valueOf(tuple);

      Assert.assertEquals(props.getId(), oldId);
      Assert.assertEquals(props.get("count"), newCount.toString());
   }
}
