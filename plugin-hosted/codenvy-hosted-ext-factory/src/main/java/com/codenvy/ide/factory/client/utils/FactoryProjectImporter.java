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
    private final ProjectServiceClient                       projectServiceClient;
    private final NotificationManager                        notificationManager;
    private final FactoryLocalizationConstant                localization;
    private final ImportProjectNotificationSubscriberFactory subscriberFactory;
    private final EventBus                                   eventBus;
    private final String                                     workspaceId;

    private Factory             factory;
    private AsyncCallback<Void> callback;

    @Inject
    public FactoryProjectImporter(ProjectServiceClient projectServiceClient,
                                  AppContext appContext,
                                  NotificationManager notificationManager,
                                  FactoryLocalizationConstant localization,
                                  ImportProjectNotificationSubscriberFactory subscriberFactory,
                                  EventBus eventBus) {
        this.projectServiceClient = projectServiceClient;
        this.notificationManager = notificationManager;
        this.localization = localization;
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
        projectServiceClient.getProjects(workspaceId, false).then(new Operation<List<ProjectConfigDto>>() {
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
            final String projectName = projectConfig.getName();
            if (existedProjects.contains(projectName)) {
                notificationManager.notify("Import", localization.projectAlreadyImported(projectName), FAIL, true);
                continue;
            }
            final StatusNotification notification =
                    notificationManager.notify(localization.cloningSource(projectName), null, PROGRESS, true);
            final ImportProjectNotificationSubscriber notificationSubscriber = subscriberFactory.createSubscriber();
            notificationSubscriber.subscribe(projectName, notification);

            Promise<Void> promise = projectServiceClient.importProject(workspaceId, projectName, true, projectConfig.getSource())
                                                        .then(new Operation<Void>() {
                                                            @Override
                                                            public void apply(Void arg) throws OperationException {
                                                                notificationSubscriber.onSuccess();
                                                                notification.setContent(localization.clonedSource(projectName));
                                                                notification.setStatus(SUCCESS);
                                                                eventBus.fireEvent(new CreateProjectEvent(projectConfig));
                                                            }
                                                        })
                                                        .catchError(new Operation<PromiseError>() {
                                                            @Override
                                                            public void apply(PromiseError arg) throws OperationException {
                                                                notificationSubscriber.onFailure(arg.getMessage());
                                                                notification.setContent(localization.cloningSourceFailed(projectName));
                                                                notification.setStatus(FAIL);
                                                                Promises.reject(arg);
                                                            }
                                                        });
            promises.add(promise);
        }

        Promises.all(promises.toArray(new Promise[promises.size()]))
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

    public Factory getFactory() {
        return factory;
    }
}
