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

import com.codenvy.dashboard.pig.store.fs.TupleTransformerFactory.ScriptType;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.pig.data.Tuple;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a>
 */
public class FileOutputFormat extends OutputFormat<WritableComparable, Tuple>
{

   /**
    * {@inheritedDoc)
    */
   @Override
   public RecordWriter<WritableComparable, Tuple> getRecordWriter(TaskAttemptContext context) throws IOException,
      InterruptedException
   {
      ScriptType type = ScriptType.valueOf(context.getConfiguration().get(FileStorage.SCRIPT_TYPE_PARAM).toUpperCase());
      TupleTransformer transformer = TupleTransformerFactory.createTupleTransformer(type);

      String baseDir = context.getConfiguration().get(FileStorage.BASE_FILE_DIR_PARAM);

      return new FileRecordWriter(baseDir, transformer);
   }

   /**
    * {@inheritedDoc)
    */
   @Override
   public void checkOutputSpecs(JobContext context) throws IOException, InterruptedException
   {
      // do nothing
   }

   /**
    * {@inheritedDoc)
    */
   @Override
   public OutputCommitter getOutputCommitter(TaskAttemptContext context) throws IOException, InterruptedException
   {
      return new DummyCommitter();
   }

}
