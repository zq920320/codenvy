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
package com.codenvy.ide.ext.gae.client.actions;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import com.codenvy.ide.ext.gae.client.GAELocalizationConstant;
import com.codenvy.ide.ext.gae.client.update.UpdateApplicationPresenter;
import com.codenvy.ide.ext.gae.client.utils.GAEUtil;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.validation.constraints.NotNull;

/**
 * Action for deploying application via Google App Engine.
 *
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@Singleton
public class DeployApplicationAction extends Action {

    private final UpdateApplicationPresenter presenter;
    private final AppContext                 appContext;
    private final AnalyticsEventLogger       eventLogger;
    private final GAEUtil                    gaeUtil;

    @Inject
    public DeployApplicationAction(UpdateApplicationPresenter presenter,
                                   GAELocalizationConstant locale,
                                   AppContext appContext,
                                   GAEUtil gaeUtil,
                                   AnalyticsEventLogger eventLogger) {
        super(locale.gaeUpdate(), locale.gaeUpdatePrompt());
        this.presenter = presenter;
        this.appContext = appContext;
        this.gaeUtil = gaeUtil;
        this.eventLogger = eventLogger;
    }

    /** {@inheritDoc} */
    @Override
    public void update(@NotNull ActionEvent event) {
        boolean visible = false;

        CurrentProject currentProject = appContext.getCurrentProject();

        if (currentProject != null) {
            visible = gaeUtil.isAppEngineProject(currentProject);
        }

        event.getPresentation().setEnabledAndVisible(visible);
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(@NotNull ActionEvent actionEvent) {
        eventLogger.log(this);
        presenter.showDialog();
    }
}