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
package com.codenvy.analytics.persistent;

import com.codenvy.analytics.Configurator;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@Singleton
public class JdbcDataPersisterFactory {

    private static final String URL      = "analytics.jdbc.url";
    private static final String USER     = "analytics.jdbc.user";
    private static final String PASSWORD = "analytics.jdbc.password";
    private static final String DRIVER   = "analytics.jdbc.driver";

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

        String url = configurator.getString(URL);
        String user = configurator.getString(USER);
        String password = configurator.getString(PASSWORD);
        Class.forName(configurator.getString(DRIVER));

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
