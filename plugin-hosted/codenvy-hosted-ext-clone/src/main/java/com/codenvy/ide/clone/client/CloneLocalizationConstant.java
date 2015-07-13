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

import com.google.gwt.i18n.client.Messages;

/**
 * @author vzhukovskii@codenvy.com
 */
public interface CloneLocalizationConstant extends Messages {
    @Key("clone.toolbar-button.text")
    String cloneToolbarButtonText();

    @Key("clone.toolbar-button.title")
    String cloneToolbarButtonTitle();

    @Key("clone.project.menu.title")
    String cloneProjectMenuTitle();


    @Key("persist.toolbar-button.text")
    String persistToolbarButtonText();

    @Key("persist.toolbar-button.title")
    String persistToolbarButtonTitle();

    @Key("copy.toolbar-button.text")
    String copyToolbarButtonText();

    @Key("copy.toolbar-button.title")
    String copyToolbarButtonTitle();


    @Key("copy.to.named.workspace.dialog.title")
    String copyToNamedWorkspaceTitle();

    @Key("copy.to.named.workspace.dialog.text")
    String copyToNamedWorkspaceText();

    @Key("copy.to.named.workspace.readonly.dialog.text")
    String copyToNamedWorkspaceReadonlyText();

     /* ***************************************************************************************************************
     *
     * Permissions
     *
     * ***************************************************************************************************************/

    @Key("permissions.view.text.persist")
    String permissionsViewPersistText();

    @Key("permissions.view.text.persist.for.owner")
    String permissionsViewPersistTextForOwner();

    @Key("permissions.view.text.copy")
    String permissionsViewCopyText();

    @Key("login.button")
    String loginButtonTitle();

    @Key("create.account.button")
    String createAccountButtonTitle();

}
