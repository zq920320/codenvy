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
package com.codenvy.ide.ext.gae.client.confirm;


import org.eclipse.che.ide.api.mvp.View;
import com.google.inject.ImplementedBy;

import javax.annotation.Nonnull;

/**
 * The abstract view that represents the create application visual part.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 * @author Dmitry Shnurenko
 */
@ImplementedBy(ConfirmViewImpl.class)
public interface ConfirmView extends View<ConfirmView.ActionDelegate> {

    /**
     * Sets the title for action button.
     *
     * @param title
     *         text of title
     */
    void setActionButtonTitle(@Nonnull String title);

    /**
     * Adds new style name for the subtitle label.
     *
     * @param styleName
     *         the name of style
     */
    void addSubtitleStyleName(@Nonnull String styleName);

    /**
     * Opens new window for creating project.
     *
     * @param url
     *         url which allows open window for adding project id and other parameters
     */
    void windowOpen(@Nonnull String url);

    /** Shows create application window. */
    void show();

    /** Closes create application window. */
    void close();

    /**
     * Sets the instructions for user into the view.
     *
     * @param instructions
     *         messages for the user
     */
    void setUserInstructions(@Nonnull String instructions);

    void setSubtitle(@Nonnull String subtitle);

    interface ActionDelegate {

        /** Performs some actions in response to user's clicking on the action button. */
        void onActionButtonClicked();

        /** Performs some actions in response to user's clicking on cancel button. */
        void onCancelButtonClicked();
    }
}
