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
import com.codenvy.dashboard.pig.store.mongodb.MongoDbRecordWriter;
import com.codenvy.dashboard.pig.store.mongodb.TupleTransformerFactory;
import com.codenvy.dashboard.pig.store.mongodb.TupleTransformerFactory.ScriptType;

import com.codenvy.dashboard.pig.scripts.BasePigTest;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a>
 */
public class TestMongoDbRecordWriter extends BasePigTest
{
   private AbstractTupleTransformer transformer;

   private TupleFactory tupleFactory;

   private MongoDbRecordWriter writer;

   @Test
   public void testWriterForSpecificEventOccurrenceTupleTransformer() throws Exception
   {
      final String collection = UUID.randomUUID().toString();
      final DBCollection dbCollection = db.getCollection(collection);

      transformer =
         (AbstractTupleTransformer)TupleTransformerFactory.createTupleTransformer(ScriptType.SPECIFIC_EVENT_OCCURRENCE);
      tupleFactory = TupleFactory.getInstance();
      writer = new MongoDbRecordWriter(SERVER_URI + "." + collection, transformer);

      Tuple tuple = tupleFactory.newTuple();
      tuple.append("tenant-created");
      tuple.append(20101010);
      tuple.append(5L);
      
      writer.write(null, tuple);
      
      DBCursor dbCursor = dbCollection.find(new BasicDBObject("_id", transformer.getId(tuple)));
      Assert.assertTrue(dbCursor.hasNext());
      
      DBObject dbObject = dbCursor.next();
      Assert.assertEquals(transformer.transform(tuple), dbObject);
      Assert.assertFalse(dbCursor.hasNext());
   }
}
