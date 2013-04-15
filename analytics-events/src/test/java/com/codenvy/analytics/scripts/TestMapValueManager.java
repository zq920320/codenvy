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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestMapValueManager extends BasePigTest {

    private MapValueManager valueManager;
    private TupleFactory    tupleFactory;
    private BufferedReader  reader;
    private BufferedWriter  writer;

    @BeforeMethod
    public void setUp() throws Exception {
        File file = new File(ScriptExecutor.RESULT_DIRECTORY, UUID.randomUUID().toString());
        file.createNewFile();

        reader = new BufferedReader(new FileReader(file));
        writer = new BufferedWriter(new FileWriter(file));
        valueManager = new MapValueManager();
        tupleFactory = TupleFactory.getInstance();
    }

    @Test
    public void testValueOfTuple() throws Exception {
        Tuple tuple = createTuple();

        Map<String, Long> value = valueManager.valueOf(tuple);

        Assert.assertTrue(value.containsKey("prop1"));
        Assert.assertEquals(value.get("prop1"), Long.valueOf(1));
    }
    
    @Test
    public void testValueOfString() throws Exception {
        Map<String, Long> value = valueManager.valueOf("prop1=1,prop2=2");

        Assert.assertEquals(value.get("prop1"), Long.valueOf(1));
        Assert.assertEquals(value.get("prop2"), Long.valueOf(2));
    }

    @Test
    public void testStoreLoad() throws Exception {
        Map<String, Long> value = new HashMap<String, Long>();
        value.put("1", 2L);
        value.put("3", 4L);

        ValueManager manager = new MapValueManager();
        manager.store(writer, value);

        Map<String, Long> newValue = (Map<String, Long>)manager.load(reader);

        Assert.assertEquals(newValue.get("1"), Long.valueOf(2));
        Assert.assertEquals(newValue.get("3"), Long.valueOf(4));
    }

    private Tuple createTuple() throws IOException {
        Tuple tuple = tupleFactory.newTuple();

        DataBag bag = new DefaultDataBag();
        Tuple innerTuple = tupleFactory.newTuple();
        innerTuple.append("prop1");
        innerTuple.append(1L);
        bag.add(innerTuple);

        tuple.append(bag);

        return tuple;
    }
}
