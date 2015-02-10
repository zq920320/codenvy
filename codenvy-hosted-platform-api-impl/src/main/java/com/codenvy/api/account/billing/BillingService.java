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
package com.codenvy.api.account.billing;


import com.codenvy.api.account.impl.shared.dto.Invoice;
import com.codenvy.api.core.ServerException;

import java.util.List;

/**
 * Provide access to existed invoices and generate new invoices for the given period.
 *
 * @author Sergii Kabashniuk
 */
public interface BillingService {
    /**
     * Generate invoices for the given period of time.
     *
     * @param from
     *         beginning of period.
     * @param till
     *         end of period.
     * @throws ServerException
     */
    void generateInvoices(long from, long till) throws ServerException;

    /**
     * Get list of invoices with given Payment state.
     *
     * @param state
     *         state of payment.
     * @param limit
     *         - limit number of invoices
     * @return - list of invoices with given Payment state.
     * @throws ServerException
     */
    List<Invoice> getInvoices(PaymentState state, int limit) throws ServerException;

    /**
     * @param accountId
     *         account id.
     * @return all invoices for given account.
     * @throws ServerException
     */
    List<Invoice> getInvoices(String accountId) throws ServerException;

    /**
     * @param limit
     *         max number of invoices to get.
     * @return list of invoices that should be send to user.
     * @throws ServerException
     */
    List<Invoice> getNotSendInvoices(int limit) throws ServerException;

    /**
     * Change payment state of invoice.
     *
     * @param invoiceId
     *         id of invoice.
     * @param state
     *         next state of invoice.
     * @throws ServerException
     */
    void setPaymentState(long invoiceId, PaymentState state) throws ServerException;

    /**
     * Mark invoice as sent to user.
     *
     * @param invoiceId
     *         Id of invoice.
     * @throws ServerException
     */
    void markInvoiceAsSent(long invoiceId) throws ServerException;
}
