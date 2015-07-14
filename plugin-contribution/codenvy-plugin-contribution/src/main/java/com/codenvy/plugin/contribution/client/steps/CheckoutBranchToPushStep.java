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
import com.codenvy.plugin.contribution.client.utils.NotificationHelper;
import com.codenvy.plugin.contribution.vcs.client.VcsService;
import com.codenvy.plugin.contribution.vcs.client.VcsServiceProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.codenvy.plugin.contribution.client.steps.events.StepEvent.Step.CHECKOUT_BRANCH_TO_PUSH;

/**
 * This step checkout the branch to push on the user repository for the contribution.
 * <ul>
 * <li>If the branch exists a checkout is executed
 * <li>If the branch doesn't exist the branch is created and then a checkout is executed
 * </ul>
 *
 * @author Kevin Pollet
 */
public class CheckoutBranchToPushStep implements Step {
    private final Step               addForkRemoteStep;
    private final VcsServiceProvider vcsServiceProvider;
    private final ContributeMessages messages;
    private final NotificationHelper notificationHelper;

    @Inject
    public CheckoutBranchToPushStep(@Nonnull final AddForkRemoteStep addForkRemoteStep,
                                    @Nonnull final VcsServiceProvider vcsServiceProvider,
                                    @Nonnull final ContributeMessages messages,
                                    @Nonnull final NotificationHelper notificationHelper,
                                    @Nonnull final WaitForkOnRemoteStepFactory waitRemoteStepFactory) {
        this.addForkRemoteStep = waitRemoteStepFactory.create(addForkRemoteStep);
        this.vcsServiceProvider = vcsServiceProvider;
        this.messages = messages;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public void execute(@Nonnull final ContributorWorkflow workflow) {
        final Context context = workflow.getContext();
        final VcsService vcsService = vcsServiceProvider.getVcsService();
        final String contributionBranchName = workflow.getConfiguration().getContributionBranchName();

        vcsService.isLocalBranchWithName(context.getProject(), contributionBranchName, new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(final Throwable exception) {
                workflow.fireStepErrorEvent(CHECKOUT_BRANCH_TO_PUSH);
                notificationHelper.showError(CheckoutBranchToPushStep.class, messages.stepCheckoutBranchToPushErrorListLocalBranches());
            }

            @Override
            public void onSuccess(final Boolean branchExists) {
                vcsService.checkoutBranch(context.getProject(), contributionBranchName, !branchExists, new AsyncCallback<String>() {
                    @Override
                    public void onFailure(final Throwable exception) {
                        workflow.fireStepErrorEvent(CHECKOUT_BRANCH_TO_PUSH);
                        notificationHelper
                                .showError(CheckoutBranchToPushStep.class, messages.stepCheckoutBranchToPushErrorCheckoutLocalBranch());
                    }

                    @Override
                    public void onSuccess(final String branchName) {
                        context.setWorkBranchName(contributionBranchName);

                        workflow.fireStepDoneEvent(CHECKOUT_BRANCH_TO_PUSH);
                        notificationHelper.showInfo(messages.stepCheckoutBranchToPushLocalBranchCheckedOut(contributionBranchName));

                        workflow.setStep(addForkRemoteStep);
                        workflow.executeStep();
                    }
                });
            }
        });
    }
}
