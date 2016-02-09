/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.plugin.contribution.client.steps;

import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.vcs.client.hosting.NoUserForkException;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingServiceProvider;
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.Repository;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.validation.constraints.NotNull;
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
    public CreateForkStep(@NotNull final VcsHostingServiceProvider vcsHostingServiceProvider,
                          @NotNull final ContributeMessages messages,
                          @NotNull final CheckoutBranchToPushStep checkoutBranchToPushStep) {
        this.vcsHostingServiceProvider = vcsHostingServiceProvider;
        this.messages = messages;
        this.checkoutBranchToPushStep = checkoutBranchToPushStep;
    }

    @Override
    public void execute(@NotNull final ContributorWorkflow workflow) {
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
