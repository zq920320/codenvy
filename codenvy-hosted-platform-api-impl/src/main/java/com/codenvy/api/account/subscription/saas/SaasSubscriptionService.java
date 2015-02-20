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
package com.codenvy.api.account.subscription.saas;

import com.codenvy.api.account.billing.BillingPeriod;
import com.codenvy.api.account.metrics.MeterBasedStorage;
import com.codenvy.api.account.server.dao.Account;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.account.server.subscription.SubscriptionService;
import com.codenvy.api.account.shared.dto.AccountResources;
import com.codenvy.api.account.shared.dto.WorkspaceResources;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.dto.server.DtoFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.codenvy.api.account.subscription.ServiceId.SAAS;

/**
 * @author Sergii Leschenko
 */
@Singleton
public class SaasSubscriptionService extends SubscriptionService {
    private static final Logger LOG = LoggerFactory.getLogger(SaasSubscriptionService.class);

    private final WorkspaceDao      workspaceDao;
    private final AccountDao        accountDao;
    private final MeterBasedStorage meterBasedStorage;
    private final BillingPeriod     billingPeriod;

    @Inject
    public SaasSubscriptionService(WorkspaceDao workspaceDao,
                                   AccountDao accountDao,
                                   MeterBasedStorage meterBasedStorage,
                                   BillingPeriod billingPeriod) {
        super(SAAS, SAAS);
        this.workspaceDao = workspaceDao;
        this.accountDao = accountDao;
        this.meterBasedStorage = meterBasedStorage;
        this.billingPeriod = billingPeriod;
    }

    @Override
    public void beforeCreateSubscription(Subscription subscription) throws ConflictException, ServerException {
        try {
            final Subscription activeSaasSubscription = accountDao.getActiveSubscription(subscription.getAccountId(), getServiceId());

            if (activeSaasSubscription != null && !"sas-community".equals(activeSaasSubscription.getPlanId())) {
                throw new ConflictException(SUBSCRIPTION_LIMIT_EXHAUSTED_MESSAGE);
            }
        } catch (NotFoundException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage());
        }

        //TODO Add checking of credit card
    }

    @Override
    public void afterCreateSubscription(Subscription subscription) throws ApiException {
        markAsPaidAndUnlockAccount(subscription.getAccountId());
    }

    @Override
    public void onRemoveSubscription(Subscription subscription) throws ApiException {
        final Account account = accountDao.getById(subscription.getAccountId());
        account.getAttributes().remove("codenvy:paid");
        try {
            accountDao.update(account);
        } catch (NotFoundException | ServerException e) {
            LOG.error("Error removing lock property into account  {} .", account.getId());
        }
    }

    @Override
    public void onCheckSubscriptions() throws ApiException {
        //TODO Implement
    }

    @Override
    public void onUpdateSubscription(Subscription oldSubscription, Subscription newSubscription) throws ApiException {
        //nothing to do
    }

    @Override
    public AccountResources getAccountResources(Subscription subscription) throws ServerException {
        final String accountId = subscription.getAccountId();
        final Map<String, Double> memoryUsedReport = meterBasedStorage.getMemoryUsedReport(accountId,
                                                                                           billingPeriod.getCurrent().getStartDate()
                                                                                                        .getTime(),
                                                                                           System.currentTimeMillis());

        final List<Workspace> workspaces = workspaceDao.getByAccount(accountId);
        final List<WorkspaceResources> result = new ArrayList<>();

        for (Workspace workspace : workspaces) {
            final Double usedMemory = memoryUsedReport.get(workspace.getId());
            result.add(DtoFactory.getInstance().createDto(WorkspaceResources.class)
                                 .withWorkspaceId(workspace.getId())
                                 .withMemory(usedMemory != null ? usedMemory : 0.0));
        }

        return DtoFactory.getInstance().createDto(AccountResources.class)
                         .withUsed(result);
    }

    private void markAsPaidAndUnlockAccount(String accountId) throws NotFoundException, ServerException {
        final Account account = accountDao.getById(accountId);
        account.getAttributes().put("codenvy:paid", Boolean.toString(true));

        //TODO Add checking unpaid old subscription
        account.getAttributes().remove(com.codenvy.api.account.server.Constants.LOCKED_PROPERTY);
        try {
            accountDao.update(account);
        } catch (NotFoundException | ServerException e) {
            LOG.error("Error removing lock property into account  {} .", account.getId());
        }

        for (Workspace ws : workspaceDao.getByAccount(account.getId())) {
            ws.getAttributes().remove(com.codenvy.api.account.server.Constants.LOCKED_PROPERTY);
            try {
                workspaceDao.update(ws);
            } catch (NotFoundException | ServerException | ConflictException e) {
                LOG.error("Error removing lock property into workspace  {} .", ws.getId());
            }
        }
    }
}
