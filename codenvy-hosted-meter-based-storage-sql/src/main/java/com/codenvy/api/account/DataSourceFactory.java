package com.codenvy.api.account;
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

import org.flywaydb.core.Flyway;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author Sergii Kabashniuk
 */
public abstract class DataSourceFactory {
    /**
     * @return connection data source
     */
    public abstract DataSource getDataSource();

    /**
     * Initialize or update database structure
     *
     * @throws SQLException
     */
    @PostConstruct
    public void init() throws SQLException {
        Flyway flyway = new Flyway();
        flyway.setDataSource(getDataSource());
        flyway.baseline();
        flyway.migrate();
    }
}
