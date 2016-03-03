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
import com.codenvy.plugin.contribution.vcs.client.VcsServiceProvider;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingServiceProvider;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.event.project.CurrentProjectChangedEvent;
import org.eclipse.che.ide.api.event.project.CurrentProjectChangedHandler;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

import static com.codenvy.plugin.contribution.projecttype.shared.ContributionProjectTypeConstants.CONTRIBUTE_BRANCH_VARIABLE_NAME;
import static com.codenvy.plugin.contribution.projecttype.shared.ContributionProjectTypeConstants.CONTRIBUTE_MODE_VARIABLE_NAME;
import static com.codenvy.plugin.contribution.projecttype.shared.ContributionProjectTypeConstants.CONTRIBUTE_VARIABLE_NAME;
import static com.codenvy.plugin.contribution.projecttype.shared.ContributionProjectTypeConstants.CONTRIBUTION_PROJECT_TYPE_ID;
import static java.util.Collections.singletonList;

/**
 * Registers event handlers for adding/removing contribution part.
 *
 * <p>Manages {@code AppContext.getCurrent().}{@link CurrentProject#getRootProject() getRootProject()}
 * current root project state, in the case of adding and removing 'contribution' mixin.
 * Contribution mixin itself is 'synthetic' one and needed only for managing plugin specific project attributes.
 *
 * @author Stephane Tournie
 * @author Kevin Pollet
 * @author Yevhenii Voevodin
 */
@Singleton
@Extension(title = "Contributor", version = "1.0.0")
public class ContributorExtension {
    private final AppContext                appContext;
    private final ContributePartPresenter   contributePartPresenter;
    private final ProjectServiceClient      projectService;
    private final ContributorWorkflow       workflow;
    private final VcsServiceProvider        vcsServiceProvider;
    private final VcsHostingServiceProvider hostingServiceProvider;

    private String  lastSelectedProjectName;
    private boolean partWasOpened;

    @Inject
    public ContributorExtension(final EventBus eventBus,
                                final ContributeResources resources,
                                final AppContext appContext,
                                final ContributePartPresenter contributePartPresenter,
                                final ProjectServiceClient projectService,
                                final ContributorWorkflow workflow,
                                final VcsServiceProvider vcsServiceProvider,
                                final VcsHostingServiceProvider vcsHostingServiceProvider) {
        this.workflow = workflow;
        this.appContext = appContext;
        this.contributePartPresenter = contributePartPresenter;
        this.projectService = projectService;
        this.vcsServiceProvider = vcsServiceProvider;
        this.hostingServiceProvider = vcsHostingServiceProvider;

        eventBus.addHandler(CurrentProjectChangedEvent.TYPE, new CurrentProjectChangedHandler() {
            @Override
            public void onCurrentProjectChanged(CurrentProjectChangedEvent event) {
                final ProjectConfigDto rootProject = appContext.getCurrentProject().getRootProject();
                if (!rootProject.getName().equals(lastSelectedProjectName) || !partWasOpened) {
                    initializeContributorExtension(rootProject);
                }
                lastSelectedProjectName = rootProject.getName();
            }
        });

        resources.contributeCss().ensureInjected();
    }

    private void initializeContributorExtension(final ProjectConfigDto project) {
        hostingServiceProvider.getVcsHostingService()
                              .thenPromise(addMixinIfAbsent(project))
                              .then(initWorkflow())
                              .catchError(cancelWorkflow(project));
    }

    // TODO: find out what is 'contribute' variable name
    private Function<VcsHostingService, Promise<ProjectConfigDto>> addMixinIfAbsent(final ProjectConfigDto project) {
        return new Function<VcsHostingService, Promise<ProjectConfigDto>>() {
            @Override
            public Promise<ProjectConfigDto> apply(VcsHostingService service) throws FunctionException {
                if (project.getMixins().contains(CONTRIBUTION_PROJECT_TYPE_ID)) {
                    return Promises.resolve(project);
                }
                project.getMixins().add(CONTRIBUTION_PROJECT_TYPE_ID);
                project.getAttributes().put(CONTRIBUTE_MODE_VARIABLE_NAME, singletonList("contribute"));

                return getCurrentBranchName(project).then(new Function<String, ProjectConfigDto>() {
                    @Override
                    public ProjectConfigDto apply(String branchName) throws FunctionException {
                        project.getAttributes().put(CONTRIBUTE_BRANCH_VARIABLE_NAME, singletonList(branchName));
                        return project;
                    }
                }).thenPromise(new Function<ProjectConfigDto, Promise<ProjectConfigDto>>() {
                    @Override
                    public Promise<ProjectConfigDto> apply(ProjectConfigDto project) throws FunctionException {
                        return projectService.updateProject(appContext.getWorkspaceId(), project.getPath(), project);
                    }
                });
            }
        };
    }

    private Operation<PromiseError> cancelWorkflow(final ProjectConfigDto project) {
        return new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                Log.info(getClass(), "Cancelling contribution plugin initialization for project " + project.getName());
                contributePartPresenter.remove();
                final List<String> mixins = project.getMixins();
                final Map<String, List<String>> projectAttributes = project.getAttributes();
                if (mixins.contains(CONTRIBUTION_PROJECT_TYPE_ID)) {
                    mixins.remove(CONTRIBUTION_PROJECT_TYPE_ID);
                    projectAttributes.remove(CONTRIBUTE_VARIABLE_NAME);
                    projectAttributes.remove(CONTRIBUTE_MODE_VARIABLE_NAME);
                    projectAttributes.remove(CONTRIBUTE_BRANCH_VARIABLE_NAME);
                    projectService.updateProject(appContext.getWorkspaceId(), project.getPath(), project);
                }
            }
        };
    }

    private Operation<ProjectConfigDto> initWorkflow() {
        Log.info(getClass(), "Initializing contribution plugin for project " + appContext.getCurrentProject().getRootProject().getName());
        return new Operation<ProjectConfigDto>() {
            @Override
            public void apply(ProjectConfigDto project) throws OperationException {
                contributePartPresenter.open();
                partWasOpened = true;
                workflow.init();
                workflow.executeStep();
            }
        };
    }

    private Promise<String> getCurrentBranchName(final ProjectConfigDto project) {
        final String branchName = project.getSource().getParameters().get("branch");
        if (branchName != null) {
            return Promises.resolve(branchName);
        }
        return vcsServiceProvider.getVcsService().getBranchName(project);
    }
}
