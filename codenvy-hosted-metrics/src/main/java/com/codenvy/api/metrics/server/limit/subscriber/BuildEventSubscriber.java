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
package com.codenvy.api.metrics.server.limit.subscriber;

import com.codenvy.api.metrics.server.builds.MeteredBuildEventSubscriber;
import com.codenvy.api.metrics.server.limit.ActiveTasksHolder;
import com.codenvy.api.metrics.server.limit.MeteredTask;

import org.eclipse.che.api.builder.BuildQueue;
import org.eclipse.che.api.builder.internal.BuilderEvent;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.notification.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.Objects;

import static java.lang.String.format;

/**
 * @author Sergii Leschenko
 */
public class BuildEventSubscriber extends MeteredBuildEventSubscriber {
    private static final Logger LOG = LoggerFactory.getLogger(BuildEventSubscriber.class);

    private final ActiveTasksHolder activeTasksHolder;
    private final EventService      eventService;

    @Inject
    public BuildEventSubscriber(BuildQueue buildQueue, ActiveTasksHolder activeTasksHolder, EventService eventService) {
        super(buildQueue);
        this.activeTasksHolder = activeTasksHolder;
        this.eventService = eventService;
    }

    @PostConstruct
    public void subscribe() {
        eventService.subscribe(this);
    }

    @PreDestroy
    public void unsubscribe() {
        eventService.unsubscribe(this);
    }

    @Override
    public void onMeteredBuildEvent(BuilderEvent event) {
        switch (event.getType()) {
            case BUILD_TASK_ADDED_IN_QUEUE:
                activeTasksHolder.addMeteredTask(new MeteredTaskBuild(event));
                break;
            case DONE:
            case CANCELED:
                activeTasksHolder.removeMeteredTask(new MeteredTaskBuild(event));
                break;
        }
    }

    class MeteredTaskBuild implements MeteredTask {
        private final BuilderEvent builderEvent;

        @Inject
        public MeteredTaskBuild(BuilderEvent builderEvent) {
            this.builderEvent = builderEvent;
        }

        @Override
        public String getWorkspaceId() {
            return builderEvent.getWorkspace();
        }

        @Override
        public String getId() {
            return "build-" + builderEvent.getTaskId();
        }

        @Override
        public void interrupt() throws Exception {
            try {
                buildQueue.getTask(builderEvent.getTaskId()).cancel();
            } catch (NotFoundException nfe) {
                LOG.error(format("Can't interrupt build %s. %s", builderEvent.getTaskId(), nfe.getLocalizedMessage()), nfe);
                activeTasksHolder.removeMeteredTask(this);
            }
        }

        @Override
        public int hashCode() {
            return 7 * (int)(builderEvent.getTaskId() ^ (builderEvent.getTaskId() >>> 32));
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof MeteredTaskBuild)) {
                return false;
            }
            final MeteredTaskBuild other = (MeteredTaskBuild)obj;
            return Objects.equals(builderEvent.getTaskId(), other.builderEvent.getTaskId());
        }
    }
}
