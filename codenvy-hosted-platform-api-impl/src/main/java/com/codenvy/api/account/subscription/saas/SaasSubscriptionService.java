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

import static com.codenvy.api.account.subscription.ServiceId.ONPREMISES;
import static com.codenvy.api.account.subscription.ServiceId.SAAS;

import com.codenvy.api.account.billing.BillingPeriod;
import com.codenvy.api.account.metrics.MeterBasedStorage;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.account.server.subscription.SubscriptionService;
import com.codenvy.api.account.shared.dto.AccountResources;
import com.codenvy.api.account.shared.dto.WorkspaceResources;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.dto.server.DtoFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Sergii Leschenko
 */
@Singleton
public class SaasSubscriptionService extends SubscriptionService {
    private final WorkspaceDao      workspaceDao;
    private final MeterBasedStorage meterBasedStorage;
    private final BillingPeriod     billingPeriod;

    @Inject
    public SaasSubscriptionService(WorkspaceDao workspaceDao,
                                   MeterBasedStorage meterBasedStorage,
                                   BillingPeriod billingPeriod) {
        super(SAAS, SAAS);
        this.workspaceDao = workspaceDao;
        this.meterBasedStorage = meterBasedStorage;
        this.billingPeriod = billingPeriod;
    }

    @Override
    public void beforeCreateSubscription(Subscription subscription) throws ConflictException, ServerException {
        //TODO Implement
    }

    @Override
    public void afterCreateSubscription(Subscription subscription) throws ApiException {
        //TODO Implement
    }

    @Override
    public void onRemoveSubscription(Subscription subscription) throws ApiException {
        //TODO Implement
    }

    @Override
    public void onCheckSubscriptions() throws ApiException {
        //TODO Implement
    }

    @Override
    public void onUpdateSubscription(Subscription oldSubscription, Subscription newSubscription) throws ApiException {
        //TODO Implement
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
}
