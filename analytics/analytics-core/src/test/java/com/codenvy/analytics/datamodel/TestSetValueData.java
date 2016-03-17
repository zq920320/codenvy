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

import java.util.Arrays;

import static org.testng.Assert.assertEquals;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestSetValueData extends BaseTest {

    @Test
    public void testSubtract() {
        SetValueData valueData1 = new SetValueData(Arrays.<ValueData>asList(StringValueData.valueOf("a"), StringValueData.valueOf("b")));
        SetValueData valueData2 = new SetValueData(Arrays.<ValueData>asList(StringValueData.valueOf("a")));
        SetValueData subtractValueData = new SetValueData(Arrays.<ValueData>asList(StringValueData.valueOf("b")));

        assertEquals(subtractValueData, valueData1.subtract(valueData2));
    }
}
