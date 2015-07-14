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
package com.codenvy.plugin.contribution.client;

import com.codenvy.plugin.contribution.client.parts.contribute.ContributePartPresenter;
import com.codenvy.plugin.contribution.client.steps.ContributorWorkflow;
import com.codenvy.plugin.contribution.client.utils.NotificationHelper;
import com.codenvy.plugin.contribution.vcs.client.VcsService;
import com.codenvy.plugin.contribution.vcs.client.VcsServiceProvider;
import com.codenvy.plugin.contribution.vcs.client.hosting.NoVcsHostingServiceImplementationException;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingServiceProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.factory.dto.Factory;
import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.ImportSourceDescriptor;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.api.project.shared.dto.ProjectUpdate;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.ProjectActionEvent;
import org.eclipse.che.ide.api.event.ProjectActionHandler;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.util.loging.Log;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

import static com.codenvy.plugin.contribution.projecttype.shared.ContributionProjectTypeConstants.CONTRIBUTE_BRANCH_VARIABLE_NAME;
import static com.codenvy.plugin.contribution.projecttype.shared.ContributionProjectTypeConstants.CONTRIBUTE_MODE_VARIABLE_NAME;
import static com.codenvy.plugin.contribution.projecttype.shared.ContributionProjectTypeConstants.CONTRIBUTE_VARIABLE_NAME;
import static com.codenvy.plugin.contribution.projecttype.shared.ContributionProjectTypeConstants.CONTRIBUTION_PROJECT_TYPE_ID;
import static java.util.Arrays.asList;

/**
 * @author Stephane Tournie
 * @author Kevin Pollet
 */
@Singleton
@Extension(title = "Contributor", version = "1.0.0")
public class ContributorExtension implements ProjectActionHandler {
    private final ContributeMessages        messages;
    private final AppContext                appContext;
    private final NotificationHelper        notificationHelper;
    private final ContributePartPresenter   contributePartPresenter;
    private final ProjectServiceClient      projectService;
    private final DtoFactory                dtoFactory;
    private final DtoUnmarshallerFactory    dtoUnmarshallerFactory;
    private final ContributorWorkflow       workflow;
    private final VcsServiceProvider        vcsServiceProvider;
    private final VcsHostingServiceProvider vcsHostingServiceProvider;

    @Inject
    public ContributorExtension(@Nonnull final EventBus eventBus,
                                @Nonnull final ContributeMessages messages,
                                @Nonnull final ContributeResources resources,
                                @Nonnull final AppContext appContext,
                                @Nonnull final NotificationHelper notificationHelper,
                                @Nonnull final ContributePartPresenter contributePartPresenter,
                                @Nonnull final ProjectServiceClient projectService,
                                @Nonnull final DtoFactory dtoFactory,
                                @Nonnull final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                @Nonnull final ContributorWorkflow workflow,
                                @Nonnull final VcsServiceProvider vcsServiceProvider,
                                @Nonnull final VcsHostingServiceProvider vcsHostingServiceProvider) {
        this.messages = messages;
        this.workflow = workflow;
        this.appContext = appContext;
        this.notificationHelper = notificationHelper;
        this.contributePartPresenter = contributePartPresenter;
        this.projectService = projectService;
        this.dtoFactory = dtoFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.vcsServiceProvider = vcsServiceProvider;
        this.vcsHostingServiceProvider = vcsHostingServiceProvider;

        resources.contributeCss().ensureInjected();
        eventBus.addHandler(ProjectActionEvent.TYPE, this);
    }

    @Override
    public void onProjectOpened(final ProjectActionEvent event) {
        initializeContributorExtension(event.getProject());
    }

    @Override
    public void onProjectClosing(ProjectActionEvent event) {

    }

    @Override
    public void onProjectClosed(final ProjectActionEvent event) {
        contributePartPresenter.remove();
    }

    private void initializeContributorExtension(final ProjectDescriptor project) {
        final VcsService vcsService = vcsServiceProvider.getVcsService();
        final List<String> projectPermissions = project.getPermissions();

        if (vcsService != null && projectPermissions != null && projectPermissions.contains("write")) {
            vcsHostingServiceProvider.getVcsHostingService(new AsyncCallback<VcsHostingService>() {
                @Override
                public void onFailure(final Throwable exception) {
                    if (exception instanceof NoVcsHostingServiceImplementationException) {
                        Log.info(ContributorExtension.class, "Contribution disabled - remote VCS hosting not supported.");
                    } else {
                        notificationHelper.showError(ContributorExtension.class, exception);
                    }
                }

                @Override
                public void onSuccess(final VcsHostingService vcsHostingService) {
                    addContributionMixin(project, vcsService, new AsyncCallback<ProjectDescriptor>() {
                        @Override
                        public void onFailure(final Throwable exception) {
                            notificationHelper.showError(ContributorExtension.class,
                                                         messages.contributorExtensionErrorUpdatingContributionAttributes(
                                                                 exception.getMessage()), exception);
                        }

                        @Override
                        public void onSuccess(final ProjectDescriptor project) {
                            contributePartPresenter.open();
                            workflow.init();
                            workflow.executeStep();
                        }
                    });
                }
            });
        }
    }

