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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.codenvy.mail.MailSenderClient;
import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.account.shared.dto.MemberDescriptor;
import org.eclipse.che.api.user.server.dao.MembershipDao;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.user.shared.model.Membership;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.IoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//TODO fix it after members refactoring

/**
 * Intercepts calls to workspace/addMember() service and do some post actions
 * <p/>
 * Invalidate user roles
 * Sends welcome email to newly added user,
 *
 * @author Max Shaposhnik
 */
public class AddWorkspaceMemberInterceptor implements MethodInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(AddWorkspaceMemberInterceptor.class);

    private static final String MAIL_TEMPLATE = "email-templates/user_added_into_workspace.html";
//
//    @Inject
//    private MailSenderClient mailSenderClient;
//
//    @Inject
//    private UserDao userDao;
//
//    @Inject
//    private AccountDao accountDao;
//
//    @Inject
//    private MembershipDao membershipDao;
//
//    @Inject
//    @Named("api.endpoint")
//    private String apiEndpoint;
//
//    @Inject
//    @Named("workspace.email.added.member.enabled")
//    private boolean sendEmailOnMemberAdded;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
//        Object result = invocation.proceed();
//        // Do not send notification if operation is turned off
//        if (!sendEmailOnMemberAdded) {
//            return result;
//        }
//        EnvironmentContext environmentContext = EnvironmentContext.getCurrent();
//        MemberDescriptor memberDescriptor = (MemberDescriptor)((Response)result).getEntity();
////        WorkspaceReference workspaceReference = memberDescriptor.getWorkspaceReference();
//        // Do not send notifications on join to temporary ws.
//        if (workspaceReference.isTemporary()) {
//            return result;
//        }
//        List<Membership> workspaceMembers = membershipDao.getAllMemberships("workspace", workspaceReference.getId());
//
//        // Do not send notification on joining of first member
//        if (workspaceMembers.size() < 2) {
//            return result;
//        }
//
//        try {
//            Optional<Membership> admin = workspaceMembers
//                    .stream()
//                    .filter(membership -> membership.getRoles()
//                                                    .contains("workspace/admin"))
//                    .findFirst();
//            Optional<org.eclipse.che.api.account.server.dao.Member> accountOwner =
//                    accountDao.getMembers(environmentContext.getAccountId())
//                              .stream()
//                              .filter(member -> member.getRoles()
//                                                      .contains("account/owner"))
//                              .findFirst();
//            String recipientEmail = userDao.getById(memberDescriptor.getUserId())
//                                           .getEmail();
//            String senderUserId = EnvironmentContext.getCurrent()
//                                                    .getUser()
//                                                    .getId();
//            String senderEmail = userDao.getById(senderUserId)
//                                        .getEmail();
//            Map<String, String> properties = new HashMap<>();
//            properties.put("com.codenvy.masterhost.url", apiEndpoint.substring(0, apiEndpoint.lastIndexOf("/")));
//            properties.put("workspace", workspaceReference.getName());
//            properties.put("usermail.whoInvited", senderEmail);
//            if (admin.isPresent()) {
//                properties.put("admin.email", userDao.getById(admin.get().getUserId()).getEmail());
//            }
//            if (accountOwner.isPresent()) {
//                properties.put("accountOwner.email", userDao.getById(accountOwner.get().getUserId()).getEmail());
//            }
//            mailSenderClient.sendMail("Codenvy <noreply@codenvy.com>", recipientEmail, null,
//                                      "Codenvy Workspace Invite",
//                                      "text/html; charset=utf-8",
//                                      IoUtil.readAndCloseQuietly(IoUtil.getResource("/" + MAIL_TEMPLATE)),
//                                      properties);
//
//            LOG.info("User added into ws message send to {}", recipientEmail);
//        } catch (Exception e) {
//            LOG.warn("Unable to send user added notification email", e);
//        }
//
//        return result;
        return null;
    }

}
