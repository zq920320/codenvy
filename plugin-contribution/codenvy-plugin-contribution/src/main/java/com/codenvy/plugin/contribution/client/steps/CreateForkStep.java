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

import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.vcs.client.hosting.NoUserForkException;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingServiceProvider;
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.Repository;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.codenvy.plugin.contribution.client.steps.events.StepEvent.Step.CREATE_FORK;

/**
 * Create a fork of the contributed project (upstream) to push the user's contribution.
 */
public class CreateForkStep implements Step {
    private final VcsHostingServiceProvider vcsHostingServiceProvider;
    private final ContributeMessages        messages;
    private final Step                      checkoutBranchToPushStep;

    @Inject
    public CreateForkStep(@Nonnull final VcsHostingServiceProvider vcsHostingServiceProvider,
                          @Nonnull final ContributeMessages messages,
                          @Nonnull final CheckoutBranchToPushStep checkoutBranchToPushStep) {
        this.vcsHostingServiceProvider = vcsHostingServiceProvider;
        this.messages = messages;
        this.checkoutBranchToPushStep = checkoutBranchToPushStep;
    }

    @Override
    public void execute(@Nonnull final ContributorWorkflow workflow) {
        final Context context = workflow.getContext();
        final String originRepositoryOwner = context.getOriginRepositoryOwner();
        final String originRepositoryName = context.getOriginRepositoryName();
        final String upstreamRepositoryOwner = context.getUpstreamRepositoryOwner();
        final String upstreamRepositoryName = context.getUpstreamRepositoryName();

        // the upstream repository has been cloned a fork must be created
        if (originRepositoryOwner.equalsIgnoreCase(upstreamRepositoryOwner) &&
            originRepositoryName.equalsIgnoreCase(upstreamRepositoryName)) {

            vcsHostingServiceProvider.getVcsHostingService(new AsyncCallback<VcsHostingService>() {
                @Override
                public void onFailure(final Throwable exception) {
                    workflow.fireStepErrorEvent(CREATE_FORK, exception.getMessage());
                }

                @Override
                public void onSuccess(final VcsHostingService vcsHostingService) {
                    vcsHostingService.getUserFork(context.getHostUserLogin(), upstreamRepositoryOwner, upstreamRepositoryName,
                                                  new AsyncCallback<Repository>() {
                                                      @Override
                                                      public void onSuccess(final Repository fork) {
                                                          proceed(fork.getName(), workflow);
                                                      }

                                                      @Override
                                                      public void onFailure(final Throwable exception) {
                                                          if (exception instanceof NoUserForkException) {
                                                              createFork(workflow, upstreamRepositoryOwner, upstreamRepositoryName);
                                                              return;
                                                          }

                                                          workflow.fireStepErrorEvent(CREATE_FORK, exception.getMessage());
                                                      }
                                                  });
                }
            });

        } else {
            // user fork has been cloned
            proceed(originRepositoryName, workflow);
        }
    }

    private void createFork(final ContributorWorkflow workflow, final String upstreamRepositoryOwner, final String upstreamRepositoryName) {
        vcsHostingServiceProvider.getVcsHostingService(new AsyncCallback<VcsHostingService>() {
            @Override
            public void onFailure(final Throwable exception) {
                workflow.fireStepErrorEvent(CREATE_FORK, exception.getMessage());
            }

            @Override
            public void onSuccess(final VcsHostingService vcsHostingService) {
                vcsHostingService.fork(upstreamRepositoryOwner, upstreamRepositoryName, new AsyncCallback<Repository>() {
                    @Override
                    public void onSuccess(final Repository result) {
                        proceed(result.getName(), workflow);
                    }

                    @Override
                    public void onFailure(final Throwable exception) {
                        final String errorMessage = messages.stepCreateForkErrorCreatingFork(upstreamRepositoryOwner,
                                                                                             upstreamRepositoryName,
                                                                                             exception.getMessage());

                        workflow.fireStepErrorEvent(CREATE_FORK, errorMessage);
                    }
                });
            }
        });
    }

    private void proceed(final String forkName, final ContributorWorkflow workflow) {
        workflow.getContext().setForkedRepositoryName(forkName);
        workflow.fireStepDoneEvent(CREATE_FORK);
        workflow.setStep(checkoutBranchToPushStep);
        workflow.executeStep();
    }
}
