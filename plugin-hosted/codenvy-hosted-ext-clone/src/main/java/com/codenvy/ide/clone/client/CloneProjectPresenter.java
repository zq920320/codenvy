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
package com.codenvy.ide.clone.client;

import com.codenvy.ide.factory.client.utils.SyncGitServiceClient;
import com.codenvy.ide.share.client.share.CommitPresenter;
import com.codenvy.ide.permissions.client.part.PermissionsPartPresenter;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.factory.dto.Factory;
import org.eclipse.che.api.factory.dto.Workspace;
import org.eclipse.che.api.factory.gwt.client.FactoryServiceClient;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.git.client.GitRepositoryInitializer;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.Config;
import org.eclipse.che.ide.util.loging.Log;
import org.vectomatic.dom.svg.ui.SVGImage;

import java.util.Random;

import static com.codenvy.ide.share.client.share.CommitPresenter.CommitActionHandler;
import static com.google.gwt.http.client.RequestBuilder.POST;
import static org.eclipse.che.ide.api.notification.Notification.Type.ERROR;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

/**
 * This presenter provides the base functionality for cloning of projects and
 * adds a Clone button onto toolbar when persist workspace is opened.
 *
 * @author Vitaliy Guliy
 * @author Sergii Leschenko
 * @author Kevin Pollet
 */
@Singleton
public class CloneProjectPresenter implements CommitActionHandler {

    private final CloneResources            resources;
    private final CloneLocalizationConstant localizationConstant;
    private final ActionManager             actionManager;
    private final NotificationManager       notificationManager;
    private final AppContext                appContext;
    private final FactoryServiceClient      factoryServiceClient;
    private final DtoFactory                dtoFactory;
    private final DtoUnmarshallerFactory    dtoUnmarshallerFactory;
    private final SyncGitServiceClient      syncGitServiceClient;
    private final FactoryBuilder            factoryBuilder;
    private final CommitPresenter           commitPresenter;
    private final PermissionsPartPresenter  permissionsPresenter;

    @Inject
    public CloneProjectPresenter(CloneLocalizationConstant localizationConstant,
                                 ActionManager actionManager,
                                 NotificationManager notificationManager,
                                 DtoFactory dtoFactory,
                                 DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                 PermissionsPartPresenter permissionsPresenter,
                                 AppContext appContext,
                                 FactoryServiceClient factoryServiceClient,
                                 CloneResources resources,
                                 SyncGitServiceClient syncGitServiceClient,
                                 CommitPresenter commitPresenter) {
        this.localizationConstant = localizationConstant;
        this.actionManager = actionManager;
        this.notificationManager = notificationManager;
        this.appContext = appContext;
        this.factoryServiceClient = factoryServiceClient;
        this.dtoFactory = dtoFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.syncGitServiceClient = syncGitServiceClient;
        this.commitPresenter = commitPresenter;
        this.resources = resources;
        this.permissionsPresenter = permissionsPresenter;
        this.factoryBuilder = new FactoryBuilder();

        this.commitPresenter.setCommitActionHandler(this);
    }

