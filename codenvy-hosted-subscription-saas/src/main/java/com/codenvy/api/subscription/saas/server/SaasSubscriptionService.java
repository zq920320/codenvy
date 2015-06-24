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
package com.codenvy.api.subscription.saas.server;

import com.codenvy.api.metrics.server.ResourcesChangesNotifier;
import com.codenvy.api.subscription.saas.server.billing.BillingService;
import com.codenvy.api.subscription.server.AbstractSubscriptionService;
import com.codenvy.api.subscription.server.SubscriptionEvent;
import com.codenvy.api.subscription.server.dao.Subscription;
import com.codenvy.api.subscription.server.dao.SubscriptionDao;
import com.codenvy.api.subscription.server.util.SubscriptionServiceHelper;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
//import org.eclipse.che.api.runner.internal.Constants;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

/**
 * @author Sergii Leschenko
 */
@Singleton
public class SaasSubscriptionService extends AbstractSubscriptionService {
    public static final  String SAAS_SUBSCRIPTION_ID = "Saas";
    private static final Logger LOG                  = LoggerFactory.getLogger(SaasSubscriptionService.class);

    private final int                       freeMaxLimit;
    private final WorkspaceDao              workspaceDao;
    private final SubscriptionDao           subscriptionDao;
    private final AccountLocker             accountLocker;
    private final EventService              eventService;
    private final BillingService            billingService;
    private final SubscriptionServiceHelper subscriptionServiceHelper;
    private final ResourcesChangesNotifier  resourcesChangesNotifier;

    @Inject
    public SaasSubscriptionService(@Named("subscription.saas.free.max_limit_mb") int freeMaxLimit,
                                   WorkspaceDao workspaceDao,
                                   SubscriptionDao subscriptionDao,
                                   AccountLocker accountLocker,
                                   EventService eventService,
                                   BillingService billingService,
                                   SubscriptionServiceHelper subscriptionServiceHelper,
                                   ResourcesChangesNotifier resourcesChangesNotifier) {
        super(SAAS_SUBSCRIPTION_ID, "Saas");
        this.freeMaxLimit = freeMaxLimit;
        this.workspaceDao = workspaceDao;
        this.subscriptionDao = subscriptionDao;
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
            final Subscription activeSaasSubscription = subscriptionDao.getActiveByServiceId(subscription.getAccountId(), getServiceId());

            if (activeSaasSubscription != null) {
                throw new ConflictException(SUBSCRIPTION_LIMIT_EXHAUSTED_MESSAGE);
            }
        } catch (NotFoundException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage());
        }

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

        accountLocker.removePaymentLock(subscription.getAccountId());
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
}
