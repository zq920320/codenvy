/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
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
package com.codenvy.user.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.codenvy.mail.MailSenderClient;
import org.eclipse.che.api.user.server.UserService;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.eclipse.che.commons.lang.IoUtil.getResource;
import static org.eclipse.che.commons.lang.IoUtil.readAndCloseQuietly;

/**
 * Intercepts {@link UserService} methods.
 * <p>
 * The purpose of the interceptor is to send "welcome to codenvy" email to user after its creation.
 *
 * @author Anatoliy Bazko
 */
@Singleton
public class CreateUserInterceptor implements MethodInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(CreateUserInterceptor.class);

    private static final String EMAIL_TEMPLATE_USER_CREATED_WITH_PASSWORD    = "email-templates/user_created_with_password.html";
    private static final String EMAIL_TEMPLATE_USER_CREATED_WITHOUT_PASSWORD = "email-templates/user_created_without_password.html";

    @Inject
    private MailSenderClient mailSenderClient;

    @Inject
    @Named("api.endpoint")
    private String apiEndpoint;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        invocation.proceed();

        try {
            User user = (User)invocation.getArguments()[0];

            String userEmail = user.getEmail();
            URL urlEndpoint = new URL(apiEndpoint);
            String template = isUserCreatedByAdmin() ? EMAIL_TEMPLATE_USER_CREATED_WITH_PASSWORD
                                                     : EMAIL_TEMPLATE_USER_CREATED_WITHOUT_PASSWORD;

            Map<String, String> properties = new HashMap<>();
            properties.put("com.codenvy.masterhost.url", urlEndpoint.getProtocol() + "://" + urlEndpoint.getHost());
            properties.put("username", user.getName());
            properties.put("password", user.getPassword());

            mailSenderClient.sendMail("Codenvy <noreply@codenvy.com>",
                                      userEmail,
                                      null,
                                      "Welcome To Codenvy",
                                      MediaType.TEXT_HTML,
                                      readAndCloseQuietly(getResource("/" + template)),
                                      properties);

            LOG.info("User created message send to {}", userEmail);
        } catch (Exception e) {
            LOG.warn("Unable to send creation notification email", e);
        }

        return Void.TYPE;
    }

    protected boolean isUserCreatedByAdmin() {
        EnvironmentContext context = EnvironmentContext.getCurrent();
        org.eclipse.che.commons.user.User contextUser = context.getUser();
        return contextUser != null && (contextUser.isMemberOf("system/admin") || contextUser.isMemberOf("system/manager"));
    }
}
