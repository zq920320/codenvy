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
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.websocket.rest.Unmarshallable;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * The factory that is used for creating callback for GAE extension.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class GAERequestCallBackFactory {

    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final NotificationManager    notificationManager;

    @Inject
    public GAERequestCallBackFactory(DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                     NotificationManager notificationManager) {

        this.notificationManager = notificationManager;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    }

    /**
     * Returns instance of GAERequestCallBack.
     *
     * @param unmarshallable
     *         unmarshaller which need to convert JSON object to JAVA object
     * @return an instance {@link GAERequestCallback}
     */
    public <T> GAERequestCallback<T> build(@NotNull Unmarshallable<T> unmarshallable, @NotNull SuccessCallback<T> successCallback) {
        return new GAERequestCallback<>(notificationManager, unmarshallable, successCallback);
    }

    /**
     * Returns instance of GAERequestCallBack using unmarshaller. Method contains creating of unmarshaller using object's class.
     *
     * @param clazz
     *         java class for which need to create unmarshaller
     * @param successCallback
     *         callback which contains method which is called when operation is success
     * @return an instance {@link GAERequestCallback}
     */
    public <T> GAERequestCallback<T> build(@NotNull Class<T> clazz, @NotNull SuccessCallback<T> successCallback) {
        Unmarshallable<T> unmarshallable = dtoUnmarshallerFactory.newWSUnmarshaller(clazz);

        return new GAERequestCallback<>(notificationManager, unmarshallable, successCallback);
    }

    /**
     * Returns instance of GAERequestCallBack using unmarshaller.
     *
     * @param notificationManager
     *         shows different messages to user
     * @param unmarshallable
     *         unmarshaller which need to convert JSON object to JAVA object
     * @param successCallback
     *         callback which contains method which is called when operation is success
     * @param failureCallback
     *         callback which contains method which is called when operation is fail
     * @return an instance {@link GAERequestCallback}
     */
    public <T> GAERequestCallback build(@NotNull NotificationManager notificationManager,
                                        @Nullable Unmarshallable<T> unmarshallable,
                                        @NotNull SuccessCallback<T> successCallback,
                                        @Nullable FailureCallback failureCallback) {

        return new GAERequestCallback<>(notificationManager, unmarshallable, successCallback, failureCallback);
    }

    /**
     * Returns instance of GAERequestCallBack using unmarshaller. Method contains creating of unmarshaller using object's class.
     *
     * @param clazz
     *         java class for which need to create unmarshaller
     * @param successCallback
     *         callback which contains method which is called when operation is success
     * @param failureCallback
     *         callback which contains method which is called when operation is failed
     * @return an instance {@link GAERequestCallback}
     */
    public <T> GAERequestCallback<T> build(@NotNull Class<T> clazz,
                                           @NotNull SuccessCallback<T> successCallback,
                                           @NotNull FailureCallback failureCallback) {
        Unmarshallable<T> unmarshallable = dtoUnmarshallerFactory.newWSUnmarshaller(clazz);

        return new GAERequestCallback<>(notificationManager, unmarshallable, successCallback, failureCallback);
    }

}
