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

import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.machine.gwt.client.events.ExtServerStateEvent;
import org.eclipse.che.api.machine.gwt.client.events.ExtServerStateHandler;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.project.CreateProjectEvent;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;

import com.codenvy.ide.factory.client.FactoryLocalizationConstant;
import com.codenvy.ide.factory.client.utils.FactoryProjectImporter;
import com.codenvy.ide.factory.client.welcome.GreetingPartPresenter;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import javax.inject.Inject;

/**
 * @author Sergii Leschenko
 */
@Singleton
public class AcceptFactoryHandler {
    private final FactoryLocalizationConstant factoryLocalization;

    private final FactoryProjectImporter   factoryProjectImporter;
    private final EventBus                 eventBus;
    private final AppContext               appContext;
    private final GreetingPartPresenter    greetingPartPresenter;
    private final ProjectExplorerPresenter projectExplorerPresenter;
    private final DtoFactory               dtoFactory;
    private       Notification             notification;

    @Inject
    public AcceptFactoryHandler(FactoryLocalizationConstant factoryLocalization,
                                FactoryProjectImporter factoryProjectImporter,
                                EventBus eventBus,
                                AppContext appContext,
                                DtoFactory dtoFactory,
                                GreetingPartPresenter greetingPartPresenter,
                                ProjectExplorerPresenter projectExplorerPresenter) {
        this.factoryProjectImporter = factoryProjectImporter;
        this.factoryLocalization = factoryLocalization;
        this.eventBus = eventBus;
        this.appContext = appContext;
        this.dtoFactory = dtoFactory;
        this.greetingPartPresenter = greetingPartPresenter;
        this.projectExplorerPresenter = projectExplorerPresenter;
    }

    /**
     * Accepts factory if it is present in context of application
     */
    public void process() {
        final Factory factory;
        if ((factory = appContext.getFactory()) == null) {
            return;
        }

        eventBus.addHandler(ExtServerStateEvent.TYPE, new ExtServerStateHandler() {
            @Override
            public void onExtServerStarted(final ExtServerStateEvent event) {
                notification = new Notification(factoryLocalization.cloningSource(), Notification.Status.PROGRESS);
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        startImporting(factory);
                    }
                });
            }

            @Override
            public void onExtServerStopped(ExtServerStateEvent event) {

            }
        });
    }

    private void startImporting(final Factory factory) {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                greetingPartPresenter.showGreeting();
            }
        });
        factoryProjectImporter.startImporting(notification, factory,
                                              new AsyncCallback<ProjectConfigDto>() {
                                                  @Override
                                                  public void onSuccess(final ProjectConfigDto result) {
                                                      notification.setStatus(Notification.Status.FINISHED);
                                                      notification.setMessage(factoryLocalization.clonedSource());
                                                      ProjectDescriptor descriptor = dtoFactory.createDto(ProjectDescriptor.class);
                                                      descriptor.withName(result.getName())
                                                                .withAttributes(result.getAttributes())
                                                                .withType(result.getType())
                                                                .withDescription(result.getDescription())
                                                                .withContentRoot(result.getPath())
                                                                .withMixins(result.getMixins())
                                                                .withModules(result.getModules());
                                                      eventBus.fireEvent(new CreateProjectEvent(descriptor));
                                                      projectExplorerPresenter.reloadProjectTree();// TODO: tmp fix
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
