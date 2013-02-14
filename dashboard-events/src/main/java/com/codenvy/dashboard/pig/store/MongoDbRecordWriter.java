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

import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.pig.data.Tuple;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class MongoDbRecordWriter extends RecordWriter<WritableComparable, Tuple>
{

   protected DBCollection dbCollection;

   protected TupleTransformer transformer;

   /**
    * MongoDbRecordWriter constructor.
    */
   MongoDbRecordWriter(DBCollection dbCollection, TupleTransformer transformer)
   {
      this.dbCollection = dbCollection;
      this.transformer = transformer;
   }

   /**
    * {@inheritedDoc)
    */
   @Override
   public void write(WritableComparable key, Tuple value) throws IOException, InterruptedException
   {
      DBObject dbObject = transformer.transform(value);
      // TODO
   }

   /**
    * {@inheritedDoc)
    */
   @Override
   public void close(TaskAttemptContext context) throws IOException, InterruptedException
   {
      // do nothing here
   }
}
