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
import org.eclipse.che.ide.commons.exception.ServerException;
import org.eclipse.che.ide.commons.exception.UnauthorizedException;
import com.codenvy.ide.ext.gae.client.GAELocalizationConstant;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.codenvy.ide.ext.gae.TestUtil.invokeOnFailureCallbackMethod;
import static com.codenvy.ide.ext.gae.TestUtil.invokeOnSuccessCallbackMethod;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Plotnikov
 */
@RunWith(MockitoJUnitRunner.class)
public class GAEAsyncRequestCallbackTest {

    private static final String SOME_TEXT = "some text";

    @Mock
    private NotificationManager     notificationManager;
    @Mock
    private GAELocalizationConstant locale;
    @Mock
    private SuccessCallback<String> successCallback;
    @Mock
    private FailureCallback         failureCallback;

    @Test
    public void successCallbackShouldBeExecuted() throws Exception {
        GAEAsyncRequestCallback<String> callback = new GAEAsyncRequestCallback<>(notificationManager, locale, successCallback);

        invokeOnSuccessCallbackMethod(callback.getClass(), callback, SOME_TEXT);

        verify(successCallback).onSuccess(SOME_TEXT);

        verifyNoMoreInteractions(notificationManager, locale, successCallback, failureCallback);
    }

    @Test
    public void successCallbackShouldBeExecutedWhenUnmarshallerIsGiven() throws Exception {
        GAEAsyncRequestCallback<String> callback = new GAEAsyncRequestCallback<>(notificationManager, locale, null, successCallback);

        invokeOnSuccessCallbackMethod(callback.getClass(), callback, SOME_TEXT);

        verify(successCallback).onSuccess(SOME_TEXT);

        verifyNoMoreInteractions(notificationManager, locale, successCallback, failureCallback);
    }

    @Test
    public void failureCallbackShouldBeExecutedWhenFailureCallbackIsExist() throws Exception {
        Throwable throwable = mock(Throwable.class);
        GAEAsyncRequestCallback<String> callback =
                new GAEAsyncRequestCallback<>(notificationManager, locale, successCallback, failureCallback);

        invokeOnFailureCallbackMethod(callback.getClass(), callback, throwable);

        verify(failureCallback).onFailure(throwable);

        verifyNoMoreInteractions(notificationManager, locale, successCallback, failureCallback);
    }

    @Test
    public void failureCallbackShouldBeExecutedWhenUnauthorizedExceptionHappened() throws Exception {
        UnauthorizedException unauthorizedException = mock(UnauthorizedException.class);
        GAEAsyncRequestCallback<String> callback = new GAEAsyncRequestCallback<>(notificationManager, locale, successCallback);

        when(locale.authorizationError()).thenReturn(SOME_TEXT);

        invokeOnFailureCallbackMethod(callback.getClass(), callback, unauthorizedException);

        verify(notificationManager).showError(SOME_TEXT);
        verify(locale).authorizationError();

        verifyNoMoreInteractions(notificationManager, locale, successCallback, failureCallback);
    }

    @Test
    public void failureCallbackShouldBeExecutedWhenServerExceptionHappened() throws Exception {
        ServerException serverException = mock(ServerException.class);
        when(serverException.getMessage()).thenReturn(SOME_TEXT);

        GAEAsyncRequestCallback<String> callback = new GAEAsyncRequestCallback<>(notificationManager, locale, successCallback);

        invokeOnFailureCallbackMethod(callback.getClass(), callback, serverException);

        verify(notificationManager).showError(SOME_TEXT);

        verifyNoMoreInteractions(notificationManager, locale, successCallback, failureCallback);
    }

    @Test
    public void failureCallbackShouldBeExecutedWhenExceptionMessageIsEmpty() throws Exception {
        Exception exception = mock(Exception.class, RETURNS_MOCKS);

        GAEAsyncRequestCallback<String> callback = new GAEAsyncRequestCallback<>(notificationManager, locale, successCallback);

        when(locale.unknownErrorMessage()).thenReturn(SOME_TEXT);

        invokeOnFailureCallbackMethod(callback.getClass(), callback, exception);

        verify(notificationManager).showError(SOME_TEXT);
        verify(locale).unknownErrorMessage();

        verifyNoMoreInteractions(notificationManager, locale, successCallback, failureCallback);
    }

    @Test
    public void failureCallbackShouldBeExecutedWhenNotServerExceptionHappened() throws Exception {
        Exception exception = mock(Exception.class);
        when(exception.getMessage()).thenReturn(SOME_TEXT);

        GAEAsyncRequestCallback<String> callback = new GAEAsyncRequestCallback<>(notificationManager, locale, successCallback);

        when(locale.unknownErrorMessage()).thenReturn(SOME_TEXT);

        invokeOnFailureCallbackMethod(callback.getClass(), callback, exception);

        verify(notificationManager).showError(SOME_TEXT);
        verify(locale).unknownErrorMessage();

        verifyNoMoreInteractions(notificationManager, locale, successCallback, failureCallback);
    }

}