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
import com.codenvy.api.account.billing.PaymentState;
import com.codenvy.api.account.shared.dto.Charge;
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
import java.util.UUID;

/**
 * @author Sergii Kabashniuk
 */
public class SqlBillingService implements BillingService {
    private final ConnectionFactory connectionFactory;

    private final static String MEMORY_CHARGES_INSERT =
            "INSERT INTO " +
            "  MEMORY_CHARGES (" +
            "                   AMOUNT, " +
            "                   ACCOUNT_ID, " +
            "                   WORKSPACE_ID,  " +
            "                   CALC_ID " +
            "                  ) " +
            "SELECT " +
            "   ROUND(SUM(AMOUNT * (LEAST(?, STOP_TIME) - GREATEST(?, START_TIME)) / (60000))/61440,2) AS AMOUNT, " +
            "   ACCOUNT_ID, " +
            "   WORKSPACE_ID,  " +
            "   ? AS CALC_ID  " +
            "FROM " +
            "  METRICS " +
            "WHERE " +
            "   START_TIME<?" +
            "   AND STOP_TIME>?" +
            "GROUP BY " +
            " ACCOUNT_ID, " +
            " WORKSPACE_ID ";


    private final static String FREE_SAAS_CHARGES_INSERT =
            "INSERT INTO " +
            "   CHARGES (" +
            "                   AMOUNT, " +
            "                   ACCOUNT_ID, " +
            "                   SERVICE_ID, " +
            "                   TYPE, " +
            "                   PRICE, " +
            "                   CALC_ID " +
            "                  ) " +
            "SELECT " +
            "   LEAST(SUM(AMOUNT), ?) AS AMOUNT, " +
            "   ACCOUNT_ID AS ACCOUNT_ID, " +
            "   ? AS SERVICE_ID, " +
            "   ? AS TYPE, " +
            "   0 AS PRICE, " +
            "   ? as CALC_ID " +
            "FROM " +
            "  MEMORY_CHARGES " +
            "WHERE " +
            "  CALC_ID = ? " +
            "GROUP BY " +
            "  ACCOUNT_ID ";


    private final static String PAID_SAAS_CHARGES_INSERT =
            "INSERT INTO " +
            "   CHARGES (" +
            "                   AMOUNT, " +
            "                   ACCOUNT_ID, " +
            "                   SERVICE_ID, " +
            "                   TYPE, " +
            "                   PRICE, " +
            "                   CALC_ID " +
            "                  ) " +
            "SELECT " +
            "   SUM(AMOUNT)-? AS AMOUNT, " +
            "   ACCOUNT_ID AS ACCOUNT_ID, " +
            "   ? AS SERVICE_ID, " +
            "   ? AS TYPE, " +
            "   ? AS PRICE, " +
            "   ? as CALC_ID " +
            "FROM " +
            "  MEMORY_CHARGES " +
            "WHERE " +
            "  CALC_ID = ? " +
            "GROUP BY " +
            "  ACCOUNT_ID " +
            "HAVING  " +
            " SUM(AMOUNT) >= ? ";


    private final static String RECEIPTS_INSERT =
            "INSERT INTO " +
            "   RECEIPTS (" +
            "                   TOTAL, " +
            "                   ACCOUNT_ID, " +
            "                   PAYMENT_STATUS, " +
            "                   FROM_TIME, " +
            "                   TILL_TIME, " +
            "                   CALC_ID " +
            "                  ) " +
            "SELECT " +
            "   ROUND(SUM(AMOUNT*PRICE), 2) AS TOTAL, " +
            "   ACCOUNT_ID AS ACCOUNT_ID, " +
            "   ? as PAYMENT_STATUS, " +
            "   ? as FROM_TIME, " +
            "   ? as TILL_TIME, " +
            "   ? as CALC_ID " +
            "FROM " +
            "  CHARGES " +
            "WHERE " +
            "  CALC_ID = ? " +
            "GROUP BY " +
            "  ACCOUNT_ID ";

