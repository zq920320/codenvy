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

import com.braintreegateway.Address;
import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.ClientTokenGateway;
import com.braintreegateway.ClientTokenRequest;
import com.braintreegateway.CreditCard;
import com.braintreegateway.CreditCardGateway;
import com.braintreegateway.Customer;
import com.braintreegateway.CustomerGateway;
import com.braintreegateway.CustomerRequest;
import com.braintreegateway.Result;
import com.braintreegateway.exceptions.NotFoundException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.dao.billing.BraintreeCreditCardDaoImpl;
import com.codenvy.commons.env.EnvironmentContext;
import com.codenvy.commons.user.User;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author Max Shaposhnik (mshaposhnik@codenvy.com) on 1/29/15.
 *
 */
@Listeners(MockitoTestNGListener.class)
public class BraintreeCreditCardDaoImplTest {
    private static final String ACCOUNT_ID = "accountId";
    private static final String TOKEN      = "token123";


    @Mock
    private BraintreeGateway gateway;

    @Mock
    private ClientTokenGateway tokenGateway;

    @Mock
    private CustomerGateway customerGateway;

    @Mock
    private CreditCardGateway cardGateway;

    @Mock
    private Customer customer;

    @Mock
    private EventService eventService;

    @Mock
    private Address address;

    @Mock
    private Result<Customer> customerResult;

    @Mock
    private Result<CreditCard> cardResult;

    @Mock
    private CreditCard creditCard;

    @Mock
    User user;

    @InjectMocks
    private BraintreeCreditCardDaoImpl dao;


    @BeforeMethod
    public void setUp() throws Exception {
        when(gateway.clientToken()).thenReturn(tokenGateway);
        when(gateway.customer()).thenReturn(customerGateway);
        when(gateway.creditCard()).thenReturn(cardGateway);
        when(creditCard.getToken()).thenReturn(TOKEN);
        EnvironmentContext context = new EnvironmentContext();
        context.setUser(user);
        EnvironmentContext.setCurrent(context);
    }

    @Test
    public void shouldBeAbleToGetToken() throws Exception {
        when(gateway.clientToken()).thenReturn(tokenGateway);
        dao.getClientToken(ACCOUNT_ID);
        verify(tokenGateway).generate(any(ClientTokenRequest.class));
    }

    @Test
    public void shouldBeAbleToRegisterFirstCard() throws Exception {
        when(customerGateway.find(anyString())).thenThrow(new NotFoundException());
        when(customerGateway.create(any(CustomerRequest.class))).thenReturn(customerResult);
        when(customerResult.isSuccess()).thenReturn(true);
        when(customerResult.getTarget()).thenReturn(customer);
        List<CreditCard> list = Arrays.asList(creditCard);
        when(customer.getCreditCards()).thenReturn(list);
        dao.registerCard(ACCOUNT_ID, "nonce123", null, null, null, null);
        verify(customerGateway).create(any(CustomerRequest.class));
    }

    @Test(expectedExceptions = ForbiddenException.class)
    public void shouldBeNotAbleToRegisterAdditionalCards() throws Exception {
        when(customerGateway.find(anyString())).thenReturn(customer);
        List<CreditCard> list = new ArrayList<>();
        list.add(creditCard);
        when(customer.getCreditCards()).thenReturn(list);
        dao.registerCard(ACCOUNT_ID, "nonce123", null, null, null, null);
    }

    @Test
    public void shouldBeAbleToGetCards() throws Exception {
        when(customerGateway.find(anyString())).thenReturn(customer);
        when(customerResult.isSuccess()).thenReturn(true);
        when(creditCard.getCustomerId()).thenReturn(ACCOUNT_ID);
        when(creditCard.getBillingAddress()).thenReturn(address);
        List<CreditCard> list = new ArrayList<>();
        list.add(creditCard);
        when(customer.getCreditCards()).thenReturn(list);
        List<com.codenvy.api.account.impl.shared.dto.CreditCard> result = dao.getCards(ACCOUNT_ID);
        assertEquals(result.get(0).getAccountId(), list.get(0).getCustomerId());
        assertEquals(result.get(0).getToken(), list.get(0).getToken());
    }

    @Test
    public void shouldBeAbleToRemoveCards() throws Exception {
        when(cardGateway.delete(anyString())).thenReturn(cardResult);
        when(cardResult.isSuccess()).thenReturn(true);
        when(cardResult.getTarget()).thenReturn(creditCard);
        dao.deleteCard(ACCOUNT_ID, TOKEN);
        verify(cardGateway).delete(anyString());

    }

    @Test(expectedExceptions = ForbiddenException.class)
    public void shouldThrowExceptionIfNullPassedGetToken() throws Exception {
        dao.getClientToken(null);
    }

    @Test(expectedExceptions = ForbiddenException.class)
    public void shouldThrowExceptionIfNullPassedGetCards() throws Exception {
        dao.getCards(null);
    }

    @Test(expectedExceptions = ForbiddenException.class)
    public void shouldThrowExceptionIfNullPassedRegisterCard() throws Exception {
        dao.registerCard(null, "nonce123", null, null, null, null);
    }



}
