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
package com.codenvy.ide.share.client.share.social;

import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * Item to share.
 */
public abstract class Item {
    private final String               name;
    private final SVGResource          icon;
    private final String               label;
    private final List<SharingChannel> sharingChannels;

    /**
     * Constructs an instance of item.
     *
     * @param name
     *         the item name, must not be {@code null}.
     * @param icon
     *         the item icon, must not be {@code null}.
     * @param label
     *         the item label, must not be {@code null}.
     * @param sharingChannels
     *         the {@link SharingChannel} list.
     */
    public Item(@NotNull String name,
                @NotNull SVGResource icon,
                @NotNull String label,
                @NotNull List<SharingChannel> sharingChannels) {
        this.name = name;
        this.icon = icon;
        this.label = label;
        this.sharingChannels = sharingChannels;
    }

    /**
     * Returns the item name.
     *
     * @return the item name, never {@code null}.
     */
    public
    @NotNull
    String getName() {
        return name;
    }

    /**
     * Returns the item icon.
     *
     * @return the item icon, never {@code null}.
     */
    public
    @NotNull
    SVGResource getIcon() {
        return icon;
    }

    /**
     * Returns the item label.
     *
     * @return the item label, never {@code null}.
     */
    public
    @NotNull
    String getLabel() {
        return label;
    }

    /**
     * Returns the item {@link SharingChannel} list.
     *
     * @return the item {@link SharingChannel}, never {@code null}.
     */
    public
    @NotNull
    List<SharingChannel> getSharingChannels() {
        return unmodifiableList(sharingChannels);
    }
}
