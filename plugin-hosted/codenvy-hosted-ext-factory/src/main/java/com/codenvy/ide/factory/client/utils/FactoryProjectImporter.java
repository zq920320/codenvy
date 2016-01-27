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
package com.codenvy.ide.factory.client.utils;

import com.codenvy.ide.factory.client.FactoryLocalizationConstant;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.project.CreateProjectEvent;
import org.eclipse.che.ide.api.importer.AbstractImporter;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.project.wizard.ImportProjectNotificationSubscriberFactory;
import org.eclipse.che.ide.api.project.wizard.ProjectNotificationSubscriber;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * @author Sergii Leschenko
 * @author Valeriy Svydenko
 * @author Anton Korneta
 */
public class FactoryProjectImporter extends AbstractImporter {

    private final EventBus                    eventBus;
    private final FactoryLocalizationConstant locale;
    private final NotificationManager         notificationManager;

    private Factory             factory;
    private AsyncCallback<Void> callback;

    @Inject
    public FactoryProjectImporter(ProjectServiceClient projectService,
                                  AppContext appContext,
                                  NotificationManager notificationManager,
                                  FactoryLocalizationConstant locale,
                                  ImportProjectNotificationSubscriberFactory subscriberFactory,
                                  EventBus eventBus) {
        super(appContext, projectService, subscriberFactory);

        this.notificationManager = notificationManager;
        this.locale = locale;
        this.eventBus = eventBus;
    }

    public void startImporting(Factory factory, AsyncCallback<Void> callback) {
        this.callback = callback;
        this.factory = factory;
        importProjects();
    }

    /**
     * Import source projects
     */
    private void importProjects() {
        projectService.getProjects(workspaceId, false).then(new Operation<List<ProjectConfigDto>>() {
            @Override
            public void apply(List<ProjectConfigDto> projectConfigs) throws OperationException {
                Set<String> projectNames = new HashSet<>();
                String createPolicy = factory.getPolicies() != null ? factory.getPolicies().getCreate() : null;
                for (ProjectConfigDto projectConfig : projectConfigs) {
                    if (isProjectExistOnFileSystem(projectConfig)) {
                        // to prevent warning when reusing same workspace
                        if (!("perUser".equals(createPolicy) || "perAccount".equals(createPolicy))) {
                            notificationManager.notify("Import", locale.projectAlreadyImported(projectConfig.getName()), FAIL, true);
                        }
                        continue;
                    }

                    projectNames.add(projectConfig.getName());
                }
                importProjects(projectNames);
            }
        });
    }

    /**
     * Import source projects and if it's already exist in workspace
     * then show warning notification
     *
     * @param projectsToImport
     *         set of project names that already exist in workspace and will be imported on file system
     */
    private void importProjects(Set<String> projectsToImport) {
        final List<Promise<Void>> promises = new ArrayList<>();
        for (final ProjectConfigDto projectConfig : factory.getWorkspace().getProjects()) {
            if (projectsToImport.contains(projectConfig.getName())) {
                promises.add(startImport(projectConfig.getPath(), projectConfig.getName(), projectConfig.getSource()));
            }
        }

        Promises.all(promises.toArray(new Promise<?>[promises.size()]))
                .then(new Operation<JsArrayMixed>() {
                    @Override
                    public void apply(JsArrayMixed arg) throws OperationException {
                        callback.onSuccess(null);
                    }
                })
                .catchError(new Operation<PromiseError>() {
                    @Override
                    public void apply(PromiseError promiseError) throws OperationException {
                        // If it is unable to import any number of projects then factory import status will be success anyway
                        callback.onSuccess(null);
                    }
                });
    }

    private boolean isProjectExistOnFileSystem(ProjectConfigDto projectConfigDto) {
        List<ProjectProblemDto> problems = projectConfigDto.getProblems();

        if (problems == null || problems.isEmpty()) {
            return true;
        }

        for (ProjectProblemDto problem : problems) {
            if (problem.getCode() == 9) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected Promise<Void> importProject(@NotNull final String pathToProject,
                                          @NotNull final String projectName,
                                          @NotNull final SourceStorageDto sourceStorage) {
        final StatusNotification notification = notificationManager.notify(locale.cloningSource(projectName), null, PROGRESS, true);
        final ProjectNotificationSubscriber subscriber = subscriberFactory.createSubscriber();
        subscriber.subscribe(projectName, notification);

        return projectService.importProject(workspaceId, projectName, true, sourceStorage)
                             .then(new Operation<Void>() {
                                 @Override
                                 public void apply(Void arg) throws OperationException {
                                     projectService.getProject(workspaceId, pathToProject)
                                                   .then(new Operation<ProjectConfigDto>() {
                                                       @Override
                                                       public void apply(ProjectConfigDto projectConfig) throws OperationException {
                                                           eventBus.fireEvent(new CreateProjectEvent(projectConfig));
                                                           subscriber.onSuccess();
                                                           notification.setContent(locale.clonedSource(projectName));
                                                           notification.setStatus(SUCCESS);
                                                       }
                                                   })
                                                   .catchError(new Operation<PromiseError>() {
                                                       @Override
                                                       public void apply(PromiseError err) throws OperationException {
                                                           subscriber.onFailure(err.getMessage());
                                                           notification.setContent(locale.configuringSourceFailed(projectName));
                                                           notification.setStatus(FAIL);
                                                           Promises.reject(err);
                                                       }
                                                   });
                                 }
                             })
                             .catchError(new Operation<PromiseError>() {
                                 @Override
                                 public void apply(PromiseError err) throws OperationException {
                                     subscriber.onFailure(err.getMessage());
                                     notification.setContent(locale.cloningSourceFailed(projectName));
                                     notification.setStatus(FAIL);
                                     Promises.reject(err);
                                 }
                             });
    }
}
