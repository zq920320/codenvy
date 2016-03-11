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
package com.codenvy.plugin.contribution.client.bitbucket;

import com.codenvy.plugin.contribution.client.steps.AddForkRemoteStep;
import com.codenvy.plugin.contribution.client.steps.AddReviewFactoryLinkStep;
import com.codenvy.plugin.contribution.client.steps.AuthorizeCodenvyOnVCSHostStep;
import com.codenvy.plugin.contribution.client.steps.CommitWorkingTreeStep;
import com.codenvy.plugin.contribution.client.steps.CreateForkStep;
import com.codenvy.plugin.contribution.client.steps.DefineWorkBranchStep;
import com.codenvy.plugin.contribution.client.steps.DetermineUpstreamRepositoryStep;
import com.codenvy.plugin.contribution.client.steps.GenerateReviewFactoryStep;
import com.codenvy.plugin.contribution.client.steps.InitializeWorkflowContextStep;
import com.codenvy.plugin.contribution.client.steps.IssuePullRequestStep;
import com.codenvy.plugin.contribution.client.steps.PushBranchOnForkStep;
import com.codenvy.plugin.contribution.client.steps.PushBranchOnOriginStep;
import com.codenvy.plugin.contribution.client.steps.UpdatePullRequestStep;
import com.codenvy.plugin.contribution.client.workflow.Context;
import com.codenvy.plugin.contribution.client.workflow.ContributionWorkflow;
import com.codenvy.plugin.contribution.client.workflow.StepsChain;
import com.google.common.base.Supplier;

import javax.inject.Inject;

/**
 * Describes steps that are included in contribution workflow for Bitbucket repositories
 *
 * @author Mihail Kuznyetsov
 */
public class BitbucketContributionWorkflow implements ContributionWorkflow {

    private final InitializeWorkflowContextStep   initializeWorkflowContextStep;
    private final DefineWorkBranchStep            defineWorkBranchStep;
    private final CommitWorkingTreeStep           commitWorkingTreeStep;
    private final AuthorizeCodenvyOnVCSHostStep   authorizeCodenvyOnVCSHostStep;
    private final DetermineUpstreamRepositoryStep determineUpstreamRepositoryStep;
    private final CreateForkStep                  createForkStep;
    private final AddForkRemoteStep               addForkRemoteStep;
    private final PushBranchOnForkStep            pushBranchOnForkStep;
    private final GenerateReviewFactoryStep       generateReviewFactoryStep;
    private final AddReviewFactoryLinkStep        addReviewFactoryLinkStep;
    private final IssuePullRequestStep            issuePullRequestStep;
    private final UpdatePullRequestStep           updatePullRequestStep;

    @Inject
    public BitbucketContributionWorkflow(InitializeWorkflowContextStep initializeWorkflowContextStep,
                                         DefineWorkBranchStep defineWorkBranchStep,
                                         CommitWorkingTreeStep commitWorkingTreeStep,
                                         AuthorizeCodenvyOnVCSHostStep authorizeCodenvyOnVCSHostStep,
                                         DetermineUpstreamRepositoryStep determineUpstreamRepositoryStep,
                                         CreateForkStep createForkStep,
                                         AddForkRemoteStep addForkRemoteStep,
                                         PushBranchOnForkStep pushBranchOnForkStep,
                                         GenerateReviewFactoryStep generateReviewFactoryStep,
                                         AddReviewFactoryLinkStep addReviewFactoryLinkStep,
                                         IssuePullRequestStep issuePullRequestStep,
                                         UpdatePullRequestStep updatePullRequestStep) {
        this.initializeWorkflowContextStep = initializeWorkflowContextStep;
        this.defineWorkBranchStep = defineWorkBranchStep;
        this.commitWorkingTreeStep = commitWorkingTreeStep;
        this.authorizeCodenvyOnVCSHostStep = authorizeCodenvyOnVCSHostStep;
        this.determineUpstreamRepositoryStep = determineUpstreamRepositoryStep;
        this.createForkStep = createForkStep;
        this.addForkRemoteStep = addForkRemoteStep;
        this.pushBranchOnForkStep = pushBranchOnForkStep;
        this.generateReviewFactoryStep = generateReviewFactoryStep;
        this.addReviewFactoryLinkStep = addReviewFactoryLinkStep;
        this.issuePullRequestStep = issuePullRequestStep;
        this.updatePullRequestStep = updatePullRequestStep;
    }

    @Override
    public StepsChain initChain(Context context) {
        return StepsChain.first(initializeWorkflowContextStep)
                         .then(defineWorkBranchStep);
    }

    @Override
    public StepsChain creationChain(final Context context) {
        return StepsChain.first(commitWorkingTreeStep)
                         .then(authorizeCodenvyOnVCSHostStep)
                         .then(determineUpstreamRepositoryStep)
                         .then(createForkStep)
                         .then(addForkRemoteStep)
                         .then(pushBranchOnForkStep)
                         .then(generateReviewFactoryStep)
                         .thenIf(new Supplier<Boolean>() {
                             @Override
                             public Boolean get() {
                                 return context.getReviewFactoryUrl() != null;
                             }
                         }, addReviewFactoryLinkStep)
                         .then(issuePullRequestStep);
    }

    @Override
    public StepsChain updateChain(final Context context) {
        return StepsChain.first(commitWorkingTreeStep)
                         .then(authorizeCodenvyOnVCSHostStep)
                         .then(pushBranchOnForkStep)
                         .then(updatePullRequestStep);
    }
}
