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
package com.codenvy.ide.ext.gae;

import org.eclipse.che.api.vfs.server.ContentStream;
import com.google.gwt.http.client.RequestCallback;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * The class contains methods, which allow us to invoke methods of {@link RequestCallback}.
 *
 * @author Dmitry Shnurenko
 */
public class TestUtil {

    private static final String ON_SUCCESS_METHOD = "onSuccess";
    private static final String ON_FAILURE_METHOD = "onFailure";

    /**
     * Methods contains logic which invokes success method using reflection. Method can throw exception.
     *
     * @param clazz
     *         class from which we need invoke method
     * @param callback
     *         object which contains method which need call
     * @param <T>
     *         generic type of callback object
     * @param parameters
     *         method input parameters
     * @throws Exception
     */
    public static <T extends RequestCallback> void invokeOnSuccessCallbackMethod(@Nonnull Class<?> clazz,
                                                                                 @Nonnull T callback,
                                                                                 @Nonnull Object... parameters) throws Exception {

        //noinspection NonJREEmulationClassesInClientCode
        Method onSuccess = clazz.getDeclaredMethod(ON_SUCCESS_METHOD, Object.class);
        onSuccess.setAccessible(true);
        onSuccess.invoke(callback, parameters);
    }

    /**
     * Methods contains logic which invokes failure method using reflection. Method can throw exception.
     *
     * @param clazz
     *         class from which we need invoke method
     * @param callback
     *         object which contains method which need call
     * @param throwable
     *         method input parameter
     * @param <T>
     *         generic type of callback object
     * @throws Exception
     */
    public static <T extends RequestCallback> void invokeOnFailureCallbackMethod(@Nonnull Class<?> clazz,
                                                                                 @Nonnull T callback,
                                                                                 @Nonnull Throwable throwable) throws Exception {
        //noinspection NonJREEmulationClassesInClientCode
        Method onFailure = clazz.getDeclaredMethod(ON_FAILURE_METHOD, Throwable.class);
        onFailure.setAccessible(true);
        onFailure.invoke(callback, throwable);
    }

    /**
     * Gets field value by name using reflection.Method returns Object.Method can throw exception.
     *
     * @param clazz
     *         class from which we need get field value
     * @param object
     *         value of object from which need get field value
     * @param fieldName
     *         name of field which need get
     * @return value of field
     * @throws Exception
     */
    @Nonnull
    public static Object getFieldValueByName(@Nonnull Class<?> clazz, @Nonnull Object object, @Nonnull String fieldName) throws Exception {

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);

        return field.get(object);
    }

    /**
     * Method returns content stream for current class using special path to resource.
     *
     * @param clazz
     *         class for which need content stream
     * @param pathToResource
     *         path to resource
     * @return instance {@link ContentStream}
     */
    @Nonnull
    public static ContentStream getContent(@Nonnull Class<?> clazz, @Nonnull String pathToResource) {
        InputStream resourceStream = clazz.getResourceAsStream(pathToResource);
        return new ContentStream(null, resourceStream, null);
    }

}