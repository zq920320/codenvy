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
import com.codenvy.ide.factory.client.accept.Authenticator;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.project.wizard.ImportProjectNotificationSubscriber;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.websocket.rest.RequestCallback;


/**
 * @author Sergii Leschenko
 * @author Valeriy Svydenko
 */
public class FactoryProjectImporter {

    private final ProjectServiceClient                projectServiceClient;
    private final NotificationManager                 notificationManager;
    private final FactoryLocalizationConstant         localization;
    private final DtoFactory                          dtoFactory;
    private final Authenticator                       authenticator;
    private final ImportProjectNotificationSubscriber notificationSubscriber;

    private Factory                         factory;
    private Notification                    notification;
    private AsyncCallback<ProjectConfigDto> callback;

    @Inject
    public FactoryProjectImporter(ProjectServiceClient projectServiceClient,
                                  NotificationManager notificationManager,
                                  FactoryLocalizationConstant localization,
                                  Authenticator authenticator,
                                  DtoFactory dtoFactory,
                                  ImportProjectNotificationSubscriber notificationSubscriber) {
        this.projectServiceClient = projectServiceClient;
        this.notificationManager = notificationManager;
        this.localization = localization;
        this.dtoFactory = dtoFactory;
        this.authenticator = authenticator;
        this.notificationSubscriber = notificationSubscriber;
    }

    public void startImporting(Notification notification, Factory factory, AsyncCallback<ProjectConfigDto> callback) {
        this.callback = callback;
        this.notification = notification;
        this.factory = factory;
        importProjects();
    }


    /**
     * Imports source to project
     */
    private void importProjects() {
        for (final ProjectConfigDto projectConfig : factory.getWorkspace().getProjects()) {
            notificationManager.showNotification(notification);
            notification.setMessage(localization.cloningSource());
            notificationSubscriber.subscribe(projectConfig.getName(), notification);
            projectServiceClient.importProject(projectConfig.getName(), true, projectConfig.getSource(),
                                               new RequestCallback<Void>() {
                                                   @Override
                                                   protected void onSuccess(Void result) {
                                                       callback.onSuccess(projectConfig);
                                                   }

                                                   @Override
                                                   protected void onFailure(Throwable exception) {
//                                                       if (exception instanceof UnauthorizedException) {
//                                                           rerunWithAuthImport(projectConfig.getSource().getLocation());
//                                                       } else {
                                                           callback.onFailure(
                                                                   new Exception("Unable to import source of project. " + dtoFactory
                                                                           .createDtoFromJson(exception.getMessage(), ServiceError.class)
                                                                           .getMessage()));
//                                                       }
                                                   }
                                               });
        }
    }

    private void rerunWithAuthImport(String location) {
        notification.setMessage(localization.needToAuthorizeBeforeAcceptMessage());
        authenticator.showOAuthWindow(location,
                                      new Authenticator.AuthCallback() {
                                          @Override
                                          public void onAuthenticated() {
                                              notification.setMessage(localization.oauthSuccess());
                                              importProjects();
                                          }

                                          @Override
                                          public void onError(String message) {
                                              notification.setMessage(localization.oauthFailed() + " " + message);
                                              notification.setType(Notification.Type.ERROR);
                                              notification.setStatus(Notification.Status.FINISHED);
                                          }
                                      });
    }

    public Factory getFactory() {
        return factory;
    }
}
