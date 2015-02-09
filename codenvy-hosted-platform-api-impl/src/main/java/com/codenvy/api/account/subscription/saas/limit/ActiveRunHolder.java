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

import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.runner.internal.RunnerEvent;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Holder for active runs. Contains map accountId to list of runs(processId's).
 * @author Max Shaposhnik (mshaposhnik@codenvy.com) on 1/15/15.
 *
 */
@Singleton
public class ActiveRunHolder {
    private static final Logger LOG =   LoggerFactory.getLogger(ActiveRunHolder.class);
    private final  ConcurrentMap<String, Set<Long>> activeRuns = new ConcurrentHashMap<>();
    private final WorkspaceDao workspaceDao;

    @Inject
    public ActiveRunHolder(WorkspaceDao workspaceDao) {
        this.workspaceDao = workspaceDao;
    }

    public void addRun(RunnerEvent event) {
        String accountId = getAccountId(event.getWorkspace());
        if (accountId != null) {
            Set<Long> processList = activeRuns.get(accountId);
            if (processList == null) {
                final Set<Long> newProcessList = new CopyOnWriteArraySet<>();
                processList = activeRuns.putIfAbsent(accountId, newProcessList);
                if (processList == null) {
                    processList = newProcessList;
                }
            }
            processList.add(event.getProcessId());
        }
    }

    public void removeRun(RunnerEvent event) {
        String accountId = getAccountId(event.getWorkspace());
        if (accountId == null) {
            return;
        }
        final Set<Long> processIds = activeRuns.get(accountId);
        if (processIds != null) {
            processIds.remove(event.getProcessId());
            if (processIds.size() == 0) {
                activeRuns.remove(accountId);
            }
        }
    }

    private String getAccountId(String workspaceId) {
        try {
            return workspaceDao.getById(workspaceId).getAccountId();
        } catch (NotFoundException | ServerException e) {
            LOG.error("Error calculate accountId  in workspace {} .", workspaceId);
        }
        return null;
    }

    public Map<String, Set<Long>> getActiveRuns() {
        return activeRuns;
    }
}
