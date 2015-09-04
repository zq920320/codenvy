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
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import com.codenvy.ide.ext.gae.client.GAELocalizationConstant;
import com.codenvy.ide.ext.gae.client.update.UpdateApplicationPresenter;
import com.codenvy.ide.ext.gae.client.utils.GAEUtil;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class DeployApplicationActionTest {

    @Mock
    private GAELocalizationConstant    locale;
    @Mock
    private UpdateApplicationPresenter presenter;
    @Mock
    private AppContext                 context;
    @Mock
    private AnalyticsEventLogger       analyticsEventLogger;
    @Mock
    private GAEUtil                    util;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private ActionEvent                event;
    @Mock
    private CurrentProject             currentProject;
    @InjectMocks
    private DeployApplicationAction    action;

    @Before
    public void setUp() throws Exception {
        when(context.getCurrentProject()).thenReturn(currentProject);
    }

    @Test
    public void constructorShouldBeDone() throws Exception {
        verify(locale).gaeUpdate();
        verify(locale).gaeUpdatePrompt();
    }

    @Test
    public void applicationShouldBeUpdated() throws Exception {
        when(context.getCurrentProject()).thenReturn(currentProject);

        action.actionPerformed(event);

        verify(analyticsEventLogger).log(action);
        verify(presenter).showDialog();
    }

    @Test
    public void actionShouldNotBeVisibleWhenCurrentProjectIsNull() throws Exception {
        when(context.getCurrentProject()).thenReturn(null);

        action.update(event);

        verify(util, never()).isAppEngineProject(any(CurrentProject.class));
        verify(event.getPresentation()).setEnabledAndVisible(false);
    }

    @Test
    public void actionShouldNotBeVisibleWhenCurrentProjectIsNotGaeProject() throws Exception {
        when(util.isAppEngineProject(currentProject)).thenReturn(false);

        action.update(event);

        verify(context).getCurrentProject();
        verify(util).isAppEngineProject(currentProject);
        verify(event.getPresentation()).setEnabledAndVisible(false);
    }

    @Test
    public void actionShouldNotBeVisible() throws Exception {
        when(util.isAppEngineProject(currentProject)).thenReturn(true);

        action.update(event);

        verify(util).isAppEngineProject(currentProject);
        verify(event.getPresentation()).setEnabledAndVisible(true);
    }
}