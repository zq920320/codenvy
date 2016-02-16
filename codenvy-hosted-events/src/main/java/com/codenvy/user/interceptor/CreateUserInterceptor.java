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
package com.codenvy.user.interceptor;

import com.codenvy.mail.MailSenderClient;
import com.codenvy.mail.shared.dto.AttachmentDto;
import com.codenvy.mail.shared.dto.EmailBeanDto;
import com.google.common.io.Files;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.api.user.server.UserService;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static org.eclipse.che.commons.lang.IoUtil.getResource;
import static org.eclipse.che.commons.lang.IoUtil.readAndCloseQuietly;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

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
    private static final String LOGO                                         = "/email-templates/header.png";
    private static final String LOGO_CID                                     = "codenvyLogo";

    @Inject
    private MailSenderClient mailSenderClient;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        invocation.proceed();

        try {
            User user = (User)invocation.getArguments()[0];

            String userEmail = user.getEmail();
            String template = isUserCreatedByAdmin() ? EMAIL_TEMPLATE_USER_CREATED_WITH_PASSWORD
                                                     : EMAIL_TEMPLATE_USER_CREATED_WITHOUT_PASSWORD;

            Map<String, String> properties = new HashMap<>();
            properties.put("logo.cid", "codenvyLogo");
            properties.put("username", user.getName());
            properties.put("password", user.getPassword());

            File logo = new File(this.getClass().getResource(LOGO).getPath());

            AttachmentDto attachmentDto = newDto(AttachmentDto.class)
                    .withContent(Base64.getEncoder().encodeToString(Files.toByteArray(logo)))
                    .withContentId(LOGO_CID)
                    .withFileName("logo.png");

            EmailBeanDto emailBeanDto = newDto(EmailBeanDto.class)
                    .withBody(Deserializer.resolveVariables(readAndCloseQuietly(getResource("/" + template)), properties))
                    .withFrom("Codenvy <noreply@codenvy.com>")
                    .withTo(userEmail)
                    .withReplyTo(null)
                    .withSubject("Welcome To Codenvy")
                    .withMimeType(TEXT_HTML)
                    .withAttachments(Collections.singletonList(attachmentDto));

            mailSenderClient.sendMail(emailBeanDto);

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
