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
package com.codenvy.service.password;

import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.api.user.shared.dto.User;

import org.codenvy.mail.MailSenderClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.codenvy.commons.lang.IoUtil.getResource;
import static com.codenvy.commons.lang.IoUtil.readAndCloseQuietly;

/** Services for password features */
@Path("/password")
public class PasswordService {
    private static final Logger LOG           = LoggerFactory.getLogger(PasswordService.class);
    private static final String MAIL_TEMPLATE = "/email-templates/password_recovery.html";

    @Inject
    private MailSenderClient mailService;

    @Inject
    private UserDao userDao;

    @Inject
    private RecoveryStorage recoveryStorage;

    // TODO made this configurable
    private final String mailSender = "Codenvy <noreply@codenvy.com>";

    // TODO made this configurable
    private final String recoverMailSubject = "Codenvy password recover";

    private final CacheControl noCache;

    public PasswordService() {
        noCache = new CacheControl();
        noCache.setNoCache(true);
    }

    @Inject
    PasswordService(MailSenderClient mailService, UserDao userDao, RecoveryStorage recoveryStorage) {
        this.recoveryStorage = recoveryStorage;
        this.mailService = mailService;
        this.userDao = userDao;
        noCache = new CacheControl();
        noCache.setNoCache(true);
    }

    /**
     * Sends mail for password restoring
     * <p/>
     * <table>
     * <tr>
     * <th>Status</th>
     * <th>Error description</th>
     * </tr>
     * <tr>
     * <td>404</td>
     * <td>specified user is not registered</td>
     * </tr>
     * <tr>
     * <td>500</td>
     * <td>problem with user database</td>
     * </tr>
     * <tr>
     * <td>500</td>
     * <td>problems on email sending</td>
     * </tr>
     * </table>
     *
     * @param userMail
     *         - the identifier of user
     * @return - the Response with corresponded status (200)
     */
    @POST
    @Path("recover/{usermail}")
    public Response recoverPassword(@PathParam("usermail") String userMail, @Context UriInfo uriInfo)
            throws ServerException, NotFoundException {
        try {
            try {
                userDao.getByAlias(userMail);
            } catch (NotFoundException e) {
                throw new NotFoundException("User " + userMail + " is not registered in the system.");
            }

            String uuid = recoveryStorage.setValidationData(userMail);

            Map<String, String> props = new HashMap<>();
            props.put("com.codenvy.masterhost.url", uriInfo.getBaseUriBuilder().replacePath(null).build().toString());
            props.put("id", uuid);

            String timeLimitationMessage =
                    RecoveryStorage.VALIDATION_MAX_AGE_IN_MILLISECONDS > 0 ? TimeUnit.MILLISECONDS.toHours(
                            RecoveryStorage.VALIDATION_MAX_AGE_IN_MILLISECONDS)
                                                                             + " hour" : "unlimited time";

            props.put("validation.token.age.message", timeLimitationMessage);

            mailService.sendMail(mailSender, userMail, null, recoverMailSubject, MediaType.TEXT_HTML,
                                 readAndCloseQuietly(getResource(MAIL_TEMPLATE)), props);

            return Response
                    .ok(String.format("We sent you instructions by email to the '%1$s'.", userMail), MediaType.TEXT_PLAIN)
                    .cacheControl(noCache).build();
        }
        // TODO review logic
        catch (IOException | MessagingException  e) {
            LOG.error("Error during recovering users password", e);
            return Response.status(500)
                           .entity("Unable to recover password. Please contact with administrators or try " + "later.").build();
        }
    }

    /**
     * Verify setup password confirmation token.
     * <p/>
     * <table>
     * <tr>
     * <th>Status</th>
     * <th>Error description</th>
     * </tr>
     * <tr>
     * <td>403</td>
     * <td>Setup password token is incorrect or has expired</td>
     * </tr>
     * </table>
     *
     * @param uuid
     *         - token of setup password operation
     * @return - the Response with corresponded status (200) and userName as
     * String in body
     */
    @GET
    @Path("verify/{uuid}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response setupConfirmation(@PathParam("uuid") String uuid) {
        if (!recoveryStorage.isValid(uuid)) {
            LOG.warn("Setup password token is incorrect or has expired");

            // remove invalid validationData
            recoveryStorage.remove(uuid);

            return Response.status(403).entity("Setup password token is incorrect or has expired")
                           .type(MediaType.TEXT_PLAIN).build();
        } else {
            // return user id from stored validation data
            Map<String, String> validationData = recoveryStorage.get(uuid);
            String userName = validationData.get("user.name");
            return Response.ok(userName).cacheControl(noCache).build();
        }
    }

    /**
     * Setup users password after verifying setup password confirmation token
     * <p/>
     * <table>
     * <tr>
     * <th>Status</th>
     * <th>Error description</th>
     * </tr>
     * <tr>
     * <td>403</td>
     * <td>Setup password token is incorrect or has expired</td>
     * </tr>
     * <tr>
     * <td>404</td>
     * <td>User is not registered in the system</td>
     * </tr>
     * <tr>
     * <td>500</td>
     * <td>Impossible to setup password</td>
     * </tr>
     * <p/>
     * <p/>
     * </table>
     *
     * @param uuid
     *         - token of setup password operation
     * @param newPassword
     *         - new users password
     * @return - the Response with corresponded status (200)
     */
    @POST
    @Path("setup")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response setupPassword(@FormParam("uuid") String uuid, @FormParam("password") String newPassword)
       throws NotFoundException, ServerException {
        // verify is confirmationId valid
        if (!recoveryStorage.isValid(uuid)) {
            LOG.warn("Setup password token is incorrect or has expired");

            // remove invalid validationData
            recoveryStorage.remove(uuid);

            return Response.status(403).entity("Setup password token is incorrect or has expired").build();
        }


        // find user and setup his/her password
        String userName = recoveryStorage.get(uuid).get("user.name");

        try {
            User user = userDao.getByAlias(userName);
            user.setPassword(newPassword);
            userDao.update(user);
        } catch (NotFoundException e) {
            // remove invalid validationData
            recoveryStorage.remove(uuid);

            throw new NotFoundException("User " + userName + " is not registered in the system.");
        }  catch (ServerException e) {
            LOG.error("Error during setting user's password", e);
            throw new ServerException("Unable to setup password. Please contact with administrators.", e);
        }

        // remove validation data from validationStorage
        recoveryStorage.remove(uuid);

        return Response.ok().build();
    }
}
