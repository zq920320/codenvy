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
package com.codenvy.ide.clone.client.persist;

import org.eclipse.che.ide.api.mvp.View;

/**
 * @author Vitaliy Guliy
 */
public interface PersistProjectView extends View<PersistProjectView.ActionDelegate> {

    interface ActionDelegate {

        void onLogin();

        void onCreateFreeAccount();

    }

    void showDialog();

    void close();

    /**
     * Set new text to display in the window.
     *
     * @param text text to display
     */
    void setText(String text);

}
