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
package com.codenvy.api.account.metrics;

import org.eclipse.che.api.builder.BuildQueue;
import org.eclipse.che.api.builder.dto.BaseBuilderRequest;
import org.eclipse.che.api.builder.dto.DependencyRequest;
import org.eclipse.che.api.builder.internal.BuilderEvent;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;

/**
 * Base class for subscribing only on metered build events
 *
 * @author Sergii Leschenko
 */
public abstract class MeteredBuildEventSubscriber implements EventSubscriber<BuilderEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(MeteredBuildEventSubscriber.class);

    private final BuildQueue buildQueue;

    public MeteredBuildEventSubscriber(BuildQueue buildQueue) {
        this.buildQueue = buildQueue;
    }

    /**
     * Receives notification that an metered build event has been published to the EventService.
     * If the method throws an unchecked exception it is ignored.
     */
    public abstract void onMeteredBuildEvent(BuilderEvent event);

    @Override
    public void onEvent(BuilderEvent event) {
        try {
            final BaseBuilderRequest request = buildQueue.getTask(event.getTaskId()).getRequest();
            if (!(request instanceof DependencyRequest)) {
                onMeteredBuildEvent(event);
            }
        } catch (NotFoundException e) {
            LOG.warn(format("Unable to determine request type for request %s", event.getTaskId()), e);
        }
    }
}
