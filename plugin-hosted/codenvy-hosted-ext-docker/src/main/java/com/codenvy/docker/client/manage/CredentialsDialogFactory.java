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


import com.codenvy.docker.client.manage.input.InputDialog;
import com.codenvy.docker.client.manage.input.callback.InputCallback;

import static com.codenvy.docker.client.manage.input.InputDialogPresenter.InputMode;

/**
 * Factory for {@link InputDialog} component.
 *
 * @author Sergii Leschenko
 */
public interface CredentialsDialogFactory {
    /**
     * Create input dialog
     *
     * @param inputMode
     *         Input mode of dialog. Can be equals to CREATE or EDIT.
     * @param inputCallback
     *         callback which will be called when user click on Save button
     * @return created instance of {@link InputDialog}
     */
    InputDialog createInputDialog(InputMode inputMode, InputCallback inputCallback);
}
