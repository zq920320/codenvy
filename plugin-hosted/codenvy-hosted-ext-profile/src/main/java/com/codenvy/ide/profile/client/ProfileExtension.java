/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
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
package com.codenvy.ide.profile.client;

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.constraints.Anchor;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;

import com.codenvy.ide.profile.client.action.RedirectToDashboardAccountAction;

import com.codenvy.ide.profile.client.action.RedirectToDashboardProjectsAction;
import com.codenvy.ide.profile.client.action.RedirectToDashboardWorkspacesAction;
import com.google.inject.Inject;
import com.google.inject.Singleton;


/**
 * Extension which add profile menu
 *
 * @author Oleksii Orel
 */
@Singleton
@Extension(title = "Profile", version = "1.0.0")
public class ProfileExtension {

    /** Create extension. */
    @Inject
    public ProfileExtension(ActionManager actionManager,
                            RedirectToDashboardAccountAction redirectToDashboardAccountAction,
                            RedirectToDashboardProjectsAction redirectToDashboardProjectsAction,
                            RedirectToDashboardWorkspacesAction redirectToDashboardWorkspacesAction,
                            ProfileLocalizationConstant localizationConstant) {

        DefaultActionGroup mainMenu = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_MAIN_MENU);
        DefaultActionGroup profileActionGroup = new DefaultActionGroup(localizationConstant.profileActionGroup(), true, actionManager);
        actionManager.registerAction(localizationConstant.redirectToDashboardAccountAction(), redirectToDashboardAccountAction);
        actionManager.registerAction(localizationConstant.redirectToDashboardProjectsAction(), redirectToDashboardProjectsAction);
        actionManager.registerAction(localizationConstant.redirectToDashboardWorkspacesAction(), redirectToDashboardWorkspacesAction);
        Constraints constraint = new Constraints(Anchor.BEFORE, IdeActions.GROUP_HELP);
        mainMenu.add(profileActionGroup, constraint);
        profileActionGroup.add(redirectToDashboardAccountAction);
        profileActionGroup.add(redirectToDashboardProjectsAction);
        profileActionGroup.add(redirectToDashboardWorkspacesAction);
    }

}
