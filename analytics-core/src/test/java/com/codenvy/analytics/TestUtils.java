/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/** @author Dmytro Nochevnov  */
public class TestUtils extends BaseTest {
    @Test(dataProvider = "roundOperationProvider")
    public void testGetRoundOperation(String fieldName, int maximumFractionDigits, String result) throws Exception {
        assertEquals(Utils.getRoundOperation(fieldName, maximumFractionDigits).toString(), result);
    }

    @DataProvider(name = "roundOperationProvider")
    public Object[][] Provider() {
        return new Object[][]{{"field", 0, "{ \"$divide\" : [ { \"$subtract\" : [ { \"$multiply\" : [ \"$field\" , 1]} , " +
                                           "{ \"$mod\" : [ { \"$multiply\" : [ \"$field\" , 1]} , 1]}]} , 10000]}"},
                              {"field", 4, "{ \"$divide\" : [ { \"$subtract\" : [ { \"$multiply\" : [ \"$field\" , 10000]} , " +
                                           "{ \"$mod\" : [ { \"$multiply\" : [ \"$field\" , 10000]} , 1]}]} , 10000]}"}};
    }

    @Test(dataProvider = "roundOperationExceptionalDataProvider",
          expectedExceptions = IllegalArgumentException.class)
    public void testGetRoundOperationException(String fieldName, int maximumFractionDigits) throws Exception {
        Utils.getRoundOperation(fieldName, maximumFractionDigits);
    }

    @DataProvider(name = "roundOperationExceptionalDataProvider")
    public Object[][] ProviderExceptionalData() {
        return new Object[][]{{"", 4},
                              {null, 4},
                              {"field", -1}};
    }
}
