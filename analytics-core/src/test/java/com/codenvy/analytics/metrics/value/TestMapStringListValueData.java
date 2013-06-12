/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.MetricType;

import org.apache.pig.data.DefaultDataBag;
import org.apache.pig.data.Tuple;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestMapStringListValueData extends BaseTest {

    private ValueData           expectedValueData;
    private ListStringValueData value;
    private String              key;
    
    @BeforeMethod
    public void prepare() {
        value = new ListStringValueData(Arrays.asList(new String[]{"a", "b", "c"}));
        key = "user";
        
        HashMap<String, ListStringValueData> map = new HashMap<String, ListStringValueData>();
        map.put(key, value);
        
        expectedValueData = new MapStringListValueData(map);
    }

    @Test
    public void testValueDataFromTuple() throws Exception {
        Tuple tuple = tupleFactory.newTuple();
        tuple.append(key);

        DefaultDataBag bag = new DefaultDataBag();
        tuple.append(bag);

        Tuple tupleA = tupleFactory.newTuple();
        tupleA.append("a");
        bag.add(tupleA);

        Tuple tupleB = tupleFactory.newTuple();
        tupleB.append("b");
        bag.add(tupleB);

        Tuple tupleC = tupleFactory.newTuple();
        tupleC.append("c");
        bag.add(tupleC);

        ValueData valueData = ValueDataFactory.createValueData(MapStringListValueData.class, Arrays.asList(new Tuple[]{tuple}).iterator());

        assertEquals(valueData, expectedValueData);
    }

    @Test
    public void testStoreLoad() throws Exception {
        FSValueDataManager.store(expectedValueData, MetricType.ACTIVE_PROJECTS_NUMBER, uuid);
        assertEquals(FSValueDataManager.load(MetricType.ACTIVE_PROJECTS_NUMBER, uuid), expectedValueData);
    }

    @Test
    public void testStoreLoadEmptyValueData() throws Exception {
        ValueData expectedValueData = new MapStringListValueData(Collections.<String, ListStringValueData> emptyMap());

        FSValueDataManager.store(expectedValueData, MetricType.ACTIVE_PROJECTS_NUMBER, uuid);
        assertEquals(FSValueDataManager.load(MetricType.ACTIVE_PROJECTS_NUMBER, uuid), expectedValueData);
    }

    @Test
    public void testStoreLoadEmptyValue() throws Exception {
        HashMap<String, ListStringValueData> map = new HashMap<String, ListStringValueData>();
        map.put(key, new ListStringValueData(Arrays.asList(new String[]{""})));

        ValueData expectedValueData = new MapStringListValueData(map);


        FSValueDataManager.store(expectedValueData, MetricType.ACTIVE_PROJECTS_NUMBER, uuid);
        assertEquals(FSValueDataManager.load(MetricType.ACTIVE_PROJECTS_NUMBER, uuid), expectedValueData);
    }

    @Test
    public void testEqualsSameOrder() throws Exception {
        ListStringValueData value = new ListStringValueData(Arrays.asList(new String[]{"a", "b", "c"}));
        HashMap<String, ListStringValueData> map = new HashMap<String, ListStringValueData>();
        map.put(key, value);

        MapStringListValueData testedValueData = new MapStringListValueData(map);

        assertEquals(testedValueData, expectedValueData);
    }

    @Test
    public void testNotEqualsDifferentOrder() throws Exception {
        ListStringValueData value = new ListStringValueData(Arrays.asList(new String[]{"a", "c", "b"}));
        HashMap<String, ListStringValueData> map = new HashMap<String, ListStringValueData>();
        map.put(key, value);

        MapStringListValueData testedValueData = new MapStringListValueData(map);

        assertNotEquals(testedValueData, expectedValueData);
    }

    @Test
    public void testNotEquals() throws Exception {
        ListStringValueData value = new ListStringValueData(Arrays.asList(new String[]{"a", "b"}));
        HashMap<String, ListStringValueData> map = new HashMap<String, ListStringValueData>();
        map.put(key, value);

        MapStringListValueData testedValueData = new MapStringListValueData(map);

        assertNotEquals(testedValueData, expectedValueData);
    }

    @Test
    public void testAdd() throws Exception {
        ListStringValueData value = new ListStringValueData(Arrays.asList(new String[]{"a", "b", "c", "a", "b", "c"}));
        HashMap<String, ListStringValueData> map = new HashMap<String, ListStringValueData>();
        map.put(key, value);

        MapStringListValueData testedValueData = new MapStringListValueData(map);

        assertEquals(expectedValueData.union(expectedValueData), testedValueData);
    }
}
