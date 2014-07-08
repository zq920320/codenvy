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
package com.codenvy.service.factory;

import com.codenvy.api.account.server.SubscriptionService;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.shared.dto.Subscription;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ServerException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Iterator;
import java.util.List;

/**
 * Subscription of tracked factories
 *
 * @author Sergii Kabashniuk
 * @author Eugene Voevodin
 */
@Singleton
public class TrackedFactoryService extends SubscriptionService {

    private final AccountDao accountDao;

    @Inject
    public TrackedFactoryService(AccountDao accountDao) {
        super("TrackedFactory", "Tracked Factory");
        this.accountDao = accountDao;
    }

    //fixme for now TrackedFactory supports only 1 active subscription per 1 account
    @Override
    public void beforeCreateSubscription(Subscription subscription) throws ApiException {
        final List<Subscription> allSubscriptions = accountDao.getSubscriptions(subscription.getAccountId());
        Subscription tracked = null;
        for (Iterator<Subscription> sIt = allSubscriptions.iterator(); sIt.hasNext() && tracked == null; ) {
            final Subscription current = sIt.next();
            if (getServiceId().equals(current.getServiceId())) {
                tracked = current;
            }
        }
        if (tracked != null) {
            throw new ServerException("TrackedFactory subscription already exists");
        }
    }

    @Override
    public void afterCreateSubscription(Subscription subscription) throws ApiException {
        //nothing to do
    }

    @Override
    public void onRemoveSubscription(Subscription subscription) {
        //nothing to do
    }

    @Override
    public void onCheckSubscription(Subscription subscription) {
        //nothing to do
    }

    @Override
    public void onUpdateSubscription(Subscription oldSubscription, Subscription newSubscription) {
        //nothing to do
    }

    @Override
    public double tarifficate(Subscription subscription) {
        return 0D;
    }
}