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

import com.codenvy.api.creditcard.server.CreditCardDao;
import com.codenvy.api.creditcard.shared.dto.CreditCard;
import com.codenvy.api.subscription.saas.server.AccountLocker;
import com.codenvy.api.subscription.saas.server.InvoicePaymentService;
import com.codenvy.api.subscription.saas.shared.dto.Invoice;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link InvoiceCharger}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class InvoiceChargerTest {
    final DtoFactory dto = DtoFactory.getInstance();

    @Mock
    BillingService        billingService;
    @Mock
    InvoicePaymentService invoicePaymentService;
    @Mock
    CreditCardDao         creditCardDao;
    @Mock
    AccountLocker         accountLocker;

    InvoiceCharger invoiceCharger;

    @BeforeMethod
    public void setUp() {
        invoiceCharger = new InvoiceCharger(invoicePaymentService, billingService, creditCardDao, accountLocker);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Account with id account does not have credit card")
    public void shouldSetPaymentStateToCreditCardMissingForInvoiceWithTotalMoreThan0WhenAccountDoesNotHaveCreditCard() throws Exception {
        when(creditCardDao.getCards(anyString())).thenReturn(Collections.<CreditCard>emptyList());

        invoiceCharger.charge(dto.createDto(Invoice.class)
                                 .withTotal(100D)
                                 .withId(1L)
                                 .withAccountId("account"));

        verify(billingService).setPaymentState(eq(1L), eq(PaymentState.CREDIT_CARD_MISSING), (String)isNull());
        verify(accountLocker).setPaymentLock("account");
    }

    @Test
    public void shouldPayInvoiceWithTotalMoreThan0() throws Exception {
        when(creditCardDao.getCards(anyString())).thenReturn(Collections.singletonList(dto.createDto(CreditCard.class)
                                                                                          .withToken("ccToken")));

        invoiceCharger.charge(dto.createDto(Invoice.class)
                                 .withTotal(100D)
                                 .withId(1L));

        verify(invoicePaymentService).charge(argThat(new ArgumentMatcher<Invoice>() {
            @Override
            public boolean matches(Object o) {
                return ((Invoice)o).getId() == 1L;
            }
        }));
        verify(billingService).setPaymentState(eq(1L), eq(PaymentState.PAID_SUCCESSFULLY), eq("ccToken"));
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldSetPaymentStateToPaymentFailedIfWereSomeChargingTroubles() throws Exception {
        when(creditCardDao.getCards(anyString())).thenReturn(Collections.singletonList(dto.createDto(CreditCard.class)
                                                                                          .withToken("ccToken")));

        doThrow(new ServerException("Exception")).when(invoicePaymentService).charge((Invoice)anyObject());

        invoiceCharger.charge(dto.createDto(Invoice.class)
                                 .withTotal(100D)
                                 .withId(1L));

        verify(invoicePaymentService).charge(argThat(new ArgumentMatcher<Invoice>() {
            @Override
            public boolean matches(Object o) {
                return ((Invoice)o).getId() == 1L;
            }
        }));
        verify(billingService).setPaymentState(eq(1L), eq(PaymentState.PAYMENT_FAIL), eq("ccToken"));
    }

}
