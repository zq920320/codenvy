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
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.ui.Widget;

import javax.validation.constraints.NotNull;

/**
 * Google plus sharing channel.
 *
 * @author Kevin Pollet
 */
public class GooglePlus extends SharingChannel {
    private final String messageTemplate;

    public GooglePlus(@NotNull String messageTemplate,
                      @NotNull ShareResources resources,
                      @NotNull ShareLocalizationConstant locale,
                      ClickHandler clickHandler) {
        super(resources.googlePlus(), locale.socialShareChannelGooglePlusText(), clickHandler);

        this.messageTemplate = messageTemplate;
    }

    @Override
    public void decorateWidget(@NotNull final Widget element, @NotNull String... params) {
        element.getElement().addClassName("g-interactivepost");
        element.getElement().setAttribute("data-clientid", "625433458903-524olpl81vfgkjf00579bafavujnlv1g.apps.googleusercontent.com");
        element.getElement().setAttribute("data-prefilltext", render(messageTemplate, params));
        element.getElement().setAttribute("data-contenturl", params[1]);
        element.getElement().setAttribute("data-calltoactionurl", params[1]);
        element.getElement().setAttribute("data-cookiepolicy", "single_host_origin");
        element.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    renderInteractivePostButton(element.getElement().getParentElement());
                }
            }

            private native void renderInteractivePostButton(Element element) /*-{
                $wnd.gapi.interactivepost.go(element);
            }-*/;
        });
    }

}
