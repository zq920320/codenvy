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
package com.codenvy.analytics.pig.udf;

import com.codenvy.analytics.BaseTest;
import org.apache.pig.data.Tuple;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/** @author Dmytro Nochevnov */
public class TestCalculateGigabyteRamHours extends BaseTest {

    private CalculateGigabyteRamHours function;

    @BeforeClass
    public void prepare() throws Exception {
        function = new CalculateGigabyteRamHours();
    }

    @Test(dataProvider = "provider")
    public void test(Long memory, Long usageTime, Double result) throws Exception {
        Tuple tuple = makeTuple(memory, usageTime);
        assertEquals(function.exec(tuple), result);
    }

    private Tuple makeTuple(Long memory, Long usageTime) {
        Tuple tuple = tupleFactory.newTuple();
        tuple.append(memory);
        tuple.append(usageTime);

        return tuple;
    }

    @DataProvider(name = "provider")
    public Object[][] createData() {
        return new Object[][]{
            {null, null, null},
            {new Long(1536), null, null},
            {new Long(1536), new Long(0), new Double(0.0)},
            {new Long(1536), new Long(120000), new Double(0.05)},
        };
    }
}
