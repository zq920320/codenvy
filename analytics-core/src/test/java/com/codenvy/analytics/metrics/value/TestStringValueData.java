/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Arrays;

import org.apache.pig.data.Tuple;
import org.testng.annotations.Test;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.MetricType;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestStringValueData extends BaseTest {

    private ValueData expectedValueData = new StringValueData("test");

    @Test
    public void testValueDataFromTuple() throws Exception {
        Tuple tuple = tupleFactory.newTuple();
        tuple.append("test");

        ValueData valueData = ValueDataFactory.createValueData(StringValueData.class, Arrays.asList(new Tuple[]{tuple}).iterator());

        assertEquals(valueData, expectedValueData);
    }

    @Test
    public void testStoreLoad() throws Exception {
        FSValueDataManager.store(expectedValueData, MetricType.ACTIVE_PROJECTS_NUMBER, uuid);
        assertEquals(FSValueDataManager.load(MetricType.ACTIVE_PROJECTS_NUMBER, uuid), expectedValueData);
    }

    @Test
    public void testStoreLoadEmptyString() throws Exception {
        ValueData expectedValueData = new StringValueData("");

        FSValueDataManager.store(expectedValueData, MetricType.ACTIVE_PROJECTS_NUMBER, uuid);
        assertEquals(FSValueDataManager.load(MetricType.ACTIVE_PROJECTS_NUMBER, uuid), expectedValueData);
    }

    @Test
    public void testEquals() throws Exception {
        assertEquals(new StringValueData("test"), expectedValueData);
    }

    @Test
    public void testNotEquals() throws Exception {
        assertNotEquals(new StringValueData("Test"), expectedValueData);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testAdd() throws Exception {
        expectedValueData.union(expectedValueData);
    }
}
