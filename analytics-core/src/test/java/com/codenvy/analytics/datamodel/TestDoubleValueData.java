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

import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.assertEquals;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestDoubleValueData extends BaseTest {

    private final ValueData valueData = new DoubleValueData(10.1);

    @Test
    public void testEquals() {
        assertEquals(valueData, new DoubleValueData(10.1));
    }

    @Test
    public void testNotEquals() {
        assertNotEquals(valueData, new DoubleValueData(100.1));
    }

    @Test
    public void testGetAsString() {
        assertEquals("10.1", valueData.getAsString());
    }

    @Test
    public void testAdd() {
        assertEquals(new DoubleValueData(20.2), valueData.add(valueData));
    }

    @Test
    public void testSubtract() {
        assertEquals(DoubleValueData.DEFAULT, valueData.subtract(valueData));
    }
}
