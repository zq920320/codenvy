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
package com.codenvy.api.account;

import com.codenvy.api.subscription.server.dao.SubscriptionDao;
import com.codenvy.api.subscription.server.dao.SubscriptionQueryBuilder;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import java.util.Collections;
import java.util.List;

/**
 * @author Igor Vinokur
 *
 * Dummy implementation of SubscriptionDao
 * injected in AccountDaoImpl
 */
public class SubscriptionDaoImpl implements SubscriptionDao {
    @Override
    public void create(com.codenvy.api.subscription.server.dao.Subscription subscription)
            throws NotFoundException, ConflictException, ServerException {

    }

    @Override
    public com.codenvy.api.subscription.server.dao.Subscription getById(String subscriptionId) throws NotFoundException, ServerException {
        return null;
    }

    @Override
    public List<com.codenvy.api.subscription.server.dao.Subscription> getActive(String accountId)
            throws NotFoundException, ServerException {
        return Collections.emptyList();
    }

    @Override
    public com.codenvy.api.subscription.server.dao.Subscription getActiveByServiceId(String accountId, String serviceId)
            throws ServerException, NotFoundException {
        return null;
    }

    @Override
    public void update(com.codenvy.api.subscription.server.dao.Subscription subscription) throws NotFoundException, ServerException {

    }

    @Override
    public void remove(String subscriptionId) throws NotFoundException, ServerException {

    }

    @Override
    public void deactivate(String subscriptionId) throws NotFoundException, ServerException {

    }

    @Override
    public SubscriptionQueryBuilder getSubscriptionQueryBuilder() {
        return null;
    }
}
