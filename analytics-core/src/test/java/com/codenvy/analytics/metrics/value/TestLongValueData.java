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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestLongValueData extends BaseTest {

    private final ValueData expectedValueData = new LongValueData(10L);

    @Test
    public void testValueDataFromTuple() throws Exception {
        Tuple tuple = tupleFactory.newTuple();
        tuple.append(10L);

        ValueData valueData = ValueDataFactory.createValueData(LongValueData.class, Arrays.asList(new Tuple[]{tuple}).iterator());

        assertEquals(valueData, expectedValueData);
    }

    @Test
    public void testStoreLoad() throws Exception {
        FSValueDataManager.store(expectedValueData, MetricType.ACTIVE_USERS, uuid);
        assertEquals(FSValueDataManager.load(MetricType.ACTIVE_USERS, uuid), expectedValueData);
    }

    @Test
    public void testEquals() throws Exception {
        assertEquals(new LongValueData(10L), expectedValueData);
    }

    @Test
    public void testNotEquals() throws Exception {
        assertNotEquals(new LongValueData(11L), expectedValueData);
    }

    @Test
    public void testAdd() throws Exception {
        assertEquals(expectedValueData.union(expectedValueData), (new LongValueData(20L)));
    }
}
