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
package com.codenvy.api.dao.sql;
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

import com.codenvy.api.account.billing.BillingPeriod;
import com.codenvy.api.account.billing.Period;
import com.codenvy.api.account.metrics.MemoryUsedMetric;
import com.codenvy.api.account.metrics.MeterBasedStorage;
import com.codenvy.api.account.metrics.UsageInformer;
import com.codenvy.api.core.ServerException;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * @author Sergii Kabashniuk
 */
public class SqlMeterBasedStorage implements MeterBasedStorage {

    private final String QUERY_INSERT_METRIC = "INSERT INTO METRICS " +
                                               "  (" +
                                               "      AMOUNT," +
                                               "      START_TIME," +
                                               "      STOP_TIME," +
                                               "      USER_ID," +
                                               "      ACCOUNT_ID," +
                                               "      WORKSPACE_ID, " +
                                               "      BILLING_PERIOD," +
                                               "      RUN_ID" +
                                               "  )" +
                                               "    VALUES (?, ?, ?, ?, ?, ? , ?, ?);";

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

    private final String QUERY_SELECT_METRIC_RUN_ID = " SELECT " +
                                                      "      AMOUNT," +
                                                      "      START_TIME," +
                                                      "      STOP_TIME," +
                                                      "      USER_ID," +
                                                      "      ACCOUNT_ID," +
                                                      "      WORKSPACE_ID,  " +
                                                      "      RUN_ID " +
                                                      "FROM " +
                                                      "  METRICS " +
                                                      "WHERE " +
                                                      "  RUN_ID=? " +
                                                      "ORDER BY " +
                                                      "  START_TIME";


    private final String QUERY_SELECT_MEMORY_TOTAL = "SELECT " +
                                                     "   SUM(AMOUNT * (LEAST(?, STOP_TIME) - GREATEST(?, START_TIME))" +
                                                     " / (60000)) " +
                                                     "FROM " +
                                                     "  METRICS " +
                                                     "WHERE " +
                                                     "   ACCOUNT_ID=?" +
                                                     "   AND START_TIME<?" +
                                                     "   AND STOP_TIME>?";

    private final String QUERY_SELECT_WS_MEMORY_TOTAL = "SELECT " +
                                                        "   SUM(AMOUNT * (LEAST(?, STOP_TIME) - GREATEST(?, " +
                                                        "START_TIME)) / (60000)), " +
                                                        "   WORKSPACE_ID " +
                                                        "FROM " +
                                                        "  METRICS " +
                                                        "WHERE " +
                                                        "   ACCOUNT_ID=?" +
                                                        "   AND START_TIME<?" +
                                                        "   AND STOP_TIME>? " +
                                                        "GROUP BY WORKSPACE_ID";


    private final ConnectionFactory connectionFactory;
    private final BillingPeriod     billingPeriod;

    @Inject
    public SqlMeterBasedStorage(ConnectionFactory connectionFactory, BillingPeriod billingPeriod) {
        this.connectionFactory = connectionFactory;
        this.billingPeriod = billingPeriod;
    }

