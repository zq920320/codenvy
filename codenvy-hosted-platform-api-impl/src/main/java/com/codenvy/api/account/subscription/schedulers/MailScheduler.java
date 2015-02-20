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
package com.codenvy.api.account.subscription.schedulers;

import com.codenvy.api.account.billing.BillingService;
import com.codenvy.api.account.billing.TemplateProcessor;
import com.codenvy.api.account.impl.shared.dto.Invoice;
import com.codenvy.api.account.subscription.service.util.SubscriptionMailSender;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.commons.lang.Strings;
import com.codenvy.commons.schedule.ScheduleDelay;

import org.codenvy.mail.MailSenderClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.mail.MessagingException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.codenvy.api.account.billing.PaymentState.NOT_REQUIRED;
import static com.codenvy.api.account.billing.PaymentState.PAID_SUCCESSFULLY;

/**
 * TODO
 *
 * @author Sergii Leschenko
 */
@Singleton
public class MailScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(MailScheduler.class);

    private static final int INVOICES_LIMIT = 50; //TODO mb make it configurable

    private final SubscriptionMailSender subscriptionMailSender;
    private final BillingService         billingService;
    private final MailSenderClient       mailSenderClient;
    private final String                 invoiceSubject;
    private final String                 invoiceNoPaymentSubject;
    private final String                 billingFailedSubject;
    private final TemplateProcessor      templateProcessor;
    private final String                 billingAddress;

    @Inject
    public MailScheduler(SubscriptionMailSender subscriptionMailSender,
                         BillingService billingService,
                         MailSenderClient mailSenderClient,
                         TemplateProcessor templateProcessor,
                         @Named("subscription.saas.mail.invoice.subject") String invoiceSubject,
                         @Named("subscription.saas.mail.invoice.no_payment.subject") String invoiceNoPaymentSubject,
                         @Named("subscription.saas.mail.billing.failed.subject") String billingFailedSubject,
                         @Named("subscription.saas.mail.address") String billingAddress) {
        this.billingService = billingService;
        this.subscriptionMailSender = subscriptionMailSender;
        this.invoiceSubject = invoiceSubject;
        this.invoiceNoPaymentSubject = invoiceNoPaymentSubject;
        this.billingFailedSubject = billingFailedSubject;
        this.mailSenderClient = mailSenderClient;
        this.templateProcessor = templateProcessor;
        this.billingAddress = billingAddress;
    }

    //TODO configure it
    @ScheduleDelay(initialDelay = 6,
                   delay = 60,
                   unit = TimeUnit.SECONDS)
    public void sendEmails() {
        try {
            List<Invoice> notSendInvoices;
            while ((notSendInvoices = billingService.getNotSendInvoices(INVOICES_LIMIT, 0)).size() != 0) {
                for (Invoice notSendInvoice : notSendInvoices) {
                    try {
                        sendMail(notSendInvoice);

                        billingService.markInvoiceAsSent(notSendInvoice.getId());
                    } catch (ApiException e) {
                        LOG.error("Can't send email", e);
                    }
                }
            }
        } catch (ServerException e) {
            LOG.error("Can't get not send invoices", e);//TODO
        }
    }

    private void sendMail(Invoice invoice) throws ServerException, NotFoundException, ForbiddenException {
        String subject;

        if (NOT_REQUIRED.getState().equals(invoice.getPaymentState())) {
            subject = invoiceNoPaymentSubject;
        } else if (PAID_SUCCESSFULLY.getState().equals(invoice.getPaymentState())) {
            subject = invoiceSubject;
        } else {
            subject = billingFailedSubject;
        }

        StringWriter htmlBody = new StringWriter();
        templateProcessor.processTemplate(invoice, htmlBody);

        final List<String> accountOwnersEmails = subscriptionMailSender.getAccountOwnersEmails(invoice.getAccountId());

        try {
            mailSenderClient.sendMail(billingAddress,
                                      Strings.join(", ", accountOwnersEmails.toArray(new String[0])),
                                      null,
                                      subject,
                                      MediaType.TEXT_HTML,
                                      htmlBody.toString());
        } catch (IOException | MessagingException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }
}
