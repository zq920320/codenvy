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
import com.codenvy.analytics.metrics.MetricType;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestSetStringValueData extends BaseTest {

    private ValueData expectedValueData = new SetStringValueData(Arrays.asList(new String[]{"a", "b", "c"}));

    @Test
    public void testValueDataFromTuple() throws Exception {
        Tuple tupleA = tupleFactory.newTuple();
        tupleA.append("a");

        Tuple tupleB = tupleFactory.newTuple();
        tupleB.append("b");

        Tuple tupleC = tupleFactory.newTuple();
        tupleC.append("c");

        ValueData valueData =
                              ValueDataFactory.createValueData(SetStringValueData.class, Arrays.asList(new Tuple[]{tupleA,
                                      tupleB,
                                      tupleC})
                                                                                            .iterator());

        assertEquals(valueData, expectedValueData);
    }

    @Test
    public void testStoreLoad() throws Exception {
        FSValueDataManager.store(expectedValueData, MetricType.ACTIVE_PROJECTS_NUMBER, uuid);
        assertEquals(FSValueDataManager.load(MetricType.ACTIVE_PROJECTS_NUMBER, uuid), expectedValueData);
    }

    @Test
    public void testStoreLoadEmptyString() throws Exception {
        ValueData expectedValueData = new SetStringValueData(Arrays.asList(new String[]{"a"}));

        FSValueDataManager.store(expectedValueData, MetricType.ACTIVE_PROJECTS_NUMBER, uuid);
        assertEquals(FSValueDataManager.load(MetricType.ACTIVE_PROJECTS_NUMBER, uuid), expectedValueData);
    }

    @Test
    public void testStoreLoadEmptyValueData() throws Exception {
        ValueData expectedValueData = new SetStringValueData(Collections.<String> emptySet());

        FSValueDataManager.store(expectedValueData, MetricType.ACTIVE_PROJECTS_NUMBER, uuid);
        assertEquals(FSValueDataManager.load(MetricType.ACTIVE_PROJECTS_NUMBER, uuid), expectedValueData);
    }


    @Test
    public void testEqualsSameOrder() throws Exception {
        assertEquals(new SetStringValueData(Arrays.asList(new String[]{"a", "b", "c"})), expectedValueData);
    }

    @Test
    public void testEqualsDifferentOrder() throws Exception {
        assertEquals(new SetStringValueData(Arrays.asList(new String[]{"a", "c", "b"})), expectedValueData);
    }

    @Test
    public void testNotEquals() throws Exception {
        assertNotEquals(new SetStringValueData(Arrays.asList(new String[]{"a", "b"})), expectedValueData);
    }

    @Test
    public void testAdd() throws Exception {
        ValueData resultValueData = new SetStringValueData(Arrays.asList(new String[]{"a", "b", "c", "a", "b", "c"}));
        assertEquals(expectedValueData.union(expectedValueData), resultValueData);
    }
}
