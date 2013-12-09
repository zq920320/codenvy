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

import javax.sql.DataSource;
import java.lang.reflect.Method;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class JdbcDataPersisterFactory {

    public static final String JDBC_DATA_PERSISTER_DATASOURCE = "jdbc.data-persister.datasource";
    public static final String JDBC_DATA_PERSISTER_URL        = "jdbc.data-persister.url";
    public static final String JDBC_DATA_PERSISTER_DRIVER     = "jdbc.data-persister.driver";
    public static final String JDBC_DATA_PERSISTER_USER       = "jdbc.data-persister.user";
    public static final String JDBC_DATA_PERSISTER_PASSWORD   = "jdbc.data-persister.password";

    public static DataPersister getDataPersister() {
        String driver = Configurator.getString(JDBC_DATA_PERSISTER_DRIVER);
        String url = Configurator.getString(JDBC_DATA_PERSISTER_URL);
        String user = Configurator.getString(JDBC_DATA_PERSISTER_USER);
        String password = Configurator.getString(JDBC_DATA_PERSISTER_PASSWORD);
        String dataSource = Configurator.getString(JDBC_DATA_PERSISTER_DATASOURCE);

        switch (driver) {
            case "org.h2.Driver":
                if (dataSource != null) {
                    return new H2DataPersister(getDataSource(dataSource));

                } else {
                    return new H2DataPersister(url, user, password);
                }
            default:
                throw new IllegalStateException(driver + " is not supported");

        }
    }

    private static DataSource getDataSource(String dataSourceName) throws IllegalStateException {
        try {
            Class<?> dsServiceClass = Class.forName("org.wso2.carbon.ndatasource.core.DataSourceService");
            Object dsServiceInstance = dsServiceClass.getConstructor().newInstance();

            Method getDataSource = dsServiceClass.getMethod("getDataSource", String.class);
            Object carbonDataSourceInstance = getDataSource.invoke(dsServiceInstance, dataSourceName);

            Method getDSObject = carbonDataSourceInstance.getClass().getMethod("getDSObject");
            return (DataSource)getDSObject.invoke(carbonDataSourceInstance);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
