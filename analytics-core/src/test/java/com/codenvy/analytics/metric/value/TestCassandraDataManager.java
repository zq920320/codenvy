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

package com.codenvy.analytics.metric.value;

import com.codenvy.analytics.BaseTest;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestCassandraDataManager extends BaseTest {

    private              String node       = "127.0.0.1";
    private static final String schemaName = "simplex_" + System.currentTimeMillis();

    @BeforeClass
    public void startCassandraCluster() throws Exception {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
    }

    @AfterClass
    public void stopCassandraCluster() throws Exception {
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    }

    @Test
    public void test() throws Exception {
        Cluster cluster = Cluster.builder().addContactPoint(node).build();

        Session session = cluster.connect("analytics_data");
        ResultSet results = session.execute("SELECT * FROM test where key='20131010'");

        for (Row row : results) {
            System.out.println(row.getLong("value"));
        }

        session.shutdown();
    }
}
