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
package com.codenvy.workspace.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.codenvy.mail.MailSenderClient;
import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.account.server.dao.Member;
import org.eclipse.che.api.core.model.workspace.UsersWorkspace;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.IoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Intercepts calls to workspace/removeMember() service and do some post actions
 * <p/>
 * Invalidate user roles
 * Sends info email to removed user,
 *
 * @author Max Shaposhnik
 */
public class RemoveWorkspaceMemberInterceptor implements MethodInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(AddWorkspaceMemberInterceptor.class);

    private static final String MAIL_TEMPLATE = "email-templates/user_removed_from_workspace.html";

    @Inject
    private MailSenderClient mailSenderClient;

    @Inject
    private UserDao userDao;

    @Inject
    private AccountDao accountDao;

    @Inject
    private WorkspaceManager workspaceManager;

    @Inject
    @Named("api.endpoint")
    private String apiEndpoint;

    @Inject
    @Named("workspace.email.removed.member.enabled")
    private boolean sendEmailOnMemberRemoved;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object result = invocation.proceed();
        // Do not send notification if operation is turned off
        if (!sendEmailOnMemberRemoved) {
            return result;
        }
        EnvironmentContext environmentContext = EnvironmentContext.getCurrent();
        String workspaceId = (String)invocation.getArguments()[0];
        UsersWorkspace ws = workspaceManager.getWorkspace(workspaceId);
        if (ws.isTemporary()) {
            return result;
        }
        String userId = (String)invocation.getArguments()[1];
        String recipientEmail = userDao.getById(userId)
                                       .getEmail();
        String senderEmail = environmentContext.getUser()
                                               .getName();
        Optional<Member> accountOwner = accountDao.getMembers(environmentContext.getAccountId())
                                                  .stream()
                                                  .filter(member -> member.getRoles()
                                                                          .contains("account/owner"))
                                                  .findFirst();
        Map<String, String> properties = new HashMap<>();
        properties.put("com.codenvy.masterhost.url", apiEndpoint.substring(0, apiEndpoint.lastIndexOf("/")));
        properties.put("workspace", ws.getName());
        properties.put("admin.email", senderEmail);
        if (accountOwner.isPresent()) {
            properties.put("accountOwner.email", userDao.getById(accountOwner.get()
                                                                             .getUserId())
                                                        .getEmail());
        }
        mailSenderClient.sendMail("Codenvy <noreply@codenvy.com>", recipientEmail, null,
                                  "Codenvy Workspace Access Removed",
                                  "text/html; charset=utf-8",
                                  IoUtil.readAndCloseQuietly(IoUtil.getResource("/" + MAIL_TEMPLATE)), properties);

        LOG.info("User added into ws message send to {}", recipientEmail);
        return result;
    }
}
