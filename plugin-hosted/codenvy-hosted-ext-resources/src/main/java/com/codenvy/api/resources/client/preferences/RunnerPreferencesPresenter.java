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
package com.codenvy.api.resources.client.preferences;

import com.codenvy.api.resources.shared.dto.UpdateResourcesDescriptor;
import com.codenvy.api.resources.client.service.ResourcesServiceClient;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.runner.internal.Constants;
import org.eclipse.che.api.workspace.gwt.client.WorkspaceServiceClient;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDescriptor;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.preferences.AbstractPreferencePagePresenter;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.runner.client.RunnerLocalizationConstant;
import org.eclipse.che.ide.ext.runner.client.tabs.properties.panel.common.Shutdown;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * The presenter for managing user's runners settings,.
 *
 * @author Ann Shumilova
 */
@Singleton
public class RunnerPreferencesPresenter extends AbstractPreferencePagePresenter implements RunnerPreferencesView.ActionDelegate {
    private final AppContext             appContext;
    private final DtoFactory             dtoFactory;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final NotificationManager    notificationManager;
    private final RunnerPreferencesView  view;
    private final ResourcesServiceClient resourcesService;
    private final WorkspaceServiceClient workspaceService;
    private       Shutdown               shutdown;

    /** Create presenter. */
    @Inject
    public RunnerPreferencesPresenter(RunnerPreferencesView view,
                                      RunnerLocalizationConstant locale,
                                      AppContext appContext,
                                      NotificationManager notificationManager,
                                      WorkspaceServiceClient workspaceService,
                                      DtoFactory dtoFactory,
                                      DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                      ResourcesServiceClient resourcesService) {
        super(locale.workspacePreferencesRunnersTitle(), locale.workspacePreferencesTitle(appContext.getWorkspace().getName()), null);
        this.view = view;
        this.appContext = appContext;
        this.dtoFactory = dtoFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.resourcesService = resourcesService;
        this.workspaceService = workspaceService;
        this.view.setDelegate(this);
        this.notificationManager = notificationManager;
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
        view.enableSetButton(false);
        if (appContext.getWorkspace().getAttributes().containsKey(Constants.RUNNER_LIFETIME)) {
            String value = appContext.getWorkspace().getAttributes().get(Constants.RUNNER_LIFETIME);
            shutdown = Shutdown.detect(Integer.parseInt(value));
            if (shutdown != null) {
                view.selectShutdown(shutdown);
            } else {
                shutdown = Shutdown.BY_TIMEOUT_4;
                view.selectShutdown(shutdown);
            }
        } else {
            shutdown = Shutdown.BY_TIMEOUT_4;
            view.selectShutdown(shutdown);
        }
    }

    @Override
    public void storeChanges() {
    }

    @Override
    public void revertChanges() {
        if (appContext.getWorkspace().getAttributes().containsKey(Constants.RUNNER_LIFETIME)) {
            String value = appContext.getWorkspace().getAttributes().get(Constants.RUNNER_LIFETIME);
            shutdown = Shutdown.detect(Integer.parseInt(value));
            if (shutdown != null) {
                view.selectShutdown(shutdown);
            } else {
                shutdown = Shutdown.BY_TIMEOUT_4;
                view.selectShutdown(shutdown);
            }
        }
        view.enableSetButton(false);
    }

    @Override
    public void onValueChanged() {
        view.enableSetButton(!view.getShutdown().equals(shutdown));
    }

    @Override
    public void onSetShutdownClicked() {
        UpdateResourcesDescriptor updateResourcesDescriptor = dtoFactory.createDto(UpdateResourcesDescriptor.class)
                                                                        .withWorkspaceId(appContext.getWorkspace().getId())
                                                                        .withRunnerTimeout(view.getShutdown().getTimeout());

        List<UpdateResourcesDescriptor> updateResourcesDescriptors = new ArrayList<>();
        updateResourcesDescriptors.add(updateResourcesDescriptor);

        resourcesService.redistributeResources(appContext.getWorkspace().getAccountId(), updateResourcesDescriptors,
                                               new AsyncRequestCallback<Void>() {
                                                   @Override
                                                   protected void onSuccess(Void result) {
                                                       shutdown = view.getShutdown();
                                                       view.enableSetButton(false);
                                                       loadWorkspace();
                                                   }

                                                   @Override
                                                   protected void onFailure(Throwable exception) {
                                                       Notification notification =
                                                               new Notification(exception.getMessage(), Notification.Type.ERROR);
                                                       notificationManager.showNotification(notification);
                                                   }
                                               });
    }

    private void loadWorkspace() {
        workspaceService.getWorkspace(appContext.getWorkspace().getId(),
                                      new AsyncRequestCallback<WorkspaceDescriptor>(
                                              dtoUnmarshallerFactory.newUnmarshaller(WorkspaceDescriptor.class)) {
                                          @Override
                                          protected void onSuccess(WorkspaceDescriptor result) {
                                              appContext.getWorkspace().setAttributes(result.getAttributes());
                                          }

                                          @Override
                                          protected void onFailure(Throwable throwable) {
                                          }
                                      }
                                     );
    }
}
