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
package com.codenvy.ide.ext.gae.client.update;

import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.project.shared.dto.BuildersDescriptor;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationManager;
import com.codenvy.ide.ext.gae.client.GAELocalizationConstant;
import com.codenvy.ide.ext.gae.client.GAEResources;
import com.codenvy.ide.ext.gae.client.confirm.ConfirmView;
import com.codenvy.ide.ext.gae.client.login.OAuthLoginPresenter;
import com.codenvy.ide.ext.gae.client.service.GAEServiceClient;
import com.codenvy.ide.ext.gae.client.service.callbacks.FailureCallback;
import com.codenvy.ide.ext.gae.client.service.callbacks.GAEAsyncCallbackFactory;
import com.codenvy.ide.ext.gae.client.service.callbacks.GAEAsyncRequestCallback;
import com.codenvy.ide.ext.gae.client.service.callbacks.SuccessCallback;
import com.codenvy.ide.ext.gae.client.utils.GAEUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.eclipse.che.ide.api.notification.Notification.Type;
import static org.eclipse.che.ide.api.notification.Notification.Type.INFO;
import static com.codenvy.ide.ext.gae.client.update.UpdateApplicationPresenter.ERROR_WEB_ENGINE;
import static com.codenvy.ide.ext.gae.client.update.UpdateApplicationPresenter.ERROR_YAML;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdateApplicationPresenterTest {

    private static final String SOME_TEXT = "someText";

    @Captor
    private ArgumentCaptor<SuccessCallback<Void>>       successCallbackValidateCaptor;
    @Captor
    private ArgumentCaptor<SuccessCallback<OAuthToken>> successCallbackLoginCaptor;
    @Captor
    private ArgumentCaptor<FailureCallback>             failureCallbackCaptor;
    @Captor
    private ArgumentCaptor<UpdateGAECallback>           updateGAECallbackCaptor;
    @Captor
    private ArgumentCaptor<Notification>                notificationCaptor;

    //constructor mocks
    @Mock
    private GAEServiceClient        service;
    @Mock
    private NotificationManager     notificationManager;
    @Mock
    private GAELocalizationConstant locale;
    @Mock
    private GAEUtil                 gaeUtil;
    @Mock
    private ConfirmView             view;
    @Mock
    private OAuthLoginPresenter     loginPresenter;
    @Mock
    private AppContext              context;
    @Mock
    private GAEAsyncCallbackFactory callbackFactory;
    @Mock
    private BuildAction             buildAction;
    @Mock
    private DeployAction            deployAction;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private GAEResources            gaeResources;

    //additional mocks
    @Mock
    private UpdateGAECallback                   buildCallBack;
    @Mock
    private UpdateGAECallback                   deployCallBack;
    @Mock
    private ProjectDescriptor                   activeProject;
    @Mock
    private CurrentProject                      currentProject;
    @Mock
    private GAEAsyncRequestCallback<Void>       validateCallBack;
    @Mock
    private OAuthToken                          oAuthToken;
    @Mock
    private GAEAsyncRequestCallback<OAuthToken> loginCallBack;
    @Mock
    private Throwable                           throwable;
    @Mock
    private BuildersDescriptor                  builderDescriptor;

    private UpdateApplicationPresenter presenter;

    @Before
    public void setUp() throws Exception {
        when(locale.updateButton()).thenReturn(SOME_TEXT);
        when(locale.deployApplicationSubtitle()).thenReturn(SOME_TEXT);
        when(locale.deployApplicationInstruction()).thenReturn(SOME_TEXT);
        when(gaeResources.gaeCSS().smallSubTitleLabel()).thenReturn(SOME_TEXT);

        when(context.getCurrentProject()).thenReturn(currentProject);
        when(currentProject.getProjectDescription()).thenReturn(activeProject);
        when(activeProject.getPath()).thenReturn(SOME_TEXT);
        when(activeProject.getName()).thenReturn(SOME_TEXT);
        when(activeProject.getBuilders()).thenReturn(builderDescriptor);

        presenter = new UpdateApplicationPresenter(deployAction,
                                                   buildAction,
                                                   notificationManager,
                                                   locale,
                                                   gaeUtil,
                                                   context,
                                                   callbackFactory,
                                                   service,
                                                   view,
                                                   gaeResources,
                                                   loginPresenter);
    }

    @Test
    public void constructorShouldBeVerified() throws Exception {
        verify(view).setDelegate(presenter);
        verify(loginPresenter).setLoginActionListener(presenter);

        verify(view).setActionButtonTitle(SOME_TEXT);
        verify(view).setSubtitle(SOME_TEXT);
        verify(view).setUserInstructions(SOME_TEXT);
        verify(view).addSubtitleStyleName(SOME_TEXT);

        verify(locale).updateButton();
        verify(locale).deployApplicationSubtitle();
        verify(locale).deployApplicationInstruction();
        verify(gaeResources.gaeCSS()).smallSubTitleLabel();
    }

    @Test
    public void dialogShouldNotBeShownWhenCurrentProjectIsNull() throws Exception {
        when(context.getCurrentProject()).thenReturn(null);

        presenter.showDialog();

        verify(currentProject, never()).getProjectDescription();
    }

    @Test
    public void loginPresenterWindowShouldBeShownWhenUserIsNotLogged() throws Exception {
        when(gaeUtil.isAuthenticatedInAppEngine(oAuthToken)).thenReturn(false);

        callSuccessMethodLoginCallBack();

        verify(loginPresenter).showDialog();
        verify(view, never()).show();
    }

    private void callSuccessMethodLoginCallBack() throws Exception {
        when(callbackFactory.build(eq(OAuthToken.class), Matchers.<SuccessCallback<OAuthToken>>anyObject())).thenReturn(loginCallBack);

        callSuccessMethodValidateCallBack();

        verify(service).getLoggedUser(loginCallBack);

        verify(callbackFactory).build(eq(OAuthToken.class), successCallbackLoginCaptor.capture());

        SuccessCallback<OAuthToken> successCallback = successCallbackLoginCaptor.getValue();

        successCallback.onSuccess(oAuthToken);
    }

    private void callSuccessMethodValidateCallBack() {
        when(callbackFactory.build(Matchers.<SuccessCallback<Void>>anyObject(),
                                   Matchers.<FailureCallback>anyObject())).thenReturn(validateCallBack);
        presenter.showDialog();

        verify(service).validateProject(SOME_TEXT, validateCallBack);

        verify(callbackFactory).build(successCallbackValidateCaptor.capture(), any(FailureCallback.class));

        SuccessCallback<Void> successCallback = successCallbackValidateCaptor.getValue();

        successCallback.onSuccess(null);
    }

    @Test
    public void deployViewShouldBeShownWhenUserIsLogged() throws Exception {
        when(gaeUtil.isAuthenticatedInAppEngine(oAuthToken)).thenReturn(true);

        callSuccessMethodLoginCallBack();

        verify(view).show();
        verify(loginPresenter, never()).showDialog();
    }

    @Test
    public void webEngineErrorShouldBeShown() throws Exception {
        when(throwable.getMessage()).thenReturn(ERROR_WEB_ENGINE);
        when(locale.errorValidateWebEngine()).thenReturn(SOME_TEXT);

        callFailureValidateMethod();

        verify(locale).errorValidateWebEngine();
        verify(notificationManager).showError(SOME_TEXT);

    }

    private void callFailureValidateMethod() throws Exception {
        presenter.showDialog();

        verify(callbackFactory).build(Matchers.<SuccessCallback<Void>>anyObject(), failureCallbackCaptor.capture());

        FailureCallback failureCallback = failureCallbackCaptor.getValue();

        failureCallback.onFailure(throwable);
    }

    @Test
    public void yamlErrorShouldBeShown() throws Exception {
        when(throwable.getMessage()).thenReturn(ERROR_YAML);
        when(locale.errorValidateYaml()).thenReturn(SOME_TEXT);

        callFailureValidateMethod();

        verify(notificationManager).showError(SOME_TEXT);
        verify(locale).errorValidateYaml();
    }

    @Test
    public void unknownErrorShouldBeShown() throws Exception {
        when(throwable.getMessage()).thenReturn(SOME_TEXT);

        callFailureValidateMethod();

        verify(notificationManager).showError(SOME_TEXT);
    }

    @Test
    public void deployWindowShouldBeShown() throws Exception {
        presenter.onLoginWindowHide();

        verify(view).show();
    }

    @Test
    public void dialogWindowShouldBeClosed() throws Exception {
        presenter.onCancelButtonClicked();

        verify(view).close();
    }

    @Test
    public void buildAndDeployShouldBeDoneWhenProjectHasBuilders() throws Exception {
        when(locale.buildStarted(SOME_TEXT)).thenReturn(SOME_TEXT);
        when(locale.deployStarted(SOME_TEXT)).thenReturn(SOME_TEXT);
        when(activeProject.getBuilders()).thenReturn(builderDescriptor);
        when(builderDescriptor.getDefault()).thenReturn(SOME_TEXT);

        presenter.showDialog();
        presenter.onActionButtonClicked();

        verify(notificationManager).showNotification(notificationCaptor.capture());

        Notification notification = notificationCaptor.getValue();

        assertNotification(notification, false, INFO, SOME_TEXT);

        verify(buildAction).perform(eq(activeProject), updateGAECallbackCaptor.capture());

        UpdateGAECallback updateGAECallback = updateGAECallbackCaptor.getValue();

        updateGAECallback.onSuccess(SOME_TEXT);

        verify(locale).deployStarted(SOME_TEXT);
        verify(deployAction).perform(eq(activeProject), eq(SOME_TEXT), any(UpdateGAECallback.class));
        verify(view).close();

        assertNotification(notification, false, INFO, SOME_TEXT);
    }

    private void assertNotification(Notification notification, boolean isFinished, Type type, String message) {
        assertThat(notification.isFinished(), is(isFinished));
        assertThat(notification.getType(), equalTo(type));
        assertThat(notification.getMessage(), equalTo(message));
    }

    @Test
    public void deployShouldNotBeStartedWhenBuildIsFailed() throws Exception {
        when(locale.buildStarted(SOME_TEXT)).thenReturn(SOME_TEXT);
        when(activeProject.getBuilders()).thenReturn(builderDescriptor);
        when(builderDescriptor.getDefault()).thenReturn(SOME_TEXT);

        presenter.showDialog();
        presenter.onActionButtonClicked();

        verify(notificationManager).showNotification(notificationCaptor.capture());

        Notification notification = notificationCaptor.getValue();

        assertNotification(notification, false, INFO, SOME_TEXT);

        verify(buildAction).perform(eq(activeProject), updateGAECallbackCaptor.capture());

        UpdateGAECallback updateGAECallback = updateGAECallbackCaptor.getValue();

        updateGAECallback.onFailure(SOME_TEXT);

        verify(view).close();
        verify(deployAction, never()).perform(any(ProjectDescriptor.class), anyString(), any(UpdateGAECallback.class));
    }

    @Test
    public void deployShouldBeDoneWithoutBuild() throws Exception {
        when(activeProject.getBuilders()).thenReturn(null);
        when(locale.deployStarted(SOME_TEXT)).thenReturn(SOME_TEXT);
        when(locale.deploySuccess(SOME_TEXT)).thenReturn(SOME_TEXT);

        presenter.showDialog();
        presenter.onActionButtonClicked();

        verify(notificationManager).showNotification(notificationCaptor.capture());

        Notification notification = notificationCaptor.getValue();

        assertNotification(notification, false, INFO, SOME_TEXT);

        verify(deployAction).perform(eq(activeProject), isNull(String.class), updateGAECallbackCaptor.capture());

        UpdateGAECallback updateGAECallback = updateGAECallbackCaptor.getValue();

        updateGAECallback.onSuccess(SOME_TEXT);

        verify(view).close();

        assertNotification(notification, true, INFO, SOME_TEXT);
    }

    @Test
    public void deployShouldBeFailed() throws Exception {
        when(activeProject.getBuilders()).thenReturn(null);
        when(locale.deployStarted(SOME_TEXT)).thenReturn(SOME_TEXT);
        when(locale.deployError(SOME_TEXT)).thenReturn(SOME_TEXT);

        presenter.showDialog();
        presenter.onActionButtonClicked();

        verify(notificationManager).showNotification(notificationCaptor.capture());

        Notification notification = notificationCaptor.getValue();

        assertNotification(notification, false, INFO, SOME_TEXT);

        verify(deployAction).perform(eq(activeProject), isNull(String.class), updateGAECallbackCaptor.capture());

        UpdateGAECallback updateGAECallback = updateGAECallbackCaptor.getValue();

        updateGAECallback.onFailure(SOME_TEXT);

        verify(view).close();
    }

}