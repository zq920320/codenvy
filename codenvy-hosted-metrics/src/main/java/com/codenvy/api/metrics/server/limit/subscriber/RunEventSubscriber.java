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

import com.codenvy.api.metrics.server.limit.ActiveTasksHolder;
import com.codenvy.api.metrics.server.limit.MeteredTask;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.runner.RunQueue;
import org.eclipse.che.api.runner.internal.RunnerEvent;
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
public class RunEventSubscriber implements EventSubscriber<RunnerEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(RunEventSubscriber.class);

    private final ActiveTasksHolder activeTasksHolder;
    private final RunQueue          runQueue;
    private final EventService      eventService;

    @Inject
    public RunEventSubscriber(ActiveTasksHolder activeTasksHolder,
                              RunQueue runQueue,
                              EventService eventService) {
        this.activeTasksHolder = activeTasksHolder;
        this.runQueue = runQueue;
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
    public void onEvent(RunnerEvent event) {
        switch (event.getType()) {
            case RUN_TASK_ADDED_IN_QUEUE:
                activeTasksHolder.addMeteredTask(new MeteredTaskRun(event));
                break;
            case ERROR:
            case STOPPED:
            case CANCELED:
                activeTasksHolder.removeMeteredTask(new MeteredTaskRun(event));
                break;
        }
    }

    class MeteredTaskRun implements MeteredTask {
        private final RunnerEvent runnerEvent;

        @Inject
        public MeteredTaskRun(RunnerEvent runnerEvent) {
            this.runnerEvent = runnerEvent;
        }

        @Override
        public String getWorkspaceId() {
            return runnerEvent.getWorkspace();
        }

        @Override
        public String getId() {
            return "run-" + runnerEvent.getProcessId();
        }

        @Override
        public void interrupt() throws Exception {
            try {
                runQueue.getTask(runnerEvent.getProcessId()).stop();
            } catch (NotFoundException nfe) {
                LOG.error(format("Can't interrupt run %s. %s", runnerEvent.getProject(), nfe.getLocalizedMessage()), nfe);
                activeTasksHolder.removeMeteredTask(this);
            }
        }

        @Override
        public int hashCode() {
            return 11 * (int)(runnerEvent.getProcessId() ^ (runnerEvent.getProcessId() >>> 32));
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof MeteredTaskRun)) {
                return false;
            }
            final MeteredTaskRun other = (MeteredTaskRun)obj;
            return Objects.equals(runnerEvent.getProcessId(), other.runnerEvent.getProcessId());
        }
    }
}
