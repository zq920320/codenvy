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

import static com.codenvy.api.dao.sql.SqlDaoQueries.CHARGES_FREE_INSERT;
import static com.codenvy.api.dao.sql.SqlDaoQueries.CHARGES_PAID_INSERT;
import static com.codenvy.api.dao.sql.SqlDaoQueries.CHARGES_SELECT;
import static com.codenvy.api.dao.sql.SqlDaoQueries.MEMORY_CHARGES_INSERT;
import static com.codenvy.api.dao.sql.SqlDaoQueries.MEMORY_CHARGES_SELECT;
import static com.codenvy.api.dao.sql.SqlDaoQueries.RECEIPTS_ACCOUNT_SELECT;
import static com.codenvy.api.dao.sql.SqlDaoQueries.RECEIPTS_INSERT;
import static com.codenvy.api.dao.sql.SqlDaoQueries.RECEIPTS_MAILING_STATE_SELECT;
import static com.codenvy.api.dao.sql.SqlDaoQueries.RECEIPTS_MAILING_TIME_UPDATE;
import static com.codenvy.api.dao.sql.SqlDaoQueries.RECEIPTS_PAYMENT_STATE_SELECT;
import static com.codenvy.api.dao.sql.SqlDaoQueries.RECEIPTS_PAYMENT_STATUS_UPDATE;

import com.codenvy.api.account.billing.BillingService;
import com.codenvy.api.account.billing.PaymentState;
import com.codenvy.api.account.impl.shared.dto.Charge;
import com.codenvy.api.account.impl.shared.dto.Invoice;
import com.codenvy.api.account.shared.dto.MemoryChargeDetails;
import com.codenvy.api.core.ServerException;
import com.codenvy.dto.server.DtoFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Database driving BillingService.
 *
 * @author Sergii Kabashniuk
 */
public class SqlBillingService implements BillingService {


    private final ConnectionFactory connectionFactory;
    private final double            saasChargeableGbHPrice;
    private final double            saasFreeGbH;


    @Inject
    public SqlBillingService(ConnectionFactory connectionFactory,
                             @Named("billing.saas.chargeable.gbh.price") Double saasChargeableGbHPrice,
                             @Named("billing.saas.free.gbh") Double saasFreeGbH

                            ) {
        this.connectionFactory = connectionFactory;
        this.saasChargeableGbHPrice = saasChargeableGbHPrice;
        this.saasFreeGbH = saasFreeGbH;
    }

