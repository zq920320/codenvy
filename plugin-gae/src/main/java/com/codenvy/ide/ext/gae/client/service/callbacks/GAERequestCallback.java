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
import org.eclipse.che.ide.websocket.rest.RequestCallback;
import org.eclipse.che.ide.websocket.rest.Unmarshallable;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Class to receive a response from a remote procedure call.
 *
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 */
public class GAERequestCallback<T> extends RequestCallback<T> {

    private FailureCallback     failureCallback;
    private SuccessCallback<T>  successCallback;
    private NotificationManager notificationManager;

    public GAERequestCallback(@NotNull NotificationManager notificationManager,
                              @Nullable Unmarshallable<T> unmarshallable,
                              @NotNull SuccessCallback<T> successCallback,
                              @Nullable FailureCallback failureCallback) {

        this(notificationManager, unmarshallable, successCallback);
        this.notificationManager = notificationManager;
        this.successCallback = successCallback;
        this.failureCallback = failureCallback;
    }

    public GAERequestCallback(@NotNull NotificationManager notificationManager,
                              @Nullable Unmarshallable<T> unmarshallable,
                              @NotNull SuccessCallback<T> successCallback) {

        this(unmarshallable, successCallback);
        this.notificationManager = notificationManager;
        this.successCallback = successCallback;
    }

    public GAERequestCallback(@Nullable Unmarshallable<T> unmarshallable, @NotNull SuccessCallback<T> successCallback) {

        super(unmarshallable);
        this.successCallback = successCallback;
    }

    /** {@inheritDoc} */
    @Override
    public void onSuccess(T result) {
        successCallback.onSuccess(result);
    }

    /** {@inheritDoc} */
    @Override
    public void onFailure(Throwable exception) {
        if (failureCallback != null) {
            failureCallback.onFailure(exception);
            return;
        }

        notificationManager.showError(exception.getMessage());
    }
}
