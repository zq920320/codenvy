/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.braintree;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Subscription;
import com.braintreegateway.WebhookNotification;
import com.braintreegateway.WebhookNotificationGateway;
import com.braintreegateway.exceptions.InvalidSignatureException;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import java.util.Calendar;

import static com.braintreegateway.WebhookNotification.Kind;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link BraintreeWebhookService}
 *
 * @author Alexander Garagatyi
 */
@Listeners(MockitoTestNGListener.class)
public class BraintreeWebhookServiceTest {
    private static final String SUBSCRIPTION_ID = "subscription_id";
    @Mock
    private BraintreeGateway           gateway;
    @Mock
    private WebhookNotificationGateway webhookNotificationGateway;
    @Mock
    private AccountDao                 accountDao;
    @Mock
    private WebhookNotification        notification;
    @Mock
    private Subscription               subscription;

    @InjectMocks
    private BraintreeWebhookService webhookService;

    @Test(dataProvider = "subscriptionWebhookProvider")
    public void shouldRemoveSubscriptionOnSubscriptionExpiredWebhook(Kind kind) throws NotFoundException, ServerException {
        when(gateway.webhookNotification()).thenReturn(webhookNotificationGateway);
        when(webhookNotificationGateway.parse(anyString(), anyString())).thenReturn(notification);
        when(notification.getKind()).thenReturn(kind);
        when(notification.getSubscription()).thenReturn(subscription);
        when(subscription.getId()).thenReturn(SUBSCRIPTION_ID);
        when(notification.getTimestamp()).thenReturn(Calendar.getInstance());

        Response response = webhookService.processWebhooks("signature", "payload");

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        verify(accountDao).removeSubscription(SUBSCRIPTION_ID);
        verify(accountDao).removeBillingProperties(SUBSCRIPTION_ID);
    }

    @DataProvider(name = "subscriptionWebhookProvider")
    private Object[][] subscriptionWebhookProvider() {
        return new Object[][]{
                {Kind.SUBSCRIPTION_CANCELED},
                {Kind.SUBSCRIPTION_EXPIRED}
        };
    }

    @Test(dataProvider = "anotherWebhookProvider")
    public void shouldDoNothingIfAnotherWebhookCame(Kind kind) {
        when(gateway.webhookNotification()).thenReturn(webhookNotificationGateway);
        when(webhookNotificationGateway.parse(anyString(), anyString())).thenReturn(notification);
        when(notification.getKind()).thenReturn(kind);
        when(notification.getTimestamp()).thenReturn(Calendar.getInstance());

        Response response = webhookService.processWebhooks("signature", "payload");

        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());

        verifyZeroInteractions(accountDao);
    }

    @DataProvider(name = "anotherWebhookProvider")
    private Object[][] anotherWebhookProvider() {
        return new Object[][]{
                {Kind.PARTNER_MERCHANT_DISCONNECTED},
                {Kind.PARTNER_MERCHANT_CONNECTED},
                {Kind.PARTNER_MERCHANT_DECLINED},
                {Kind.SUB_MERCHANT_ACCOUNT_APPROVED},
                {Kind.SUB_MERCHANT_ACCOUNT_DECLINED},
                {Kind.SUBSCRIPTION_CHARGED_SUCCESSFULLY},
                {Kind.SUBSCRIPTION_CHARGED_UNSUCCESSFULLY},
                {Kind.SUBSCRIPTION_TRIAL_ENDED},
                {Kind.SUBSCRIPTION_WENT_ACTIVE},
                {Kind.SUBSCRIPTION_WENT_PAST_DUE},
                {Kind.TRANSACTION_DISBURSED},
                {Kind.DISBURSEMENT_EXCEPTION},
                {Kind.DISBURSEMENT},
                {Kind.UNRECOGNIZED}
        };
    }

    @Test
    public void shouldRespondServerErrorIfExceptionOccurs() {
        when(gateway.webhookNotification()).thenReturn(webhookNotificationGateway);
        when(webhookNotificationGateway.parse(anyString(), anyString())).thenThrow(new InvalidSignatureException("message"));

        Response response = webhookService.processWebhooks("signature", "payload");

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

        verifyZeroInteractions(accountDao);
    }
}