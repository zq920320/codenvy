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
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

import javax.annotation.Nonnull;

/**
 * Facebook sharing channel.
 *
 * @author Kevin Pollet
 */
public class Facebook extends SharingChannel {
    private final String                    imageURL;
    private final String                    messageTemplate;
    private final ShareLocalizationConstant locale;

    public Facebook(@Nonnull String messageTemplate,
                    @Nonnull ShareResources resources,
                    @Nonnull ShareLocalizationConstant locale,
                    ClickHandler clickHandler) {
        super(resources.facebook(), locale.socialShareChannelFacebookText(), clickHandler);

        this.messageTemplate = messageTemplate;
        this.locale = locale;
        this.imageURL = new UrlBuilder().setProtocol(Window.Location.getProtocol())
                                        .setHost(Window.Location.getHost())
                                        .setPath("factory/resources/codenvy.png")
                                        .buildString();
    }

    @Override
    public void decorateWidget(@Nonnull Widget element, @Nonnull final String... params) {
        element.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openFeedDialog(render(messageTemplate, params), locale.socialShareCloningTemplateFacebookDescription(),
                               params[1], imageURL);
            }

            private native void openFeedDialog(String name, String description, String factoryShareURL, String pictureURL) /*-{
                $wnd.FB.ui({
                    method: 'feed',
                    name: name,
                    link: factoryShareURL,
                    caption: factoryShareURL,
                    description: description,
                    picture: pictureURL
                });
            }-*/;

        }, ClickEvent.getType());
    }
}
