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
package com.codenvy.dashboard.pig.scripts;

import org.apache.pig.data.DataBag;
import org.apache.pig.data.DefaultDataBag;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestScriptTypeResultEventParamAll extends BasePigTest
{

   private TupleFactory tupleFactory;

   private final String event = "tenant-created";

   private final String param = "USER";

   private final Integer date = 20111010;

   private Tuple tuple;

   private FileObject fileObject;

   @BeforeMethod
   public void prepare() throws Exception
   {
      tupleFactory = TupleFactory.getInstance();

      tuple = tupleFactory.newTuple();
      tuple.append(event);
      tuple.append(param);
      tuple.append(date);

      Tuple innerTuple = tupleFactory.newTuple();
      innerTuple.append("user1");
      innerTuple.append(1L);
      
      DataBag bag = new DefaultDataBag();
      bag.add(innerTuple);

      tuple.append(bag);

      fileObject = ScriptResultType.EVENT_PARAM_ALL.createFileObject(BASE_DIR, tuple);
   }

   @Test
   public void fileObjectShouldReturnCorrectProperties() throws Exception
   {
      Assert.assertNotNull(fileObject.getKeys().get(Constants.EVENT));
      Assert.assertNotNull(fileObject.getKeys().get(Constants.PARAM_NAME));
      Assert.assertNotNull(fileObject.getKeys().get(Constants.DATE));

      Iterator<String> iter = fileObject.getKeys().keySet().iterator();
      Assert.assertEquals(iter.next(), Constants.EVENT);
      Assert.assertEquals(iter.next(), Constants.PARAM_NAME);
      Assert.assertEquals(iter.next(), Constants.DATE);

      Assert.assertEquals(fileObject.getTypeResult(), ScriptResultType.EVENT_PARAM_ALL);
      Assert.assertEquals(fileObject.getKeys().get(Constants.EVENT), event);
      Assert.assertEquals(fileObject.getKeys().get(Constants.PARAM_NAME), param);
      Assert.assertEquals(fileObject.getKeys().get(Constants.DATE), date.toString());
      Assert.assertEquals(((Properties)fileObject.getValue()).getProperty("user1"), "1");

      File file = new File("target/event_param_all/tenant/created/user/2011/10/10/value");
      file.delete();

      Assert.assertFalse(file.exists());

      fileObject.store();

      Assert.assertTrue(file.exists());
   }
}
