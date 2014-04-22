/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2013] Codenvy, S.A.
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
package com.codenvy.organization.invite;

import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.user.server.Constants;
import com.codenvy.api.user.server.dao.MemberDao;
import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.api.user.server.dao.UserProfileDao;
import com.codenvy.api.user.shared.dto.Attribute;
import com.codenvy.api.user.shared.dto.Member;
import com.codenvy.api.user.shared.dto.Profile;
import com.codenvy.api.user.shared.dto.User;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.api.workspace.shared.dto.Workspace;
import com.codenvy.commons.env.EnvironmentContext;
import com.codenvy.commons.lang.NameGenerator;
import com.codenvy.dto.server.DtoFactory;

import org.codenvy.mail.MailSenderClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codenvy.commons.lang.IoUtil.getResource;
import static com.codenvy.commons.lang.IoUtil.readAndCloseQuietly;

/** This class contains services for sending invitations messages to codenvy users */
@Path("/invite/{ws-id}")
public class InviteService {

    public static final  Pattern EMAIL_PATTERN = Pattern.compile("^(?:.*<)?(.+@.+?)(?:>)?$");
    private static final Logger  LOG           = LoggerFactory.getLogger(InviteService.class);
    protected final URI              INVITE_ERROR_PAGE;
    protected final UserDao          userDao;
    protected final MemberDao        memberDao;
    protected final WorkspaceDao     workspaceDao;
    protected final UserProfileDao   profileDao;
    protected final MailSenderClient mailSenderClient;

    @Inject
    public InviteService(UserDao userDao, WorkspaceDao workspaceDao, MemberDao memberDao, UserProfileDao profileDao,
                         MailSenderClient mailSenderClient) throws URISyntaxException {
        this.userDao = userDao;
        this.workspaceDao = workspaceDao;
        this.profileDao =profileDao;
        this.memberDao = memberDao;
        this.mailSenderClient = mailSenderClient;
        this.INVITE_ERROR_PAGE = new URI("/site/error/error-send-invite");
    }

    /**
     * Sends email message to invited person and creates record about sent invite.
     *
     * @param mailRecipient
     *         - address of invited person
     * @param personalMessage
     *         - message what will receive invited person
     * @return the Response with corresponded status (200)
     */
    @POST
    @Path("{mailrecipient}")
    @RolesAllowed("workspace/admin")
    @Consumes("text/*")
    public Response createInvitation(@PathParam("ws-id") String workspaceId, @Context SecurityContext security,
                                     @PathParam("mailrecipient") String mailRecipient, String personalMessage,
                                     @Context UriInfo uriInfo) throws ConflictException, ServerException, NotFoundException {
        try {
            String sender = security.getUserPrincipal().getName();
            EnvironmentContext context = EnvironmentContext.getCurrent();
            String workspaceName = context.getWorkspaceName();
            if (workspaceName == null) {
                LOG.error("Workspace name variable are not set in EnvironmentContext");
                throw new WebApplicationException();
            }

            try {
                InternetAddress emailAddr = new InternetAddress(mailRecipient);
                emailAddr.validate();
            } catch (AddressException ex) {
                return Response.status(400).entity("Specified email address is not valid.").build();
            }
            Matcher matcher = EMAIL_PATTERN.matcher(mailRecipient);
            if (matcher.matches()) {
                mailRecipient = matcher.group(1);
            } else {
                return Response.status(400).entity("Specified email address is not valid.").build();
            }



//            if (user == null) {
                // generate password and delete all "-" symbols which are generated by randomUUID()
            User user = null;
            try {
                user = userDao.getByAlias(mailRecipient);

            } catch (NotFoundException e) {
                String userId = NameGenerator.generate(User.class.getSimpleName(), Constants.ID_LENGTH);
                user = DtoFactory.getInstance().createDto(User.class).withId(userId).withEmail(mailRecipient)
                        .withPassword(
                                UUID.randomUUID().toString().replace("-", "").substring(0, 12));
                userDao.create(user);
                Attribute attribute =
                        DtoFactory.getInstance().createDto(Attribute.class).withName("temporary").withValue(
                                "false");
                profileDao.create(DtoFactory.getInstance().createDto(Profile.class).withId(userId).withUserId(userId)
                        .withAttributes(Arrays.asList(attribute)));
            }
//            }
            Workspace workspace =  workspaceDao.getById(workspaceId);
            memberDao.create(DtoFactory.getInstance().createDto(Member.class).withWorkspaceId(workspaceId)
                                       .withUserId(user.getId()).withRoles(Arrays.asList("workspace/developer")));

            Map<String, String> inviteMessageProperties = new HashMap<>();
            inviteMessageProperties.put("com.codenvy.masterhost.url", uriInfo.getBaseUriBuilder().replacePath(null).build().toString());
            inviteMessageProperties.put("inviter.email", sender);
            inviteMessageProperties.put("workspace", workspace.getName());
            inviteMessageProperties.put("user.name", mailRecipient);

            if (personalMessage != null && personalMessage.length() > 0) {
                inviteMessageProperties.put("personal-message", "<td><p><strong>Personal message</strong></p><p>"
                                                                + personalMessage + "</p></td>");
            }

            mailSenderClient
                    .sendMail("Codenvy <noreply@cloud-ide.com>", mailRecipient, null,
                              "You've been invited to use " + workspace.getName() + " workspace",
                              "text/html; charset=utf-8",
                              readAndCloseQuietly(getResource("/codenvy-template-mail-invitation-registered-user.html")),
                              inviteMessageProperties);

            LOG.info("EVENT#user-invite# EMAIL#" + mailRecipient + "#");
            return Response.ok().build();
        } catch (IOException | MessagingException e) {
            throw new WebApplicationException(e);
        }
    }
}
