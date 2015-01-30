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

import com.codenvy.api.account.billing.BillingService;
import com.codenvy.api.account.metrics.MemoryUsedMetric;
import com.codenvy.api.account.shared.dto.Receipt;
import com.codenvy.api.core.ServerException;
import com.codenvy.dto.server.DtoFactory;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergii Kabashniuk
 */
public class SqlBillingService implements BillingService {
    private final ConnectionFactory connectionFactory;

    private final static String MEMORY_CHARGES_INSERT =
            "INSERT INTO " +
            "  MEMORY_CHARGES (" +
            "                   AMOUNT, " +
            "                   PRICE, " +
            "                   ACCOUNT_ID, " +
            "                   WORKSPACE_ID " +
            "                  ) " +
            "SELECT " +
            "   SUM(AMOUNT * (LEAST(?, STOP_TIME) - GREATEST(?, START_TIME)) / (60000))/61440 AS AMOUNT, " +
            "   0.0 as PRICE, " +
            " ACCOUNT_ID, " +
            " WORKSPACE_ID " +
            "FROM " +
            "  METRICS " +
            "WHERE " +
            "   START_TIME<?" +
            "   AND STOP_TIME>?" +
            "GROUP BY " +
            " ACCOUNT_ID, " +
            " WORKSPACE_ID ";

    private final static String MEMORY_CHARGES_UPDATE_PRICE =
            "UPDATE " +
            "   MEMORY_CHARGES " +
            "SET " +
            "   PRICE = ?" +
            "WHERE " +
            "  AMOUNT>? ";

    private final static String RECEIPTS_INSERT =
            "INSERT INTO " +
            "   RECEIPTS (" +
            "                   TOTAL, " +
            "                   ACCOUNT_ID, " +
            "                   PAYMENT_STATUS " +
            "                  ) " +
            "SELECT " +
            "   AMOUNT*PRICE AS TOTAL, " +
            "   ACCOUNT_ID AS ACCOUNT_ID, " +
            "   0 as PAYMENT_STATUS " +
            "FROM " +
            "  MEMORY_CHARGES ";

    private final static String RECEIPTS_SELECT_UNPAID =
            "SELECT " +
            "   * " +
            "FROM " +
            "  RECEIPTS " +
            "WHERE " +
            " PAYMENT_TIME IS NULL "+
            " LIMIT ?"

            ;


    @Inject
    public SqlBillingService(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void generateReceipts(String billingPeriodId) throws ServerException {
        try (Connection connection = connectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement memoryChargesStatement = connection.prepareStatement(MEMORY_CHARGES_INSERT)) {
                    memoryChargesStatement.setLong(1, Long.MAX_VALUE);
                    memoryChargesStatement.setLong(2, Long.MIN_VALUE);
                    memoryChargesStatement.setLong(3, Long.MAX_VALUE);
                    memoryChargesStatement.setLong(4, Long.MIN_VALUE);
                    memoryChargesStatement.execute();
                }
                try (PreparedStatement updateMemoryChargesStatement = connection.prepareStatement(MEMORY_CHARGES_UPDATE_PRICE)) {
                    updateMemoryChargesStatement.setDouble(1, 2.8);
                    updateMemoryChargesStatement.setDouble(2, 10.0);
                    updateMemoryChargesStatement.execute();
                }

                try (PreparedStatement receiptInsertStatement = connection.prepareStatement(RECEIPTS_INSERT)) {
                    receiptInsertStatement.execute();
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new ServerException(e.getLocalizedMessage(), e);
            }
        } catch (SQLException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }

    }

    @Override
    public List<Receipt> getUnpaidReceipt(int limit) throws ServerException {
        try (Connection connection = connectionFactory.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(RECEIPTS_SELECT_UNPAID)) {
                statement.setLong(1, limit);
                try (ResultSet resultSet = statement.executeQuery()) {
                    List<Receipt> result = new ArrayList<>(limit);
                    while (resultSet.next()) {
                        //result.add(DtoFactory.getInstance().createDto(Receipt.class).withAccountId());
                    }
                    return result;
                }
            }
        } catch (SQLException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void setPaidStatus(long receiptId, int status) {

    }

    @Override
    public List<Receipt> getNotSendReceipt(int limit) {
        return null;
    }

    @Override
    public void markReceiptAsSent(long receiptId) {

    }
}
