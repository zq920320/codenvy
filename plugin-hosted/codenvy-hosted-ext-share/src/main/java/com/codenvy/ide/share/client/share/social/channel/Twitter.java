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

import com.codenvy.ide.share.client.ShareLocalizationConstant;
import com.codenvy.ide.share.client.ShareResources;
import com.codenvy.ide.share.client.share.social.SharingChannel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

import javax.annotation.Nonnull;

import static com.google.gwt.http.client.URL.encodeQueryString;

/**
 * Twitter sharing channel.
 *
 * @author Kevin Pollet
 */
public class Twitter extends SharingChannel {
    private final String messageTemplate;

    public Twitter(@Nonnull String messageTemplate,
                   @Nonnull ShareResources resources,
                   @Nonnull ShareLocalizationConstant locale,
                   ClickHandler clickHandler) {
        super(resources.twitter(), locale.socialShareChannelTwitterText(), clickHandler);

        this.messageTemplate = messageTemplate;
    }

    @Override
    public void decorateWidget(@Nonnull Widget element, @Nonnull final String... params) {
        element.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int popupWidth = 550;
                final int popupHeight = 253;
                final int top = (Window.getClientHeight() - popupHeight) / 2;
                final int left = (Window.getClientWidth() - popupWidth) / 2;

                Window.open("https://twitter.com/intent/tweet?text=" + encodeQueryString(render(messageTemplate, params)), "",
                            "menubar=no,toolbar=no,resizable=yes,scrollbars=yes,width=" + popupWidth + ",height=" + popupHeight +
                            ",left=" +
                            left + ",top=" + top);
            }
        }, ClickEvent.getType());
    }
}
