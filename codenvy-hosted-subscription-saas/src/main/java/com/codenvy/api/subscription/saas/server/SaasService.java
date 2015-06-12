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
package com.codenvy.api.subscription.saas.server;

import com.codenvy.api.subscription.saas.server.billing.BillingPeriod;
import com.codenvy.api.subscription.saas.server.billing.BillingService;
import com.codenvy.api.subscription.saas.server.billing.Bonus;
import com.codenvy.api.subscription.saas.server.billing.BonusFilter;
import com.codenvy.api.subscription.saas.server.billing.InvoiceService;
import com.codenvy.api.subscription.saas.server.billing.Period;
import com.codenvy.api.subscription.saas.server.billing.ResourcesFilter;
import com.codenvy.api.subscription.saas.server.dao.BonusDao;
import com.codenvy.api.subscription.saas.server.dao.MeterBasedStorage;
import com.codenvy.api.subscription.saas.server.dao.sql.LockDao;
import com.codenvy.api.subscription.saas.server.service.util.SubscriptionMailSender;
import com.codenvy.api.subscription.saas.shared.dto.AccountResources;
import com.codenvy.api.subscription.saas.shared.dto.BonusDescriptor;
import com.codenvy.api.subscription.saas.shared.dto.NewBonus;
import com.codenvy.api.subscription.saas.shared.dto.PromotionRequest;
import com.codenvy.api.subscription.saas.shared.dto.Resources;
import com.codenvy.api.subscription.server.dao.Subscription;
import com.codenvy.api.subscription.server.dao.SubscriptionDao;
import com.codenvy.api.subscription.shared.dto.ProvidedResourcesDescriptor;
import com.codenvy.api.subscription.shared.dto.WorkspaceResources;
import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import org.eclipse.che.api.account.server.AccountService;
import org.eclipse.che.api.account.server.Constants;
import org.eclipse.che.api.account.server.dao.Account;
import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.account.shared.dto.AccountDescriptor;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.util.LinksHelper;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.eclipse.che.dto.server.DtoFactory;

import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.codenvy.api.subscription.saas.server.SaasSubscriptionService.SAAS_SUBSCRIPTION_ID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author Sergii Leschenko
 */
@Beta
@Path("/saas")
public class SaasService extends Service {
    private final double                 defaultBonusSize;
    private final BillingService         billingService;
    private final BillingPeriod          billingPeriod;
    private final AccountDao             accountDao;
    private final LockDao                lockDao;
    private final SubscriptionDao        subscriptionDao;
    private final BonusDao               bonusDao;
    private final PreferenceDao          preferenceDao;
    private final UserDao                userDao;
    private final SubscriptionMailSender mailSender;
    private final MeterBasedStorage      meterBasedStorage;
    private final WorkspaceDao           workspaceDao;

    @Inject
    public SaasService(@Named("promotion.bonus.resources.gb") Double bonusSize,
                       BillingService billingService,
                       BillingPeriod billingPeriod,
                       AccountDao accountDao,
                       LockDao lockDao,
                       SubscriptionDao subscriptionDao,
                       BonusDao bonusDao,
                       PreferenceDao preferenceDao,
                       UserDao userDao,
                       SubscriptionMailSender mailSender,
                       MeterBasedStorage meterBasedStorage,
                       WorkspaceDao workspaceDao) {
        this.defaultBonusSize = bonusSize;
        this.billingService = billingService;
        this.billingPeriod = billingPeriod;
        this.accountDao = accountDao;
        this.lockDao = lockDao;
        this.subscriptionDao = subscriptionDao;
        this.bonusDao = bonusDao;
        this.preferenceDao = preferenceDao;
        this.userDao = userDao;
        this.mailSender = mailSender;
        this.meterBasedStorage = meterBasedStorage;
        this.workspaceDao = workspaceDao;
    }

