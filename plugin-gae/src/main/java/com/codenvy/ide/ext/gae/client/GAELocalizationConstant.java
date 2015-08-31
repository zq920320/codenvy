/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.ext.gae.client;

import com.google.gwt.i18n.client.Messages;
import com.google.inject.Singleton;

/**
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@Singleton
public interface GAELocalizationConstant extends Messages {
    @Key("create.application.instruction")
    String createApplicationInstruction();

    @Key("messages.buildInProgress")
    String buildInProgress(String project);

    @Key("build.started")
    String buildStarted(String project);

    @Key("cancel.button")
    String cancelButton();

    @Key("unknown.error.message")
    String unknownErrorMessage();

    @Key("login.oauth.label")
    String loginOauthLabel();

    @Key("login.button")
    String loginButton();

    @Key("update.button")
    String updateButton();

    @Key("deploy.application.instruction")
    String deployApplicationInstruction();

    @Key("dashboard.gae.project.update")
    String gaeUpdate();

    @Key("dashboard.gae.project.update.prompt")
    String gaeUpdatePrompt();

    @Key("messages.build.failed")
    String messagesBuildFailed();

    @Key("messages.build.canceled")
    String messagesBuildCanceled(String projectName);

    @Key("error.user.not.authorized")
    String authorizationError();

    @Key("deploy.started")
    String deployStarted(String applicationName);

    @Key("deploy.success")
    String deploySuccess(String link);

    @Key("application.action.view.title")
    String applicationActionViewTitle();

    @Key("create.application.subtitle")
    String createApplicationSubtitle();

    @Key("create.button")
    String createApplicationButtonTitle();

    @Key("login.oauth.subtitle")
    String loginOauthSubtitle();

    @Key("deploy.application.subtitle")
    String deployApplicationSubtitle();

    @Key("deploy.error")
    String deployError(String applicationName);

    @Key("wizard.maven.configuration")
    String wizardMavenConfiguration();

    @Key("wizard.maven.group.id")
    String wizardMavenGroupId();

    @Key("wizard.maven.artifact.id")
    String wizardMavenArtifactId();

    @Key("wizard.maven.version")
    String wizardMavenVersion();

    @Key("wizard.app.engine.configuration")
    String wizardAppEngineConfiguration();

    @Key("wizard.app.engine.application.id")
    String wizardAppEngineApplicationId();

    @Key("wizard.default.app.version")
    String wizardDefaultAppVersion();

    @Key("deploy.group.menu")
    String deployGroupMenu();

    @Key("error.validate.web.engine")
    String errorValidateWebEngine();

    @Key("error.validate.yaml")
    String errorValidateYaml();

    @Key("wizard.application.id.default")
    String wizardApplicationIdDefault();

    @Key("wizard.label.app.id.java")
    String wizardLabelAppIdJava();

    @Key("wizard.label.app.id.yaml")
    String wizardLabelAppIdYaml();
}