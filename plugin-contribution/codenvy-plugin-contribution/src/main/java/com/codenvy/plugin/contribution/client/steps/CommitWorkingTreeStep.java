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
import com.codenvy.plugin.contribution.client.dialogs.commit.CommitPresenter;
import com.codenvy.plugin.contribution.client.utils.NotificationHelper;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.validation.constraints.NotNull;
import javax.inject.Inject;

import static com.codenvy.plugin.contribution.client.dialogs.commit.CommitPresenter.CommitActionHandler.CommitAction.CANCEL;
import static com.codenvy.plugin.contribution.client.steps.events.StepEvent.Step.COMMIT_WORKING_TREE;

/**
 * This step allow the user to commit the current working tree if the git repository status is not clean.
 *
 * @author Kevin Pollet
 */
public class CommitWorkingTreeStep implements Step {
    private final CommitPresenter    commitPresenter;
    private final ContributeMessages messages;
    private final NotificationHelper notificationHelper;
    private final Step               createForkStep;

    @Inject
    public CommitWorkingTreeStep(@NotNull final CommitPresenter commitPresenter,
                                 @NotNull final ContributeMessages messages,
                                 @NotNull final NotificationHelper notificationHelper,
                                 @NotNull final CreateForkStep createForkStep) {
        this.commitPresenter = commitPresenter;
        this.messages = messages;
        this.notificationHelper = notificationHelper;
        this.createForkStep = createForkStep;
    }

    @Override
    public void execute(@NotNull final ContributorWorkflow workflow) {
        final Configuration configuration = workflow.getConfiguration();

        commitPresenter.setCommitActionHandler(new CommitPresenter.CommitActionHandler() {
            @Override
            public void onCommitAction(final CommitAction action) {
                if (action == CANCEL) {
                    workflow.fireStepErrorEvent(COMMIT_WORKING_TREE);

                } else {
                    proceed(workflow);
                }
            }
        });
        commitPresenter.hasUncommittedChanges(new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(final Throwable exception) {
                workflow.fireStepErrorEvent(COMMIT_WORKING_TREE);
                notificationHelper.showError(CommitWorkingTreeStep.class, exception);
            }

            @Override
            public void onSuccess(final Boolean hasUncommittedChanges) {
                if (hasUncommittedChanges) {
                    commitPresenter
                            .showView(messages.contributorExtensionDefaultCommitDescription(configuration.getContributionBranchName(),
                                                                                            configuration.getContributionTitle()));
                } else {
                    proceed(workflow);
                }
            }
        });
    }

    private void proceed(final ContributorWorkflow workflow) {
        workflow.fireStepDoneEvent(COMMIT_WORKING_TREE);
        workflow.setStep(createForkStep);
        workflow.executeStep();
    }
}
