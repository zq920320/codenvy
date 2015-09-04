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
package com.codenvy.ide.ext.gae.client.create;

import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import com.codenvy.ide.ext.gae.client.GAELocalizationConstant;
import com.codenvy.ide.ext.gae.client.GAEResources;
import com.codenvy.ide.ext.gae.client.confirm.ConfirmView;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static com.codenvy.ide.ext.gae.client.create.CreateApplicationPresenter.GOOGLE_APP_ENGINE_URL;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 * @author Dmitry Shnurenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class CreateApplicationPresenterTest {
    private static final String SOME_TEXT = "someText";

    private static final String REST_CONTEXT = "restContext";
    private static final String WORKSPACE_ID = "workspaceId";

    @Mock
    private ConfirmView             view;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private AppContext              context;
    @Mock
    private GAELocalizationConstant locale;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private GAEResources            gaeResources;
    @Mock
    private CurrentProject          currentProject;
    @Mock
    private ProjectDescriptor       projectDescription;

    private CreateApplicationPresenter presenter;

    @Before
    public void setUp() {
        when(locale.createApplicationButtonTitle()).thenReturn(SOME_TEXT);
        when(locale.createApplicationSubtitle()).thenReturn(SOME_TEXT);
        when(locale.createApplicationInstruction()).thenReturn(SOME_TEXT);
        when(gaeResources.gaeCSS().bigSubTitleLabel()).thenReturn(SOME_TEXT);

        presenter = new CreateApplicationPresenter(REST_CONTEXT,
                                                   WORKSPACE_ID,
                                                   view,
                                                   context,
                                                   locale,
                                                   gaeResources);
    }

    @Test
    public void constructorShouldBeDone() throws Exception {
        verify(view).setDelegate(presenter);

        verify(view).setActionButtonTitle(SOME_TEXT);
        verify(view).setSubtitle(SOME_TEXT);
        verify(view).setUserInstructions(SOME_TEXT);
        verify(view).addSubtitleStyleName(SOME_TEXT);
    }

    @Test
    public void createApplicationWindowShouldBeShown() throws Exception {
        presenter.showDialog();

        verify(view).show();
    }

    @Test
    public void viewShouldBeClosedWhenCancelButtonWasClick() throws Exception {
        presenter.onCancelButtonClicked();

        verify(view).close();
    }

    @Test
    public void noActionWhenCurrentProjectIsNullAndDeployButtonClicked() throws Exception {
        when(context.getCurrentProject()).thenReturn(null);

        presenter.onActionButtonClicked();

        verify(view, never()).close();
        verify(view, never()).windowOpen(anyString());
    }

    @Test
    @Ignore
    public void creationProjectWindowShouldBeOpened() throws Exception {
        context.getWorkspace().setId(WORKSPACE_ID);
        when(context.getCurrentProject()).thenReturn(currentProject);
        when(currentProject.getProjectDescription()).thenReturn(projectDescription);
        when(projectDescription.getPath()).thenReturn("/path");

        presenter.onActionButtonClicked();

        String url = GOOGLE_APP_ENGINE_URL +
                     "?redirect_url=https://codenvy.com/" + REST_CONTEXT +
                     "/appengine/" + WORKSPACE_ID + "/change-appid?projectpath=%2Fpath";

        verify(view).close();
        verify(view).windowOpen(eq(url));
    }
}