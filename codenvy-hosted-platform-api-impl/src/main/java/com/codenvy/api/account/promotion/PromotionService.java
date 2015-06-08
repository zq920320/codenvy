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
package com.codenvy.api.account.promotion;

import com.codenvy.api.account.billing.Bonus;
import com.codenvy.api.account.billing.BonusDao;
import com.codenvy.api.account.impl.shared.dto.PromotionRequest;
import com.codenvy.api.account.subscription.service.util.SubscriptionMailSender;
import com.wordnik.swagger.annotations.Api;

import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.UserDao;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author Max Shaposhnik
 */
@Api(value = "/promotion",
        description = "Promotion service")
@Path("/promotion")
public class PromotionService extends Service {

    private final AccountDao             accountDao;
    private final BonusDao               bonusDao;
    private final PreferenceDao          preferenceDao;
    private final UserDao                userDao;
    private final SubscriptionMailSender mailSender;
    private final double                 defaultBonusSize;

    @Inject
    public PromotionService(@Named("promotion.bonus.resources.gb") Double bonusSize,
                            AccountDao accountDao,
                            PreferenceDao preferenceDao,
                            BonusDao promotionDao,
                            UserDao userDao,
                            SubscriptionMailSender mailSender) {
        this.accountDao = accountDao;
        this.preferenceDao = preferenceDao;
        this.bonusDao = promotionDao;
        this.userDao = userDao;
        this.mailSender = mailSender;
        this.defaultBonusSize = bonusSize;
    }


    @Path("add")
    @POST
    @RolesAllowed({"system/admin", "system/manager"})
    @Consumes(APPLICATION_JSON)
    public Response addPromotion(PromotionRequest promotionRequest)
            throws ServerException, ForbiddenException, NotFoundException, ConflictException {
        if (promotionRequest == null) {
            throw new ForbiddenException("Request must contain promotion object.");
        }

        if (promotionRequest.getRecipientEmail() == null || promotionRequest.getSenderEmail() == null) {
            throw new ForbiddenException("Both sender and recipient emails should be set.");
        }

        if (promotionRequest.getRecipientEmail().equals(promotionRequest.getSenderEmail())) {
            throw new ForbiddenException("Sender and recipient emails cannot be the same.");
        }

        String recipientId = userDao.getByAlias(promotionRequest.getRecipientEmail()).getId();

        Map<String, String> preferences = preferenceDao.getPreferences(recipientId);
        if (Long.parseLong(preferences.get("codenvy:created")) < promotionRequest.getTimestamp()) {
            throw new ConflictException(
                    String.format("User %s is registered before promotion was send.", promotionRequest.getRecipientEmail()));
        }
        // This may happens when 2 users invited him,  so me must prevent 2nd time bonus.
        if (Boolean.parseBoolean(preferences.get("codenvy:has_promotion"))) {
            throw new ConflictException(String.format("User %s is already used promotion bonus.", promotionRequest.getRecipientEmail()));
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(3000, Calendar.JANUARY, 1);

        String senderId = userDao.getByAlias(promotionRequest.getSenderEmail()).getId();
        Bonus senderBonus = new Bonus().withAccountId(accountDao.getByOwner(senderId).get(0).getId())
                                       .withFromDate(System.currentTimeMillis())
                                       .withTillDate(calendar.getTimeInMillis())
                                       .withResources(
                                               promotionRequest.getBonusSize() != null ? promotionRequest.getBonusSize() : defaultBonusSize)
                                       .withCause("Promotion bonus as inviter.");
        Bonus recipientBonus = new Bonus().withAccountId(accountDao.getByOwner(recipientId).get(0).getId())
                                              .withFromDate(System.currentTimeMillis())
                                              .withTillDate(calendar.getTimeInMillis())
                                              .withCause("promotion bonus as invited")
                                              .withResources(
                                                      promotionRequest.getBonusSize() != null ? promotionRequest.getBonusSize()
                                                                                              : defaultBonusSize);
        bonusDao.create(senderBonus);
        mailSender.sendReferringBonusNotification(senderBonus);
        bonusDao.create(recipientBonus);
        preferences.put("codenvy:has_promotion", "true");
        preferenceDao.setPreferences(recipientId, preferences);
        mailSender.sendReferredBonusNotification(recipientBonus);
        return Response.ok().build();
    }
}
