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

import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Properties;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestFileRecordWriter
{
   private TupleTransformer transformer;

   private TupleFactory tupleFactory;

   private FileRecordWriter writer;

   @Test
   public void testWriterForSpecificEventOccurrenceTupleTransformer() throws Exception
   {
      transformer = TupleTransformerFactory.createTupleTransformer(ScriptType.SPECIFIC_EVENT_OCCURRENCE);
      tupleFactory = TupleFactory.getInstance();
      writer = new FileRecordWriter("target", transformer);

      Tuple tuple = tupleFactory.newTuple();
      tuple.append("tenant-created");
      tuple.append(20101010);
      tuple.append(5L);

      writer.write(null, tuple);

      File file = new File("target/specific_event_occurrence/tenant/created/2010/10/10/value");
      Assert.assertTrue(file.exists());

      Reader reader = new BufferedReader(new FileReader(file));

      Properties props = new Properties();
      props.load(reader);

      reader.close();

      Assert.assertEquals(props, transformer.transform(tuple));
   }
}
