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
package com.codenvy.docker.client.manage.input;

import com.google.inject.ImplementedBy;

/**
 * The view interface for the input dialog component.
 *
 * @author Sergii Leschenko
 */
@ImplementedBy(InputDialogViewImpl.class)
public interface InputDialogView {
    void setDelegate(ActionDelegate delegate);

    void showDialog();

    void closeDialog();

    void showErrorHint(String message);

    void hideErrorHint();

    String getEmail();

    String getPassword();

    String getServerAddress();

    String getUsername();

    void setUsername(String username);

    void setServerAddress(String serverAddress);

    void setEmail(String email);

    void setPassword(String password);

    void setTitle(String title);

    void setReadOnlyServer();

    void setHideServer();

    boolean isVisibleServer();

    interface ActionDelegate {
        void cancelled();

        void accepted();

        void onEnterClicked();

        void dataChanged();
    }
}
