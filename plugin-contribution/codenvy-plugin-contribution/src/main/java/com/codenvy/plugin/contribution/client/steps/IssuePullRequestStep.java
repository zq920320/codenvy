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
import com.codenvy.plugin.contribution.vcs.client.hosting.NoHistoryInCommonException;
import com.codenvy.plugin.contribution.vcs.client.hosting.PullRequestAlreadyExistsException;
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.PullRequest;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;

import javax.inject.Inject;

import static com.codenvy.plugin.contribution.client.steps.events.StepEvent.Step.ISSUE_PULL_REQUEST;

/**
 * Create the pull request on the remote VCS repository.
 *
 * @author Kevin Pollet
 */
public class IssuePullRequestStep implements Step {
    private final ContributeMessages messages;

    @Inject
    public IssuePullRequestStep(final ContributeMessages messages) {
        this.messages = messages;
    }

    @Override
    public void execute(final ContributorWorkflow workflow) {
        final Context context = workflow.getContext();
        final Configuration configuration = workflow.getConfiguration();
        workflow.getContext()
                .getVcsHostingService()
                .createPullRequest(context.getUpstreamRepositoryOwner(),
                                   context.getUpstreamRepositoryName(),
                                   context.getHostUserLogin(),
                                   context.getForkedRepositoryName(),
                                   context.getWorkBranchName(),
                                   context.getClonedBranchName(),
                                   configuration.getContributionTitle(),
                                   configuration.getContributionComment())
                .then(new Operation<PullRequest>() {
                    @Override
                    public void apply(PullRequest pullRequest) throws OperationException {
                        context.setPullRequestIssueNumber(pullRequest.getNumber());
                        context.setPullRequestId(pullRequest.getId());
                        workflow.fireStepDoneEvent(ISSUE_PULL_REQUEST);
                    }
                })
                .catchError(new Operation<PromiseError>() {
                    @Override
                    public void apply(final PromiseError exception) throws OperationException {
                        try {
                            throw exception.getCause();
                        } catch (PullRequestAlreadyExistsException prEx) {
                            handlePrExistsEx(prEx, workflow);
                        } catch (NoCommitsInPullRequestException noCommitsEx) {
                            handleNoCommitsInPrEx(noCommitsEx, workflow);
                        } catch (NoHistoryInCommonException noHistory) {
                            workflow.fireStepErrorEvent(ISSUE_PULL_REQUEST, noHistory.getMessage());
                        } catch (Throwable thr) {
                            handleThrowable(thr, workflow);
                        }
                    }
                });
    }

    private void handleThrowable(final Throwable thr, final ContributorWorkflow workflow) {
        workflow.fireStepErrorEvent(ISSUE_PULL_REQUEST, messages.stepIssuePullRequestErrorCreatePullRequest());
    }

    private void handleNoCommitsInPrEx(final NoCommitsInPullRequestException ex, final ContributorWorkflow workflow) {
        workflow.fireStepErrorEvent(ISSUE_PULL_REQUEST, messages.stepIssuePullRequestErrorCreatePullRequestWithoutCommits());
    }

    private void handlePrExistsEx(final PullRequestAlreadyExistsException ex, final ContributorWorkflow workflow) {
        final Context context = workflow.getContext();
        // TODO: consider creating separate UpdatePullRequest step
        if (!context.isUpdateMode()) {
            workflow.fireStepErrorEvent(ISSUE_PULL_REQUEST, ex.getMessage());
            return;
        }
        context.getVcsHostingService()
               .getPullRequest(context.getUpstreamRepositoryOwner(),
                               context.getUpstreamRepositoryName(),
                               context.getHostUserLogin(),
                               context.getWorkBranchName())
               .then(new Operation<PullRequest>() {
                   @Override
                   public void apply(PullRequest pullRequest) throws OperationException {
                       context.setPullRequestIssueNumber(pullRequest.getNumber());
                       context.setPullRequestId(pullRequest.getId());
                       workflow.fireStepDoneEvent(ISSUE_PULL_REQUEST);
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(PromiseError err) throws OperationException {
                       workflow.fireStepErrorEvent(ISSUE_PULL_REQUEST, err.getMessage());
                   }
               });
    }
}
