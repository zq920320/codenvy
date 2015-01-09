package com.codenvy.api.account;

import com.codenvy.mbstorage.sql.ConnectionFactory;
import com.codenvy.mbstorage.sql.JndiDataSourcedConnectionFactory;
import com.codenvy.mbstorage.sql.SQLMeterBasedStorage;
import com.codenvy.mbstorage.sql.StorageInitializer;

import org.hsqldb.jdbc.JDBCDataSource;
import org.postgresql.ds.PGPoolingDataSource;
import org.testng.annotations.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Hashtable;
import java.util.Random;

public class DataSourceFactoryTest {

    Random random = new Random();

    @Test
    public void testinit() throws SQLException, NamingException {


        final PGPoolingDataSource source = new PGPoolingDataSource();
        source.setDataSourceName("A Data Source");
        source.setServerName("localhost");
        source.setDatabaseName("postgres");
        source.setUser("docker");
        source.setPassword("docker");
        source.setMaxConnections(10);
        source.setPortNumber(49153);

        StorageInitializer initializer = new StorageInitializer(source, true);
        initializer.init();

        SQLMeterBasedStorage sqlMeterBasedStorage = new SQLMeterBasedStorage(new ConnectionFactory() {
            @Override
            public Connection getConnection() throws SQLException {
                return source.getConnection();
            }
        });

        for (int i = 0; i < 100; i++) {
            sqlMeterBasedStorage.createMemoryUsedRecord(new MemoryUsedMetric(random.nextInt(2048),
                                                                             new Date(),
                                                                             new Date(),
                                                                             "user" + random.nextInt(4),
                                                                             "user" + random.nextInt(4),
                                                                             "user" + random.nextInt(4), Integer.toString(random.nextInt())

                                                        )
                                                       );
        }


    }

}