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
import com.codenvy.plugin.contribution.client.utils.NotificationHelper;
import com.codenvy.plugin.contribution.vcs.client.VcsService;
import com.codenvy.plugin.contribution.vcs.client.VcsServiceProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.validation.constraints.NotNull;
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
    public CheckoutBranchToPushStep(@NotNull final AddForkRemoteStep addForkRemoteStep,
                                    @NotNull final VcsServiceProvider vcsServiceProvider,
                                    @NotNull final ContributeMessages messages,
                                    @NotNull final NotificationHelper notificationHelper,
                                    @NotNull final WaitForkOnRemoteStepFactory waitRemoteStepFactory) {
        this.addForkRemoteStep = waitRemoteStepFactory.create(addForkRemoteStep);
        this.vcsServiceProvider = vcsServiceProvider;
        this.messages = messages;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public void execute(@NotNull final ContributorWorkflow workflow) {
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
