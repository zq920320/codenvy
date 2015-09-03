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
package com.codenvy.api.subscription.saas.server.billing.invoice;

import com.codenvy.api.subscription.saas.server.AccountLocker;
import com.codenvy.api.metrics.server.period.MetricPeriod;
import com.codenvy.api.subscription.saas.server.billing.BillingService;
import com.codenvy.api.subscription.saas.shared.dto.Invoice;
import com.codenvy.api.subscription.saas.shared.dto.InvoiceDescriptor;
import com.google.common.annotations.Beta;
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
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.util.LinksHelper;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.User;
import org.eclipse.che.dto.server.DtoFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.codenvy.api.subscription.saas.server.billing.PaymentState.CREDIT_CARD_MISSING;
import static com.codenvy.api.subscription.saas.server.billing.PaymentState.PAYMENT_FAIL;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * @author Sergii Leschenko
 * @author Max Shaposhnik
 */
@Beta
@Path("/invoice")
public class InvoiceService extends Service {
    private final BillingService           billingService;
    private final InvoiceTemplateProcessor invoiceTemplateProcessor;
    private final MetricPeriod             metricPeriod;
    private final AccountDao               accountDao;
    private final AccountLocker            accountLocker;
    private final InvoiceCharger           invoiceCharger;

    @Inject
    public InvoiceService(BillingService billingService,
                          InvoiceTemplateProcessor initializer,
                          MetricPeriod metricPeriod,
                          AccountDao accountDao,
                          AccountLocker accountLocker,
                          InvoiceCharger invoiceCharger) {
        this.billingService = billingService;
        this.invoiceTemplateProcessor = initializer;
        this.metricPeriod = metricPeriod;
        this.accountDao = accountDao;
        this.accountLocker = accountLocker;
        this.invoiceCharger = invoiceCharger;
    }

