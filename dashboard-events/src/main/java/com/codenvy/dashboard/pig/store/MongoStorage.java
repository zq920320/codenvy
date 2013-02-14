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

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.pig.StoreFunc;
import org.apache.pig.data.Tuple;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a>
 */
public class MongoStorage extends StoreFunc
{

   private RecordWriter writer;

   @Override
   public OutputFormat getOutputFormat() throws IOException
   {
      return new TableOutputFormat();
   }

   @Override
   public void setStoreLocation(String location, Job job) throws IOException
   {
      FileOutputFormat.setOutputPath(job, new Path(location));
   }

   @Override
   public void prepareToWrite(RecordWriter writer) throws IOException
   {
      this.writer = writer;
   }

   @Override
   public void putNext(Tuple t) throws IOException
   {
      try
      {
         writer.write(null, t);
      }
      catch (InterruptedException e)
      {
         throw new IOException(e);
      }
   }
}
