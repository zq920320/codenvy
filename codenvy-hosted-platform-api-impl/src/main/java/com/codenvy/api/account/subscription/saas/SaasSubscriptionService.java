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

import com.codenvy.api.account.AccountLocker;
import com.codenvy.api.account.billing.BillingPeriod;
import com.codenvy.api.account.billing.BillingService;
import com.codenvy.api.account.metrics.MeterBasedStorage;
import com.codenvy.api.account.server.ResourcesChangesNotifier;
import com.codenvy.api.account.subscription.SubscriptionEvent;
import com.codenvy.api.account.subscription.service.util.SubscriptionServiceHelper;

import org.eclipse.che.api.account.server.SubscriptionService;
import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.account.server.dao.Subscription;
import org.eclipse.che.api.account.shared.dto.UsedAccountResources;
import org.eclipse.che.api.account.shared.dto.WorkspaceResources;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
//import org.eclipse.che.api.runner.internal.Constants;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
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

    private final int                       freeMaxLimit;
    private final WorkspaceDao              workspaceDao;
    private final AccountDao                accountDao;
    private final MeterBasedStorage         meterBasedStorage;
    private final BillingPeriod             billingPeriod;
    private final AccountLocker             accountLocker;
    private final EventService              eventService;
    private final BillingService            billingService;
    private final SubscriptionServiceHelper subscriptionServiceHelper;
    private final ResourcesChangesNotifier  resourcesChangesNotifier;

    @Inject
    public SaasSubscriptionService(@Named("subscription.saas.free.max_limit_mb") int freeMaxLimit,
                                   WorkspaceDao workspaceDao,
                                   AccountDao accountDao,
                                   MeterBasedStorage meterBasedStorage,
                                   BillingPeriod billingPeriod,
                                   AccountLocker accountLocker,
                                   EventService eventService,
                                   BillingService billingService,
                                   SubscriptionServiceHelper subscriptionServiceHelper,
                                   ResourcesChangesNotifier resourcesChangesNotifier) {
        super(SAAS, SAAS);
        this.freeMaxLimit = freeMaxLimit;
        this.workspaceDao = workspaceDao;
        this.accountDao = accountDao;
        this.meterBasedStorage = meterBasedStorage;
        this.billingPeriod = billingPeriod;
        this.accountLocker = accountLocker;
        this.eventService = eventService;
        this.billingService = billingService;
        this.subscriptionServiceHelper = subscriptionServiceHelper;
        this.resourcesChangesNotifier = resourcesChangesNotifier;
    }

    @Override
    public void beforeCreateSubscription(Subscription subscription) throws ConflictException, ServerException, ForbiddenException {
        //TODO Add checking unpaid old subscription
        try {
            final Subscription activeSaasSubscription = accountDao.getActiveSubscription(subscription.getAccountId(), getServiceId());

            if (activeSaasSubscription != null && !"sas-community".equals(activeSaasSubscription.getPlanId())) {
                throw new ConflictException(SUBSCRIPTION_LIMIT_EXHAUSTED_MESSAGE);
            }
        } catch (NotFoundException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage());
        }

        subscriptionServiceHelper.checkTrial(subscription);
        subscriptionServiceHelper.checkCreditCard(subscription);
        subscriptionServiceHelper.fillDates(subscription);
    }

    @Override
    public void afterCreateSubscription(Subscription subscription) throws ApiException {
        //TODO Enable payment when will be exist Saas prepaid subscriptions with non null number of GbH
        //Saas subscriptions will be paid in end of billing period
        //subscriptionServiceHelper.chargeSubscriptionIfNeed(subscription);
        subscriptionServiceHelper.fillDates(subscription);

        eventService.publish(SubscriptionEvent.subscriptionAddedEvent(subscription));

        accountLocker.removeResourcesLock(subscription.getAccountId());

        final String prepaidGbH = subscription.getProperties().get("PrepaidGbH");
        billingService.addSubscription(subscription.getAccountId(),
                                       prepaidGbH == null ? 0D : Double.parseDouble(prepaidGbH),
                                       subscription.getStartDate().getTime(),
                                       subscription.getEndDate().getTime());

    }

    @Override
    public void onRemoveSubscription(Subscription subscription) throws ApiException {
        eventService.publish(SubscriptionEvent.subscriptionRemovedEvent(subscription));
        //TODO replace System.currentTimeMillis() to subscription.getEndDate().getTime() when end date of subscription will be updated after removing of subscription
        billingService.removeSubscription(subscription.getAccountId(), System.currentTimeMillis());

        List<Workspace> workspaces;
        try {
            workspaces = workspaceDao.getByAccount(subscription.getAccountId());
        } catch (ServerException e) {
            LOG.error("Can't get workspaces by account %s for resetting runner ram to max allowed");
            return;
        }
        for (Workspace workspace : workspaces) {
            /*if (!workspace.getAttributes().containsKey(Constants.RUNNER_MAX_MEMORY_SIZE)) {
                continue;
            }

            final String runnerRam = workspace.getAttributes().get(Constants.RUNNER_MAX_MEMORY_SIZE);
            if (Integer.parseInt(runnerRam) > freeMaxLimit) {
                workspace.getAttributes().put(Constants.RUNNER_MAX_MEMORY_SIZE, String.valueOf(freeMaxLimit));
                try {
                    workspaceDao.update(workspace);
                    resourcesChangesNotifier.publishTotalMemoryChangedEvent(workspace.getId(), String.valueOf(freeMaxLimit));
                } catch (NotFoundException | ConflictException | ServerException e) {
                    LOG.error("Can't reset size of runner ram to max allowed for workspace " + workspace.getId());
                }
            }*/
        }
    }

    @Override
    public void onUpdateSubscription(Subscription oldSubscription, Subscription newSubscription) throws ApiException {

    }

    @Override
    public void onCheckSubscriptions() throws ApiException {
    }

    @Override
    public UsedAccountResources getAccountResources(Subscription subscription) throws ServerException {
        final String accountId = subscription.getAccountId();
        final Map<String, Double> memoryUsedReport = meterBasedStorage.getMemoryUsedReport(accountId,
                                                                                           billingPeriod.getCurrent().getStartDate()
                                                                                                        .getTime(),
                                                                                           System.currentTimeMillis());

        List<Workspace> workspaces = workspaceDao.getByAccount(accountId);
        for (Workspace workspace : workspaces) {
            if (!memoryUsedReport.containsKey(workspace.getId())) {
                memoryUsedReport.put(workspace.getId(), 0D);
            }
        }

        List<WorkspaceResources> result = new ArrayList<>();
        for (Map.Entry<String, Double> usedMemory : memoryUsedReport.entrySet()) {
            result.add(DtoFactory.getInstance().createDto(WorkspaceResources.class)
                                 .withWorkspaceId(usedMemory.getKey())
                                 .withMemory(usedMemory.getValue()));
        }

        return DtoFactory.getInstance().createDto(UsedAccountResources.class)
                         .withUsed(result);
    }
}
