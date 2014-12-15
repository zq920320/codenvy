/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2014] Codenvy, S.A. 
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
package com.codenvy.auth.sso.server.interceptor;

/**
 * @author Sergii Kabashniuk
 */

import com.codenvy.api.workspace.shared.dto.MemberDescriptor;
import com.codenvy.auth.sso.server.ticket.RolesInvalidator;
import com.codenvy.commons.env.EnvironmentContext;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

/**
 * Intercepts calls to workspace/addMember() service and do some post actions
 * <p/>
 * Invalidate user roles
 *
 * @author Sergii Kabashniuk
 */
public class AddWorkspaceMemberInterceptor implements MethodInterceptor {


    private static final Logger LOG = LoggerFactory.getLogger(AddWorkspaceMemberInterceptor.class);

    @Inject
    RolesInvalidator rolesInvalidator;


    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object result = invocation.proceed();
        if ("addMember".equals(invocation.getMethod().getName())) {
            MemberDescriptor descriptor = (MemberDescriptor)((Response)result).getEntity();
            EnvironmentContext environmentContext = EnvironmentContext.getCurrent();

            try {
                //TODO fix accountid get, its a hack.
                rolesInvalidator.invalidateRoles(descriptor.getUserId(), descriptor.getWorkspaceReference().getId(),
                                                 environmentContext.getAccountId());
                LOG.info("Roles for user {} invalidated", descriptor.getUserId());


            } catch (Exception e) {
                LOG.warn("Unable to invalidate user roles", e);
            }
        }
        return result;
    }

}
