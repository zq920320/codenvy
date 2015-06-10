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
package com.codenvy.ide.onpremises;

import com.google.gwt.i18n.client.Messages;

/**
 * The localization constants for the permission action.
 *
 * @author Oleksii Orel
 */
public interface ActionPermissionLocalizationConstant extends Messages {
    /*
     * Locked dialog
     */
    @Key("actionPermission.locked.account.dialog.title")
    String lockedAccountDialogTitle();

    @Key("actionPermission.locked.account.dialog.message")
    String lockedAccountDialogMessage(String ownerEmail);

    @Key("actionPermission.locked.workspace.dialog.title")
    String lockedWorkspaceDialogTitle();

    @Key("actionPermission.locked.workspace.dialog.message")
    String lockedWorkspaceDialogMessage(String ownerEmail);

    @Key("actionPermission.unlocked.dialog.title")
    String unlockedDialogTitle();

    @Key("actionPermission.unlocked.dialog.message")
    String unlockedDialogMessage();
}
