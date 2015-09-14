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
import org.eclipse.che.ide.websocket.rest.RequestCallback;
import org.eclipse.che.ide.websocket.rest.Unmarshallable;

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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class GAERequestCallBackFactoryTest {

    private static final String NOTIFICATION_MANAGER_FIELD_NAME = "notificationManager";
    private static final String UNMARSHALLER_FIELD_NAME         = "unmarshaller";
    private static final String SUCCESS_FIELD_NAME              = "successCallback";
    private static final String FAILURE_FIELD_NAME              = "failureCallback";

    //constructor mocks
    @Mock
    private DtoUnmarshallerFactory dtoUnmarshallerFactory;
    @Mock
    private NotificationManager    notificationManager;

    //needed mocks
    @Mock
    private Unmarshallable<Object>  unmarshallable;
    @Mock
    private SuccessCallback<Object> successCallback;
    @Mock
    private FailureCallback         failureCallback;

    @InjectMocks
    private GAERequestCallBackFactory callBackFactory;

    @Test
    public void asyncCallBackShouldBeReturnedWithUnmurshallerAndSuccessCallBack() throws Exception {
        GAERequestCallback requestCallback = callBackFactory.build(unmarshallable, successCallback);

        Map<String, Object> fieldsValues = getFieldsValues(requestCallback);

        assertGeneralFieldsValues(fieldsValues);
        assertThat(fieldsValues.get(FAILURE_FIELD_NAME), nullValue());
    }

    private Map<String, Object> getFieldsValues(@NotNull RequestCallback requestCallback) throws Exception {
        Map<String, Object> fieldsValues = new HashMap<>();

        Class clazz = requestCallback.getClass();

        fieldsValues.put(NOTIFICATION_MANAGER_FIELD_NAME, getFieldValueByName(clazz, requestCallback, NOTIFICATION_MANAGER_FIELD_NAME));
        fieldsValues.put(UNMARSHALLER_FIELD_NAME, getFieldValueByName(clazz.getSuperclass(), requestCallback, UNMARSHALLER_FIELD_NAME));
        fieldsValues.put(SUCCESS_FIELD_NAME, getFieldValueByName(clazz, requestCallback, SUCCESS_FIELD_NAME));
        fieldsValues.put(FAILURE_FIELD_NAME, getFieldValueByName(clazz, requestCallback, FAILURE_FIELD_NAME));

        return fieldsValues;
    }

    private void assertGeneralFieldsValues(@NotNull Map<String, Object> fieldsValues) {
        assertSame(fieldsValues.get(NOTIFICATION_MANAGER_FIELD_NAME), notificationManager);
        assertSame(fieldsValues.get(SUCCESS_FIELD_NAME), successCallback);
        assertSame(fieldsValues.get(UNMARSHALLER_FIELD_NAME), unmarshallable);
    }

    @Test
    public void asyncCallBackShouldBeReturnedWithSuccessCallBackAndCreatedUnmarshaller() throws Exception {
        when(dtoUnmarshallerFactory.newWSUnmarshaller(Object.class)).thenReturn(unmarshallable);

        GAERequestCallback<Object> requestCallback = callBackFactory.build(Object.class, successCallback);

        Map<String, Object> fieldsValues = getFieldsValues(requestCallback);

        assertGeneralFieldsValues(fieldsValues);
        assertThat(fieldsValues.get(FAILURE_FIELD_NAME), nullValue());
    }

    @Test
    public void asyncCallBackShouldBeReturnedWithAllParameters() throws Exception {
        GAERequestCallback requestCallback = callBackFactory.build(notificationManager, unmarshallable, successCallback, failureCallback);

        Map<String, Object> fieldsValues = getFieldsValues(requestCallback);

        assertGeneralFieldsValues(fieldsValues);
        assertSame(fieldsValues.get(FAILURE_FIELD_NAME), failureCallback);
    }

    @Test
    public void asyncCallBackShouldBeReturnedWithAllParametersAndCreatedUnmarshaller() throws Exception {
        when(dtoUnmarshallerFactory.newWSUnmarshaller(Object.class)).thenReturn(unmarshallable);

        GAERequestCallback requestCallback = callBackFactory.build(Object.class, successCallback, failureCallback);

        Map<String, Object> fieldsValues = getFieldsValues(requestCallback);

        assertGeneralFieldsValues(fieldsValues);
        assertSame(fieldsValues.get(FAILURE_FIELD_NAME), failureCallback);
    }

}