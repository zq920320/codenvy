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
package com.codenvy.api.resources.client.preferences;

import com.google.inject.ImplementedBy;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.ext.runner.client.tabs.properties.panel.common.Shutdown;

import javax.annotation.Nonnull;

/**
 * @author Ann Shumilova
 */
@ImplementedBy(RunnerPreferencesViewImpl.class)
public interface RunnerPreferencesView extends View<RunnerPreferencesView.ActionDelegate> {

    /**
     * Select a given value into Shutdown field.
     *
     * @param shutdown
     *         value that needs to be chosen
     */
    void selectShutdown(@Nonnull Shutdown shutdown);

    /** @return chosen value of Shutdown field */
    @Nonnull
    Shutdown getShutdown();

    /**
     * Change state of set timeout button.
     *
     * @param isEnabled
     *         enabled state
     */
    void enableSetButton(boolean isEnabled);

    public interface ActionDelegate {

        void onValueChanged();

        void onSetShutdownClicked();
    }
}
