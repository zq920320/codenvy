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
package com.codenvy.subscription.service.saas;

import com.codenvy.api.account.server.dao.Account;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.core.notification.EventSubscriber;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.workspace.event.CreateWorkspaceEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

import static java.lang.Boolean.parseBoolean;

/**
 * Ensures the availability of resources to created workspace
 *
 * @author Sergii Leschenko
 */
@Singleton
public class SaasWorkspaceResourcesProvider implements EventSubscriber<CreateWorkspaceEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(SaasWorkspaceResourcesProvider.class);


    private final AccountDao          accountDao;
    private final EventService        eventService;
    private final SaasResourceManager saasResourceManager;
    private final boolean             onPremises;

    @Inject
    public SaasWorkspaceResourcesProvider(EventService eventService,
                                          AccountDao accountDao,
                                          SaasResourceManager saasResourceManager,
                                          @Named("subscription.orgaddon.enabled") boolean onPremises) {
        this.eventService = eventService;
        this.accountDao = accountDao;
        this.onPremises = onPremises;
        this.saasResourceManager = saasResourceManager;
    }

    @PostConstruct
    private void subscribe() {
        eventService.subscribe(this);
    }

    @PreDestroy
    private void unsubscribe() {
        eventService.unsubscribe(this);
    }

    @Override
    public void onEvent(CreateWorkspaceEvent event) {
        final Workspace createdWorkspace = event.getWorkspace();

        if (onPremises || createdWorkspace.isTemporary()) {
            return;
        }

        try {
            if (isMultiWsAllowed(createdWorkspace.getAccountId())) {
                final Subscription saasSubscription = getSaasSubscription(createdWorkspace.getAccountId());
                if (saasSubscription != null && !"Community".equals(saasSubscription.getProperties().get("Package"))) {
                    saasResourceManager.setResources(createdWorkspace, saasSubscription);
                }
            }
        } catch (ApiException e) {
            LOG.error("Can't update attributes of created workspace " + createdWorkspace.getId(), e);
        }
    }

    private boolean isMultiWsAllowed(String accountId) throws NotFoundException, ServerException {
        final Account account = accountDao.getById(accountId);
        return parseBoolean(account.getAttributes().get("codenvy:multi-ws"));
    }

    private Subscription getSaasSubscription(String accountId) throws NotFoundException, ServerException {
        List<Subscription> subscriptions = accountDao.getSubscriptions(accountId, "Saas");
        if (!subscriptions.isEmpty()) {
            return subscriptions.get(0);
        }
        return null;
    }
}
