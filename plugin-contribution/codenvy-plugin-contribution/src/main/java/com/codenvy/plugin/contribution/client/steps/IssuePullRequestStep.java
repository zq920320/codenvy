/*
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
import com.codenvy.plugin.contribution.vcs.client.hosting.NoCommitsInPullRequestException;
import com.codenvy.plugin.contribution.vcs.client.hosting.PullRequestAlreadyExistsException;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingServiceProvider;
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.PullRequest;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.validation.constraints.NotNull;
import javax.inject.Inject;

import static com.codenvy.plugin.contribution.client.steps.events.StepEvent.Step.ISSUE_PULL_REQUEST;

/**
 * Create the pull request on the remote VCS repository.
 *
 * @author Kevin Pollet
 */
public class IssuePullRequestStep implements Step {
    private final VcsHostingServiceProvider vcsHostingServiceProvider;
    private final ContributeMessages        messages;

    @Inject
    public IssuePullRequestStep(@NotNull final VcsHostingServiceProvider vcsHostingServiceProvider,
                                @NotNull final ContributeMessages messages) {
        this.vcsHostingServiceProvider = vcsHostingServiceProvider;
        this.messages = messages;
    }

    @Override
    public void execute(@NotNull final ContributorWorkflow workflow) {
        final Context context = workflow.getContext();
        final Configuration configuration = workflow.getConfiguration();
        final String upstreamRepositoryOwner = context.getUpstreamRepositoryOwner();
        final String upstreamRepositoryName = context.getUpstreamRepositoryName();
        final String contributionTitle = configuration.getContributionTitle();
        final String contributionComment = configuration.getContributionComment();

        vcsHostingServiceProvider.getVcsHostingService(new AsyncCallback<VcsHostingService>() {
            @Override
            public void onFailure(final Throwable exception) {
                workflow.fireStepErrorEvent(ISSUE_PULL_REQUEST, exception.getMessage());
            }

            @Override
            public void onSuccess(final VcsHostingService vcsHostingService) {
                vcsHostingService.createPullRequest(upstreamRepositoryOwner, upstreamRepositoryName, context.getHostUserLogin(),
                                                    context.getForkedRepositoryName(), context.getWorkBranchName(),
                                                    context.getClonedBranchName(), contributionTitle, contributionComment,
                                                    new AsyncCallback<PullRequest>() {
                                                        @Override
                                                        public void onSuccess(final PullRequest pullRequest) {
                                                            context.setPullRequestIssueNumber(pullRequest.getNumber());
                                                            context.setPullRequestId(pullRequest.getId());
                                                            workflow.fireStepDoneEvent(ISSUE_PULL_REQUEST);
                                                        }

                                                        @Override
                                                        public void onFailure(final Throwable exception) {
                                                            if (exception instanceof PullRequestAlreadyExistsException) {
                                                                // TODO: consider creating separate UpdatePullRequest step
                                                                if (!context.isUpdateMode()) {
                                                                    workflow.fireStepErrorEvent(ISSUE_PULL_REQUEST, exception.getMessage());
                                                                    return;
                                                                }
                                                                vcsHostingService
                                                                        .getPullRequest(upstreamRepositoryOwner, upstreamRepositoryName,
                                                                                        context.getHostUserLogin(),
                                                                                        context.getWorkBranchName(),
                                                                                        new AsyncCallback<PullRequest>() {
                                                                                            @Override
                                                                                            public void onSuccess(
                                                                                                    final PullRequest pullRequest) {
                                                                                                context.setPullRequestIssueNumber(
                                                                                                        pullRequest.getNumber());
                                                                                                context.setPullRequestId(
                                                                                                        pullRequest.getId());
                                                                                                workflow.fireStepDoneEvent(
                                                                                                        ISSUE_PULL_REQUEST);
                                                                                            }

                                                                                            @Override
                                                                                            public void onFailure(
                                                                                                    final Throwable exception) {
                                                                                                workflow.fireStepErrorEvent(
                                                                                                        ISSUE_PULL_REQUEST,
                                                                                                        exception.getMessage());
                                                                                            }
                                                                                        });

                                                            } else if (exception instanceof NoCommitsInPullRequestException) {
                                                                workflow.fireStepErrorEvent(ISSUE_PULL_REQUEST,
                                                                                            messages.stepIssuePullRequestErrorCreatePullRequestWithoutCommits());

                                                            } else {
                                                                workflow.fireStepErrorEvent(ISSUE_PULL_REQUEST,
                                                                                            messages.stepIssuePullRequestErrorCreatePullRequest());
                                                            }
                                                        }
                                                    });
            }
        });
    }
}