    /**
     * Registers an action for cloning the project and adds Clone button onto toolbar.
     */
    public void process() {
        if (Config.getCurrentWorkspace() == null) {
            return;
        }

        CloneProjectAction cloneProjectAction = new CloneProjectAction();
        actionManager.registerAction("cloneProject", cloneProjectAction);
        DefaultActionGroup mainToolbarGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_RIGHT_MAIN_MENU);
        mainToolbarGroup.add(cloneProjectAction);
    }

    @Override
    public void onCommitAction(CommitAction action) {
        openFactoryURL();
    }

    private class CloneButton extends Button {

        private final Element tooltip;

        public CloneButton() {
            super();

            setText(localizationConstant.cloneToolbarButtonText());

            ensureDebugId("clone-project-toolbar-button");

            final SVGImage image = new SVGImage(resources.cloneIcon());
            image.setWidth("18px");
            image.setHeight("16px");
            image.getStyle().setProperty("fill", "#e7e7e7");
            image.getStyle().setProperty("verticalAlign", "middle");
            image.getStyle().setProperty("marginRight", "5px");
            image.getStyle().setProperty("paddingBottom", "2px");

            getElement().insertFirst(image.getElement());

            addStyleName(resources.cloneCSS().mainMenuBarButton());
            addStyleName(resources.cloneCSS().buttonTooltip());
            addStyleName(resources.cloneCSS().tooltip());

            getElement().getStyle().setCursor(Style.Cursor.POINTER);

            tooltip = DOM.createSpan();
            tooltip.setInnerHTML(localizationConstant.cloneToolbarButtonTitle());

            addMouseOverHandler(new MouseOverHandler() {
                @Override
                public void onMouseOver(MouseOverEvent event) {
                    final Element link = event.getRelativeElement();
                    if (!link.isOrHasChild(tooltip)) {
                        tooltip.getStyle().setProperty("top", (link.getOffsetHeight() + 10) + "px");
                        tooltip.getStyle().setProperty("right", ((Document.get().getClientWidth() - link.getAbsoluteRight()) - 30) + "px");
                        link.appendChild(tooltip);
                    }
                }
            });
        }
    }

    /**
     * Action to provide Clone button onto toolbar.
     */
    public class CloneProjectAction extends Action implements CustomComponentAction {
        @Inject
        public CloneProjectAction() {
            super(localizationConstant.cloneProjectMenuTitle(),
                  localizationConstant.cloneToolbarButtonText(),
                  null,
                  resources.cloneIcon());
        }

        @Override
        public Widget createCustomComponent(Presentation presentation) {
            final CloneButton button = new CloneButton();
            button.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    cloneProject();
                }
            });

            return button;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            cloneProject();
        }

        @Override
        public void update(ActionEvent e) {
            e.getPresentation().setVisible(appContext.getCurrentProject() != null &&
                                           !appContext.getCurrentProject().isReadOnly() &&
                                           Config.getCurrentWorkspace() != null);
        }
    }

    /**
     * Open the factory URL.
     */
    private void openFactoryURL() {
        factoryBuilder.build(new AsyncCallback<Factory>() {
            @Override
            public void onSuccess(Factory result) {
                final String url = new UrlBuilder().setProtocol(Window.Location.getProtocol())
                                                   .setHost(Window.Location.getHost())
                                                   .setPath("factory")
                                                   .buildString();

                final String factoryURL = url + "?id=" + result.getId();
                Window.open(factoryURL, "_blank", "");
            }

            @Override
            public void onFailure(Throwable throwable) {
                processError(throwable.getMessage());
            }
        });
    }

    private void cloneProject() {
        if (appContext.getCurrentProject() == null || appContext.getCurrentProject().getRootProject() == null) {
            Log.error(getClass(), "Open the project before use clone");
            return;
        }

        if (Config.getCurrentWorkspace().isTemporary()) {
            permissionsPresenter.showDialog();
        } else if (commitPresenter.hasUncommittedChanges()) {
            commitPresenter.showView();
        } else {
            openFactoryURL();
        }
    }

    /**
     * Show error message
     *
     * @param message
     *         error message
     */
    private void processError(String message) {
        Notification notification = new Notification(message, ERROR);
        notificationManager.showNotification(notification);
    }

    private class FactoryBuilder {

        public void build(final AsyncCallback<Factory> callback) {
            if (!GitRepositoryInitializer.isGitRepository(appContext.getCurrentProject().getRootProject())) {
                syncGitServiceClient.init(appContext.getCurrentProject().getRootProject());
            }

            getFactoryJson(new AsyncCallback<Factory>() {
                @Override
                public void onSuccess(final Factory factory) {
                    factory.setWorkspace(dtoFactory.createDto(Workspace.class).withType("temp").withLocation("owner"));
                    saveFactory(factory, callback);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    processError(throwable.getMessage());
                }
            });

        }

        private void getFactoryJson(final AsyncCallback<Factory> callback) {
            factoryServiceClient.getFactoryJson(appContext.getWorkspace().getId(),
                                                appContext.getCurrentProject().getProjectDescription().getPath(),
                                                new AsyncRequestCallback<Factory>(dtoUnmarshallerFactory.newUnmarshaller(Factory.class)) {
                                                    @Override
                                                    public void onSuccess(Factory factory) {
                                                        callback.onSuccess(factory);
                                                    }

                                                    @Override
                                                    public void onFailure(Throwable exception) {
                                                        processError(exception.getMessage());
                                                    }
                                                });
        }


        private void saveFactory(Factory factory, final AsyncCallback<Factory> callback) {
            final String boundary = Long.toString(new Random().nextLong(), 36);
            final String data = "--" + boundary + "\r\n"
                                + "Content-Disposition: form-data; name=\"factoryUrl\"\r\n\r\n"
                                + dtoFactory.toJson(factory)
                                + "\r\n--" + boundary + "--";
            final RequestBuilder request = new RequestBuilder(POST, "/api/factory");
            request.setHeader(CONTENT_TYPE, "multipart/form-data; boundary=" + boundary);
            try {
                request.sendRequest(data, new RequestCallback() {
                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        callback.onSuccess(dtoFactory.createDtoFromJson(response.getText(), Factory.class));
                    }

                    @Override
                    public void onError(Request request, Throwable exception) {
                        processError(exception.getMessage());
                    }
                });

            } catch (RequestException e) {
                processError(e.getMessage());
            }
        }
    }
}
