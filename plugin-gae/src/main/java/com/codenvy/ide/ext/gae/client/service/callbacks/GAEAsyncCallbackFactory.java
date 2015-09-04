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
import com.codenvy.ide.ext.gae.client.GAELocalizationConstant;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.annotation.Nonnull;

/**
 * The factory that is used for creating callback for GAE extension.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 * @author Dmitry Shnurenko
 */
@Singleton
public class GAEAsyncCallbackFactory {

    private final NotificationManager     notificationManager;
    private final GAELocalizationConstant locale;
    private final DtoUnmarshallerFactory  dtoUnmarshallerFactory;

    @Inject
    public GAEAsyncCallbackFactory(NotificationManager notificationManager,
                                   GAELocalizationConstant locale,
                                   DtoUnmarshallerFactory dtoUnmarshallerFactory) {

        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.notificationManager = notificationManager;
        this.locale = locale;
    }

    /**
     * Returns instance of GAEAsyncRequestCallBack.
     *
     * @param successCallback
     *         callback which contains method which is called when operation is success
     * @return an instance {@link GAEAsyncRequestCallback}
     */
    public <T> GAEAsyncRequestCallback<T> build(@Nonnull SuccessCallback<T> successCallback) {
        return new GAEAsyncRequestCallback<>(notificationManager, locale, successCallback);
    }

    /**
     * Returns instance of GAEAsyncRequestCallBack.
     *
     * @param successCallback
     *         callback which contains method which is called when operation is success
     * @param failureCallback
     *         callback which contains method which is called when operation is fail
     * @return an instance {@link GAEAsyncRequestCallback}
     */
    public <T> GAEAsyncRequestCallback<T> build(@Nonnull SuccessCallback<T> successCallback, @Nonnull FailureCallback failureCallback) {
        return new GAEAsyncRequestCallback<>(notificationManager, locale, successCallback, failureCallback);
    }

    /**
     * Returns instance of GAEAsyncRequestCallBack using unmarshaller.
     *
     * @param unmarshallable
     *         unmarshaller which need to convert JSON object to JAVA object
     * @param successCallback
     *         callback which contains method which is called when operation is success
     * @return an instance {@link GAEAsyncRequestCallback}
     */
    public <T> GAEAsyncRequestCallback<T> build(@Nonnull Unmarshallable<T> unmarshallable, @Nonnull SuccessCallback<T> successCallback) {
        return new GAEAsyncRequestCallback<>(notificationManager, locale, unmarshallable, successCallback);
    }

    /**
     * Returns instance of GAEAsyncRequestCallBack using unmarshaller. Method contains creating of unmarshaller using object's class.
     *
     * @param clazz
     *         java class for which need to create unmarshaller
     * @param successCallback
     *         callback which contains method which is called when operation is success
     * @return an instance {@link GAEAsyncRequestCallback}
     */
    public <T> GAEAsyncRequestCallback<T> build(@Nonnull Class<T> clazz, @Nonnull SuccessCallback<T> successCallback) {
        Unmarshallable<T> unmarshallable = dtoUnmarshallerFactory.newUnmarshaller(clazz);

        return build(unmarshallable, successCallback);
    }

    /**
     * Returns instance of GAEAsyncRequestCallBack using unmarshaller. Method contains creating of unmarshaller using object's class.
     *
     * @param clazz
     *         java class for which need to create unmarshaller
     * @param successCallback
     *         callback which contains method which is called when operation is success
     * @param failureCallback
     *         callback which contains method which is called when operation is failure
     * @param <T>
     *         generic parameter
     * @return an instance {@link GAEAsyncRequestCallback}
     */
    public <T> GAEAsyncRequestCallback<T> build(@Nonnull Class<T> clazz,
                                                @Nonnull SuccessCallback<T> successCallback,
                                                @Nonnull FailureCallback failureCallback) {
        Unmarshallable<T> unmarshallable = dtoUnmarshallerFactory.newUnmarshaller(clazz);

        return new GAEAsyncRequestCallback<>(notificationManager, locale, unmarshallable, successCallback, failureCallback);
    }

}