    private final static String RECEIPTS_SELECT =
            "SELECT " +
            "                   ID, " +
            "                   TOTAL, " +
            "                   ACCOUNT_ID, " +
            "                   FROM_TIME, " +
            "                   TILL_TIME, " +
            "                   CALC_ID " +
            "FROM " +
            "  RECEIPTS " +
            "WHERE " +
            " PAYMENT_STATUS = ? " +
            " LIMIT ?";

    private final static String CHARGES_SELECT =
            "SELECT " +
            "                   AMOUNT, " +
            "                   SERVICE_ID, " +
            "                   TYPE, " +
            "                   PRICE " +
            "FROM " +
            "  CHARGES " +
            "WHERE " +
            " ACCOUNT_ID  = ? " +
            " AND CALC_ID = ? ";


    @Inject
    public SqlBillingService(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void generateReceipts(long from, long till) throws ServerException {
        String calculationId = UUID.randomUUID().toString();

        try (Connection connection = connectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try {
                //calculate memory usage statistic
                try (PreparedStatement memoryChargesStatement = connection.prepareStatement(MEMORY_CHARGES_INSERT)) {
                    memoryChargesStatement.setLong(1, till);
                    memoryChargesStatement.setLong(2, from);
                    memoryChargesStatement.setString(3, calculationId);
                    memoryChargesStatement.setLong(4, till);
                    memoryChargesStatement.setLong(5, from);
                    memoryChargesStatement.execute();
                }

                try (PreparedStatement freeSaasCharges = connection.prepareStatement(FREE_SAAS_CHARGES_INSERT)) {
                    freeSaasCharges.setDouble(1, 10.0);
                    freeSaasCharges.setString(2, "Saas");
                    freeSaasCharges.setString(3, "Free");
                    freeSaasCharges.setString(4, calculationId);
                    freeSaasCharges.setString(5, calculationId);

                    freeSaasCharges.execute();
                }
                try (PreparedStatement paidSaasCharges = connection.prepareStatement(PAID_SAAS_CHARGES_INSERT)) {
                    paidSaasCharges.setDouble(1, 10.0);
                    paidSaasCharges.setString(2, "Saas");
                    paidSaasCharges.setString(3, "Paid");
                    paidSaasCharges.setDouble(4, 0.15);
                    paidSaasCharges.setString(5, calculationId);
                    paidSaasCharges.setString(6, calculationId);
                    paidSaasCharges.setDouble(7, 10.0);
                    paidSaasCharges.execute();
                }

                try (PreparedStatement receipts = connection.prepareStatement(RECEIPTS_INSERT)) {
                    receipts.setInt(1, PaymentState.WAITING_EXECUTOR.getState());
                    receipts.setLong(2, from);
                    receipts.setLong(3, till);
                    receipts.setString(4, calculationId);
                    receipts.setString(5, calculationId);
                    receipts.execute();
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
            try (PreparedStatement statement = connection.prepareStatement(RECEIPTS_SELECT)) {
                statement.setInt(1, PaymentState.WAITING_EXECUTOR.getState());
                statement.setLong(2, limit);
                try (ResultSet receiptsResultSet = statement.executeQuery()) {
                    List<Receipt> result = new ArrayList<>(limit);
                    while (receiptsResultSet.next()) {
                        List<Charge> charges = new ArrayList<>();

                        try (PreparedStatement chargesStatement = connection.prepareStatement(CHARGES_SELECT)) {
                            chargesStatement.setString(1, receiptsResultSet.getString(3));
                            chargesStatement.setString(2, receiptsResultSet.getString(6));
                            try (ResultSet chargesResultSet = chargesStatement.executeQuery()) {
                                while (chargesResultSet.next()) {
                                    charges.add(DtoFactory.getInstance().createDto(Charge.class)
                                                          .withAmount(chargesResultSet.getDouble(1))
                                                          .withServiceId(chargesResultSet.getString(2))
                                                          .withType(chargesResultSet.getString(3))
                                                          .withPrice(chargesResultSet.getDouble(4))
                                               );
                                }
                            }

                        }


                        result.add(DtoFactory.getInstance().createDto(Receipt.class)
                                             .withId(receiptsResultSet.getLong(1))
                                             .withTotal(receiptsResultSet.getDouble(2))
                                             .withAccountId(receiptsResultSet.getString(3))
                                             .withCharges(charges)
                                  );


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
