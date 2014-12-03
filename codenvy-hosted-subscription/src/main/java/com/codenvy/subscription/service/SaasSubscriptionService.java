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
package com.codenvy.subscription.service;

import com.codenvy.api.account.server.SubscriptionService;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.subscription.service.saas.SaasResourceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Service provide functionality of Saas subscription.
 *
 * @author Sergii Kabashniuk
 * @author Sergii Leschenko
 * @author Eugene Voevodin
 * @author Alexander Garagatyi
 */
@Singleton
public class SaasSubscriptionService extends SubscriptionService {
    private static final Logger LOG = LoggerFactory.getLogger(SaasSubscriptionService.class);

    private final WorkspaceDao        workspaceDao;
    private final AccountDao          accountDao;
    private final SaasResourceManager saasResourceManager;

    @Inject
    public SaasSubscriptionService(WorkspaceDao workspaceDao,
                                   AccountDao accountDao,
                                   SaasResourceManager saasResourceManager) {
        super("Saas", "Saas");
        this.workspaceDao = workspaceDao;
        this.accountDao = accountDao;
        this.saasResourceManager = saasResourceManager;
    }

    /**
     * @param subscription
     *         new subscription
     * @throws com.codenvy.api.core.ConflictException
     *         if subscription state is not valid
     * @throws com.codenvy.api.core.ServerException
     *         if internal error occurs
     */
    @Override
    public void beforeCreateSubscription(Subscription subscription) throws ConflictException, ServerException {
        if (subscription.getProperties().get("Package") == null) {
            throw new ConflictException("Subscription property 'Package' required");
        }
        if (subscription.getProperties().get("RAM") == null) {
            throw new ConflictException("Subscription property 'RAM' required");
        }

        try {
            final List<Subscription> subscriptions = accountDao.getSubscriptions(subscription.getAccountId(), getServiceId());
            if (!subscriptions.isEmpty() && !"sas-community".equals(subscriptions.get(0).getPlanId())) {
                throw new ConflictException(SUBSCRIPTION_LIMIT_EXHAUSTED_MESSAGE);
            }
        } catch (ServerException | NotFoundException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage());
        }

        if (workspaceDao.getByAccount(subscription.getAccountId()).isEmpty()) {
            throw new ConflictException("Given account doesn't have any workspaces.");
        }
    }

    @Override
    public void afterCreateSubscription(Subscription subscription) throws ApiException {
        saasResourceManager.setResources(subscription);
    }

    @Override
    public void onRemoveSubscription(Subscription subscription) throws ServerException, NotFoundException, ConflictException {
        saasResourceManager.resetResources(subscription);
    }

    @Override
    public void onCheckSubscription(Subscription subscription) throws ServerException, NotFoundException, ConflictException {
        saasResourceManager.setResources(subscription);
    }

    @Override
    public void onUpdateSubscription(Subscription oldSubscription, Subscription newSubscription)
            throws ServerException, NotFoundException, ConflictException {
        saasResourceManager.setResources(newSubscription);
    }
}
