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
package com.codenvy.service;

import com.codenvy.api.account.server.SubscriptionService;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Service provide functionality of On-premises subscription.
 *
 * @author Sergii Leschenko
 * @author Alexander Garagatyi
 */
@Singleton
public class OnPremisesService extends SubscriptionService {
    private final AccountDao accountDao;

    @Inject
    public OnPremisesService(AccountDao accountDao) {
        super("onPremises", "onPremises");
        this.accountDao = accountDao;
    }

    /**
     * @param subscription
     *         new subscription
     * @throws com.codenvy.api.core.ApiException
     *         if subscription state is not valid
     */
    @Override
    public void beforeCreateSubscription(Subscription subscription) throws ApiException {
        if (subscription.getProperties() == null) {
            throw new ConflictException("Subscription properties required");
        }
        if (subscription.getProperties().get("Package") == null) {
            throw new ConflictException("Subscription property 'Package' required");
        }
        if (subscription.getProperties().get("Users") == null) {
            throw new ConflictException("Subscription property 'Users' required");
        }

        for (Subscription current : accountDao.getSubscriptions(subscription.getAccountId())) {
            if (getServiceId().equals(current.getServiceId())) {
                throw new ConflictException("Subscriptions limit exhausted");
            }
        }
    }

    @Override
    public void afterCreateSubscription(Subscription subscription) throws ApiException {
    }

    @Override
    public void onRemoveSubscription(Subscription subscription) throws ServerException, NotFoundException, ConflictException {

    }

    @Override
    public void onCheckSubscription(Subscription subscription) throws ServerException, NotFoundException, ConflictException {

    }

    @Override
    public void onUpdateSubscription(Subscription oldSubscription, Subscription newSubscription)
            throws ServerException, NotFoundException, ConflictException {
    }
}
