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
import java.util.UUID;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestArrayDoubleValueManager extends BasePigTest {

    private ArrayDoubleValueManager valueManager;
    private TupleFactory     tupleFactory;
    private BufferedReader   reader;
    private BufferedWriter   writer;

    @BeforeMethod
    public void setUp() throws Exception {
        File file = new File(ScriptExecutor.RESULT_DIRECTORY, UUID.randomUUID().toString());
        file.createNewFile();

        reader = new BufferedReader(new FileReader(file));
        writer = new BufferedWriter(new FileWriter(file));
        valueManager = new ArrayDoubleValueManager();
        tupleFactory = TupleFactory.getInstance();
    }

    @Test
    public void testValueOfString() throws Exception {
        Double[] value = valueManager.valueOf("1,2,3");

        Assert.assertEquals(value[0], 1D);
        Assert.assertEquals(value[1], 2D);
        Assert.assertEquals(value[2], 3D);
    }

    @Test
    public void testValueOfStringWithBrackets() throws Exception {
        Double[] value = valueManager.valueOf("[1,2,3]");

        Assert.assertEquals(value[0], 1D);
        Assert.assertEquals(value[1], 2D);
        Assert.assertEquals(value[2], 3D);
    }

    @Test
    public void testStoreLoad() throws Exception {
        Double[] value = {10D, 11.0D};

        valueManager.store(writer, value);
        Double[] newValue = valueManager.load(reader);

        Assert.assertEquals(value[0], 10D);
        Assert.assertEquals(value[1], 11.0D);
    }

    private Tuple createTuple() {
        Tuple tuple = tupleFactory.newTuple();

        Tuple innterTuple = tupleFactory.newTuple();
        innterTuple.append(10D);
        innterTuple.append(11.1D);
        DataBag bag = new DefaultDataBag();
        bag.add(innterTuple);
        tuple.append(bag);

        return tuple;
    }
}
