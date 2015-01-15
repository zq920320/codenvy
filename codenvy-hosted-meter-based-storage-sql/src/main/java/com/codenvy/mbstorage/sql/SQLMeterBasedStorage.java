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

import com.codenvy.api.account.MemoryUsedMetric;
import com.codenvy.api.account.MeterBasedStorage;
import com.codenvy.api.account.UsageInformer;
import com.codenvy.api.core.ServerException;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Sergii Kabashniuk
 */
public class SQLMeterBasedStorage implements MeterBasedStorage {

    private final String QUERY_INSERT_METRIC = "INSERT INTO METRICS " +
                                               "  (" +
                                               "      AMOUNT," +
                                               "      START_TIME," +
                                               "      STOP_TIME," +
                                               "      USER_ID," +
                                               "      ACCOUNT_ID," +
                                               "      WORKSPACE_ID, " +
                                               "      RUN_ID" +
                                               "  )" +
                                               "    VALUES (?, ?, ?, ?, ? , ?, ?);";

    private final String QUERY_SELECT_METRIC = " SELECT " +
                                               "      AMOUNT," +
                                               "      START_TIME," +
                                               "      STOP_TIME," +
                                               "      USER_ID," +
                                               "      ACCOUNT_ID," +
                                               "      WORKSPACE_ID,  " +
                                               "      RUN_ID " +
                                               "FROM " +
                                               "  METRICS " +
                                               "WHERE ID=?";

    private final String QUERY_SELECT_MEMORY_TOTAL = "SELECT " +
                                                     "   SUM(AMOUNT * (LEAST(?, STOP_TIME) - GREATEST(?, START_TIME)) / (60000)) " +
                                                     "FROM " +
                                                     "  METRICS " +
                                                     "WHERE " +
                                                     "   ACCOUNT_ID=?" +
                                                     "   AND START_TIME<?" +
                                                     "   AND STOP_TIME>?";

    private final String QUERY_SELECT_WS_MEMORY_TOTAL = "SELECT " +
                                                        "   SUM(AMOUNT * (LEAST(?, STOP_TIME) - GREATEST(?, START_TIME)) / (60000)), " +
                                                        "   WORKSPACE_ID " +
                                                        "FROM " +
                                                        "  METRICS " +
                                                        "WHERE " +
                                                        "   ACCOUNT_ID=?" +
                                                        "   AND START_TIME<?" +
                                                        "   AND STOP_TIME>? " +
                                                        "GROUP BY WORKSPACE_ID";


    private final ConnectionFactory connectionFactory;

    @Inject
    public SQLMeterBasedStorage(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public UsageInformer createMemoryUsedRecord(MemoryUsedMetric metric) throws ServerException {
        try (Connection connection = connectionFactory.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(QUERY_INSERT_METRIC, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, metric.getAmount());
                statement.setLong(2, metric.getStartTime());
                statement.setLong(3, metric.getStopTime());
                statement.setString(4, metric.getUserId());
                statement.setString(5, metric.getAccountId());
                statement.setString(6, metric.getWorkspaceId());
                statement.setString(7, metric.getRunId());
                statement.execute();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        return new SQLUsageInformer(keys.getInt(1), connectionFactory);
                    } else {
                        throw new ServerException("Can't find inserted record id");
                    }
                }
            }
        } catch (SQLException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }

    }

    /**
     * Get memory metric from storage.
     *
     * @param id
     *         - id of metrics
     * @return - Memory metric from storage if it exists or null.
     * @throws ServerException
     */
    MemoryUsedMetric getMetric(long id) throws ServerException {
        try (Connection connection = connectionFactory.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(QUERY_SELECT_METRIC)) {
                statement.setLong(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return new MemoryUsedMetric(
                                resultSet.getInt(1),
                                resultSet.getLong(2),
                                resultSet.getLong(3),
                                resultSet.getString(4),
                                resultSet.getString(5),
                                resultSet.getString(6),
                                resultSet.getString(7)
                        );
                    }
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }


    @Override
    public Long getMemoryUsed(String accountId, long from, long until) throws ServerException {
        try (Connection connection = connectionFactory.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(QUERY_SELECT_MEMORY_TOTAL)) {
                statement.setLong(1, until);
                statement.setLong(2, from);
                statement.setString(3, accountId);
                statement.setLong(4, until);
                statement.setLong(5, from);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getLong(1);
                    }
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public Map<String, Long> getMemoryUsedReport(String accountId, long from, long until) throws ServerException {
        Map<String, Long> result = new HashMap<>();
        try (Connection connection = connectionFactory.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(QUERY_SELECT_WS_MEMORY_TOTAL)) {
                statement.setLong(1, until);
                statement.setLong(2, from);
                statement.setString(3, accountId);
                statement.setLong(4, until);
                statement.setLong(5, from);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        result.put(resultSet.getString(2), resultSet.getLong(1));
                    }

                }
            }
        } catch (SQLException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
        return result;
    }

    final static class SQLUsageInformer implements UsageInformer {
        public final String QUERY_UPDATE_METRIC = "UPDATE  METRICS " +
                                                  " SET STOP_TIME=? " +
                                                  " WHERE ID=? ";
        private final long recordId;

        private final ConnectionFactory connectionFactory;

        private boolean isResourceUsageStopped = false;

        private SQLUsageInformer(long recordId, ConnectionFactory connectionFactory) {
            this.recordId = recordId;
            this.connectionFactory = connectionFactory;
        }


        @Override
        public void resourceInUse() throws ServerException {
            if (!isResourceUsageStopped) {
                try (Connection connection = connectionFactory.getConnection()) {
                    try (PreparedStatement statement = connection.prepareStatement(QUERY_UPDATE_METRIC)) {
                        statement.setLong(1, new Date().getTime());
                        statement.setLong(2, recordId);
                        statement.execute();
                    }
                } catch (SQLException e) {
                    throw new ServerException(e.getLocalizedMessage(), e);
                }
            }
        }

        @Override
        public void resourceUsageStopped() throws ServerException {
            resourceInUse();
            isResourceUsageStopped = true;
        }

        long getRecordId() {
            return recordId;
        }
    }
}
