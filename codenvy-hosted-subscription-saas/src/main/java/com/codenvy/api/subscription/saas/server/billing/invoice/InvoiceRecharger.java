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
package com.codenvy.api.subscription.saas.server.billing.invoice;

import com.codenvy.api.creditcard.server.event.CreditCardRegistrationEvent;
import com.codenvy.api.subscription.saas.server.AccountLocker;
import com.codenvy.api.subscription.saas.server.billing.BillingService;
import com.codenvy.api.subscription.saas.shared.dto.Invoice;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.commons.lang.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.codenvy.api.creditcard.server.event.CreditCardRegistrationEvent.EventType.CREDIT_CARD_ADDED;
import static com.codenvy.api.subscription.saas.server.billing.PaymentState.CREDIT_CARD_MISSING;
import static com.codenvy.api.subscription.saas.server.billing.PaymentState.PAYMENT_FAIL;

/**
 * Schedules recharging of invoices and automatically recharges unpaid invoices after adding of credit cards
 *
 * @author Sergii Leschenko
 */
@Singleton
public class InvoiceRecharger implements EventSubscriber<CreditCardRegistrationEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(InvoiceRecharger.class);

    static TimeUnit usedTimeUnit = TimeUnit.MINUTES;

    private final BillingService           billingService;
    private final InvoiceCharger           invoiceCharger;
    private final EventService             eventService;
    private final ScheduledExecutorService executorService;
    private final AccountLocker            accountLocker;
    private final int                      chargingPeriod;
    private final int                      chargingAttemptLimit;

    @Inject
    public InvoiceRecharger(BillingService billingService,
                            InvoiceCharger invoiceCharger,
                            EventService eventService,
                            AccountLocker accountLocker,
                            @Named("invoice.recharging.period.min") int chargingPeriod,
                            @Named("invoice.recharging.attempts.limit") int chargingAttemptLimit) {
        this.billingService = billingService;
        this.invoiceCharger = invoiceCharger;
        this.eventService = eventService;
        this.accountLocker = accountLocker;
        this.chargingPeriod = chargingPeriod;
        this.chargingAttemptLimit = chargingAttemptLimit;
        this.executorService = Executors.newScheduledThreadPool(10, new NamedThreadFactory("Invoices recharging", false));
    }

    public void scheduleCharging(long invoiceId) {
        new ScheduledCharging(invoiceId).schedule();
    }

    private class ScheduledCharging implements Runnable {
        private final AtomicInteger runCount = new AtomicInteger();
        private final    long               invoiceId;
        private volatile ScheduledFuture<?> self;

        public ScheduledCharging(long invoiceId) {
            this.invoiceId = invoiceId;
        }

        @Override
        public void run() {
            try {
                Invoice invoice = billingService.getInvoice(invoiceId);
                String paymentState = invoice.getPaymentState();
                if (paymentState.equals(PAYMENT_FAIL.getState())
                    || paymentState.equals(CREDIT_CARD_MISSING.getState())) {

                    invoiceCharger.charge(invoice);
                    self.cancel(false);
                    accountLocker.removePaymentLock(invoice.getAccountId());
                }
            } catch (ApiException e) {
                LOG.error("Error recharging of invoice with id " + invoiceId, e);
                if (runCount.incrementAndGet() >= chargingAttemptLimit) {
                    self.cancel(false);
                }
            }
        }

        public void schedule() {
            self = executorService.scheduleWithFixedDelay(this, 0, chargingPeriod, usedTimeUnit);
        }
    }

    @PostConstruct
    private void subscribe() {
        eventService.subscribe(this);
    }

    @PreDestroy
    private void unsubscribe() {
        eventService.unsubscribe(this);
    }

    public void onEvent(CreditCardRegistrationEvent event) {
        if (CREDIT_CARD_ADDED.equals(event.getType())) {
            final List<Invoice> invoices;
            try {
                invoices = billingService.getInvoices(InvoiceFilter.builder().withAccountId(event.getAccountId())
                                                                   .withPaymentStates(CREDIT_CARD_MISSING, PAYMENT_FAIL)
                                                                   .build());
            } catch (ServerException e) {
                LOG.error("Can't get invoices for account with id " + event.getAccountId() + " for recharging");
                return;
            }

            for (Invoice invoice : invoices) {
                try {
                    invoiceCharger.charge(invoice);
                } catch (ApiException e) {
                    LOG.error("Can't recharge invoice " + invoice.getId(), e);
                }
            }

            accountLocker.removePaymentLock(event.getAccountId());
        }
    }
}
