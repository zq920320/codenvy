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
package com.codenvy.ide.factory.client;

import com.google.gwt.i18n.client.Messages;

/**
 * @author vzhukovskii@codenvy.com
 */
public interface FactoryLocalizationConstant extends Messages {

    /* ************************************************************************************************************
     *
     * Accept
     *
     * ************************************************************************************************************/

    @Key("project.import.configuring.cloning")
    String cloningSource();

    @Key("project.import.configured.cloned")
    String clonedSource();

    @Key("accept.oauth.login.prompt")
    String oAuthLoginPrompt(String host);

    @Key("accept.oauth.login.title")
    String oAuthLoginTitle();

    @Key("accept.oauth.failed.to.get.current.loggedin.user")
    String oauthFailedToGetCurrentLoggedInUser();

    @Key("accept.oauth.failed")
    String oauthFailed();

    @Key("accept.oauth.success")
    String oauthSuccess();

    @Key("accept.before.need.to.authorize")
    String needToAuthorizeBeforeAcceptMessage();

    @Key("accept.not.supported.authorize")
    String notSupportedAuthorize();

    @Key("accept.canceled.required.authorize")
    String canceledRequiredAuthorize();

    /* ***************************************************************************************************************
     *
     * Importing from Config File
     *
     * **************************************************************************************************************/

    @Key("import.config.view.name")
    String importFromConfigurationName();

    @Key("import.config.view.description")
    String importFromConfigurationDescription();

    @Key("import.config.view.title")
    String importFromConfigurationTitle();

    @Key("import.config.form.prompt")
    String configFileTitle();

    @Key("import.config.form.button.cancel")
    String cancelButton();

    @Key("import.config.form.button.import")
    String importButton();

    /* ***************************************************************************************************************
     *
     * Exporting Config File
     *
     * **************************************************************************************************************/

    @Key("export.config.view.name")
    String exportConfigName();

    @Key("export.config.view.description")
    String exportConfigDescription();

    /* ***************************************************************************************************************
     *
     * Welcome
     *
     * **************************************************************************************************************/

    @Key("welcome.preferences.title")
    String welcomePreferencesTitle();
}
