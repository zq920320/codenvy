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
package com.codenvy.ide.dashboard.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.extension.Extension;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_LEFT_MAIN_MENU;

/**
 * @author Sergii Leschenko
 */
@Singleton
@Extension(title = "Dashboard", version = "3.0.0")
public class DashboardExtension {

    @Inject
    public DashboardExtension(ActionManager actionManager,
                              RedirectToDashboardAction redirectToDashboardAction,
                              DashboardResources dashboardResources) {
        actionManager.registerAction("redirectToUserDashboardAction", redirectToDashboardAction);
        DefaultActionGroup mainToolbarGroup = (DefaultActionGroup)actionManager.getAction(GROUP_LEFT_MAIN_MENU);
        mainToolbarGroup.add(redirectToDashboardAction);
        mainToolbarGroup.addSeparator();

        dashboardResources.dashboardCSS().ensureInjected();
    }
}
