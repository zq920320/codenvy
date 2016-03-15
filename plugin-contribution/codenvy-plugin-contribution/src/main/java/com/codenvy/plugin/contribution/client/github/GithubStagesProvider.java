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
package com.codenvy.plugin.contribution.client.github;

import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.parts.contribute.StagesProvider;
import com.codenvy.plugin.contribution.client.steps.CommitWorkingTreeStep;
import com.codenvy.plugin.contribution.client.steps.DetectPullRequestStep;
import com.codenvy.plugin.contribution.client.workflow.Context;
import com.codenvy.plugin.contribution.client.steps.CreateForkStep;
import com.codenvy.plugin.contribution.client.steps.IssuePullRequestStep;
import com.codenvy.plugin.contribution.client.steps.PushBranchOnForkStep;
import com.codenvy.plugin.contribution.client.workflow.Step;
import com.codenvy.plugin.contribution.client.steps.UpdatePullRequestStep;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * @author Yevhenii Voevodin
 */
@Singleton
public class GithubStagesProvider implements StagesProvider {

    private static final Set<Class<? extends Step>> UPDATE_STEP_DONE_TYPES;
    private static final Set<Class<? extends Step>> CREATION_STEP_DONE_TYPES;

    static {
        UPDATE_STEP_DONE_TYPES = ImmutableSet.of(PushBranchOnForkStep.class,
                                                 UpdatePullRequestStep.class);
        CREATION_STEP_DONE_TYPES = ImmutableSet.of(CreateForkStep.class,
                                                   PushBranchOnForkStep.class,
                                                   IssuePullRequestStep.class);
    }

    private final ContributeMessages messages;

    @Inject
    public GithubStagesProvider(final ContributeMessages messages) {
        this.messages = messages;
    }

    @Override
    public List<String> getStages(final Context context) {
        if (context.isUpdateMode()) {
            return asList(messages.contributePartStatusSectionNewCommitsPushedStepLabel(),
                          messages.contributePartStatusSectionPullRequestUpdatedStepLabel());
        } else {
            return asList(messages.contributePartStatusSectionForkCreatedStepLabel(),
                          messages.contributePartStatusSectionBranchPushedForkStepLabel(),
                          messages.contributePartStatusSectionPullRequestIssuedStepLabel());
        }
    }

    @Override
    public Set<Class<? extends Step>> getStepDoneTypes(Context context) {
        return context.isUpdateMode() ? UPDATE_STEP_DONE_TYPES : CREATION_STEP_DONE_TYPES;
    }

    @Override
    public Set<Class<? extends Step>> getStepErrorTypes(Context context) {
        return getStepDoneTypes(context);
    }

    @Override
    public Class<? extends Step> getDisplayStagesType(Context context) {
        return context.isUpdateMode() ? CommitWorkingTreeStep.class : DetectPullRequestStep.class;
    }
}
