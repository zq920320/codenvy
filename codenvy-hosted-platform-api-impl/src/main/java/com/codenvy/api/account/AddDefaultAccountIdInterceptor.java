/*
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
package com.codenvy.api.account;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.api.workspace.server.WorkspaceService;

import javax.inject.Singleton;

/**
 * Intercepts {@link WorkspaceService} methods.
 *
 * <p>The strategy: if method has the last parameter with a {@link String} type and it is null
 * then default account {@link DefaultAccountCreator#DEFAULT_ACCOUNT_ID identifier} will be used
 *
 * <p>This is temporary solution so this interceptor must live as long as {@link DefaultAccountCreator} does.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class AddDefaultAccountIdInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        final Class<?>[] paramTypes = invocation.getMethod().getParameterTypes();
        final Object[] arguments = invocation.getArguments();
        final int accountParamIdx = arguments.length - 1;
        if (arguments.length > 0 && arguments[accountParamIdx] == null && String.class.isAssignableFrom(paramTypes[accountParamIdx])) {
            arguments[accountParamIdx] = DefaultAccountCreator.DEFAULT_ACCOUNT_ID;
        }
        return invocation.proceed();
    }
}
