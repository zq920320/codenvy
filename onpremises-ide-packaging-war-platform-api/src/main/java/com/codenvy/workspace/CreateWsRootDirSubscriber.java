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
package com.codenvy.workspace;

import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.core.notification.EventSubscriber;
import com.codenvy.vfs.impl.fs.LocalFSMountStrategy;
import com.codenvy.workspace.event.CreateWorkspaceEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;

/**
 * This class provide execution required actions in virtual file system when workspace was created
 *
 * @author Sergii Leschenko
 * @author Alexander Garagatyi
 */
@Singleton
public class CreateWsRootDirSubscriber {
    private static final Logger LOG = LoggerFactory.getLogger(CreateWsRootDirSubscriber.class);

    private final EventService eventService;

    private final EventSubscriber<CreateWorkspaceEvent> eventSubscriber;

    @Inject
    public CreateWsRootDirSubscriber(EventService eventService, final LocalFSMountStrategy mountStrategy) {
        this.eventService = eventService;
        this.eventSubscriber = new EventSubscriber<CreateWorkspaceEvent>() {
            @Override
            public void onEvent(CreateWorkspaceEvent event) {
                try {
                    File wsPath = mountStrategy.getMountPath(event.getWorkspace().getId());
                    if (!wsPath.exists()) {
                        if (!wsPath.mkdirs()) {
                            LOG.warn("Can not create root folder for workspace VFS {}", event.getWorkspace().getId());
                        }
                    }
                } catch (ServerException e) {
                    LOG.warn("Can not calculate path to root folder for workspace VFS {}", event.getWorkspace().getId());
                }
            }
        };
    }

    @PostConstruct
    private void subscribe() {
        eventService.subscribe(eventSubscriber);
    }

    @PreDestroy
    private void unsubscribe() {
        eventService.unsubscribe(eventSubscriber);
    }
}