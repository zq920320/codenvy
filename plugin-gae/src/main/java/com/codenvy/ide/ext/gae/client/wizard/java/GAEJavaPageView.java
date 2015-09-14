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
package com.codenvy.ide.ext.gae.client.wizard.java;

import org.eclipse.che.ide.api.mvp.View;
import com.google.inject.ImplementedBy;

import javax.validation.constraints.NotNull;

/**
 * Interface describes methods which allow get and set special values parameters on view.
 *
 * @author Dmitry Shnurenko
 */
@ImplementedBy(GAEJavaPageViewImpl.class)
public interface GAEJavaPageView extends View<GAEJavaPageView.ActionDelegate> {

    /**
     * Returns value of group id from special place on view.
     *
     * @return value of group id
     */
    @NotNull
    String getGroupIdValue();

    /**
     * Sets value of group id to special place on view.
     *
     * @param groupIdValue
     *         value which need set
     */
    void setGroupIdValue(@NotNull String groupIdValue);

    /**
     * Returns value of artifact id from special place on view.
     *
     * @return value of artifact id
     */
    @NotNull
    String getArtifactIdValue();

    /**
     * Sets value of artifact id to special place on view.
     *
     * @param artifactIdValue
     *         value which need set
     */
    void setArtifactIdValue(@NotNull String artifactIdValue);

    /**
     * Returns value of version from special place on view.
     *
     * @return value of version
     */
    @NotNull
    String getVersionValue();

    /**
     * Sets value of version to special place on view.
     *
     * @param versionValue
     *         value which need set
     */
    void setVersion(@NotNull String versionValue);

    /**
     * Returns value of application id, which need to create gae project, from special place on view.
     *
     * @return value of group id
     */
    @NotNull
    String getGaeAppIdValue();

    /**
     * Sets value of application id in special place on view.
     *
     * @param applicationId
     *         application id value which need set
     */
    void setGaeApplicationId(@NotNull String applicationId);

    /**
     * Sets spacial colour border around the place on view in which group id is set.
     *
     * @param isCorrect
     *         <code>true</code> incorrect border is shown,<code>false</code> border is not shown
     */
    void showGroupIdInCorrectIndicator(boolean isCorrect);

    /**
     * Sets spacial colour border around the place on view in which artifact id is set.
     *
     * @param isCorrect
     *         <code>true</code> incorrect border is shown,<code>false</code> border is not shown
     */
    void showArtifactIdInCorrectIndicator(boolean isCorrect);

    /**
     * Sets spacial colour border around the place on view in which version is set.
     *
     * @param isCorrect
     *         <code>true</code> incorrect border is shown,<code>false</code> border is not shown
     */
    void showVersionInCorrectIndicator(boolean isCorrect);

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
