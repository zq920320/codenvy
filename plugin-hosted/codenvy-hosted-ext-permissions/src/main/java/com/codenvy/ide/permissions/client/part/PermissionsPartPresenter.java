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
package com.codenvy.ide.permissions.client.part;

import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.ProjectActionEvent;
import org.eclipse.che.ide.api.event.ProjectActionHandler;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import com.codenvy.ide.permissions.client.PermissionsLocalizationConstant;
import org.eclipse.che.ide.util.Config;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nullable;
import java.util.List;

/**
 * This presenter displays and manages project permissions.
 *
 * @author Vitaliy Guliy
 */
@Singleton
public class PermissionsPartPresenter extends BasePresenter implements PermissionsPartView.ActionDelegate, ProjectActionHandler {

    private final PermissionsPartView             view;
    private final WorkspaceAgent                  workspaceAgent;
    private final PermissionsLocalizationConstant locale;
    private final AppContext                      appContext;
    private       ProjectDescriptor               project;
    private       String                          permissions;

    @Inject
    public PermissionsPartPresenter(PermissionsLocalizationConstant locale,
                                    PermissionsPartView view,
                                    AppContext appContext,
                                    EventBus eventBus,
                                    WorkspaceAgent workspaceAgent) {
        this.view = view;
        this.workspaceAgent = workspaceAgent;
        this.locale = locale;
        this.appContext = appContext;

        view.setDelegate(this);
        eventBus.addHandler(ProjectActionEvent.TYPE, this);
    }

    public void process() {
        permissions = null;
    }

    @Override
    public void onProjectOpened(ProjectActionEvent event) {
        project = event.getProject();

        checkProjectPermissions();

        showPanel();
    }

    @Override
    public void onProjectClosing(ProjectActionEvent event) {
    }

    private void checkProjectPermissions() {
        // Workspace is not set and project is not opened
        if (Config.getCurrentWorkspace() != null && project != null &&
            project.getPermissions() != null && !project.getPermissions().isEmpty() &&
            project.getPermissions().size() == 1 && "read".equalsIgnoreCase(project.getPermissions().get(0))) {

            // User can only read
            permissions = "read";
            return;
        }

        permissions = "write";
    }


    @Override
    public void onProjectClosed(ProjectActionEvent event) {
        project = null;
        permissions = null;

        hidePanel();
    }

    private void showPanel() {
        workspaceAgent.openPart(this, PartStackType.NAVIGATION);
        view.updatePermissions(permissions);
    }

    private void hidePanel() {
        workspaceAgent.removePart(this);
    }

    /**
     * Display and activate the view.
     */
    public void activateView() {
        partStack.setActivePart(this);
    }

    public void showDialog() {
        PartPresenter activePart = partStack.getActivePart();
        if (activePart == null || !activePart.equals(this)) {
            partStack.setActivePart(this);
        }
    }

    @Override
    public String getTitle() {
        return locale.permissionsViewTitle();
    }

    @Nullable
    @Override
    public ImageResource getTitleImage() {
        return null;
    }

    @Nullable
    @Override
    public String getTitleToolTip() {
        return null;
    }

    @Override
    public int getSize() {
        return 250;
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    /**
     * Determines whether current project is opened in read-only mode.
     *
     * @return <b>true</b> if project is opened in read-only mode, otherwise returns <b>false</b>
     */
    public boolean isReadOnlyMode() {
        ProjectDescriptor project = appContext.getCurrentProject().getRootProject();

        // Workspace is not set and project is not opened
        List<String> permissions;
        if (project != null
            && (permissions = project.getPermissions()) != null
            && permissions.size() == 1
            && "read".equalsIgnoreCase(permissions.get(0))) {

            // User can only read
            return true;
        }

        return false;
    }

    /**
     * Returns current permissions as string.
     *
     * @return current permissions or null.
     */
    public String getPermissions() {
        return permissions;
    }

}
