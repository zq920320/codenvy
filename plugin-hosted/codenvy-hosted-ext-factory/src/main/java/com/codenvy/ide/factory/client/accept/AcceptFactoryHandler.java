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
package com.codenvy.ide.factory.client.accept;

import org.eclipse.che.api.factory.dto.Factory;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.api.workspace.gwt.client.WorkspaceServiceClient;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDescriptor;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.ConfigureProjectEvent;
import org.eclipse.che.ide.api.event.OpenProjectEvent;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationManager;
import com.codenvy.ide.factory.client.FactoryLocalizationConstant;
import com.codenvy.ide.factory.client.utils.FactoryProjectImporter;
import com.codenvy.ide.factory.client.welcome.GreetingPartPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.util.Config;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Sergii Leschenko
 */
@Singleton
public class AcceptFactoryHandler {
    private final FactoryLocalizationConstant factoryLocalization;
    private final NotificationManager         notificationManager;
    private final FactoryProjectImporter      factoryProjectImporter;
    private final WorkspaceServiceClient      workspaceServiceClient;
    private final EventBus                    eventBus;
    private final AppContext                  appContext;
    private final GreetingPartPresenter       greetingPartPresenter;

    private Notification notification;

    @Inject
    public AcceptFactoryHandler(FactoryLocalizationConstant factoryLocalization,
                                FactoryProjectImporter factoryProjectImporter,
                                NotificationManager notificationManager,
                                WorkspaceServiceClient workspaceServiceClient,
                                EventBus eventBus,
                                AppContext appContext,
                                GreetingPartPresenter greetingPartPresenter) {
        this.factoryProjectImporter = factoryProjectImporter;
        this.notificationManager = notificationManager;
        this.factoryLocalization = factoryLocalization;
        this.workspaceServiceClient = workspaceServiceClient;
        this.eventBus = eventBus;
        this.appContext = appContext;
        this.greetingPartPresenter = greetingPartPresenter;
    }

    /**
     * Accepts factory if it is present in context of application
     */
    public void process() {
        final Factory factory;

        if ((factory = appContext.getFactory()) == null) {
            return;
        }

        notification = new Notification(factoryLocalization.cloningSource(), Notification.Status.PROGRESS);
        notificationManager.showNotification(notification);

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                prepareWorkspace(factory);
            }
        });
    }

    private void prepareWorkspace(final Factory factory) {
        if (Config.getCurrentWorkspace().isTemporary() && factory.getProject() != null
            && "public".equals(factory.getProject().getVisibility())) {

            Map<String, String> attributes = new HashMap<>();
            attributes.put("allowAnyoneAddMember", "true");

            workspaceServiceClient.updateAttributes(attributes, new AsyncRequestCallback<WorkspaceDescriptor>() {
                @Override
                protected void onSuccess(WorkspaceDescriptor result) {
                    startImporting(factory);
                }

                @Override
                protected void onFailure(Throwable exception) {
                    notification.setMessage(exception.getMessage());
                    notification.setType(Notification.Type.ERROR);
                }
            });
        } else {
            startImporting(factory);
        }
    }

    private void startImporting(final Factory factory) {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                greetingPartPresenter.showGreeting();
            }
        });

        factoryProjectImporter.startImporting(notification, factory,
                                              new AsyncCallback<ProjectDescriptor>() {
                                                  @Override
                                                  public void onSuccess(final ProjectDescriptor projectDescriptor) {
                                                      notification.setStatus(Notification.Status.FINISHED);
                                                      notification.setMessage(factoryLocalization.clonedSource());

                                                      eventBus.fireEvent(new OpenProjectEvent(projectDescriptor.getName()));

                                                      if (!projectDescriptor.getProblems().isEmpty()) {
                                                          eventBus.fireEvent(new ConfigureProjectEvent(projectDescriptor));
                                                      }
                                                  }

                                                  @Override
                                                  public void onFailure(Throwable throwable) {
                                                      notification.setType(Notification.Type.ERROR);
                                                      notification.setImportant(true);
                                                      notification.setMessage(throwable.getMessage());
                                                  }
                                              });
    }
}
