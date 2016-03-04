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
package com.codenvy.api.workspace;

import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.permission.Operation;
import org.eclipse.che.api.core.rest.permission.PermissionManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.eclipse.che.api.workspace.shared.Constants.START_WORKSPACE;

/**
 * Rejects/allows workspace service related operations.
 *
 * @author Eugene Voevodin
 */
@Singleton
public class WorkspacePermissionManager implements PermissionManager {

    private final AccountDao accountDao;

    @Inject
    public WorkspacePermissionManager(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public void checkPermission(Operation operation,
                                String userId,
                                Map<String, String> params) throws ForbiddenException, ServerException {
        requireNonNull(operation, "Operation must not be null");
        requireNonNull(userId, "User id must not be null");
        requireNonNull(params, "Parameters must not be null");

        final String accountId = params.get("accountId");

        if (START_WORKSPACE.equals(operation)) {
            if (accountDao.getMembers(accountId)
                          .stream()
                          .noneMatch(member -> userId.equals(member.getUserId()) && member.getRoles().contains("account/owner"))) {
                throw new ForbiddenException(format("Workspace start rejected. User '%s' doesn't own account '%s'", userId, accountId));
            }
        }
    }
}
