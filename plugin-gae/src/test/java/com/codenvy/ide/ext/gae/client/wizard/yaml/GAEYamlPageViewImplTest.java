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

import com.codenvy.ide.ext.gae.client.GAELocalizationConstant;
import com.codenvy.ide.ext.gae.client.GAEResources;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static com.codenvy.ide.ext.gae.client.wizard.yaml.GAEYamlPageView.ActionDelegate;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(GwtMockitoTestRunner.class)
public class GAEYamlPageViewImplTest {

    private static final String SOME_TEXT = "someText";

    @Mock
    private GAELocalizationConstant locale;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private GAEResources            resource;
    @Mock
    private ActionDelegate          delegate;

    @InjectMocks
    private GAEYamlPageViewImpl view;

    @Before
    public void setUp() {
        view.setDelegate(delegate);
    }

    @Test
    public void gaeApplicationIdShouldBeGot() throws Exception {
        view.getGaeAppIdValue();

        verify(view.gaeAppId).getText();
    }

    @Test
    public void applicationIdShouldBeSet() throws Exception {
        view.setGaeApplicationId(SOME_TEXT);

        verify(view.gaeAppId).setText(SOME_TEXT);
    }

    @Test
    public void incorrectIndicatorShouldBeShown() throws Exception {
        when(resource.gaeCSS().wizardIncorrectValueBorder()).thenReturn(SOME_TEXT);

        view.showApplicationIdInCorrectIndicator(true);

        verify(resource.gaeCSS()).wizardIncorrectValueBorder();
        verify(view.gaeAppId).addStyleName(SOME_TEXT);
        verify(view.gaeAppId, never()).removeStyleName(anyString());
    }

    @Test
    public void incorrectIndicatorShouldNotBeShown() throws Exception {
        when(resource.gaeCSS().wizardIncorrectValueBorder()).thenReturn(SOME_TEXT);

        view.showApplicationIdInCorrectIndicator(false);

        verify(resource.gaeCSS()).wizardIncorrectValueBorder();
        verify(view.gaeAppId).removeStyleName(SOME_TEXT);
        verify(view.gaeAppId, never()).addStyleName(anyString());
    }

    @Test
    public void focusShouldBeSet() throws Exception {
        view.setFocusToApplicationIdField();

        verify(view.gaeAppId).setFocus(true);
    }

    @Test
    public void applicationIdShouldBeChanged() throws Exception {
        KeyUpEvent keyUpEvent = mock(KeyUpEvent.class);

        view.onApplicationIdChanged(keyUpEvent);

        verify(delegate).onValueChanged();
    }

}