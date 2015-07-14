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

import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

/**
 * View for displaying permissions of current user.
 * The 'Persist' button can be displayed on this view.
 *
 * @author Vitaliy Guliy
 */
public interface PermissionsPartView extends View<PermissionsPartView.ActionDelegate> {

    public interface ActionDelegate extends BaseActionDelegate {
    }

    /**
     * Displays specified permissions.
     *
     * @param permissions
     *         permissions to be displayed.
     *         Use "read" to display read-only permissions;
     *         "write" to display all permissions.
     */
    void updatePermissions(String permissions);

    /**
     * Displays widget on Permissions panel.
     *
     * @param widget
     */
    void displayWidget(Widget widget);

    void setVisible(boolean visible);

}
