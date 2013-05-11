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
public class TestDoubleValueData extends BaseTest {

    private final ValueData expectedValueData = new DoubleValueData(1.1D);

    @Test
    public void testValueDataFromTuple() throws Exception {
        Tuple tuple = tupleFactory.newTuple();
        tuple.append(1.1D);

        ValueData valueData = ValueDataFactory.createValueData(DoubleValueData.class, Arrays.asList(new Tuple[]{tuple}).iterator());

        assertEquals(valueData, expectedValueData);
    }

    @Test
    public void testStoreLoad() throws Exception {
        FSValueDataManager.store(expectedValueData, MetricType.ACTIVE_PROJECTS_NUMBER, uuid);
        assertEquals(FSValueDataManager.load(MetricType.ACTIVE_PROJECTS_NUMBER, uuid), expectedValueData);
    }

    @Test
    public void testEquals() throws Exception {
        assertEquals(new DoubleValueData(1.1D), expectedValueData);
    }

    @Test
    public void testNotEquals() throws Exception {
        assertNotEquals(new DoubleValueData(1.2D), expectedValueData);
    }

    public void testAdd() throws Exception {
        assertEquals(new DoubleValueData(2.2D), expectedValueData.union(expectedValueData));
    }
}
