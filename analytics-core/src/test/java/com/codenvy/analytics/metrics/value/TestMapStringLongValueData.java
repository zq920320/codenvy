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

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestMapStringLongValueData extends BaseTest {

    private ValueData expectedValueData;

    @BeforeMethod
    public void initValueData() {
        Map<StringValueData, LongValueData> mapVD = new HashMap<StringValueData, LongValueData>();
        mapVD.put(new StringValueData("a"), new LongValueData(1L));
        mapVD.put(new StringValueData("b"), new LongValueData(2L));
        mapVD.put(new StringValueData("c"), new LongValueData(3L));

        expectedValueData = new MapStringLongValueData(mapVD);
    }
    
    @Test
    public void testValueDataFromTuple() throws Exception {
        Tuple tupleA = tupleFactory.newTuple();
        tupleA.append("a");
        tupleA.append("1");

        Tuple tupleB = tupleFactory.newTuple();
        tupleB.append("b");
        tupleB.append("2");

        Tuple tupleC = tupleFactory.newTuple();
        tupleC.append("c");
        tupleC.append("3");

        ValueData valueData =
                              ValueDataFactory.createValueData(MapStringLongValueData.class, Arrays.asList(new Tuple[]{tupleA,
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
        ValueData expectedValueData = new MapStringLongValueData(Collections.<StringValueData, LongValueData> emptyMap());

        valueManager.store(expectedValueData, uuid);
        assertEquals(valueManager.load(uuid), expectedValueData);
    }

    @Test
    public void testStoreLoadEmptyString() throws Exception {
        Map<StringValueData, LongValueData> mapVD = new HashMap<StringValueData, LongValueData>();
        mapVD.put(new StringValueData(""), new LongValueData(1L));

        ValueData expectedValueData = new MapStringLongValueData(mapVD);

        valueManager.store(expectedValueData, uuid);
        assertEquals(valueManager.load(uuid), expectedValueData);
    }

    @Test
    public void testEqualsSameOrder() throws Exception {
        Map<StringValueData, LongValueData> mapVD = new HashMap<StringValueData, LongValueData>();
        mapVD.put(new StringValueData("a"), new LongValueData(1L));
        mapVD.put(new StringValueData("b"), new LongValueData(2L));
        mapVD.put(new StringValueData("c"), new LongValueData(3L));

        ValueData valueData = new MapStringLongValueData(mapVD);

        assertEquals(valueData, expectedValueData);
    }

    @Test
    public void testEqualsDifferentOrder() throws Exception {
        Map<StringValueData, LongValueData> mapVD = new HashMap<StringValueData, LongValueData>();
        mapVD.put(new StringValueData("c"), new LongValueData(3L));
        mapVD.put(new StringValueData("a"), new LongValueData(1L));
        mapVD.put(new StringValueData("b"), new LongValueData(2L));

        ValueData valueData = new MapStringLongValueData(mapVD);

        assertEquals(valueData, expectedValueData);
    }

    @Test
    public void testNotEquals() throws Exception {
        Map<StringValueData, LongValueData> mapVD = new HashMap<StringValueData, LongValueData>();
        mapVD.put(new StringValueData("a"), new LongValueData(1L));
        mapVD.put(new StringValueData("b"), new LongValueData(2L));

        ValueData valueData = new MapStringLongValueData(mapVD);

        assertNotEquals(valueData, expectedValueData);
    }


    @Test
    public void testAdd() throws Exception {
        Map<StringValueData, LongValueData> mapVD = new HashMap<StringValueData, LongValueData>();
        mapVD.put(new StringValueData("a"), new LongValueData(1L));
        mapVD.put(new StringValueData("d"), new LongValueData(4L));

        ValueData newValueData = new MapStringLongValueData(mapVD);

        mapVD = new HashMap<StringValueData, LongValueData>();
        mapVD.put(new StringValueData("a"), new LongValueData(2L));
        mapVD.put(new StringValueData("b"), new LongValueData(2L));
        mapVD.put(new StringValueData("c"), new LongValueData(3L));
        mapVD.put(new StringValueData("d"), new LongValueData(4L));

        ValueData resultValueData = new MapStringLongValueData(mapVD);

        assertEquals(expectedValueData.union(newValueData), resultValueData);
    }
}
