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

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.pig.data.Tuple;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class FileRecordWriter extends RecordWriter<WritableComparable, Tuple>
{

   /**
    * Contains {@link FileStorage#BASE_FILE_DIR_PARAM} value.
    */
   protected final String baseDir;

   /**
    * Contains {@link FileStorage#SCRIPT_TYPE_PARAM} value. 
    */
   private final String scriptType;

   /**
    * FileRecordWriter constructor.
    */
   FileRecordWriter(String baseDir, String scriptType) throws IOException
   {
      this.baseDir = baseDir;
      this.scriptType = scriptType;
   }

   /**
    * {@inheritedDoc)
    */
   @Override
   public void write(WritableComparable key, Tuple value) throws IOException, InterruptedException
   {
      FileObject props = FileObject.createFileObject(baseDir, scriptType, value);
      props.store();
   }

   /**
    * {@inheritedDoc)
    */
   @Override
   public void close(TaskAttemptContext context) throws IOException, InterruptedException
   {
      // do nothing
   }
}
