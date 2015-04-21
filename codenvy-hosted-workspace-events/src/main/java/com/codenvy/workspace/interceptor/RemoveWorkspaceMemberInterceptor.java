/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.workspace.interceptor;

import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.IoUtil;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.codenvy.mail.MailSenderClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * Intercepts calls to workspace/removeMember() service and do some post actions
 * <p/>
 * Invalidate user roles
 * Sends info email to removed user,
 *
 * @author Max Shaposhnik
 */
public class RemoveWorkspaceMemberInterceptor implements MethodInterceptor {


    @Inject
    private MailSenderClient mailSenderClient;

    @Inject
    private UserDao userDao;

    @Inject
    private WorkspaceDao workspaceDao;

    @Inject
    @Named("api.endpoint")
    private String apiEndpoint;

    private static final String MAIL_TEMPLATE = "email-templates/user_removed_from_workspace.html";

    private static final Logger LOG = LoggerFactory.getLogger(AddWorkspaceMemberInterceptor.class);

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object result = invocation.proceed();
        EnvironmentContext environmentContext = EnvironmentContext.getCurrent();
        if ("removeMember".equals(invocation.getMethod().getName())) {
            String workspaceId = (String)invocation.getArguments()[0];
            String userId = (String)invocation.getArguments()[1];
            String recipientEmail = userDao.getById(userId).getEmail();

            String senderEmail = environmentContext.getUser().getName();
            Workspace ws = workspaceDao.getById(workspaceId);
            Map<String, String> props = new HashMap<>();
            props.put("com.codenvy.masterhost.url", apiEndpoint.substring(0, apiEndpoint.lastIndexOf("/")));
            props.put("workspace", ws.getName());
            props.put("admin.email", senderEmail);

            mailSenderClient.sendMail("Codenvy <noreply@codenvy.com>", recipientEmail, null,
                                      "Codenvy Workspace Access Removed",
                                      "text/html; charset=utf-8",
                                      IoUtil.readAndCloseQuietly(IoUtil.getResource("/" + MAIL_TEMPLATE)), props);

            LOG.info("User added into ws message send to {}", recipientEmail);
        }
        return result;
    }

}
