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
package com.codenvy.analytics.storage;

import com.codenvy.analytics.Configurator;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class JdbcDataPersisterFactory {

    public static final String JDBC_DATA_PERSISTER_URL      = "jdbc.data-persister.url";
    public static final String JDBC_DATA_PERSISTER_DRIVER   = "jdbc.data-persister.driver";
    public static final String JDBC_DATA_PERSISTER_USER     = "jdbc.data-persister.user";
    public static final String JDBC_DATA_PERSISTER_PASSWORD = "jdbc.data-persister.password";

    public static DataPersister getDataManager() {
        String driver = Configurator.getString(JDBC_DATA_PERSISTER_DRIVER);
        String url = Configurator.getString(JDBC_DATA_PERSISTER_URL);
        String user = Configurator.getString(JDBC_DATA_PERSISTER_USER);
        String password = Configurator.getString(JDBC_DATA_PERSISTER_PASSWORD);

        switch (driver) {
            case "org.h2.Driver":
                return new H2DataPersister(url, user, password);
            default:
                throw new IllegalStateException(driver + " is not supported");

        }
    }
}
