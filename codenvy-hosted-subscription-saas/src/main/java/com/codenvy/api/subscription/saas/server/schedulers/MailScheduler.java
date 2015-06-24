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
package com.codenvy.api.subscription.saas.server.schedulers;

import com.codenvy.api.subscription.saas.server.billing.BillingService;
import com.codenvy.api.subscription.saas.server.billing.invoice.InvoiceFilter;
import com.codenvy.api.subscription.saas.server.billing.PaymentState;
import com.codenvy.api.subscription.saas.server.billing.invoice.InvoiceTemplateProcessor;
import com.codenvy.api.subscription.saas.shared.dto.Invoice;
import com.codenvy.api.subscription.saas.server.service.util.SubscriptionMailSender;

import org.eclipse.che.commons.schedule.ScheduleDelay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.StringWriter;
import java.util.List;


/**
 * Sends emails about resources consumption and charges
 *
 * @author Sergii Leschenko
 */
@Singleton
public class MailScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(MailScheduler.class);

    private static final String INVOICE_FETCH_LIMIT = "billing.invoice.fetch.limit";

    private final SubscriptionMailSender   subscriptionMailSender;
    private final BillingService           billingService;
    private final InvoiceTemplateProcessor invoiceTemplateProcessor;
    private final int                      invoices_limit;

    @Inject
    public MailScheduler(SubscriptionMailSender subscriptionMailSender,
                         BillingService billingService,
                         InvoiceTemplateProcessor invoiceTemplateProcessor,
                         @Named(INVOICE_FETCH_LIMIT) int invoices_limit) {
        this.billingService = billingService;
        this.subscriptionMailSender = subscriptionMailSender;
        this.invoiceTemplateProcessor = invoiceTemplateProcessor;
        this.invoices_limit = invoices_limit;
    }

    @ScheduleDelay(delay = 5)
    public void sendEmails() {
        try {
            List<Invoice> notSendInvoices = billingService.getInvoices(InvoiceFilter.builder()
                                                                                    .withIsMailNotSend()
                                                                                    .withPaymentStates(PaymentState.PAYMENT_FAIL,
                                                                                                       PaymentState.PAID_SUCCESSFULLY)
                                                                                    .withMaxItems(invoices_limit)
                                                                                    .withSkipCount(0)
                                                                                    .build());
            for (Invoice notSendInvoice : notSendInvoices) {
                try {
                    StringWriter htmlBody = new StringWriter();
                    invoiceTemplateProcessor.processTemplate(notSendInvoice, htmlBody);
                    subscriptionMailSender.sendInvoice(notSendInvoice, htmlBody.toString());
                    billingService.markInvoiceAsSent(notSendInvoice.getId());
                } catch (Exception e) {
                    LOG.error("Can't send invoice " + notSendInvoice.getId(), e);
                }
            }
        } catch (Exception e) {
            LOG.error("Error of invoices sending. " + e.getLocalizedMessage(), e);
        }
    }


}
