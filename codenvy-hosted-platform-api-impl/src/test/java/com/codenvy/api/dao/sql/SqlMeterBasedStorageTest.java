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
package com.codenvy.api.dao.sql;

import com.codenvy.api.account.billing.BillingPeriod;
import com.codenvy.api.account.billing.MonthlyBillingPeriod;
import com.codenvy.api.account.metrics.MemoryUsedMetric;
import com.codenvy.api.account.metrics.UsageInformer;

import org.eclipse.che.api.core.ServerException;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/**
 * Tests for {@link SqlMeterBasedStorage}
 */
public class SqlMeterBasedStorageTest extends AbstractSQLTest {


    private BillingPeriod billingPeriod = new MonthlyBillingPeriod();
    private SqlMeterBasedStorage meterBasedStorage;

    @BeforeTest
    public void initT() throws SQLException {
        DataSourceConnectionFactory connectionFactory = new DataSourceConnectionFactory(source);
        meterBasedStorage = new SqlMeterBasedStorage(connectionFactory);
    }


    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp =
            "ERROR: range lower bound must be less than or equal to range upper bound")
    public void shouldCheckIfStartTimeLessThenStop()
            throws ParseException, ServerException {
        //given
        MemoryUsedMetric expected =
                new MemoryUsedMetric(1000,
                                     sdf.parse("12-01-2015 10:20:56").getTime(),
                                     sdf.parse("10-01-2015 10:00:00").getTime(),
                                     "usr-123",
                                     "ac-46534",
                                     "ws-235423",
                                     "run-234");
        //when
        meterBasedStorage.createMemoryUsedRecord(expected);

    }

    @Test
    public void shouldBeAbleToStoreMetric()
            throws ParseException, ServerException {
        //given
        MemoryUsedMetric expected =
                new MemoryUsedMetric(1000,
                                     sdf.parse("10-01-2015 10:20:56").getTime(),
                                     sdf.parse("31-01-2015 10:00:00").getTime(),
                                     "usr-123",
                                     "ac-46534",
                                     "ws-235423",
                                     "run-234");
        //when
        UsageInformer usageInformer = meterBasedStorage.createMemoryUsedRecord(expected);
        //then
        //meterBasedStorage.getMetric()
        Assert.assertEquals(
                meterBasedStorage.getMetric(((SqlMeterBasedStorage.SQLUsageInformer)usageInformer).getRecordId()),
                expected);
    }

    @Test
    public void shouldBeAbleToUpdateEndTime()
            throws ServerException, ParseException {
        //given
        MemoryUsedMetric expected =
                new MemoryUsedMetric(1000,
                                     billingPeriod.getCurrent().getStartDate().getTime(),
                                     billingPeriod.getCurrent().getStartDate().getTime() + 1000,
                                     "usr-123",
                                     "ac-46534",
                                     "ws-235423",
                                     "run-234");
        //when
        UsageInformer usageInformer = meterBasedStorage.createMemoryUsedRecord(expected);
        usageInformer.resourceInUse();
        //
        MemoryUsedMetric actual =
                meterBasedStorage.getMetric(((SqlMeterBasedStorage.SQLUsageInformer)usageInformer).getRecordId());

        Assert.assertTrue(new Date().getTime() < actual.getStopTime() + 1000 * 60);
        Assert.assertEquals(actual.getStartTime().longValue(), billingPeriod.getCurrent().getStartDate().getTime());

    }

    @Test
    public void shouldNotUpdateAfterStop()
            throws ServerException, ParseException {
        //given
        MemoryUsedMetric usedMetric =
                new MemoryUsedMetric(1000,
                                     billingPeriod.getCurrent().getStartDate().getTime(),
                                     billingPeriod.getCurrent().getStartDate().getTime() + 1000,

                                     "usr-123",
                                     "ac-46534",
                                     "ws-235423",
                                     "run-234");
        UsageInformer usageInformer = meterBasedStorage.createMemoryUsedRecord(usedMetric);
        usageInformer.resourceUsageStopped();
        //when
        long recordId = ((SqlMeterBasedStorage.SQLUsageInformer)usageInformer).getRecordId();
        MemoryUsedMetric expected = meterBasedStorage.getMetric(recordId);
        usageInformer.resourceInUse();
        //
        Assert.assertEquals(meterBasedStorage.getMetric(recordId).getStopTime(), expected.getStopTime());
    }


    @Test
    public void shouldGetSumByDifferentWs()
            throws ServerException, ParseException {
        //given
        //when
        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2014 11:00:00").getTime(),
                                                                      sdf.parse("10-01-2014 11:01:00").getTime(),
                                                                      "usr-123453",
                                                                      "ac-348798",
                                                                      "ws-235675423",
                                                                      "run-2344567"));

        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2013 10:00:00").getTime(),
                                                                      sdf.parse("10-01-2013 10:05:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-1"));

        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2014 09:55:00").getTime(),
                                                                      sdf.parse("10-01-2014 10:05:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-2"));
        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2014 11:00:00").getTime(),
                                                                      sdf.parse("10-01-2014 11:07:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-3"));

        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2014 10:00:00").getTime(),
                                                                      sdf.parse("10-01-2014 11:08:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-124",
                                                                      "run-5"));

        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(1000,
                                                                      sdf.parse("10-01-2014 12:00:00").getTime(),
                                                                      sdf.parse("10-01-2014 12:20:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-6"));

        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2015 10:00:00").getTime(),
                                                                      sdf.parse("10-01-2015 10:05:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-7"));

        //then
        Map<String, Double> result = meterBasedStorage
                .getMemoryUsedReport("ac-46534", sdf.parse("10-01-2014 10:00:00").getTime(),
                                     sdf.parse("10-01-2014 12:10:00").getTime());
        Assert.assertEquals(result.get("ws-124"), 0.290133);
        Assert.assertEquals(result.get("ws-235423"), 0.217867);
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void shouldGetSumForGivenWorkspace() throws Exception {
        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2013 10:00:00").getTime(),
                                                                      sdf.parse("10-01-2013 10:05:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-1"));

        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2014 09:55:00").getTime(),
                                                                      sdf.parse("10-01-2014 10:05:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-2"));

        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(1000,
                                                                      sdf.parse("10-01-2014 12:00:00").getTime(),
                                                                      sdf.parse("10-01-2014 12:20:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-6"));

        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2015 10:00:00").getTime(),
                                                                      sdf.parse("10-01-2015 10:05:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-other",
                                                                      "run-7"));

        Double result = meterBasedStorage.getUsedMemoryByWorkspace("ws-235423", sdf.parse("10-01-2014 10:00:00").getTime(),
                                                                   sdf.parse("10-01-2014 12:10:00").getTime());
        Assert.assertEquals(result, 0.188);
    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp = "Metric with given id and period already exist")
    public void shouldFailToAddRunWithOverlappingPeriodAdd2Records()
            throws ParseException, ServerException {
        //then
        //when
        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(1000,
                                                                      sdf.parse("10-01-2014 12:00:00").getTime(),
                                                                      sdf.parse("01-02-2014 12:20:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-09889797"));
        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(1000,
                                                                      sdf.parse("08-01-2014 12:00:00").getTime(),
                                                                      sdf.parse("10-01-2014 15:20:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-09889797"));


    }

}