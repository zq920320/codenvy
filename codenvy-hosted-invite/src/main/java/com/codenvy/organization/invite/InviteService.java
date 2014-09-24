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
package com.codenvy.organization.invite;

import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.rest.HttpJsonHelper;
import com.codenvy.api.core.rest.shared.dto.Link;
import com.codenvy.api.user.shared.dto.UserDescriptor;
import com.codenvy.api.workspace.shared.dto.NewMembership;
import com.codenvy.api.workspace.shared.dto.WorkspaceDescriptor;
import com.codenvy.commons.env.EnvironmentContext;
import com.codenvy.commons.lang.Pair;
import com.codenvy.dto.server.DtoFactory;

import org.codenvy.mail.MailSenderClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codenvy.commons.lang.IoUtil.getResource;
import static com.codenvy.commons.lang.IoUtil.readAndCloseQuietly;

/** This class contains services for sending invitations messages to codenvy users */
@Path("/invite/{ws-id}")
public class InviteService {

    public static final  Pattern EMAIL_PATTERN = Pattern.compile("^(?:.*<)?(.+@.+?)(?:>)?$");
    private static final Logger  LOG           = LoggerFactory.getLogger(InviteService.class);
    protected final URI                 INVITE_ERROR_PAGE;
    protected final BearerTokenProvider provider;
    protected final MailSenderClient    mailSenderClient;

    @Inject
    public InviteService(
            BearerTokenProvider provider,
            MailSenderClient mailSenderClient) throws URISyntaxException {
        this.mailSenderClient = mailSenderClient;
        this.provider = provider;
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


            Link getWorkspaceLink = DtoFactory.getInstance().createDto(Link.class).withMethod("GET")
                                              .withHref(uriInfo.getBaseUriBuilder()
                                                               .replacePath("api/workspace/" + workspaceId).build().toString());
            WorkspaceDescriptor workspace = HttpJsonHelper.request(WorkspaceDescriptor.class, getWorkspaceLink);
            UserDescriptor user;
            try {
                Link getUserLink = DtoFactory.getInstance().createDto(Link.class).withMethod("GET")
                                             .withHref(uriInfo.getBaseUriBuilder()
                                                              .replacePath("api/user/find").build().toString());
                user = HttpJsonHelper.request(UserDescriptor.class, getUserLink, Pair.of("email", mailRecipient));
            } catch (NotFoundException e) {
                String token = provider.getBearerToken(mailRecipient);
                Link createUserLink = DtoFactory.getInstance().createDto(Link.class).withMethod("POST")
                                                .withHref(uriInfo.getBaseUriBuilder()
                                                                 .replacePath("api/user/create").build().toString());

                user = HttpJsonHelper.request(UserDescriptor.class, createUserLink, Pair.of("token", token));
            }

            Link createMembershipLink = DtoFactory.getInstance().createDto(Link.class).withMethod("POST")
                                                  .withHref(uriInfo.getBaseUriBuilder().replacePath(
                                                          "api/workspace/" + workspaceId + "/members").build()
                                                                   .toString());

            NewMembership membership = DtoFactory.getInstance().createDto(NewMembership.class).withUserId(user.getId())
                                                 .withRoles(Arrays.asList("workspace/developer"));

            HttpJsonHelper.request(null, createMembershipLink, membership);

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
                              sender + " Has Invited You To Codenvy",
                              "text/html; charset=utf-8",
                              readAndCloseQuietly(getResource("/email-templates/invite_a_friend.html")),
                              inviteMessageProperties);

            LOG.info("EVENT#user-invite# EMAIL#{}# USER#{}# WS#{}#", mailRecipient, sender, workspace.getName());
            return Response.ok().build();
        } catch (IOException | MessagingException | ApiException e) {
            throw new WebApplicationException(e);
        }
    }
}
