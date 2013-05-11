/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.pig.data.Tuple;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.MetricType;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestMapStringLongValueData extends BaseTest {

    private ValueData expectedValueData;

    @BeforeMethod
    public void initValueData() {
        Map<String, Long> mapVD = new HashMap<String, Long>();
        mapVD.put("a", 1L);
        mapVD.put("b", 2L);
        mapVD.put("c", 3L);

        expectedValueData = new MapStringLongValueData(mapVD);
    }
    
    @Test
    public void testValueDataFromTuple() throws Exception {
        Tuple tupleA = tupleFactory.newTuple();
        tupleA.append("a");
        tupleA.append(1L);

        Tuple tupleB = tupleFactory.newTuple();
        tupleB.append("b");
        tupleB.append(2L);

        Tuple tupleC = tupleFactory.newTuple();
        tupleC.append("c");
        tupleC.append(3L);

        ValueData valueData =
                              ValueDataFactory.createValueData(MapStringLongValueData.class, Arrays.asList(new Tuple[]{tupleA,
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
        ValueData expectedValueData = new MapStringLongValueData(Collections.<String, Long> emptyMap());

        FSValueDataManager.store(expectedValueData, MetricType.ACTIVE_PROJECTS_NUMBER, uuid);
        assertEquals(FSValueDataManager.load(MetricType.ACTIVE_PROJECTS_NUMBER, uuid), expectedValueData);

    }

    @Test
    public void testStoreLoadEmptyString() throws Exception {
        Map<String, Long> mapVD = new HashMap<String, Long>();
        mapVD.put("", 1L);

        ValueData expectedValueData = new MapStringLongValueData(mapVD);

        FSValueDataManager.store(expectedValueData, MetricType.ACTIVE_PROJECTS_NUMBER, uuid);
        assertEquals(FSValueDataManager.load(MetricType.ACTIVE_PROJECTS_NUMBER, uuid), expectedValueData);
    }

    @Test
    public void testEqualsSameOrder() throws Exception {
        Map<String, Long> mapVD = new HashMap<String, Long>();
        mapVD.put("a", 1L);
        mapVD.put("b", 2L);
        mapVD.put("c", 3L);

        ValueData valueData = new MapStringLongValueData(mapVD);

        assertEquals(valueData, expectedValueData);
    }

    @Test
    public void testEqualsDifferentOrder() throws Exception {
        Map<String, Long> mapVD = new HashMap<String, Long>();
        mapVD.put("a", 1L);
        mapVD.put("c", 3L);
        mapVD.put("b", 2L);

        ValueData valueData = new MapStringLongValueData(mapVD);

        assertEquals(valueData, expectedValueData);
    }

    @Test
    public void testNotEquals() throws Exception {
        Map<String, Long> mapVD = new HashMap<String, Long>();
        mapVD.put("a", 1L);
        mapVD.put("c", 3L);

        ValueData valueData = new MapStringLongValueData(mapVD);

        assertNotEquals(valueData, expectedValueData);
    }


    @Test
    public void testAdd() throws Exception {
        Map<String, Long> mapVD = new HashMap<String, Long>();
        mapVD.put("a", 1L);
        mapVD.put("d", 4L);

        ValueData newValueData = new MapStringLongValueData(mapVD);

        mapVD = new HashMap<String, Long>();
        mapVD.put("a", 2L);
        mapVD.put("b", 2L);
        mapVD.put("c", 3L);
        mapVD.put("d", 4L);

        ValueData resultValueData = new MapStringLongValueData(mapVD);
        assertEquals(expectedValueData.union(newValueData), resultValueData);
    }
}
