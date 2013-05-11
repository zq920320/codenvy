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
public class TestSetListStringValueData extends BaseTest {

    private ValueData expectedValueData =
                                          new SetListStringValueData(Arrays.asList(new ListStringValueData[]{
                                          new ListStringValueData(Arrays.asList(new String[]{"ws1", "pr1"})),
                                          new ListStringValueData(Arrays.asList(new String[]{"ws1", "pr2"})),
                                          new ListStringValueData(Arrays.asList(new String[]{"ws2", "pr1"}))}));

    @Test
    public void testValueDataFromTuple() throws Exception {
        Tuple innerTuple = tupleFactory.newTuple();
        innerTuple.append(tupleFactory.newTuple("ws1"));
        innerTuple.append(tupleFactory.newTuple("pr1"));
        Tuple tupleA = tupleFactory.newTuple(innerTuple);


        innerTuple = tupleFactory.newTuple();
        innerTuple.append(tupleFactory.newTuple("ws1"));
        innerTuple.append(tupleFactory.newTuple("pr2"));
        Tuple tupleB = tupleFactory.newTuple(innerTuple);

        innerTuple = tupleFactory.newTuple();
        innerTuple.append(tupleFactory.newTuple("ws2"));
        innerTuple.append(tupleFactory.newTuple("pr1"));
        Tuple tupleC = tupleFactory.newTuple(innerTuple);

        ValueData valueData =
                              ValueDataFactory.createValueData(SetListStringValueData.class, Arrays.asList(new Tuple[]{tupleA,
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
        ValueData expectedValueData = new SetListStringValueData(Collections.<ListStringValueData> emptyList());

        FSValueDataManager.store(expectedValueData, MetricType.ACTIVE_PROJECTS_NUMBER, uuid);
        assertEquals(FSValueDataManager.load(MetricType.ACTIVE_PROJECTS_NUMBER, uuid), expectedValueData);

    }

    @Test
    public void testStoreLoadEmptyCollection() throws Exception {
        ValueData expectedValueData =
                                      new SetListStringValueData(
                                                                 Arrays.asList(new ListStringValueData[]{}));

        FSValueDataManager.store(expectedValueData, MetricType.ACTIVE_PROJECTS_NUMBER, uuid);
        assertEquals(FSValueDataManager.load(MetricType.ACTIVE_PROJECTS_NUMBER, uuid), expectedValueData);

    }

    @Test
    public void testStoreLoadEmptyString() throws Exception {
        ValueData expectedValueData =
                                      new SetListStringValueData(
                                                                 Arrays.asList(new ListStringValueData[]{new ListStringValueData(
                                                                                                                                 Arrays.asList(new String[]{""})),}));
        FSValueDataManager.store(expectedValueData, MetricType.ACTIVE_PROJECTS_NUMBER, uuid);
        assertEquals(FSValueDataManager.load(MetricType.ACTIVE_PROJECTS_NUMBER, uuid), expectedValueData);

    }

    @Test
    public void testEqualsSameOrder() throws Exception {
        ValueData newValueData =
                                 new SetListStringValueData(Arrays.asList(new ListStringValueData[]{
                                         new ListStringValueData(Arrays.asList(new String[]{"ws1", "pr1"})),
                                         new ListStringValueData(Arrays.asList(new String[]{"ws1", "pr2"})),
                                         new ListStringValueData(Arrays.asList(new String[]{"ws2", "pr1"}))}));

        assertEquals(newValueData, expectedValueData);
    }

    @Test
    public void testEqualsDifferentOrder() throws Exception {
        ValueData newValueData =
                                 new SetListStringValueData(Arrays.asList(new ListStringValueData[]{
                                         new ListStringValueData(Arrays.asList(new String[]{"ws1", "pr2"})),
                                         new ListStringValueData(Arrays.asList(new String[]{"ws1", "pr1"})),
                                         new ListStringValueData(Arrays.asList(new String[]{"ws2", "pr1"}))}));

        assertEquals(newValueData, expectedValueData);
    }

    @Test
    public void testNotEquals() throws Exception {
        ValueData newValueData =
                                 new SetListStringValueData(Arrays.asList(new ListStringValueData[]{
                                         new ListStringValueData(Arrays.asList(new String[]{"ws1", "pr1"})),
                                         new ListStringValueData(Arrays.asList(new String[]{"ws2", "pr1"}))}));


        assertNotEquals(newValueData, expectedValueData);
    }
}
