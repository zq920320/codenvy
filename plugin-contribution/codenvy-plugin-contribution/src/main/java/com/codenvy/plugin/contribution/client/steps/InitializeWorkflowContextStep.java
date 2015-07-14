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
import com.codenvy.plugin.contribution.vcs.client.Remote;
import com.codenvy.plugin.contribution.vcs.client.VcsService;
import com.codenvy.plugin.contribution.vcs.client.VcsServiceProvider;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingServiceProvider;
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.Repository;
import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static com.codenvy.plugin.contribution.projecttype.shared.ContributionProjectTypeConstants.CONTRIBUTE_BRANCH_VARIABLE_NAME;

/**
 * This step initialize the contribution workflow context.
 *
 * @author Kevin Pollet
 */
public class InitializeWorkflowContextStep implements Step {
    private static final String ORIGIN_REMOTE_NAME = "origin";

    private final VcsServiceProvider        vcsServiceProvider;
    private final VcsHostingServiceProvider vcsHostingServiceProvider;
    private final AppContext                appContext;
    private final NotificationHelper        notificationHelper;
    private final ContributeMessages        messages;
    private final Step                      defineWorkBranchStep;

    @Inject
    public InitializeWorkflowContextStep(@Nonnull final VcsServiceProvider vcsServiceProvider,
                                         @Nonnull final VcsHostingServiceProvider vcsHostingServiceProvider,
                                         @Nonnull final AppContext appContext,
                                         @Nonnull final NotificationHelper notificationHelper,
                                         @Nonnull final ContributeMessages messages,
                                         @Nonnull final DefineWorkBranchStep defineWorkBranchStep) {
        this.vcsServiceProvider = vcsServiceProvider;
        this.vcsHostingServiceProvider = vcsHostingServiceProvider;
        this.appContext = appContext;
        this.notificationHelper = notificationHelper;
        this.messages = messages;
        this.defineWorkBranchStep = defineWorkBranchStep;
    }

    @Override
    public void execute(@Nonnull final ContributorWorkflow workflow) {
        final Context context = workflow.getContext();
        final CurrentProject currentProject = appContext.getCurrentProject();
        final VcsService vcsService = vcsServiceProvider.getVcsService();

        if (currentProject != null && vcsService != null) {
            final ProjectDescriptor project = currentProject.getRootProject();
            final Map<String, List<String>> attributes = project.getAttributes();

            context.setProject(project);

            // get origin repository's URL from default remote
            vcsHostingServiceProvider.getVcsHostingService(new AsyncCallback<VcsHostingService>() {
                @Override
                public void onFailure(final Throwable exception) {
                    notificationHelper.showError(InitializeWorkflowContextStep.class, exception);
                }

                @Override
                public void onSuccess(final VcsHostingService vcsHostingService) {
                    vcsService.listRemotes(project, new AsyncCallback<List<Remote>>() {
                        @Override
                        public void onSuccess(final List<Remote> result) {
                            for (final Remote remote : result) {

                                // save origin repository name & owner in context
                                if (ORIGIN_REMOTE_NAME.equalsIgnoreCase(remote.getName())) {
                                    final String originUrl = remote.getUrl();
                                    final String originRepositoryName = vcsHostingService.getRepositoryNameFromUrl(originUrl);
                                    final String originRepositoryOwner = vcsHostingService.getRepositoryOwnerFromUrl(originUrl);

                                    context.setOriginRepositoryOwner(originRepositoryOwner);
                                    context.setOriginRepositoryName(originRepositoryName);

                                    // set project information
                                    if (attributes.containsKey(CONTRIBUTE_BRANCH_VARIABLE_NAME) &&
                                        !attributes.get(CONTRIBUTE_BRANCH_VARIABLE_NAME).isEmpty()) {

                                        final String clonedBranch = attributes.get(CONTRIBUTE_BRANCH_VARIABLE_NAME).get(0);
                                        context.setClonedBranchName(clonedBranch);
                                    }

                                    // here we have to determine what is the upstream repository
                                    vcsHostingService
                                            .getRepository(originRepositoryOwner, originRepositoryName, new AsyncCallback<Repository>() {
                                                @Override
                                                public void onFailure(final Throwable exception) {
                                                    notificationHelper.showError(InitializeWorkflowContextStep.class, exception);
                                                }

                                                @Override
                                                public void onSuccess(final Repository repository) {
                                                    if (repository.isFork() &&
                                                        originRepositoryOwner.equalsIgnoreCase(context.getHostUserLogin())) {
                                                        final String upstreamUrl = repository.getParent().getCloneUrl();
                                                        final String upstreamRepositoryName =
                                                                vcsHostingService.getRepositoryNameFromUrl(upstreamUrl);
                                                        final String upstreamRepositoryOwner =
                                                                vcsHostingService.getRepositoryOwnerFromUrl(upstreamUrl);

                                                        context.setUpstreamRepositoryName(upstreamRepositoryName);
                                                        context.setUpstreamRepositoryOwner(upstreamRepositoryOwner);

                                                    } else {
                                                        context.setUpstreamRepositoryName(originRepositoryName);
                                                        context.setUpstreamRepositoryOwner(originRepositoryOwner);
                                                    }

                                                    workflow.setStep(defineWorkBranchStep);
                                                    workflow.executeStep();
                                                }
                                            });
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onFailure(final Throwable exception) {
                            notificationHelper.showError(InitializeWorkflowContextStep.class,
                                                         messages.contributorExtensionErrorSetupOriginRepository(exception.getMessage()),
                                                         exception);
                        }
                    });
                }
            });
        }
    }
}
