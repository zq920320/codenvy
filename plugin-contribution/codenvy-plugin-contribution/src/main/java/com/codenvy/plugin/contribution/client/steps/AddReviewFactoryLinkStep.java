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
import com.codenvy.plugin.contribution.client.utils.NotificationHelper;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingServiceProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.validation.constraints.NotNull;
import javax.inject.Inject;

import static com.codenvy.plugin.contribution.client.steps.events.StepEvent.Step.ADD_REVIEW_FACTORY_LINK;

/**
 * Adds a factory link to the contribution comment.
 *
 * @author Kevin Pollet
 */
public class AddReviewFactoryLinkStep implements Step {
    private final Step                      issuePullRequestStep;
    private final VcsHostingServiceProvider vcsHostingServiceProvider;
    private final ContributeMessages        messages;
    private final NotificationHelper        notificationHelper;

    @Inject
    public AddReviewFactoryLinkStep(@NotNull final IssuePullRequestStep issuePullRequestStep,
                                    @NotNull final VcsHostingServiceProvider vcsHostingServiceProvider,
                                    @NotNull final ContributeMessages messages,
                                    @NotNull final NotificationHelper notificationHelper) {
        this.issuePullRequestStep = issuePullRequestStep;
        this.messages = messages;
        this.notificationHelper = notificationHelper;
        this.vcsHostingServiceProvider = vcsHostingServiceProvider;
    }

    @Override
    public void execute(@NotNull final ContributorWorkflow workflow) {
        final String reviewFactoryUrl = workflow.getContext().getReviewFactoryUrl();
        if (reviewFactoryUrl == null) {
            notificationHelper.showWarning(messages.stepAddReviewFactoryLinkErrorAddingReviewFactoryLink());
            proceed(workflow);

        } else {
            vcsHostingServiceProvider.getVcsHostingService(new AsyncCallback<VcsHostingService>() {
                @Override
                public void onFailure(final Throwable exception) {
                    workflow.fireStepErrorEvent(ADD_REVIEW_FACTORY_LINK, exception.getMessage());
                }

                @Override
                public void onSuccess(final VcsHostingService vcsHostingService) {
                    addReviewFactoryUrlToContributionComment(vcsHostingService, workflow, reviewFactoryUrl);
                    proceed(workflow);
                }
            });
        }
    }

    /**
     * Adds the review factory link to the beginning of the contribution comment.
     *
     * @param vcsHostingService
     *         the vcs hosting service.
     * @param workflow
     *         the contributor workflow.
     * @param reviewFactoryUrl
     *         the review factory url.
     */
    private void addReviewFactoryUrlToContributionComment(final VcsHostingService vcsHostingService, final ContributorWorkflow workflow,
                                                          final String reviewFactoryUrl) {
        final Configuration contributionConfiguration = workflow.getConfiguration();
        final String formattedReviewFactoryUrl = vcsHostingService.formatReviewFactoryUrl(reviewFactoryUrl);
        final String contributionCommentWithReviewFactoryUrl =
                formattedReviewFactoryUrl + "\n\n" + contributionConfiguration.getContributionComment();

        contributionConfiguration.withContributionComment(contributionCommentWithReviewFactoryUrl);
    }

    private void proceed(final ContributorWorkflow workflow) {
        workflow.fireStepDoneEvent(ADD_REVIEW_FACTORY_LINK);
        workflow.setStep(issuePullRequestStep);
        workflow.executeStep();
    }
}
