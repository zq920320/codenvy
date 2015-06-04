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
package com.codenvy.api.account.billing;

import com.codenvy.api.account.impl.shared.dto.AccountResources;
import com.codenvy.api.account.impl.shared.dto.BonusDescriptor;
import com.codenvy.api.account.impl.shared.dto.NewBonus;
import com.codenvy.api.account.impl.shared.dto.Resources;
import com.codenvy.api.account.subscription.service.util.SubscriptionMailSender;
import com.google.common.annotations.Beta;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.util.LinksHelper;
import org.eclipse.che.dto.server.DtoFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.UriBuilder;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

@Beta
@Path("/billing")
public class BillingRestService extends Service {
    private final BillingService         billingService;
    private final BillingPeriod          billingPeriod;
    private final BonusDao               bonusDao;
    private final SubscriptionMailSender mailSender;

    @Inject
    public BillingRestService(BillingService billingService,
                              BillingPeriod billingPeriod,
                              BonusDao bonusDao,
                              SubscriptionMailSender mailSender) {
        this.billingService = billingService;
        this.billingPeriod = billingPeriod;
        this.bonusDao = bonusDao;
        this.mailSender = mailSender;
    }

    @POST
    @Path("/bonus")
    @Produces(APPLICATION_JSON)
    @RolesAllowed({"system/admin", "system/manager"})
    public BonusDescriptor createBonus(NewBonus newBonus) throws ServerException, NotFoundException, ForbiddenException {
        requiredNotNull(newBonus.getAccountId(), "Account id");
        requiredNotNull(newBonus.getResources(), "Value of resources");
        requiredNotNull(newBonus.getFromDate(), "From date");
        requiredNotNull(newBonus.getTillDate(), "Till date");
        requiredNotNull(newBonus.getCause(), "Cause ");

        Bonus result = bonusDao.create(new Bonus().withAccountId(newBonus.getAccountId())
                                                  .withResources(newBonus.getResources())
                                                  .withFromDate(newBonus.getFromDate())
                                                  .withTillDate(newBonus.getTillDate())
                                                  .withCause(newBonus.getCause()));
        mailSender.sendBonusNotification(result);
        return toDescriptor(result);
    }

    @GET
    @Path("/bonus")
    @Produces(APPLICATION_JSON)
    @RolesAllowed({"system/admin", "system/manager"})
    public List<BonusDescriptor> getBonuses(@QueryParam("startPeriod") Long startPeriod,
                                            @QueryParam("endPeriod") Long endPeriod,
                                            @QueryParam("accountId") String accountId,
                                            @QueryParam("cause") String cause,
                                            @DefaultValue("20") @QueryParam("maxItems") int maxItems,
                                            @QueryParam("skipCount") int skipCount) throws ServerException {
        if (startPeriod == null) {
            startPeriod = billingPeriod.getCurrent().getStartDate().getTime();
        }

        if (endPeriod == null) {
            endPeriod = System.currentTimeMillis();
        }

        final List<Bonus> bonuses = bonusDao.getBonuses(BonusFilter.builder()
                                                                   .withAccountId(accountId)
                                                                   .withFromDate(startPeriod)
                                                                   .withFromDate(endPeriod)
                                                                   .withMaxItems(maxItems)
                                                                   .withSkipCount(skipCount)
                                                                   .withCause(cause)
                                                                   .build());
        List<BonusDescriptor> result = new ArrayList<>(bonuses.size());
        for (Bonus bonus : bonuses) {
            result.add(toDescriptor(bonus));
        }

        return result;
    }

    @DELETE
    @Path("/bonus/{id}")
    @Produces(APPLICATION_JSON)
    @RolesAllowed({"system/admin", "system/manager"})
    public void removeBonus(@PathParam("id") Long bonusId) throws ServerException {
        bonusDao.remove(bonusId, System.currentTimeMillis());
    }

    @GET
    @Path("/resources")
    @Produces(APPLICATION_JSON)
    @RolesAllowed({"system/admin", "system/manager"})
    public Resources getEstimatedResources(@QueryParam("startPeriod") Long startPeriod,
                                           @QueryParam("endPeriod") Long endPeriod) throws ServerException {
        if (startPeriod == null) {
            startPeriod = billingPeriod.getCurrent().getStartDate().getTime();
        }

        if (endPeriod == null) {
            endPeriod = System.currentTimeMillis();
        }

        return billingService.getEstimatedUsage(startPeriod, endPeriod);
    }

