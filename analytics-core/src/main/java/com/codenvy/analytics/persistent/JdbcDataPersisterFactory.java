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

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class JdbcDataPersisterFactory {

    public static final String JDBC_DATA_PERSISTER_DATASOURCE = "jdbc.data-persister.datasource";
    public static final String JDBC_DATA_PERSISTER_URL        = "jdbc.data-persister.url";
    public static final String JDBC_DATA_PERSISTER_USER       = "jdbc.data-persister.user";
    public static final String JDBC_DATA_PERSISTER_PASSWORD   = "jdbc.data-persister.password";

    public static DataPersister getDataPersister() {
        try {
            if (Configurator.exists(JDBC_DATA_PERSISTER_DATASOURCE)) {
                String dsName = Configurator.getString(JDBC_DATA_PERSISTER_DATASOURCE);
                DataSource ds = getDataSource(dsName);
                String vendor = getVendor(ds);

                switch (vendor) {
                    case "H2":
                        return new H2DataPersister(ds);
                    default:
                        throw new IllegalStateException("Vendor " + vendor + " is not supported");
                }

            } else {
                String url = Configurator.getString(JDBC_DATA_PERSISTER_URL);
                String user = Configurator.getString(JDBC_DATA_PERSISTER_USER);
                String password = Configurator.getString(JDBC_DATA_PERSISTER_PASSWORD);

                if (url.toUpperCase().contains(":H2:")) {
                    return new H2DataPersister(url, user, password);
                } else {
                    throw new IllegalStateException("Driver for " + url + " not found");
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static DataSource getDataSource(String dataSourceName) throws ClassNotFoundException,
                                                                          NoSuchMethodException,
                                                                          IllegalAccessException,
                                                                          InvocationTargetException,
                                                                          InstantiationException {

        Class<?> dsServiceClass = Class.forName("org.wso2.carbon.ndatasource.core.DataSourceService");
        Object dsServiceInstance = dsServiceClass.getConstructor().newInstance();

        Method getDataSource = dsServiceClass.getMethod("getDataSource", String.class);
        Object carbonDataSourceInstance = getDataSource.invoke(dsServiceInstance, dataSourceName);

        Method getDSObject = carbonDataSourceInstance.getClass().getMethod("getDSObject");
        return (DataSource)getDSObject.invoke(carbonDataSourceInstance);
    }

    private static String getVendor(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName();
        }
    }
}
