/*
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
package com.codenvy.ide.hosted.client.workspace;

import com.google.gwt.user.client.Window;

import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * This window is shown when workspace has been stopped not by user
 *
 * @author Mihail Kuznyetsov
 */
@Singleton
public class WorkspaceNotRunningPresenter implements WorkspaceNotRunningView.ActionDelegate {

    private final WorkspaceNotRunningView view;

    private WorkspaceDto workspace;


    @Inject
    public WorkspaceNotRunningPresenter(WorkspaceNotRunningView view) {

        this.view = view;
        this.view.setDelegate(this);
    }

    /**
     * Show dialog
     *
     * @param workspace
     *         workspace that has been shutdown
     */
    public void show(WorkspaceDto workspace) {
        this.workspace = workspace;

        view.show();
    }

    @Override
    public void onOpenDashboardButtonClicked() {
        Window.Location.replace("/dashboard/");
    }

    @Override
    public void onRestartButtonClicked() {
        Window.Location.replace("/dashboard/#/ide/" + workspace.getConfig().getName());
    }
}
