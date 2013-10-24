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

package com.codenvy.analytics;

import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.factory.HFactory;

import org.apache.pig.data.TupleFactory;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.testng.annotations.BeforeSuite;

/** @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a> */
public class BaseTest {

    public static final String BASE_DIR = "target";

    public static final TupleFactory tupleFactory = TupleFactory.getInstance();

    public static final String CASSANDRA_COLUMN_FAMILY = "test";

    public static final String CASSANDRA_KEY_SPACE = "test";

    public static final String CASSANDRA_HOST = "localhost:9171";

    public static final String CASSANDRA_URL = "cassandra://" + CASSANDRA_KEY_SPACE;

    @BeforeSuite
    public void startCassandraCluster() throws Exception {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();

        Cluster cluster = HFactory.getOrCreateCluster("TestCluster", new CassandraHostConfigurator(CASSANDRA_HOST));

        HFactory.createKeyspace(CASSANDRA_KEY_SPACE, cluster);
        HFactory.createColumnFamilyDefinition(CASSANDRA_KEY_SPACE,
                                              CASSANDRA_COLUMN_FAMILY,
                                              ComparatorType.UTF8TYPE);
    }
}
