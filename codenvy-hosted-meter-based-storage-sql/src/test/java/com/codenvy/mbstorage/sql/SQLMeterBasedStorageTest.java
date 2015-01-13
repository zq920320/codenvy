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
package com.codenvy.mbstorage.sql;

import com.codenvy.api.account.MemoryUsedMetric;
import com.codenvy.api.account.UsageInformer;
import com.codenvy.api.core.ServerException;

import org.hsqldb.jdbc.JDBCDataSource;
import org.postgresql.ds.PGPoolingDataSource;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SQLMeterBasedStorageTest {

    private DataSource dataSource;

    private SQLMeterBasedStorage meterBasedStorage;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

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

        dataSource = source;
    }

    @BeforeMethod
    public void init() {
        StorageInitializer initializer = new StorageInitializer(dataSource, true, "hsqldb");
        initializer.clean();
        initializer.init();
        meterBasedStorage = new SQLMeterBasedStorage(new DataSourceConnectionFactory(dataSource));
    }


    @Test
    public void shouldBeAbleToStoreMetric() throws ParseException, ServerException {
        //given
        MemoryUsedMetric expected =
                new MemoryUsedMetric(1024,
                                     sdf.parse("10-01-2015 10:20:56"),
                                     sdf.parse("31-01-2015 10:00:00"),
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
                                     sdf.parse("10-01-2014 10:20:56"),
                                     sdf.parse("31-01-2014 10:00:00"),
                                     "usr-123",
                                     "ac-46534",
                                     "ws-235423",
                                     "run-234");
        //when
        UsageInformer usageInformer = meterBasedStorage.createMemoryUsedRecord(expected);
        usageInformer.resourceInUse();
        //
        MemoryUsedMetric actual = meterBasedStorage.getMetric(((SQLMeterBasedStorage.SQLUsageInformer)usageInformer).getRecordId());

        Assert.assertTrue(new Date().getTime() < actual.getStopTime().getTime() + 1000 * 60);
        Assert.assertEquals(actual.getStartTime(), sdf.parse("10-01-2014 10:20:56"));

    }

    @Test
    public void shouldNotUpdateAfterStop() throws ServerException, ParseException {
        //given
        MemoryUsedMetric usedMetric =
                new MemoryUsedMetric(1024,
                                     sdf.parse("10-01-2014 10:20:56"),
                                     sdf.parse("31-01-2014 10:00:00"),
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
}