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

import com.codenvy.ide.ext.gae.client.GAELocalizationConstant;
import com.codenvy.ide.ext.gae.client.GAEResources;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static com.codenvy.ide.ext.gae.client.GAEResources.EditorCSS;
import static com.codenvy.ide.ext.gae.client.wizard.java.GAEJavaPageView.ActionDelegate;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class GAEMavenPageViewImplTest {

    private static final String SOME_TEXT = "someText";

    @Mock
    private GAELocalizationConstant locale;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private GAEResources            resources;
    @Mock
    private KeyUpEvent              keyUpEvent;
    @Mock
    private EditorCSS               editorCSS;
    @Mock
    private ActionDelegate          delegate;
    @InjectMocks
    private GAEJavaPageViewImpl     view;

    @Before
    public void setUp() throws Exception {
        view.setDelegate(delegate);
    }

    @Test
    public void groupIdValueShouldBeGot() throws Exception {
        view.getGroupIdValue();

        verify(view.groupId).getText();
    }

    @Test
    public void groupIdShouldBeSet() throws Exception {
        view.setGroupIdValue(SOME_TEXT);

        verify(view.groupId).setText(SOME_TEXT);
    }

    @Test
    public void artifactIdValueShouldBeGot() throws Exception {
        view.getArtifactIdValue();

        verify(view.artifactId).getText();
    }

    @Test
    public void artifactIdValueShouldBeSet() throws Exception {
        view.setArtifactIdValue(SOME_TEXT);

        verify(view.artifactId).setText(SOME_TEXT);
    }

    @Test
    public void versionValueShouldBeGot() throws Exception {
        view.getVersionValue();

        verify(view.version).getText();
    }

    @Test
    public void versionValueShouldBeSet() throws Exception {
        view.setVersion(SOME_TEXT);

        verify(view.version).setText(SOME_TEXT);
    }

    @Test
    public void gaeAppIdValueShouldBeGot() throws Exception {
        view.getGaeAppIdValue();

        verify(view.gaeAppId).getText();
    }

    @Test
    public void setGaeAppIdVersion() throws Exception {
        view.setGaeApplicationId(SOME_TEXT);

        verify(view.gaeAppId).setText(SOME_TEXT);
    }

    @Test
    public void incorrectGroupIdIndicatorShouldBeShown() throws Exception {
        when(resources.gaeCSS().wizardIncorrectValueBorder()).thenReturn(SOME_TEXT);

        view.showGroupIdInCorrectIndicator(true);

        verify(view.groupId).addStyleName(SOME_TEXT);
        verify(view.groupId, never()).removeStyleName(anyString());
    }

    @Test
    public void incorrectGroupIdIndicatorShouldBeHidden() throws Exception {
        when(resources.gaeCSS().wizardIncorrectValueBorder()).thenReturn(SOME_TEXT);

        view.showGroupIdInCorrectIndicator(false);

        verify(view.groupId).removeStyleName(SOME_TEXT);
        verify(view.groupId, never()).addStyleName(anyString());
    }

    @Test
    public void incorrectArtifactIdIndicatorShouldBeShown() throws Exception {
        when(resources.gaeCSS().wizardIncorrectValueBorder()).thenReturn(SOME_TEXT);

        view.showArtifactIdInCorrectIndicator(true);

        verify(view.artifactId).addStyleName(SOME_TEXT);
        verify(view.artifactId, never()).removeStyleName(anyString());
    }

    @Test
    public void incorrectArtifactIdIndicatorShouldBeHidden() throws Exception {
        when(resources.gaeCSS().wizardIncorrectValueBorder()).thenReturn(SOME_TEXT);

        view.showArtifactIdInCorrectIndicator(false);

        verify(view.artifactId).removeStyleName(SOME_TEXT);
        verify(view.artifactId, never()).addStyleName(anyString());
    }

    @Test
    public void incorrectVersionIndicatorShouldBeShown() throws Exception {
        when(resources.gaeCSS().wizardIncorrectValueBorder()).thenReturn(SOME_TEXT);

        view.showVersionInCorrectIndicator(true);

        verify(view.version).addStyleName(SOME_TEXT);
        verify(view.version, never()).removeStyleName(anyString());
    }

    @Test
    public void incorrectVersionIndicatorShouldBeHidden() throws Exception {
        when(resources.gaeCSS().wizardIncorrectValueBorder()).thenReturn(SOME_TEXT);

        view.showVersionInCorrectIndicator(false);

        verify(view.version).removeStyleName(SOME_TEXT);
        verify(view.version, never()).addStyleName(anyString());
    }

    @Test
    public void incorrectGaeAppIdIndicatorShouldBeShown() throws Exception {
        when(resources.gaeCSS().wizardIncorrectValueBorder()).thenReturn(SOME_TEXT);

        view.showApplicationIdInCorrectIndicator(true);

        verify(view.gaeAppId).addStyleName(SOME_TEXT);
        verify(view.gaeAppId, never()).removeStyleName(anyString());
    }

    @Test
    public void incorrectGaeAppIdIndicatorShouldBeHidden() throws Exception {
        when(resources.gaeCSS().wizardIncorrectValueBorder()).thenReturn(SOME_TEXT);

        view.showApplicationIdInCorrectIndicator(false);

        verify(view.gaeAppId).removeStyleName(SOME_TEXT);
        verify(view.gaeAppId, never()).addStyleName(anyString());
    }

    @Test
    public void focusShouldBeSet() throws Exception {
        view.setFocusToApplicationIdField();

        verify(view.gaeAppId).setFocus(true);
    }

    @Test
    public void groupIdParameterShouldBeChanged() throws Exception {
        view.onGroupIdParameterChanged(keyUpEvent);

        verify(delegate).onValueChanged();
    }

    @Test
    public void artifactIdParameterShouldBeChanged() throws Exception {
        view.onArtifactIdValueChanged(keyUpEvent);

        verify(delegate).onValueChanged();
    }

    @Test
    public void versionParameterShouldBeChanged() throws Exception {
        view.onVersionChanged(keyUpEvent);

        verify(delegate).onValueChanged();
    }

    @Test
    public void gaeApplicationIdParameterShouldBeChanged() throws Exception {
        view.onApplicationIdChanged(keyUpEvent);

        verify(delegate).onValueChanged();
    }

}