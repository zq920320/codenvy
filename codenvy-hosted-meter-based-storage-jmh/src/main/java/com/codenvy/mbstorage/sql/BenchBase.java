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

import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.openjdk.jmh.annotations.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

/**
 * @author Sergii Kabashniuk
 */
@State(Scope.Benchmark)
public class BenchBase {
    protected static final int MIN_POOL_SIZE = 0;

    @Param({"hsqldb", "mysql", "postgres"})
        public String dbType;

    @Param({"32"})
    public int maxPoolSize;

    public static volatile DataSource DS;

    public static volatile SQLMeterBasedStorage storage;
    public static volatile StorageInitializer   initializer;

    @Setup
    public void setup() throws SQLException {
        switch (dbType) {
            case "hsqldb":
                setupHsqldb();
                break;
            case "mysql":
                setupMysql();
                break;
            case "postgres":
                setupPostgres();
                break;
        }
        storage = new SQLMeterBasedStorage(new DataSourceConnectionFactory(DS));
        initializer = new StorageInitializer(DS, true);
        initializer.clean();
        initializer.init();
    }

    @TearDown
    public void teardown() {
        ((org.apache.tomcat.jdbc.pool.DataSource)DS).close();
    }


    protected void setupHsqldb() {
        PoolProperties props = new PoolProperties();
        props.setUrl("jdbc:hsqldb:mem:test");
        props.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
        props.setUsername("SA");
        props.setPassword("");
        props.setInitialSize(MIN_POOL_SIZE);
        props.setMinIdle(MIN_POOL_SIZE);
        props.setMaxIdle(maxPoolSize);
        props.setMaxActive(maxPoolSize);
        props.setMaxWait(8000);
        props.setDefaultAutoCommit(true);
        props.setRollbackOnReturn(true);
        props.setMinEvictableIdleTimeMillis((int)TimeUnit.MINUTES.toMillis(30));
        props.setTestOnBorrow(true);
        props.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        props.setValidationQuery("VALUES 1");
        props.setJdbcInterceptors("ConnectionState");

        DS = new org.apache.tomcat.jdbc.pool.DataSource(props);
    }


    protected void setupMysql() {
        PoolProperties props = new PoolProperties();
        props.setUrl("jdbc:mysql://localhost:3306/codenvy");
        props.setDriverClassName("com.mysql.jdbc.Driver");
        props.setUsername("codenvy");
        props.setPassword("codenvy");
        props.setInitialSize(MIN_POOL_SIZE);
        props.setMinIdle(MIN_POOL_SIZE);
        props.setMaxIdle(maxPoolSize);
        props.setMaxActive(maxPoolSize);
        props.setMaxWait(8000);
        props.setDefaultAutoCommit(true);
        props.setRollbackOnReturn(true);
        props.setMinEvictableIdleTimeMillis((int)TimeUnit.MINUTES.toMillis(30));
        props.setTestOnBorrow(true);
        props.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        props.setValidationQuery("VALUES 1");
        props.setJdbcInterceptors("ConnectionState");


//
//        p.setJmxEnabled(true);
//        p.setTestWhileIdle(false);
//        p.setTestOnBorrow(true);
//        p.setValidationQuery("SELECT 1");
//        p.setTestOnReturn(false);
//        p.setValidationInterval(30000);
//        p.setTimeBetweenEvictionRunsMillis(30000);
//        p.setMaxActive(100);
//        p.setInitialSize(10);
//        p.setMaxWait(10000);
//        p.setRemoveAbandonedTimeout(60);
//        p.setMinEvictableIdleTimeMillis(30000);
//        p.setMinIdle(10);
//        p.setLogAbandoned(true);
//        p.setRemoveAbandoned(true);
//        p.setJdbcInterceptors(
//                "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"+
//                "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");




        DS = new org.apache.tomcat.jdbc.pool.DataSource(props);
    }

    protected void setupPostgres() {
        PoolProperties props = new PoolProperties();
        props.setUrl("jdbc:postgresql://localhost:5432/codenvy");
        props.setDriverClassName("org.postgresql.Driver");
        props.setUsername("codenvy");
        props.setPassword("codenvy");
        props.setInitialSize(MIN_POOL_SIZE);
        props.setMinIdle(MIN_POOL_SIZE);
        props.setMaxIdle(maxPoolSize);
        props.setMaxActive(maxPoolSize);
        props.setMaxWait(8000);
        props.setDefaultAutoCommit(true);
        props.setRollbackOnReturn(true);
        props.setMinEvictableIdleTimeMillis((int)TimeUnit.MINUTES.toMillis(30));
        props.setTestOnBorrow(true);
        props.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        props.setValidationQuery("VALUES 1");
        props.setJdbcInterceptors("ConnectionState");

        DS = new org.apache.tomcat.jdbc.pool.DataSource(props);
    }

}