    private void addContributionMixin(final ProjectDescriptor project,
                                      final VcsService vcsService,
                                      final AsyncCallback<ProjectDescriptor> callback) {

        final List<String> projectMixins = project.getMixins();
        final Map<String, List<String>> projectAttributes = project.getAttributes();

        if (!projectMixins.contains(CONTRIBUTION_PROJECT_TYPE_ID)) {
            projectMixins.add(CONTRIBUTION_PROJECT_TYPE_ID);

            // set the contribute flag
            final Factory factory = appContext.getFactory();
            if (factory != null) {
                final List<String> contributeValues = factory.getProject().getAttributes().get(CONTRIBUTE_VARIABLE_NAME);
                if (contributeValues != null && contributeValues.contains("github")) {
                    projectAttributes.put(CONTRIBUTE_VARIABLE_NAME, contributeValues);
                }
            }

            // set the contribute_mode
            projectAttributes.put(CONTRIBUTE_MODE_VARIABLE_NAME, asList("contribute"));

            // set the contribute_branch
            setClonedBranch(projectAttributes, factory, vcsService, project, new AsyncCallback<Void>() {
                @Override
                public void onFailure(final Throwable exception) {
                    callback.onFailure(exception);
                }

                @Override
                public void onSuccess(final Void notUsed) {
                    final Unmarshallable<ProjectDescriptor> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(ProjectDescriptor.class);

                    updateProjectAttributes(project, new AsyncRequestCallback<ProjectDescriptor>(unmarshaller) {
                        @Override
                        protected void onSuccess(final ProjectDescriptor projectDescriptor) {
                            callback.onSuccess(projectDescriptor);
                        }

                        @Override
                        protected void onFailure(final Throwable exception) {
                            callback.onFailure(exception);
                        }
                    });
                }
            });

        } else {
            callback.onSuccess(null);
        }
    }

    private void setClonedBranch(final Map<String, List<String>> projectAttributes,
                                 final Factory factory,
                                 final VcsService vcsService,
                                 final ProjectDescriptor project,
                                 final AsyncCallback<Void> callback) {

        if (projectAttributes.containsKey(CONTRIBUTE_BRANCH_VARIABLE_NAME)) {
            callback.onSuccess(null);

        } else {
            if (factory != null && factory.getSource() != null) {
                final ImportSourceDescriptor factoryProject = factory.getSource().getProject();
                final Map<String, String> parameters = factoryProject.getParameters();

                final String branchName = parameters.get("branch");
                if (branchName != null) {
                    projectAttributes.put(CONTRIBUTE_BRANCH_VARIABLE_NAME, asList(branchName));
                    callback.onSuccess(null);
                    return;
                }
            }

            vcsService.getBranchName(project, new AsyncCallback<String>() {
                @Override
                public void onFailure(final Throwable exception) {
                    callback.onFailure(exception);
                }

                @Override
                public void onSuccess(final String branchName) {
                    projectAttributes.put(CONTRIBUTE_BRANCH_VARIABLE_NAME, asList(branchName));
                    callback.onSuccess(null);
                }
            });
        }
    }

    private void updateProjectAttributes(final ProjectDescriptor project, final AsyncRequestCallback<ProjectDescriptor> updateCallback) {
        final ProjectUpdate projectToUpdate = dtoFactory.createDto(ProjectUpdate.class);
        copyProjectInfo(project, projectToUpdate);

        projectService.updateProject(project.getPath(), projectToUpdate, updateCallback);
    }

    private void copyProjectInfo(final ProjectDescriptor projectDescriptor, final ProjectUpdate projectUpdate) {
        projectUpdate.setType(projectDescriptor.getType());
        projectUpdate.setDescription(projectDescriptor.getDescription());
        projectUpdate.setAttributes(projectDescriptor.getAttributes());
        projectUpdate.setRunners(projectDescriptor.getRunners());
        projectUpdate.setMixinTypes(projectDescriptor.getMixins());
        projectUpdate.setBuilders(projectDescriptor.getBuilders());
        projectUpdate.setVisibility(projectDescriptor.getVisibility());
    }
}
