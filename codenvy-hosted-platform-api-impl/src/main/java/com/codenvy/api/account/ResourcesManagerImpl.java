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

import com.codenvy.api.account.server.ResourcesManager;
import com.codenvy.api.account.server.dao.Account;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.shared.dto.AccountMetrics;
import com.codenvy.api.account.shared.dto.UpdateResourcesDescriptor;
import com.codenvy.api.account.shared.dto.WorkspaceMetrics;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.runner.dto.ResourcesDescriptor;
import com.codenvy.api.runner.internal.Constants;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.dto.server.DtoFactory;

import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.codenvy.api.util.BillingDates.getCurrentPeriodEndDate;
import static com.codenvy.api.util.BillingDates.getCurrentPeriodStartDate;
import static java.lang.String.format;

/**
 * Implementation of {@link com.codenvy.api.account.server.ResourcesManager}
 *
 * @author Sergii Leschenko
 * @author Max Shaposhnik
 */
public class ResourcesManagerImpl implements ResourcesManager {
    private static final Logger LOG = LoggerFactory.getLogger(ResourcesManagerImpl.class);

    private final AccountDao        accountDao;
    private final WorkspaceDao      workspaceDao;
    private final MeterBasedStorage meterBasedStorage;
    private final Long              freeMemory;
    private final Integer           freeMaxLimit;

    @com.google.inject.Inject(optional = true)
    @Named(Constants.RUNNER_WS_MAX_MEMORY_SIZE)
    private int defMaxMemorySize = 512;


    @Inject
    public ResourcesManagerImpl(@Named("subscription.saas.usage.free.mb_minutes") long freeMemory,
                                @Named("subscription.saas.free.max_limit_mb") int freeMaxLimit,
                                AccountDao accountDao,
                                WorkspaceDao workspaceDao,
                                MeterBasedStorage meterBasedStorage) {
        this.freeMemory = freeMemory;
        this.freeMaxLimit = freeMaxLimit;
        this.accountDao = accountDao;
        this.workspaceDao = workspaceDao;
        this.meterBasedStorage = meterBasedStorage;
    }

    @Override
    public void redistributeResources(String accountId, List<UpdateResourcesDescriptor> updates) throws NotFoundException,
                                                                                                        ServerException,
                                                                                                        ConflictException,
                                                                                                        ForbiddenException {
        final Map<String, Workspace> ownWorkspaces = new HashMap<>();
        for (Workspace workspace : workspaceDao.getByAccount(accountId)) {
            ownWorkspaces.put(workspace.getId(), workspace);
        }
        validateUpdates(accountId, updates, ownWorkspaces);

        for (UpdateResourcesDescriptor resourcesDescriptor : updates) {
            Workspace workspace = ownWorkspaces.get(resourcesDescriptor.getWorkspaceId());

            if (resourcesDescriptor.getRunnerRam() != null) {
                workspace.getAttributes()
                         .put(Constants.RUNNER_MAX_MEMORY_SIZE, Integer.toString(resourcesDescriptor.getRunnerRam()));
            }

            if (resourcesDescriptor.getBuilderTimeout() != null) {
                workspace.getAttributes().put(com.codenvy.api.builder.internal.Constants.BUILDER_EXECUTION_TIME,
                                              Integer.toString(resourcesDescriptor.getBuilderTimeout()));
            }

            if (resourcesDescriptor.getRunnerTimeout() != null) {
                workspace.getAttributes().put(Constants.RUNNER_LIFETIME,
                                              Integer.toString(resourcesDescriptor.getRunnerTimeout()));
            }
            workspaceDao.update(workspace);
            if (resourcesDescriptor.getRunnerRam() != null) {
                publishResourcesChangedWsEvent(resourcesDescriptor.getWorkspaceId(),
                                               Integer.toString(resourcesDescriptor.getRunnerRam()));
            }
        }
    }

    @Override
    public AccountMetrics getAccountMetrics(String accountId) throws ServerException, NotFoundException {
        final Account account = accountDao.getById(accountId);
        final Map<String, String> accountAttributes = account.getAttributes();
        final boolean isPaid = accountAttributes.containsKey("codenvy:paid") &&
                               "true".equals(accountAttributes.get("codenvy:paid"));

        final List<WorkspaceMetrics> workspacesMetrics = getWorkspacesMetricsByAccount(accountId);
        Long usedMemory = 0L;
        for (WorkspaceMetrics workspaceMetrics : workspacesMetrics) {
            usedMemory += workspaceMetrics.getUsedMemoryInCurrentBillingPeriod();
        }

        return DtoFactory.getInstance().createDto(AccountMetrics.class)
                         .withWorkspaceMetrics(workspacesMetrics)
                         .withUsedMemoryInCurrentBillingPeriod(usedMemory)
                         .withFreeMemory(freeMemory)
                         .withResourcesResetTime(getCurrentPeriodEndDate().getTime())
                         .withMaxWorkspaceMemoryLimit(isPaid ? -1 : freeMaxLimit)
                         .withPremium(isPaid);
    }

