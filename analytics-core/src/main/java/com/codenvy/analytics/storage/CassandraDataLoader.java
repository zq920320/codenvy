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
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.Parameters;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class CassandraDataLoader implements DataLoader {

    public static final String CASSANDRA_DATA_LOADER_HOST     = "cassandra.data.loader.host";
    public static final String CASSANDRA_DATA_LOADER_PORT     = "cassandra.data.loader.port";
    public static final String CASSANDRA_DATA_LOADER_KEYSPACE = "cassandra.data.loader.keyspace";
    public static final String CASSANDRA_DATA_LOADER_USER     = "cassandra.data.loader.user";
    public static final String CASSANDRA_DATA_LOADER_PASSWORD = "cassandra.data.loader.password";

    private static final String KEYSPACE = Configurator.getString(CASSANDRA_DATA_LOADER_KEYSPACE);

    public static final String VALUE_COLUMN = "value";

    private final Cluster cluster;

    public CassandraDataLoader() {
        Cluster.Builder builder = Cluster.builder();
        for (String node : Configurator.getArray(CASSANDRA_DATA_LOADER_HOST)) {
            builder.addContactPoint(node);
        }
        builder.withPort(Configurator.getInt(CASSANDRA_DATA_LOADER_PORT));
        builder.withCredentials(Configurator.getString(CASSANDRA_DATA_LOADER_USER),
                                Configurator.getString(CASSANDRA_DATA_LOADER_PASSWORD));

        cluster = builder.build();
    }

    /** {@inheritDoc} */
    @Override
    public ValueData loadValue(Metric metric, Map<String, String> clauses) throws IOException {
        Session session = cluster.connect(KEYSPACE);
        try {
            String query = prepareQuery(metric, clauses);
            ResultSet resultSet = session.execute(query);

            return createdValueData(metric.getValueDataClass(), resultSet);
        } catch (ParseException e) {
            throw new IOException(e);
        } finally {
            session.shutdown();
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getStorageUrl(Map<String, String> context) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("cassandra://");
        stringBuilder.append(Configurator.getString(CASSANDRA_DATA_LOADER_USER));
        stringBuilder.append(":");
        stringBuilder.append(Configurator.getString(CASSANDRA_DATA_LOADER_PASSWORD));
        stringBuilder.append("@");
        stringBuilder.append(Configurator.getString(CASSANDRA_DATA_LOADER_KEYSPACE));
        stringBuilder.append("/");

        return stringBuilder.toString();
    }

    private String prepareQuery(Metric metric, Map<String, String> clauses) throws ParseException {
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
                if (Parameters.TO_DATE.get(clauses).equals(Parameters.FROM_DATE.get(clauses))) {
                    builder.append("key=");
                    builder.append('\'');
                    builder.append(entry.getValue());
                    builder.append('\'');
                } else {
                    builder.append("key IN (");

                    Calendar fromDate = Utils.getFromDate(clauses);
                    Calendar toDate = Utils.getToDate(clauses);
                    while (!fromDate.after(toDate)) {
                        builder.append('\'');
                        builder.append(Utils.formatDate(fromDate));
                        builder.append('\'');

                        if (!fromDate.equals(toDate)) {
                            builder.append(',');
                        }

                        fromDate.add(Calendar.DAY_OF_MONTH, 1);
                    }

                    builder.append(")");
                }
            }
        }

        return builder.toString();
    }

    private ValueData createdValueData(Class<? extends ValueData> clazz, ResultSet resultSet) {
        if (clazz == LongValueData.class) {
            return createLongValueData(resultSet);
        }

        throw new IllegalArgumentException("Unknown class " + clazz.getName());
    }

    private ValueData createLongValueData(ResultSet resultSet) {
        long value = 0;
        for (Row row : resultSet) {
            value += row.getLong(VALUE_COLUMN);
        }

        return new LongValueData(value);
    }
}
