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
import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.cassandra.CassandraDataManager;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.testng.AssertJUnit.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestCassandraDataManager extends BaseTest {

    private Cluster cluster;

    @BeforeClass
    public void startCassandraCluster() throws Exception {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();

        Cluster.Builder builder = Cluster.builder();
        for (String node : Configurator.getArray(CassandraDataManager.CASSANDRA_DATA_MANAGER_HOST)) {
            builder.addContactPoint(node);
        }
        builder.withPort(Configurator.getInt(CassandraDataManager.CASSANDRA_DATA_MANAGER_PORT));
        builder.withCredentials(Configurator.getString(CassandraDataManager.CASSANDRA_DATA_MANAGER_USER),
                                Configurator.getString(CassandraDataManager.CASSANDRA_DATA_MANAGER_PASSWORD));

        cluster = builder.build();

        Session session = cluster.connect();
        try {
            session.execute(
                    "CREATE KEYSPACE " + Configurator.getString(CassandraDataManager.CASSANDRA_DATA_MANAGER_KEYSPACE) +
                    " WITH replication = {'class':'SimpleStrategy', 'replication_factor':1}");
            session.execute("use " + Configurator.getString(CassandraDataManager.CASSANDRA_DATA_MANAGER_KEYSPACE));
            session.execute("CREATE COLUMNFAMILY test (key varchar PRIMARY KEY, value bigint)");
            session.execute("INSERT INTO test (key, value) VALUES('20130910', 100)");
        } finally {
            session.shutdown();
        }

        cluster.shutdown();
    }

    @AfterClass
    public void stopCassandraCluster() throws Exception {
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    }

    @Test
    public void test() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130910");
        Parameters.TO_DATE.put(context, "20130910");
        ValueData valueData = CassandraDataManager.loadValue(new TestMetric(), context);

        assertEquals(new LongValueData(100), valueData);
    }

    private class TestMetric extends ReadBasedMetric {

        private TestMetric() {
            super("test");
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return LongValueData.class;
        }

        @Override
        public Set<Parameters> getParams() {
            return Collections.emptySet();
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
