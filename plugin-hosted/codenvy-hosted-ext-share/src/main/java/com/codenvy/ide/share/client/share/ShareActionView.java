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
package com.codenvy.ide.share.client.share;

import org.eclipse.che.ide.api.mvp.View;
import com.codenvy.ide.share.client.share.social.Item;
import com.codenvy.ide.share.client.share.social.SharingChannel;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Share action view allowing to share item to sharing channels.
 *
 * @author Kevin Pollet
 */
public interface ShareActionView extends View<ShareActionView.ActionDelegate> {

    interface ActionDelegate {
        /**
         * Called when the mouse is over the share action.
         */
        void onMouseOver();

        /**
         * Called when the mouse leaves the share action.
         */
        void onMouseOut();

        /**
         * Called when the share action is clicked.
         */
        void onClick();

        /**
         * Called when an item to share is clicked.
         *
         * @param item
         *         the clicked item never {@code null}.
         */
        void onItemToShareClick(@Nonnull Item item);

        /**
         * Called when the sharing channel back button is clicked.
         */
        void onSharingChannelsBackClick();
    }

    /**
     * Show the share action tooltip.
     */
    void showTooltip();

    /**
     * Hide the share action tooltip.
     */
    void hideTooltip();

    /**
     * Returns if the share action dropdown is visible.
     *
     * @return {@code true} if visible, {@code false} otherwise.
     */
    boolean isDropDownVisible();

    /**
     * Show the {@link Item} to share dropdown.
     *
     * @param itemsToShare
     *         the {@link Item} to share list, must not be {@code null}.
     */
    void showItemsToShareDropDown(@Nonnull List<Item> itemsToShare);

    /**
     * Show the sharing channels dropdown for the given {@link Item}.
     *
     * @param item
     *         the {@link Item} to share, must not be {@code null}.
     * @param params
     *         parameters map passed to {@link SharingChannel#decorateWidget(com.google.gwt.user.client.ui.Widget,
     *         String...)}, must not be {@code null}
     */
    void showSharingChannelsDropDown(@Nonnull Item item, @Nonnull String... params);

    /**
     * Hide the share dropdown.
     */
    void hideDropDown();

}
