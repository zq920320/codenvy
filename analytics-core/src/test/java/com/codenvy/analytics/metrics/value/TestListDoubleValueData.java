/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import com.codenvy.analytics.BaseTest;


import java.util.Arrays;
import java.util.Collections;

import org.apache.pig.data.Tuple;
import org.testng.annotations.Test;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestListDoubleValueData extends BaseTest {

    private ValueData expectedValueData = new ListDoubleValueData(Arrays.asList(new DoubleValueData[]{
                                              new DoubleValueData(1.0),
                                              new DoubleValueData(2.0),
                                              new DoubleValueData(3.0)}));

    @Test
    public void testValueDataFromTuple() throws Exception {
        Tuple tupleA = tupleFactory.newTuple();
        tupleA.append(1.0D);

        Tuple tupleB = tupleFactory.newTuple();
        tupleB.append(2.0D);

        Tuple tupleC = tupleFactory.newTuple();
        tupleC.append(3.0D);

        ValueData valueData =
                              ValueDataFactory.createValueData(ListDoubleValueData.class, Arrays.asList(new Tuple[]{tupleA,
                                      tupleB,
                                      tupleC})
                                                                                                .iterator());

        assertEquals(valueData, expectedValueData);
    }

    @Test
    public void testStoreLoad() throws Exception {
        valueManager.store(expectedValueData, uuid);
        assertEquals(valueManager.load(uuid), expectedValueData);
    }

    @Test
    public void testStoreLoadEmptyValueData() throws Exception {
        ValueData expectedValueData = new ListDoubleValueData(Collections.<DoubleValueData> emptyList());

        valueManager.store(expectedValueData, uuid);
        assertEquals(valueManager.load(uuid), expectedValueData);
    }


    @Test
    public void testEqualsSameOrder() throws Exception {
        ValueData newValueData = new ListDoubleValueData(Arrays.asList(new DoubleValueData[]{
                new DoubleValueData(1.0),
                new DoubleValueData(2.0),
                new DoubleValueData(3.0)}));

        assertEquals(newValueData, expectedValueData);
    }

    @Test
    public void testNotEqualsDifferentOrder() throws Exception {
        ValueData newValueData = new ListDoubleValueData(Arrays.asList(new DoubleValueData[]{
                new DoubleValueData(2.0),
                new DoubleValueData(1.0),
                new DoubleValueData(3.0)}));

        assertNotEquals(newValueData, expectedValueData);
    }

    @Test
    public void testNotEquals() throws Exception {
        ValueData newValueData = new ListDoubleValueData(Arrays.asList(new DoubleValueData[]{
                new DoubleValueData(2.0),
                new DoubleValueData(3.0)}));

        assertNotEquals(newValueData, expectedValueData);
    }

    @Test
    public void testAdd() throws Exception {

        ValueData newValueData = new ListDoubleValueData(Arrays.asList(new DoubleValueData[]{
                new DoubleValueData(1.0),
                new DoubleValueData(2.0),
                new DoubleValueData(3.0),
                new DoubleValueData(1.0),
                new DoubleValueData(2.0),
                new DoubleValueData(3.0)}));

        assertEquals(newValueData, expectedValueData.union(expectedValueData));
    }
}
