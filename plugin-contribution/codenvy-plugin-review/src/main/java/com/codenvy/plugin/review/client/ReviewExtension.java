/*******************************************************************************
 * Copyright (c) 2014-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.plugin.review.client;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.api.event.ProjectActionEvent;
import org.eclipse.che.ide.api.event.ProjectActionHandler;
import org.eclipse.che.ide.api.extension.Extension;
import com.google.web.bindery.event.shared.EventBus;

@Singleton
@Extension(title = "Review", version = "1.0.0")
public class ReviewExtension implements ProjectActionHandler {
    private final ReviewMessages messages;

    @Inject
    public ReviewExtension(@Nonnull final EventBus eventBus,
                           @Nonnull final ReviewMessages messages,
                           @Nonnull final ReviewResources resources) {
        this.messages = messages;

        eventBus.addHandler(ProjectActionEvent.TYPE, this);
    }

    @Override
    public void onProjectOpened(final ProjectActionEvent event) {
        initializeReviewExtension(event.getProject());
    }

    @Override
    public void onProjectClosing(ProjectActionEvent event) {
    }

    @Override
    public void onProjectClosed(final ProjectActionEvent event) {
    }

    private void initializeReviewExtension(final ProjectDescriptor project) {
    }
}
