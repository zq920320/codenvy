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
package com.codenvy.analytics.services.view;

import com.codenvy.analytics.Utils;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Alexander Reshetnyak
 */
public class TestInitRowsCountForCVSReport {

    @Test(dataProvider = "provider")
    public void testActionFilter(String fromDate, String toDate, Parameters.TimeUnit timeUnit, long rowsCount) throws Exception {
        Context.Builder builder = new Context.Builder();

        builder.put(Parameters.FROM_DATE, fromDate);
        builder.put(Parameters.TO_DATE, toDate);
        builder.put(Parameters.TIME_UNIT, timeUnit.toString());

        Assert.assertEquals(Utils.initRowsCountForCSVReport(builder.build()).getAsLong(Parameters.REPORT_ROWS), rowsCount);
    }

    @DataProvider(name = "provider")
    public Object[][] actionFilterProvider() {
        return new Object[][]{
                {"20130101","20131229", Parameters.TimeUnit.DAY, 364},
                {"20140101","20140509", Parameters.TimeUnit.DAY, 130},
                {"20140502","20140509", Parameters.TimeUnit.DAY, 9},
                {"20130101","20140509", Parameters.TimeUnit.DAY, ViewBuilder.MAX_CSV_ROWS},

                {"20130101","20140509", Parameters.TimeUnit.WEEK, 72},
                {"20140101","20140509", Parameters.TimeUnit.WEEK, 20},
                {"20140401","20140509", Parameters.TimeUnit.WEEK, 7},
                {"20140503","20140509", Parameters.TimeUnit.WEEK, 3},
                {"20140101","20250509", Parameters.TimeUnit.WEEK, ViewBuilder.MAX_CSV_ROWS},

                {"20130101","20140509", Parameters.TimeUnit.MONTH, 18},
                {"20140101","20140509", Parameters.TimeUnit.MONTH, 6},
                {"20140501","20140509", Parameters.TimeUnit.MONTH, 2},
                {"20140501","20500509", Parameters.TimeUnit.MONTH, ViewBuilder.MAX_CSV_ROWS},

                {"20140101","20140509", Parameters.TimeUnit.LIFETIME, 2},
                {"20130101","20140509", Parameters.TimeUnit.LIFETIME, 2},

        };
    }
}
