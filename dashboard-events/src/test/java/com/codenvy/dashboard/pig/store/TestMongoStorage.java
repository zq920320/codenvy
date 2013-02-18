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
package com.codenvy.dashboard.pig.store;

import com.codenvy.dashboard.pig.scripts.BasePigTest;
import com.codenvy.dashboard.pig.scripts.PigConstants;
import com.codenvy.dashboard.pig.scripts.util.Event;
import com.codenvy.dashboard.pig.scripts.util.LogGenerator;
import com.codenvy.dashboard.pig.store.TupleTransformerFactory.ScriptType;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a>
 */
public class TestMongoStorage extends BasePigTest
{
   @Test
   public void testStoreDataIntoDbSpecificEventOccurrenceUsecase() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createTenantCreatedEvent("ws1-1", "user1").withDate("2010-10-01").build());

      File log = LogGenerator.generateLog(events);

      String collection = UUID.randomUUID().toString();

      runPigScript("specific-event-occurrence.pig", log, new String[][]{{PigConstants.EVENT_PARAM, "tenant-created"},
         {PigConstants.STORE_INTO_PARAM, SERVER_URI + "." + collection}});

      DBCollection dbCollection = db.getCollectionFromString(collection);
      DBCursor dbCursor = dbCollection.find();

      Assert.assertTrue(dbCursor.hasNext());

      DBObject dbObject = dbCursor.next();
      Assert.assertEquals(dbObject.get("event"), "tenant-created");
      Assert.assertEquals(dbObject.get("type"), ScriptType.SPECIFIC_EVENT_OCCURRENCE.toString());
      Assert.assertEquals(dbObject.get("date"), 20101001);
      Assert.assertEquals(dbObject.get("count"), 1L);

      Assert.assertFalse(dbCursor.hasNext());
   }
}
