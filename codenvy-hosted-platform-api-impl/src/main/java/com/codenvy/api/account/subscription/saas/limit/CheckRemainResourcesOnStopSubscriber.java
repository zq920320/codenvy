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
package com.codenvy.api.account.subscription.saas.limit;

import com.codenvy.api.account.AccountLocker;
import com.codenvy.api.account.billing.BillingPeriod;
import com.codenvy.api.account.billing.ResourcesFilter;
import com.codenvy.api.account.impl.shared.dto.AccountResources;
import com.codenvy.api.account.metrics.MeterBasedStorage;
import com.codenvy.api.account.server.dao.Account;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.account.subscription.ServiceId;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.core.notification.EventSubscriber;
import com.codenvy.api.runner.internal.RunnerEvent;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * After run stops, checks that remaining RAM resources is enough, or block further runs for given account.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com) on 1/15/15.
 */
public class CheckRemainResourcesOnStopSubscriber implements EventSubscriber<RunnerEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(CheckRemainResourcesOnStopSubscriber.class);

    private final EventService      eventService;
    private final WorkspaceDao      workspaceDao;
    private final AccountDao        accountDao;
    private final MeterBasedStorage storage;
    private final ActiveRunHolder   activeRunHolder;
    private final BillingPeriod     billingPeriod;
    private final AccountLocker     accountLocker;
    private final Double            freeUsageLimit;


    @Inject
    public CheckRemainResourcesOnStopSubscriber(EventService eventService,
                                                WorkspaceDao workspaceDao,
                                                AccountDao accountDao,
                                                MeterBasedStorage storage,
                                                ActiveRunHolder activeRunHolder,
                                                BillingPeriod billingPeriod,
                                                AccountLocker accountLocker,
                                                @Named("subscription.saas.usage.free.gbh") Double freeUsage) {
        this.eventService = eventService;
        this.workspaceDao = workspaceDao;
        this.accountDao = accountDao;
        this.billingPeriod = billingPeriod;
        this.freeUsageLimit = freeUsage;
        this.storage = storage;
        this.accountLocker = accountLocker;
        this.activeRunHolder = activeRunHolder;
    }


    @PostConstruct
    private void startScheduling() {
        eventService.subscribe(this);
    }

    @PreDestroy
    private void unsubscribe() {
        eventService.unsubscribe(this);
    }

    @Override
    public void onEvent(RunnerEvent event) {
        switch (event.getType()) {
            case STARTED:
                activeRunHolder.addRun(event);
                break;
            case STOPPED:
                activeRunHolder.removeRun(event);
                checkRemainResources(event);
                break;
        }
    }

    private void checkRemainResources(RunnerEvent event) {
        try {
            final Workspace workspace = workspaceDao.getById(event.getWorkspace());
            final Account account = accountDao.getById(workspace.getAccountId());
            final Subscription activeSaasSubscription = accountDao.getActiveSubscription(workspace.getAccountId(), ServiceId.SAAS);
            if (activeSaasSubscription != null && !"sas-community".equals(activeSaasSubscription.getPlanId())) {
                return;
            }

            final List<AccountResources> usedMemory = storage.getUsedMemory(ResourcesFilter.builder()
                                                                                           .withAccountId(workspace.getAccountId())
                                                                                           .withFromDate(
                                                                                                   billingPeriod.getCurrent().getStartDate()
                                                                                                                .getTime())
                                                                                           .withUntilDate(System.currentTimeMillis())
                                                                                           .withPaidGbHMoreThan(0)
                                                                                           .build());
            if (!usedMemory.isEmpty()) {
                accountLocker.lockAccountResources(account.getId());
            }
        } catch (NotFoundException | ServerException e) {
            LOG.error("Error check remaining resources {} in workspace {} .", event.getProcessId(), event.getWorkspace());
        }
    }
}
