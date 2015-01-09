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

import javax.inject.Inject;
import java.beans.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

/**
 * @author Sergii Kabashniuk
 */
public class SQLMeterBasedStorage implements MeterBasedStorage {

    private final String STM_INSERT = "INSERT INTO METRICS " +
                                      "  (" +
                                      "      AMOUNT," +
                                      "      START_TIME," +
                                      "      STOP_TIME," +
                                      "      USER_ID," +
                                      "      ACCOUNT_ID," +
                                      "      WORKSPACE_ID" +
                                      "  )" +
                                      "    VALUES (?, ?, ?, ?, ? , ?);";
    private final ConnectionFactory connectionFactory;

    @Inject
    public SQLMeterBasedStorage(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public UsageInformer createMemoryUsedRecord(MemoryUsedMetric metric) {
        try (Connection connection = connectionFactory.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(STM_INSERT)) {
                statement.setInt(1, metric.getAmount());
                statement.setTimestamp(2, new Timestamp(metric.getStartTime().getTime()));
                statement.setTimestamp(3, new Timestamp(metric.getStopTime().getTime()));
                statement.setString(4, metric.getUserId());
                statement.setString(5, metric.getAccountId());
                statement.setString(6, metric.getWorkspaceId());
                statement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Long getMemoryUsed(String accountId, Date from, Date until) {
        return null;
    }

    @Override
    public Map<String, Long> getMemoryUsedReport(String accountId, Date from, Date until) {
        return null;
    }
}
