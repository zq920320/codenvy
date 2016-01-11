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
package com.codenvy.api.subscription.server;

import com.codenvy.api.subscription.server.dao.PlanDao;
import com.codenvy.api.subscription.server.dao.Subscription;
import com.codenvy.api.subscription.server.dao.SubscriptionDao;
import com.codenvy.api.subscription.shared.dto.NewSubscription;
import com.codenvy.api.subscription.shared.dto.Plan;
import com.codenvy.api.subscription.shared.dto.SubscriptionDescriptor;
import com.codenvy.api.subscription.shared.dto.SubscriptionState;
import com.google.common.annotations.Beta;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.account.server.dao.Member;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.api.core.rest.annotations.Required;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.util.LinksHelper;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Subscription API
 *
 * @author Sergii Leschenko
 */
@Beta
@Path("/subscription")
public class SubscriptionService extends Service {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionService.class);

    private final AccountDao                  accountDao;
    private final SubscriptionDao             subscriptionDao;
    private final SubscriptionServiceRegistry registry;
    private final PlanDao                     planDao;

    @Inject
    public SubscriptionService(AccountDao accountDao,
                               SubscriptionDao subscriptionDao,
                               SubscriptionServiceRegistry registry,
                               PlanDao planDao) {
        this.accountDao = accountDao;
        this.subscriptionDao = subscriptionDao;
        this.registry = registry;
        this.planDao = planDao;
    }

    /**
     * <p>Creates new subscription. Returns {@link SubscriptionDescriptor}
     * when subscription has been created successfully.
     * <p>Each new subscription should contain plan id and account id </p>
     *
     * @param newSubscription
     *         new subscription
     * @return descriptor of created subscription
     * @throws ConflictException
     *         when new subscription is {@code null}
     *         or new subscription plan identifier is {@code null}
     *         or new subscription account identifier is {@code null}
     * @throws NotFoundException
     *         if plan with certain identifier is not found
     * @throws org.eclipse.che.api.core.ApiException
     * @see SubscriptionDescriptor
     * @see #getById(String, SecurityContext)
     * @see #deactivate(String, SecurityContext)
     */
    @Beta
    @ApiOperation(value = "Add new subscription",
                  notes = "Add a new subscription to an account. JSON with subscription details is sent. Roles: account/owner, system/admin.",
                  response = SubscriptionDescriptor.class,
                  position = 1)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "CREATED"),
            @ApiResponse(code = 400, message = "Invalid subscription parameter"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 409, message = "Unknown ServiceID is used or payment token is invalid"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @POST
    @GenerateLink(rel = Constants.LINK_REL_ADD_SUBSCRIPTION)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@ApiParam(value = "Subscription details", required = true)
                           @Required NewSubscription newSubscription,
                           @Context SecurityContext securityContext)
            throws ApiException {
        requiredNotNull(newSubscription, "New subscription");
        requiredNotNull(newSubscription.getAccountId(), "Account identifier");
        requiredNotNull(newSubscription.getPlanId(), "Plan identifier");
        requiredNotNull(newSubscription.getUsePaymentSystem(), "Use payment system");

        //check user has access to add subscription
        final Set<String> roles = new HashSet<>();
        if (securityContext.isUserInRole("user")) {
            roles.addAll(resolveRolesForSpecificAccount(newSubscription.getAccountId()));
            if (!roles.contains("account/owner")) {
                throw new ForbiddenException("Access denied");
            }
        }

        final Plan plan = planDao.getPlanById(newSubscription.getPlanId());

        // check service exists
        final AbstractSubscriptionService service = registry.get(plan.getServiceId());
        if (null == service) {
            throw new ConflictException("Unknown serviceId is used");
        }

        //Not admin has additional restrictions
        if (!securityContext.isUserInRole("system/admin") && !securityContext.isUserInRole("system/manager")) {
            // check that subscription is allowed for not admin
            if (plan.getSalesOnly()) {
                throw new ForbiddenException("User not authorized to add this subscription, please contact support");
            }

            // only admins are allowed to disable payment on subscription addition
            if (!newSubscription.getUsePaymentSystem().equals(plan.isPaid())) {
                throw new ConflictException("Given value of attribute usePaymentSystem is not allowed");
            }

            //only admins can override properties
            if (!newSubscription.getProperties().isEmpty()) {
                throw new ForbiddenException("User not authorized to add subscription with custom properties, please contact support");
            }
        }

        // disable payment if subscription is free
        if (!plan.isPaid()) {
            newSubscription.setUsePaymentSystem(false);
        }

        //preparing properties
        Map<String, String> properties = plan.getProperties();
        Map<String, String> customProperties = newSubscription.getProperties();
        for (Map.Entry<String, String> propertyEntry : customProperties.entrySet()) {
            if (properties.containsKey(propertyEntry.getKey())) {
                properties.put(propertyEntry.getKey(), propertyEntry.getValue());
            } else {
                throw new ForbiddenException("Forbidden overriding of non-existent plan properties");
            }
        }

        //create new subscription
        Subscription subscription = new Subscription()
                .withId(NameGenerator.generate(Subscription.class.getSimpleName().toLowerCase(), Constants.ID_LENGTH))
                .withAccountId(newSubscription.getAccountId())
                .withUsePaymentSystem(newSubscription.getUsePaymentSystem())
                .withServiceId(plan.getServiceId())
                .withPlanId(plan.getId())
                .withProperties(properties)
                .withDescription(plan.getDescription())
                .withBillingCycleType(plan.getBillingCycleType())
                .withBillingCycle(plan.getBillingCycle())
                .withBillingContractTerm(plan.getBillingContractTerm())
                .withState(SubscriptionState.ACTIVE);

        service.beforeCreateSubscription(subscription);

        LOG.info("Add subscription# id#{}# userId#{}# accountId#{}# planId#{}#",
                 subscription.getId(),
                 EnvironmentContext.getCurrent().getUser().getId(),
                 subscription.getAccountId(),
                 subscription.getPlanId());

        subscriptionDao.create(subscription);

        service.afterCreateSubscription(subscription);

        LOG.info("Added subscription. Subscription ID #{}# Account ID #{}#", subscription.getId(), subscription.getAccountId());

        return Response.status(Response.Status.CREATED)
                       .entity(toDescriptor(subscription, securityContext, roles))
                       .build();
    }


    /**
     * Returns list of subscriptions descriptors for certain account.
     * If service identifier is provided returns subscriptions that matches provided service.
     *
     * @param accountId
     *         account identifier
     * @param serviceId
     *         service identifier
     * @return subscriptions descriptors
     * @throws NotFoundException
     *         when account with given identifier doesn't exist
     * @throws ServerException
     *         when some error occurred while retrieving subscriptions
     * @see SubscriptionDescriptor
     */
    @Beta
    @ApiOperation(value = "Get account subscriptions",
                  notes = "Get information on account subscriptions. This API call requires account/owner, account/member, system/admin or system/manager role.",
                  response = SubscriptionDescriptor.class,
                  responseContainer = "List",
                  position = 2)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Account ID not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/find/account")
    @RolesAllowed({"user", "system/admin", "system/manager"})
    @Produces(MediaType.APPLICATION_JSON)
    public List<SubscriptionDescriptor> getActive(@ApiParam(value = "Account ID", required = true)
                                                  @QueryParam("id") String accountId,
                                                  @ApiParam(value = "Service ID", required = false)
                                                  @QueryParam("service") String serviceId,
                                                  @Context SecurityContext securityContext)
            throws NotFoundException, ServerException, ForbiddenException, BadRequestException {
        requiredNotNull(accountId, "Account Id");
        Set<String> roles = new HashSet<>();
        if (securityContext.isUserInRole("user")) {
            Set<String> accountRoles = resolveRolesForSpecificAccount(accountId);
            if (!accountRoles.contains("account/member") && !accountRoles.contains("account/owner")) {
                throw new ForbiddenException("Access denied");
            }
            roles.addAll(accountRoles);
        }

        final List<Subscription> subscriptions = new ArrayList<>();
        if (serviceId == null || serviceId.isEmpty()) {
            subscriptions.addAll(subscriptionDao.getActive(accountId));
        } else {
            final Subscription activeSubscription = subscriptionDao.getActiveByServiceId(accountId, serviceId);
            if (activeSubscription != null) {
                subscriptions.add(activeSubscription);
            }
        }
        final List<SubscriptionDescriptor> result = new ArrayList<>(subscriptions.size());
        for (Subscription subscription : subscriptions) {
            result.add(toDescriptor(subscription, securityContext, roles));
        }
        return result;
    }

    /**
     * Returns {@link SubscriptionDescriptor} for subscription with given identifier.
     *
     * @param subscriptionId
     *         subscription identifier
     * @return descriptor of subscription
     * @throws NotFoundException
     *         when subscription with given identifier doesn't exist
     * @throws ForbiddenException
     *         when user hasn't access to call this method
     * @see SubscriptionDescriptor
     * @see #getActive(String, String serviceId, SecurityContext)
     * @see #deactivate(String, SecurityContext)
     */
    @Beta
    @ApiOperation(value = "Get subscription details",
                  notes = "Get information on a particular subscription by its unique ID.",
                  response = SubscriptionDescriptor.class,
                  position = 3)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 403, message = "User not authorized to call this method"),
            @ApiResponse(code = 404, message = "Account ID not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/{subscriptionId}")
    @RolesAllowed({"user", "system/admin", "system/manager"})
    @Produces(MediaType.APPLICATION_JSON)
    public SubscriptionDescriptor getById(@ApiParam(value = "Subscription ID", required = true)
                                          @PathParam("subscriptionId") String subscriptionId,
                                          @Context SecurityContext securityContext) throws NotFoundException,
                                                                                           ServerException,
                                                                                           ForbiddenException {
        final Subscription subscription = subscriptionDao.getById(subscriptionId);
        Set<String> roles = new HashSet<>();
        if (securityContext.isUserInRole("user")) {
            roles = resolveRolesForSpecificAccount(subscription.getAccountId());
            if (!roles.contains("account/owner") && !roles.contains("account/member")) {
                throw new ForbiddenException("Access denied");
            }
        }
        return toDescriptor(subscription, securityContext, roles);
    }

    /**
     * Removes subscription by id. Actually makes it inactive.
     *
     * @param subscriptionId
     *         id of the subscription to remove
     * @throws NotFoundException
     *         if subscription with such id is not found
     * @throws ForbiddenException
     *         if user hasn't permissions
     * @throws ServerException
     *         if internal server error occurs
     * @throws org.eclipse.che.api.core.ApiException
     * @see #create(NewSubscription, SecurityContext)
     * @see #getActive(String, String, SecurityContext)
     */
    @Beta
    @ApiOperation(value = "Remove subscription",
                  notes = "Remove subscription from account. Roles: account/owner, system/admin.",
                  position = 4)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "OK"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "Invalid subscription ID"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @DELETE
    @Path("/{subscriptionId}")
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public void deactivate(@ApiParam(value = "Subscription ID", required = true)
                           @PathParam("subscriptionId") String subscriptionId,
                           @Context SecurityContext securityContext) throws ApiException {
        final Subscription toRemove = subscriptionDao.getById(subscriptionId);
        if (securityContext.isUserInRole("user") && !resolveRolesForSpecificAccount(toRemove.getAccountId()).contains("account/owner")) {
            throw new ForbiddenException("Access denied");
        }
        if (SubscriptionState.INACTIVE == toRemove.getState()) {
            throw new ConflictException("Subscription with id " + subscriptionId + " is inactive already");
        }
        if (!securityContext.isUserInRole("system/admin") && !securityContext.isUserInRole("system/manager")
            && planDao.getPlanById(toRemove.getPlanId()).getSalesOnly()) {
            throw new ForbiddenException("User not authorized to remove this subscription, please contact support");
        }

        LOG.info("Remove subscription# id#{}# userId#{}# accountId#{}#", subscriptionId, EnvironmentContext.getCurrent().getUser().getId(),
                 toRemove.getAccountId());
        subscriptionDao.deactivate(subscriptionId);
        final AbstractSubscriptionService service = registry.get(toRemove.getServiceId());
        service.onRemoveSubscription(toRemove);
    }

    /**
     * Can be used only in methods that is restricted with @RolesAllowed. Require "user" role.
     *
     * @param currentAccountId
     *         account id to resolve roles for
     * @return set of user roles
     */
    private Set<String> resolveRolesForSpecificAccount(String currentAccountId) {
        try {
            final String userId = EnvironmentContext.getCurrent().getUser().getId();
            for (Member membership : accountDao.getByMember(userId)) {
                if (membership.getAccountId().equals(currentAccountId)) {
                    return new HashSet<>(membership.getRoles());
                }
            }
        } catch (ApiException ignored) {
        }
        return Collections.emptySet();
    }

    /**
     * Create {@link SubscriptionDescriptor} from {@link Subscription}.
     * Set with roles should be used if account roles can't be resolved with {@link SecurityContext}
     * (If there is no id of the account in the REST path.)
     *
     * @param subscription
     *         subscription that should be converted to {@link SubscriptionDescriptor}
     * @param resolvedRoles
     *         resolved roles. Do not use if id of the account presents in REST path.
     */
    private SubscriptionDescriptor toDescriptor(Subscription subscription,
                                                final SecurityContext securityContext,
                                                @NotNull final Set<String> resolvedRoles) {
        List<Link> links = new ArrayList<>(2);
        // community subscriptions should not use urls
        final UriBuilder uriBuilder = getServiceContext().getServiceUriBuilder();
        links.add(LinksHelper.createLink(HttpMethod.GET,
                                         uriBuilder.clone()
                                                   .path(getClass(), "getById")
                                                   .build(subscription.getId())
                                                   .toString(),
                                         null,
                                         MediaType.APPLICATION_JSON,
                                         Constants.LINK_REL_GET_SUBSCRIPTION));
        final boolean isUserPrivileged = resolvedRoles.contains("account/owner") || securityContext.isUserInRole("system/admin")
                                         || securityContext.isUserInRole("system/manager");
        if (SubscriptionState.ACTIVE.equals(subscription.getState()) && isUserPrivileged) {
            links.add(LinksHelper.createLink(HttpMethod.DELETE,
                                             uriBuilder.clone()
                                                       .path(getClass(), "deactivate")
                                                       .build(subscription.getId())
                                                       .toString(),
                                             null,
                                             null,
                                             Constants.LINK_REL_DEACTIVATE_SUBSCRIPTION));
        }

        // Do not send with REST properties that starts from 'codenvy:' for all or 'restricted:' for not acc/admins
        Map<String, String> filteredProperties =
                Maps.filterEntries(subscription.getProperties(), new Predicate<Map.Entry<String, String>>() {
                    @Override
                    public boolean apply(Map.Entry<String, String> input) {
                        return isUserPrivileged || !input.getKey().startsWith("restricted:");
                    }
                });


        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        dateFormat.setLenient(false);

        return DtoFactory.getInstance().createDto(SubscriptionDescriptor.class)
                         .withId(subscription.getId())
                         .withAccountId(subscription.getAccountId())
                         .withServiceId(subscription.getServiceId())
                         .withProperties(filteredProperties)
                         .withPlanId(subscription.getPlanId())
                         .withState(subscription.getState())
                         .withDescription(subscription.getDescription())
                         .withStartDate(null == subscription.getStartDate() ? null : dateFormat.format(subscription.getStartDate()))
                         .withEndDate(null == subscription.getEndDate() ? null : dateFormat.format(subscription.getEndDate()))
                         .withUsePaymentSystem(subscription.getUsePaymentSystem())
                         .withBillingStartDate(
                                 null == subscription.getBillingStartDate() ? null : dateFormat.format(subscription.getBillingStartDate()))
                         .withBillingEndDate(
                                 null == subscription.getBillingEndDate() ? null : dateFormat.format(subscription.getBillingEndDate()))
                         .withNextBillingDate(
                                 null == subscription.getNextBillingDate() ? null : dateFormat.format(subscription.getNextBillingDate()))
                         .withBillingCycle(subscription.getBillingCycle())
                         .withBillingCycleType(subscription.getBillingCycleType())
                         .withBillingContractTerm(subscription.getBillingContractTerm())
                         .withLinks(links);
    }

    /**
     * Checks object reference is not {@code null}
     *
     * @param object
     *         object reference to check
     * @param subject
     *         used as subject of exception message "{subject} required"
     * @throws BadRequestException
     *         when object reference is {@code null}
     */
    private void requiredNotNull(Object object, String subject) throws BadRequestException {
        if (object == null) {
            throw new BadRequestException(subject + " required");
        }
    }
}
