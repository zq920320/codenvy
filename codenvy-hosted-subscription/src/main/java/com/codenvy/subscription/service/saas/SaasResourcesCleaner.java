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

import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.core.notification.EventSubscriber;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.workspace.event.DeleteWorkspaceEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

import static java.lang.String.format;

/**
 * Moves resources from removed extra workspace to primary workspace.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class SaasResourcesCleaner implements EventSubscriber<DeleteWorkspaceEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(SaasResourcesCleaner.class);

    private final WorkspaceDao workspaceDao;
    private final EventService eventService;
    private final boolean      onPremises;

    @Inject
    public SaasResourcesCleaner(WorkspaceDao workspaceDao,
                                EventService eventService,
                                @Named("subscription.orgaddon.enabled") boolean onPremises) {
        this.workspaceDao = workspaceDao;
        this.eventService = eventService;
        this.onPremises = onPremises;
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
    public void onEvent(DeleteWorkspaceEvent event) {
        Workspace workspaceToDelete = event.getWorkspace();
        //moving RAM to primary workspace
        if (!onPremises && "extra".equals(workspaceToDelete.getAttributes().get("codenvy:role"))) {
            try {
                //removing extra workspace
                Workspace primaryWorkspace = getPrimaryWorkspace(workspaceToDelete.getAccountId());

                if (workspaceToDelete.getAttributes().containsKey("codenvy:runner_ram")) {
                    addRAMToWorkspace(primaryWorkspace, workspaceToDelete.getAttributes().get("codenvy:runner_ram"));
                }
            } catch (ApiException exception) {
                LOG.error(exception.getMessage(), exception);
            }
        }
    }

    private Workspace getPrimaryWorkspace(String accountId) throws ApiException {
        List<Workspace> workspaces = workspaceDao.getByAccount(accountId);
        //finding primary
        for (Workspace workspace : workspaces) {
            if (!workspace.getAttributes().containsKey("codenvy:role")) {
                return workspace;
            }
        }

        throw new ApiException(format("Can't find primary workspace for account %s", accountId));
    }

    private void addRAMToWorkspace(Workspace targetWorkspace, String RAM) throws ApiException {
        try {
            int movedRAM = Integer.parseInt(RAM);
            int oldRAM = 0;
            if (targetWorkspace.getAttributes().containsKey("codenvy:runner_ram")) {
                oldRAM = Integer.parseInt(targetWorkspace.getAttributes().get("codenvy:runner_ram"));
            }
            targetWorkspace.getAttributes().put("codenvy:runner_ram", String.valueOf(oldRAM + movedRAM));
        } catch (NumberFormatException nfe) {
            throw new ApiException(format("Can't add %s of RAM to workspace %s. %s", RAM, targetWorkspace.getId(), nfe.getMessage()));
        }

        workspaceDao.update(targetWorkspace);
    }
}
