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
package com.codenvy.ide.clone.client.persist;

import com.codenvy.ide.clone.client.CloneLocalizationConstant;
import com.codenvy.ide.clone.client.CloneResources;
import com.codenvy.ide.permissions.client.part.PermissionsPartPresenter;
import com.codenvy.ide.permissions.client.part.PermissionsPartView;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.workspace.gwt.client.WorkspaceServiceClient;
import org.eclipse.che.api.workspace.shared.dto.MemberDescriptor;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDescriptor;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceReference;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.ProjectActionEvent;
import org.eclipse.che.ide.api.event.ProjectActionHandler;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.Config;
import org.eclipse.che.ide.util.loging.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides basic client-side of 'Copy to Named Workspace' functionality.
 *
 * @author Vitaliy Guliy
 */
@Singleton
public class PersistProjectPresenter {

    private final CloneLocalizationConstant localizationConstant;
    private final ActionManager             actionManager;
    private final DtoUnmarshallerFactory    dtoUnmarshallerFactory;
    private final NotificationManager       notificationManager;
    private final WorkspaceServiceClient    workspaceServiceClient;

    private final PersistProjectAction persistProjectAction;

    private final AppContext appContext;

    private final PersistProjectView view;
    private final CloneResources     resources;

    private List<String> persistWorkspaces;

    private final PermissionsPartPresenter permissionsPartPresenter;
    private final PermissionsPartView      permissionsPartView;

    @Inject
    public PersistProjectPresenter(DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                   NotificationManager notificationManager,
                                   CloneLocalizationConstant localizationConstant,
                                   ActionManager actionManager,
                                   PersistProjectView view,
                                   PermissionsPartView permissionsView,
                                   WorkspaceServiceClient workspaceServiceClient,
                                   CloneResources resources,
                                   AppContext appContext,
                                   PermissionsPartPresenter permissionsPresenter,
                                   PersistProjectAction persistProjectAction,
                                   EventBus eventBus) {
        this.localizationConstant = localizationConstant;
        this.actionManager = actionManager;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.notificationManager = notificationManager;
        this.workspaceServiceClient = workspaceServiceClient;
        this.resources = resources;
        this.appContext = appContext;
        this.permissionsPartPresenter = permissionsPresenter;
        this.permissionsPartView = permissionsView;
        this.view = view;

        this.persistProjectAction = persistProjectAction;


        view.setDelegate(new PersistProjectView.ActionDelegate() {
            @Override
            public void onLogin() {
                login();
            }

            @Override
            public void onCreateFreeAccount() {
                createAccountAndCopy();
            }
        });

        eventBus.addHandler(ProjectActionEvent.TYPE, new ProjectActionHandler() {
            @Override
            public void onProjectOpened(ProjectActionEvent event) {
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        updatePermissionsView();
                    }
                });
            }

            @Override
            public void onProjectClosing(ProjectActionEvent event) {
            }

