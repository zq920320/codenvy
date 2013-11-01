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
package com.codenvy.analytics.jdbc;

import com.codenvy.analytics.metrics.value.ValueData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractDataManager implements JdbcDataManager {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDataManager.class);

    private final String password;
    private final String url;
    private final String user;

    public AbstractDataManager(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    protected Connection openConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(url, user, password);
        connection.setAutoCommit(false);

        return connection;
    }

    @Override
    public void retainData(String tableName, List<ValueData> fields, List<List<ValueData>> data) throws SQLException {
        Connection connection = openConnection();
        try {
            dropTableIfExists(connection, tableName);
            createTable(connection, tableName, fields, data);
            insertData(connection, tableName, data);

            connection.commit();
            LOG.info("Data have been stored in " + tableName);
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                LOG.error("Can't rollback connection: " + e1.getMessage(), e1);
            }

            throw e;
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                LOG.error("Can't close connection: " + e.getMessage(), e);
            }
        }
    }

    private void insertData(Connection connection, String tableName, List<List<ValueData>> data) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(getInsertQuery(tableName, data.get(0).size()));

        for (List<ValueData> rowData : data) {
            statement.clearParameters();

            for (int i = 0; i < rowData.size(); i++) {
                statement.setString(i + 1, rowData.get(i).getAsString());
            }
            statement.execute();
        }
    }

    private String getInsertQuery(String tableName, int size) {
        StringBuilder builder = new StringBuilder();

        builder.append("INSERT INTO ");
        builder.append(tableName);
        builder.append(" VALUES(");
        for (int i = 0; i < size; i++) {
            builder.append("?");
            if (i != size - 1) {
                builder.append(",");
            }
        }
        builder.append(" );");

        return builder.toString();
    }

    private void createTable(Connection connection,
                             String tableName,
                             List<ValueData> fields,
                             List<List<ValueData>> data) throws SQLException {

        Statement statement = connection.createStatement();
        statement.executeUpdate(getCreateTableQuery(tableName, fields, data));
    }

    private String getCreateTableQuery(String tableName, List<ValueData> fields, List<List<ValueData>> data) {
        StringBuilder builder = new StringBuilder();

        builder.append("CREATE TABLE ");
        builder.append(tableName);
        builder.append(" (");
        for (int i = 0; i < fields.size(); i++) {
            builder.append(fields.get(i).getAsString().replace(" ", "_"));
            builder.append(" VARCHAR(");
            builder.append(getMaxLength(data, i));
            builder.append(")");

            if (i != fields.size() - 1) {
                builder.append(",");
            }
        }
        builder.append(" );");

        return builder.toString();
    }

    private int getMaxLength(List<List<ValueData>> data, int column) {
        int length = 0;
        for (List<ValueData> rowData : data) {
            length = Math.max(length, rowData.get(column).getAsString().length());
        }

        return length;
    }

    private void dropTableIfExists(Connection connection, String tableName) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(getDropTableQuery(tableName));
    }

    private String getDropTableQuery(String tableName) {
        return "DROP TABLE IF EXISTS " + tableName;
    }
}
