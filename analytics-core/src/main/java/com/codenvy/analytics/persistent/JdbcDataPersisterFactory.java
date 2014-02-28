/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.persistent;

import com.codenvy.analytics.Configurator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.InvocationTargetException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@Singleton
public class JdbcDataPersisterFactory {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcDataPersisterFactory.class);

    private static final String JDBC_DATA_PERSISTER_URL      = "jdbc.data-persister.url";
    private static final String JDBC_DATA_PERSISTER_USER     = "jdbc.data-persister.user";
    private static final String JDBC_DATA_PERSISTER_PASSWORD = "jdbc.data-persister.password";
    private static final String JDBC_DATA_PERSISTER_DRIVER   = "jdbc.data-persister.driver";

    private final DataPersister dataPersister;

    @Inject
    public JdbcDataPersisterFactory(Configurator configurator) throws ClassNotFoundException,
                                                                      NoSuchMethodException,
                                                                      InstantiationException,
                                                                      IllegalAccessException,
                                                                      InvocationTargetException,
                                                                      SQLException {
        dataPersister = initDataPersister(configurator);
    }

    private DataPersister initDataPersister(Configurator configurator) throws ClassNotFoundException,
                                                                              NoSuchMethodException,
                                                                              InvocationTargetException,
                                                                              InstantiationException,
                                                                              IllegalAccessException,
                                                                              SQLException {

        String url = configurator.getString(JDBC_DATA_PERSISTER_URL);
        String user = configurator.getString(JDBC_DATA_PERSISTER_USER);
        String password = configurator.getString(JDBC_DATA_PERSISTER_PASSWORD);
        Class.forName(configurator.getString(JDBC_DATA_PERSISTER_DRIVER));

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Enumeration<Driver> drivers = DriverManager.getDrivers();
                while (drivers.hasMoreElements()) {
                    Driver driver = drivers.nextElement();

                    try {
                        DriverManager.deregisterDriver(driver);
                        LOG.info("Driver " + driver + " successfully unregistered");
                    } catch (SQLException e) {
                        LOG.error("Failed to unregister driver " + driver);
                    }
                }
            }
        });

        if (url.toUpperCase().contains(":H2:")) {
            return new H2DataPersister(url, user, password);
        } else if (url.toUpperCase().contains(":HSQLDB:")) {
            return new H2DataPersister(url, user, password);
        } else {
            throw new IllegalStateException("Driver for " + url + " not found");
        }
    }

    public DataPersister getDataPersister() {
        return dataPersister;
    }
}
