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

import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.project.CloseCurrentProjectEvent;
import org.eclipse.che.ide.api.event.project.CloseCurrentProjectHandler;
import org.eclipse.che.ide.api.event.project.OpenProjectEvent;
import org.eclipse.che.ide.api.event.project.OpenProjectHandler;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
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
public class ContributorExtension implements OpenProjectHandler, CloseCurrentProjectHandler {
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
    public ContributorExtension(@NotNull final EventBus eventBus,
                                @NotNull final ContributeMessages messages,
                                @NotNull final ContributeResources resources,
                                @NotNull final AppContext appContext,
                                @NotNull final NotificationHelper notificationHelper,
                                @NotNull final ContributePartPresenter contributePartPresenter,
                                @NotNull final ProjectServiceClient projectService,
                                @NotNull final DtoFactory dtoFactory,
                                @NotNull final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                @NotNull final ContributorWorkflow workflow,
                                @NotNull final VcsServiceProvider vcsServiceProvider,
                                @NotNull final VcsHostingServiceProvider vcsHostingServiceProvider) {
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

        eventBus.addHandler(OpenProjectEvent.TYPE, this);
        eventBus.addHandler(CloseCurrentProjectEvent.TYPE, this);
    }

    @Override
    public void onProjectOpened(OpenProjectEvent event) {
        initializeContributorExtension(event.getProjectConfig());
    }

    @Override
    public void onCloseCurrentProject(CloseCurrentProjectEvent event) {
        contributePartPresenter.remove();
    }

    private void initializeContributorExtension(final ProjectConfigDto project) {
        final VcsService vcsService = vcsServiceProvider.getVcsService();
//        final List<String> projectPermissions = project.getPermissions();

        if (vcsService != null) {
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
                    addContributionMixin(project, vcsService, new AsyncCallback<ProjectConfigDto>() {

                        @Override
                        public void onFailure(Throwable ex) {
                            String errMessage = messages.contributorExtensionErrorUpdatingContributionAttributes(ex.getMessage());
                            notificationHelper.showError(ContributorExtension.class, errMessage, ex);
                        }

                        @Override
                        public void onSuccess(ProjectConfigDto config) {
                            contributePartPresenter.open();
                            workflow.init();
                            workflow.executeStep();
                        }
                    });
                }
            });
        } else {
            removeContributionMixin(project, new AsyncCallback<ProjectConfigDto>() {
                @Override
                public void onFailure(Throwable exception) {
                    notificationHelper.showError(ContributorExtension.class,
                                                 messages.contributorExtensionErrorUpdatingContributionAttributes(
                                                         exception.getMessage()), exception);
                }

                @Override
                public void onSuccess(ProjectConfigDto result) {
                }
            });
        }
    }

    private void addContributionMixin(final ProjectConfigDto project,
                                      final VcsService vcsService,
                                      final AsyncCallback<ProjectConfigDto> callback) {

        final List<String> projectMixins = project.getMixins();
        final Map<String, List<String>> projectAttributes = project.getAttributes();

        if (!projectMixins.contains(CONTRIBUTION_PROJECT_TYPE_ID)) {
            projectMixins.add(CONTRIBUTION_PROJECT_TYPE_ID);

            // set the contribute flag
            final Factory factory = appContext.getFactory();
            if (factory != null) {
                final List<String> contributeValues = project.getAttributes().get(CONTRIBUTE_VARIABLE_NAME);
                if (contributeValues != null && contributeValues.contains("github")) {
                    projectAttributes.put(CONTRIBUTE_VARIABLE_NAME, contributeValues);
                }
            }

            // set the contribute_mode
            projectAttributes.put(CONTRIBUTE_MODE_VARIABLE_NAME, asList("contribute"));

            // set the contribute_branch
            setClonedBranch(projectAttributes, vcsService, project, new AsyncCallback<Void>() {
                @Override
                public void onFailure(final Throwable exception) {
                    callback.onFailure(exception);
                }

                @Override
                public void onSuccess(final Void notUsed) {
                    projectService.updateProject(appContext.getWorkspaceId(),
                                                 project.getPath(),
                                                 project,
                                                 new AsyncRequestCallback<ProjectConfigDto>() {
                                                     @Override
                                                     protected void onSuccess(ProjectConfigDto result) {
                                                         callback.onSuccess(result);
                                                     }

                                                     @Override
                                                     protected void onFailure(Throwable exception) {
                                                         callback.onFailure(exception);
                                                     }
                                                 });
                }
            });

        } else {
            callback.onSuccess(project);
        }
    }

    private void updateProject(final ProjectConfigDto project, final AsyncCallback<ProjectConfigDto> callback) {
        projectService.updateProject(appContext.getWorkspaceId(),
                                     project.getPath(),
                                     project,
                                     new AsyncRequestCallback<ProjectConfigDto>() {
                                         @Override
                                         protected void onSuccess(ProjectConfigDto result) {
                                             callback.onSuccess(result);
                                         }

                                         @Override
                                         protected void onFailure(Throwable exception) {
                                             callback.onFailure(exception);
                                         }
                                     });
    }

    private void removeContributionMixin(final ProjectConfigDto project,
                                         final AsyncCallback<ProjectConfigDto> callback) {
        final List<String> projectMixins = project.getMixins();
        final Map<String, List<String>> projectAttributes = project.getAttributes();

        if (projectMixins.contains(CONTRIBUTION_PROJECT_TYPE_ID)) {
            projectMixins.remove(CONTRIBUTION_PROJECT_TYPE_ID);
            projectAttributes.remove(CONTRIBUTE_VARIABLE_NAME);
            projectAttributes.remove(CONTRIBUTE_MODE_VARIABLE_NAME);
            projectAttributes.remove(CONTRIBUTE_BRANCH_VARIABLE_NAME);

            updateProject(project, callback);
        } else {
            callback.onSuccess(project);
        }
    }

    private void setClonedBranch(final Map<String, List<String>> projectAttributes,
                                 final VcsService vcsService,
                                 final ProjectConfigDto project,
                                 final AsyncCallback<Void> callback) {

        if (projectAttributes.containsKey(CONTRIBUTE_BRANCH_VARIABLE_NAME)) {
            callback.onSuccess(null);
        } else {
            if (project != null) {
                final String branchName = project.getSource().getParameters().get("branch");
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
}
