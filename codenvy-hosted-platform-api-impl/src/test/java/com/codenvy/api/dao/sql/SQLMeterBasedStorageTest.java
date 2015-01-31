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
import com.codenvy.api.core.ServerException;

import org.hsqldb.jdbc.JDBCDataSource;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.postgresql.ds.PGPoolingDataSource;
import org.testng.Assert;
import org.testng.annotations.*;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class SQLMeterBasedStorageTest {

    private DataSource[] sources;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy HH:mm:ss");

    private BillingPeriod billingPeriod = new MonthlyBillingPeriod();

    @BeforeSuite
    public void initSources() {

        final JDBCDataSource hsqldb = new JDBCDataSource();
        hsqldb.setUrl("jdbc:hsqldb:mem:test");
        hsqldb.setUser("SA");
        hsqldb.setPassword("");


//        final PGPoolingDataSource postgresql = new PGPoolingDataSource();
//        postgresql.setDataSourceName("codenvy1");
//        postgresql.setServerName("localhost");
//        postgresql.setDatabaseName("codenvy");
//        postgresql.setUser("codenvy");
//        postgresql.setPassword("codenvy");
//        postgresql.setMaxConnections(10);
//        postgresql.setPortNumber(5432);

        sources = new DataSource[]{
                hsqldb
                //postgresql
        };
    }


    @BeforeMethod
    @AfterMethod
    public void cleanup() throws SQLException {
        for (DataSource source : sources) {
            StorageInitializer initializer = new StorageInitializer(source, true);
            initializer.clean();
            initializer.init();
        }
    }

    @DataProvider(name = "storage")
    public Object[][] createDS() throws SQLException {

        Object[][] result = new Object[sources.length][];
        for (int i = 0; i < sources.length; i++) {
            result[i] = new Object[]{
                    new SQLMeterBasedStorage(new DataSourceConnectionFactory(sources[i]), billingPeriod)};
        }
        return result;
    }


    @Test(dataProvider = "storage", expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp =
            "Stop time can't be less then start time")
    public void shouldCheckIfStartTimeLessThenStop(SQLMeterBasedStorage meterBasedStorage)
            throws ParseException, ServerException {
        //given
        MemoryUsedMetric expected =
                new MemoryUsedMetric(1024,
                                     sdf.parse("12-01-2015 10:20:56").getTime(),
                                     sdf.parse("10-01-2015 10:00:00").getTime(),
                                     "usr-123",
                                     "ac-46534",
                                     "ws-235423",
                                     "run-234");
        //when
        meterBasedStorage.createMemoryUsedRecord(expected);

    }

    @Test(dataProvider = "storage")
    public void shouldBeAbleToStoreMetric(SQLMeterBasedStorage meterBasedStorage)
            throws ParseException, ServerException {
        //given
        MemoryUsedMetric expected =
                new MemoryUsedMetric(1024,
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
                meterBasedStorage.getMetric(((SQLMeterBasedStorage.SQLUsageInformer)usageInformer).getRecordId()),
                expected);
    }

    @Test(dataProvider = "storage")
    public void shouldBeAbleToUpdateEndTime(SQLMeterBasedStorage meterBasedStorage)
            throws ServerException, ParseException {
        //given
        MemoryUsedMetric expected =
                new MemoryUsedMetric(1024,
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
                meterBasedStorage.getMetric(((SQLMeterBasedStorage.SQLUsageInformer)usageInformer).getRecordId());

        Assert.assertTrue(new Date().getTime() < actual.getStopTime() + 1000 * 60);
        Assert.assertEquals(actual.getStartTime().longValue(), billingPeriod.getCurrent().getStartDate().getTime());

    }

    @Test(dataProvider = "storage")
    public void shouldNotUpdateAfterStop(SQLMeterBasedStorage meterBasedStorage)
            throws ServerException, ParseException {
        //given
        MemoryUsedMetric usedMetric =
                new MemoryUsedMetric(1024,
                                     billingPeriod.getCurrent().getStartDate().getTime(),
                                     billingPeriod.getCurrent().getStartDate().getTime() + 1000,

                                     "usr-123",
                                     "ac-46534",
                                     "ws-235423",
                                     "run-234");
        UsageInformer usageInformer = meterBasedStorage.createMemoryUsedRecord(usedMetric);
        usageInformer.resourceUsageStopped();
        //when
        long recordId = ((SQLMeterBasedStorage.SQLUsageInformer)usageInformer).getRecordId();
        MemoryUsedMetric expected = meterBasedStorage.getMetric(recordId);
        usageInformer.resourceInUse();
        //
        Assert.assertEquals(meterBasedStorage.getMetric(recordId).getStopTime(), expected.getStopTime());
    }

    @Test(dataProvider = "storage")
    public void shouldGetSumByAccountWithAllDatesBetweenPeriod(SQLMeterBasedStorage meterBasedStorage)
            throws ServerException, ParseException {
        //given
        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2014 11:00:00").getTime(),
                                                                      sdf.parse("10-01-2014 11:01:00").getTime(),
                                                                      "usr-123453",
                                                                      "ac-348798",
                                                                      "ws-235675423",
                                                                      "run-2344567"));

        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2014 10:00:00").getTime(),
                                                                      sdf.parse("10-01-2014 10:05:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-234"));
        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2014 11:00:00").getTime(),
                                                                      sdf.parse("10-01-2014 11:07:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-234"));

        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(1024,
                                                                      sdf.parse("10-01-2014 12:00:00").getTime(),
                                                                      sdf.parse("10-01-2014 12:20:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-234"));

        //when


        //then
        Assert.assertEquals(meterBasedStorage
                                    .getMemoryUsed("ac-46534", sdf.parse("10-01-2014 09:00:00").getTime(),
                                                   sdf.parse("10-01-2014 14:00:00").getTime()), (Long)23552L);
        Assert.assertEquals(meterBasedStorage
                                    .getMemoryUsed("ac-348798", sdf.parse("10-01-2014 09:00:00").getTime(),
                                                   sdf.parse("10-01-2014 14:00:00").getTime()), (Long)256L);
    }

    @Test(dataProvider = "storage")
    public void shouldGetSumByAccountWithDatesBetweenPeriod(SQLMeterBasedStorage meterBasedStorage)
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
                                                                      "run-234"));

        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2014 09:55:00").getTime(),
                                                                      sdf.parse("10-01-2014 10:05:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-234"));
        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2014 11:00:00").getTime(),
                                                                      sdf.parse("10-01-2014 11:07:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-234"));

        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(1024,
                                                                      sdf.parse("10-01-2014 12:00:00").getTime(),
                                                                      sdf.parse("10-01-2014 12:20:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-234"));

        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2015 10:00:00").getTime(),
                                                                      sdf.parse("10-01-2015 10:05:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-234"));

        //then
        Assert.assertEquals(meterBasedStorage
                                    .getMemoryUsed("ac-46534", sdf.parse("10-01-2014 10:00:00").getTime(),
                                                   sdf.parse("10-01-2014 12:10:00").getTime()), (Long)13312L);

    }

    @Test(dataProvider = "storage")
    public void shouldGetSumByDifferentWs(SQLMeterBasedStorage meterBasedStorage)
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
                                                                      "run-234"));

        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2014 09:55:00").getTime(),
                                                                      sdf.parse("10-01-2014 10:05:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-234"));
        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2014 11:00:00").getTime(),
                                                                      sdf.parse("10-01-2014 11:07:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-234"));

        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2014 10:00:00").getTime(),
                                                                      sdf.parse("10-01-2014 11:08:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-124",
                                                                      "run-234"));

        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(1024,
                                                                      sdf.parse("10-01-2014 12:00:00").getTime(),
                                                                      sdf.parse("10-01-2014 12:20:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-234"));

        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(256,
                                                                      sdf.parse("10-01-2015 10:00:00").getTime(),
                                                                      sdf.parse("10-01-2015 10:05:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-234"));

        //then

        Map<String, Long> result = meterBasedStorage
                .getMemoryUsedReport("ac-46534", sdf.parse("10-01-2014 10:00:00").getTime(),
                                     sdf.parse("10-01-2014 12:10:00").getTime());
        Assert.assertEquals(result.get("ws-124"), (Long)17408L);
        Assert.assertEquals(result.get("ws-235423"), (Long)13312L);
        Assert.assertEquals(2, result.size());
    }

    @Test(dataProvider = "storage")
    public void shouldAdd2Records(SQLMeterBasedStorage meterBasedStorage) throws ParseException, ServerException {
        //then
        meterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(1024,
                                                                      sdf.parse("10-01-2014 12:00:00").getTime(),
                                                                      sdf.parse("01-02-2014 12:20:00").getTime(),
                                                                      "usr-123",
                                                                      "ac-46534",
                                                                      "ws-235423",
                                                                      "run-09889797"));
        //when
        List<MemoryUsedMetric> actual = meterBasedStorage.getMetricsByRunId("run-09889797");
        Assert.assertEquals(actual.size(), 2);

    }

}