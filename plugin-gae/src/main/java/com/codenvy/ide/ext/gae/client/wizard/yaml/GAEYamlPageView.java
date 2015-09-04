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
package com.codenvy.ide.ext.gae.client.wizard.yaml;

import org.eclipse.che.ide.api.mvp.View;
import com.google.inject.ImplementedBy;

import javax.annotation.Nonnull;

/**
 * Interface describes methods which allow get and set special values parameters on view.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(GAEYamlPageViewImpl.class)
public interface GAEYamlPageView extends View<GAEYamlPageView.ActionDelegate> {

    /**
     * Returns value of application id, which need to create gae project, from special place on view.
     *
     * @return value of group id
     */
    @Nonnull
    String getGaeAppIdValue();

    /**
     * Sets value of application id in special place on view.
     *
     * @param applicationId
     *         application id value which need set
     */
    void setGaeApplicationId(@Nonnull String applicationId);

    /**
     * Sets spacial colour border around the place on view in which application id is set.
     *
     * @param isCorrect
     *         <code>true</code> incorrect border is shown,<code>false</code> border is not shown
     */
    void showApplicationIdInCorrectIndicator(boolean isCorrect);

    /** Sets focus to application id field on view. */
    void setFocusToApplicationIdField();

    /** Required for delegating functions in the view. */
    interface ActionDelegate {
        /** Method sets value of parameter to attribute map when user changed it. */
        void onValueChanged();
    }
}
