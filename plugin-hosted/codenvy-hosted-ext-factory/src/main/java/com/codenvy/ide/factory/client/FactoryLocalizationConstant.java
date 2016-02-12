/*
 *  [2012] - [2016] Codenvy, S.A.
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

    @Key("projects.import.configuring.cloning")
    String cloningSource();

    @Key("project.import.configuring.cloning")
    String cloningSource(String projectName);

    @Key("project.import.configured.cloned")
    String clonedSource(String projectName);

    @Key("project.import.cloning.failed.title")
    String cloningSourceFailedTitle(String projectName);

    @Key("project.import.configuring.failed")
    String configuringSourceFailed(String projectName);

    @Key("project.already.imported")
    String projectAlreadyImported(String projectName);

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

    @Key("accept.dialog.ssh.not.found.title")
    String dialogSshNotFoundTitle();

    @Key("accept.dialog.ssh.not.found.text")
    String dialogSshNotFoundText();

    @Key("accept.notification.ssh.generated.successfully")
    String notificationSshGeneratedSuccessfully();

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

    @Key("export.config.error.message")
    String exportConfigErrorMessage();

    @Key("export.config.dialog.not.under.vcs.title")
    String exportConfigDialogNotUnderVcsTitle();

    @Key("export.config.dialog.not.under.vcs.text")
    String exportConfigDialogNotUnderVcsText();

    /* ***************************************************************************************************************
     *
     * Welcome
     *
     * **************************************************************************************************************/

    @Key("welcome.preferences.title")
    String welcomePreferencesTitle();

    /* ***************************************************************************************************************
     *
     * Create factory
     *
     * **************************************************************************************************************/

    @Key("create.factory.form.title")
    String createFactoryTitle();

    @Key("create.factory.action.title")
    String createFactoryActionTitle();

    @Key("create.factory.label.name")
    String createFactoryName();

    @Key("create.factory.button.create")
    String createFactoryButton();

    @Key("create.factory.button.close")
    String createFactoryButtonClose();

    @Key("create.factory.label.link")
    String createFactoryLink();

    @Key("create.factory.already.exist")
    String createFactoryAlreadyExist();

    @Key("create.factory.unable.create.from.current.workspace")
    String createFactoryFromCurrentWorkspaceFailed();

    @Key("create.factory.configure.button.tooltip")
    String createFactoryConfigureTooltip();

    @Key("create.factory.launch.button.tooltip")
    String createFactoryLaunchTooltip();
}
