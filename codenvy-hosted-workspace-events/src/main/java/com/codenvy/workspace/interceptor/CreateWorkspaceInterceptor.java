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

import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.api.workspace.shared.dto.WorkspaceDescriptor;
import com.codenvy.commons.env.EnvironmentContext;
import com.codenvy.commons.lang.IoUtil;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.codenvy.mail.MailSenderClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Intercepts calls to workspace/create() service and sends welcome email.
 * @author Max Shaposhnik
 */
public class CreateWorkspaceInterceptor implements MethodInterceptor {

    @Inject
    private MailSenderClient mailSenderClient;

    @Inject
    private UserDao userDao;


    @Inject
    @Named("api.endpoint")
    private String apiEndpoint;

    private static final String MAIL_TEMPLATE = "email-templates/workspace_created.html";

    private static final Logger LOG = LoggerFactory.getLogger(CreateWorkspaceInterceptor.class);

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object result = invocation.proceed();
        if ("create".equals(invocation.getMethod().getName())) {
            try {
                String creatorEmail = userDao.getById(EnvironmentContext.getCurrent().getUser().getId()).getEmail();
                WorkspaceDescriptor descriptor = (WorkspaceDescriptor)((Response)result).getEntity();

                Map<String, String> props = new HashMap<>();
                props.put("com.codenvy.masterhost.url", apiEndpoint.substring(0, apiEndpoint.lastIndexOf("/")));
                props.put("workspace", descriptor.getName());

                mailSenderClient.sendMail("Codenvy <noreply@codenvy.com>", creatorEmail, null,
                                          "Welcome To Codenvy",
                                          MediaType.TEXT_HTML,
                                          IoUtil.readAndCloseQuietly(IoUtil.getResource("/" + MAIL_TEMPLATE)), props);

                LOG.info("Workspace created message send to {}", creatorEmail);
            } catch (Exception e) {
                LOG.warn("Unable to send workspace creation notification email", e);
            }

        }
        return result;
    }
}
