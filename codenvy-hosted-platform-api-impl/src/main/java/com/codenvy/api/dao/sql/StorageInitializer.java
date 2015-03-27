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
package com.codenvy.api.dao.sql;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.callback.FlywayCallback;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Initialize database structures.
 *
 * @author Sergii Kabashniuk
 */
public class StorageInitializer {

    private final Flyway  flyway;
    private final Boolean cleanupOnStartup;

    @Inject
    public StorageInitializer(JndiDataSourcedConnectionFactory dataSourcedConnectionFactory,
                              @Named("dbcodenvy.clean_on_startup") Boolean cleanupOnStartup) throws SQLException {
        this.cleanupOnStartup = cleanupOnStartup;
        flyway = new Flyway();
        flyway.setDataSource(dataSourcedConnectionFactory.getDataSource());
        flyway.setLocations(getScriptLocation());

    }

    public StorageInitializer(DataSource dataSource, boolean cleanOnValidationError) throws SQLException {
        cleanupOnStartup = cleanOnValidationError;
        flyway = new Flyway();
        flyway.setCleanOnValidationError(cleanOnValidationError);
        flyway.setDataSource(dataSource);
        flyway.setLocations(getScriptLocation());
    }

    /**
     * Drops all objects (tables, views, procedures, triggers, ...) in the configured schemas.
     */
    public void clean() {
        flyway.clean();
    }


    @PostConstruct
    public void init() {
        if (cleanupOnStartup) {
            clean();
        }
        flyway.migrate();
    }

    private String getScriptLocation() throws SQLException {
        try (Connection connection = flyway.getDataSource().getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            switch (metadata.getDatabaseProductName()) {

                case "PostgreSQL":
                    return "db/migration/postgresql";
                case "H2":
                    return "db/migration/h2";
                default:
                    throw new RuntimeException("Unknown database " + metadata.getDatabaseProductName());
            }
        }
    }

}
