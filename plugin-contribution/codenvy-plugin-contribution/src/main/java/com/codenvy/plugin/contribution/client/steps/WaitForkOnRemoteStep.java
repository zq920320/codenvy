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
package com.codenvy.plugin.contribution.client.steps;

import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingServiceProvider;
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.Repository;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import javax.annotation.Nonnull;

public class WaitForkOnRemoteStep implements Step {
    private static final int POLL_FREQUENCY_MS = 1000;

    private final VcsHostingServiceProvider vcsHostingServiceProvider;
    private final Step                      nextStep;
    private       Timer                     timer;

    @AssistedInject
    public WaitForkOnRemoteStep(@Nonnull final VcsHostingServiceProvider vcsHostingServiceProvider,
                                @Nonnull final @Assisted Step nextStep) {
        this.vcsHostingServiceProvider = vcsHostingServiceProvider;
        this.nextStep = nextStep;
    }

    @Override
    public void execute(@Nonnull final ContributorWorkflow workflow) {
        if (timer == null) {
            timer = new Timer() {
                @Override
                public void run() {
                    checkRepository(workflow.getContext(), new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(final Throwable caught) {
                            timer.schedule(POLL_FREQUENCY_MS);
                        }

                        @Override
                        public void onSuccess(final Void result) {
                            workflow.setStep(nextStep);
                            workflow.executeStep();
                        }
                    });
                }
            };
        }

        timer.schedule(POLL_FREQUENCY_MS);
    }

    private void checkRepository(final Context context, final AsyncCallback<Void> callback) {
        vcsHostingServiceProvider.getVcsHostingService(new AsyncCallback<VcsHostingService>() {
            @Override
            public void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }

            @Override
            public void onSuccess(final VcsHostingService vcsHostingService) {
                vcsHostingService
                        .getRepository(context.getHostUserLogin(), context.getForkedRepositoryName(), new AsyncCallback<Repository>() {
                            @Override
                            public void onFailure(final Throwable exception) {
                                callback.onFailure(exception);
                            }

                            @Override
                            public void onSuccess(final Repository repository) {
                                callback.onSuccess(null);
                            }
                        });
            }
        });
    }
}
