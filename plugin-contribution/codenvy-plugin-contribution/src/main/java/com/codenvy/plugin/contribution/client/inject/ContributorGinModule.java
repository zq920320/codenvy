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

import com.codenvy.plugin.contribution.client.bitbucket.BitbucketContributionWorkflow;
import com.codenvy.plugin.contribution.client.bitbucket.BitbucketStagesProvider;
import com.codenvy.plugin.contribution.client.github.GithubStagesProvider;
import com.codenvy.plugin.contribution.client.parts.contribute.StagesProvider;
import com.codenvy.plugin.contribution.client.steps.PushBranchStepFactory;
import com.codenvy.plugin.contribution.client.steps.AddForkRemoteStepFactory;
import com.codenvy.plugin.contribution.client.vsts.VstsContributionWorkflow;
import com.codenvy.plugin.contribution.client.vsts.VstsStagesProvider;
import com.codenvy.plugin.contribution.client.workflow.ContributionWorkflow;
import com.codenvy.plugin.contribution.client.github.GitHubContributionWorkflow;
import com.codenvy.plugin.contribution.client.dialogs.commit.CommitView;
import com.codenvy.plugin.contribution.client.dialogs.commit.CommitViewImpl;
import com.codenvy.plugin.contribution.client.parts.contribute.ContributePartPresenter;
import com.codenvy.plugin.contribution.client.parts.contribute.ContributePartView;
import com.codenvy.plugin.contribution.client.parts.contribute.ContributePartViewImpl;
import com.codenvy.plugin.contribution.client.steps.PushBranchOnForkStep;
import com.codenvy.plugin.contribution.client.steps.WaitForkOnRemoteStepFactory;
import com.codenvy.plugin.contribution.vcs.client.hosting.BitbucketHostingService;
import com.codenvy.plugin.contribution.client.workflow.WorkflowExecutor;
import com.codenvy.plugin.contribution.vcs.client.hosting.GitHubHostingService;
import com.codenvy.plugin.contribution.vcs.client.hosting.MicrosoftHostingService;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMapBinder;

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
        bind(WorkflowExecutor.class).in(Singleton.class);
        bind(PushBranchOnForkStep.class);
        install(new GinFactoryModuleBuilder().build(WaitForkOnRemoteStepFactory.class));
        install(new GinFactoryModuleBuilder().build(PushBranchStepFactory.class));
        install(new GinFactoryModuleBuilder().build(AddForkRemoteStepFactory.class));

        final GinMapBinder<String, ContributionWorkflow> workflowBinder
                = GinMapBinder.newMapBinder(binder(),
                                            String.class,
                                            ContributionWorkflow.class);
        workflowBinder.addBinding(GitHubHostingService.SERVICE_NAME).to(GitHubContributionWorkflow.class);
        workflowBinder.addBinding(MicrosoftHostingService.SERVICE_NAME).to(VstsContributionWorkflow.class);
        workflowBinder.addBinding(BitbucketHostingService.SERVICE_NAME).to(BitbucketContributionWorkflow.class);

        final GinMapBinder<String, StagesProvider> stagesProvider
                = GinMapBinder.newMapBinder(binder(),
                                            String.class,
                                            StagesProvider.class);
        stagesProvider.addBinding(GitHubHostingService.SERVICE_NAME).to(GithubStagesProvider.class);
        stagesProvider.addBinding(MicrosoftHostingService.SERVICE_NAME).to(VstsStagesProvider.class);
        stagesProvider.addBinding(BitbucketHostingService.SERVICE_NAME).to(BitbucketStagesProvider.class);
    }
}
