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

import com.codenvy.dashboard.pig.store.TupleTransformerFactory.ScriptType;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.pig.StoreFunc;
import org.apache.pig.data.Tuple;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a>
 */
public class MongoStorage extends StoreFunc
{

   /**
    * Job parameter where destination server url is stored.
    * Serve url parameter has the next format: user:password@server:port
    */
   public static final String SERVER_URL_PARAM = "server.url";

   /**
    * Job parameter where script type value is stored. Every Pig-latin
    * script has its own resulted format to be stored. So, it is needed 
    * to use special {@link TupleTransformer} then.
    */
   public static final String SCRIPT_TYPE_PARAM = "script.type";

   /**
    * Writer to mongo storage. 
    */
   private RecordWriter writer;

   /**
    * Contains Pig-latin script type {@link ScriptType}.
    */
   private final String scriptType;

   /**
    * {@link MongoStorage} constructor.
    */
   public MongoStorage(String scriptType)
   {
      this.scriptType = scriptType;
   }

   /**
    * {@inheritedDoc)
    */
   @Override
   public OutputFormat getOutputFormat() throws IOException
   {
      return new TableOutputFormat();
   }

   /**
    * {@inheritedDoc)
    */
   @Override
   public void setStoreLocation(String location, Job job) throws IOException
   {
      job.getConfiguration().set(SERVER_URL_PARAM, location);
      job.getConfiguration().set(SCRIPT_TYPE_PARAM, scriptType);
   }

   /**
    * {@inheritedDoc)
    */
   @Override
   public void prepareToWrite(RecordWriter writer) throws IOException
   {
      this.writer = writer;
   }

   /**
    * {@inheritedDoc)
    */
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
