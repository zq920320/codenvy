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
import com.google.gwt.http.client.RequestCallback;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

import static com.codenvy.ide.ext.gae.TestUtil.getFieldValueByName;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class GAEAsyncCallbackFactoryTest {

    private static final String NOTIFICATION_MANAGER_FIELD_NAME = "notificationManager";
    private static final String LOCALE_FIELD_NAME               = "locale";
    private static final String UNMARSHALLER_FIELD_NAME         = "unmarshaller";
    private static final String SUCCESS_FIELD_NAME              = "successCallback";
    private static final String FAILURE_FIELD_NAME              = "failureCallback";

    //constructor parameters
    @Mock
    private NotificationManager     notificationManager;
    @Mock
    private GAELocalizationConstant locale;
    @Mock
    private DtoUnmarshallerFactory  dtoUnmarshallerFactory;

    //needed mocks
    @Mock
    private SuccessCallback<Object> successCallback;
    @Mock
    private Unmarshallable<Object>  unmarshallable;
    @Mock
    private FailureCallback         failureCallback;

    @InjectMocks
    private GAEAsyncCallbackFactory asyncCallbackFactory;

    @Test
    public void asyncRequestCallBackShouldBeReturnedWithSuccessCallBackParameter() throws Exception {
        GAEAsyncRequestCallback gaeAsyncRequestCallback = asyncCallbackFactory.build(successCallback);

        Map<String, Object> fieldsValues = getFieldsValues(gaeAsyncRequestCallback);

        assertGeneralFieldsValues(fieldsValues);
        assertThat(fieldsValues.get(UNMARSHALLER_FIELD_NAME), nullValue());
        assertThat(fieldsValues.get(FAILURE_FIELD_NAME), nullValue());
    }

    private Map<String, Object> getFieldsValues(@NotNull RequestCallback requestCallback) throws Exception {
        Map<String, Object> fieldsValues = new HashMap<>();

        Class clazz = requestCallback.getClass();

        fieldsValues.put(NOTIFICATION_MANAGER_FIELD_NAME, getFieldValueByName(clazz, requestCallback, NOTIFICATION_MANAGER_FIELD_NAME));
        fieldsValues.put(LOCALE_FIELD_NAME, getFieldValueByName(clazz, requestCallback, LOCALE_FIELD_NAME));
        fieldsValues.put(UNMARSHALLER_FIELD_NAME, getFieldValueByName(clazz.getSuperclass(), requestCallback, UNMARSHALLER_FIELD_NAME));
        fieldsValues.put(SUCCESS_FIELD_NAME, getFieldValueByName(clazz, requestCallback, SUCCESS_FIELD_NAME));
        fieldsValues.put(FAILURE_FIELD_NAME, getFieldValueByName(clazz, requestCallback, FAILURE_FIELD_NAME));

        return fieldsValues;
    }

    @Test
    public void asyncRequestCallBackShouldBeReturnedWithSuccessCallBackAndUnmarshaller() throws Exception {
        GAEAsyncRequestCallback asyncRequestCallback = asyncCallbackFactory.build(unmarshallable, successCallback);

        Map<String, Object> fieldsValues = getFieldsValues(asyncRequestCallback);

        assertGeneralFieldsValues(fieldsValues);
        assertSame(fieldsValues.get(UNMARSHALLER_FIELD_NAME), unmarshallable);
        assertThat(fieldsValues.get(FAILURE_FIELD_NAME), nullValue());

    }

    private void assertGeneralFieldsValues(@NotNull Map<String, Object> fieldsValues) {
        assertSame(fieldsValues.get(NOTIFICATION_MANAGER_FIELD_NAME), notificationManager);
        assertSame(fieldsValues.get(LOCALE_FIELD_NAME), locale);
        assertSame(fieldsValues.get(SUCCESS_FIELD_NAME), successCallback);
    }

    @Test
    public void asyncRequestCallBackShouldBeReturnedWithSuccessCallBackAndCreatedUnmarshaller() throws Exception {
        when(dtoUnmarshallerFactory.newUnmarshaller(Object.class)).thenReturn(unmarshallable);

        GAEAsyncRequestCallback asyncRequestCallback = asyncCallbackFactory.build(Object.class, successCallback);

        Map<String, Object> fieldsValues = getFieldsValues(asyncRequestCallback);

        assertGeneralFieldsValues(fieldsValues);
        assertSame(fieldsValues.get(UNMARSHALLER_FIELD_NAME), unmarshallable);
        assertThat(fieldsValues.get(FAILURE_FIELD_NAME), nullValue());
    }

    @Test
    public void asyncRequestCallBackShouldBeReturnedWithSuccessAndFailureCallBacksAndUnmarshaller() throws Exception {
        when(dtoUnmarshallerFactory.newUnmarshaller(Object.class)).thenReturn(unmarshallable);

        GAEAsyncRequestCallback asyncRequestCallback = asyncCallbackFactory.build(Object.class, successCallback, failureCallback);

        Map<String, Object> fieldsValues = getFieldsValues(asyncRequestCallback);

        assertGeneralFieldsValues(fieldsValues);
        assertSame(fieldsValues.get(FAILURE_FIELD_NAME), failureCallback);
    }

    @Test
    public void asyncRequestCallBackShouldBeReturnedWithSuccessAndFailureCallBacksParameters() throws Exception {
        GAEAsyncRequestCallback gaeAsyncRequestCallback = asyncCallbackFactory.build(successCallback, failureCallback);

        Map<String, Object> fieldsValues = getFieldsValues(gaeAsyncRequestCallback);

        assertGeneralFieldsValues(fieldsValues);
        assertThat(fieldsValues.get(UNMARSHALLER_FIELD_NAME), nullValue());
        assertSame(fieldsValues.get(FAILURE_FIELD_NAME), failureCallback);

    }

}