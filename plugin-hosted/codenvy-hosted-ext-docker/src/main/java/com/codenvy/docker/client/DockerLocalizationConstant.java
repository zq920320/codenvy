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
package com.codenvy.docker.client;

import com.google.gwt.i18n.client.Messages;

import javax.inject.Singleton;

/**
 * @author Sergii Leschenko
 */
@Singleton
public interface DockerLocalizationConstant extends Messages {
    @Key("docker.preferences.title")
    String dockerPreferencesTitle();

    @Key("docker.preferences.category")
    String dockerPreferencesCategory();


    @Key("docker.add.credential.text")
    String dockerAddCredentialsText();


    @Key("docker.input.credentials.server.address.label")
    String inputCredentialsServerAddressLabel();

    @Key("docker.input.credentials.username.label")
    String inputCredentialsUsernameLabel();

    @Key("docker.input.credentials.password.label")
    String inputCredentialsPasswordLabel();

    @Key("docker.input.credentials.email.label")
    String inputCredentialsEmailLabel();

    @Key("docker.input.missed.value.of.field")
    String inputMissedValueOfField(String invalidField);


    @Key("docker.input.credentials.cancel.button.text")
    String inputCredentialsCancelButtonText();

    @Key("docker.input.credentials.save.button.text")
    String inputCredentialsSaveButtonText();

    @Key("docker.remove.credentials.confirm.title")
    String removeCredentialsConfirmTitle();

    @Key("docker.remove.credentials.confirm.text.regexp")
    String removeCredentialsConfirmText(String serverAddress);
}
