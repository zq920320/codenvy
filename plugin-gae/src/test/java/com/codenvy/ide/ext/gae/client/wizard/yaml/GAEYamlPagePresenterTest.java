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

import org.eclipse.che.api.project.shared.dto.ImportProject;
import org.eclipse.che.api.project.shared.dto.NewProject;
import com.codenvy.ide.ext.gae.client.GAELocalizationConstant;
import com.codenvy.ide.ext.gae.client.service.GAEServiceClient;
import com.codenvy.ide.ext.gae.client.service.callbacks.GAEAsyncCallbackFactory;
import com.codenvy.ide.ext.gae.client.service.callbacks.GAEAsyncRequestCallback;
import com.codenvy.ide.ext.gae.client.service.callbacks.SuccessCallback;
import com.codenvy.ide.ext.gae.client.utils.GAEUtil;
import com.codenvy.ide.ext.gae.shared.YamlParameterInfo;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.CREATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.UPDATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar.PROJECT_PATH_KEY;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar.WIZARD_MODE_KEY;
import static org.eclipse.che.ide.api.wizard.Wizard.UpdateDelegate;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.APPLICATION_ID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GAEYamlPagePresenterTest {

    private static final String SOME_TEXT = "someText";

    @Captor
    private ArgumentCaptor<SuccessCallback<YamlParameterInfo>> successCallbackCaptor;

    //constructor mocks
    @Mock
    private GAEYamlPageView         view;
    @Mock
    private GAEServiceClient        service;
    @Mock
    private GAEAsyncCallbackFactory callBackFactory;
    @Mock
    private GAEUtil                 gaeUtil;
    @Mock
    private GAELocalizationConstant locale;
    @Mock
    private ImportProject           dataObject;
    @Mock
    private Map<String, String>     wizardContext;
    @Mock
    private UpdateDelegate          updateDelegate;

    //additional mocks
    @Mock
    private NewProject                                 newProject;
    @Mock
    private Map<String, List<String>>                  attributes;
    @Mock
    private List<String>                               appIdValues;
    @Mock
    private AcceptsOneWidget                           container;
    @Mock
    private GAEAsyncRequestCallback<YamlParameterInfo> yamlParameterInfoCallBack;
    @Mock
    private YamlParameterInfo                          yamlParameterInfo;

    @InjectMocks
    private GAEYamlPagePresenter presenter;

    @Before
    public void setUp() {
        setProjectCreatingMode();

        when(dataObject.getProject()).thenReturn(newProject);
        when(newProject.getAttributes()).thenReturn(attributes);
        when(locale.wizardApplicationIdDefault()).thenReturn(SOME_TEXT);

        presenter.setContext(wizardContext);
        presenter.setUpdateDelegate(updateDelegate);

        when(view.getGaeAppIdValue()).thenReturn(SOME_TEXT);
    }

    @Test
    public void constructorShouldBeDone() throws Exception {
        verify(view).setDelegate(presenter);
    }

    @Test
    public void defaultAppIdShouldBeSetToAttributeWhenCreatingProject() throws Exception {
        preparePresenter();

        verify(attributes).put(APPLICATION_ID, Arrays.asList(SOME_TEXT));
    }

    @Test
    public void existedAppIdShouldBeSetToAttributeWhenUpdatingProject() throws Exception {
        setProjectUpdatingMode();

        when(yamlParameterInfo.getApplicationId()).thenReturn(SOME_TEXT);
        when(callBackFactory.build(eq(YamlParameterInfo.class),
                                   Matchers.<SuccessCallback<YamlParameterInfo>>anyObject())).thenReturn(yamlParameterInfoCallBack);

        preparePresenter();

        verify(service).readGAEYamlParameters(SOME_TEXT, yamlParameterInfoCallBack);
        verify(callBackFactory).build(eq(YamlParameterInfo.class), successCallbackCaptor.capture());

        SuccessCallback<YamlParameterInfo> successCallback = successCallbackCaptor.getValue();

        successCallback.onSuccess(yamlParameterInfo);

        verify(attributes).put(APPLICATION_ID, Arrays.asList(SOME_TEXT));
    }

    @Test
    public void pageShouldBeCompletedWhenAppIdIsCorrect() throws Exception {
        prepareAttributesMock();
        preparePresenter();

        when(gaeUtil.isCorrectAppId(SOME_TEXT)).thenReturn(true);

        assertThat(presenter.isCompleted(), is(true));

        verify(gaeUtil).isCorrectAppId(SOME_TEXT);
    }

    @Test
    public void pageShouldNotBeCompletedWhenAppIdIsIncorrect() throws Exception {
        prepareAttributesMock();
        preparePresenter();

        when(gaeUtil.isCorrectAppId(SOME_TEXT)).thenReturn(false);

        assertThat(presenter.isCompleted(), is(false));

        verify(gaeUtil).isCorrectAppId(SOME_TEXT);
    }

    @Test
    public void incorrectIndicatorShouldNotBeShownWhenAppIdIsCorrectAndValueChanged() throws Exception {
        when(gaeUtil.isCorrectAppId(SOME_TEXT)).thenReturn(true);

        preparePresenter();

        presenter.onValueChanged();

        verify(view).showApplicationIdInCorrectIndicator(false);
        verify(updateDelegate).updateControls();
    }

    @Test
    public void incorrectIndicatorShouldBeShownWhenAppIdIsCorrectAndValueChanged() throws Exception {
        when(gaeUtil.isCorrectAppId(SOME_TEXT)).thenReturn(false);
        when(view.getGaeAppIdValue()).thenReturn(SOME_TEXT);

        preparePresenter();

        presenter.onValueChanged();

        verify(view).showApplicationIdInCorrectIndicator(true);
        verify(updateDelegate).updateControls();
    }

    @Test
    public void wizardShouldBeShown() throws Exception {
        prepareAttributesMock();
        preparePresenter();

        when(locale.wizardApplicationIdDefault()).thenReturn(SOME_TEXT);

        presenter.go(container);

        verify(container).setWidget(view);
        verify(view).setFocusToApplicationIdField();
        verify(view).setGaeApplicationId(SOME_TEXT);
        verify(view).showApplicationIdInCorrectIndicator(anyBoolean());

        verify(service, never()).readGAEYamlParameters(anyString(), Matchers.<GAEAsyncRequestCallback<YamlParameterInfo>>anyObject());
    }

    private void preparePresenter() {
        presenter.init(dataObject);
    }

    private void setProjectCreatingMode() {
        when(wizardContext.get(eq(WIZARD_MODE_KEY))).thenReturn(CREATE.toString());
    }

    private void setProjectUpdatingMode() {
        when(wizardContext.get(eq(WIZARD_MODE_KEY))).thenReturn(UPDATE.toString());
        when(wizardContext.get(eq(PROJECT_PATH_KEY))).thenReturn(SOME_TEXT);
    }

    private void prepareAttributesMock() throws Exception {
        when(appIdValues.get(0)).thenReturn(SOME_TEXT);
        when(attributes.get(APPLICATION_ID)).thenReturn(appIdValues);
    }
}