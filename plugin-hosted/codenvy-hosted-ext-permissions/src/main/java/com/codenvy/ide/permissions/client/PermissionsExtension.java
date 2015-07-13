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
package com.codenvy.ide.permissions.client;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.Separator;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.event.ProjectActionEvent;
import org.eclipse.che.ide.api.event.ProjectActionHandler;
import org.eclipse.che.ide.api.extension.Extension;
import com.codenvy.ide.permissions.client.indicator.PermissionsIndicatorAction;
import com.codenvy.ide.permissions.client.part.PermissionsAction;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_RIGHT_MAIN_MENU;
import static org.eclipse.che.ide.api.constraints.Anchor.AFTER;
import static org.eclipse.che.ide.api.constraints.Anchor.BEFORE;
import static com.codenvy.ide.permissions.client.indicator.PermissionsIndicatorAction.PERMISSIONS_INDICATOR_ACTION_ID;
import static com.codenvy.ide.permissions.client.part.PermissionsAction.PERMISSIONS_ACTION_ID;

/**
 * The permissions extension.
 *
 * @author Kevin Pollet
 */
@Singleton
@Extension(title = "Permissions", version = "1.0.0")
public class PermissionsExtension implements ProjectActionHandler {
    private static final String PRIVACY_ACTION_ID     = "privacy";
    public static final  String SHARE_GROUP_MAIN_MENU = "share";

    private final ActionManager              actionManager;
    private final PermissionsIndicatorAction permissionsIndicatorAction;
    private final DefaultActionGroup         rightMainMenuGroup;

    @Inject
    public PermissionsExtension(ActionManager actionManager,
                                PermissionsIndicatorAction permissionsIndicatorAction,
                                PermissionsAction permissionsAction,
                                PermissionsResources resources,
                                EventBus eventBus) {

        this.actionManager = actionManager;
        this.permissionsIndicatorAction = permissionsIndicatorAction;
        this.rightMainMenuGroup = (DefaultActionGroup)actionManager.getAction(GROUP_RIGHT_MAIN_MENU);

        resources.permissionsCSS().ensureInjected();
        eventBus.addHandler(ProjectActionEvent.TYPE, this);
        actionManager.registerAction(PERMISSIONS_ACTION_ID, permissionsAction);

        final DefaultActionGroup shareMainMenuGroup = (DefaultActionGroup)actionManager.getAction(SHARE_GROUP_MAIN_MENU);
        if (shareMainMenuGroup != null) {
            shareMainMenuGroup.add(permissionsAction);
        }
    }

    @Override
    public void onProjectOpened(ProjectActionEvent event) {
        actionManager.registerAction(PERMISSIONS_INDICATOR_ACTION_ID, permissionsIndicatorAction);

        if (rightMainMenuGroup != null) {
            rightMainMenuGroup.add(permissionsIndicatorAction, new Constraints(BEFORE, PRIVACY_ACTION_ID));
            rightMainMenuGroup.add(Separator.getInstance(), new Constraints(AFTER, PERMISSIONS_INDICATOR_ACTION_ID));
        }
    }

    @Override
    public void onProjectClosing(ProjectActionEvent event) {
    }

    @Override
    public void onProjectClosed(ProjectActionEvent event) {
        actionManager.unregisterAction(PERMISSIONS_INDICATOR_ACTION_ID);

        if (rightMainMenuGroup != null) {
            boolean found = false;
            int index = 0;
            final Action[] actions = rightMainMenuGroup.getChildActionsOrStubs();
            for (Action oneAction : actions) {
                if (oneAction.equals(permissionsIndicatorAction)) {
                    found = true;
                    break;
                }
                index++;
            }

            if (found) {
                rightMainMenuGroup.remove(permissionsIndicatorAction);
                if (index + 1 < actions.length && actions[index + 1] instanceof Separator) {
                    rightMainMenuGroup.remove(actions[index + 1]);
                }
            }
        }
    }
}
