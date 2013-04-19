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
package com.codenvy.analytics.scripts;


import org.apache.pig.data.DataBag;
import org.apache.pig.data.DefaultDataBag;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestListValueManager extends BasePigTest {

    private ListValueManager valueManager;
    private TupleFactory     tupleFactory;
    private BufferedReader   reader;
    private BufferedWriter   writer;

    @BeforeMethod
    public void setUp() throws Exception {
        File file = new File(ScriptExecutor.RESULT_DIRECTORY, UUID.randomUUID().toString());
        file.createNewFile();

        reader = new BufferedReader(new FileReader(file));
        writer = new BufferedWriter(new FileWriter(file));
        valueManager = new ListValueManager();
        tupleFactory = TupleFactory.getInstance();
    }

    @Test
    public void testValueOfTuple() throws Exception {
        Tuple tuple = createTuple();

        List<String> value = valueManager.valueOf(tuple);

        Assert.assertNotNull(value.get(0));
        Assert.assertEquals(value.get(0), "user1");
    }

    @Test
    public void testValueOfString() throws Exception {
        List<String> value = valueManager.valueOf("1,2,3");

        Assert.assertEquals(value.get(0), "1");
        Assert.assertEquals(value.get(1), "2");
        Assert.assertEquals(value.get(2), "3");
    }

    @Test
    public void testValueOfStringWithBrackets() throws Exception {
        List<String> value = valueManager.valueOf("[1,2,3]");

        Assert.assertEquals(value.get(0), "1");
        Assert.assertEquals(value.get(1), "2");
        Assert.assertEquals(value.get(2), "3");
    }

    @Test
    public void testStoreLoad() throws Exception {
        List<String> value = new ArrayList<String>();
        value.add("1");
        value.add("2");

        ValueManager manager = new ListValueManager();
        manager.store(writer, value);

        List<String> newValue = (List<String>)manager.load(reader);

        Assert.assertEquals(newValue.get(0), "1");
        Assert.assertEquals(newValue.get(1), "2");
    }

    private Tuple createTuple() {
        Tuple tuple = tupleFactory.newTuple();

        DataBag bag = new DefaultDataBag();
        Tuple innerTuple = tupleFactory.newTuple();
        innerTuple.append("user1");
        bag.add(innerTuple);

        tuple.append(bag);

        return tuple;
    }
}
