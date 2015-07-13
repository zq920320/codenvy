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
package com.codenvy.ide.share.client;

import com.google.gwt.i18n.client.Messages;

/**
 * @author vzhukovskii@codenvy.com
 */
public interface ShareLocalizationConstant extends Messages {
     /* ************************************************************************************************************
     *
     * Commit dialog
     *
     * ************************************************************************************************************/

    @Key("commit.dialog.title")
    String commitDialogTitle();

    @Key("commit.dialog.message")
    String commitDialogMessage();

    @Key("commit.dialog.description.title")
    String commitDialogDescriptionTitle();

    @Key("commit.dialog.description.default.message")
    String commitDialogDescriptionDefaultMessage(String date);

    @Key("commit.dialog.button.ok.text")
    String commitDialogButtonOkText();

    @Key("commit.dialog.button.continue.text")
    String commitDialogButtonContinueText();

    /* ***************************************************************************************************************
     *
     * Share button
     *
     * **************************************************************************************************************/

    @Key("share.button.text")
    String shareButtonText();

    @Key("share.button.tooltip.text")
    String shareButtonTooltipText();

    @Key("share.button.dropdown.header.title")
    String shareButtonDropDownHeaderTitle(String item);

    @Key("share.button.dropdown.share.project.name")
    String shareButtonDropDownShareProjectName();

    @Key("share.button.dropdown.share.project.text")
    String shareButtonDropDownShareProjectText();

    @Key("share.button.dropdown.share.cloning.template.name")
    String shareButtonDropDownShareCloningTemplateName();

    @Key("share.button.dropdown.share.cloning.template.text")
    String shareButtonDropDownShareCloningTemplateText();

    /* ***************************************************************************************************************
     *
     * Social
     *
     * **************************************************************************************************************/

    @Key("social.share.project.message")
    String socialShareProjectMessage();

    @Key("social.share.project.facebook.message")
    String socialShareProjectFacebookMessage();

    @Key("social.share.cloning.template.facebook.message")
    String socialShareCloningTemplateFacebookMessage();

    @Key("social.share.cloning.template.message")
    String socialShareCloningTemplateMessage();

    @Key("social.share.cloning.template.facebook.description")
    String socialShareCloningTemplateFacebookDescription();

    @Key("social.share.channel.facebook.text")
    String socialShareChannelFacebookText();

    @Key("social.share.channel.google.plus.text")
    String socialShareChannelGooglePlusText();

    @Key("social.share.channel.twitter.text")
    String socialShareChannelTwitterText();

    @Key("social.share.channel.iframe.text")
    String socialShareChannelIFrameText();

    @Key("social.share.channel.iframe.snippet.title")
    String socialShareChannelIFrameSnippetTitle();

    @Key("social.share.channel.iframe.snippet.template")
    String socialShareChannelIFrameSnippetTemplate();

    @Key("social.share.channel.html.text")
    String socialShareChannelHtmlText();

    @Key("social.share.channel.html.snippet.title")
    String socialShareChannelHtmlSnippetTitle();

    @Key("social.share.channel.html.snippet.template")
    String socialShareChannelHtmlSnippetTemplate();

    @Key("social.share.channel.github.text")
    String socialShareChannelGitHubText();

    @Key("social.share.channel.github.snippet.title")
    String socialShareChannelGitHubSnippetTitle();

    @Key("social.share.channel.github.snippet.template")
    String socialShareChannelGitHubSnippetTemplate();

    @Key("social.share.channel.bitbucket.text")
    String socialShareChannelBitbucketText();

    @Key("social.share.channel.bitbucket.snippet.title")
    String socialShareChannelBitbucketSnippetTitle();

    @Key("social.share.channel.bitbucket.snippet.template")
    String socialShareChannelBitbucketSnippetTemplate();

    @Key("social.share.channel.email.text")
    String socialShareChannelEmailText();
}
