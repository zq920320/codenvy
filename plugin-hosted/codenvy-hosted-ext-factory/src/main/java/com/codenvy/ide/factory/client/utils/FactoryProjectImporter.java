/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.project.CreateProjectEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.project.wizard.ImportProjectNotificationSubscriber;
import org.eclipse.che.ide.api.project.wizard.ImportProjectNotificationSubscriberFactory;
import org.eclipse.che.ide.util.loging.Log;

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
public class FactoryProjectImporter {
    private final ProjectServiceClient                       projectService;
    private final EventBus                                   eventBus;
    private final FactoryLocalizationConstant                locale;
    private final NotificationManager                        notificationManager;
    private final ImportProjectNotificationSubscriberFactory subscriberFactory;
    private final String                                     workspaceId;

    private Factory             factory;
    private AsyncCallback<Void> callback;

    @Inject
    public FactoryProjectImporter(ProjectServiceClient projectServiceClient,
                                  AppContext appContext,
                                  NotificationManager notificationManager,
                                  FactoryLocalizationConstant locale,
                                  ImportProjectNotificationSubscriberFactory subscriberFactory,
                                  EventBus eventBus) {
        this.projectService = projectServiceClient;
        this.notificationManager = notificationManager;
        this.locale = locale;
        this.subscriberFactory = subscriberFactory;
        this.eventBus = eventBus;
        this.workspaceId = appContext.getWorkspace().getId();
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
                for (ProjectConfigDto projectConfigDto : projectConfigs) {
                    projectNames.add(projectConfigDto.getName());
                }
                importProjects(projectNames);
            }
        });
    }

    /**
     * Import source projects and if it's already exist in workspace
     * then show warning notification
     *
     * @param existedProjects
     *         set of project names that already exist in workspace
     */
    private void importProjects(Set<String> existedProjects) {
        final List<Promise<Void>> promises = new ArrayList<>();
        for (final ProjectConfigDto projectConfig : factory.getWorkspace().getProjects()) {
            if (existedProjects.contains(projectConfig.getName())) {
                notificationManager.notify("Import", locale.projectAlreadyImported(projectConfig.getName()), FAIL, true);
                continue;
            }
            promises.add(importProject(projectConfig));
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

    /** Importing a single project in case of failure will display the notification with appropriate status */
    private Promise<Void> importProject(final ProjectConfigDto projectConfig) {
        final String projectName = projectConfig.getName();
        final StatusNotification notification = notificationManager.notify(locale.cloningSource(projectName), null, PROGRESS, true);
        final ImportProjectNotificationSubscriber subscriber = subscriberFactory.createSubscriber();
        subscriber.subscribe(projectName, notification);

        return projectService.importProject(workspaceId, projectName, true, projectConfig.getSource())
                             .then(new Operation<Void>() {
                                 @Override
                                 public void apply(Void arg) throws OperationException {
                                     projectService.getProject(workspaceId, projectConfig.getPath())
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