    @POST
    @Path("/bonus")
    @Produces(APPLICATION_JSON)
    @RolesAllowed({"system/admin", "system/manager"})
    public BonusDescriptor createBonus(NewBonus newBonus) throws ServerException, NotFoundException, ForbiddenException {
        requiredNotNull(newBonus, "Bonus");
        requiredNotNull(newBonus.getAccountId(), "Account id");
        requiredNotNull(newBonus.getResources(), "Value of resources");
        requiredNotNull(newBonus.getFromDate(), "From date");
        requiredNotNull(newBonus.getTillDate(), "Till date");
        requiredNotNull(newBonus.getCause(), "Cause");

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
            endPeriod = billingPeriod.getCurrent().getEndDate().getTime();
        }

        final List<Bonus> bonuses = bonusDao.getBonuses(BonusFilter.builder()
                                                                   .withAccountId(accountId)
                                                                   .withFromDate(startPeriod)
                                                                   .withTillDate(endPeriod)
                                                                   .withMaxItems(maxItems)
                                                                   .withSkipCount(skipCount)
                                                                   .withCause(cause)
                                                                   .build());

        return FluentIterable.from(bonuses)
                             .transform(new Function<Bonus, BonusDescriptor>() {
                                 @Override
                                 public BonusDescriptor apply(Bonus recipe) {
                                     return toDescriptor(recipe);
                                 }
                             })
                             .toList();
    }

    @DELETE
    @Path("/bonus/{id}")
    @RolesAllowed({"system/admin", "system/manager"})
    public void removeBonus(@PathParam("id") Long bonusId) throws ServerException {
        bonusDao.remove(bonusId, System.currentTimeMillis());
    }

    /**
     * Returns resources which are provided by saas service
     *
     * @param accountId
     *         account id
     */
    @ApiOperation(value = "Get resources which are provided by saas service",
            notes = "Returns used resources, provided by saas service. Roles: account/owner, account/member, system/manager, system/admin.",
            position = 17)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/resources/{accountId}/provided")
    @RolesAllowed({"account/owner", "account/member", "system/manager", "system/admin"})
    @Produces(MediaType.APPLICATION_JSON)
    public ProvidedResourcesDescriptor getProvidedResources(@ApiParam(value = "Account ID", required = true)
                                                            @PathParam("accountId") String accountId) throws ServerException,
                                                                                                             NotFoundException,
                                                                                                             ConflictException {
        ProvidedResourcesDescriptor result = DtoFactory.getInstance().createDto(ProvidedResourcesDescriptor.class);
        Period current = billingPeriod.getCurrent();
        result.withFreeAmount(billingService.getProvidedFreeResources(accountId,
                                                                      current.getStartDate().getTime(),
                                                                      current.getEndDate().getTime()));

        final Subscription activeSaasSubscription = subscriptionDao.getActiveByServiceId(accountId, SAAS_SUBSCRIPTION_ID);
        String prepaidAmount;
        if (activeSaasSubscription != null && (prepaidAmount = activeSaasSubscription.getProperties().get("PrepaidGbH")) != null) {
            result.setPrepaidAmount(Double.parseDouble(prepaidAmount));
        } else {
            result.setPrepaidAmount(0D);
        }

        return result;
    }

    /**
     * Returns used resources, provided by saas service
     *
     * @param accountId
     *         account id
     */
    @ApiOperation(value = "Get used resources, provided by saas service",
            notes = "Returns used resources, provided by saas service. Roles: account/owner, account/member, system/manager, system/admin.",
            position = 17)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/resources/{accountId}/used")
    @RolesAllowed({"account/owner", "account/member", "system/manager", "system/admin"})
    @Produces(MediaType.APPLICATION_JSON)
    public List<WorkspaceResources> getUsedResources(@ApiParam(value = "Account ID", required = true)//TODO Add paging
                                                     @PathParam("accountId") String accountId) throws ServerException,
                                                                                                      NotFoundException,
                                                                                                      ConflictException {
        final Map<String, Double> memoryUsedReport = meterBasedStorage.getMemoryUsedReport(accountId,
                                                                                           billingPeriod.getCurrent().getStartDate()
                                                                                                        .getTime(),
                                                                                           System.currentTimeMillis());

        List<Workspace> workspaces = workspaceDao.getByAccount(accountId);
        for (Workspace workspace : workspaces) {
            if (!memoryUsedReport.containsKey(workspace.getId())) {
                memoryUsedReport.put(workspace.getId(), 0D);
            }
        }

        List<WorkspaceResources> result = new ArrayList<>();
        for (Map.Entry<String, Double> usedMemory : memoryUsedReport.entrySet()) {
            result.add(DtoFactory.getInstance().createDto(WorkspaceResources.class)
                                 .withWorkspaceId(usedMemory.getKey())
                                 .withMemory(usedMemory.getValue()));
        }

        return result;
    }

    /**
     * Returns locked accounts list.
     *
     */
    @ApiOperation(value = "Get locked accounts list",
            notes = "Returns locked accounts list. Roles: system/manager, system/admin.",
            position = 17)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/locked")
    @Produces(APPLICATION_JSON)
    @RolesAllowed({"system/admin", "system/manager"})
    public List<AccountDescriptor> getLockedAccounts(@DefaultValue("20") @QueryParam("maxItems") int maxItems,
                                                     @QueryParam("skipCount") int skipCount) throws ServerException, ForbiddenException {

        return FluentIterable.from(lockDao.getAccountsWithLockedResources()).transform(
                new Function<Account, AccountDescriptor>() {
                    @Nullable
                    @Override
                    public AccountDescriptor apply(Account input) {
                        return toDescriptor(input);
                    }
                }).skip(skipCount).limit(maxItems).toList();
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
            endPeriod = billingPeriod.getCurrent().getEndDate().getTime();
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

    @Path("/promotion")
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
                                       .withResources(promotionRequest.getBonusSize() != null ? promotionRequest.getBonusSize()
                                                                                              : defaultBonusSize)
                                       .withCause("Email invites");
        Bonus recipientBonus = new Bonus().withAccountId(accountDao.getByOwner(recipientId).get(0).getId())
                                          .withFromDate(System.currentTimeMillis())
                                          .withTillDate(calendar.getTimeInMillis())
                                          .withResources(promotionRequest.getBonusSize() != null ? promotionRequest.getBonusSize()
                                                                                                 : defaultBonusSize)
                                          .withCause("Email invites");
        bonusDao.create(senderBonus);
        mailSender.sendReferringBonusNotification(senderBonus);
        bonusDao.create(recipientBonus);
        preferences.put("codenvy:has_promotion", "true");
        preferenceDao.setPreferences(recipientId, preferences);
        mailSender.sendReferredBonusNotification(recipientBonus);
        return Response.ok().build();
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

    private AccountDescriptor toDescriptor(Account account) {
        final UriBuilder baseUriBuilder = getServiceContext().getBaseUriBuilder();
        final List<Link> links = new LinkedList<>();
        links.add(LinksHelper.createLink(HttpMethod.GET,
                                         baseUriBuilder.clone()
                                                       .path(AccountService.class)
                                                       .path(AccountService.class, "getMembers")
                                                       .build(account.getId())
                                                       .toString(),
                                         null,
                                         MediaType.APPLICATION_JSON,
                                         Constants.LINK_REL_GET_MEMBERS));
        links.add(LinksHelper.createLink(HttpMethod.GET,
                                         baseUriBuilder.clone()
                                                       .path(AccountService.class)
                                                       .path(AccountService.class, "getById")
                                                       .build(account.getId())
                                                       .toString(),
                                         null,
                                         MediaType.APPLICATION_JSON,
                                         Constants.LINK_REL_GET_ACCOUNT_BY_ID));
        links.add(LinksHelper.createLink(HttpMethod.GET,
                                         baseUriBuilder.clone()
                                                       .path(InvoiceService.class)
                                                       .path(InvoiceService.class, "getInvoices")
                                                       .queryParam("accountId", account.getId())
                                                       .build()
                                                       .toString(),
                                         null,
                                         MediaType.APPLICATION_JSON,
                                         "get invoices"));
        account.getAttributes().remove("codenvy:creditCardToken");
        account.getAttributes().remove("codenvy:billing.date");
        return DtoFactory.getInstance().createDto(AccountDescriptor.class)
                         .withId(account.getId())
                         .withName(account.getName())
                         .withAttributes(account.getAttributes())
                         .withLinks(links);
    }
}
