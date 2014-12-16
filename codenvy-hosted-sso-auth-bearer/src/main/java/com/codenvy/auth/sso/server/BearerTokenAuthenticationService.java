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
package com.codenvy.auth.sso.server;

import com.codenvy.api.auth.AuthenticationException;
import com.codenvy.api.dao.authentication.AccessTicket;
import com.codenvy.api.dao.authentication.CookieBuilder;
import com.codenvy.api.dao.authentication.TicketManager;
import com.codenvy.api.dao.authentication.TokenGenerator;

import com.codenvy.auth.sso.server.handler.BearerTokenAuthenticationHandler;
import com.codenvy.auth.sso.server.organization.UserCreator;
import com.codenvy.auth.sso.server.organization.WorkspaceCreationValidator;
import com.codenvy.commons.lang.IoUtil;
import com.codenvy.commons.user.User;

import org.codenvy.mail.MailSenderClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Service to authenticate users using bearer tokens.
 * <p/>
 *
 * @author Alexander Garagatyi
 * @author Sergey Kabashniuk
 */
@Path("internal/token")
public class BearerTokenAuthenticationService {

    private static final Logger LOG           = LoggerFactory.getLogger(BearerTokenAuthenticationService.class);
    // TODO made this configurable
    private static final String MAIL_TEMPLATE = "email-templates/verify_email_address.html";
    @Inject
    protected TicketManager                    ticketManager;
    @Inject
    protected TokenGenerator                   uniqueTokenGenerator;
    @Inject
    private   BearerTokenAuthenticationHandler handler;
    @Inject
    protected MailSenderClient                 mailSenderClient;
    @Inject
    protected InputDataValidator               inputDataValidator;
    @Inject
    protected CookieBuilder                    cookieBuilder;
    @Inject
    protected WorkspaceCreationValidator       creationValidator;
    @Inject
    protected UserCreator                      userCreator;


    /**
     * Authenticates user by provided token, than creates the ldap user
     * and set the access/logged in cookies.
     *
     * @param credentials
     *         user credentials
     * @return principal user principal
     * @throws AuthenticationException
     */
    @POST
    @Path("authenticate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticate(@CookieParam("session-access-key") Cookie tokenAccessCookie,
                                 Credentials credentials, @Context UriInfo uriInfo)
            throws AuthenticationException {


        if (handler == null) {
            LOG.warn("Bearer authenticator is null.");
            return Response.serverError().build();
        }

        boolean isSecure = uriInfo.getRequestUri().getScheme().equals("https");
        String username = credentials.getUsername();
        if (!handler.isValid(credentials.getToken())) {
            throw new AuthenticationException("Provided token is not valid");
        }
        Map<String, String> payload = handler.getPayload(credentials.getToken());
        handler.authenticate(username, credentials.getToken());

        try {
            User principal = userCreator.createUser(username, payload.get("firstName"), payload.get("lastName"));


            Response.ResponseBuilder builder = Response.ok();
            if (tokenAccessCookie != null) {
                AccessTicket accessTicket = ticketManager.getAccessTicket(tokenAccessCookie.getValue());
                if (accessTicket != null) {
                    if (!principal.equals(accessTicket.getPrincipal())) {
                        // DO NOT REMOVE! This log will be used in statistic analyzing
                        LOG.info("EVENT#user-changed-name# OLD-USER#{}# NEW-USER#{}#",
                                 accessTicket.getPrincipal().getName(),
                                 principal.getName());
                        LOG.info("EVENT#user-sso-logged-out# USER#{}#", accessTicket.getPrincipal().getName());
                        // DO NOT REMOVE! This log will be used in statistic analyzing
                        ticketManager.removeTicket(accessTicket.getAccessToken());
                    }
                } else {
                    //cookie is outdated, clearing
                    cookieBuilder.clearCookies(builder, tokenAccessCookie.getValue(), isSecure);
                }
            }

            if (payload.containsKey("initiator")) {
                // DO NOT REMOVE! This log will be used in statistic analyzing
                LOG.info("EVENT#user-sso-logged-in# USING#{}# USER#{}#", payload.get("initiator"), username);
            }


            // If we obtained principal  - authentication is done.
            String token = uniqueTokenGenerator.generate();
            ticketManager.putAccessTicket(new AccessTicket(token, principal, "bearer"));

            cookieBuilder.setCookies(builder, token, isSecure);
            builder.entity(Collections.singletonMap("token", token));

            LOG.debug("Authenticate user {} with token {}", username, token);
            return builder.build();
        } catch (IOException e) {
            throw new AuthenticationException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Validates user email and workspace name, than sends confirmation mail.
     *
     * @param validationData
     *         - email and workspace name for validation
     * @param uriInfo
     * @return
     * @throws java.io.IOException
     * @throws MessagingException
     */
    @POST
    @Path("validate")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response validate(ValidationData validationData, @Context UriInfo uriInfo)
            throws IOException, MessagingException, AuthenticationException {

        /* Uncomment to block IDE2 registrations
          if (IdeVersionHolder.get()) {
              return Response.status(409).entity("Registrations to this Codenvy version are closed. Please use latest Codenvy version.").build();
          }
        */

        try {
            inputDataValidator.validateWSName(validationData.getWorkspacename());
            inputDataValidator.validateUserMail(validationData.getEmail());
            creationValidator.ensureUserCreationAllowed(validationData.getEmail(), validationData.getWorkspacename());
        } catch (IOException e) {
            switch (e.getLocalizedMessage()) {
                case "You are the owner of another persistent workspace.":
                    return Response.status(403).entity(e.getMessage()).build();
                case "This workspace name is reserved, please choose another name.":
                    return Response.status(409).entity(e.getMessage()).build();
                default:
                    throw e;
            }
        } catch (InputDataException e) {
            return Response.status(500).entity(e.getMessage()).build();
        }


        Map<String, String> props = new HashMap<>();
        props.put("com.codenvy.masterhost.url", uriInfo.getBaseUriBuilder().replacePath(null).build().toString());
        props.put("workspace", validationData.getWorkspacename());
        props.put("username", validationData.getEmail());
        props.put("bearertoken", handler.generateBearerToken(validationData.getEmail(), Collections.singletonMap("initiator", "email")));
        props.put("additional.query.params", uriInfo.getRequestUri().getQuery());

        mailSenderClient.sendMail("Codenvy <noreply@codenvy.com>", validationData.getEmail(), null,
                                  "Verify Your Codenvy Account",
                                  MediaType.TEXT_HTML,
                                  IoUtil.readAndCloseQuietly(IoUtil.getResource("/" + MAIL_TEMPLATE)), props);

        LOG.info("Email validation message send to {}", validationData.getEmail());

        return Response.ok().build();

    }

    public static class ValidationData {
        private String email;
        private String workspacename;

        public ValidationData() {
        }

        public ValidationData(String email, String workspacename) {
            this.email = email;
            this.workspacename = workspacename;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getWorkspacename() {
            return workspacename;
        }

        public void setWorkspacename(String workspacename) {
            this.workspacename = workspacename;
        }
    }

    public static class Credentials {
        private String username;
        private String token;

        public Credentials() {
        }

        public Credentials(String username, String token) {
            this.username = username;
            this.token = token;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