    @Override
    public void generateInvoices(long from, long till) throws ServerException {
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

                try (PreparedStatement freeSaasCharges = connection.prepareStatement(CHARGES_FREE_INSERT)) {
                    freeSaasCharges.setDouble(1, saasFreeGbH);
                    freeSaasCharges.setString(2, "Saas");
                    freeSaasCharges.setString(3, "Free");
                    freeSaasCharges.setString(4, calculationId);
                    freeSaasCharges.setString(5, calculationId);

                    freeSaasCharges.execute();
                }
                try (PreparedStatement paidSaasCharges = connection.prepareStatement(CHARGES_PAID_INSERT)) {
                    paidSaasCharges.setDouble(1, saasFreeGbH);
                    paidSaasCharges.setString(2, "Saas");
                    paidSaasCharges.setString(3, "Paid");
                    paidSaasCharges.setDouble(4, saasChargeableGbHPrice);
                    paidSaasCharges.setString(5, calculationId);
                    paidSaasCharges.setString(6, calculationId);
                    paidSaasCharges.setDouble(7, saasFreeGbH);
                    paidSaasCharges.execute();
                }

                try (PreparedStatement invoices = connection.prepareStatement(RECEIPTS_INSERT)) {
                    invoices.setInt(1, PaymentState.WAITING_EXECUTOR.getState());
                    invoices.setLong(2, from);
                    invoices.setLong(3, till);
                    invoices.setString(4, calculationId);
                    invoices.setString(5, calculationId);
                    invoices.execute();
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
    public void setPaymentState(long invoiceId, PaymentState state) throws ServerException {
        try (Connection connection = connectionFactory.getConnection()) {
            try {
                connection.setAutoCommit(false);
                try (PreparedStatement statement = connection.prepareStatement(RECEIPTS_PAYMENT_STATUS_UPDATE)) {
                    statement.setInt(1, state.getState());
                    statement.setLong(2, invoiceId);
                    statement.execute();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
            }
        } catch (SQLException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }


    @Override
    public List<Invoice> getInvoices(PaymentState state, int limit) throws ServerException {
        try (Connection connection = connectionFactory.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(RECEIPTS_PAYMENT_STATE_SELECT)) {
                statement.setInt(1, state.getState());
                statement.setLong(2, limit);
                try (ResultSet invoicesResultSet = statement.executeQuery()) {
                    List<Invoice> result = new ArrayList<>(limit);
                    while (invoicesResultSet.next()) {

                        result.add(DtoFactory.getInstance().createDto(Invoice.class)
                                             .withId(invoicesResultSet.getLong(1))
                                             .withTotal(invoicesResultSet.getDouble(2))
                                             .withAccountId(invoicesResultSet.getString(3))
                                             .withCharges(
                                                     getCharges(connection, invoicesResultSet.getString(3), invoicesResultSet.getString(6)))
                                             .withMemoryChargeDetails(getMemoryChargeDetails(connection, invoicesResultSet.getString(3),
                                                                                             invoicesResultSet.getString(6)))
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
    public List<Invoice> getInvoices(String accountId) throws ServerException {
        try (Connection connection = connectionFactory.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(RECEIPTS_ACCOUNT_SELECT)) {
                statement.setString(1, accountId);
                try (ResultSet invoicesResultSet = statement.executeQuery()) {
                    List<Invoice> result = new ArrayList<>();
                    while (invoicesResultSet.next()) {

                        result.add(DtoFactory.getInstance().createDto(Invoice.class)
                                             .withId(invoicesResultSet.getLong(1))
                                             .withTotal(invoicesResultSet.getDouble(2))
                                             .withAccountId(invoicesResultSet.getString(3))
                                             .withCharges(
                                                     getCharges(connection, accountId, invoicesResultSet.getString(6)))
                                             .withMemoryChargeDetails(getMemoryChargeDetails(connection, accountId,
                                                                                             invoicesResultSet.getString(6)))
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
    public List<Invoice> getNotSendInvoices(int limit) throws ServerException {
        try (Connection connection = connectionFactory.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(RECEIPTS_MAILING_STATE_SELECT)) {
                statement.setLong(1, limit);
                try (ResultSet invoicesResultSet = statement.executeQuery()) {
                    List<Invoice> result = new ArrayList<>(limit);
                    while (invoicesResultSet.next()) {

                        result.add(DtoFactory.getInstance().createDto(Invoice.class)
                                             .withId(invoicesResultSet.getLong(1))
                                             .withTotal(invoicesResultSet.getDouble(2))
                                             .withAccountId(invoicesResultSet.getString(3))
                                             .withCharges(
                                                     getCharges(connection, invoicesResultSet.getString(3), invoicesResultSet.getString(6)))
                                             .withMemoryChargeDetails(getMemoryChargeDetails(connection, invoicesResultSet.getString(3),
                                                                                             invoicesResultSet.getString(6)))
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
    public void markInvoiceAsSent(long invoiceId) throws ServerException {
        try (Connection connection = connectionFactory.getConnection()) {
            try {
                connection.setAutoCommit(false);
                try (PreparedStatement statement = connection.prepareStatement(RECEIPTS_MAILING_TIME_UPDATE)) {
                    statement.setLong(1, System.currentTimeMillis());
                    statement.setLong(2, invoiceId);
                    statement.execute();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
            }
        } catch (SQLException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    private List<MemoryChargeDetails> getMemoryChargeDetails(Connection connection, String accountId, String calculationID)
            throws SQLException {

        List<MemoryChargeDetails> mCharges = new ArrayList<>();
        try (PreparedStatement memoryCharges = connection.prepareStatement(MEMORY_CHARGES_SELECT)) {
            memoryCharges.setString(1, accountId);
            memoryCharges.setString(2, calculationID);

            try (ResultSet chargesResultSet = memoryCharges.executeQuery()) {
                while (chargesResultSet.next()) {
                    mCharges.add(DtoFactory.getInstance().createDto(MemoryChargeDetails.class)
                                           .withAmount(chargesResultSet.getDouble(1))
                                           .withWorkspaceId(chargesResultSet.getString(2))

                                );
                }
            }
        }
        return mCharges;

    }

    private List<Charge> getCharges(Connection connection, String accountId, String calculationID) throws SQLException {
        List<Charge> charges = new ArrayList<>();

        try (PreparedStatement chargesStatement = connection.prepareStatement(CHARGES_SELECT)) {
            chargesStatement.setString(1, accountId);
            chargesStatement.setString(2, calculationID);

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
        return charges;
    }
}