    private List<WorkspaceMetrics> getWorkspacesMetricsByAccount(String accountId) throws ServerException {
        final Map<String, Long> memoryUsedReport = meterBasedStorage.getMemoryUsedReport(accountId,
                                                                                         getCurrentPeriodStartDate().getTime(),
                                                                                         System.currentTimeMillis());

        final List<Workspace> workspaces = workspaceDao.getByAccount(accountId);
        final List<WorkspaceMetrics> result = new ArrayList<>();

        for (Workspace workspace : workspaces) {
            final Long usedMemory = memoryUsedReport.get(workspace.getId());
            final String maxRunnerRam = workspace.getAttributes().get(Constants.RUNNER_MAX_MEMORY_SIZE);
            result.add(DtoFactory.getInstance().createDto(WorkspaceMetrics.class)
                                 .withWorkspaceId(workspace.getId())
                                 .withUsedMemoryInCurrentBillingPeriod(usedMemory != null ? usedMemory : 0)
                                 .withWorkspaceMemoryLimit(maxRunnerRam != null ? Integer.parseInt(maxRunnerRam) : defMaxMemorySize));
        }

        return result;
    }

    private void validateUpdates(String accountId, List<UpdateResourcesDescriptor> updates, Map<String, Workspace> ownWorkspaces)
            throws ForbiddenException, ConflictException, NotFoundException, ServerException {

        for (UpdateResourcesDescriptor resourcesDescriptor : updates) {
            if (!ownWorkspaces.containsKey(resourcesDescriptor.getWorkspaceId())) {
                throw new ForbiddenException(
                        format("Workspace %s is not related to account %s", resourcesDescriptor.getWorkspaceId(),
                               accountId));
            }

            if (resourcesDescriptor.getRunnerTimeout() == null && resourcesDescriptor.getRunnerRam() == null &&
                resourcesDescriptor.getBuilderTimeout() == null) {
                throw new ConflictException(format("Missed description of resources for workspace %s",
                                                   resourcesDescriptor.getWorkspaceId()));
            }
            Integer runnerRam = resourcesDescriptor.getRunnerRam();
            if (runnerRam != null) {
                if (runnerRam < 0) {
                    throw new ConflictException(format("Size of RAM for workspace %s is a negative number",
                                                       resourcesDescriptor.getWorkspaceId()));
                }
                Account account = accountDao.getById(accountId);
                if (!account.getAttributes().containsKey("codenvy:paid") && runnerRam > freeMaxLimit) {
                    throw new ConflictException(format("Size of RAM for workspace %s has a 4096 MB limit.",
                                                       resourcesDescriptor.getWorkspaceId()));

                }

            }
            if (resourcesDescriptor.getBuilderTimeout() != null) {
                if (resourcesDescriptor.getBuilderTimeout() < 0) {
                    throw new ConflictException(format("Builder timeout for workspace %s is a negative number",
                                                       resourcesDescriptor.getWorkspaceId()));
                }
            }

            if (resourcesDescriptor.getRunnerTimeout() != null) {
                if (resourcesDescriptor.getRunnerTimeout() < -1) { // we allow -1 here
                    throw new ConflictException(format("Runner timeout for workspace %s is a negative number",
                                                       resourcesDescriptor.getWorkspaceId()));
                }
            }
        }
    }

    private void publishResourcesChangedWsEvent(String workspaceId, String totalMemory) {
        try {
            final ChannelBroadcastMessage bm = new ChannelBroadcastMessage();

            bm.setChannel(format("workspace:resources:%s", workspaceId));

            final ResourcesDescriptor resourcesDescriptor = DtoFactory.getInstance().createDto(ResourcesDescriptor.class)
                                                                      .withTotalMemory(totalMemory);

            bm.setBody(DtoFactory.getInstance().toJson(resourcesDescriptor));
            WSConnectionContext.sendMessage(bm);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
