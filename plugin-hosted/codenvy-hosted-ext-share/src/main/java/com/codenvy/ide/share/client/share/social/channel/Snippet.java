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
package com.codenvy.ide.share.client.share.social.channel;

import com.codenvy.ide.share.client.ShareResources;
import com.codenvy.ide.share.client.share.SnippetPopup;
import com.codenvy.ide.share.client.share.social.SharingChannel;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.ui.zeroClipboard.ClipboardButtonBuilder;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;

/**
 * Snippet sharing channel.
 *
 * @author Kevin Pollet
 */
public class Snippet extends SharingChannel {
    private final String                   title;
    private final String                   snippetTemplate;
    private final ShareResources           resources;
    private final ClipboardButtonBuilder   clipboardButtonBuilder;
    private final CoreLocalizationConstant coreLocale;

    public Snippet(@NotNull SVGResource icon,
                   @NotNull String label,
                   @NotNull String title,
                   @NotNull String snippetTemplate,
                   ClickHandler clickHandler,
                   @NotNull ShareResources resources,
                   @NotNull ClipboardButtonBuilder clipboardButtonBuilder,
                   @NotNull CoreLocalizationConstant coreLocale) {
        super(icon, label, clickHandler);

        this.title = title;
        this.snippetTemplate = snippetTemplate;
        this.resources = resources;
        this.clipboardButtonBuilder = clipboardButtonBuilder;
        this.coreLocale = coreLocale;
    }

    @Override
    public void decorateWidget(@NotNull Widget element, @NotNull final String... params) {
        element.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new SnippetPopup(title, render(snippetTemplate, params), clipboardButtonBuilder, coreLocale, resources);
            }

        }, ClickEvent.getType());
    }
}
