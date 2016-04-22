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
package com.codenvy.api.workspace.server.stack;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.api.core.acl.AclEntryImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;

import javax.inject.Singleton;
import java.util.ArrayList;

/**
 * Adds acl entry for creator before stack creation
 *
 * @author Sergii Leschenko
 */
@Singleton
public class StackCreatorPermissionsProvider implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        final StackImpl stack = (StackImpl)methodInvocation.getArguments()[0];
        final String creator = stack.getCreator();
        stack.getAcl().removeIf(aclEntry -> aclEntry.getUser().equals(creator));
        stack.getAcl().add(new AclEntryImpl(creator,
                                            new ArrayList<>(new StackDomain().getAllowedActions())));
        return methodInvocation.proceed();
    }
}
