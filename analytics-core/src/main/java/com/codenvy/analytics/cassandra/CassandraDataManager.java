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


package com.codenvy.analytics.cassandra;

import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.value.*;
import com.datastax.driver.core.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class CassandraDataManager {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraDataManager.class);

    public static final String CASSANDRA_ANALYTICS_HOST = "cassandra.analytics.host";

    public static final String CASSANDRA_ANALYTICS_PORT = "cassandra.analytics.port";

    public static final String CASSANDRA_ANALYTICS_KEYSPACE = "cassandra.analytics.keyspace";

    public static final String CASSANDRA_ANALYTICS_USER = "cassandra.analytics.user";

    public static final String CASSANDRA_ANALYTICS_PASSWORD = "cassandra.analytics.password";

    private static final String KEYSPACE = Configurator.getString(CASSANDRA_ANALYTICS_KEYSPACE);

    private static Cluster cluster;

    static {
        Cluster.Builder builder = Cluster.builder();
        for (String node : Configurator.getArray(CASSANDRA_ANALYTICS_HOST)) {
            builder.addContactPoint(node);
        }
        builder.withPort(Configurator.getInt(CASSANDRA_ANALYTICS_PORT));
        builder.withCredentials(Configurator.getString(CASSANDRA_ANALYTICS_USER),
                                Configurator.getString(CASSANDRA_ANALYTICS_PASSWORD));

        cluster = builder.build();

        LOGGER.info("Cassandra driver is started");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                cluster.shutdown();
                LOGGER.info("Cassandra driver is shutdown");
            }
        });
    }

    /** {@inheritDoc} */
    public static ValueData loadValue(Metric metric, Map<String, String> clauses) throws IOException {
        validateDateParams(clauses);

        Session session = cluster.connect(KEYSPACE);
        try {
            String query = prepareQuery(metric, clauses);
            ResultSet resultSet = session.execute(query);
            List<RowValueData> rows = readRows(resultSet);

            return createdValueData(metric.getValueDataClass(), rows);
        } finally {
            session.shutdown();
        }
    }

    private static String prepareQuery(Metric metric, Map<String, String> clauses) {
        StringBuilder builder = new StringBuilder();

        builder.append("SELECT * FROM");
        builder.append(' ');
        builder.append(metric.getName().toLowerCase());
        builder.append(' ');
        builder.append("WHERE");
        builder.append(' ');

        for (Map.Entry<String, String> entry : clauses.entrySet()) {
            String param = entry.getKey();

            if (Parameters.TO_DATE.toString().equals(param)) {
                builder.append("key=");
                builder.append('\'');
                builder.append(entry.getValue());
                builder.append('\'');
            }
        }

        return builder.toString();
    }

    /**
     * Makes sure that {@link com.codenvy.analytics.metrics.Parameters#TO_DATE} and {@link
     * com.codenvy.analytics.metrics.Parameters#FROM_DATE} are the same, otherwise {@link IllegalStateException}
     * will be thrown.
     */
    private static void validateDateParams(Map<String, String> clauses) throws IllegalStateException {
        if (!Parameters.TO_DATE.exists(clauses) || !Parameters.FROM_DATE.exists(clauses) ||
            !Parameters.TO_DATE.get(clauses).equals(Parameters.FROM_DATE.get(clauses))) {

            throw new IllegalStateException("The date params are different or absent in context");
        }
    }

    private static ValueData createdValueData(Class<? extends ValueData> clazz, List<RowValueData> rows) {
        if (clazz == LongValueData.class || clazz == DoubleValueData.class || clazz == StringValueData.class) {
            if (rows.size() == 1) {
                if (rows.get(0).size() == 1) {
                    ValueData valueData = rows.get(0).getAll().values().iterator().next();

                    if (valueData.getClass() == clazz) {
                        return valueData;
                    } else {
                        throw new IllegalArgumentException(
                                "Row contains " + valueData.getClass() + " instead of " + clazz);
                    }
                } else {
                    throw new IllegalArgumentException(
                            "Wrong data format for " + clazz.getName() + ". The number of columns is greater than 1");
                }
            } else {
                throw new IllegalArgumentException(
                        "Wrong data format for " + clazz.getName() + ". The number of rows is greater than 1");
            }

        } else if (clazz == RowValueData.class) {
            if (rows.size() == 1) {
                return rows.get(0);
            } else {
                throw new IllegalArgumentException(
                        "Wrong data format for " + clazz.getName() + ". The number of rows is greater than 1");
            }

        } else if (clazz == ListValueData.class) {
            return new ListValueData(rows);
        }

        throw new IllegalArgumentException("Unknown class " + clazz.getName());
    }

    private static List<RowValueData> readRows(ResultSet resultSet) {
        List<RowValueData> rows = new ArrayList<>();

        Iterator<Row> iterator = resultSet.iterator();
        while (iterator.hasNext()) {
            Row row = iterator.next();
            ColumnDefinitions definitions = row.getColumnDefinitions();

            Map<String, ValueData> map = new HashMap<>(definitions.size());

            for (int i = 1; i < definitions.size(); i++) {
                ValueData valueData;

                DataType type = definitions.getType(i);
                switch (type.getName()) {
                    case BIGINT:
                        valueData = new LongValueData(row.getLong(i));
                        break;
                    case DOUBLE:
                        valueData = new DoubleValueData(row.getDouble(i));
                        break;
                    case TEXT:
                        valueData = new StringValueData(row.getString(i));
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported data type " + type.getName());
                }

                map.put(definitions.getName(i), valueData);
            }

            rows.add(new RowValueData(map));
        }

        return rows;
    }
}
