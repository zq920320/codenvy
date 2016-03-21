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
package com.codenvy.workspace;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.api.workspace.server.model.impl.UsersWorkspaceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;

/**
 * This class provide execution required actions in virtual file system when workspace was created
 *
 * @author Sergii Leschenko
 * @author Alexander Garagatyi
 * @author Max Shaposhnik
 */
@Singleton
public class CreateWsRootDirInterceptor implements MethodInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(CreateWsRootDirInterceptor.class);

    @Inject
    @Named("che.user.workspaces.storage")
    private File rootDirectory;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object result = invocation.proceed();
        String wsId = ((UsersWorkspaceImpl)result).getId();

        File wsPath = new File(rootDirectory, wsId);
        if (!wsPath.exists()) {
            if (!wsPath.mkdirs()) {
                LOG.warn("Can not create root folder for workspace VFS {}", wsId);
            }
        }
        return result;
    }
}
