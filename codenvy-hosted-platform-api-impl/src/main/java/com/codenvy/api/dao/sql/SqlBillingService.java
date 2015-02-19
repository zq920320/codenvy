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
import com.codenvy.api.account.billing.InvoiceFilter;
import com.codenvy.api.account.billing.PaymentState;
import com.codenvy.api.account.impl.shared.dto.Charge;
import com.codenvy.api.account.impl.shared.dto.Invoice;
import com.codenvy.api.account.subscription.ServiceId;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.dao.sql.postgresql.Int8RangeType;
import com.codenvy.dto.server.DtoFactory;

import org.postgresql.util.PGobject;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.codenvy.api.dao.sql.SqlDaoQueries.CHARGES_SELECT;
import static com.codenvy.api.dao.sql.SqlDaoQueries.INVOICES_INSERT;
import static com.codenvy.api.dao.sql.SqlDaoQueries.INVOICES_MAILING_TIME_UPDATE;
import static com.codenvy.api.dao.sql.SqlDaoQueries.INVOICES_PAYMENT_STATE_AND_CC_UPDATE;
import static com.codenvy.api.dao.sql.SqlDaoQueries.INVOICES_PAYMENT_STATE_UPDATE;
import static com.codenvy.api.dao.sql.SqlDaoQueries.MEMORY_CHARGES_INSERT;
import static com.codenvy.api.dao.sql.SqlDaoQueries.MEMORY_CHARGES_SELECT;
import static com.codenvy.api.dao.sql.SqlDaoQueries.PREPAID_INSERT;


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
                Int8RangeType range = new Int8RangeType(from, till, true, true);
                //calculate memory usage statistic
                try (PreparedStatement memoryChargesStatement = connection.prepareStatement(MEMORY_CHARGES_INSERT)) {

                    memoryChargesStatement.setObject(1, range);
                    memoryChargesStatement.setObject(2, range);
                    memoryChargesStatement.setString(3, calculationId);
                    memoryChargesStatement.setObject(4, range);
                    memoryChargesStatement.execute();
                }

                try (PreparedStatement saasCharges = connection.prepareStatement(SqlDaoQueries.CHARGES_MEMORY_INSERT)) {
                    saasCharges.setString(1, ServiceId.SAAS);
                    saasCharges.setDouble(2, saasFreeGbH);
                    saasCharges.setDouble(3, saasFreeGbH);
                    saasCharges.setDouble(4, saasFreeGbH);
                    saasCharges.setDouble(5, saasChargeableGbHPrice);
                    saasCharges.setString(6, calculationId);

                    saasCharges.setObject(7, range);
                    saasCharges.setObject(8, range);
                    saasCharges.setDouble(9, till - from);
                    saasCharges.setObject(10, range);
                    saasCharges.setString(11, calculationId);

                    saasCharges.execute();
                }

                try (PreparedStatement invoices = connection.prepareStatement(INVOICES_INSERT)) {
                    invoices.setLong(1, System.currentTimeMillis());
                    invoices.setObject(2, range);
                    invoices.setString(3, calculationId);
                    invoices.setString(4, calculationId);
                    invoices.execute();
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                if (e.getLocalizedMessage().contains("conflicts with existing key (faccount_id, fperiod)")) {
                    throw new ServerException("Not able to generate invoices. Result overlaps with existed invoices.");
                }
                throw new ServerException(e.getLocalizedMessage(), e);
            }
        } catch (SQLException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }

    }


    @Override
    public void setPaymentState(long invoiceId, PaymentState state, String creditCard) throws ServerException {
        if ((state == PaymentState.PAYMENT_FAIL || state == PaymentState.PAID_SUCCESSFULLY)) {
            if (creditCard == null || creditCard.isEmpty()) {
                throw new ServerException("Credit card parameter is missing for states  PAYMENT_FAIL or PAID_SUCCESSFULLY");
            }
        } else {
            if (creditCard != null && !creditCard.isEmpty()) {
                throw new ServerException(
                        "Credit card parameter should be null for states different when PAYMENT_FAIL or PAID_SUCCESSFULLY");
            }
        }
        try (Connection connection = connectionFactory.getConnection()) {
            try {
                connection.setAutoCommit(false);
                if (state == PaymentState.PAYMENT_FAIL || state == PaymentState.PAID_SUCCESSFULLY) {
                    try (PreparedStatement statement = connection.prepareStatement(INVOICES_PAYMENT_STATE_AND_CC_UPDATE)) {
                        statement.setString(1, state.getState());
                        statement.setString(2, creditCard);
                        statement.setLong(3, invoiceId);
                        statement.execute();
                    }
                } else {
                    try (PreparedStatement statement = connection.prepareStatement(INVOICES_PAYMENT_STATE_UPDATE)) {
                        statement.setString(1, state.getState());
                        statement.setLong(2, invoiceId);
                        statement.execute();
                    }

                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public List<Invoice> getInvoices(InvoiceFilter filter) throws ServerException {
        try (Connection connection = connectionFactory.getConnection()) {
            connection.setAutoCommit(false);

            StringBuilder invoiceSelect = new StringBuilder();
            invoiceSelect.append("SELECT ").append(SqlDaoQueries.INVOICES_FIELDS).append(" FROM INVOICES ");

            SqlQueryAppender.appendEqual(invoiceSelect, "FID", filter.getId());
            SqlQueryAppender.appendEqual(invoiceSelect, "FACCOUNT_ID", filter.getAccountId());
            SqlQueryAppender.appendIn(invoiceSelect, "FPAYMENT_STATE", filter.getStates());
            SqlQueryAppender.appendIsNull(invoiceSelect, "FMAILING_TIME", filter.getIsMailNotSend());
            SqlQueryAppender.appendContainsRange(invoiceSelect, "FPERIOD", filter.getFromDate(),
                                                 filter.getUntilDate());

            invoiceSelect.append(" ORDER BY FACCOUNT_ID, FCREATED_TIME DESC ");

            SqlQueryAppender.appendLimit(invoiceSelect, filter.getMaxItems());
            SqlQueryAppender.appendOffset(invoiceSelect, filter.getSkipCount());


            try (PreparedStatement statement = connection.prepareStatement(invoiceSelect.toString())) {

                statement.setFetchSize(filter.getMaxItems() != null ? filter.getMaxItems() : 0);
                try (ResultSet invoicesResultSet = statement.executeQuery()) {
                    List<Invoice> result =
                            filter.getMaxItems() != null ? new ArrayList<Invoice>(filter.getMaxItems())
                                                         : new ArrayList<Invoice>();
                    while (invoicesResultSet.next()) {
                        result.add(toInvoice(connection, invoicesResultSet));
                    }
                    return result;
                }
            }
        } catch (SQLException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public List<Invoice> getInvoices(PaymentState state, int maxItems, int skipCount) throws ServerException {
        return getInvoices(InvoiceFilter.builder()
                                        .withPaymentStates(state)
                                        .withMaxItems(maxItems)
                                        .withSkipCount(skipCount)
                                        .build());
    }

    @Override
    public List<Invoice> getInvoices(String accountId, int maxItems, int skipCount) throws ServerException {
        return getInvoices(InvoiceFilter.builder()
                                        .withAccountId(accountId)
                                        .withMaxItems(maxItems)
                                        .withSkipCount(skipCount)
                                        .build());
    }

    @Override
    public List<Invoice> getNotSendInvoices(int maxItems, int skipCount) throws ServerException {
        return getInvoices(InvoiceFilter.builder()
                                        .withIsMailNotSend()
                                        .withPaymentStates(PaymentState.NOT_REQUIRED,
                                                           PaymentState.PAYMENT_FAIL,
                                                           PaymentState.PAID_SUCCESSFULLY,
                                                           PaymentState.CREDIT_CARD_MISSING)
                                        .withMaxItems(maxItems)
                                        .withSkipCount(skipCount).build());
    }


    @Override
    public Invoice getInvoice(long id) throws ServerException, NotFoundException {
        List<Invoice> invoices = getInvoices(InvoiceFilter.builder().withId(id).build());
        if (invoices.size() < 1) {
            throw new NotFoundException("Invoice with id " + id + " is not found");
        }
        return invoices.get(0);
    }

    @Override
    public void markInvoiceAsSent(long invoiceId) throws ServerException {
        try (Connection connection = connectionFactory.getConnection()) {
            try {
                connection.setAutoCommit(false);
                try (PreparedStatement statement = connection.prepareStatement(INVOICES_MAILING_TIME_UPDATE)) {
                    statement.setLong(1, invoiceId);
                    statement.execute();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void addPrepaid(String accountId, double amount, long from, long till) throws ServerException {
        try (Connection connection = connectionFactory.getConnection()) {
            try {
                connection.setAutoCommit(false);
                Int8RangeType range = new Int8RangeType(from, till, true, true);
                try (PreparedStatement statement = connection.prepareStatement(PREPAID_INSERT)) {
                    statement.setString(1, accountId);
                    statement.setDouble(2, amount);
                    statement.setObject(3, range);
                    statement.execute();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                if (e.getLocalizedMessage().contains("conflicts with existing key (faccount_id, fperiod)")) {
                    throw new ServerException(
                            "Unable to add new prepaid time since it overlapping with existed period");
                }
                throw e;
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
                    mCharges.put(chargesResultSet.getString("FWORKSPACE_ID"),
                                 Double.toString(chargesResultSet.getDouble("FAMOUNT")));
                }
            }
        }
        return mCharges;

    }

    private Invoice toInvoice(Connection connection, ResultSet invoicesResultSet) throws SQLException {
        Int8RangeType range = new Int8RangeType((PGobject)invoicesResultSet.getObject("FPERIOD"));
        Date fpayment_time = invoicesResultSet.getDate("FPAYMENT_TIME");
        Date fmailing_time = invoicesResultSet.getDate("FMAILING_TIME");
        return DtoFactory.getInstance().createDto(Invoice.class)
                         .withId(invoicesResultSet.getLong("FID"))
                         .withTotal(invoicesResultSet.getDouble("FTOTAL"))
                         .withAccountId(invoicesResultSet.getString("FACCOUNT_ID"))
                         .withCreditCardId(invoicesResultSet.getString("FCREDIT_CARD"))
                         .withPaymentDate(fpayment_time != null ? fpayment_time.getTime() : 0)
                         .withPaymentState(invoicesResultSet.getString("FPAYMENT_STATE"))
                         .withMailingDate(fmailing_time != null ? fmailing_time.getTime() : 0)
                         .withCreationDate(invoicesResultSet.getLong("FCREATED_TIME"))
                         .withFromDate(range.getFrom())
                         .withUntilDate(range.getUntil())
                         .withCharges(getCharges(connection,
                                                 invoicesResultSet.getString("FACCOUNT_ID"),
                                                 invoicesResultSet.getString("FCALC_ID")));
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
