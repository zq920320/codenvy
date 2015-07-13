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
import com.codenvy.plugin.contribution.vcs.client.BranchUpToDateException;
import com.codenvy.plugin.contribution.vcs.client.VcsService;
import com.codenvy.plugin.contribution.vcs.client.VcsServiceProvider;
import com.codenvy.plugin.contribution.vcs.client.hosting.NoPullRequestException;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingServiceProvider;
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.PullRequest;
import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.ide.ext.git.shared.PushResponse;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import static com.codenvy.plugin.contribution.client.steps.events.StepEvent.Step.PUSH_BRANCH_ON_FORK;

/**
 * Push the local contribution branch on the user fork.
 *
 * @author Kevin Pollet
 */
public class PushBranchOnForkStep implements Step {

    private final Step                      generateReviewFactoryStep;
    private final VcsServiceProvider        vcsServiceProvider;
    private final VcsHostingServiceProvider vcsHostingServiceProvider;
    private final ContributeMessages        messages;
    private final DialogFactory             dialogFactory;

    @Inject
    public PushBranchOnForkStep(@Nonnull final GenerateReviewFactoryStep generateReviewFactoryStep,
                                @Nonnull final VcsServiceProvider vcsServiceProvider,
                                @Nonnull final VcsHostingServiceProvider vcsHostingServiceProvider,
                                @NotNull final ContributeMessages messages,
                                @NotNull final DialogFactory dialogFactory) {
        this.generateReviewFactoryStep = generateReviewFactoryStep;
        this.vcsServiceProvider = vcsServiceProvider;
        this.vcsHostingServiceProvider = vcsHostingServiceProvider;
        this.messages = messages;
        this.dialogFactory = dialogFactory;
    }

    @Override
    public void execute(@Nonnull final ContributorWorkflow workflow) {
        final Context context = workflow.getContext();
        final String upstreamRepositoryOwner = context.getUpstreamRepositoryOwner();
        final String upstreamRepositoryName = context.getUpstreamRepositoryName();

        /*
         * Check if a Pull Request with given base and head branches already exists.
         * If there is none, push the contribution branch.
         * If there is one, propose to update the pull request.
         */
        vcsHostingServiceProvider.getVcsHostingService(new AsyncCallback<VcsHostingService>() {
            @Override
            public void onFailure(final Throwable exception) {
                workflow.fireStepErrorEvent(PUSH_BRANCH_ON_FORK, exception.getMessage());
            }

            @Override
            public void onSuccess(final VcsHostingService vcsHostingService) {
                vcsHostingService.getPullRequest(upstreamRepositoryOwner,
                                                 upstreamRepositoryName,
                                                 context.getHostUserLogin(),
                                                 context.getWorkBranchName(),
                                                 new AsyncCallback<PullRequest>() {
                                                     @Override
                                                     public void onSuccess(final PullRequest pullRequest) {
                                                         final ConfirmCallback okCallback = new ConfirmCallback() {
                                                             @Override
                                                             public void accepted() {
                                                                 pushBranch(workflow, context);
                                                             }
                                                         };
                                                         final CancelCallback cancelCallback = new CancelCallback() {
                                                             @Override
                                                             public void cancelled() {
                                                                 workflow.fireStepErrorEvent(PUSH_BRANCH_ON_FORK,
                                                                                             messages.stepPushBranchCanceling());
                                                             }
                                                         };

                                                         dialogFactory.createConfirmDialog(
                                                                 messages.contributePartConfigureContributionDialogUpdateTitle(),
                                                                 messages.contributePartConfigureContributionDialogUpdateText(
                                                                         pullRequest.getHead().getLabel()),
                                                                 okCallback,
                                                                 cancelCallback).show();
                                                     }

                                                     @Override
                                                     public void onFailure(final Throwable exception) {
                                                         if (exception instanceof NoPullRequestException) {
                                                             pushBranch(workflow, context);
                                                             return;
                                                         }

                                                         workflow.fireStepErrorEvent(PUSH_BRANCH_ON_FORK, exception.getMessage());
                                                     }
                                                 });
            }
        });
    }

    protected void pushBranch(final ContributorWorkflow workflow, final Context context) {
        final VcsService vcsService = vcsServiceProvider.getVcsService();
        vcsService.pushBranch(context.getProject(), context.getForkedRemoteName(), context.getWorkBranchName(),
                              new AsyncCallback<PushResponse>() {
                                  @Override
                                  public void onSuccess(final PushResponse result) {
                                      workflow.fireStepDoneEvent(PUSH_BRANCH_ON_FORK);
                                      workflow.setStep(generateReviewFactoryStep);
                                      workflow.executeStep();
                                  }

                                  @Override
                                  public void onFailure(final Throwable exception) {
                                      final String errorMessage;
                                      if (exception instanceof BranchUpToDateException) {
                                          errorMessage = messages.stepPushBranchErrorBranchUpToDate();
                                      } else {
                                          errorMessage = messages.stepPushBranchErrorPushingBranch(exception.getMessage());
                                      }

                                      workflow.fireStepErrorEvent(PUSH_BRANCH_ON_FORK, errorMessage);
                                  }
                              });
    }
}
