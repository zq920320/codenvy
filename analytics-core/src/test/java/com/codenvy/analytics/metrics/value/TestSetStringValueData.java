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
        FSValueDataManager.storeValue(expectedValueData, MetricType.ACTIVE_USERS, uuid);
        assertEquals(FSValueDataManager.loadValue(MetricType.ACTIVE_USERS, uuid), expectedValueData);
    }

    @Test
    public void testStoreLoadEmptyValueData() throws Exception {
        ValueData expectedValueData = new SetStringValueData(Collections.<String> emptyList());

        FSValueDataManager.storeValue(expectedValueData, MetricType.ACTIVE_USERS, uuid);
        assertEquals(FSValueDataManager.loadValue(MetricType.ACTIVE_USERS, uuid), expectedValueData);
    }

    @Test
    public void testStoreLoadEmptyString() throws Exception {
        ValueData expectedValueData = new SetStringValueData(Arrays.asList(new String[]{""}));

        FSValueDataManager.storeValue(expectedValueData, MetricType.ACTIVE_USERS, uuid);
        assertEquals(FSValueDataManager.loadValue(MetricType.ACTIVE_USERS, uuid), expectedValueData);
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
        ValueData newValueData = new SetStringValueData(Arrays.asList(new String[]{"a", "b", "c"}));
        assertEquals(expectedValueData.union(expectedValueData), newValueData);
    }
}
