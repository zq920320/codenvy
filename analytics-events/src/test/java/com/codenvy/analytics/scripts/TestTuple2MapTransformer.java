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
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestTuple2MapTransformer extends BasePigTest {

    private final Tuple2MapTransformer translator = new Tuple2MapTransformer();
    private final TupleFactory tupleFactory = TupleFactory.getInstance();

    @Test
    public void testTranslateCorrectBagTuple2Field() throws Exception {
        Tuple tuple = createTuple();

        Map<String, Long> value = translator.transform(tuple);

        Assert.assertTrue(value.containsKey("prop1"));
        Assert.assertEquals(value.get("prop1"), Long.valueOf(1));
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