    @GET
    @Path("/resources/accounts")
    @Produces(APPLICATION_JSON)
    @RolesAllowed({"system/admin", "system/manager"})
    public List<AccountResources> getEstimatedResourcesByAccounts(@QueryParam("startPeriod") Long startPeriod,
                                                                  @QueryParam("endPeriod") Long endPeriod,
                                                                  @DefaultValue("-1") @QueryParam("maxItems") int maxItems,
                                                                  @QueryParam("skipCount") int skipCount,
                                                                  @QueryParam("freeGbH") Double freeGbH,
                                                                  @QueryParam("paidGbH") Double paidGbH,
                                                                  @QueryParam("prepaidGbH") Double prepaidGbH,
                                                                  @QueryParam("accountId") String accountId) throws ServerException {
        if (startPeriod == null) {
            startPeriod = billingPeriod.getCurrent().getStartDate().getTime();
        }

        if (endPeriod == null) {
            endPeriod = System.currentTimeMillis();
        }

        ResourcesFilter.Builder builder = ResourcesFilter.builder()
                                                         .withFromDate(startPeriod)
                                                         .withTillDate(endPeriod)
                                                         .withSkipCount(skipCount)
                                                         .withMaxItems(maxItems)
                                                         .withAccountId(accountId);

        if (freeGbH != null) {
            builder.withFreeGbHMoreThan(freeGbH);
        }

        if (prepaidGbH != null) {
            builder.withPrePaidGbHMoreThan(prepaidGbH);
        }
        if (paidGbH != null) {
            builder.withPaidGbHMoreThan(paidGbH);
        }

        return billingService.getEstimatedUsageByAccount(builder.build());
    }

    @POST
    @Path("/invoices/generate")
    @Produces({TEXT_PLAIN})
    @RolesAllowed({"system/admin", "system/manager"})
    public String generateInvoices(@QueryParam("startPeriod") Long startPeriod,
                                   @QueryParam("endPeriod") Long endPeriod) throws ServerException {
        if (startPeriod == null) {
            startPeriod = billingPeriod.getCurrent().getPreviousPeriod().getStartDate().getTime();
        }

        if (endPeriod == null) {
            endPeriod = billingPeriod.getCurrent().getPreviousPeriod().getEndDate().getTime();
        }

        return Integer.toString(billingService.generateInvoices(startPeriod, endPeriod));
    }

    /**
     * Checks object reference is not {@code null}
     *
     * @param object
     *         object reference to check
     * @param subject
     *         used as subject of exception message "{subject} required"
     * @throws ForbiddenException
     *         when object reference is {@code null}
     */
    private void requiredNotNull(Object object, String subject) throws ForbiddenException {
        if (object == null) {
            throw new ForbiddenException(subject + " required");
        }
    }

    private BonusDescriptor toDescriptor(Bonus bonus) {
        final UriBuilder serviceUriBuilder = getServiceContext().getServiceUriBuilder();
        List<Link> links = new ArrayList<>(2);
        links.add(LinksHelper.createLink("GET",
                                         serviceUriBuilder.clone()
                                                          .path(getClass(), "getBonuses")
                                                          .queryParam("accountId", bonus.getAccountId())
                                                          .build()
                                                          .toString(),
                                         null,
                                         APPLICATION_JSON,
                                         "get account bonuses"));

        links.add(LinksHelper.createLink("DELETE",
                                         serviceUriBuilder.clone()
                                                          .path(getClass(), "removeBonus")
                                                          .build(bonus.getId())
                                                          .toString(),
                                         null,
                                         APPLICATION_JSON,
                                         "remove bonus"));

        return DtoFactory.getInstance().createDto(BonusDescriptor.class)
                         .withId(bonus.getId())
                         .withAccountId(bonus.getAccountId())
                         .withResources(bonus.getResources())
                         .withFromDate(bonus.getFromDate())
                         .withTillDate(bonus.getTillDate())
                         .withCause(bonus.getCause())
                         .withLinks(links);
    }
}
