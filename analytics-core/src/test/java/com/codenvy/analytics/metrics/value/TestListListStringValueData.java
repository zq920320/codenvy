/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */


package com.codenvy.analytics.metrics.value;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.MetricType;
import org.apache.pig.data.Tuple;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestListListStringValueData extends BaseTest {

    private ValueData expectedValueData = new ListListStringValueData(Arrays.asList(new ListStringValueData[]{
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
                              ValueDataFactory.createValueData(ListListStringValueData.class, Arrays.asList(new Tuple[]{tupleA,
                                      tupleB,
                                      tupleC})
                                                                                                   .iterator());

        assertEquals(valueData, expectedValueData);
    }

    @Test
    public void testStoreLoad() throws Exception {
        FSValueDataManager.store(expectedValueData, MetricType.ACTIVE_USERS, uuid);
        assertEquals(FSValueDataManager.load(MetricType.ACTIVE_USERS, uuid), expectedValueData);
    }

    @Test
    public void testStoreLoadEmptyValueData() throws Exception {
        ValueData expectedValueData = new ListListStringValueData(Collections.<ListStringValueData> emptyList());

        FSValueDataManager.store(expectedValueData, MetricType.ACTIVE_USERS, uuid);
        assertEquals(FSValueDataManager.load(MetricType.ACTIVE_USERS, uuid), expectedValueData);
    }

    @Test
    public void testStoreLoadEmptyCollection() throws Exception {
        ValueData expectedValueData =
                                      new ListListStringValueData(
                                                                  Arrays.asList(new ListStringValueData[]{}));

        FSValueDataManager.store(expectedValueData, MetricType.ACTIVE_USERS, uuid);
        assertEquals(FSValueDataManager.load(MetricType.ACTIVE_USERS, uuid), expectedValueData);
    }

    @Test
    public void testStoreLoadEmptyString() throws Exception {
        ValueData expectedValueData =
                                      new ListListStringValueData(
                                                                  Arrays.asList(new ListStringValueData[]{new ListStringValueData(
                                                                                                                                  Arrays.asList(new String[]{""})),}));

        FSValueDataManager.store(expectedValueData, MetricType.ACTIVE_USERS, uuid);
        assertEquals(FSValueDataManager.load(MetricType.ACTIVE_USERS, uuid), expectedValueData);
    }

    @Test
    public void testEqualsSameOrder() throws Exception {
        ValueData newValueData = new ListListStringValueData(Arrays.asList(new ListStringValueData[]{
                new ListStringValueData(Arrays.asList(new String[]{"ws1", "pr1"})),
                new ListStringValueData(Arrays.asList(new String[]{"ws1", "pr2"})),
                new ListStringValueData(Arrays.asList(new String[]{"ws2", "pr1"}))}));

        assertEquals(newValueData, expectedValueData);
    }

    @Test
    public void testNotEqualsDifferentOrder() throws Exception {
        ValueData newValueData = new ListListStringValueData(Arrays.asList(new ListStringValueData[]{
                new ListStringValueData(Arrays.asList(new String[]{"ws1", "pr1"})),
                new ListStringValueData(Arrays.asList(new String[]{"ws2", "pr1"})),
                new ListStringValueData(Arrays.asList(new String[]{"ws1", "pr2"}))}));

        assertNotEquals(newValueData, expectedValueData);
    }

    @Test
    public void testNotEquals() throws Exception {
        ValueData newValueData = new ListListStringValueData(Arrays.asList(new ListStringValueData[]{
                new ListStringValueData(Arrays.asList(new String[]{"ws1", "pr1"})),
                new ListStringValueData(Arrays.asList(new String[]{"ws2", "pr1"}))}));

        assertNotEquals(newValueData, expectedValueData);
    }
}
