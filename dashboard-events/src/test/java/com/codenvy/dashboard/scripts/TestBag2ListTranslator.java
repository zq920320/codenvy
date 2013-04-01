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
package com.codenvy.dashboard.scripts;

import com.codenvy.dashboard.scripts.Bag2ListTranslator;

import org.apache.pig.data.DataBag;
import org.apache.pig.data.DefaultDataBag;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestBag2ListTranslator extends BasePigTest
{

    private final Bag2ListTranslator translator   = new Bag2ListTranslator();

    private final File               file         = new File(BASE_DIR, UUID.randomUUID().toString());

    private final TupleFactory       tupleFactory = TupleFactory.getInstance();

    @Test
    public void testCorrectTranslation() throws Exception
    {
        DataBag bag = new DefaultDataBag();
        Tuple tuple = tupleFactory.newTuple();
        tuple.append("user1");
        bag.add(tuple);

        Object value = translator.translate(bag);

        Assert.assertTrue(value instanceof List);

        List<String> list = (List<String>)value;

        Assert.assertNotNull(list.get(0));
        Assert.assertEquals(list.get(0), "user1");
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testTryToModifyResult() throws Exception
    {
        DataBag bag = new DefaultDataBag();
        Tuple tuple = tupleFactory.newTuple();
        tuple.append("user1");
        bag.add(tuple);

        Object value = translator.translate(bag);

        Assert.assertTrue(value instanceof List);
        List<String> list = (List<String>)value;

        list.add("some value");
    }


    @Test(expectedExceptions = IOException.class)
    public void testIncorrectValueTranslation() throws Exception
    {
        translator.translate("some value");
    }

    @Test(expectedExceptions = IOException.class)
    public void testTranslationBagWith2Tuples() throws Exception
    {
        DataBag bag = new DefaultDataBag();
        Tuple tuple = tupleFactory.newTuple();
        tuple.append("user1");
        tuple.append("user2");
        bag.add(tuple);

        translator.translate(bag);
    }

    @Test
    public void testTranslateReadWriter() throws Exception
    {
        List<String> users = new ArrayList<String>(2);
        users.add("user1");
        users.add("user2");

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        translator.doWrite(writer, users);
        writer.close();

        BufferedReader reader = new BufferedReader(new FileReader(file));
        Object value = translator.doRead(reader);
        reader.close();

        Assert.assertTrue(value instanceof List);

        users = (List<String>)value;

        Assert.assertNotNull(users.get(0));
        Assert.assertNotNull(users.get(1));
        Assert.assertEquals(users.get(0), "user1");
        Assert.assertEquals(users.get(1), "user2");
    }

    @Test(expectedExceptions = IOException.class)
    public void testTranslateReadWriterWrongObject() throws Exception
    {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        try
        {
            translator.doWrite(null, "value");
        } finally
        {
            writer.close();
        }
    }
}
