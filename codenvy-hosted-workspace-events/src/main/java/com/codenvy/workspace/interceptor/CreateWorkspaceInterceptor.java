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

import com.codenvy.workspace.activity.WsActivityEventSender;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.codenvy.mail.MailSenderClient;
import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.account.server.dao.Member;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDescriptor;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.IoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Intercepts calls to workspace/create() service, updates WS last access time and sends welcome email in necessary.
 *
 * @author Max Shaposhnik
 */
public class CreateWorkspaceInterceptor implements MethodInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(CreateWorkspaceInterceptor.class);

    private static final String MAIL_TEMPLATE = "email-templates/workspace_created.html";

    @Inject
    private MailSenderClient mailSenderClient;

    @Inject
    private WsActivityEventSender wsActivityEventSender;

    @Inject
    private WorkspaceDao workspaceDao;

    @Inject
    private AccountDao accountDao;

    @Inject
    private UserDao userDao;

    @Inject
    @Named("api.endpoint")
    private String apiEndpoint;

    @Inject
    @Named("subscription.saas.usage.free.gbh")
    private String freeGbh;

    @Inject
    @Named("subscription.saas.free.max_limit_mb")
    private String freeLimit;

    @Inject
    @Named("workspace.email.created.enabled")
    private boolean sendEmailOnWorkspaceCreated;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object result = invocation.proceed();
        // Do not send notification if operation is turned off
        WorkspaceDescriptor descriptor = (WorkspaceDescriptor)((Response)result).getEntity();
        wsActivityEventSender.onActivity(descriptor.getId(), descriptor.isTemporary());

        if (!sendEmailOnWorkspaceCreated) {
            return result;
        }

        if (!descriptor.isTemporary()) {
            if (workspaceDao.getByAccount(descriptor.getAccountId()).stream().noneMatch(
                    workspace -> !workspace.isTemporary() && !workspace.getId().equals(descriptor.getId()))) {
                try {
                    Optional<Member> accountOwner = accountDao.getMembers(descriptor.getAccountId())
                                                        .stream()
                                                        .filter(member -> member.getRoles().contains("account/owner"))
                                                        .findFirst();
                    String creatorEmail = userDao.getById(EnvironmentContext.getCurrent().getUser().getId()).getEmail();
                    Map<String, String> properties = new HashMap<>();
                    properties.put("com.codenvy.masterhost.url", apiEndpoint.substring(0, apiEndpoint.lastIndexOf("/")));
                    properties.put("workspace", descriptor.getName());
                    properties.put("free.gbh", freeGbh);
                    if (accountOwner.isPresent()) {
                        properties.put("email", userDao.getById(accountOwner.get().getUserId()).getEmail());
                    }
                    properties.put("free.limit", Long.toString(Math.round((float)Long.parseLong(freeLimit) / 1000f)));
                    mailSenderClient.sendMail("Codenvy <noreply@codenvy.com>", creatorEmail, null,
                                              "Welcome To Codenvy",
                                              MediaType.TEXT_HTML,
                                              IoUtil.readAndCloseQuietly(IoUtil.getResource("/" + MAIL_TEMPLATE)),
                                              properties);

                    LOG.info("Workspace created message send to {}", creatorEmail);

                } catch (Exception e) {
                    LOG.warn("Unable to send workspace creation notification email", e);
                }
            }
        }
        return result;
    }
}
