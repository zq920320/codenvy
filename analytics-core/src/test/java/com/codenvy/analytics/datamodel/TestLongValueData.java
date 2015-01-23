/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.datamodel;

import com.codenvy.analytics.BaseTest;

import org.testng.annotations.Test;

import java.io.*;

import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.assertEquals;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestLongValueData extends BaseTest {

    private final ValueData valueData = new LongValueData(10L);

    @Test
    public void testSerialization() throws Exception {
        File file = new File(BASE_DIR, "value");

        try (ObjectOutput out = new ObjectOutputStream(new FileOutputStream(file))) {
            valueData.writeExternal(out);
        }

        LongValueData newValueData = new LongValueData();
        try (ObjectInput in = new ObjectInputStream(new FileInputStream(file))) {
            newValueData.readExternal(in);
        }


        assertEquals(valueData, newValueData);
    }

    @Test
    public void testEquals() {
        assertEquals(valueData, new LongValueData(10L));
    }

    @Test
    public void testNotEquals() {
        assertNotEquals(valueData, new LongValueData(100L));
    }

    @Test
    public void testGetAsString() {
        assertEquals("10", valueData.getAsString());
    }

    @Test
    public void testAdd() {
        assertEquals(new LongValueData(20L), valueData.add(valueData));
    }

    @Test
    public void testSubtract() {
        assertEquals(LongValueData.DEFAULT, valueData.subtract(valueData));
    }
}
