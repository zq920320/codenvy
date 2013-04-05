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

import com.codenvy.analytics.scripts.Bag2PropertiesTranslator;

import org.apache.pig.data.DataBag;
import org.apache.pig.data.DefaultDataBag;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Properties;
import java.util.UUID;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestBag2PropertiesTranslator extends BasePigTest {

    private final Bag2PropertiesTranslator translator = new Bag2PropertiesTranslator();

    private final File file = new File(BASE_DIR, UUID.randomUUID().toString());

    private final TupleFactory tupleFactory = TupleFactory.getInstance();

    @Test
    public void testTranslateCorrectBagTuple2Field() throws Exception {
        DataBag bag = new DefaultDataBag();
        Tuple tuple = tupleFactory.newTuple();
        tuple.append("prop1");
        tuple.append(1L);
        bag.add(tuple);

        Object value = translator.translate(bag);

        Assert.assertTrue(value instanceof Properties);

        Properties props = (Properties)value;

        Assert.assertNotNull(props.getProperty("prop1"));
        Assert.assertEquals(props.getProperty("prop1"), "1");
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testTranslateCorrectBagTuple2FieldTryModifyResult() throws Exception {
        DataBag bag = new DefaultDataBag();
        Tuple tuple = tupleFactory.newTuple();
        tuple.append("prop1");
        tuple.append(1L);
        bag.add(tuple);

        Object value = translator.translate(bag);

        Assert.assertTrue(value instanceof Properties);

        Properties props = (Properties)value;
        props.put("prop1", "2");
    }


    @Test(expectedExceptions = IOException.class)
    public void testTranslateWrongObject() throws Exception {
        translator.translate("some value");
    }

    @Test(expectedExceptions = IOException.class)
    public void testTranslateBagTuple1Field() throws Exception {
        DataBag bag = new DefaultDataBag();
        Tuple tuple = tupleFactory.newTuple();
        tuple.append("prop1");
        bag.add(tuple);

        translator.translate(bag);
    }

    @Test(expectedExceptions = IOException.class)
    public void testTranslateBagTuple3Field() throws Exception {
        DataBag bag = new DefaultDataBag();
        Tuple tuple = tupleFactory.newTuple();
        tuple.append("prop1");
        tuple.append(1L);
        tuple.append("prop2");
        bag.add(tuple);

        translator.translate(bag);
    }

    @Test
    public void testTranslateReadWriter() throws Exception {
        Properties props = new Properties();
        props.put("prop1", "1");

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        translator.doWrite(writer, props);
        writer.close();

        BufferedReader reader = new BufferedReader(new FileReader(file));
        Object value = translator.doRead(reader);
        reader.close();

        Assert.assertTrue(value instanceof Properties);

        props = (Properties)value;

        Assert.assertNotNull(props.getProperty("prop1"));
        Assert.assertEquals(props.getProperty("prop1"), "1");
    }

    @Test(expectedExceptions = IOException.class)
    public void testTranslateReadWriterWrongObject() throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        try {
            translator.doWrite(null, "value");
        } finally {
            writer.close();
        }
    }
}
