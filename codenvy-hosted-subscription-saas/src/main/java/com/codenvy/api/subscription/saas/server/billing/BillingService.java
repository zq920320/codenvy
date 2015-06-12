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
package com.codenvy.api.subscription.saas.server.billing;


import com.codenvy.api.subscription.saas.shared.dto.AccountResources;
import com.codenvy.api.subscription.saas.shared.dto.Invoice;
import com.codenvy.api.subscription.saas.shared.dto.Resources;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

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
     * @return number of generated invoices
     * @throws ServerException
     */
    int generateInvoices(long from, long till) throws ServerException;

    /**
     * Get list of invoices with given Payment state.
     *
     * @param filter
     *         filter condition
     * @return - list of invoices with given Payment state.
     * @throws ServerException
     */
    List<Invoice> getInvoices(InvoiceFilter filter) throws ServerException;

    /**
     * Get invoice by id.
     *
     * @param id
     *         id of invoice
     * @return invoice
     * @throws ServerException
     */
    Invoice getInvoice(long id) throws ServerException, NotFoundException;

    /**
     * Change payment state of invoice.
     *
     * @param invoiceId
     *         id of invoice.
     * @param state
     *         next state of invoice.
     * @param creditCard
     *         credit card id. Null if no credit card was involved during operation with invoice.
     * @throws ServerException
     */
    void setPaymentState(long invoiceId, PaymentState state, String creditCard) throws ServerException;


    /**
     * Mark invoice as sent to user.
     *
     * @param invoiceId
     *         Id of invoice.
     * @throws ServerException
     */
    void markInvoiceAsSent(long invoiceId) throws ServerException;

    /**
     * Add prepaid GB*h
     *
     * @param accountId
     *         id of account for whom will be added prepaid GB*h
     * @param amount
     *         prepaid GB*h
     * @param from
     *         period when prepaid GB*h is active
     * @param till
     *         period when prepaid GB*h is active
     */
    void addSubscription(String accountId, double amount, long from, long till) throws ServerException;

    /**
     * Deactivate prepaid GB*h for account
     *
     * @param accountId
     *         id of account for whom will be deactivated prepaid GB*h
     * @param till
     *         period when prepaid GB*h is inactive
     */
    void removeSubscription(String accountId, long till) throws ServerException;

    /**
     * Get total used resources by given period.
     *
     * @return resources related to given account by given period
     */
    Resources getEstimatedUsage(long from, long till) throws ServerException;

    /**
     * Get resources related to accounts by given period.
     *
     * @return resources related to accounts by given period
     */
    List<AccountResources> getEstimatedUsageByAccount(ResourcesFilter resourcesFilter) throws ServerException;

    /**
     * Checks availability of resources for account by given period.
     *
     * @param accountId
     *         id of account
     * @param from
     *         begin of period
     * @param till
     *         end of period
     * @return if account has available resources {@code true} else {@code false}
     * @throws ServerException
     */
    boolean hasAvailableResources(String accountId, Long from, Long till) throws ServerException;

    /**
     * Get provided free resources for account by given period.
     *
     * @param accountId
     *         id of account
     * @param from
     *         begin of period
     * @param till
     *         end of period
     * @return number of provided free resources
     * @throws ServerException
     */
    double getProvidedFreeResources(String accountId, Long from, Long till) throws ServerException;

}
