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
package com.codenvy.ide.ext.gae.client.login;

import org.eclipse.che.ide.api.app.AppContext;
import com.codenvy.ide.ext.gae.client.GAELocalizationConstant;
import com.codenvy.ide.ext.gae.client.GAEResources;
import com.codenvy.ide.ext.gae.client.confirm.ConfirmView;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.verify;

/**
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class OAuthLoginPresenterTest {

    public static final String REST_CONTEXT = "restContext";

    @Mock
    private GAELocalizationConstant locale;
    @Mock
    private ConfirmView             view;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private GAEResources            gaeResources;
    @Mock
    private AppContext              context;


    private OAuthLoginPresenter presenter;

    @Before
    public void setUp() throws Exception {
        presenter = new OAuthLoginPresenter(locale, view, gaeResources, context, REST_CONTEXT);
    }

    @Test
    public void constructorShouldBeDone() throws Exception {
        verify(view).setActionButtonTitle(locale.loginButton());
        verify(view).setSubtitle(locale.loginOauthSubtitle());
        verify(view).setUserInstructions(locale.loginOauthLabel());
        verify(view).addSubtitleStyleName(gaeResources.gaeCSS().bigSubTitleLabel());
        verify(view).setDelegate(presenter);
    }

    @Test
    public void dialogShouldBeShowed() throws Exception {
        presenter.showDialog();

        verify(view).show();
    }

    @Test
    public void windowShouldBeClosedWhenCancelButtonClicked() throws Exception {
        presenter.onCancelButtonClicked();

        verify(view).close();
    }
}