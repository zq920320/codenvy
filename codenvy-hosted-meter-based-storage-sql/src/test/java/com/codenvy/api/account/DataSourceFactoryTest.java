package com.codenvy.api.account;

import com.codenvy.mbstorage.sql.JndiDataSourcedConnectionFactory;
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
import java.util.Hashtable;

public class DataSourceFactoryTest {

    @Test
    public void testinit() throws SQLException, NamingException {


        PGPoolingDataSource source = new PGPoolingDataSource();
        source.setDataSourceName("A Data Source");
        source.setServerName("localhost");
        source.setDatabaseName("postgres");
        source.setUser("docker");
        source.setPassword("docker");
        source.setMaxConnections(10);
        source.setPortNumber(49153);

        StorageInitializer initializer = new StorageInitializer(source, true);
        initializer.init();




    }

}