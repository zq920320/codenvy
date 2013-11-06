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
public class JdbcDataManagerFactory {

    public static final String JDBC_DATA_MANAGER_URL      = "jdbc.data-manager.url";
    public static final String JDBC_DATA_MANAGER_DRIVER   = "jdbc.data-manager.driver";
    public static final String JDBC_DATA_MANAGER_USER     = "jdbc.data-manager.user";
    public static final String JDBC_DATA_MANAGER_PASSWORD = "jdbc.data-manager.password";

    public static JdbcDataManager getDataManager() {
        String driver = Configurator.getString(JDBC_DATA_MANAGER_DRIVER);
        String url = Configurator.getString(JDBC_DATA_MANAGER_URL);
        String user = Configurator.getString(JDBC_DATA_MANAGER_USER);
        String password = Configurator.getString(JDBC_DATA_MANAGER_PASSWORD);

        switch (driver) {
            case "org.h2.Driver":
                return new H2DataManager(url, user, password);
            default:
                throw new IllegalStateException(driver + " is not supported");

        }
    }
}
