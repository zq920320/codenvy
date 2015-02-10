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
import com.codenvy.api.dao.billing.BraintreeCreditCardDaoImpl;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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
    private Customer customer1;

    @Mock
    private Result<Customer> customerResult;

    @Mock
    private Result<CreditCard> cardResult;

    @Mock
    private CreditCard creditCard;


    @InjectMocks
    private BraintreeCreditCardDaoImpl dao;


    @BeforeMethod
    public void setUp() throws Exception {
        when(gateway.clientToken()).thenReturn(tokenGateway);
        when(gateway.customer()).thenReturn(customerGateway);
        when(gateway.creditCard()).thenReturn(cardGateway);
        when(creditCard.getToken()).thenReturn(TOKEN);
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

    @Test
    public void shouldBeAbleToRegisterAdditionalCards() throws Exception {
        when(customerGateway.find(anyString())).thenReturn(customer1);
        when(customer1.getCreditCards()).thenReturn(Collections.<CreditCard>emptyList());
        when(customer1.getId()).thenReturn(ACCOUNT_ID);
        when(customerGateway.update(eq(ACCOUNT_ID), any(CustomerRequest.class))).thenReturn(customerResult);
        when(customerResult.isSuccess()).thenReturn(true);
        when(customerResult.getTarget()).thenReturn(customer);
        List<CreditCard> list = new ArrayList<>();
        list.add(creditCard);
        when(customer.getCreditCards()).thenReturn(list);
        dao.registerCard(ACCOUNT_ID, "nonce123", null, null, null, null);
        verify(customerGateway).update(eq(ACCOUNT_ID), any(CustomerRequest.class));
    }

    @Test
    public void shouldBeAbleToGetCards() throws Exception {
        when(customerGateway.find(anyString())).thenReturn(customer);
        when(customerResult.isSuccess()).thenReturn(true);
        when(creditCard.getCustomerId()).thenReturn(ACCOUNT_ID);
        List<CreditCard> list =  new ArrayList<>();
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
