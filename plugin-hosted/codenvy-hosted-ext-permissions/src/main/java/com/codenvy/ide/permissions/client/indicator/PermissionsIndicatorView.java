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
package com.codenvy.ide.permissions.client.indicator;

import org.eclipse.che.ide.api.mvp.View;

/**
 * Indicator displaying the user permissions for the current project.
 *
 * @author Kevin Pollet
 */
public interface PermissionsIndicatorView extends View<PermissionsIndicatorView.ActionDelegate> {
    /**
     * Delegation of user events.
     */
    interface ActionDelegate {
        /**
         * Called when the user mouse is on the permissions indicator.
         */
        void onMouseOver();

        /**
         * Called when the user mouse leave the permissions indicator.
         */
        void onMouseOut();

        /**
         * Called when the user click on the permissions indicator.
         */
        void onClick();

        /**
         * Called when the user click on the tooltip link.
         */
        void onTooltipLinkClick();
    }

    /**
     * Set if the user permissions for the project is only read.
     *
     * @param readOnly
     *         {@code true} if user permissions for the project is only read, {@code false} otherwise.
     */
    void setReadOnly(boolean readOnly);

    /**
     * The user permissions {@link java.lang.String} for the current project.
     *
     * @param permissions
     *         the permissions {@link java.lang.String}.
     */
    void setPermissions(String permissions);

    /**
     * Show the permissions indicator tooltip.
     */
    void showTooltip();

    /**
     * Hide the permissions indicator tooltip.
     */
    void hideTooltip();

    /**
     * Set the permissions indicator tooltip title.
     *
     * @param title
     *         the header title {@link java.lang.String}.
     */
    void setTooltipTitle(String title);

    /**
     * Set the permissions indicator tooltip message.
     *
     * @param message
     *         the body message {@link java.lang.String}.
     */
    void setTooltipMessage(String message);

    /**
     * Set the permissions indicator tooltip link text.
     *
     * @param text
     *         the link text {@link java.lang.String}.
     */
    void setTooltipLinkText(String text);
}
