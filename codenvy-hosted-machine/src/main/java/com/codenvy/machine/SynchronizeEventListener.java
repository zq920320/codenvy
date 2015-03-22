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
package com.codenvy.machine;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.vfs.server.observation.MoveEvent;
import org.eclipse.che.api.vfs.server.observation.VirtualFileEvent;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Alexander Garagatyi
 */
@Singleton
public class SynchronizeEventListener implements EventSubscriber<VirtualFileEvent> {
    private EventService                                                 eventService;
    private ConcurrentHashMap<String, Set<SyncthingSynchronizeNotifier>> syncNotifiers;

    @Inject
    public SynchronizeEventListener(EventService eventService) {
        this.eventService = eventService;
        this.syncNotifiers = new ConcurrentHashMap<>();
    }

    public void addProjectSynchronizeNotifier(SyncthingSynchronizeNotifier notifier) {
        syncNotifiers.putIfAbsent(notifier.getWorkspaceId(), new LinkedHashSet<SyncthingSynchronizeNotifier>());
        syncNotifiers.get(notifier.getWorkspaceId()).add(notifier);
    }

    public void removeProjectSynchronizeNotifier(SyncthingSynchronizeNotifier notifier) {
        final Set<SyncthingSynchronizeNotifier> notifiers = syncNotifiers.get(notifier.getWorkspaceId());
        if (notifiers != null) {
            notifiers.remove(notifier);
        }
    }

    @Override
    public void onEvent(VirtualFileEvent event) {
        final String workspace = event.getWorkspaceId();
        final String path = event.getPath().substring(1);
        final Set<SyncthingSynchronizeNotifier> notifiers;
        if ((notifiers = syncNotifiers.get(workspace)) != null && !notifiers.isEmpty()) {
            for (SyncthingSynchronizeNotifier notifier : notifiers) {
                if (path.startsWith(notifier.getProject())) {
                    // synchronizer doesn't support notification with the type of changes
                    // so we should notify about changes in path where type of changes can be recognized by synchronizer
                    switch (event.getType()) {
                        case CONTENT_UPDATED:
                        case CREATED:
                        case DELETED:
                            notifier.notifySynchronizer(path.substring(notifier.getProject().length()));
                            break;
                        case RENAMED:
                            // notify about change in the parent folder
                            notifier.notifySynchronizer(new File(path.substring(notifier.getProject().length())).getParent());
                            break;
                        case MOVED:
                            // notify about change in common directory path for move source and destination
                            // fixme won't work in the case of synchronization of subproject with move operation
                            // where path or old path outside of subproject
                            MoveEvent mvEvent = (MoveEvent)event;
                            Path commonParent = null;
                            final Iterator<java.nio.file.Path> srcPathIt = Paths.get(mvEvent.getOldPath()).iterator();
                            final Iterator<java.nio.file.Path> destPathIt = Paths.get(mvEvent.getPath()).iterator();
                            for (java.nio.file.Path destParent = destPathIt.next(), srcParent = srcPathIt.next();
                                 destParent.equals(srcParent) && destPathIt.hasNext() && srcPathIt.hasNext();
                                 destParent = destPathIt.next(), srcParent = srcPathIt.next()) {
                                commonParent = destParent;
                            }
                            // commonParent must have correct value always
                            notifier.notifySynchronizer(commonParent.toString());
                            break;
                        default:
                            return;
                    }
                }
            }
        }
    }

    @PostConstruct
    private void subscribeToEvents() {
        eventService.subscribe(this);
    }

    @PreDestroy
    private void unsubscribeFromEvents() {
        eventService.unsubscribe(this);
    }

}
