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

import org.eclipse.che.api.project.shared.dto.ImportProject;
import org.eclipse.che.api.project.shared.dto.NewProject;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.api.wizard.Wizard;
import com.codenvy.ide.ext.gae.client.GAELocalizationConstant;
import com.codenvy.ide.ext.gae.client.service.GAEServiceClient;
import com.codenvy.ide.ext.gae.client.service.callbacks.GAEAsyncCallbackFactory;
import com.codenvy.ide.ext.gae.client.service.callbacks.GAEAsyncRequestCallback;
import com.codenvy.ide.ext.gae.client.service.callbacks.SuccessCallback;
import com.codenvy.ide.ext.gae.client.utils.GAEUtil;
import com.codenvy.ide.ext.gae.shared.GAEMavenInfo;
import org.eclipse.che.ide.rest.Unmarshallable;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.CREATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.UPDATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar.PROJECT_PATH_KEY;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar.WIZARD_MODE_KEY;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.APPLICATION_ID;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.ARTIFACT_ID;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.GROUP_ID;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.VERSION;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@RunWith(DataProviderRunner.class)
public class GAEMavenPagePresenterTest {

    private static final String SOME_TEXT = "sometext";

    @Captor
    private ArgumentCaptor<SuccessCallback<GAEMavenInfo>> successCallbackCaptor;

    @Mock
    private GAEJavaPageView                       view;
    @Mock
    private AcceptsOneWidget                      container;
    @Mock
    private GAELocalizationConstant               locale;
    @Mock
    private ImportProject                         dataObject;
    @Mock
    private GAEUtil                               gaeUtil;
    @Mock
    private ProjectDescriptor                     projectDescriptor;
    @Mock
    private Wizard.UpdateDelegate                 updateDelegate;
    @Mock
    private Map<String, String>                   wizardContext;
    @Mock
    private Map<String, List<String>>             attributes;
    @Mock
    private NewProject                            newProject;
    @Mock
    private GAEServiceClient                      serviceClient;
    @Mock
    private GAEAsyncCallbackFactory               callbackFactory;
    @Mock
    private Unmarshallable<GAEMavenInfo>          stringUnmarshaller;
    @Mock
    private GAEAsyncRequestCallback<GAEMavenInfo> asyncRequestCallback;
    @Mock
    private GAEMavenInfo                          gaeMavenInfo;
    @Mock
    private List<String>                          appIdValues;

    @InjectMocks
    private GAEJavaPagePresenter presenter;

    @DataProvider
    public static Object[][] checkIsCorrectAppId() {
        return new Object[][]{
                {true, "sometext"},
                {true, "some-text"},
                {true, "some-text-538"},
                {false, "someText"},
                {false, "a"},
                {false, "some*"},
                {false, "some/"},
                {false, "text-"},
                {false, "some("},
                {false, "3some"},
                {false, "SomeText"}
        };
    }

    @DataProvider
    public static Object[][] checkIsCompletedMethod() {
        return new Object[][]{
                {"", SOME_TEXT, SOME_TEXT, SOME_TEXT, false},
                {SOME_TEXT, "", SOME_TEXT, SOME_TEXT, false},
                {SOME_TEXT, SOME_TEXT, "", SOME_TEXT, false},
                {SOME_TEXT, SOME_TEXT, SOME_TEXT, "", false},
                {SOME_TEXT, SOME_TEXT, SOME_TEXT, SOME_TEXT, true}
        };
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        setProjectCreatingMode();

        when(dataObject.getProject()).thenReturn(newProject);
        when(newProject.getAttributes()).thenReturn(attributes);
        when(locale.wizardApplicationIdDefault()).thenReturn(SOME_TEXT);
        when(locale.wizardDefaultAppVersion()).thenReturn(SOME_TEXT);

        presenter.setContext(wizardContext);
        presenter.setUpdateDelegate(updateDelegate);
    }

    @Test
    public void defaultValuesShouldBeSetToAttributeWhenCreatingProject() throws Exception {
        preparePresenter();

        verify(attributes).put(VERSION, Arrays.asList(SOME_TEXT));
        verify(attributes).put(APPLICATION_ID, Arrays.asList(SOME_TEXT));
    }

    @Test
    public void existedAppIdShouldBeSetToAttributeWhenUpdatingProject() throws Exception {
        setProjectUpdatingMode();

        when(gaeMavenInfo.getArtifactId()).thenReturn(SOME_TEXT);
        when(gaeMavenInfo.getGroupId()).thenReturn(SOME_TEXT);
        when(gaeMavenInfo.getVersion()).thenReturn(SOME_TEXT);
        when(gaeMavenInfo.getApplicationId()).thenReturn(SOME_TEXT);
        when(callbackFactory.build(eq(GAEMavenInfo.class),
                                   Matchers.<SuccessCallback<GAEMavenInfo>>anyObject())).thenReturn(asyncRequestCallback);

        preparePresenter();

        verify(serviceClient).readGAEMavenParameters(SOME_TEXT, asyncRequestCallback);
        verify(callbackFactory).build(eq(GAEMavenInfo.class), successCallbackCaptor.capture());

        SuccessCallback<GAEMavenInfo> successCallback = successCallbackCaptor.getValue();

        successCallback.onSuccess(gaeMavenInfo);

        verify(attributes).put(ARTIFACT_ID, Arrays.asList(SOME_TEXT));
        verify(attributes).put(GROUP_ID, Arrays.asList(SOME_TEXT));
        verify(attributes).put(VERSION, Arrays.asList(SOME_TEXT));
        verify(attributes).put(APPLICATION_ID, Arrays.asList(SOME_TEXT));
    }

