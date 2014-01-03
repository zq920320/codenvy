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
package com.codenvy.analytics.persistent;

import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractJDBCDataPersister implements DataPersister {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractJDBCDataPersister.class);

    private final DecimalFormat colFormat = new DecimalFormat("00");

    private final String     password;
    private final String     url;
    private final String     user;
    private final DataSource ds;

    public AbstractJDBCDataPersister(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.ds = null;
    }

    public AbstractJDBCDataPersister(DataSource ds) {
        this.ds = ds;
        this.password = null;
        this.user = null;
        this.url = null;
    }

    protected Connection openConnection() throws SQLException {
        Connection connection;

        if (ds != null) {
            connection = ds.getConnection();
        } else {
            connection = DriverManager.getConnection(url, user, password);
        }

        connection.setAutoCommit(false);
        return connection;
    }

    @Override
    public List<List<ValueData>> loadData(String tableName) throws SQLException, IOException {
        List<List<ValueData>> result = new ArrayList<>();

        try (Connection connection = openConnection()) {
            ResultSet resultSet = selectData(connection, tableName);
            while (resultSet.next()) {

                int size = resultSet.getMetaData().getColumnCount();
                List<ValueData> rowData = new ArrayList<>(size);

                for (int i = 0; i < size; i++) {
                    rowData.add(new StringValueData(resultSet.getString(i + 1)));
                }

                result.add(rowData);
            }
        }

        return result;
    }

    @Override
    public void storeData(Map<String, List<List<ValueData>>> viewData) throws SQLException {
        Connection connection = openConnection();
        try {
            for (Map.Entry<String, List<List<ValueData>>> section : viewData.entrySet()) {
                String tableName = section.getKey();
                List<List<ValueData>> data = section.getValue();

                dropTableIfExists(connection, tableName);
                createTable(connection, tableName, data);
                insertData(connection, tableName, data);

                connection.commit();

                LOG.info("Data have been stored in " + tableName);
            }

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

    private ResultSet selectData(Connection connection, String tableName) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(getSelectQuery(tableName));
        return statement.executeQuery();
    }

    private void insertData(Connection connection,
                            String tableName,
                            List<List<ValueData>> data) throws SQLException {

        PreparedStatement statement = connection.prepareStatement(getInsertQuery(tableName, data.get(0).size()));

        for (List<ValueData> rowData : data) {
            if (!rowData.isEmpty()) {
                statement.clearParameters();

                for (int i = 0; i < rowData.size(); i++) {
                    statement.setString(i + 1, rowData.get(i).getAsString());
                }
                statement.execute();
            }
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

    private String getSelectQuery(String tableName) {
        return "SELECT * FROM " + tableName;
    }

    private void createTable(Connection connection,
                             String tableName,
                             List<List<ValueData>> data) throws SQLException {

        Statement statement = connection.createStatement();
        statement.executeUpdate(getCreateTableQuery(tableName, data));
    }

    private String getCreateTableQuery(String tableName, List<List<ValueData>> data) {
        StringBuilder builder = new StringBuilder();

        builder.append("CREATE TABLE ");
        builder.append(tableName);
        builder.append(" (");

        int colCount = data.get(0).size();

        for (int i = 0; i < colCount; i++) {
            builder.append("COL_");
            builder.append(colFormat.format(i));
            builder.append(" VARCHAR(");
            builder.append(getMaxLength(data, i));
            builder.append(")");

            if (i != colCount - 1) {
                builder.append(",");
            }
        }
        builder.append(" );");

        return builder.toString();
    }

    private int getMaxLength(List<List<ValueData>> data, int column) {
        int length = 0;

        for (List<ValueData> rowData : data) {
            if (!rowData.isEmpty()) {
                length = Math.max(length, rowData.get(column).getAsString().length());
            }
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
