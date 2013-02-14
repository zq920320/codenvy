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

import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a>
 */
public class MyOutCommitter extends OutputCommitter
{

   @Override
   public void setupJob(JobContext jobContext) throws IOException
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void cleanupJob(JobContext jobContext) throws IOException
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setupTask(TaskAttemptContext taskContext) throws IOException
   {
      // TODO Auto-generated method stub

   }

   @Override
   public boolean needsTaskCommit(TaskAttemptContext taskContext) throws IOException
   {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public void commitTask(TaskAttemptContext taskContext) throws IOException
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void abortTask(TaskAttemptContext taskContext) throws IOException
   {
      // TODO Auto-generated method stub

   }

}
