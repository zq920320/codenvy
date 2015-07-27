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
package com.codenvy.docker.client.manage;

import com.codenvy.docker.dto.AuthConfig;
import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * The view interface for the preferences window which displays user's credentials to docker registry.
 *
 * @author Sergii Leschenko
 */
@ImplementedBy(CredentialsPreferencesViewImpl.class)
public interface CredentialsPreferencesView extends View<CredentialsPreferencesView.ActionDelegate> {

    void setKeys(@Nonnull Collection<AuthConfig> keys);

    interface ActionDelegate {
        void onAddClicked();

        void onEditClicked(AuthConfig authConfig);

        void onDeleteClicked(AuthConfig authConfig);
    }
}
