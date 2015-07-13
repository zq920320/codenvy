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
import com.codenvy.plugin.contribution.client.dialogs.commit.CommitPresenter;
import com.codenvy.plugin.contribution.client.utils.NotificationHelper;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.annotation.Nonnull;
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
    public CommitWorkingTreeStep(@Nonnull final CommitPresenter commitPresenter,
                                 @Nonnull final ContributeMessages messages,
                                 @Nonnull final NotificationHelper notificationHelper,
                                 @Nonnull final CreateForkStep createForkStep) {
        this.commitPresenter = commitPresenter;
        this.messages = messages;
        this.notificationHelper = notificationHelper;
        this.createForkStep = createForkStep;
    }

    @Override
    public void execute(@Nonnull final ContributorWorkflow workflow) {
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
