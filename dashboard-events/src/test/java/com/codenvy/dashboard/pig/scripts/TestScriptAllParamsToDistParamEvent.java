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

import com.codenvy.dashboard.pig.scripts.util.Event;
import com.codenvy.dashboard.pig.scripts.util.LogGenerator;

import org.apache.pig.data.DataBag;
import org.apache.pig.data.DefaultDataBag;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestScriptAllParamsToDistParamEvent extends BasePigTest
{

   @Test
   public void testEventFound() throws Exception
   {
      List<Event> events = new ArrayList<Event>();
      events.add(Event.Builder.createUserSSOLoggedInEvent("user1", "google").withDate("2010-10-01").build());
      events.add(Event.Builder.createUserSSOLoggedInEvent("user1", "jaas").withDate("2010-10-01").build());
      events.add(Event.Builder.createUserSSOLoggedInEvent("user2", "google").withDate("2010-10-01").build());

      File log = LogGenerator.generateLog(events);

      executePigScript(ScriptType.ALL_PARAMS_TO_DIST_2PARAMS_EVENT, log, new String[][]{
         {Constants.EVENT, "user-sso-logged-in"}, {Constants.PARAM_NAME, "USER"},
         {Constants.SECOND_PARAM_NAME, "USING"}, {Constants.DATE, "20101001"}, {Constants.TO_DATE, "20101003"}});

      FileObject fileObject =
         ScriptType.ALL_PARAMS_TO_DIST_2PARAMS_EVENT.createFileObject(BASE_DIR, "user-sso-logged-in", "USER", "USING",
            20101001, 20101003);

      Properties props = (Properties)fileObject.getValue();
      Assert.assertEquals(props.getProperty("google"), "2");
      Assert.assertEquals(props.getProperty("jaas"), "1");
      Assert.assertNull(props.getProperty("github"));
   }

   @Test
   public void fileObjectShouldReturnCorrectProperties() throws Exception
   {
      Tuple tuple = TupleFactory.getInstance().newTuple();
      tuple.append("user-sso-logged-in");
      tuple.append("USER");
      tuple.append("USING");
      tuple.append(20121103);
      tuple.append(20121105);
      
      Tuple innerTuple = TupleFactory.getInstance().newTuple();
      innerTuple.append("google");
      innerTuple.append(2L);
      DataBag bag = new DefaultDataBag();
      bag.add(innerTuple);
      
      tuple.append(bag);

      FileObject fileObject = ScriptType.ALL_PARAMS_TO_DIST_2PARAMS_EVENT.createFileObject(BASE_DIR, tuple);

      Assert.assertNotNull(fileObject.getKeys().get(Constants.EVENT));
      Assert.assertNotNull(fileObject.getKeys().get(Constants.PARAM_NAME));
      Assert.assertNotNull(fileObject.getKeys().get(Constants.SECOND_PARAM_NAME));
      Assert.assertNotNull(fileObject.getKeys().get(Constants.DATE));
      Assert.assertNotNull(fileObject.getKeys().get(Constants.TO_DATE));

      Iterator<String> iter = fileObject.getKeys().keySet().iterator();
      Assert.assertEquals(iter.next(), Constants.EVENT);
      Assert.assertEquals(iter.next(), Constants.PARAM_NAME);
      Assert.assertEquals(iter.next(), Constants.SECOND_PARAM_NAME);
      Assert.assertEquals(iter.next(), Constants.DATE);
      Assert.assertEquals(iter.next(), Constants.TO_DATE);

      Assert.assertEquals(fileObject.getTypeResult(), ScriptTypeResult.EVENT_2PARAM_TIMEFRAME_FOR_PROPERTIES);
      Assert.assertEquals(fileObject.getKeys().get(Constants.EVENT), "user-sso-logged-in");
      Assert.assertEquals(fileObject.getKeys().get(Constants.PARAM_NAME), "USER");
      Assert.assertEquals(fileObject.getKeys().get(Constants.SECOND_PARAM_NAME), "USING");
      Assert.assertEquals(fileObject.getKeys().get(Constants.DATE), "20121103");
      Assert.assertEquals(fileObject.getKeys().get(Constants.TO_DATE), "20121105");
      Assert.assertEquals(((Properties)fileObject.getValue()).getProperty("google"), "2");

      File file =
         new File(BASE_DIR + "/" + ScriptType.ALL_PARAMS_TO_DIST_2PARAMS_EVENT.toString().toLowerCase()
            + "/user/sso/logged/in/user/using/2012/11/03/20121105/value");
      file.delete();

      Assert.assertFalse(file.exists());

      fileObject.store();

      Assert.assertTrue(file.exists());
   }
}
