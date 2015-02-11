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

import static com.codenvy.api.dao.sql.SqlDaoQueries.CHARGES_SELECT;
import static com.codenvy.api.dao.sql.SqlDaoQueries.INVOICES_ACCOUNT_SELECT;
import static com.codenvy.api.dao.sql.SqlDaoQueries.INVOICES_INSERT;
import static com.codenvy.api.dao.sql.SqlDaoQueries.INVOICES_MAILING_STATE_SELECT;
import static com.codenvy.api.dao.sql.SqlDaoQueries.INVOICES_MAILING_TIME_UPDATE;
import static com.codenvy.api.dao.sql.SqlDaoQueries.INVOICES_PAYMENT_STATE_SELECT;
import static com.codenvy.api.dao.sql.SqlDaoQueries.INVOICES_PAYMENT_STATE_UPDATE;
import static com.codenvy.api.dao.sql.SqlDaoQueries.MEMORY_CHARGES_INSERT;
import static com.codenvy.api.dao.sql.SqlDaoQueries.MEMORY_CHARGES_SELECT;

import com.codenvy.api.account.billing.BillingService;
import com.codenvy.api.account.billing.PaymentState;
import com.codenvy.api.account.impl.shared.dto.Charge;
import com.codenvy.api.account.impl.shared.dto.Invoice;
import com.codenvy.api.core.ServerException;
import com.codenvy.dto.server.DtoFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

                try (PreparedStatement freeSaasCharges = connection.prepareStatement(SqlDaoQueries.CHARGES_MEMORY_INSERT)) {
                    freeSaasCharges.setString(1, "Saas");
                    freeSaasCharges.setDouble(2, saasFreeGbH);
                    freeSaasCharges.setDouble(3, saasFreeGbH);
                    freeSaasCharges.setDouble(4, saasChargeableGbHPrice);
                    freeSaasCharges.setString(5, calculationId);
                    freeSaasCharges.setString(6, calculationId);

                    freeSaasCharges.execute();
                }

                try (PreparedStatement invoices = connection.prepareStatement(INVOICES_INSERT)) {
                    invoices.setString(1, PaymentState.WAITING_EXECUTOR.getState());
                    invoices.setLong(2, System.currentTimeMillis());
                    invoices.setLong(3, from);
                    invoices.setLong(4, till);
                    invoices.setString(5, calculationId);
                    invoices.setString(6, calculationId);
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
                try (PreparedStatement statement = connection.prepareStatement(INVOICES_PAYMENT_STATE_UPDATE)) {
                    statement.setString(1, state.getState());
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
    public List<Invoice> getInvoices(PaymentState state, int maxItems, int skipCount) throws ServerException {
        try (Connection connection = connectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(INVOICES_PAYMENT_STATE_SELECT)) {
                statement.setString(1, state.getState());
                statement.setInt(2, maxItems > 0 ? maxItems : Integer.MAX_VALUE);
                statement.setInt(3, skipCount);
                statement.setFetchSize(maxItems > 0 ? maxItems :0);
                try (ResultSet invoicesResultSet = statement.executeQuery()) {
                    List<Invoice> result = maxItems > 0 ? new ArrayList<Invoice>(maxItems) : new ArrayList<Invoice>();
                    while (invoicesResultSet.next()) {

                        result.add(DtoFactory.getInstance().createDto(Invoice.class)
                                             .withId(invoicesResultSet.getLong(1))
                                             .withTotal(invoicesResultSet.getDouble(2))
                                             .withAccountId(invoicesResultSet.getString(3))
                                             .withPaymentState(state.getState())
                                             .withCharges(
                                                     getCharges(connection, invoicesResultSet.getString(3), invoicesResultSet.getString(6)))
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
    public List<Invoice> getInvoices(String accountId, int maxItems, int skipCount) throws ServerException {
        try (Connection connection = connectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(INVOICES_ACCOUNT_SELECT)) {
                statement.setString(1, accountId);
                statement.setInt(2, maxItems > 0 ? maxItems : Integer.MAX_VALUE);
                statement.setInt(3, skipCount);
                statement.setFetchSize(maxItems > 0 ? maxItems :0);
                try (ResultSet invoicesResultSet = statement.executeQuery()) {
                    List<Invoice> result = maxItems > 0 ? new ArrayList<Invoice>(maxItems) : new ArrayList<Invoice>();
                    while (invoicesResultSet.next()) {

                        result.add(DtoFactory.getInstance().createDto(Invoice.class)
                                             .withId(invoicesResultSet.getLong("FID"))
                                             .withTotal(invoicesResultSet.getDouble("FTOTAL"))
                                             .withAccountId(invoicesResultSet.getString("FACCOUNT_ID"))
                                             .withCreditCardId(invoicesResultSet.getString("FCREDIT_CARD"))
                                             .withPaymentDate(invoicesResultSet.getLong("FPAYMENT_TIME"))
                                             .withPaymentState(invoicesResultSet.getString("FPAYMENT_STATE"))
                                             .withMailingDate(invoicesResultSet.getLong("FMAILING_TIME"))
                                             .withCreationDate(invoicesResultSet.getLong("FCREATED_TIME"))
                                             .withFromDate(invoicesResultSet.getLong("FFROM_TIME"))
                                             .withUntilDate(invoicesResultSet.getLong("FTILL_TIME"))
                                             .withCharges(
                                                     getCharges(connection, accountId, invoicesResultSet.getString("FCALC_ID")))

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
    public List<Invoice> getNotSendInvoices(int maxItems, int skipCount) throws ServerException {
        try (Connection connection = connectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(INVOICES_MAILING_STATE_SELECT)) {
                statement.setInt(1, maxItems > 0 ? maxItems : Integer.MAX_VALUE);
                statement.setInt(2, skipCount);
                statement.setFetchSize(maxItems > 0 ? maxItems :0);
                try (ResultSet invoicesResultSet = statement.executeQuery()) {
                    List<Invoice> result = maxItems > 0 ? new ArrayList<Invoice>(maxItems) : new ArrayList<Invoice>();
                    while (invoicesResultSet.next()) {

                        result.add(DtoFactory.getInstance().createDto(Invoice.class)
                                             .withId(invoicesResultSet.getLong(1))
                                             .withTotal(invoicesResultSet.getDouble(2))
                                             .withAccountId(invoicesResultSet.getString(3))
                                             .withCharges(
                                                     getCharges(connection, invoicesResultSet.getString(3), invoicesResultSet.getString(6)))
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
                try (PreparedStatement statement = connection.prepareStatement(INVOICES_MAILING_TIME_UPDATE)) {
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

    private Map<String, String> getMemoryChargeDetails(Connection connection, String accountId, String calculationID)
            throws SQLException {

        Map<String, String> mCharges = new HashMap<>();
        try (PreparedStatement memoryCharges = connection.prepareStatement(MEMORY_CHARGES_SELECT)) {
            memoryCharges.setString(1, accountId);
            memoryCharges.setString(2, calculationID);


            try (ResultSet chargesResultSet = memoryCharges.executeQuery()) {
                while (chargesResultSet.next()) {
                    mCharges.put(chargesResultSet.getString("FWORKSPACE_ID"), Double.toString(chargesResultSet.getDouble("FAMOUNT")));
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
                                          .withPaidAmount(chargesResultSet.getDouble("FPAID_AMOUNT"))
                                          .withServiceId(chargesResultSet.getString("FSERVICE_ID"))
                                          .withPaidPrice(chargesResultSet.getDouble("FPAID_PRICE"))
                                          .withFreeAmount(chargesResultSet.getDouble("FFREE_AMOUNT"))
                                          .withPrePaidAmount(chargesResultSet.getDouble("FPREPAID_AMOUNT"))
                                          .withDetails(getMemoryChargeDetails(connection, accountId, calculationID))
                               );
                }
            }

        }
        return charges;
    }
}
