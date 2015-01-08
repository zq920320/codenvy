package com.codenvy.api.account;

import org.hsqldb.jdbc.JDBCDataSource;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DataSourceFactoryTest {

    @Test
    public void testinit() throws SQLException {
        HSQLDBDA d = new HSQLDBDA();
        d.init();
        Connection connection = d.getDataSource().getConnection();
        try(Statement st = connection.createStatement()){
            // db writes out to files and performs clean shuts down
            // otherwise there will be an unclean shutdown
            // when program ends
            st.execute("SHUTDOWN");

        }
        connection.commit();
        connection.close();


    }


    public static class HSQLDBDA extends DataSourceFactory{

        @Override
        public DataSource getDataSource() {

            JDBCDataSource dataSource = new JDBCDataSource();

            dataSource.setDatabase("jdbc:hsqldb:mydatabase" );
            dataSource.setPassword("");
            dataSource.setUser("SA");
            return dataSource;
        }
    }

}