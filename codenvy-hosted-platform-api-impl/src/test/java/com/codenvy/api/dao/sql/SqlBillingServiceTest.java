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

import static org.testng.Assert.*;

import com.codenvy.api.account.billing.BillingService;
import com.codenvy.api.account.metrics.MemoryUsedMetric;
import com.codenvy.api.account.metrics.MeterBasedStorage;
import com.codenvy.api.account.metrics.UsageInformer;
import com.codenvy.api.core.ServerException;

import org.hsqldb.jdbc.JDBCDataSource;
import org.postgresql.ds.PGPoolingDataSource;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class SqlBillingServiceTest {

    private DataSource[] sources;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy HH:mm:ss");

    @BeforeSuite
    public void initSources() {

//        final JDBCDataSource hsqldb = new JDBCDataSource();
//        hsqldb.setUrl("jdbc:hsqldb:mem:test");
//        hsqldb.setUser("SA");
//        hsqldb.setPassword("");
//
//
//        final PGPoolingDataSource postgresql = new PGPoolingDataSource();
//        postgresql.setDataSourceName("codenvy");
//        postgresql.setServerName("localhost");
//        postgresql.setDatabaseName("codenvy");
//        postgresql.setUser("codenvy");
//        postgresql.setPassword("codenvy");
//        postgresql.setMaxConnections(10);
//        postgresql.setPortNumber(5432);

        sources = new DataSource[]{
               // hsqldb
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
            DataSourceConnectionFactory connectionFactory = new DataSourceConnectionFactory(sources[i]);
            result[i] = new Object[]{new SQLMeterBasedStorage(connectionFactory), new SqlBillingService(connectionFactory)};
        }
        return result;
    }


    @Test(dataProvider = "storage")
    public void shouldBeAbleToStoreMetric(MeterBasedStorage meterBasedStorage, BillingService billingService)
            throws ParseException, ServerException {
        //given
        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1024,
                                     sdf.parse("10-01-2015 10:20:56").getTime(),
                                     sdf.parse("11-01-2015 10:20:56").getTime(),
                                     "usr-123",
                                     "ac-5",
                                     "ws-7",
                                     "run-1254"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(1024,
                                     sdf.parse("10-01-2015 10:20:56").getTime(),
                                     sdf.parse("10-01-2015 18:20:56").getTime(),
                                     "usr-123",
                                     "ac-46534",
                                     "ws-235423",
                                     "run-234"));

        meterBasedStorage.createMemoryUsedRecord(
                new MemoryUsedMetric(256,
                                     sdf.parse("01-01-2015 10:20:56").getTime(),
                                     sdf.parse("10-01-2015 10:00:00").getTime(),
                                     "usr-345",
                                     "ac-3",
                                     "ws-235423",
                                     "run-234"));
        //when
        billingService.generateReceipts(null);

        //then
      //  Assert.assertEquals(billingService.getNotSendReceipt(1).size(), 1);
       // Assert.assertEquals(billingService.getUnpaidReceipt(1).size(), 1);
    }

    @Test
    public void testGenerateReceipts() throws Exception {

    }
}