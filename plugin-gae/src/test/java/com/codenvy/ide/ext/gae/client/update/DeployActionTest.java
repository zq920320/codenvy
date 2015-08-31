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

import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.ext.gae.client.create.CreateApplicationPresenter;
import com.codenvy.ide.ext.gae.client.service.GAEServiceClient;
import com.codenvy.ide.ext.gae.client.service.callbacks.FailureCallback;
import com.codenvy.ide.ext.gae.client.service.callbacks.GAERequestCallBackFactory;
import com.codenvy.ide.ext.gae.client.service.callbacks.GAERequestCallback;
import com.codenvy.ide.ext.gae.client.service.callbacks.SuccessCallback;
import com.codenvy.ide.ext.gae.shared.ApplicationInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.codenvy.ide.ext.gae.client.update.DeployAction.APP_NOT_EXIST;
import static com.codenvy.ide.ext.gae.client.update.DeployAction.DO_NOT_HAVE_PERMISSION;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class DeployActionTest {

    private static final String SOME_TEXT = "someText";
    private static final String LINK      = "<a href='someText' target='_blank'>someText</a>";

    @Captor
    private ArgumentCaptor<SuccessCallback<ApplicationInfo>> successCallbackCaptor;
    @Captor
    private ArgumentCaptor<FailureCallback>                  failureCallbackCaptor;

    //constructor mocks
    @Mock
    private GAERequestCallBackFactory  requestCallBackFactory;
    @Mock
    private CreateApplicationPresenter createApplicationPresenter;
    @Mock
    private GAEServiceClient           service;

    //additional mocks
    @Mock
    private ProjectDescriptor                   activeProject;
    @Mock
    private UpdateGAECallback                   updateGAECallback;
    @Mock
    private ApplicationInfo                     applicationInfo;
    @Mock
    private GAERequestCallback<ApplicationInfo> gaeRequestCallback;
    @Mock
    private Throwable                           throwable;

    @InjectMocks
    private DeployAction deployAction;

    @Before
    public void setUp() {
        when(activeProject.getPath()).thenReturn(SOME_TEXT);
    }

    @Test
    public void deployShouldBeSuccess() throws Exception {
        when(applicationInfo.getWebURL()).thenReturn(SOME_TEXT);
        when(requestCallBackFactory.build(eq(ApplicationInfo.class),
                                          Matchers.<SuccessCallback<ApplicationInfo>>anyObject(),
                                          Matchers.<FailureCallback>anyObject())).thenReturn(gaeRequestCallback);

        deployAction.perform(activeProject, SOME_TEXT, updateGAECallback);

        verify(service).update(SOME_TEXT, SOME_TEXT, gaeRequestCallback);

        verify(requestCallBackFactory).build(eq(ApplicationInfo.class), successCallbackCaptor.capture(), any(FailureCallback.class));

        SuccessCallback<ApplicationInfo> successCallback = successCallbackCaptor.getValue();

        successCallback.onSuccess(applicationInfo);

        verify(updateGAECallback).onSuccess(LINK);
    }

    @Test
    public void createApplicationWindowShouldBeShownWhenAppNotExist() throws Exception {
        when(throwable.getMessage()).thenReturn(APP_NOT_EXIST);

        callFailureMethod();

        verify(createApplicationPresenter).showDialog();
    }

    private void callFailureMethod() throws Exception {
        deployAction.perform(activeProject, SOME_TEXT, updateGAECallback);

        verify(requestCallBackFactory).build(eq(ApplicationInfo.class),
                                             Matchers.<SuccessCallback<ApplicationInfo>>anyObject(),
                                             failureCallbackCaptor.capture());

        FailureCallback failureCallback = failureCallbackCaptor.getValue();
        failureCallback.onFailure(throwable);
    }

    @Test
    public void createApplicationWindowShouldBeShownWhenDoNotHavePermission() throws Exception {
        when(throwable.getMessage()).thenReturn(DO_NOT_HAVE_PERMISSION);

        callFailureMethod();

        verify(createApplicationPresenter).showDialog();
    }

    @Test
    public void createApplicationWindowShouldNotBeShownWhenUnknownExceptionCame() throws Exception {
        when(throwable.getMessage()).thenReturn(SOME_TEXT);

        callFailureMethod();

        verify(updateGAECallback).onFailure(SOME_TEXT);
        verify(createApplicationPresenter, never()).showDialog();
    }
}