    @Override
    public UsageInformer createMemoryUsedRecord(MemoryUsedMetric metric) throws ServerException {
        if (metric.getStopTime() < metric.getStartTime()) {
            throw new ServerException("Stop time can't be less then start time");
        }
        if (billingPeriod.get(new Date(metric.getStartTime())).getNextPeriod().getEndDate().getTime() <
            metric.getStopTime()) {
            throw new ServerException("Stop time should be in the same or next billing period with start time");
        }


        try (Connection connection = connectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection
                    .prepareStatement(QUERY_INSERT_METRIC, Statement.RETURN_GENERATED_KEYS)) {
                try {
                    long lastRecordId = -1;
                    long startTime = metric.getStartTime();
                    Period period = billingPeriod.get(new Date(startTime));
                    long stopTime = Math.min(metric.getStopTime(),
                                             period.getEndDate().getTime());

                    while (startTime < stopTime && stopTime <= metric.getStopTime()) {

                        lastRecordId = doCreateMemoryRecord(statement,
                                                            new MemoryUsedMetric(metric.getAmount(), startTime,
                                                                                 stopTime,
                                                                                 metric.getUserId(),
                                                                                 metric.getAccountId(),
                                                                                 metric.getWorkspaceId(),
                                                                                 metric.getRunId()));
                        period = period.getNextPeriod();
                        startTime = period.getStartDate().getTime();
                        stopTime = Math.min(metric.getStopTime(),
                                            period.getEndDate().getTime());


                    }
                    connection.commit();
                    return new SQLUsageInformer(lastRecordId,
                                                metric,
                                                connectionFactory);
                } catch (SQLException | ServerException e) {
                    connection.rollback();
                    throw new ServerException(e.getLocalizedMessage(), e);
                }
            }
        } catch (SQLException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }

    }

    private long doCreateMemoryRecord(PreparedStatement statement, MemoryUsedMetric metric)
            throws SQLException, ServerException {
        String billingPeriod = this.billingPeriod.get(new Date(metric.getStartTime())).getId();
        if (!billingPeriod
                .equals(this.billingPeriod.get(new Date(metric.getStopTime())).getId())) {
            throw new ServerException("Start and stop time should be in same billing period");
        }

        statement.setInt(1, metric.getAmount());
        statement.setLong(2, metric.getStartTime());
        statement.setLong(3, metric.getStopTime());
        statement.setString(4, metric.getUserId());
        statement.setString(5, metric.getAccountId());
        statement.setString(6, metric.getWorkspaceId());
        statement.setString(7, billingPeriod);
        statement.setString(8, metric.getRunId());
        statement.execute();
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getInt(1);
            } else {
                throw new ServerException("Can't find inserted record id");
            }
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

    /**
     * Get memory metric from storage.
     *
     * @param runId
     *         - id of run.
     * @return - Memory metric from storage if it exists or null.
     * @throws ServerException
     */
    List<MemoryUsedMetric> getMetricsByRunId(String runId) throws ServerException {
        try (Connection connection = connectionFactory.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(QUERY_SELECT_METRIC_RUN_ID)) {
                statement.setString(1, runId);
                List<MemoryUsedMetric> result = new ArrayList<>();
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        result.add(new MemoryUsedMetric(
                                resultSet.getInt(1),
                                resultSet.getLong(2),
                                resultSet.getLong(3),
                                resultSet.getString(4),
                                resultSet.getString(5),
                                resultSet.getString(6),
                                resultSet.getString(7)
                        ));
                    }
                    return result;
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

    final class SQLUsageInformer implements UsageInformer {
        public final String QUERY_UPDATE_METRIC = "UPDATE  METRICS " +
                                                  " SET STOP_TIME=? " +
                                                  " WHERE ID=? ";
        private long recordId;

        private       Period           currentBillingPeriod;
        private final MemoryUsedMetric metric;

        private final ConnectionFactory connectionFactory;

        private boolean isResourceUsageStopped = false;

        private SQLUsageInformer(long recordId, MemoryUsedMetric metric, ConnectionFactory connectionFactory) {
            this.recordId = recordId;
            this.metric = metric;
            this.currentBillingPeriod =
                    billingPeriod.get(new Date(metric.getStopTime()));
            this.connectionFactory = connectionFactory;
        }


        @Override
        public void resourceInUse() throws ServerException {
            if (!isResourceUsageStopped) {
                try (Connection connection = connectionFactory.getConnection()) {
                    try {
                        connection.setAutoCommit(false);
                        Date now = new Date();
                        //in the same initial billing period.
                        if (now.before(currentBillingPeriod.getEndDate())) {
                            try (PreparedStatement statement = connection.prepareStatement(QUERY_UPDATE_METRIC)) {
                                statement.setLong(1, new Date().getTime());
                                statement.setLong(2, recordId);
                                statement.execute();
                            }
                        } else {
                            Period nextBillingPeriod = currentBillingPeriod.getNextPeriod();
                            //jump to the next billing period.
                            if (now.before(nextBillingPeriod.getEndDate())) {
                                //close previous record
                                try (PreparedStatement statement = connection.prepareStatement(QUERY_UPDATE_METRIC)) {
                                    statement.setLong(1, currentBillingPeriod.getEndDate().getTime());
                                    statement.setLong(2, recordId);
                                    statement.execute();
                                }

                                try (PreparedStatement statement = connection
                                        .prepareStatement(QUERY_INSERT_METRIC, Statement.RETURN_GENERATED_KEYS)) {

                                    recordId = doCreateMemoryRecord(statement,
                                                                    new MemoryUsedMetric(metric.getAmount(),
                                                                                         currentBillingPeriod
                                                                                                 .getStartDate()
                                                                                                 .getTime(),
                                                                                         now.getTime(),
                                                                                         metric.getUserId(),
                                                                                         metric.getAccountId(),
                                                                                         metric.getWorkspaceId(),
                                                                                         metric.getRunId()));
                                }
                                currentBillingPeriod = nextBillingPeriod;
                            } else {
                                throw new RuntimeException(
                                        "UsageInformer is out of date for more then one billing period");
                            }
                        }
                        connection.commit();
                    } catch (SQLException e) {
                        connection.rollback();
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
