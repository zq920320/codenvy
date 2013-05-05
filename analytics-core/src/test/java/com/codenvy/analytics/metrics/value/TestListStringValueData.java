/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.Collections;

import org.apache.pig.data.Tuple;
import org.testng.annotations.Test;

import com.codenvy.analytics.BaseTest;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestListStringValueData extends BaseTest {

    private ValueData expectedValueData = new ListStringValueData(Arrays.asList(new StringValueData[]{
                                        new StringValueData("a"),
                                        new StringValueData("b"),
                                        new StringValueData("c")}));

    @Test
    public void testValueDataFromTuple() throws Exception {
        Tuple tupleA = tupleFactory.newTuple();
        tupleA.append("a");

        Tuple tupleB = tupleFactory.newTuple();
        tupleB.append("b");

        Tuple tupleC = tupleFactory.newTuple();
        tupleC.append("c");

        ValueData valueData =
                              ValueDataFactory.createValueData(ListStringValueData.class, Arrays.asList(new Tuple[]{tupleA,
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
        ValueData expectedValueData = new ListStringValueData(Collections.<StringValueData> emptyList());

        valueManager.store(expectedValueData, uuid);
        assertEquals(valueManager.load(uuid), expectedValueData);
    }

    @Test
    public void testStoreLoadEmptyString() throws Exception {
        ValueData expectedValueData = new ListStringValueData(Arrays.asList(new StringValueData[]{new StringValueData("")}));

        valueManager.store(expectedValueData, uuid);
        assertEquals(valueManager.load(uuid), expectedValueData);
    }



    @Test
    public void testEqualsSameOrder() throws Exception {
        assertEquals(new ListStringValueData(Arrays.asList(new StringValueData[]{new StringValueData("a"),
                new StringValueData("b"),
                new StringValueData("c")})), expectedValueData);
    }

    @Test
    public void testNotEqualsDifferentOrder() throws Exception {
        assertNotEquals(new ListStringValueData(Arrays.asList(new StringValueData[]{new StringValueData("c"),
                new StringValueData("b"),
                new StringValueData("a")})), expectedValueData);
    }

    @Test
    public void testNotEquals() throws Exception {
        assertNotEquals(new ListStringValueData(Arrays.asList(new StringValueData[]{new StringValueData("c"),
                new StringValueData("a")})), expectedValueData);
    }

    @Test
    public void testAdd() throws Exception {
        ValueData newValueData = new ListStringValueData(Arrays.asList(new StringValueData[]{
                new StringValueData("a"),
                new StringValueData("b"),
                new StringValueData("c"),
                new StringValueData("a"),
                new StringValueData("b"),
                new StringValueData("c")}));

        assertEquals(expectedValueData.union(expectedValueData), newValueData);
    }
}
