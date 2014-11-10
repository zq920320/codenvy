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
import com.codenvy.api.account.server.dao.Account;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

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
    private static final Logger LOG                         = LoggerFactory.getLogger(SaasSubscriptionService.class);
    private static final String SAAS_RUNNER_LIFETIME        = "saas.runner.lifetime";
    private static final String SAAS_BUILDER_EXECUTION_TIME = "saas.builder.execution_time";

    private final String       saasRunnerLifetime;
    private final String       saasBuilderExecutionTime;
    private final WorkspaceDao workspaceDao;
    private final AccountDao   accountDao;

    @Inject
    public SaasSubscriptionService(WorkspaceDao workspaceDao,
                                   AccountDao accountDao,
                                   @Named(SAAS_RUNNER_LIFETIME) String saasRunnerLifetime,
                                   @Named(SAAS_BUILDER_EXECUTION_TIME) String saasBuilderExecutionTime) {
        super("Saas", "Saas");
        this.saasRunnerLifetime = saasRunnerLifetime;
        this.saasBuilderExecutionTime = saasBuilderExecutionTime;
        this.workspaceDao = workspaceDao;
        this.accountDao = accountDao;
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
        if (subscription.getProperties() == null) {
            throw new ConflictException("Subscription properties required");
        }
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
        setResources(subscription);
    }

    @Override
    public void onRemoveSubscription(Subscription subscription) throws ServerException, NotFoundException, ConflictException {
        unsetResources(subscription);
    }

    @Override
    public void onCheckSubscription(Subscription subscription) throws ServerException, NotFoundException, ConflictException {
        setResources(subscription);
    }

    @Override
    public void onUpdateSubscription(Subscription oldSubscription, Subscription newSubscription)
            throws ServerException, NotFoundException, ConflictException {
        setResources(newSubscription);
    }

    private void setResources(Subscription subscription) throws NotFoundException, ConflictException, ServerException {
        final Map<String, String> properties = subscription.getProperties();
        if (properties == null) {
            throw new ConflictException("Subscription properties required");
        }

        String tariffPackage = ensureExistsAndGet("Package", subscription).toLowerCase();

        if ("team".equals(tariffPackage) || "enterprise".equals(tariffPackage)) {
            try {
                final Account account = accountDao.getById(subscription.getAccountId());
                account.getAttributes().put("codenvy:multi-ws", "true");
                accountDao.update(account);
            } catch (NotFoundException e) {
                throw new ConflictException(e.getLocalizedMessage());
            }
        }

        List<Workspace> workspaces = workspaceDao.getByAccount(subscription.getAccountId());
        if (!workspaces.isEmpty()) {
            boolean ramIsSet = false;
            for (Workspace workspace : workspaces) {
                final Map<String, String> wsAttributes = workspace.getAttributes();
                switch (tariffPackage) {
                    case "developer":
                    case "team":
                        //1 hour
                        wsAttributes.put("codenvy:runner_lifetime", saasRunnerLifetime);
                        wsAttributes.put("codenvy:runner_infra", "paid");
                        break;
                    case "project":
                    case "enterprise":
                        //unlimited
                        wsAttributes.put("codenvy:runner_lifetime", "-1");
                        wsAttributes.put("codenvy:runner_infra", "always_on");
                        break;
                    default:
                        throw new NotFoundException(String.format("Package %s not found", tariffPackage));
                }
                wsAttributes.put("codenvy:builder_execution_time", saasBuilderExecutionTime);
                if (ramIsSet) {
                    wsAttributes.put("codenvy:runner_ram", "0");
                } else {
                    wsAttributes.put("codenvy:runner_ram", String.valueOf(convert(ensureExistsAndGet("RAM", subscription))));
                    ramIsSet = true;
                }

                workspaceDao.update(workspace);
            }
        } else {
            throw new ConflictException("Given account doesn't have any workspaces.");
        }
    }

    private String ensureExistsAndGet(String propertyName, Subscription src) throws ConflictException {
        final String target = src.getProperties().get(propertyName);
        if (target == null) {
            throw new ConflictException(String.format("Subscription property %s required", propertyName));
        }
        return target;
    }

    private void unsetResources(Subscription subscription) throws NotFoundException, ServerException, ConflictException {
        String tariffPackage = subscription.getProperties().get("Package");
        tariffPackage = null == tariffPackage ? null : tariffPackage.toLowerCase();

        if ("team".equals(tariffPackage) || "enterprise".equals(tariffPackage)) {
            try {
                final Account account = accountDao.getById(subscription.getAccountId());
                account.getAttributes().remove("codenvy:multi-ws");
                accountDao.update(account);
            } catch (ApiException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

        boolean defaultRamUsed = false;
        for (Workspace workspace : workspaceDao.getByAccount(subscription.getAccountId())) {
            try {
                final Map<String, String> wsAttributes = workspace.getAttributes();
                if (defaultRamUsed) {
                    wsAttributes.put("codenvy:runner_ram", "0");
                } else {
                    wsAttributes.remove("codenvy:runner_ram");
                    defaultRamUsed = true;
                }
                wsAttributes.remove("codenvy:runner_lifetime");
                wsAttributes.remove("codenvy:builder_execution_time");
                wsAttributes.remove("codenvy:runner_infra");
                workspaceDao.update(workspace);
            } catch (ApiException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Converts String RAM with suffix GB or MB to int RAM in MB.
     * e.g.
     * "1GB" -> 1024
     *
     * @param RAM
     *         string RAM in GB or MB
     * @return int RAM in MB
     */
    private int convert(String RAM) throws ConflictException {
        try {
            int ramMb;
            switch (RAM.substring(RAM.length() - 2)) {
                case "GB":
                    ramMb = 1024 * Integer.parseInt(RAM.substring(0, RAM.length() - 2));
                    break;
                case "MB":
                    ramMb = Integer.parseInt(RAM.substring(0, RAM.length() - 2));
                    break;
                default:
                    LOG.error("Bad RAM value " + RAM);
                    throw new ConflictException("Subscription with such plan can't be added");
            }

            return ramMb;
        } catch (NumberFormatException nfEx) {
            LOG.error("Bad RAM value " + RAM);
            throw new ConflictException("Subscription with such plan can't be added");
        }
    }
}
