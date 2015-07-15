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
package com.codenvy.ide.onpremises.permits;


import com.codenvy.ide.onpremises.ActionPermissionLocalizationConstant;
import com.google.gwt.user.client.ui.HTML;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.action.permits.ActionDenyAccessDialog;
import org.eclipse.che.ide.api.action.permits.ResourcesLockedActionPermit;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

/**
 * Implementation of resources locked deny access dialog component for build and run actions.
 *
 * @author Oleksii Orel
 */
public class ResourcesLockedDenyAccessDialogImpl implements ActionDenyAccessDialog {
    private final DialogFactory                        dialogFactory;
    private final ActionPermissionLocalizationConstant localizationConstants;
    private final ResourcesLockedActionPermit          resourcesLockedActionPermit;

    @Inject
    public ResourcesLockedDenyAccessDialogImpl(DialogFactory dialogFactory,
                                               ResourcesLockedActionPermit resourcesLockedActionPermit,
                                               ActionPermissionLocalizationConstant localizationConstants) {
        this.dialogFactory = dialogFactory;
        this.resourcesLockedActionPermit = resourcesLockedActionPermit;
        this.localizationConstants = localizationConstants;

    }

    private String getDialogMessage() {
        if (resourcesLockedActionPermit.isWorkspaceLocked()) {
            return localizationConstants.lockedWorkspaceDialogMessage();
        } else {
            return localizationConstants.unlockedDialogMessage();
        }
    }

    private String getDialogTitle() {
        if (resourcesLockedActionPermit.isWorkspaceLocked()) {
            return localizationConstants.lockedWorkspaceDialogTitle();
        } else {
            return localizationConstants.unlockedDialogTitle();
        }
    }

    @Override
    public void show() {
        dialogFactory.createMessageDialog(getDialogTitle(),
                                          new HTML(getDialogMessage()),
                                          new ConfirmCallback() {
                                              @Override
                                              public void accepted() {
                                              }
                                          })
                     .show();
    }
}
