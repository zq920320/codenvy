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
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import javax.annotation.Nonnull;

/**
 * Email sharing channel.
 *
 * @author Kevin Pollet
 */
public class Email extends SharingChannel {

    public Email(ShareResources resources, ShareLocalizationConstant locale, ClickHandler clickHandler) {
        super(resources.email(), locale.socialShareChannelEmailText(), clickHandler);
    }

    @Override
    public void decorateWidget(@Nonnull Widget element, @Nonnull String... params) {
    }
}
