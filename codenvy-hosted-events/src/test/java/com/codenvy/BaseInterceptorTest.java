/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy;

import org.aopalliance.intercept.MethodInterceptor;

import java.lang.reflect.Field;

/**
 * @author Anatoliy Bazko
 */
public class BaseInterceptorTest {

    protected void setInterceptorPrivateFieldValue(MethodInterceptor interceptor,
                                                   String fieldName,
                                                   Object value) throws NoSuchFieldException, IllegalAccessException {

        Field notificationTurnedOn = interceptor.getClass().getDeclaredField(fieldName);
        notificationTurnedOn.setAccessible(true);
        notificationTurnedOn.set(interceptor, value);
    }
}
