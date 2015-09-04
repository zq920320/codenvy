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
package com.codenvy.ide.ext.gae.client.service.callbacks;

import org.eclipse.che.ide.api.notification.NotificationManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Plotnikov
 */
@RunWith(MockitoJUnitRunner.class)
public class GAERequestCallbackTest {

    private static final String SOME_TEXT = "some text";

    @Mock
    private FailureCallback         failureCallback;
    @Mock
    private SuccessCallback<String> successCallback;
    @Mock
    private NotificationManager     notificationManager;
    @Mock
    private Throwable               throwable;

    @Test
    public void successCallbackShouldBeExecuted() throws Exception {
        GAERequestCallback<String> callback = new GAERequestCallback<>(notificationManager, null, successCallback);

        callback.onSuccess(SOME_TEXT);

        verify(successCallback).onSuccess(SOME_TEXT);

        verifyNoMoreInteractions(notificationManager, successCallback, failureCallback);
    }

    @Test
    public void failureCallbackShouldBeExecutedWhenFailureCallbackIsExist() throws Exception {
        GAERequestCallback<String> callback = new GAERequestCallback<>(notificationManager, null, successCallback, failureCallback);

        callback.onFailure(throwable);

        verify(failureCallback).onFailure(throwable);

        verifyNoMoreInteractions(notificationManager, successCallback, failureCallback);
    }

    @Test
    public void failureCallbackShouldBeExecutedWhenFailureCallbackIsNotExist() throws Exception {
        when(throwable.getMessage()).thenReturn(SOME_TEXT);
        GAERequestCallback<String> callback = new GAERequestCallback<>(notificationManager, null, successCallback);

        callback.onFailure(throwable);

        verify(notificationManager).showError(SOME_TEXT);

        verifyNoMoreInteractions(notificationManager, successCallback, failureCallback);
    }

}