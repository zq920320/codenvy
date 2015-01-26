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
package com.codenvy.mbstorage.sql;

import com.codenvy.api.account.MemoryUsedMetric;
import com.codenvy.api.account.UsageInformer;
import com.codenvy.api.core.ServerException;

import org.hsqldb.jdbc.JDBCDataSource;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class SQLMeterBasedStorageTest {

    private DataSource dataSource;

    private SQLMeterBasedStorage meterBasedStorage;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy HH:mm:ss");

    @BeforeSuite
    public void initConnection() {
//        final PGPoolingDataSource source = new PGPoolingDataSource();
//        source.setDataSourceName("A Data Source");
//        source.setServerName("localhost");
//        source.setDatabaseName("docker");
//        source.setUser("docker");
//        source.setPassword("docker");
//        source.setMaxConnections(10);
//        source.setPortNumber(49153);

        JDBCDataSource source = new JDBCDataSource();
        source.setUrl("jdbc:hsqldb:mem:test");
        source.setUser("SA");
        source.setPassword("");

//        MysqlDataSource source = new MysqlDataSource();
//        source.setURL("jdbc:mysql://localhost:3306/LVBsp");
//        source.setUser("Me");
//        source.setPassword("mine");

//        MysqlDataSource source = new MysqlDataSource();
//        source.setURL("jdbc:mysql://dev.box.com:3306/test");
//        source.setUser("root");
//        source.setPassword("MysqlROOTpass");

        dataSource = source;
        sdf.setLenient(false);
    }

    @BeforeMethod
    public void init() throws SQLException {
        StorageInitializer initializer = new StorageInitializer(dataSource, true);
        initializer.clean();
        initializer.init();
        meterBasedStorage = new SQLMeterBasedStorage(new DataSourceConnectionFactory(dataSource));
    }


    @Test
    public void shouldBeAbleToStoreMetric() throws ParseException, ServerException {
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
        Assert.assertEquals(meterBasedStorage.getMetric(((SQLMeterBasedStorage.SQLUsageInformer)usageInformer).getRecordId()), expected);
    }

    @Test
    public void shouldBeAbleToUpdateEndTime() throws ServerException, ParseException {
        //given
        MemoryUsedMetric expected =
                new MemoryUsedMetric(1024,
                                     sdf.parse("10-01-2014 10:20:56").getTime(),
                                     sdf.parse("31-01-2014 10:00:00").getTime(),
                                     "usr-123",
                                     "ac-46534",
                                     "ws-235423",
                                     "run-234");
        //when
        UsageInformer usageInformer = meterBasedStorage.createMemoryUsedRecord(expected);
        usageInformer.resourceInUse();
        //
        MemoryUsedMetric actual = meterBasedStorage.getMetric(((SQLMeterBasedStorage.SQLUsageInformer)usageInformer).getRecordId());

        Assert.assertTrue(new Date().getTime() < actual.getStopTime() + 1000 * 60);
        Assert.assertEquals(actual.getStartTime(), (Long)sdf.parse("10-01-2014 10:20:56").getTime());

    }

    @Test
    public void shouldNotUpdateAfterStop() throws ServerException, ParseException {
        //given
        MemoryUsedMetric usedMetric =
                new MemoryUsedMetric(1024,
                                     sdf.parse("10-01-2014 10:20:56").getTime(),
                                     sdf.parse("31-01-2014 10:00:00").getTime(),
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

    @Test
    public void shouldGetSumByAccountWithAllDatesBetweenPeriod() throws ServerException, ParseException {
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

    @Test
    public void shouldGetSumByAccountWithDatesBetweenPeriod() throws ServerException, ParseException {
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

    @Test
    public void shouldGetSumByDifferentWs() throws ServerException, ParseException {
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

}