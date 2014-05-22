/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2013] Codenvy, S.A. 
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
package com.codenvy.workspace;

import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.core.notification.EventSubscriber;
import com.codenvy.api.event.workspace.DeleteWorkspaceEvent;
import com.codenvy.workspace.event.StopWsEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

/**
 * Unregister vfs provider if comes event that ws was stopped.
 * Remove filesystem of workspace if comes event that ws was removed.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class VfsStopSubscriber {
    private static final Logger LOG = LoggerFactory.getLogger(VfsStopSubscriber.class);
    private final EventService        eventService;
    private final VfsCleanupPerformer vfsCleanupPerformer;

    @Inject
    public VfsStopSubscriber(EventService eventService, VfsCleanupPerformer vfsCleanupPerformer) {
        this.eventService = eventService;
        this.vfsCleanupPerformer = vfsCleanupPerformer;
    }

    @PostConstruct
    public void subscribe() {
        eventService.subscribe(new EventSubscriber<DeleteWorkspaceEvent>() {
            @Override
            public void onEvent(DeleteWorkspaceEvent event) {
                String id = event.getWorkspaceId();
                try {
                    vfsCleanupPerformer.unregisterProvider(id);
                } catch (IOException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }

                try {
                    vfsCleanupPerformer.removeFS(id, event.isTemporary());
                } catch (IOException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        });

        eventService.subscribe(new EventSubscriber<StopWsEvent>() {
            @Override
            public void onEvent(StopWsEvent event) {
                String id = event.getWorkspaceId();
                try {
                    vfsCleanupPerformer.unregisterProvider(id);
                } catch (IOException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        });
    }
}
