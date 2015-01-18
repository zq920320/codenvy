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
package com.codenvy.subscription.service.saas;

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
import java.util.List;
import java.util.Map;

import static com.codenvy.commons.lang.Size.parseSizeToMegabytes;

/**
 * This class manages resources of workspaces and accounts in accordance to Saas subscription
 *
 * @author Sergii Leschenko
 */
public class SaasResourceManager {
    private static final Logger LOG = LoggerFactory.getLogger(SaasResourceManager.class);

    private static final String SAAS_RUNNER_LIFETIME        = "saas.runner.lifetime";
    private static final String SAAS_BUILDER_EXECUTION_TIME = "saas.builder.execution_time";

    private final String saasRunnerLifetime;
    private final String saasBuilderExecutionTime;

    private final WorkspaceDao workspaceDao;
    private final AccountDao   accountDao;

    @Inject
    public SaasResourceManager(WorkspaceDao workspaceDao,
                               AccountDao accountDao,
                               @Named(SAAS_RUNNER_LIFETIME) String saasRunnerLifetime,
                               @Named(SAAS_BUILDER_EXECUTION_TIME) String saasBuilderExecutionTime) {
        this.saasRunnerLifetime = saasRunnerLifetime;
        this.saasBuilderExecutionTime = saasBuilderExecutionTime;
        this.workspaceDao = workspaceDao;
        this.accountDao = accountDao;
    }

    /**
     * Sets resources of account and related workspaces in accordance to Saas subscription
     */
    public void setResources(Subscription subscription) throws NotFoundException, ConflictException, ServerException {
        String tariffPackage = ensureExistsAndGet("Package", subscription).toLowerCase();

        final Account account = accountDao.getById(subscription.getAccountId());
        final List<Workspace> workspaces = workspaceDao.getByAccount(subscription.getAccountId());

        if (workspaces.isEmpty()) {
            throw new ConflictException("Given account doesn't have any workspaces.");
        }

        if ("team".equals(tariffPackage) || "enterprise".equals(tariffPackage)) {
            account.getAttributes().put("codenvy:multi-ws", "true");
            accountDao.update(account);
        }

        for (Workspace workspace : workspaces) {
            setResources(workspace, subscription);
        }
    }

    /**
     * Sets resources of workspaces in accordance to Saas subscription
     */
    public void setResources(Workspace workspace, Subscription subscription) throws NotFoundException,
                                                                                    ConflictException,
                                                                                    ServerException {
        String tariffPackage = ensureExistsAndGet("Package", subscription).toLowerCase();

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

        if (isPrimaryWorkspace(workspace)) {
            try {
                wsAttributes.put("codenvy:runner_ram", String.valueOf(parseSizeToMegabytes(ensureExistsAndGet("RAM", subscription))));
            } catch (IllegalArgumentException exception) {
                throw new ConflictException("Subscription with such plan can't be added");
            }
        } else {
            wsAttributes.put("codenvy:runner_ram", "0");
        }

        workspaceDao.update(workspace);
    }

    /**
     * Resets to default resources of account and related workspaces to it
     */
    public void resetResources(Subscription oldSubscription) throws NotFoundException, ServerException, ConflictException {
        String tariffPackage = oldSubscription.getProperties().get("Package");
        tariffPackage = null == tariffPackage ? null : tariffPackage.toLowerCase();

        if ("team".equals(tariffPackage) || "enterprise".equals(tariffPackage)) {
            try {
                final Account account = accountDao.getById(oldSubscription.getAccountId());
                account.getAttributes().remove("codenvy:multi-ws");
                accountDao.update(account);
            } catch (ApiException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

        for (Workspace workspace : workspaceDao.getByAccount(oldSubscription.getAccountId())) {
            resetResources(workspace);
        }
    }

    /**
     * Resets to default resources of workspace
     */
    private void resetResources(Workspace workspace) throws NotFoundException, ServerException, ConflictException {
        try {
            final Map<String, String> wsAttributes = workspace.getAttributes();
            if (isPrimaryWorkspace(workspace)) {
                wsAttributes.remove("codenvy:runner_ram");
            } else {
                wsAttributes.put("codenvy:runner_ram", "0");
            }
            wsAttributes.remove("codenvy:runner_lifetime");
            wsAttributes.remove("codenvy:builder_execution_time");
            wsAttributes.remove("codenvy:runner_infra");
            workspaceDao.update(workspace);
        } catch (ApiException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    private boolean isPrimaryWorkspace(Workspace workspace) {
        return !workspace.getAttributes().containsKey("codenvy:role");
    }

    private String ensureExistsAndGet(String propertyName, Subscription src) throws ConflictException {
        final String target = src.getProperties().get(propertyName);
        if (target == null) {
            throw new ConflictException(String.format("Subscription property %s required", propertyName));
        }
        return target;
    }
}
