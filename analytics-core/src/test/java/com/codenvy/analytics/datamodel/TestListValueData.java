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


package com.codenvy.analytics.datamodel;

import com.codenvy.analytics.BaseTest;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestListValueData extends BaseTest {

    private ValueData valueData;

    @BeforeClass
    public void init() {
        Map<String, ValueData> value = new HashMap<>();
        value.put("key1", new LongValueData(10));
        MapValueData rowValueData1 = new MapValueData(value);

        value = new HashMap<>();
        value.put("key1", new DoubleValueData(20));
        MapValueData rowValueData2 = new MapValueData(value);

        valueData = new ListValueData(Arrays.<ValueData>asList(rowValueData1, rowValueData2));
    }

    @Test
    public void testSerialization() throws Exception {
        File file = new File(BASE_DIR, "value");

        try (ObjectOutput out = new ObjectOutputStream(new FileOutputStream(file))) {
            valueData.writeExternal(out);
        }

        ValueData newValueData = new ListValueData();
        try (ObjectInput in = new ObjectInputStream(new FileInputStream(file))) {
            newValueData.readExternal(in);
        }


        assertEquals(valueData, newValueData);
    }

    @Test
    public void testEquals() {
        Map<String, ValueData> value = new HashMap<>();
        value.put("key1", new LongValueData(10));
        MapValueData rowValueData1 = new MapValueData(value);

        value = new HashMap<>();
        value.put("key1", new DoubleValueData(20));
        MapValueData rowValueData2 = new MapValueData(value);

        ListValueData newValueData = new ListValueData(Arrays.<ValueData>asList(rowValueData1, rowValueData2));

        assertEquals(valueData, newValueData);
    }

    @Test
    public void testNotEquals() {
        Map<String, ValueData> value = new HashMap<>();
        value.put("key1", new LongValueData(10));
        MapValueData rowValueData1 = new MapValueData(value);

        value = new HashMap<>();
        value.put("key1", new DoubleValueData(20));
        MapValueData rowValueData2 = new MapValueData(value);

        ListValueData newValueData = new ListValueData(Arrays.<ValueData>asList(rowValueData2, rowValueData1));

        assertNotEquals(valueData, newValueData);
    }

    @Test
    public void testGetAsString() {
        String asString = valueData.getAsString();
        assertTrue(asString.contains("[key1=10]"));
        assertTrue(asString.contains("[key1=20.0]"));
    }

    @Test
    public void testUnion() {
        Map<String, ValueData> value = new HashMap<>();
        value.put("key1", new StringValueData("hello"));
        MapValueData rowValueData1 = new MapValueData(value);

        ListValueData newValueData = new ListValueData(Arrays.<ValueData>asList(rowValueData1));

        value = new HashMap<>();
        value.put("key1", new LongValueData(10));
        rowValueData1 = new MapValueData(value);

        value = new HashMap<>();
        value.put("key1", new DoubleValueData(20));
        MapValueData rowValueData2 = new MapValueData(value);

        value = new HashMap<>();
        value.put("key1", new StringValueData("hello"));
        MapValueData rowValueData3 = new MapValueData(value);

        ListValueData sumValueData =
                new ListValueData(Arrays.<ValueData>asList(rowValueData1, rowValueData2, rowValueData3));

        assertEquals(sumValueData, valueData.add(newValueData));
    }
}
