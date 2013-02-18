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

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;
import com.mongodb.WriteConcern;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.Tuple;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class MongoDbRecordWriter extends RecordWriter<WritableComparable, Tuple>
{

   /**
    * Collection to write data in.
    */
   protected DBCollection dbCollection;

   /**
    * Mongo client. Have to be closed.
    */
   protected Mongo mongo;

   /**
    * {@link TupleTransformer}.
    */
   protected TupleTransformer transformer;

   /**
    * MongoDbRecordWriter constructor.
    */
   MongoDbRecordWriter(String serverUrl, TupleTransformer transformer) throws IOException
   {
      MongoURI uri = new MongoURI(serverUrl);

      this.mongo = new Mongo(uri);
      DB db = mongo.getDB(uri.getDatabase());

      if (uri.getUsername() != null)
      {
         db.authenticate(uri.getUsername(), uri.getPassword());
      }

      db.setWriteConcern(WriteConcern.ACKNOWLEDGED);

      this.dbCollection = db.getCollection(uri.getCollection());
      this.transformer = transformer;
   }

   /**
    * {@inheritedDoc)
    */
   @Override
   public void write(WritableComparable key, Tuple value) throws IOException, InterruptedException
   {
      try
      {
         DBObject dbObject = transformer.transform(value);
         dbCollection.save(dbObject);
      }
      catch (ExecException e)
      {
         throw new IOException(e);
      }
   }

   /**
    * {@inheritedDoc)
    */
   @Override
   public void close(TaskAttemptContext context) throws IOException, InterruptedException
   {
      mongo.close();
   }
}
