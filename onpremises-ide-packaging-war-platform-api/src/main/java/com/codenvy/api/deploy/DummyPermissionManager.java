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
package com.codenvy.api.deploy;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.permission.Operation;
import org.eclipse.che.api.core.rest.permission.PermissionManager;

import javax.inject.Singleton;
import java.util.Map;

// TODO remove it after account is established

/**
 * Dummy implementation of permission manager.
 *
 * @author Eugene Voevodin
 */
@Singleton
public class DummyPermissionManager implements PermissionManager {
    @Override
    public void checkPermission(Operation operation, String s, Map<String, String> map) throws ForbiddenException, ServerException {
    }
}