            @Override
            public void onProjectClosed(ProjectActionEvent event) {
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        permissionsPartView.displayWidget(null);
                    }
                });
            }
        });

    }

    private void updatePermissionsView() {
        FlowPanel permissionsPersistPanel = new FlowPanel();

        final HTML text = new HTML();
        text.setStyleName(resources.cloneCSS().permissionsText());
        permissionsPersistPanel.add(text);

        FlowPanel buttonPanel = new FlowPanel();
        buttonPanel.getElement().getStyle().setTextAlign(Style.TextAlign.CENTER);
        buttonPanel.getElement().getStyle().setMarginTop(2, Style.Unit.PX);
        permissionsPersistPanel.add(buttonPanel);

        Widget persistButton = createPersistButton();
        buttonPanel.add(persistButton);

        if (Config.getCurrentWorkspace().isTemporary()) {
            persistButton.getElement().setInnerText(localizationConstant.persistToolbarButtonText() + " ");
            persistButton.setTitle(localizationConstant.persistToolbarButtonTitle());

            permissionsPartView.displayWidget(permissionsPersistPanel);

            workspaceServiceClient.getMembership(Config.getWorkspaceId(), new AsyncRequestCallback<MemberDescriptor>(
                    dtoUnmarshallerFactory.newUnmarshaller(MemberDescriptor.class)) {
                @Override
                protected void onSuccess(MemberDescriptor result) {
                    if (result.getRoles().contains("workspace/admin")) {
                        text.getElement().setInnerHTML(localizationConstant.permissionsViewPersistTextForOwner());
                    } else {
                        text.getElement().setInnerHTML(localizationConstant.permissionsViewPersistText());
                    }
                }

                @Override
                protected void onFailure(Throwable exception) {
                    text.getElement().setInnerHTML(localizationConstant.permissionsViewPersistText());
                }
            });
        } else if ("read".equalsIgnoreCase(permissionsPartPresenter.getPermissions())) {
            persistButton.getElement().setInnerText(localizationConstant.copyToolbarButtonText() + " ");
            persistButton.setTitle(localizationConstant.copyToolbarButtonTitle());
            permissionsPartView.displayWidget(permissionsPersistPanel);

            text.getElement().setInnerHTML(localizationConstant.permissionsViewCopyText());
        } else {
            permissionsPartView.displayWidget(null);
        }
    }


    /**
     * Verifies current workspace and adds the 'Persist' button if project can be persisted.
     */
    public void process() {
        WorkspaceDescriptor workspace = Config.getCurrentWorkspace();
        if (workspace == null) {
            return;
        }

        if (workspace.getAttributes() != null && "true".equals(workspace.getAttributes().get("hidecopybutton"))) {
            return;
        }

        actionManager.registerAction("persistProjectAction", persistProjectAction);
        DefaultActionGroup mainToolbarGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_RIGHT_MAIN_MENU);
        mainToolbarGroup.add(persistProjectAction);
    }

    /**
     * Creates a dropdown button and binds his actions with this presenter.
     * Embed this button in your widget to persist the project directly.
     *
     * @return new instance of Persist button
     */
    private Widget createPersistButton() {
        final Button button = new Button();

        if (!Config.getCurrentWorkspace().isTemporary() && permissionsPartPresenter.isReadOnlyMode()) {
            button.setText(localizationConstant.copyToolbarButtonText() + " ");
            button.setTitle(localizationConstant.copyToolbarButtonTitle());
        } else {
            button.setText(localizationConstant.persistToolbarButtonText() + " ");
            button.setTitle(localizationConstant.persistToolbarButtonTitle());
        }

        button.getElement().getStyle().setProperty("background", "#266c9e");

        button.getElement().getStyle().setProperty("height", "25px");
        button.getElement().getStyle().setProperty("lineHeight", "24px");

        button.getElement().getStyle().setProperty("paddingLeft", "17px");
        button.getElement().getStyle().setProperty("paddingRight", "17px");

        button.getElement().getStyle().setProperty("textAlign", "center");
        button.getElement().getStyle().setProperty("whiteSpace", "nowrap");
        button.getElement().getStyle().setProperty("verticalAlign", "middle");


        if (appContext.getCurrentUser().isUserPermanent()) {
            /**
             * Add arrow
             */
            final Element span = DOM.createSpan();
            span.getStyle().setProperty("display", "inline-block");
            span.getStyle().setProperty("verticalAlign", "middle");
            span.getStyle().setProperty("borderTop", "4px solid");
            span.getStyle().setProperty("borderRight", "4px solid transparent");
            span.getStyle().setProperty("borderLeft", "4px solid transparent");
            span.getStyle().setProperty("marginLeft", "2px");

            if (persistWorkspaces != null) {
                if (persistWorkspaces.size() > 1) {
                    button.getElement().appendChild(span);
                }
            } else {
                try {
                    workspaceServiceClient.getMemberships(new AsyncRequestCallback<List<MemberDescriptor>>(
                            dtoUnmarshallerFactory.newListUnmarshaller(MemberDescriptor.class)) {
                        @Override
                        protected void onSuccess(List<MemberDescriptor> result) {
                            persistWorkspaces = new ArrayList<>();

                            // add persist
                            for (MemberDescriptor aResult : result) {
                                if (!aResult.getWorkspaceReference().isTemporary()) {
                                    persistWorkspaces.add(aResult.getWorkspaceReference().getName());
                                }
                            }

                            if (persistWorkspaces.size() > 1) {
                                button.getElement().appendChild(span);
                            }
                        }

                        @Override
                        protected void onFailure(Throwable throwable) {
                            notificationManager
                                    .showNotification(new Notification("Unable to get list of workspaces. " + throwable.getMessage(),
                                                                       Notification.Type.ERROR));
                            Log.error(PersistProjectHandler.class,
                                      "Unable to get list of workspaces. " + throwable.getMessage());
                        }
                    });
                } catch (Exception exception) {
                    notificationManager.showNotification(
                            new Notification("Unable to get list of workspaces. " + exception.getMessage(), Notification.Type.ERROR));
                    Log.error(PersistProjectHandler.class,
                              "Unable to get list of workspaces. " + exception.getMessage());
                }
            }

            button.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    selectWorkspace(button.getElement().getAbsoluteLeft(),
                                    button.getElement().getAbsoluteTop() + button.getElement().getOffsetHeight());
                }
            });
        } else {
            button.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (Config.getCurrentWorkspace().isTemporary()) {
                        view.setText(localizationConstant.copyToNamedWorkspaceText());
                    } else {
                        view.setText(localizationConstant.copyToNamedWorkspaceReadonlyText());
                    }

                    view.showDialog();
                }
            });
        }

        return button;
    }

    /**
     * Redirects to login page.
     */
    private native void login() /*-{
        console.log($wnd.location.search);
        $wnd.location.search = "?login&" +
            this.@com.codenvy.ide.clone.client.persist.PersistProjectPresenter::getQueryParamsForCopyProjectAction()();
    }-*/;

    /**
     * Redirects to creating account page.
     */
    private void createAccountAndCopy() {
        Window.Location.replace("/site/create-account?" + getQueryParamsForCopyProjectAction());
    }

    /**
     * Opens drop-down menu and persists the current project into selected workspace.
     */
    private void selectWorkspace(final int left, final int top) {
        try {
            workspaceServiceClient.getMemberships(new AsyncRequestCallback<List<MemberDescriptor>>(
                    dtoUnmarshallerFactory.newListUnmarshaller(MemberDescriptor.class)) {
                @Override
                protected void onSuccess(List<MemberDescriptor> result) {
                    List<WorkspaceReference> persistWorkspaces = new ArrayList<>();

                    for (MemberDescriptor memberDescriptor : result) {
                        if (!memberDescriptor.getWorkspaceReference().isTemporary()) {
                            persistWorkspaces.add(memberDescriptor.getWorkspaceReference());
                        }
                    }

                    if (persistWorkspaces.size() > 1) {
                        List<String> items = new ArrayList<>();
                        for (WorkspaceReference persistWorkspace : persistWorkspaces) {
                            String href = Config.getContext() + "/" + persistWorkspace.getName() +
                                          "?" + getQueryParamsForCopyProjectAction(persistWorkspace.getName());
                            items.add("<a href='" + href + "' >" + persistWorkspace.getName() + "</a>");
                        }

                        new DropdownMenu(items, left, top);
                        return;
                    }

                    if (persistWorkspaces.size() == 1) {
                        Window.Location.replace(Config.getContext() + "/" + persistWorkspaces.get(0).getName() +
                                                "?" + getQueryParamsForCopyProjectAction(persistWorkspaces.get(0).getName()));
                        return;
                    }

                    Log.warn(getClass(), "User has not persistent workspaces");
                }

                @Override
                protected void onFailure(Throwable throwable) {
                    notificationManager.showNotification(
                            new Notification("Unable to get list of workspaces. " + throwable.getMessage(),
                                             Notification.Type.ERROR));
                    Log.error(PersistProjectHandler.class, "Unable to get list of workspaces. " + throwable.getMessage());
                }
            });
        } catch (Exception exception) {
            notificationManager.showNotification(
                    new Notification("Unable to get list of workspaces. " + exception.getMessage(), Notification.Type.ERROR));
            Log.error(PersistProjectHandler.class, "Unable to get list of workspaces. " + exception.getMessage());
        }
    }

    private String getQueryParamsForCopyProjectAction() {
        String query = "action=clone-projects&src-workspace-id=" + Config.getCurrentWorkspace().getId();
        if (!Config.getCurrentWorkspace().isTemporary() && null != appContext.getCurrentProject()) {
            query += "&src-project-name=" + appContext.getCurrentProject().getRootProject().getName();
        }
        return query;
    }

    private String getQueryParamsForCopyProjectAction(String dstWorkspaceName) {
        return getQueryParamsForCopyProjectAction() + "&dest-workspace-name=" + dstWorkspaceName;
    }

}
