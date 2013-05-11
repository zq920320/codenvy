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
public class TestListStringValueData extends BaseTest {

    private ValueData expectedValueData = new ListStringValueData(Arrays.asList(new String[]{"a", "b", "c"}));

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
        FSValueDataManager.store(expectedValueData, MetricType.ACTIVE_PROJECTS_NUMBER, uuid);
        assertEquals(FSValueDataManager.load(MetricType.ACTIVE_PROJECTS_NUMBER, uuid), expectedValueData);
    }

    @Test
    public void testStoreLoadEmptyValueData() throws Exception {
        ValueData expectedValueData = new ListStringValueData(Collections.<String> emptyList());

        FSValueDataManager.store(expectedValueData, MetricType.ACTIVE_PROJECTS_NUMBER, uuid);
        assertEquals(FSValueDataManager.load(MetricType.ACTIVE_PROJECTS_NUMBER, uuid), expectedValueData);
    }

    @Test
    public void testStoreLoadEmptyString() throws Exception {
        ValueData expectedValueData = new ListStringValueData(Arrays.asList(new String[]{""}));

        FSValueDataManager.store(expectedValueData, MetricType.ACTIVE_PROJECTS_NUMBER, uuid);
        assertEquals(FSValueDataManager.load(MetricType.ACTIVE_PROJECTS_NUMBER, uuid), expectedValueData);
    }

    @Test
    public void testEqualsSameOrder() throws Exception {
        assertEquals(new ListStringValueData(Arrays.asList(new String[]{"a", "b", "c"})), expectedValueData);
    }

    @Test
    public void testNotEqualsDifferentOrder() throws Exception {
        assertNotEquals(new ListStringValueData(Arrays.asList(new String[]{"a", "c", "b"})), expectedValueData);
    }

    @Test
    public void testNotEquals() throws Exception {
        assertNotEquals(new ListStringValueData(Arrays.asList(new String[]{"a", "b"})), expectedValueData);
    }

    @Test
    public void testAdd() throws Exception {
        ValueData newValueData = new ListStringValueData(Arrays.asList(new String[]{"a", "b", "c", "a", "b", "c"}));
        assertEquals(expectedValueData.union(expectedValueData), newValueData);
    }
}
