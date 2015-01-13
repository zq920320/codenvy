/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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

import org.flywaydb.core.Flyway;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * Initialize database structures.
 *
 * @author Sergii Kabashniuk
 */
public class StorageInitializer {

    private final Flyway flyway;

    @Inject
    public StorageInitializer(@Named("jdbc.url") String url,
                              @Named("jdbc.username") String userName,
                              @Named("jdbc.password") String password
                             ) {
        flyway = new Flyway();
        Properties config = new Properties();
        config.setProperty("flyway.url", url);
        config.setProperty("flyway.user", userName);
        config.setProperty("flyway.password", password);
        flyway.configure(config);
    }

    public StorageInitializer(DataSource dataSource, boolean cleanOnValidationError, String database) {
        flyway = new Flyway();
        flyway.setCleanOnValidationError(cleanOnValidationError);
        flyway.setDataSource(dataSource);
        flyway.setLocations("db/migration/" + database);
    }

    /**
     * Drops all objects (tables, views, procedures, triggers, ...) in the configured schemas.
     */
    public void clean() {
        flyway.clean();
    }


    @PostConstruct
    public void init() {
        flyway.migrate();
    }
}