    /**
     * Searches for invoice with given identifies and returns {@link InvoiceDescriptor} if found.
     *
     * @param invoiceId
     *         invoice identifier
     * @return descriptor of invoice
     * @throws NotFoundException
     *         when invoice with given identifier doesn't exist
     * @throws ServerException
     *         when some error occurred while retrieving invoice
     * @throws ForbiddenException
     *         when user doesn't have permissions to get invoice
     */
    @ApiOperation(value = "Get invoice as json",
                  response = InvoiceDescriptor.class,
                  position = 1)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 403, message = "Access to required invoice is forbidden"),
            @ApiResponse(code = 500, message = "Internal server error")})
    @GET
    @Path("/{invoiceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public InvoiceDescriptor getInvoiceJson(@ApiParam(value = "Invoice ID")
                                            @PathParam("invoiceId") Long invoiceId)
            throws NotFoundException, ServerException, ForbiddenException, BadRequestException {
        requiredNotNull(invoiceId, "Invoice id");
        return toDescriptor(getInvoice(invoiceId));
    }

    /**
     * Searches for invoice with given identifies and returns html representation of it.
     *
     * @param invoiceId
     *         invoice identifier
     * @return html representation of invoice
     * @throws NotFoundException
     *         when invoice with given identifier doesn't exist
     * @throws ServerException
     *         when some error occurred while retrieving invoice
     * @throws ForbiddenException
     *         when user doesn't have permissions to get invoice
     */
    @ApiOperation(value = "Get invoice as html",
                  response = InvoiceDescriptor.class,
                  position = 2)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 403, message = "Access to required invoice is forbidden"),
            @ApiResponse(code = 500, message = "Internal server error")})
    @GET
    @Path("/{invoiceId}")
    @Produces(MediaType.TEXT_HTML)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public Response getInvoiceHtml(@ApiParam(value = "Invoice ID")
                                   @PathParam("invoiceId") final Long invoiceId)
            throws NotFoundException, ServerException, ForbiddenException, BadRequestException {
        requiredNotNull(invoiceId, "Invoice id");
        final Invoice invoice = getInvoice(invoiceId);
        StreamingOutput response = outputStream -> {
            try (PrintWriter w = new PrintWriter(outputStream)) {
                invoiceTemplateProcessor.processTemplate(invoice, w);
            } catch (ServerException | ForbiddenException | NotFoundException e) {
               throw new WebApplicationException(e);
            }
        };
        return Response.ok(response).build();
    }

    /**
     * Charges invoice with given identifier if its payment failed
     *
     * @param invoiceId
     *         invoice identifier
     * @throws NotFoundException
     *         when invoice with given identifier doesn't exist
     * @throws ForbiddenException
     *         when user doesn't have permissions to get invoice
     * @throws ConflictException
     *         when invoice doesn't require charging
     * @throws ServerException
     *         when some error occurred while retrieving invoice
     */
    @ApiOperation(value = "Charge invoice",
                  position = 3)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 403, message = "Access to required invoice is forbidden"),
            @ApiResponse(code = 409, message = "Invoice doesn't require charging"),
            @ApiResponse(code = 500, message = "Internal server error")})
    @POST
    @Path("/{invoiceId}/charge")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public Response charge(@ApiParam(value = "Invoice ID")
                           @PathParam("invoiceId") Long invoiceId)
            throws ServerException, NotFoundException, ForbiddenException, ConflictException, BadRequestException {
        requiredNotNull(invoiceId, "Invoice id");

        final Invoice invoice = getInvoice(invoiceId);
        if (!CREDIT_CARD_MISSING.getState().equals(invoice.getPaymentState())
            && !PAYMENT_FAIL.getState().equals(invoice.getPaymentState())) {

            throw new ConflictException("Payment is not required for invoice with id " + invoiceId);
        }

        invoiceCharger.charge(invoice);
        accountLocker.removePaymentLock(invoice.getAccountId());
        return Response.status(204).build();
    }

    /**
     * Returns list of {@link InvoiceDescriptor} based on {@code accountId, maxItems, skipCount, startPeriod, endPeriod}.
     * NOTE: {@code AccountId} is required for all user except of <i>system/admin</i> and <i>system/manager</i>
     */
    @ApiOperation(value = "Get invoices",
                  response = InvoiceDescriptor.class,
                  responseContainer = "List",
                  position = 4)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 403, message = "Access to required invoices is forbidden"),
            @ApiResponse(code = 500, message = "Internal server error")})
    @GET
    @Path("/find")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public List<InvoiceDescriptor> getInvoices(@ApiParam(value = "Account ID", required = false)
                                               @QueryParam("accountId") String accountId,
                                               @ApiParam(value = "Max items count", required = false)
                                               @DefaultValue("30") @QueryParam("maxItems") int maxItems,
                                               @ApiParam(value = "Skip count", required = false)
                                               @DefaultValue("0") @QueryParam("skipCount") int skipCount,
                                               @ApiParam(value = "Start period in milliseconds from epoch time", required = false)
                                               @QueryParam("startPeriod") Long startPeriod,
                                               @ApiParam(value = "End period in milliseconds from epoch time", required = false)
                                               @QueryParam("endPeriod") Long endPeriod)
            throws ForbiddenException, ServerException, BadRequestException {
        final User currentUser = EnvironmentContext.getCurrent().getUser();
        final boolean isAdmin = currentUser.isMemberOf("system/admin") || currentUser.isMemberOf("system/manager");
        if (!isAdmin) {
            if (accountId == null) {
                throw new BadRequestException("Missed value of account id parameter");
            } else if (!resolveRolesForSpecificAccount(accountId).contains("account/owner")) {
                throw new ForbiddenException("Access denied");
            }
        }

        InvoiceFilter filter = InvoiceFilter.builder()
                                            .withAccountId(accountId)
                                            .withMaxItems(maxItems)
                                            .withSkipCount(skipCount)
                                            .withFromDate(startPeriod)
                                            .withTillDate(endPeriod)
                                            .build();

        return billingService.getInvoices(filter).stream().map(this::toDescriptor).collect(Collectors.toList());
    }

    /**
     * Generate invoices for given billing period.
     * If bounds of period are missed then will be use bounds of previous billing period
     *
     * @param startPeriod
     *         start of billing period
     * @param endPeriod
     *         start of billing period
     * @return count of generated invoices
     * @throws ServerException
     *         when some error occurred while generating of invoices
     */
    @ApiOperation(value = "Generate invoices",
                  notes = "Generate invoices. Roles: system/manager, system/admin.",
                  response = String.class,
                  position = 5)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal server error")})
    @POST
    @Path("/generate")
    @Produces({TEXT_PLAIN})
    @RolesAllowed({"system/admin", "system/manager"})
    public String generateInvoices(@ApiParam(value = "Start of billing period")
                                   @QueryParam("startPeriod") Long startPeriod,
                                   @ApiParam(value = "End of billing period")
                                   @QueryParam("endPeriod") Long endPeriod) throws ServerException {
        if (startPeriod == null) {
            startPeriod = metricPeriod.getCurrent().getPreviousPeriod().getStartDate().getTime();
        }

        if (endPeriod == null) {
            endPeriod = metricPeriod.getCurrent().getPreviousPeriod().getEndDate().getTime();
        }

        return String.valueOf(billingService.generateInvoices(startPeriod, endPeriod));
    }

    private Invoice getInvoice(long invoiceId) throws ServerException,
                                                      NotFoundException,
                                                      ForbiddenException {
        Invoice invoice = billingService.getInvoice(invoiceId);
        Set<String> roles = resolveRolesForSpecificAccount(invoice.getAccountId());
        final User currentUser = EnvironmentContext.getCurrent().getUser();
        final boolean isAdmin = currentUser.isMemberOf("system/admin") || currentUser.isMemberOf("system/manager");
        if (!isAdmin && !roles.contains("account/owner")) {
            throw new ForbiddenException("Access denied");
        }
        return invoice;
    }

    private InvoiceDescriptor toDescriptor(Invoice invoice) {
        return DtoFactory.getInstance().createDto(InvoiceDescriptor.class).withId(invoice.getId())
                         .withAccountId(invoice.getAccountId())
                         .withCreationDate(invoice.getCreationDate())
                         .withFromDate(invoice.getFromDate())
                         .withTillDate(invoice.getTillDate())
                         .withCharges(invoice.getCharges())
                         .withCreditCardId(invoice.getCreditCardId())
                         .withMailingDate(invoice.getMailingDate())
                         .withPaymentDate(invoice.getPaymentDate())
                         .withPaymentState(invoice.getPaymentState())
                         .withTotal(invoice.getTotal())
                         .withLinks(generateLinks(invoice));
    }

    private List<Link> generateLinks(Invoice invoice) {
        final UriBuilder uriBuilder = getServiceContext().getServiceUriBuilder();
        final Link jsonLink = LinksHelper.createLink(HttpMethod.GET,
                                                     uriBuilder.clone()
                                                               .path(getClass(), "getInvoiceJson")
                                                               .build(invoice.getId())
                                                               .toString(),
                                                     MediaType.APPLICATION_JSON,
                                                     null,
                                                     "self");
        final Link httpLink = LinksHelper.createLink(HttpMethod.GET,
                                                     uriBuilder.clone()
                                                               .path(getClass(), "getInvoiceHtml")
                                                               .build(invoice.getId())
                                                               .toString(),
                                                     MediaType.TEXT_HTML,
                                                     null,
                                                     "html view");
        return Arrays.asList(jsonLink, httpLink);
    }

    /**
     * Can be used only in methods that is restricted with @RolesAllowed. Require "user" role.
     *
     * @param accountId
     *         account id to resolve roles for
     * @return set of user roles
     */
    private Set<String> resolveRolesForSpecificAccount(String accountId) {
        try {
            final String userId = EnvironmentContext.getCurrent().getUser().getId();
            for (Member membership : accountDao.getByMember(userId)) {
                if (membership.getAccountId().equals(accountId)) {
                    return new HashSet<>(membership.getRoles());
                }
            }
        } catch (ApiException ignored) {
        }
        return Collections.emptySet();
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
