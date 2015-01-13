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
package com.codenvy.api.account;

import com.codenvy.api.core.ServerException;
import com.codenvy.mbstorage.sql.ConnectionFactory;
import com.codenvy.mbstorage.sql.SQLMeterBasedStorage;
import com.codenvy.mbstorage.sql.StorageInitializer;

import org.postgresql.ds.PGPoolingDataSource;
import org.testng.annotations.Test;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class DataSourceFactoryTest {

    Random random = new Random();

    @Test(enabled = false)
    public void testinit() throws SQLException, NamingException, ServerException, InterruptedException {


        final PGPoolingDataSource source = new PGPoolingDataSource();
        source.setDataSourceName("A Data Source");
        source.setServerName("localhost");
        source.setDatabaseName("docker");
        source.setUser("docker");
        source.setPassword("docker");
        source.setMaxConnections(10);
        source.setPortNumber(49153);

        StorageInitializer initializer = new StorageInitializer(source, true, "hsqldb");
        initializer.init();

        SQLMeterBasedStorage sqlMeterBasedStorage = new SQLMeterBasedStorage(new ConnectionFactory() {
            @Override
            public Connection getConnection() throws SQLException {
                return source.getConnection();
            }
        });

        final UsageInformer informer = sqlMeterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(random.nextInt(2048),
                                                                                                        new Date(),
                                                                                                        new Date(),
                                                                                                        "user" + random.nextInt(4),
                                                                                                        "user" + random.nextInt(4),
                                                                                                        "user" + random.nextInt(4),
                                                                                                        Integer.toString(random.nextInt())

                                                                                   )
                                                                                  );
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    informer.resourceInUse();
                } catch (ServerException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000);

        Thread.sleep(1000000);

//        for (int i = 0; i < 100; i++) {
//            sqlMeterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(random.nextInt(2048),
//                                                                             new Date(),
//                                                                             new Date(),
//                                                                             "user" + random.nextInt(4),
//                                                                             "user" + random.nextInt(4),
//                                                                             "user" + random.nextInt(4), Integer.toString(random
// .nextInt())
//
//                                                        )
//                                                       );
//        }


    }

}