    @Test
    @UseDataProvider("checkIsCorrectAppId")
    public void applicationIdShouldBeChecked(boolean isCorrect, String appId) {
        when(gaeUtil.isCorrectAppId(appId)).thenReturn(isCorrect);
        prepareAttributesMock(SOME_TEXT, SOME_TEXT, SOME_TEXT, appId);
        preparePresenter();

        assertThat(presenter.isCompleted(), is(isCorrect));
    }

    @Test
    public void constructorShouldBeVerified() throws Exception {
        verify(view).setDelegate(presenter);
    }

    @Test
    @UseDataProvider("checkIsCompletedMethod")
    public void pageCompletedShouldBeCorrected(String artifactId, String groupId, String version, String applicationId, boolean expected) {
        prepareAttributesMock(artifactId, groupId, version, applicationId);
        preparePresenter();
        when(gaeUtil.isCorrectAppId(SOME_TEXT)).thenReturn(true);

        assertThat(presenter.isCompleted(), is(expected));
    }

    @Test
    public void inCorrectIndicatorsShouldBeSet() throws Exception {
        prepareViewMocks("", "", "", "");
        preparePresenter();

        presenter.onValueChanged();

        verifyOnValueChanged(true);

        verify(updateDelegate).updateControls();
    }

    private void verifyOnValueChanged(boolean isCorrect) {
        verify(view).showGroupIdInCorrectIndicator(isCorrect);
        verify(view).showArtifactIdInCorrectIndicator(isCorrect);
        verify(view).showVersionInCorrectIndicator(isCorrect);
        verify(view).showApplicationIdInCorrectIndicator(isCorrect);
    }

    @Test
    public void inCorrectIndicatorShouldNotBeSet() throws Exception {
        prepareViewMocks(SOME_TEXT, SOME_TEXT, SOME_TEXT, SOME_TEXT);
        preparePresenter();
        when(gaeUtil.isCorrectAppId(SOME_TEXT)).thenReturn(true);

        presenter.onValueChanged();

        verifyOnValueChanged(false);

        verify(updateDelegate).updateControls();
    }

    @Test
    public void pageShouldBeShown() throws Exception {
        prepareAttributesMock(SOME_TEXT, SOME_TEXT, SOME_TEXT, SOME_TEXT);
        prepareViewMocks(SOME_TEXT, SOME_TEXT, SOME_TEXT, SOME_TEXT);
        preparePresenter();
        when(newProject.getName()).thenReturn(SOME_TEXT);
        when(gaeUtil.isCorrectAppId(SOME_TEXT)).thenReturn(true);

        presenter.go(container);

        verify(container).setWidget(view);
        verify(view).setFocusToApplicationIdField();

        verify(view).setArtifactIdValue(SOME_TEXT);
        verify(view).setGroupIdValue(SOME_TEXT);
        verify(view).setVersion(SOME_TEXT);
        verify(view).setGaeApplicationId(SOME_TEXT);

        verifyOnValueChanged(false);
    }

    @Test
    public void projectNameShouldBeUsedAsArtifactIdAndGroupId() throws Exception {
        prepareAttributesMock("", "", SOME_TEXT, SOME_TEXT);
        prepareViewMocks(SOME_TEXT, SOME_TEXT, SOME_TEXT, SOME_TEXT);
        preparePresenter();
        final String projectName = "projectName";
        when(newProject.getName()).thenReturn(projectName);

        presenter.go(container);

        verify(container).setWidget(view);
        verify(view).setFocusToApplicationIdField();

        verify(attributes).put(ARTIFACT_ID, Arrays.asList(projectName));
        verify(attributes).put(GROUP_ID, Arrays.asList(projectName));

        verify(updateDelegate).updateControls();
    }

    private void prepareViewMocks(String artifactId, String groupId, String version, String applicationId) {
        when(view.getArtifactIdValue()).thenReturn(artifactId);
        when(view.getGroupIdValue()).thenReturn(groupId);
        when(view.getVersionValue()).thenReturn(version);
        when(view.getGaeAppIdValue()).thenReturn(applicationId);
    }

    private void prepareAttributesMock(String artifactId, String groupId, String version, String applicationId) {
        List<String> artifactIdValues = mock(List.class);
        when(artifactIdValues.get(0)).thenReturn(artifactId);
        when(attributes.get(ARTIFACT_ID)).thenReturn(artifactIdValues);

        List<String> groupIdValues = mock(List.class);
        when(groupIdValues.get(0)).thenReturn(groupId);
        when(attributes.get(GROUP_ID)).thenReturn(groupIdValues);

        List<String> versionValues = mock(List.class);
        when(versionValues.get(0)).thenReturn(version);
        when(attributes.get(VERSION)).thenReturn(versionValues);

        List<String> appIdValues = mock(List.class);
        when(appIdValues.get(0)).thenReturn(applicationId);
        when(attributes.get(APPLICATION_ID)).thenReturn(appIdValues);
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
}