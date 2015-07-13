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
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingServiceProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.annotation.Nonnull;
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
    public AddReviewFactoryLinkStep(@Nonnull final IssuePullRequestStep issuePullRequestStep,
                                    @Nonnull final VcsHostingServiceProvider vcsHostingServiceProvider,
                                    @Nonnull final ContributeMessages messages,
                                    @Nonnull final NotificationHelper notificationHelper) {
        this.issuePullRequestStep = issuePullRequestStep;
        this.messages = messages;
        this.notificationHelper = notificationHelper;
        this.vcsHostingServiceProvider = vcsHostingServiceProvider;
    }

    @Override
    public void execute(@Nonnull final ContributorWorkflow workflow) {
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
