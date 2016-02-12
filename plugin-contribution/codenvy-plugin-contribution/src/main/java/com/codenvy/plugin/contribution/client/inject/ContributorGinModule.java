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
package com.codenvy.plugin.contribution.client.inject;

import com.codenvy.plugin.contribution.client.dialogs.commit.CommitView;
import com.codenvy.plugin.contribution.client.dialogs.commit.CommitViewImpl;
import com.codenvy.plugin.contribution.client.parts.contribute.ContributePartPresenter;
import com.codenvy.plugin.contribution.client.parts.contribute.ContributePartView;
import com.codenvy.plugin.contribution.client.parts.contribute.ContributePartViewImpl;
import com.codenvy.plugin.contribution.client.steps.AddForkRemoteStep;
import com.codenvy.plugin.contribution.client.steps.AddReviewFactoryLinkStep;
import com.codenvy.plugin.contribution.client.steps.AuthorizeCodenvyOnVCSHostStep;
import com.codenvy.plugin.contribution.client.steps.CheckoutBranchToPushStep;
import com.codenvy.plugin.contribution.client.steps.CommitWorkingTreeStep;
import com.codenvy.plugin.contribution.client.steps.ContributorWorkflow;
import com.codenvy.plugin.contribution.client.steps.CreateForkStep;
import com.codenvy.plugin.contribution.client.steps.DefineWorkBranchStep;
import com.codenvy.plugin.contribution.client.steps.GenerateReviewFactoryStep;
import com.codenvy.plugin.contribution.client.steps.InitializeWorkflowContextStep;
import com.codenvy.plugin.contribution.client.steps.IssuePullRequestStep;
import com.codenvy.plugin.contribution.client.steps.PushBranchOnForkStep;
import com.codenvy.plugin.contribution.client.steps.WaitForkOnRemoteStepFactory;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;

import javax.inject.Singleton;

/**
 * Gin module definition for the contributor extension.
 */
@ExtensionGinModule
public class ContributorGinModule extends AbstractGinModule {

    @Override
    protected void configure() {

        // bind the commit dialog view
        bind(CommitView.class).to(CommitViewImpl.class);

        // bind the part view
        bind(ContributePartView.class).to(ContributePartViewImpl.class);
        bind(ContributePartPresenter.class);

        // the steps
        bind(ContributorWorkflow.class).in(Singleton.class);
        bind(InitializeWorkflowContextStep.class);
        bind(DefineWorkBranchStep.class);
        bind(CommitWorkingTreeStep.class);
        bind(AuthorizeCodenvyOnVCSHostStep.class);
        bind(AddReviewFactoryLinkStep.class);
        bind(GenerateReviewFactoryStep.class);
        bind(IssuePullRequestStep.class);
        bind(PushBranchOnForkStep.class);
        bind(CheckoutBranchToPushStep.class);
        bind(AddForkRemoteStep.class);
        bind(CreateForkStep.class);
        install(new GinFactoryModuleBuilder().build(WaitForkOnRemoteStepFactory.class));
    }
}
